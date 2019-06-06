package com.example.jrd48.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyService;
import com.luobin.dvr.DvrService;
import com.luobin.utils.VideoRoadUtils;

/**
 * Created by Administrator on 2018/1/16.
 */

public class LauncherBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!GlobalStatus.isReceiveLauncher()) {
//            ToastR.setToast(MyApplication.getContext(),"LauncherBroadcastReceiver");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("wsDvr", "LauncherBroadcastReceiver startUSBCamera");
                    VideoRoadUtils.startUSBCamera();
                }
            }).start();
            GlobalStatus.setIsReceiveLauncher(true);
            DvrService.start(context);
            MyService.start(context);
        }
    }
}
