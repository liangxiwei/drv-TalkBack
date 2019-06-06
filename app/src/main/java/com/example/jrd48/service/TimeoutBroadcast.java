package com.example.jrd48.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

/**
 * Created by qhb on 16-12-1.
 */

public class TimeoutBroadcast {
    private static final String TIME_OUT_ACTION = "timeout";
    public static final int TIME_OUT_IIME = 60;

    Context context;
    IntentFilter filter;
    BroadcastReceiver mReceiver;
    ITimeoutBroadcast mPostAction;
    TimeoutBroadcastManager manager = null;
    private MyLogger mLog = MyLogger.jLog();

    private String mTimeoutAction = "";


    /**
     * 超时广播，推荐使用此函数，来构造超时广播对象
     *
     * @param context
     * @param filter
     * @param manager
     */
    @SuppressWarnings("deprecation")
    public TimeoutBroadcast(Context context, IntentFilter filter, TimeoutBroadcastManager manager) {
        this(context, filter);
        this.manager = manager;
        manager.add(this);
    }

    /**
     * @param context
     * @param filter
     * 超时广播, 此函数已不推荐使用，请使用 TimeoutBroadcast(context, filter, manager).
     */
    private TimeoutBroadcast(Context context, IntentFilter Afilter) {
        this.context = context;
        this.filter = Afilter;

        final String action = filter.getAction(0);
        if (action == null) {
            throw new RuntimeException("timeoutbroadcast action in filter is null");
        }

        mTimeoutAction = action + "_" + TIME_OUT_ACTION;
        this.filter.addAction(mTimeoutAction);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(mTimeoutAction)) {
                    mLog.i("广播超时: "+action);
                    try {
//                        MyService.restart(context);
                        mPostAction.onTimeout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mLog.i("接收到广播: "+action);
                    try {
                        mPostAction.onGot(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 自动停止
                stop();
            }
        };
    }


    /**
     * timeout 秒数
     */
    public void startReceiver(int timeout, ITimeoutBroadcast func) {
        this.mPostAction = func;
        context.registerReceiver(mReceiver, filter);

        //new RuntimeException("test").printStackTrace();

        // 启动Alarm 发送心跳包
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long nextTime = SystemClock.elapsedRealtime() + timeout * 1000;
        Intent intent = new Intent(mTimeoutAction);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        // 先取消，再开始
        alarm.cancel(pi);
        alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pi);
    }

    /**
     * 停止广播
     */
    public void stop() {
        try {

            if (mReceiver != null) {
                mLog.i("广播停止: " + filter.getAction(0));
                // 停止计时
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(mTimeoutAction);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
                // 先取消，再开始
                alarm.cancel(pi);

                context.unregisterReceiver(mReceiver);
                mReceiver = null;
            } else {
                mLog.i("广播未启动，无需要停止: " + filter.getAction(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (manager != null) {
            manager.remove(this);
        }
    }
}
