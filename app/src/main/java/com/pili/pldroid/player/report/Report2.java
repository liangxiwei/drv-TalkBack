//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.report;

import android.content.Context;

import com.pili.pldroid.player.report.common.ReportCommon1;
import com.pili.pldroid.player.report.core.Core4;

public final class Report2 {
    private static boolean a = false;

    public static void a(Context var0) {
        if (!a && var0 != null) {
            a = true;
            Report1.a().a(var0.getApplicationContext());
            Report1.a().a(true);
            Core4.a().a(var0.getApplicationContext());
        }
    }

    public static void a() {
        Core4.a().b((Context) null);
        Report1.a().b();
        ReportCommon1.h();
        a = false;
    }
}
