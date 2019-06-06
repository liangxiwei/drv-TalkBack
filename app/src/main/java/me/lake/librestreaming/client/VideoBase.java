package me.lake.librestreaming.client;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Created by zhouyuhuan on 2018/4/2.
 */

public class VideoBase extends SurfaceView {
    public VideoBase(Context context) {
        super(context);
    }

    public VideoBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setIsDrawing(boolean isDrawing){

    }

    public void setNoDrawing(boolean isDrawing){

    }
}
