//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.qos;

import android.content.Context;
import android.content.Intent;

import com.pili.pldroid.player.report.Report1;
import com.pili.pldroid.player.report.core.Core5;

public class Qos3 {
    public Context a;

    private Qos3() {
    }

    public static Qos3 a() {
        return Qos3.newQos.a;
    }

    public void a(Context var1) {
        if(var1 != null) {
            this.a = var1.getApplicationContext();
        } else {
            this.a = null;
        }

    }

    public int b() {
        return Core5.a();
    }

    public void a(Intent var1) {
        if(this.a != null) {
            Report1.a().a(var1);
        }
    }

    private static class newQos {
        public static final Qos3 a = new Qos3();
    }
}
