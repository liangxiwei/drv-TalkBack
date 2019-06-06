package com.example.jrd48.service.protocol;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/11/19 0019.
 */

public interface IByteable {
    void toBuffer(ByteBuffer bb);
    void fromBuffer(ByteBuffer bb);
    int getSize();
}
