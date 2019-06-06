package com.example.jrd48.service.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by quhuabo on 2016/9/23.
 */

public class ByteBufferLE {
    public static ByteBuffer wrap(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb;
    }
    public static ByteBuffer wrap(byte[] data, int start, int cnt) {
        ByteBuffer bb = ByteBuffer.wrap(data, start, cnt);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb;
    }

    public static ByteBuffer allocate(int buffSize) {
        ByteBuffer bb = ByteBuffer.allocate(buffSize);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb;
    }

}
