package com.luobin.dvr;

import android.graphics.Bitmap;

public abstract class DvrInterface {
    abstract public boolean show(int x, int y, int w, int h);
    abstract public boolean hide();
    abstract public boolean startPreview(int w, int h, boolean audioEnabled);
    abstract public boolean startRecord(String file, boolean withBufferedVideo);
    abstract public boolean stopRecord();
    abstract public boolean stopPreview();
    abstract public boolean release();
    abstract public boolean setWaterMark(Bitmap bitmap, int gravity);
    abstract public boolean takePhoto(String file);
    abstract public boolean isPreviewing();
    abstract public boolean isRecording();

    abstract public void onClick();
}
