package com.luobin.voice;

import android.content.Context;

import com.example.jrd48.service.MyBroadcastObject;


/**
 * Created by Administrator on 2016/10/22 0022.
 */

public class PttKeyBroadcast extends MyBroadcastObject {
    public PttKeyBroadcast(Context context) {
        super(context);
        setPackageName(getPackageName());
        setActionName("com.example.jrd48.action.start_voice");
    }
}
