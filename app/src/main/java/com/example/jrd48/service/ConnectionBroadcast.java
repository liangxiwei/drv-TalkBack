package com.example.jrd48.service;

import android.content.Context;

import com.example.jrd48.chat.crash.MyApplication;

/**
 * Created by qhb on 2016/10/18 0018.
 */

public class ConnectionBroadcast extends MyBroadcastObject {
    public ConnectionBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName("com.luobin.mychat.action.socket_conn_change");
    }
}
