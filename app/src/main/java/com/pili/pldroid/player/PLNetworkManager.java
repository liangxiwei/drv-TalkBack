//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player;

import android.content.Context;
import android.net.Uri;

import com.pili.pldroid.player.network.Network1;

import java.net.UnknownHostException;

public final class PLNetworkManager {
    private Network1 a;

    private PLNetworkManager() {
        this.a = new Network1();
    }

    public static PLNetworkManager getInstance() {
        return PLNetworkManager.newManager.a;
    }

    public void setDnsServer(String var1) throws UnknownHostException {
        this.a.a(var1);
    }

    public void setDnsCacheUpdateInterval(int var1) {
        this.a.a(var1);
    }

    public void startDnsCacheService(Context var1) throws UnknownHostException {
        this.a.a(var1);
    }

    public void startDnsCacheService(Context var1, String[] var2) throws UnknownHostException {
        this.a.a(var1, var2);
    }

    public void stopDnsCacheService(Context var1) {
        this.a.b(var1);
    }

    String a(String var1) {
        return this.a.b(var1);
    }

    Uri a(Uri var1) {
        return this.a.a(var1);
    }

    private static class newManager {
        public static final PLNetworkManager a = new PLNetworkManager();
    }
}
