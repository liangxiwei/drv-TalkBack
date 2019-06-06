package com.example.jrd48.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.jrd48.chat.ToastR;

/**
 * Created by Administrator on 2017/12/1.
 */

public class ToastReceiver extends BroadcastReceiver {
    public static final String TOAST_ACTION = "com.luobin.dvr.TOAST_ACTION";
    public static final String TOAST_CONTENT = "content";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(TOAST_ACTION)){
            String str = intent.getStringExtra(TOAST_CONTENT);
            if(str != null){
                Log.e(TOAST_CONTENT,"str:" + str);
                ToastR.setToast(context,str);
            }
        }
    }
}
