package me.lake.librestreaming.client;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.luobin.dvr.grafika.EncoderThread;
import com.luobin.dvr.grafika.gles.EglCore;
import com.luobin.dvr.grafika.gles.FullFrameRect;
import com.luobin.dvr.grafika.gles.Texture2dProgram;
import com.luobin.dvr.grafika.gles.WindowSurface;

import java.io.IOException;
import java.nio.ByteBuffer;

import me.lake.librestreaming.model.RESConfig;

/**
 * Created by Administrator on 2017/9/13.
 */

public class GlobalMediaCodec {
    private static final String TAG = "GlobalMediaCodec";
    public static final String VIDEO_MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private final float[] mTmpMatrix = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f};
    private MediaCodec mVideoEncoder;
    private EncoderThread mVideoEncoderThread = null;
    private Surface mInputSurface;
    private final Object mLock = new Object();
    private boolean isStarted = false;

    private WindowSurface mEncoderSurface;
    private FullFrameRect mUsbFrameBlit;
    public GlobalMediaCodec() {

    }

    public void start(EglCore mEglCore) {
        if (mVideoEncoder == null) {
            MediaFormat videoformat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, RESConfig.VIDEO_WIDTH, RESConfig.VIDEO_HEIGHT);

            // Set some properties.  Failing to specify some of these can cause the MediaCodec
            // configure() call to throw an unhelpful exception.
            videoformat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoformat.setInteger(MediaFormat.KEY_BIT_RATE, RESConfig.BITRATE);
            videoformat.setInteger(MediaFormat.KEY_FRAME_RATE, RESConfig.FPS);
            videoformat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            Log.d(TAG, "format: " + videoformat);
            try {
                mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
                mVideoEncoder.configure(videoformat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mInputSurface = mVideoEncoder.createInputSurface();
                mVideoEncoder.start();


                mEncoderSurface = new WindowSurface(mEglCore, getmInputSurface(), true);
                mEncoderSurface.makeCurrent();
                Log.e("wsDvr","mEncoderSurface.makeCurrent();");
                mUsbFrameBlit = new FullFrameRect(
                        new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D));
                Log.e("wsDvr","mUsbFrameBlit create");
                // Start the encoder thread last.  That way we're sure it can see all of the state
                // we've initialized.
                mVideoEncoderThread = new EncoderThread(mVideoEncoder, false) {

                    @Override
                    public void handleEncodedData(ByteBuffer encodedData,
                                                  MediaCodec.BufferInfo bufferInfo) {
                        synchronized (mLock) {
                            RESClient.getInstance().pushData(mVideoEncoder,encodedData, bufferInfo);
                        }
                    }
                };
                mVideoEncoderThread.start();
                mVideoEncoderThread.waitUntilReady();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isStarted = true;
    }

    public void frameAvailableSoon() {
//        Log.e(TAG,"frameAvailableSoon");
        if (mVideoEncoderThread != null) {
            Handler handler = mVideoEncoderThread.getHandler();
            handler.sendMessage(handler.obtainMessage(
                    EncoderThread.EncoderHandler.MSG_FRAME_AVAILABLE_SOON));
        }
    }

    public void shutdown() {
        isStarted = false;
        if (mVideoEncoderThread != null) {
            Log.d(TAG, "releasing encoder objects");
            try {
                Handler handler = mVideoEncoderThread.getHandler();
                handler.sendMessage(handler.obtainMessage(EncoderThread.EncoderHandler.MSG_SHUTDOWN));
                mVideoEncoderThread.join();
            } catch (Exception ie) {
                Log.w(TAG, "Encoder thread join() was interrupted", ie);
            }
        }

        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }

        if(mEncoderSurface != null){
            mEncoderSurface.release();
            mEncoderSurface = null;
        }

        if(mUsbFrameBlit != null){
            mUsbFrameBlit.release(true);
        }
    }

    public Surface getmInputSurface() {
        return mInputSurface;
    }

    public void setmInputSurface(Surface mInputSurface) {
        this.mInputSurface = mInputSurface;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public WindowSurface getmEncoderSurface(){
        return mEncoderSurface;
    }

    public void drawFrame(int mUsbTextureId){
        if(isStarted) {
            try {
                mEncoderSurface.makeCurrent();
                int mCamPrevWidth = RESConfig.VIDEO_WIDTH;
                int mCamPrevHeight = RESConfig.VIDEO_HEIGHT;
                if (mUsbTextureId >= 0) {
                    GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                    mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                }
                frameAvailableSoon();
                long presentationTimeUs = System.nanoTime();
                mEncoderSurface.setPresentationTime(presentationTimeUs);
                mEncoderSurface.swapBuffers();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void drawFrame(FullFrameRect mFullFrameRect,int mUsbTextureId,float[] mTmpMatrix){
        //Log.e(TAG,"drawFrame isStarted="+isStarted);
        if(isStarted) {
            try {
                mEncoderSurface.makeCurrent();
                int mCamPrevWidth = RESConfig.VIDEO_WIDTH;
                int mCamPrevHeight = RESConfig.VIDEO_HEIGHT;
                if (mUsbTextureId >= 0) {
                    GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                    mFullFrameRect.drawFrame(mUsbTextureId, mTmpMatrix);
                }
                frameAvailableSoon();
                long presentationTimeUs = System.nanoTime();
                mEncoderSurface.setPresentationTime(presentationTimeUs);
                mEncoderSurface.swapBuffers();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
