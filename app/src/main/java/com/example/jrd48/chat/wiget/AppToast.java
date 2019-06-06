package com.example.jrd48.chat.wiget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.timer.CountDown;


/**
 * Created by Administrator on 2017/12/1.
 */

public class AppToast {
    private ViewGroup layout;
//    private ViewGroup content;
    private TextView textView;
    private boolean isShow;
    private boolean isAdd;
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private CountDown countDown;
    private Handler handler;
    /**
     * APP级别Toast
     */
    public AppToast(Context application) {
        handler = new Handler(Looper.getMainLooper());
        wm = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        layout = (ViewGroup) LayoutInflater.from(application).inflate(R.layout.layout_toast, null);
//        content = (ViewGroup) layout.getChildAt(0);
        textView = (TextView) layout.getChildAt(0);
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.BOTTOM;
        params.y = 150;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        layout.setVisibility(View.GONE);
    }

    public void setGravity(int gravity,int x,int y){
        params.gravity = gravity;
    }


    /**
     * 显示Toast
     */
    public void show(String s) {
        show(s, 2500);
    }

    /**
     * 显示Toast
     */
    public void show(final String s,final int delay) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                layout.setVisibility(View.GONE);
                textView.setText(s);
                if(!isAdd) {
                    isAdd = true;
                    wm.addView(layout, params);
                }
                layout.setVisibility(View.VISIBLE);
                startCountDown(true,delay);
            }
        });
    }

    public void startCountDown(boolean isCountDown,long time) {
        if(countDown != null){
            countDown.cancel();
        }

        if(isCountDown) {
            countDown = new CountDown(time, 500) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    end();
                }
            }.start();
        }
    }

    /**
     * 结束
     */
    private void end() {
        layout.setVisibility(View.GONE);
        try {
            wm.removeView(layout);
        } catch (Exception e){
            e.printStackTrace();
        }
        isAdd = false;
    }
}
