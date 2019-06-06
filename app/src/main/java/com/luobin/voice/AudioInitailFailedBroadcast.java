package com.luobin.voice;

import android.content.Context;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyBroadcastObject;

/**
 * Created by qhb on 17-1-12.
 */

public class AudioInitailFailedBroadcast extends MyBroadcastObject {
    public AudioInitailFailedBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName("action.audio_initial_failed");

    }
}
