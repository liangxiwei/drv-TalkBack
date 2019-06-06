package com.example.jrd48.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class MyFileUtil {
    public static String getMemoryPath(Context context) {
        File sdDir = null;

        sdDir = context.getFilesDir();
        //Log.i("pocdemo", "file dir: " + sdDir.toString());

        return sdDir.getAbsolutePath();
    }

    public static String getSDCardPath(Context context) {
        File sdDir = null;
        boolean sdCardExist =
                Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = context.getExternalFilesDir(null);// 获取跟目录
            String szTemp = System.getenv("EXTERNAL_STORAGE");

            System.out.println(sdDir.toString());
        } else {
            Log.i("config", "没找到SD卡.");
            return "";
        }
        return sdDir.toString();
    }
    /**
     * 读取整个文件
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static byte[] readFile(File file) throws Exception {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int buf_size = 10240;
            byte[] data = new byte[buf_size];
            int length = 0;
            long total_length = 0;
            while (-1 != (length = inputStream.read(data, 0, buf_size))) {
                total_length += length;
                outStream.write(data, 0, length);
            }
            System.out.println("文件的长度为：" + total_length + ", " + file.getAbsolutePath());
            inputStream.close();
            outStream.flush();
            outStream.close();

            return outStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 拷贝文件
     *
     * @param fromFile
     * @param toFile
     * @param rewrite  true:覆盖，false 追加
     * @return
     */
    public static boolean copyfile(File fromFile, File toFile, Boolean rewrite)

    {
        try {

            if (!fromFile.exists()) {

                return false;

            }

            if (!fromFile.isFile()) {

                return false;

            }

            if (!fromFile.canRead()) {

                return false;

            }

            if (!toFile.getParentFile().exists()) {

                toFile.getParentFile().mkdirs();

            }

            if (toFile.exists() && rewrite) {

                toFile.delete();

            }

//当文件不存时，canWrite一直返回的都是false

// if (!toFile.canWrite()) {

// MessageDialog.openError(new Shell(),"错误信息","不能够写将要复制的目标文件" + toFile.getPath());

// Toast.makeText(this,"不能够写将要复制的目标文件", Toast.LENGTH_SHORT);

// return ;

// }

            try {

                java.io.FileInputStream fosfrom = new java.io.FileInputStream(fromFile);

                java.io.FileOutputStream fosto = new FileOutputStream(toFile);

                byte bt[] = new byte[1024];

                int c;

                while ((c = fosfrom.read(bt)) > 0) {

                    fosto.write(bt, 0, c); //将内容写到新文件当中

                }

                fosfrom.close();

                fosto.close();
                Log.i("myfileutil", "复制文件成功： " + fromFile + " -->> " + toFile);
                return true;
            } catch (Exception ex) {

                Log.e("readfile", ex.getMessage());

            }
        } catch (Exception e) {
            Log.w("myfileutil", "copy file failed from " + fromFile + ", to " + toFile);
            e.printStackTrace();
        }
        return false;
    }
}
