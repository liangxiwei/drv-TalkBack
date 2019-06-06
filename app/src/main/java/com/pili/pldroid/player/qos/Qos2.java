//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.qos;

import android.content.Context;

import com.pili.pldroid.player.report.Report2;

public class Qos2 {
    private static boolean a = true;
    private static boolean b = false;

    public static void a(Context var0) {
        if(a || !b) {
            b = true;
            Report2.a(var0);
            Qos3.a().a(var0);
        }
    }

    public static void b(Context var0) {
        if(a || b) {
            b = false;
            Qos3.a().a((Context)null);
        }
    }
}