package com.luobin.voice;

/**
 * Created by Administrator on 2016/10/21 0021.
 */

public class VolatileBool {
    private final Object mutex = new Object();
    private volatile boolean isRunning = false;

    public VolatileBool() {

    }

    public VolatileBool(boolean b) {
        isRunning = b;
    }

    public boolean getValue() {
        synchronized (mutex) {
            return isRunning;
        }
    }

    public void setValue(boolean isRunning) {
        synchronized (mutex) {
            this.isRunning = isRunning;
            if (this.isRunning) {
                mutex.notify();
            }
        }
    }
}
