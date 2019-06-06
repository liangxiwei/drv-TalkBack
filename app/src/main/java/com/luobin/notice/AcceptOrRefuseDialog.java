package com.luobin.notice;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.jrd48.GlobalStatus;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.timer.CountDown;
import com.luobin.utils.ButtonUtils;

/**
 * Created by Administrator on 2017/8/8.
 */

public class AcceptOrRefuseDialog extends Dialog {
    public final static String DISMISS_ACTION = "com.luobin.notice.DIALOG_DISMISS";
    private AcceptOrRefuseInterface acceptOrRefuseInterface;
    private Button accept;
    private Button refuse;
    public AcceptOrRefuseDialog(@NonNull Context context, AcceptOrRefuseInterface acceptOrRefuseInterface) {
        super(context);
        this.acceptOrRefuseInterface = acceptOrRefuseInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_accept_or_refuse);
        accept = (Button) findViewById(R.id.accept);
        refuse = (Button) findViewById(R.id.refuse);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOrRefuseInterface.accept();
                dismiss();
            }
        });

        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOrRefuseInterface.refuse();
                dismiss();
            }
        });
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
    public void show() {
//        long teamdId= intent.getLongExtra("group",0);
//        String phone = intent.getStringExtra("linkmanPhone");
//        CallState callState = null;
//        if(teamdId != 0){
//            callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + teamdId);
//        } else {
//            callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + phone);
//        }
//
//        if(GlobalStatus.equalPhone(phone) || GlobalStatus.equalTeamID(teamdId) || (callState != null && callState.getState() > GlobalStatus.STATE_CLOSE)){
//            getContext().startActivity(intent);
//        } else{
            super.show();
//            IntentFilter intentFilter = new IntentFilter(DISMISS_ACTION);
//            getContext().registerReceiver(receiver, intentFilter);
//            countDownTimer = new CountDown(3000, 1000) {
//                public void onTick(long millisUntilFinished) {
//                    if (millisUntilFinished % 1000 != 0) {
//                        accept.setText(getContext().getString(R.string.video_call) + "(" + (1 + millisUntilFinished / 1000) + "s)");
//                    } else {
//                        accept.setText(getContext().getString(R.string.video_call) + "(" + millisUntilFinished / 1000 + "s)");
//                    }
//                }
//
//                public void onFinish() {
//                    accept.performClick();
//                }
//
//            }.start();
//        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
