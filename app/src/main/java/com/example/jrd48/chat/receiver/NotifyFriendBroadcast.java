package com.example.jrd48.chat.receiver;

import android.content.Context;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyBroadcastObject;

/**
 * Created by Administrator on 2017/1/22.
 */

public class NotifyFriendBroadcast extends MyBroadcastObject {
    public NotifyFriendBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName("com.example.jrd48.chat.receiver.NotifyFriendBroadcast");
    }
}
