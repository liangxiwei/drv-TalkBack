
package com.luobin.voice.encode;


import com.example.jrd48.service.MyLogger;

public class Speex {

    /* quality
     * 1 : 4kbps (very noticeable artifacts, usually intelligible)
     * 2 : 6kbps (very noticeable artifacts, good intelligibility)
     * 4 : 8kbps (noticeable artifacts sometimes)
     * 6 : 11kpbs (artifacts usually only noticeable with headphones)
     * 8 : 15kbps (artifacts not usually noticeable)
     */
    private static final int DEFAULT_COMPRESSION = 8;
    private MyLogger log = MyLogger.jLog();

    static {
        try {
            System.loadLibrary("speex");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Speex() {
    }

    public void init() {
        open(DEFAULT_COMPRESSION);
        log.i("speex opened");
    }

    public native int open(int compression);

    public native int getFrameSize();

    public native int decode(byte encoded[], short lin[], int size);

    public native int encode(short lin[], int offset, byte encoded[], int size);

    public native void close();

}
