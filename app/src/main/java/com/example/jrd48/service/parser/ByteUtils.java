package com.example.jrd48.service.parser;

import junit.framework.Assert;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

public class ByteUtils {

    /**
     * UINT 转为 4字节数组
     */
    public static byte[] uintToBytes(long x) {
        ByteBuffer buffer = ByteBufferLE.allocate(4);
        buffer.putInt((int) x);
        return buffer.array();
    }

    /**
     * 4字节数组转为 UINT
     */
    public static long bytesToUInt(byte[] bytes) {
        Assert.assertEquals(4, bytes.length);
        ByteBuffer buffer = ByteBufferLE.wrap(bytes);

        return  buffer.getInt() & 0xffffffffL;

    }


    /**
     * 从指定位置，转换两个字节为 int
     *
     * @param buff
     * @param offset
     * @return
     */
    public static int makeUShort(byte[] buff, int offset) {
        ByteBuffer bb = ByteBufferLE.wrap(ArrayUtils.subarray(buff, offset, offset + 2));
        return bb.getShort() & 0xffff;
    }

    public static int makeUShort(byte[] buff) {
        ByteBuffer bb = ByteBufferLE.wrap(buff);
        return bb.getShort() & 0xffff;
    }


    public static byte[] ushortToBytes(int value) {
        ByteBuffer bb = ByteBufferLE.allocate(2);
        bb.putShort((short) value);
        return bb.array();
    }

}