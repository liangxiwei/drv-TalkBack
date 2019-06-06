package com.example.jrd48.service.protocol;

import com.example.jrd48.service.parser.ByteBufferLE;

import java.nio.ByteBuffer;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class Data implements IByteable {
    private int cmd;    //

    public Data() {
    }

    public Data(int cmd) {
        this.cmd = cmd;
    }

    public static Data from(ByteBuffer bb) {
        Data ret = new Data();
        ret.setCmd(bb.getInt());
        return ret;
    }

    public static Data from(int x) {
        return new Data(x);
    }

    @Override
    public int getSize() {
        return Integer.SIZE/8;
    }

    public void from(byte[] data){
        if (data.length<getSize()){
            throw new DataLenException("data class", getSize(), data.length);
        }
        ByteBuffer bb = ByteBufferLE.wrap(data, 0, getSize());
        bb.get(cmd);
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    @Override
    public void toBuffer(ByteBuffer bb) {
        bb.putInt(cmd);
    }

    @Override
    public void fromBuffer(ByteBuffer bb) {
        bb.get(cmd);
    }

    @Override
    public String toString() {
        return "Data{" +
                "cmd=" + cmd +
                '}';
    }
}
