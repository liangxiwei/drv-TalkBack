package com.luobin.search.friends.car.listForSelectUsed;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class ViewUtil {

    /**
     * 数值转换成dip
     *
     * @param size 要转换的px数值
     */
    public static int numToDIP(int num) {
        final Resources res = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                num, res.getDisplayMetrics());
    }

    /**
     * 单位转换 将px转换成dip
     *
     * @param size 要转换的px数值
     */
    public static float getDimension(Context context, float size) {
        final Resources res = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                res.getDisplayMetrics());
    }

    /**
     * 数值转换成sp
     *
     * @param num 要转换的数值
     * @return
     */
    public static int numToSp(int num) {
        final Resources res = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, num,
                res.getDisplayMetrics());
    }

    /**
     * 单位转换 将px转换成sp
     *
     * @param context
     * @param size    要转换的数值
     * @return
     */
    public static int getDimensionSp(Context context, float size) {
        final Resources res = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, res.getDisplayMetrics());
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param fontScale （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(float spValue) {
        final Resources res = Resources.getSystem();
        float fontScale = res.getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 单位转换 将dip转换成px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int[] toDIPArray(int[] width) {
        if (null != width && width.length > 0) {
            int[] result = new int[width.length];
            for (int i = 0; i < width.length; i++) {
                result[i] = numToDIP(width[i]);
            }
            return result;
        }
        return null;
    }
}
