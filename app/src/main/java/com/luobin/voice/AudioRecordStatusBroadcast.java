package com.luobin.voice;

import android.content.Context;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyBroadcastObject;

/**
 * Created by quhuabo on 2017/1/20 0020.
 */

public class AudioRecordStatusBroadcast extends MyBroadcastObject {
    public AudioRecordStatusBroadcast(Context context) {
        super(context);
        setPackageName(MyApplication.getContext().getPackageName());
        setActionName(getPackageName() + ".action.audio_recorder_status_changed");
    }
}
