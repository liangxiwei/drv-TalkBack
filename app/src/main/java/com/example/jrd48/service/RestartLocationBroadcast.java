package com.example.jrd48.service;

import android.content.Context;

import com.example.jrd48.chat.crash.MyApplication;

/**
 * Created by qhb on 17-3-16.
 */

public class RestartLocationBroadcast extends MyBroadcastObject {
    public RestartLocationBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName("com.luobin.pocdemo.action.restart_location_broadcast");
    }
}
