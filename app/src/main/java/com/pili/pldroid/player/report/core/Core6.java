//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.report.core;

import android.content.Context;
import android.content.Intent;

import com.pili.pldroid.player.report.Report2;
import com.pili.pldroid.player.report.common.ReportCommon1;

import java.util.ArrayList;
import java.util.Iterator;

public final class Core6 {
    private static Context a;
    private static String b = null;
    private Core6.a c = new Core6.a();
    private Core6.b d = new Core6.b();
    private Core6.d e = new Core6.d();
    private Core6.c f;
    private ArrayList<Float> g;
    private ArrayList<Float> h;
    private ArrayList<Float> i;
    private ArrayList<Float> j;
    private Core5 k = new Core5();

    public Core6() {
    }

    private static String b(String var0) {
        return var0 != null && !"".equals(var0)?var0:"-";
    }

    public void a(Context var1) {
        a = var1.getApplicationContext();
        this.k.a(a);
        b = ReportCommon1.a(a);
    }

    public void a() {
        this.k.b();
    }

    public Core6.b b() {
        return this.d;
    }

    public Core6.c c() {
        if(this.f == null) {
            this.f = new Core6.c();
        }

        return this.f;
    }

    public void d() {
        this.j();
    }

    private String i() {
        String var1 = ReportCommon1.c(a);
        String var2 = ReportCommon1.a();
        String var3 = ReportCommon1.b();
        String var4 = null;
        String var5 = null;
        int var6 = 0;
        int var7 = 0;
        boolean var8 = var1.equals("WIFI");
        boolean var9 = var1.equals("None");
        String[] var10;
        if(var8) {
            var10 = ReportCommon1.h(a);
            if(var10 != null && var10.length >= 2) {
                var4 = var10[0];
                if(ReportCommon1.b(var10[1])) {
                    var6 = Integer.parseInt(var10[1]);
                }
            }
        } else if(!var9) {
            var10 = ReportCommon1.i(a);
            if(var10 != null && var10.length >= 2) {
                var5 = var10[0];
                if(ReportCommon1.b(var10[1])) {
                    var7 = Integer.parseInt(var10[1]);
                }
            }
        }

        return b(var1) + "\t" + b(var2) + "\t" + b(var3) + "\t" + b(var4) + "\t" + b(var5) + "\t" + var6 + "\t" + var7;
    }

    private void j() {
        this.c.a("network_change", "v5");
        this.c(this.c.toString() + "\t" + this.e.toString() + "\t" + this.i() + "\n");
    }

    public void e() {
        this.k();
    }

    private void k() {
        float var1 = 0.0F;
        float var2 = 0.0F;
        float var3 = 0.0F;
        float var4 = 0.0F;
        ReportCommon1.a var5 = ReportCommon1.f();
        ReportCommon1.b var6 = ReportCommon1.b(a);
        var1 = var5.a / 100.0F;
        var2 = var5.b / 100.0F;
        if(var6.a != 0L) {
            var3 = (float)var6.b / (float)var6.a;
            var4 = (float)var6.c / (float)var6.a;
        }

        if(this.g == null) {
            this.g = new ArrayList();
        }

        if(this.h == null) {
            this.h = new ArrayList();
        }

        if(this.i == null) {
            this.i = new ArrayList();
        }

        if(this.j == null) {
            this.j = new ArrayList();
        }

        this.g.add(Float.valueOf(var1));
        this.h.add(Float.valueOf(var2));
        this.i.add(Float.valueOf(var3));
        this.j.add(Float.valueOf(var4));
    }

    private float a(ArrayList<Float> var1) {
        float var2 = 0.0F;
        if(var1 != null && !var1.isEmpty()) {
            float var3 = 0.0F;

            Float var5;
            for(Iterator var4 = var1.iterator(); var4.hasNext(); var3 += var5.floatValue()) {
                var5 = (Float)var4.next();
            }

            var2 = var3 / (float)var1.size();
        }

        return var2;
    }

    private String a(int var1, int var2) {
        float var3 = this.a(this.g);
        float var4 = this.a(this.h);
        float var5 = this.a(this.i);
        float var6 = this.a(this.j);
        int var7 = ReportCommon1.i();
        Object var8 = null;
        return this.e.toString() + "\t" + String.format("%.3f", new Object[]{Float.valueOf(var3)}) + "\t" + String.format("%.3f", new Object[]{Float.valueOf(var4)}) + "\t" + String.format("%.3f", new Object[]{Float.valueOf(var5)}) + "\t" + String.format("%.3f", new Object[]{Float.valueOf(var6)}) + "\t" + "ffmpeg-3.2;PLDroidPlayer-1.5.0" + "\t" + var7 + "\t" + this.i() + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + b((String)var8) + "\t" + var1 + "\t" + var2;
    }

    private void c(String var1) {
        this.k.b(var1);
    }

    private void d(String var1) {
        this.k.a(var1);
    }

