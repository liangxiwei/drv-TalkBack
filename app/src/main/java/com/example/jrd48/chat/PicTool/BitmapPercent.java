package com.example.jrd48.chat.PicTool;

import android.graphics.Bitmap;

/**
 * Created by jrd48 on 2017/2/13.
 */

public class BitmapPercent {
    public static Bitmap setAlphaPercent(Bitmap sourceImg, int number, int percent) {
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值
        number = number * 255 / 100;
        int len = (argb.length / 100) * percent;
        for (int i = len; i < argb.length; i++) {
            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);// [/i][i]修改最高2[/i][i]位的值
        }
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;
    }
}
