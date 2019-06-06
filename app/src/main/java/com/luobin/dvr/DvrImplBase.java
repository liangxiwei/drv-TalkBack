package com.luobin.dvr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;

public abstract class DvrImplBase extends DvrInterface {
    private final String TAG = DvrService.TAG;
    protected Context mContext = null;
    protected Bitmap mWaterMarkBmp = null;
    protected Bitmap mUsbBmp = null;
    protected BitmapFactory.Options options;
    protected final Object mBmpLock = new Object();
    protected int mWaterMarkGravity = Gravity.TOP | Gravity.LEFT;
    protected boolean mSavingFile = false;

    public DvrImplBase(Context context) {
        mContext = context;
    }

    @Override
    public boolean setWaterMark(Bitmap bitmap, int gravity) {
        synchronized(this) {
        	synchronized(mBmpLock) {
            	mWaterMarkBmp = bitmap;
        	}
            mWaterMarkGravity = gravity;
        }
        return true;
    }

    @Override
    public boolean isRecording() {
//        Log.d(TAG, "DvrImplBase isRecording:"+mSavingFile);
        return mSavingFile;
    }
}