    public void a(Intent var1) {
        long var2 = var1.getLongExtra("firstVideoTime", 0L);
        long var4 = var1.getLongExtra("firstAudioTime", 0L);
        long var6 = var1.getLongExtra("gopTime", -1L);
        String var8 = var1.getStringExtra("videoDecoderType");
        String var9 = var1.getStringExtra("audioDecoderType");
        int var10 = var1.getIntExtra("tcpConnectTime", 0);
        int var11 = var1.getIntExtra("firstByteTime", 0);
        this.c.a("play_start", "v5");
        String var12 = this.d.toString() + "\t" + var2 + "\t" + var4 + "\t" + var6 + "\t" + b(var8) + "\t" + b(var9) + "\t" + var10 + "\t" + var11 + "\n";
        this.d(var12);
        this.c(var12);
    }

    public void b(Intent var1) {
        long var2 = var1.getLongExtra("beginAt", 0L);
        long var4 = var1.getLongExtra("endAt", 0L);
        long var6 = var1.getLongExtra("bufferingTotalCount", -1L);
        long var8 = var1.getLongExtra("bufferingTotalTimes", -1L);
        long var10 = var1.getLongExtra("totalRecvBytes", -1L);
        int var12 = var1.getIntExtra("endBufferingTime", -1);
        long var13 = var1.getLongExtra("gopTime", -1L);
        int var15 = var1.getIntExtra("errorCode", 0);
        int var16 = var1.getIntExtra("errorOSCode", 0);
        this.c.a("play_end", "v5");
        String var17 = this.d.toString() + "\t" + var2 + "\t" + var4 + "\t" + var6 + "\t" + var8 + "\t" + var10 + "\t" + var12 + "\t" + var13 + "\t" + this.a(var15, var16) + "\n";
        this.d(var17);
        this.c(var17);
        Report2.a();
    }

    public void f() {
        this.c.a("play", "v5");
        this.d(this.d.toString() + "\t" + this.f.toString() + "\n");
    }

    public final class c {
        private long b = 0L;
        private long c = 0L;
        private long d = 0L;
        private int e = 0;
        private int f = 0;
        private int g = 0;
        private int h = 0;
        private int i = 0;
        private int j = 0;
        private int k = 0;
        private int l = 0;
        private long m = 0L;
        private long n = 0L;

        public c() {
        }

        public boolean a(long var1, long var3, long var5, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, long var15, long var17) {
            boolean var19 = true;
            this.b = var1;
            this.c = var3;
            this.e = var7;
            this.f = var8;
            this.g = var9;
            this.d = var5;
            this.h = var10;
            this.i = var11;
            this.j = var12;
            this.k = var13;
            this.l = var14;
            this.m = var15;
            this.n = var17;
            return var19;
        }

        public String toString() {
            StringBuilder var1 = new StringBuilder();
            var1.append(this.b + "\t");
            var1.append(this.c + "\t");
            var1.append(this.d + "\t");
            var1.append(this.e + "\t");
            var1.append(this.f + "\t");
            var1.append(this.g + "\t");
            var1.append(this.h + "\t");
            var1.append(this.i + "\t");
            var1.append(this.j + "\t");
            var1.append(this.k + "\t");
            var1.append(this.l + "\t");
            var1.append(this.m + "\t");
            var1.append(this.n);
            return var1.toString();
        }
    }

    public final class d {
        private String b;
        private String c;
        private String d;
        private String e;
        private String f;

        public d() {
        }

        private void a() {
            this.b = ReportCommon1.e();
            this.c = "Android";
            this.d = ReportCommon1.d();
            this.e = ReportCommon1.d(Core6.a);
            this.f = ReportCommon1.e(Core6.a);
        }

        public String toString() {
            this.a();
            return Core6.b(this.b) + "\t" + Core6.b(this.c) + "\t" + Core6.b(this.d) + "\t" + Core6.b(this.e) + "\t" + Core6.b(this.f);
        }
    }

    public final class b {
        private String b;
        private String c;
        private String d;
        private String e;
        private String f;

        public b() {
        }

        public void a(String var1, String var2, String var3, String var4, String var5) {
            this.b = var1;
            this.c = var2;
            this.f = var3;
            this.d = var4;
            this.e = var5;
        }

        public String toString() {
            return Core6.this.c.toString() + "\t" + Core6.b(this.b) + "\t" + Core6.b(this.c) + "\t" + Core6.b(this.d) + "\t" + Core6.b(this.e) + "\t" + Core6.b(this.f);
        }
    }

    public final class a {
        private String b;
        private long c;

        public a() {
        }

        public void a(String var1, String var2) {
            this.b = var1 + "." + var2;
        }

        private void a() {
            this.c = System.currentTimeMillis();
        }

        public String toString() {
            this.a();
            return Core6.b(this.b) + "\t" + this.c + "\t" + Core6.b(Core6.b) + "\t" + Core6.b("1.5.0");
        }
    }
}
