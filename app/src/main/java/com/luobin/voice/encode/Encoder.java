package com.luobin.voice.encode;

import android.util.Log;

import com.example.jrd48.service.MyLogger;
import com.luobin.voice.io.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Encoder implements Runnable {

    private MyLogger mLog = MyLogger.jLog();
    private volatile int leftSize = 0;
    private final Object mutex = new Object();
    private volatile boolean isRunning;

    private Speex speex = new Speex();
    private int frameSize;
    private Consumer consumer;
    //private byte[] processedData = new byte[4096];
    //private short[] rawdata = new short[4096];

    BlockingDeque<short[]> rawDataList = new LinkedBlockingDeque<>();

    public Encoder(Consumer consumer) {
        super();
        this.consumer = consumer;
        speex.init();
        frameSize = speex.getFrameSize();
        mLog.i("frameSize {" + frameSize + "}");
    }

    public void run() {

        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        int getSize = 0;
        try {
            while (this.isRunning()) {
//                synchronized (mutex) {
//                    while (isIdle()) {
//                        mutex.wait();
//                    }
//                }
                short[] rawdata = rawDataList.poll(100, TimeUnit.MILLISECONDS);
                if (rawdata == null) {
                    continue;
                }
                short[] temp = new short[frameSize];
                leftSize = rawdata.length;
                byte[] endcodeData = new byte[frameSize];
                for (int i = 0; i < leftSize / frameSize && isRunning(); i++) {
                    System.arraycopy(rawdata, i * frameSize, temp, 0, frameSize);
                    getSize = speex.encode(temp, 0, endcodeData, frameSize);
                    //mLog.i("encoded size {" + frameSize + " -> " + getSize + "}");
                    if (getSize > 0) {
                        //consumer.putData(System.currentTimeMillis(), endcodeData, getSize);
                        consumer.putData(0, endcodeData, getSize);
                    } else {
                        Log.w("mychat", "encode miss data size: " + frameSize);
                    }
                }
                //}
                //setIdle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putData(short[] data, int size) {
        try {
            rawDataList.offerLast(ArrayUtils.subarray(data, 0, size));
            //mutex.notify();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public boolean isIdle() {
        return leftSize == 0 ? true : false;
    }

    public void setIdle() {
        leftSize = 0;
    }

    public void setRunning(boolean isRunning) {
        synchronized (mutex) {
            this.isRunning = isRunning;
            if (this.isRunning) {
                mutex.notify();
            }
        }
    }

    public boolean isRunning() {
        //synchronized (mutex) {
        return isRunning;
        //}
    }
}
