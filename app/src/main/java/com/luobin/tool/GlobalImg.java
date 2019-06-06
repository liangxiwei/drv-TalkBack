package com.luobin.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.example.jrd48.chat.ImageTool;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.friend.FriendFaceUtill;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by quhuabo on 2017/1/19 0019.
 */

public class GlobalImg {
    private static Map<String, Bitmap> mImages = new HashMap<String, Bitmap>();

    public static Bitmap getImage(Context context, String phone) {

        Bitmap b = mImages.get(phone);

        if (b == null) {
            b = FriendFaceUtill.getUserFace(context, phone);
            if (b != null) {
                mImages.put(phone, b);
            }
        }
        return b;
    }

    public static void reloadImg(Context context, String phone) {
        Bitmap b = FriendFaceUtill.getUserFace(context, phone);
        if (b != null) {
            mImages.put(phone, b);
        }
    }

    public static void clear() {
        try {
            throw new Exception("[Warning] clear all images");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mImages.clear();
    }

    //圆形图片
    public static Bitmap getUserFace(Bitmap bmp,String str,Context context) {
        try {
            if (bmp == null) {
                LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
                String nameStr = TextUtils.isEmpty(str) ? "1":str;
                drawable.setContactDetails(nameStr, nameStr);
                bmp = FriendFaceUtill.drawableToBitmap(drawable);
            }

            Bitmap bitmap = Bitmap.createScaledBitmap(bmp, 150, 150,
                    false); // 将图片缩小
            ImageTool ll = new ImageTool(); // 图片头像变成圆型
            bmp = ll.toRoundBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

}
