package com.luobin.voice.decode;


import android.util.Log;

import com.luobin.voice.encode.Speex;
import com.luobin.voice.io.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class SpeexDecoder implements Runnable, Decoder {
    private volatile int leftSize = 0;
    private final Object mutex = new Object();
    private Speex speex = new Speex();
    private long ts;
    private Consumer consumer;
    private short[] processedData = new short[4096];
    private BlockingDeque<byte[]> rawdataList = new LinkedBlockingDeque<>();
    private volatile boolean isRunning;

    public SpeexDecoder(Consumer consumer) {
        super();
        this.consumer = consumer;
        speex.init();
    }

    public void run() {

        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        Log.i("mychat", "decoder begin....");
        try {
            int getSize = 0;
            while (this.isRunning()) {
                byte[] rawdata = rawdataList.poll(100, TimeUnit.MILLISECONDS);
                if (rawdata == null) continue;
                for (int i = 0; i + 38 <= rawdata.length && this.isRunning(); i += 38) {
                    short temp[] = new short[160];
                    getSize = speex.decode(ArrayUtils.subarray(rawdata, i, 38), temp, 38);
                    //Log.i("mychat", "decode size: {"+38+"} -> {"+getSize+"}");
                    consumer.putData(ts, temp, getSize);
                }
            }
            Log.i("mychat", "decoder stop....");
        } catch (Exception e) {
            e.printStackTrace();
        }
        leftSize = 0;
    }

    public void putData(long ts, byte[] data, int size) {
        rawdataList.offerLast(ArrayUtils.subarray(data, 0, size));

    }

//    public void putData(long ts, byte[] data, int offset, int size) {
//        synchronized (mutex) {
//            this.ts = ts;
//            System.arraycopy(data, offset, rawdata, 0, size);
//            this.leftSize = size;
//            mutex.notify();
//        }
//    }

    public void setRunning(boolean isRunning) {
        synchronized (mutex) {
            this.isRunning = isRunning;
            if (this.isRunning) {
                mutex.notify();
            }
        }
    }

    public boolean isRunning() {
        synchronized (mutex) {
            return isRunning;
        }
    }

    @Override
    public boolean isIdle() {
        return false;
    }

}
