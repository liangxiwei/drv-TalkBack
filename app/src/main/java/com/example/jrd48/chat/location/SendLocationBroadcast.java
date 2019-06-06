package com.example.jrd48.chat.location;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyBroadcastObject;

import android.content.Context;

public class SendLocationBroadcast extends MyBroadcastObject {

    public SendLocationBroadcast(Context context) {
        super(context);
        setActionName("com.example.jrd48.chat.location.got_location");
        setPackageName(MyApplication.getContext().getPackageName());
    }

}
