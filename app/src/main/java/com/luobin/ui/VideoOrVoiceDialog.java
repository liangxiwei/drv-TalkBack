package com.luobin.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ConnectionChangeReceiver;
import com.luobin.model.CallState;
import com.luobin.timer.CountDown;
import com.luobin.dvr.R;
import com.luobin.utils.ButtonUtils;

/**
 * Created by Administrator on 2017/8/8.
 */

public class VideoOrVoiceDialog extends Dialog {
    public final static String DISMISS_ACTION = "android.luobin.action.DIALOG_DISMISS";
    private Intent intent;
    private Button video;
    private Button voice;
    private CountDown countDownTimer;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DISMISS_ACTION)){
                dismiss();
            }
        }
    };
    public VideoOrVoiceDialog(@NonNull Context context, Intent intent) {
        super(context);
        this.intent = intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_video_or_voice);
        video = (Button) findViewById(R.id.video_call);
        voice = (Button) findViewById(R.id.voice_call);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalStatus.setIsVideo(true);
                getContext().startActivity(intent);
                dismiss();
            }
        });

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalStatus.setIsVideo(false);
                getContext().startActivity(intent);
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        if(!ConnUtil.isConnected(getContext())){
            ToastR.setToast(getContext(),"连接服务器失败，请检查网络连接");
            return;
        }
        long teamdId= intent.getLongExtra("group",0);
        String phone = intent.getStringExtra("linkmanPhone");
        CallState callState = null;
        if(teamdId != 0){
            callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + teamdId);
        } else {
            callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + phone);
        }
        getContext().startActivity(intent);
//        if(GlobalStatus.equalPhone(phone) || GlobalStatus.equalTeamID(teamdId) || (callState != null && callState.getState() > GlobalStatus.STATE_CLOSE)){
//            getContext().startActivity(intent);
//        } else{
//            super.show();
//            IntentFilter intentFilter = new IntentFilter(DISMISS_ACTION);
//            getContext().registerReceiver(receiver, intentFilter);
//            countDownTimer = new CountDown(3000, 1000) {
//                public void onTick(long millisUntilFinished) {
//                    if (millisUntilFinished % 1000 != 0) {
//                        video.setText(getContext().getString(R.string.video_call) + "(" + (1 + millisUntilFinished / 1000) + "s)");
//                    } else {
//                        video.setText(getContext().getString(R.string.video_call) + "(" + millisUntilFinished / 1000 + "s)");
//                    }
//                }
//
//                public void onFinish() {
//                    video.performClick();
//                }
//
//            }.start();
//        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                ButtonUtils.changeLeftOrRight(true);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                ButtonUtils.changeLeftOrRight(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        getContext().unregisterReceiver(receiver);
        countDownTimer.cancel();
    }
}
