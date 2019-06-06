package com.luobin.dvr.ori;

import android.content.Context;

import com.luobin.dvr.DvrImplBase;

public class DvrImpl extends DvrImplBase {
    
    public DvrImpl(Context context) {
        super(context);
    }

    @Override
    public boolean show(int x, int y, int w, int h) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hide() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startPreview(int w, int h, boolean audioEnabled) {
        
        return false;
    }

    @Override
    public boolean stopPreview() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startRecord(String file, boolean withBufferedVideo) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopRecord() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean takePhoto(String file) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPreviewing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onClick() {
        return;
    }
}
