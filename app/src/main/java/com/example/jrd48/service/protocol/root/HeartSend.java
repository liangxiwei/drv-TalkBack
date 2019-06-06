package com.example.jrd48.service.protocol.root;

import com.example.jrd48.service.protocol.Data;
import com.example.jrd48.service.protocol.IByteable;

import java.nio.ByteBuffer;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class HeartSend extends Data implements IByteable{
    byte dummy;

    @Override
    public int getSize() {
        return super.getSize()+1;
    }

    @Override
    public void fromBuffer(ByteBuffer bb) {
        super.fromBuffer(bb);
        dummy = bb.get();
    }

    @Override
    public void toBuffer(ByteBuffer bb) {
        super.toBuffer(bb);
        bb.put(dummy);
    }
}
