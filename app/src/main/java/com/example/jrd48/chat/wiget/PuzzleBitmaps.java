package com.example.jrd48.chat.wiget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.List;

public class PuzzleBitmaps {

    public static final void Puzzle(Canvas canvas, int dimension, List<Bitmap> bitmaps) {
        if (bitmaps == null)
            return;
        int count = Math.min(bitmaps.size(), PuzzleLayout.max());
        float size = PuzzleLayout.size(count);
        Puzzle(canvas, dimension, bitmaps, count, size);
    }

    public static final void Puzzle(Canvas canvas, int dimension, List<Bitmap> bitmaps, int count, float size) {
        Puzzle(canvas, dimension, bitmaps, count, size, 0.15f);
    }

    public static final void Puzzle(Canvas canvas, int dimension, List<Bitmap> bitmaps, float gapSize) {
        if (bitmaps == null)
            return;
        int count = Math.min(bitmaps.size(), PuzzleLayout.max());
        float size = PuzzleLayout.size(count);
        Puzzle(canvas, dimension, bitmaps, count, size, gapSize);
    }

    public static final void Puzzle(Canvas canvas, int dimension, List<Bitmap> bitmaps, int count, float size, float gapSize) {
        if (bitmaps == null)
            return;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Matrix matrixJoin = new Matrix();
        matrixJoin.postScale(size, size);
        canvas.save();
        for (int index = 0; index < bitmaps.size(); index++) {
            Bitmap bitmap = bitmaps.get(index);
            // MATRIX
            Matrix matrix = new Matrix();
            matrix.postScale((float) dimension / bitmap.getWidth(), (float) dimension / bitmap.getHeight());
            canvas.save();
            matrix.postConcat(matrixJoin);
            float[] offset = PuzzleLayout.offset(count, index, dimension, size);
            canvas.translate(offset[0], offset[1]);
            // 缩放
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            canvas.drawBitmap(newBitmap, 0, 0, paint);
            canvas.restore();
        }

        canvas.restore();
    }

    public static final Bitmap createBitmap(int width, int height, List<Bitmap> bitmaps) {
        int count = Math.min(bitmaps.size(), PuzzleLayout.max());
        float size = PuzzleLayout.size(count);
        return createBitmap(width, height, bitmaps, count, size, 0.15f);
    }

    public static final Bitmap createBitmap(int width, int height, List<Bitmap> bitmaps, int count, float size) {
        return createBitmap(width, height, bitmaps, count, size, 0.15f);
    }

    public static final Bitmap createBitmap(int width, int height, List<Bitmap> bitmaps, int count, float size, float gapSize) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int dimen = Math.min(width, height);
        Puzzle(canvas, dimen, bitmaps, count, size, gapSize);
        return output;
    }

}
