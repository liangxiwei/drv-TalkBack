package com.example.jrd48.chat.friend;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.jrd48.chat.MyFileUtil;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/1/5.
 */

public class FriendFaceUtill {

    /**
     * 保存好友头像
     *
     * @param phoneNum
     * @param friendUserFace
     * @param context
     */
    public static synchronized void saveFriendFaceImg(String name,String phoneNum, byte[] friendUserFace, Context context) {

        File file = getFriendFaceImgFile(context, phoneNum);

        if (friendUserFace==null||friendUserFace.length <= 0) {
            // quhuabo: 创建空头像
            Log.i("chatjrd","进来了");
            LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
            drawable.setContactDetails(name, name);
            Bitmap bmp = drawableToBitmap(drawable);
            try {
                if (file.exists() == false) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                Log.i("pocdemo", "create empty head image for: " + phoneNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }


        try {
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(friendUserFace);
            fos.flush();
            fos.close();

            Log.i("SaveJpgLocal", "保存本地文件成功: " + file.getAbsolutePath());
        } catch (IOException e) {

            e.printStackTrace();
            Log.e("CHAT", "save friend face img Failed: " + file.getAbsolutePath());
        }
    }

    /**
     * 保存好友头像
     *
     * @param phoneNum
     * @param bitmap
     * @param context
     */
    public static synchronized void saveFriendBitmapFaceImg(String name, String phoneNum, Bitmap bitmap, Context context) {

        File file = getFriendFaceImgFile(context, phoneNum);

        if (bitmap == null) {
            // quhuabo: 创建空头像
            Log.i("chatjrd", "进来了");
            LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
            if (name != null && name.length() > 0) {
                drawable.setContactDetails(name, name);
            } else {
                drawable.setContactDetails("1", "1");
            }
            Bitmap bmp = drawableToBitmap(drawable);
            try {
                if (file.exists() == false) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                Log.i("pocdemo", "create empty head image for: " + phoneNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }


        try {
            if (file.exists() == false) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Log.i("SaveJpgLocal", "保存本地文件成功: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CHAT", "save friend face img Failed: " + file.getAbsolutePath());
        }
    }

    public static File getFriendFaceImgFile(Context context, String friendUserID) {
        String strPath = getFriendFaceImgPath(context);

        strPath += "/" + friendUserID + ".jpg";

        return new File(strPath);
    }

    private static String getFriendFaceImgPath(Context context) {
        String strPath = MyFileUtil.getMemoryPath(context);
//        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
//        String phone = preferences.getString("phone", "");
//        strPath += "/" + phone + "/friend_face";//     strPath/手机号/friend_face
        strPath += "/friend_face";//     strPath/friend_face
        File file = new File(strPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return strPath;
    }


    /**
     * 删除好友头像
     *
     * @param context
     */
    public static void deleteFriendsFace(Context context) {
        try {
            Log.i("CHAT", "delete friend cache face img");
            // 删除缓存图片
            File f = new File(getFriendFaceImgPath(context));
            if (f.exists()) {
                for (File file : f.listFiles()) {
                    Log.i("CHAT", "delete friend cache face img: " + file.getName());
                    file.delete();
                }
            }
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取好友头像
     *
     * @param context
     * @param phoneNum
     * @return
     * @throws Exception
     */
    public static byte[] getFriendsFace(Context context, String phoneNum) {
        try {
            File file = getFriendFaceImgFile(context, phoneNum);
            return MyFileUtil.readFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取好友头像
     *
     * @param context
     * @param phoneNum
     * @return
     */
    public static Bitmap getUserFace(Context context, String phoneNum) {
        Bitmap bmp = null;
        try {
            byte[] pic = getFriendsFace(context, phoneNum);
            if (pic == null || pic.length == 0) {
                return null;
            } else {
                //optimize bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inPurgeable = true;
                options.inInputShareable = true;
                bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取该用户手机号目录下的子目录的路径
     *
     * @param context
     * @param str
     * @return
     */
    private static String getFilePath(Context context, String str) {
        String strPath = MyFileUtil.getMemoryPath(context);
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String phone = preferences.getString("phone", "");
        strPath += "/" + phone + "/" + str;
        File file = new File(strPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return strPath;
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(200,200, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, 200, 200);

        drawable.draw(canvas);

        return bitmap;

    }
}
