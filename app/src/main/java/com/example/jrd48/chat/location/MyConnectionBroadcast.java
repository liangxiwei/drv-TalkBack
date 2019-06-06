package com.example.jrd48.chat.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyBroadcastObject;

public class MyConnectionBroadcast extends MyBroadcastObject {
    public MyConnectionBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName("com.example.jrd48.chat.location.MyConnectionBroadcast");
    }
}
