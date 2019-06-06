package com.luobin.dvr;

import android.content.Context;

public class DvrImplFactory {
    public static enum IMPL_TYPE {
        ANDROID_ORI,
        GRAFIKA,
    };
    static final IMPL_TYPE mImplType = IMPL_TYPE.GRAFIKA;
    static public DvrInterface createDvrImpl(Context context) {
        switch (mImplType) {
        case ANDROID_ORI:
            return new com.luobin.dvr.ori.DvrImpl(context);
        case GRAFIKA:
            return new com.luobin.dvr.grafika.DvrImpl(context);
        default:
            return null;
        }
    }
}
