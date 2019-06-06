//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.report.core;

public final class Core3 {
    private Object a = new Object();
    private StringBuilder b = new StringBuilder();

    public Core3() {
    }

    public static Core3 a() {
        return Core3.newCore.a;
    }

    public void b() {
        Object var1 = this.a;
        synchronized(this.a) {
            this.b.delete(0, this.b.length());
        }
    }

    public String c() {
        if(this.b != null && this.b.length() != 0) {
            Object var1 = this.a;
            synchronized(this.a) {
                return this.b.toString();
            }
        } else {
            return null;
        }
    }

    public boolean a(String var1) {
        if(var1 != null && !var1.equals("")) {
            if(this.b.length() > 65536) {
                return false;
            } else {
                try {
                    Object var2 = this.a;
                    synchronized(this.a) {
                        this.b.append(var1);
                    }
                } catch (OutOfMemoryError var5) {
                    var5.printStackTrace();
                }

                return true;
            }
        } else {
            return false;
        }
    }

    private static class newCore {
        public static final Core3 a = new Core3();
    }
}
