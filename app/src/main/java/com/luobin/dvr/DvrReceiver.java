package com.luobin.dvr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.SharedPreferencesUtils;

public class DvrReceiver extends BroadcastReceiver {
    private static final String TAG = "DvrReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
////            if(GlobalStatus.getUsbVideo1() != null){
////                GlobalStatus.getUsbVideo1().close();
////                GlobalStatus.setUsbVideo1(null);
////            }
//
//            if(GlobalStatus.getUsbVideo2() != null){
//                GlobalStatus.getUsbVideo2().close();
//                GlobalStatus.setUsbVideo2(null);
//            }
            Log.d(TAG, "DvrReceiver:boot completed");
            SharedPreferencesUtils.put(context, "group_booting", true);
            SharedPreferencesUtils.put(context, "member_booting", true);
        }

    }

}
