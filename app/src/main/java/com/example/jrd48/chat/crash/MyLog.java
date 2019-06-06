package com.example.jrd48.chat.crash;

import android.util.Log;

/**
 * Created by quhuabo on 2016/9/24 0024.
 */

public class MyLog {
    final static String APPTAG = "CHAT_POC";

    public static void i(String str) {
        Log.i(APPTAG, Thread.currentThread().getStackTrace()[3] + " --- " + str);
    }

    public static void e(String str) {
        Log.e(APPTAG, Thread.currentThread().getStackTrace()[3] + " *** " + str);
    }

    public static void w(String str) {
        Log.w(APPTAG, Thread.currentThread().getStackTrace()[3] + " *** " + str);
    }

    public static void e(String str, Throwable e) {
        Log.e(APPTAG, "[ Exception ]" + str + " >>> " + Thread.currentThread().getStackTrace()[3] + " *** " + e.toString());
    }
}
