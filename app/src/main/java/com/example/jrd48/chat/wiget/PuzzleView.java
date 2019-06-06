package com.example.jrd48.chat.wiget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import static com.example.jrd48.chat.wiget.PuzzleBitmaps.createBitmap;

public class PuzzleView extends View {

    protected int viewWidth;
    protected int viewHeight;

    protected ArrayList<Bitmap> bmps;

    public PuzzleView(Context context) {
        super(context);
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PuzzleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int dimen = Math.min(width, height);
        setMeasuredDimension(dimen, dimen);
    }

    public void setImageBitmaps(ArrayList<Bitmap> bitmaps) {
        if (bitmaps == null)
            throw new IllegalArgumentException("bitmaps can not be Null");
        if (bitmaps.size() > PuzzleLayout.max())
            throw new IllegalArgumentException("bitmaps size can not be greater than "
                    + PuzzleLayout.max());
        this.bmps = bitmaps;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        viewWidth = viewHeight = Math.min(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (viewWidth > 0 && viewHeight > 0) {
            PuzzleBitmaps.Puzzle(canvas, viewWidth, bmps, 0.15f);
        }
    }

    public Bitmap saveImage(int width, int height) {
        Bitmap temp = createBitmap(width, height, bmps);
        //TODO 保存 到 file 中： bitmap 转为 PNG或JPEG
        return temp;
    }
}
