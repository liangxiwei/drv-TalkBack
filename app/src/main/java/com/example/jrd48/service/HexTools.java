package com.example.jrd48.service;

/**
 * Created by Administrator on 2016/9/7 0007.
 */

public class HexTools {

    /**
     * 字节数组转16进制字符串，当参数为 null 时返回 "<null>"
     *
     * @param a
     * @return
     */
    public static String byteArrayToHex(byte[] a) {
        if (null == a) {
            return "<null>";
        }
        StringBuilder sb = new StringBuilder(a.length * 3 - 1);
        int i = 0;
        for (byte b : a) {
            if (i != 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", b & 0xff));
            ++i;
        }

        return sb.toString();
    }

    public static String byteArrayToHex(byte[] a, int offset, int count) {
        if (null == a) {
            return "<null>";
        }

        StringBuilder sb = new StringBuilder(count * 3 - 1);
        for (int i = offset; i < offset + count; ++i) {
            if (i != offset) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", a[i] & 0xff));
        }
        return sb.toString();
    }

    public static String byteArrayToHex(byte[] a, int offste, int count, int topCount){
        String str = "(bytes count: "+count+")"+byteArrayToHex(a, 0, Math.min(count, topCount));
        if (topCount<count) {
            str += "...";
            str += " << end 4 bytes: " + byteArrayToHex(a, count-4, 4) + " >>";
        }
        return str;
    }
    /**
     * 16进制字符串转字节数组，要求空格分开
     *
     * @param hexStr
     * @return
     */
    public static byte[] HexToByteArray(String hexStr) {
        String[] temp = hexStr.split(" ");
        byte[] bytesArray = new byte[temp.length];
        int index = 0;
        for (String item : temp) {
            bytesArray[index] = (byte) (Integer.parseInt(item, 16) & 0xff);
            index++;
        }
        return bytesArray;
    }


}
