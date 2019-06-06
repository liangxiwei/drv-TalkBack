//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.report.core;

import android.annotation.TargetApi;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;

import com.example.jrd48.chat.crash.MyApplication;

import java.util.concurrent.TimeUnit;

@TargetApi(16)
public final class Core1 implements FrameCallback {
    public static final Core1 a = new Core1();
    private static final long b;
    private static final long c;
    private static long d;
    private static long e;
    private static int f;
    private static int g;
    private static boolean h;

    public Core1() {
    }

    public void a() {
        if(h) {
            h = false;
            g = 0;
            f = 0;
            e = 0L;
            d = 0L;
        }
    }

    public void b() {
        h = true;
    }

    public int c() {
        this.d();
        return g;
    }

    private void d() {
        if(g == 0 || e - d >= c) {
            long var1 = e - d;
            g = Math.round((float)((long)f * b) / (float)var1);
            d = e;
            f = 0;
        }

    }

    public void doFrame(long var1) {
        ++f;
        if(d == 0L) {
            d = var1;
            MyApplication.getChoreographer().postFrameCallback(this);
        } else {
            e = var1;
            if(h) {
                MyApplication.getChoreographer().removeFrameCallback(this);
            } else {
                MyApplication.getChoreographer().postFrameCallback(this);
            }

        }
    }

    static {
        b = TimeUnit.NANOSECONDS.convert(1L, TimeUnit.SECONDS);
        c = TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
        d = 0L;
        e = 0L;
        f = 0;
        g = 0;
        h = false;
    }
}
