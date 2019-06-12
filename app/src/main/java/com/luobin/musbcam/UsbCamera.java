package com.luobin.musbcam;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by xugq on 8/2/17.
 */

public class UsbCamera {
    private static final String TAG = "DvrService:UsbCam";
    private String mFrameFmt;
    private PictureCallback callback;
    private String mDev;
    private int mReqSize[];
    private int mRealSize[];

    private byte[] mFrameBuf;
    private int mFrameBufLen;

    private FrameRateTest mFps = new FrameRateTest();

    private native boolean nativeOpen(String dev, int w, int h, String fmt);

    private native void nativeClose();

    private native boolean nativeReadJpeg(byte[] buf);

    private native void nativeGetFrameSize(int[] size);

    private native int nativeGetFrameLen();

    private long mNativeCam;

    private UsbCameraListener mListener;

    private Runnable mReadRunnable = new Runnable() {

        @Override
        public void run() {
            while (!mExit) {
                //Log.e(TAG, "sleep 500,"+Thread.currentThread().getName()+","+Thread.currentThread().getId());
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                nativeReadJpeg(mFrameBuf);
                if (mListener != null) {
                    mListener.onJpegFrame(mFrameBuf);
                    mFps.tick(mDev);
                }

                if (callback != null) {
                    callback.onPictureTaken(mFrameBuf);
                }
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
            Log.e(TAG, "mReadRunnable end");
        }
    };
    private Thread mReadThread = null;

    private boolean mExit = false;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private class FrameRateTest {
        int mFrameCount = -1;
        long mLastMillis = -1;
        int mLogInternal = 10; //second

        public void tick(String dev) {
            long t = SystemClock.uptimeMillis();
            if (mLastMillis < 0 || mFrameCount < 0) {
                mFrameCount = 0;
                mLastMillis = t;
                return;
            }
            mFrameCount++;
            if (mLastMillis / (mLogInternal * 1000) != t / (mLogInternal * 1000)) {
                Log.d(TAG, dev + " preview fps = " + mFrameCount / mLogInternal);
                Log.d("====", dev + " preview fps = " + mFrameCount / mLogInternal);
                mFrameCount = 0;
                mLastMillis = t;
            }
        }
    }

    public interface UsbCameraListener {
        public void onJpegFrame(byte framebuf[]);
    }

    ;

    public UsbCamera() {
        mFrameFmt = new String("MJPEG");
    }

    /*
     * dev: video device path name, /dev/video3
     * reqsize: requested video size, null for max size supported
     */
    public boolean open(String dev, int reqsize[]) {
        Log.e(TAG, "open " + dev);
        Log.e("====", "========open " + dev);
        mDev = new String(dev);
        mReqSize = new int[2];
        if (reqsize != null) {
            mReqSize[0] = reqsize[0];
            mReqSize[1] = reqsize[1];
        } else {
            mReqSize[0] = 0;
            mReqSize[1] = 0;
        }

        if (!nativeOpen(mDev, mReqSize[0], mReqSize[1], mFrameFmt)) {
//            Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
//            intent.putExtra(ToastReceiver.TOAST_CONTENT, mDev + MyApplication.getContext().getString(R.string.usb_open_failed));
//            MyApplication.getContext().sendBroadcast(intent);
            Log.e("====", "=============UsbCamera=!nativeOpen");
            return false;
        }

        mRealSize = new int[2];
        nativeGetFrameSize(mRealSize);
        mFrameBufLen = nativeGetFrameLen();
        mFrameBuf = new byte[mFrameBufLen];
//        mFrameBuf = new byte[1024];
        return true;
    }

    public boolean start(UsbCameraListener l) {
        mListener = l;
        if (mReadThread != null) {
            mExit = true;
            try {
                mReadThread.join(1000);
                mReadThread.interrupt();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mReadThread = null;
        }
        mExit = false;
        mReadThread = new Thread(mReadRunnable);
        mReadThread.start();
        return true;
    }


    public void stop() {
        mListener = null;
//        if (mReadThread != null) {
//            mExit = true;
//            try {
//                mReadThread.join(1000);
//                mReadThread.interrupt();
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            mReadThread = null;
//        }
    }

    public void close() {
        if (mReadThread != null) {
            mExit = true;
            try {
                mReadThread.join(1000);
                mReadThread.interrupt();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mReadThread = null;
        }
        nativeClose();
        Log.e(TAG, "nativeClose:" + mDev);
    }

    public void setPictureCallback(PictureCallback callback) {
        this.callback = callback;
    }

    public interface PictureCallback {
        void onPictureTaken(byte[] data);
    }

    ;
}
