package com.luobin.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.service.MyService;


public class PttKeyReceiver extends BroadcastReceiver {

    public final static int KEY_DOWN = 1;
    public final static int KEY_UP = 2;

    public PttKeyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean pttKeyDown = false;
        Log.v("wsDvr","PttKeyReceiver action:" + intent.getAction());
//        NotifyManager.getInstance().showNotification("123");
        if (intent.getAction().equals("com.android.action.ptt")) {
            pttKeyDown = (intent.getIntExtra("ptt_action", 0) == 1);
        } else if (intent.getAction().equals("com.agold.hy.ptt.down")) {
            pttKeyDown = true;
        }

        GlobalStatus.setPttBroadCast(pttKeyDown);
        if (pttKeyDown) {
            // 唤醒
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pocdemo");
            wl.setReferenceCounted(false);
            wl.acquire(5000);
        }

        if(GlobalStatus.getChatRoomMsg() != null) {
            Intent service = new Intent(context, MyService.class);

            service.putExtra("ptt_key_action", pttKeyDown);
            // record ptt status
            SharedPreferencesUtils.put(context, "pttKeyDown", pttKeyDown);
        /*
         改到z服务中运行

        if (pttKeyDown) {
            GlobalStatus.setPttKeyDown(true);
            // key down
            VoiceHandler.speakBeginAndRecording(context);
        } else {
            // key up
            GlobalStatus.setPttKeyDown(false);
            VoiceHandler.speakEndAndRecroding(context);
        }
        */
            context.startService(service);
            // set video chat default
            //GlobalStatus.setIsVideo(true);
        }

    }
}
