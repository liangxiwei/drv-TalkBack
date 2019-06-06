package com.luobin.dvr.grafika;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import me.lake.librestreaming.client.RESClient;

/**
 * Object that encapsulates the encoder thread.
 * <p>
 * We want to sleep until there's work to do.  We don't actually know when a new frame
 * arrives at the encoder, because the other thread is sending frames directly to the
 * input surface.  We will see data appear at the decoder output, so we can either use
 * an infinite timeout on dequeueOutputBuffer() or wait() on an object and require the
 * calling app wake us.  It's very useful to have all of the buffer management local to
 * this thread -- avoids synchronization -- so we want to do the file muxing in here.
 * So, it's best to sleep on an object and do something appropriate when awakened.
 * <p>
 * This class does not manage the MediaCodec encoder startup/shutdown.  The encoder
 * should be fully started before the thread is created, and not shut down until this
 * thread has been joined.
 */
public class EncoderThread extends Thread {
    private static final String TAG = com.luobin.dvr.DvrService.TAG;
    private static final boolean VERBOSE = false;

    private MediaCodec mEncoder;
    private MediaFormat mEncodedFormat = null;
    private MediaCodec.BufferInfo mBufferInfo;

    private EncoderHandler mHandler;

    private final Object mLock = new Object();
    private volatile boolean mReady = false;
    private boolean isDvr = false;

    public EncoderThread(MediaCodec mediaCodec, boolean isDvr) {
        mEncoder = mediaCodec;
        this.isDvr = isDvr;
        mBufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * Thread entry point.
     * <p>
     * Prepares the Looper, Handler, and signals anybody watching that we're ready to go.
     */
    @Override
    public void run() {
        Looper.prepare();
        mHandler = new EncoderHandler(this);    // must create on encoder thread
        Log.d(TAG, "encoder thread ready");
        synchronized (mLock) {
            mReady = true;
            mLock.notify();    // signal waitUntilReady()
        }

        Looper.loop();

        synchronized (mLock) {
            mReady = false;
            mHandler = null;
        }
        Log.d(TAG, "looper quit");
    }

    /**
     * Waits until the encoder thread is ready to receive messages.
     * <p>
     * Call from non-encoder thread.
     */
    public void waitUntilReady() {
        synchronized (mLock) {
            while (!mReady) {
                try {
                    mLock.wait();
                } catch (InterruptedException ie) { /* not expected */ }
            }
        }
    }

    public MediaFormat getFormat() {
        return mEncodedFormat;
    }

    /**
     * Returns the Handler used to send messages to the encoder thread.
     */
    public EncoderHandler getHandler() {
        synchronized (mLock) {
            // Confirm ready state.
            if (!mReady) {
                throw new RuntimeException("not ready");
            }
        }
        return mHandler;
    }

    /**
     * Drains all pending output from the decoder, and adds it to the circular buffer.
     */
    public void drainEncoder() {
        final int TIMEOUT_USEC = 0;     // no timeout -- check for buffers, fail if none

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Should happen before receiving buffers, and should only happen once.
                // The MediaFormat contains the csd-0 and csd-1 keys, which we'll need
                // for MediaMuxer.  It's unclear what else MediaMuxer might want, so
                // rather than extract the codec-specific data and reconstruct a new
                // MediaFormat later, we just grab it here and keep it around.
                mEncodedFormat = mEncoder.getOutputFormat();

                if (mEncodedFormat.getString(MediaFormat.KEY_MIME).equals("video/avc") && !isDvr) {
                    RESClient.getInstance().setDrvFormat(mEncodedFormat);
                    RESClient.getInstance().sendAVCDecoderConfigurationRecord(0, mEncodedFormat);
                }
                Log.d(TAG, "encoder output format changed: " + mEncodedFormat);
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out when we got the
                    // INFO_OUTPUT_FORMAT_CHANGED status.  The MediaMuxer won't accept
                    // a single big blob -- it wants separate csd-0/csd-1 chunks --
                    // so simply saving this off won't work.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    handleEncodedData(encodedData, mBufferInfo);

                    if (VERBOSE) {
                        if (mEncodedFormat.getString(MediaFormat.KEY_MIME).equals("video/avc")) {
                            Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                    mBufferInfo.presentationTimeUs);
                        } else {
                            Log.d(TAG, "sent voice " + mBufferInfo.size + " bytes to muxer, ts=" +
                                    mBufferInfo.presentationTimeUs);
                        }

                    }
                }

                try {
                    mEncoder.releaseOutputBuffer(encoderStatus, false);
                } catch (Throwable e){
                    e.printStackTrace();
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.w(TAG, "reached end of stream unexpectedly");
                    break;      // out of while
                }
            }
        }
    }

    /**
     * Drains the encoder output.
     * <p>
     * See notes for {@link DvrEncoder#frameAvailableSoon()}.
     */
    void frameAvailableSoon() {
        if (VERBOSE) Log.d(TAG, "frameAvailableSoon");
        drainEncoder();
    }

    /**
     * Tells the Looper to quit.
     */
    void shutdown() {
        Log.d(TAG, "Encoder thread shutdown");
        Looper.myLooper().quit();
    }

    public void handleEncodedData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
//        synchronized(mEncBuffer) {
//            mEncBuffer.add(encodedData, VIDEO_TRACK, mBufferInfo.flags,
//                    mBufferInfo.presentationTimeUs);
//        }
    }

    ;

    /**
     * Handler for EncoderThread.  Used for messages sent from the UI thread (or whatever
     * is driving the encoder) to the encoder thread.
     * <p>
     * The object is created on the encoder thread.
     */
    public class EncoderHandler extends Handler {
        public static final int MSG_FRAME_AVAILABLE_SOON = 1;
        public static final int MSG_SHUTDOWN = 2;

        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<EncoderThread> mWeakEncoderThread;

        /**
         * Constructor.  Instantiate object from encoder thread.
         */
        public EncoderHandler(EncoderThread et) {
            mWeakEncoderThread = new WeakReference<EncoderThread>(et);
        }

        @Override  // runs on encoder thread
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (false) {
                Log.v(TAG, "EncoderHandler: what=" + what);
            }

            EncoderThread encoderThread = mWeakEncoderThread.get();
            if (encoderThread == null) {
                Log.w(TAG, "EncoderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_FRAME_AVAILABLE_SOON:
                    encoderThread.frameAvailableSoon();
                    break;
                case MSG_SHUTDOWN:
                    encoderThread.shutdown();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }
}