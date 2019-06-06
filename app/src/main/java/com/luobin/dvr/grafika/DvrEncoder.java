/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luobin.dvr.grafika;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.example.jrd48.GlobalStatus;
import com.luobin.dvr.DvrService;
import com.luobin.voice.DefaultSetting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import me.lake.librestreaming.client.RESClient;

/**
 * Encodes video in a fixed-size circular buffer.
 * <p>
 * The obvious way to do this would be to store each packet in its own buffer and hook it
 * into a linked list.  The trouble with this approach is that it requires constant
 * allocation, which means we'll be driving the GC to distraction as the frame rate and
 * bit rate increase.  Instead we create fixed-size pools for video data and metadata,
 * which requires a bit more work for us but avoids allocations in the steady state.
 * <p>
 * Video must always start with a sync frame (a/k/a key frame, a/k/a I-frame).  When the
 * circular buffer wraps around, we either need to delete all of the data between the frame at
 * the head of the list and the next sync frame, or have the file save function know that
 * it needs to scan forward for a sync frame before it can start saving data.
 * <p>
 * When we're told to save a snapshot, we create a MediaMuxer, write all the frames out,
 * and then go back to what we were doing.
 */
public class DvrEncoder {
    private static final String TAG = com.luobin.dvr.DvrService.TAG;
    private static final boolean VERBOSE = false;

    private static final String VIDEO_MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int IFRAME_INTERVAL = 1;//2;           // sync frame every second

    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm"; // audio format
    private static final int SAMPLE_RATE = 44100;

    private static final int VIDEO_TRACK = 0;
    private static final int AUDIO_TRACK = 1;

    private EncoderThread mVideoEncoderThread = null;
    private EncoderThread mAudioEncoderThread = null;
    private CircularEncoderBuffer encBuffer;
    private SavingFileThread mSavingThread = null;
    private Surface mInputSurface;
    private MediaCodec mVideoEncoder;
    private MediaCodec mAudioEncoder;
    /**
     * Configures encoder, and prepares the input Surface.
     *
     * @param width          Width of encoded video, in pixels.  Should be a multiple of 16.
     * @param height         Height of encoded video, in pixels.  Usually a multiple of 16 (1080 is ok).
     * @param bitRate        Target bit rate, in bits.
     * @param frameRate      Expected frame rate.
     * @param desiredSpanSec How many seconds of video we want to have in our buffer at any time.
     */
    public DvrEncoder(int width, int height, int bitRate, int frameRate,
                      int desiredSpanSec, boolean audioEnabled, boolean isOpen) throws IOException {
        // The goal is to size the buffer so that we can accumulate N seconds worth of video,
        // where N is passed in as "desiredSpanSec".  If the codec generates data at roughly
        // the requested bit rate, we can compute it as time * bitRate / bitsPerByte.
        //
        // Sync frames will appear every (frameRate * IFRAME_INTERVAL) frames.  If the frame
        // rate is higher or lower than expected, various calculations may not work out right.
        //
        // Since we have to start muxing from a sync frame, we want to ensure that there's
        // room for at least one full GOP in the buffer, preferrably two.
        Log.d(TAG, "DvrEncoder create width=" + width + ", height=" + height + ", bitRate=" + bitRate +
                ", frameRate=" + frameRate + ", desiredSpanSec=" + desiredSpanSec + ", audioEnabled=" + audioEnabled);
        if (desiredSpanSec < IFRAME_INTERVAL * 2) {
            throw new RuntimeException("Requested time span is too short: " + desiredSpanSec +
                    " vs. " + (IFRAME_INTERVAL * 2));
        } else if (desiredSpanSec > 7) {
            throw new RuntimeException("Requested time span is too long: " + desiredSpanSec +
                    " vs. 7");
        }
        encBuffer = new CircularEncoderBuffer(bitRate, frameRate,
                desiredSpanSec);

        MediaFormat videoformat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        videoformat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoformat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        videoformat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        videoformat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        Log.d(TAG, "format: " + videoformat);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        mVideoEncoder.configure(videoformat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();

        // Start the encoder thread last.  That way we're sure it can see all of the state
        // we've initialized.
        mVideoEncoderThread = new EncoderThread(mVideoEncoder, true) {

            @Override
            public void handleEncodedData(ByteBuffer encodedData,
                                          BufferInfo bufferInfo) {
                synchronized (encBuffer) {
                    encBuffer.add(encodedData, VIDEO_TRACK, bufferInfo.flags,
                            bufferInfo.presentationTimeUs);
//                    if(!RESClient.getInstance().getSelf_video() && RESClient.getInstance().getStatus() == RESClient.STATUS_SUCCESS) {
//                        RESClient.getInstance().pushData(encodedData, bufferInfo);
//                    }
                }
            }
        };
        mVideoEncoderThread.start();
        mVideoEncoderThread.waitUntilReady();

        if (audioEnabled) {
            MediaFormat audioformat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, SAMPLE_RATE, 1);
            audioformat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);
            audioformat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioformat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
            audioformat.setInteger(MediaFormat.KEY_BIT_RATE, DefaultSetting.bitrate);
            audioformat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
            audioformat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 163840);
            Log.d(TAG, "audio format: " + audioformat);

            mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
            mAudioEncoder.configure(audioformat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioEncoder.start();

            mAudioEncoderThread = new EncoderThread(mAudioEncoder, true) {

                @Override
                public void handleEncodedData(ByteBuffer encodedData,
                                              BufferInfo bufferInfo) {
                    synchronized (encBuffer) {
                        encBuffer.add(encodedData, AUDIO_TRACK, bufferInfo.flags,
                                bufferInfo.presentationTimeUs);
//                        Log.i(TAG,"RESClient.getInstance().isTransferVoice():" + RESClient.getInstance().isTransferVoice());
                        if (RESClient.getInstance().isTransferVoice()) {
                            try {
                                RESClient.getInstance().pushVoiceData(encodedData, bufferInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            };
            mAudioEncoderThread.start();
            mAudioEncoderThread.waitUntilReady();
        }

        mSavingThread = new SavingFileThread(encBuffer, mVideoEncoderThread, mAudioEncoderThread);
        mSavingThread.start();
        mSavingThread.waitUntilReady();
    }

    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    /**
     * Shuts down the encoder thread, and releases encoder resources.
     * <p>
     * Does not return until the encoder thread has stopped.
     */
    public void shutdown() {
        Log.d(TAG, "releasing encoder objects");

        Handler handler = mVideoEncoderThread.getHandler();
        handler.sendMessage(handler.obtainMessage(EncoderThread.EncoderHandler.MSG_SHUTDOWN));

        if (mAudioEncoderThread != null) {
            handler = mAudioEncoderThread.getHandler();
            handler.sendMessage(handler.obtainMessage(EncoderThread.EncoderHandler.MSG_SHUTDOWN));
        }

        handler = mSavingThread.getHandler();
        handler.sendEmptyMessage(SavingFileThread.SavingHandler.MSG_SHUTDOWN);

        try {
            mVideoEncoderThread.join();
            if (mAudioEncoderThread != null) {
                mAudioEncoderThread.join();
            }
            mSavingThread.join();
        } catch (InterruptedException ie) {
            Log.w(TAG, "Encoder thread join() was interrupted", ie);
        }

        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }

        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }
    }

    /**
     * Notifies the encoder thread that a new frame will shortly be provided to the encoder.
     * <p>
     * There may or may not yet be data available from the encoder output.  The encoder
     * has a fair mount of latency due to processing, and it may want to accumulate a
     * few additional buffers before producing output.  We just need to drain it regularly
     * to avoid a situation where the producer gets wedged up because there's no room for
     * additional frames.
     * <p>
     * If the caller sends the frame and then notifies us, it could get wedged up.  If it
     * notifies us first and then sends the frame, we guarantee that the output buffers
     * were emptied, and it will be impossible for a single additional frame to block
     * indefinitely.
     */
    public void frameAvailableSoon() {
        Handler handler = mVideoEncoderThread.getHandler();
        handler.sendMessage(handler.obtainMessage(
                EncoderThread.EncoderHandler.MSG_FRAME_AVAILABLE_SOON));
    }

    public void audioAvailable(byte[] buf, int len) {
        if (mAudioEncoder == null) {
            Log.w(TAG, "audioAvailable called when mAudioEncorder = null");
            return;
        }

        long presentationTimeUs = (System.nanoTime()) / 1000;
//        Log.d(TAG,"presentationTimeUs--"+presentationTimeUs);

        try {
            ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
            int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(-1);
//            Log.d(TAG,"inputBufferIndex--"+inputBufferIndex);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buf, 0, len);

                mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, len, presentationTimeUs, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Handler handler = mAudioEncoderThread.getHandler();
        handler.sendMessage(handler.obtainMessage(
                EncoderThread.EncoderHandler.MSG_FRAME_AVAILABLE_SOON));
    }

    /**
     * Initiates saving the currently-buffered frames to the specified output file.  The
     * data will be written as a .mp4 file.  The call returns immediately.  When the file
     * save completes, the callback will be notified.
     * <p>
     * The file generation is performed on the encoder thread, which means we won't be
     * draining the output buffers while this runs.  It would be wise to stop submitting
     * frames during this time.
     */
    public void saveFile(String outputFile, boolean withBufferedVideo) {
        Handler handler = mSavingThread.getHandler();
        handler.sendMessage(handler.obtainMessage(
                SavingFileThread.SavingHandler.MSG_SAVE_FILE, withBufferedVideo ? 1 : 0, 0, outputFile));
    }

    public void stopRecord() {
        Handler handler = mSavingThread.getHandler();
        handler.sendMessage(handler.obtainMessage(
                SavingFileThread.SavingHandler.MSG_STOP));
    }

    public void setLockNow(boolean lockNow) {
        if(mSavingThread != null) {
            mSavingThread.setLockNow(lockNow);
        }
    }


    private static class SavingFileThread extends Thread {

        private SavingHandler mHandler;
        private CircularEncoderBuffer mBuffer;
        private EncoderThread mVideoEncoderThread, mAudioEncoderThread;
        private int mBufIndex = -1;

        private final Object mLock = new Object();
        private volatile boolean mReady = false;

        private String mNextFile = null;
        private String mOutputFile = null;
        private boolean mWithBufferedVideo = false;
        private MediaMuxer mMuxer = null;
        private int mVideoTrack = -1;
        private int mAudioTrack = -1;

        private long mAudioLastTimestampUs;
        private long mVideoLastTimestampUs;
        private long mStartTimestampUs;
        private int lastFlag = 0;
        private boolean lcokNow = false;
        private CircularEncoderBuffer.Listener mBufferListener = new CircularEncoderBuffer.Listener() {

            @Override
            public void onBufferAdded() {
                //Log.d(TAG, "onBufferAdded");
                if (mHandler == null) {
                    Log.e(TAG, "onBufferAdded mHandler == null");
                    return;
                }
                mHandler.sendEmptyMessage(SavingHandler.MSG_BUF_ADDED);
            }
        };


        private static class SavingHandler extends Handler {

            private static final int MSG_SAVE_FILE = 1;
            private static final int MSG_BUF_ADDED = 2;
            private static final int MSG_STOP = 3;
            private static final int MSG_SHUTDOWN = 4;

            private SavingFileThread mSavingThread;

            public SavingHandler(SavingFileThread p) {
                mSavingThread = p;
            }

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SAVE_FILE) {
                    mSavingThread.saveFile((String) msg.obj, msg.arg1 > 0);
                } else if (msg.what == MSG_BUF_ADDED) {
                    mSavingThread.onBufferAdded();
                } else if (msg.what == MSG_STOP) {
                    mSavingThread.stopRecording();
                } else if (msg.what == MSG_SHUTDOWN) {
                    mSavingThread.shutdown();
                }
            }

        }

        ;


        public SavingFileThread(CircularEncoderBuffer buf,
                                EncoderThread videoThread, EncoderThread audioThread) {
            mBuffer = buf;
            mVideoEncoderThread = videoThread;
            mAudioEncoderThread = audioThread;
        }

        @Override
        public void run() {
            mBuffer.setListener(mBufferListener);
            Looper.prepare();
            mHandler = new SavingHandler(this);    // must create on encoder thread
            Log.d(TAG, "saving thread ready");
            synchronized (mLock) {
                mReady = true;
                mLock.notify();    // signal waitUntilReady()
            }

            Looper.loop();

            synchronized (mLock) {
                mReady = false;
                mHandler = null;
            }
            Log.d(TAG, "saving thread looper quit");
        }

        void shutdown() {
            Log.d(TAG, "saving thread shutdown");
            stopRecording();
            Looper.myLooper().quit();
        }

        public void waitUntilReady() {
            synchronized (mLock) {
                while (!mReady) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        public SavingHandler getHandler() {
            synchronized (mLock) {
                // Confirm ready state.
                if (!mReady) {
                    throw new RuntimeException("not ready");
                }
            }
            return mHandler;
        }

        private void setLockNow(boolean lockNow){
            this.lcokNow = lockNow;
        }

        private void saveFile(String file, boolean withBufferedVideo) {
            synchronized (this) {
                File foder = new File(file);
                if (!foder.getParentFile().exists()) {
                    mkDir(foder.getParentFile());
                    Log.d(TAG, "saveFile mkdir()" + foder.getParentFile());
                }
                /*if (!foder.exists()) {
					try {
						foder.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d(TAG, "saveFile createNewFile()" + foder.getAbsolutePath());
				}*/
                lastFlag = mBuffer.getCurrentIndex();
                Log.d(TAG, "lastFlag =" + lastFlag);
                mNextFile = new String(file);
                mWithBufferedVideo = withBufferedVideo;
                Log.d(TAG, "saveFile " + file + " withBufferedVideo=" + mWithBufferedVideo);
            }
        }

        private void mkDir(File file) {
            if (file.getParentFile().exists()) {
                file.mkdir();
            } else {
                mkDir(file.getParentFile());
                file.mkdir();
            }
        }

        private void onBufferAdded() {
            synchronized (this) {
                boolean newfile = false;
                if (mOutputFile == null && mNextFile == null) {
                    return;
                }

                if (mVideoEncoderThread.getFormat() == null) {
                    Log.w(TAG, "null video format when buffer added");
                    return;
                }

                if (mAudioEncoderThread == null) {
                    // Log.w(TAG, "SavingFileThread without audio");
                } else if (mAudioEncoderThread.getFormat() == null) {
                    Log.w(TAG, "null audio format when buffer added");
                    return;
                }
                // Log.d(TAG, "onBufferAdded moutputFile="+mOutputFile+" mNextFile="+mNextFile);
                if (mOutputFile == null && mNextFile != null) {
                    Log.d(TAG, "mOutputFile == null && mNextFile != null");
                    mOutputFile = mNextFile;
                    mNextFile = null;
                    newfile = true;
                    try {
                        mMuxer = new MediaMuxer(mOutputFile,
                                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return;
                    }
                    mStartTimestampUs = 0;
                    mVideoTrack = mMuxer.addTrack(mVideoEncoderThread.getFormat());
                    Log.d(TAG, "create mVideoTrack " + mVideoTrack);
                    mVideoLastTimestampUs = 0;
                    if (mAudioEncoderThread != null) {
                        mAudioTrack = mMuxer.addTrack(mAudioEncoderThread.getFormat());
                        Log.d(TAG, "create mAudioTrack " + mAudioTrack);
                        mAudioLastTimestampUs = 0;
                    }
                    mMuxer.start();
                } else if (mOutputFile != null && mNextFile != null) {
                    Log.d(TAG, "-->mOutputFile != null && mNextFile != null");
                    try {
                        if (mMuxer != null) {
                            mMuxer.stop();
                            mMuxer.release();
                            mMuxer = null;
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    if(lcokNow) {
                        lcokNow = false;
                        File file = new File(mOutputFile);
                        if (file.exists()) {
                            boolean isSuccess = file.renameTo(new File(mOutputFile.replace(".mp4", DvrService.LOCK + ".mp4")));
                            Log.d(TAG, "==== rename ==== " + isSuccess);
                        }
                    }
                    mOutputFile = null;
                    mHandler.sendEmptyMessage(SavingHandler.MSG_BUF_ADDED);
                    return;
                }
                synchronized (mBuffer) {
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    do {
                        if (mBufIndex < 0 || newfile) {
                            if (mWithBufferedVideo) {
                                mBufIndex = mBuffer.getFirstIndex();
                                Log.d(TAG, "got first index " + mBufIndex);
                            } else {
                                mBufIndex = mBuffer.getLastIndex(lastFlag);
                                if (mBufIndex < 0) {
                                    mBufIndex = mBuffer.getFirstIndex();
                                }
                                Log.d(TAG, "got last index " + mBufIndex);
                            }
                            newfile = false;
                            if (mBufIndex < 0) break;
                        } else {
                            int index = mBuffer.getNextIndex(mBufIndex);
                            if (index < 0) break;
                            mBufIndex = index;
                        }
                        ByteBuffer buf = mBuffer.getChunk(mBufIndex, info);
                        int track = mBuffer.getTrack(mBufIndex);
                        if (VERBOSE) {
                            Log.d(TAG, "SAVE " + mBufIndex + " flags=0x" + Integer.toHexString(info.flags)
                                    + (track == VIDEO_TRACK ? " VIDEO" : " AUDIO") + " " + info.presentationTimeUs);
                        }
                        if (track == VIDEO_TRACK && mVideoTrack >= 0) {
                            if (mStartTimestampUs == 0) {
                                mStartTimestampUs = info.presentationTimeUs;
                            }
                            if (mVideoLastTimestampUs < info.presentationTimeUs) {
                                mVideoLastTimestampUs = info.presentationTimeUs;
                                if (buf != null && info != null) {
                                    mMuxer.writeSampleData(mVideoTrack, buf, info);
                                }
                            }
                        } else if (track == AUDIO_TRACK && mAudioTrack >= 0) {
                            if (mAudioLastTimestampUs < info.presentationTimeUs) {
                                mAudioLastTimestampUs = info.presentationTimeUs;
                                if (buf != null && info != null) {
                                    mMuxer.writeSampleData(mAudioTrack, buf, info);
                                }
                            }
                        } else {
                            GlobalStatus.findError("invalid track : " + track);
//                            throw new RuntimeException("invalid track : " + track);
                        }
                    } while (true);
                }
            }
        }

        private void stopRecording() {
            Log.d(TAG, "stopRecording");
            try {
                if (mMuxer != null) {
                    mMuxer.stop();
                    mMuxer.release();
                    mMuxer = null;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            if(mOutputFile != null){
                if(lcokNow) {
                    lcokNow = false;
                    File file = new File(mOutputFile);
                    if (file.exists()) {
                        boolean isSuccess = file.renameTo(new File(mOutputFile.replace(".mp4", DvrService.LOCK + ".mp4")));
                        Log.d(TAG, "==== rename ==== " + isSuccess);
                    }
                }
            }
            Log.d(TAG, "mStartTimestampUs=" + mStartTimestampUs + "  && mOutputFile ");
            if (mStartTimestampUs > 0 && mOutputFile != null) {
                try {
                    FileWriter fw = new FileWriter(mOutputFile, true);
                    DecimalFormat df = new DecimalFormat("000000000000000");
                    fw.write("STUS:" + df.format(mStartTimestampUs));
                    fw.flush();
                    fw.close();
                    Log.d(TAG, "write start time stampus:" + df.format(mStartTimestampUs));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mStartTimestampUs = 0;
            }
            mOutputFile = null;
            mNextFile = null;
        }
    }
}
