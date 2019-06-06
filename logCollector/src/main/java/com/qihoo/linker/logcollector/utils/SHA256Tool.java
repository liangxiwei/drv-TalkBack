package com.qihoo.linker.logcollector.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created by jrd48 on 2017/2/10.
 */

public class SHA256Tool {
    String official_checksum = "";

    public static byte[] getTextSHA256(String str) {
        MessageDigest digest = null;
        FileInputStream in = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(str.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return digest.digest();//转换成16进制字符串
    }

    public static byte[] getFileSHA256(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
                //Log.i("jrdsha256", "文件长度：" + len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        byte[] result = digest.digest().clone();
        //Log.i("chatjrd", "校验字：" + bytes2Hex(result));
        return result;//转换成16进制字符串
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    boolean verifyFile(String code, File file) {

        String FileSHA256 = "";
        if (file.exists()) {
            FileSHA256 = bytes2Hex(getFileSHA256(file));
            Log.i("jrdsha256", FileSHA256);
        } else {
            return false;
        }
        if (code.equals(FileSHA256)) {
            return true;
        } else {
            return false;
        }
    }
}
