package com.luobin.voice.decode;

public interface Decoder {

    public void putData(long ts, byte[] buf, int size);

    public void setRunning(boolean isRunning);

    public boolean isRunning();

    public boolean isIdle();

}
