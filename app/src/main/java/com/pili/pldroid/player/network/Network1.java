//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.network;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.pili.pldroid.player.common.Util;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class Network1 {
    private DnsManager a;
    private Context b;
    private final Object c = new Object();
    private HashMap<String, Network1.a> d = new HashMap();
    private int e = 100000;
    private Handler f;
    private HandlerThread g;
    private BroadcastReceiver h = new BroadcastReceiver() {
        public void onReceive(Context var1, Intent var2) {
            if("android.intent.action.ANY_DATA_STATE".equals(var2.getAction())) {
                Network1.this.b();
                Network1.this.f.sendEmptyMessage(1);
            }

        }
    };
    private Callback i = new Callback() {
        public boolean handleMessage(Message var1) {
            Network1.this.a();
            if(var1.what == 0) {
                Network1.this.f.sendEmptyMessageDelayed(0, (long)Network1.this.e);
            }

            return true;
        }
    };

    public Network1() {
    }

    public void a(String var1) throws UnknownHostException {
        if(this.a == null) {
            this.a = d(var1);
        }

    }

    public void a(int var1) {
        if(this.e <= 0) {
            throw new IllegalArgumentException("cache update interval must greater than 0 !");
        } else {
            this.e = var1;
        }
    }

    public void a(Context var1) throws UnknownHostException {
        if(this.g == null) {
            this.b = var1.getApplicationContext();
            if(this.a == null) {
                this.a = d("119.29.29.29");
            }

            this.g = new HandlerThread("DNSCacheManager");
            this.g.start();
            this.f = new Handler(this.g.getLooper(), this.i);
            IntentFilter var2 = new IntentFilter();
            var2.addAction("android.intent.action.ANY_DATA_STATE");
            var1.getApplicationContext().registerReceiver(this.h, var2);
            this.f.sendEmptyMessage(0);
            Log.d("DNSCacheManager", "startCacheService !");
        }
    }

    public void a(Context var1, String[] var2) throws UnknownHostException {
        if(this.g == null) {
            String[] var3 = var2;
            int var4 = var2.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String var6 = var3[var5];
                this.d.put(var6, null);
            }

            this.a(var1);
        }
    }

    public void b(Context var1) {
        if(this.g != null) {
            var1.getApplicationContext().unregisterReceiver(this.h);
            this.g.interrupt();
            this.g.quit();
            this.g = null;
            Object var2 = this.c;
            synchronized(this.c) {
                this.d.clear();
            }

            Log.d("DNSCacheManager", "stopCacheService !");
        }
    }

    public String b(String var1) {
        if(var1 != null && this.g != null) {
            Uri var2 = Uri.parse(var1);
            return this.a(var2).toString();
        } else {
            return var1;
        }
    }

    public Uri a(Uri var1) {
        if(var1 != null && this.g != null) {
            String var2 = var1.getScheme();
            String var3 = var1.getHost();
            if(var3 != null && var2 != null && !var1.toString().contains(".m3u8")) {
                if(!var2.equalsIgnoreCase("rtmp") && !var2.equalsIgnoreCase("http")) {
                    return var1;
                } else {
                    Object var5 = this.c;
                    Network1.a var4;
                    synchronized(this.c) {
                        if(!this.d.containsKey(var3)) {
                            this.d.put(var3, null);
                            this.f.sendEmptyMessage(2);
                            return var1;
                        }

                        var4 = (Network1.a)this.d.get(var3);
                    }

                    if(var4 != null && var4.a.length > 0) {
                        var4.b = (var4.b + 1) % var4.a.length;
                        String var8 = var1.toString();
                        if(var8.contains("?")) {
                            var8 = var8.replaceFirst(var3, var4.a[var4.b]) + "&domain=" + var3;
                        } else {
                            var8 = var8.replaceFirst(var3, var4.a[var4.b]) + "?domain=" + var3;
                        }

                        return Uri.parse(var8);
                    } else {
                        return var1;
                    }
                }
            } else {
                return var1;
            }
        } else {
            return var1;
        }
    }

    private void a() {
        if(this.a != null && !this.d.isEmpty() && Util.isNetworkConnected(this.b) && !c(this.b)) {
            Object var2 = this.c;
            Object[] var1;
            synchronized(this.c) {
                var1 = this.d.keySet().toArray();
            }

            Object[] var12 = var1;
            int var3 = var1.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object var5 = var12[var4];
                String var6 = (String)var5;
                String[] var7 = this.c(var6);
                if(var7 != null && var7.length > 0) {
                    Object var8 = this.c;
                    synchronized(this.c) {
                        this.d.put(var6, new Network1.a(var7, 0));
                    }
                }
            }

        }
    }

    private String[] c(String var1) {
        try {
            return this.a.query(var1);
        } catch (IOException var3) {
            return null;
        }
    }

    private void b() {
        Object var1 = this.c;
        synchronized(this.c) {
            Object[] var2 = this.d.keySet().toArray();
            this.d.clear();
            Object[] var3 = var2;
            int var4 = var2.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Object var6 = var3[var5];
                this.d.put((String)var6, null);
            }

        }
    }

    private static DnsManager d(String var0) throws UnknownHostException {
        DnspodFree var1 = new DnspodFree();
        IResolver var2 = AndroidDnsServer.defaultResolver();
        Resolver var3 = new Resolver(InetAddress.getByName(var0));
        return new DnsManager(NetworkInfo.normal, new IResolver[]{var1, var2, var3});
    }

    public static boolean c(Context var0) {
        if(var0 == null) {
            return true;
        } else {
            String var1 = var0.getPackageName();
            if(var1 == null) {
                return true;
            } else {
                ActivityManager var2 = (ActivityManager)var0.getSystemService(Context.ACTIVITY_SERVICE);
                List var3 = var2.getRunningAppProcesses();
                if(var3 == null) {
                    return true;
                } else {
                    Iterator var4 = var3.iterator();

                    RunningAppProcessInfo var5;
                    do {
                        if(!var4.hasNext()) {
                            return false;
                        }

                        var5 = (RunningAppProcessInfo)var4.next();
                    } while(!var1.equals(var5.processName));

                    if(var5.importance != 100) {
                        Log.i(var0.getPackageName(), "Background" + var5.processName);
                        return true;
                    } else {
                        Log.i(var0.getPackageName(), "Foreground" + var5.processName);
                        return false;
                    }
                }
            }
        }
    }

    private class a {
        public String[] a;
        public int b;

        public a(String[] var2, int var3) {
            this.a = var2;
            this.b = var3;
        }
    }
}