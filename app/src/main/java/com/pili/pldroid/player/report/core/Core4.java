//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.report.core;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import com.pili.pldroid.player.report.Report1;
import com.pili.pldroid.player.report.common.ReportCommon1;

public final class Core4 {
    private static Context a;
    private Core6 b;
    private BroadcastReceiver c;
    private BroadcastReceiver d;

    private Core4() {
        this.c = new BroadcastReceiver() {
            public void onReceive(Context var1, Intent var2) {
                if("pldroid-player-qos-filter".equals(var2.getAction())) {
                    Core4.this.a(var2);
                }
            }
        };
        this.d = new BroadcastReceiver() {
            @TargetApi(11)
            public void onReceive(Context var1, Intent var2) {
                if("android.intent.action.ANY_DATA_STATE".equals(var2.getAction())) {
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            Core4.this.b.d();
                        }
                    });
                }
            }
        };
    }

    public static Core4 a() {
        return Core4.newCore.a;
    }

    public void a(Context var1) {
        if(a == null) {
            a = var1.getApplicationContext();
            this.b = new Core6();
            this.b.a(var1);
            Report1.a().a(this.c, new IntentFilter("pldroid-player-qos-filter"));
            a.registerReceiver(this.c, new IntentFilter("pldroid-player-qos-filter"));
            a.registerReceiver(this.d, new IntentFilter("android.intent.action.ANY_DATA_STATE"));
        }
    }

    public void b(Context var1) {
        if(a != null) {
            a.unregisterReceiver(this.d);
            a = null;
            Report1.a().a(this.c);
            this.b.a();
        }
    }

    private boolean a(Intent var1) {
        int var2 = var1.getIntExtra("pldroid-qos-msg-type", -1);
        switch(var2) {
            case 4:
                String var3 = var1.getStringExtra("reqid");
                this.b.b().a(var1.getStringExtra("scheme"), var1.getStringExtra("domain"), var1.getStringExtra("remoteIp"), var1.getStringExtra("path"), var3);
                this.b.e();
                ReportCommon1.g();
                break;
            case 162:
                this.b.e();
                break;
            case 193:
                this.b(var1);
                break;
            case 195:
                this.b.a(var1);
                break;
            case 196:
                this.b.b(var1);
        }

        return true;
    }

    private void b(Intent var1) {
        long var2 = var1.getLongExtra("beginAt", 0L);
        long var4 = var1.getLongExtra("endAt", 0L);
        long var6 = var1.getLongExtra("bufferingTimes", 0L);
        int var8 = var1.getIntExtra("videoSourceFps", 0);
        int var9 = var1.getIntExtra("dropVideoFrames", 0);
        int var10 = var1.getIntExtra("audioSourceFps", 0);
        int var11 = var1.getIntExtra("audioDropFrames", 0);
        int var12 = var1.getIntExtra("videoRenderFps", 0);
        int var13 = var1.getIntExtra("audioRenderFps", 0);
        int var14 = var1.getIntExtra("videoBufferTime", 0);
        int var15 = var1.getIntExtra("audioBufferTime", 0);
        long var16 = var1.getLongExtra("audioBitrate", 0L);
        long var18 = var1.getLongExtra("videoBitrate", 0L);
        if(this.b.c().a(var2, var4, var6, var8, var9, var10, var11, var12, var13, var14, var15, var16, var18)) {
            this.b.f();
        }
    }

    private static class newCore {
        public static final Core4 a = new Core4();
    }
}
