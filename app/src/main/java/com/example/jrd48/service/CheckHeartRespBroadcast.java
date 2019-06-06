package com.example.jrd48.service;

import android.content.Context;

import com.example.jrd48.chat.crash.MyApplication;

/**
 * Created by Administrator on 2017/1/19 0019.
 */

public class CheckHeartRespBroadcast extends MyBroadcastObject {
    public CheckHeartRespBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName("com.example.jrd48.chat.check_heart_resp");
    }
}
