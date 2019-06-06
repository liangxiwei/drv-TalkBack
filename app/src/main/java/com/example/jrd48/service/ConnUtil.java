package com.example.jrd48.service;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SharedPreferencesUtils;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Administrator on 2016/10/18 0018.
 */

public class ConnUtil {
    public static boolean isConnected(Context context) {
//        ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
//        if (cm == null)
//            return false;
//
//        boolean bConnected = false;
//        // 有网络
//        NetworkInfo ni = cm.getActiveNetworkInfo();
//        if (ni != null) {
//            if (ni.isConnected()) {
//                bConnected = true;
//
//            } else {
//                // 没有网络
//                bConnected = false;
//
//
//            }
//        }
//        return bConnected;
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean checkNetworkWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public static String getDeviceId(Context context) {
        String oldImei = (String) SharedPreferencesUtils.get(context,"custom_imei","");
        String imei = null;
        if(TextUtils.isEmpty(oldImei)) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                imei = tm.getDeviceId();
            } catch (Exception e){
                e.printStackTrace();
            }
            if (imei != null && imei.length() == 15) {
                imei = imei.substring(0, 14);
            }

            if (GlobalStatus.getRetryCount() > 20 && (TextUtils.isEmpty(imei) || imei.length() < 10)) {
                imei = radomImei();
                SharedPreferencesUtils.put(context,"custom_imei",imei);
            }
        } else {
            imei = oldImei;
        }
//        String imei = null;
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        try {
//            imei = tm.getDeviceId();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (imei != null && imei.length() == 15) {
//            imei = imei.substring(0, 14);
//        }
        return imei;
    }

    public static String radomImei(){
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        for(int i=0; i<12;i++){
            buffer.append(random.nextInt(10));
        }
        String imei = "AA" + buffer.toString();
        return imei;
    }

    public static void screenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);// init powerManager
        PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|
                PowerManager.FULL_WAKE_LOCK,"target");
        mWakelock.acquire(1000); // Wake up Screen and keep screen lighting
    }

    public static String getSimIccid(Context context){
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String iccid = mTelephonyManager.getSimSerialNumber();
        if(iccid == null){
            return null;
        }
         return iccid;
    }
}
