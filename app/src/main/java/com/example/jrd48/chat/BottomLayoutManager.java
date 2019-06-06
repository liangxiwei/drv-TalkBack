package com.example.jrd48.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.MyService;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.timer.CountDown;
import com.luobin.utils.ButtonUtils;
import com.luobin.voice.VoiceHandler;

import me.lake.librestreaming.client.RESClient;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Administrator on 2017/9/22.
 */

public class BottomLayoutManager {
    private final static String TAG = "BottomLayoutManager";
    public static final String ACTION_VIDEO_CONTROL_SHOW = "com.luobin.dvr.action.ACTION_VIDEO_CONTROL_SHOW";
    private WindowManager mWindowManager;
    private View view;
    private View titleView;
    private FirstActivity activity;
    private ObjectAnimator o;
    private ObjectAnimator o2;
    private ObjectAnimator titleO;
    private ObjectAnimator titleO2;
    private boolean isShow;

    public TextView titleText;
    private CountDown countDownTimer;
    public ImageButton switchVideo;
    public TextView switchVideoText;
    public ImageButton close;
    public ImageButton switchCamera;
    public ImageButton ptt;
    public ImageButton mButtonVideo;
    public TextView mButtonVideoText;
    public ImageButton mButtonVoice;
    public TextView mButtonVoiceText;
    private static boolean isUP = true;
    private static long lastClickDown = 0;

    public BottomLayoutManager(FirstActivity mActivity) {
        this.activity = mActivity;
        mWindowManager = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = LayoutInflater.from(MyApplication.getContext());
        view = layoutInflater.inflate(R.layout.chat_bottom_layout, null);
        titleView = layoutInflater.inflate(R.layout.chat_title_layout, null);
        titleView.setFocusable(false);
        titleView.setClickable(false);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.i(TAG, "keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && isUP) {
                    isUP = false;
                    lastClickDown = SystemClock.elapsedRealtime();
                } else if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    isUP = true;
                    if (SystemClock.elapsedRealtime() - lastClickDown < 1500) {
                        activity.onBackPressed();
                    }
                } else {
                    startCountDown();
                }
                return false;
            }
        });
        float height = MyApplication.getContext().getResources().getDimension(R.dimen.bottom_layout_height);
        o2 = ObjectAnimator.ofFloat(view, "translationY", height, 0f);
        o2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startCountDown();
                o2.cancel();
            }
        });
        o = ObjectAnimator.ofFloat(view, "translationY", 0f, height);
        o.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(GONE);
                removeView();
            }
        });
        float titleHeight = MyApplication.getContext().getResources().getDimension(R.dimen.title_layout_height);
        titleO2 = ObjectAnimator.ofFloat(titleView, "translationY", -titleHeight, 0f);
        titleO2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                startCountDown();
//                titleO2.cancel();
            }
        });
        titleO = ObjectAnimator.ofFloat(titleView, "translationY", 0f, -titleHeight);
        titleO.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                removeView();
                titleView.setVisibility(GONE);
            }
        });
        initView2();
    }

    private void initView2() {
        //switchVideo = (ImageButton) view.findViewById(R.id.switch_video);
        //switchVideoText = (TextView) view.findViewById(R.id.switch_video_text);

        mButtonVideo = (ImageButton) view.findViewById(R.id.button_video);
        mButtonVideoText = (TextView) view.findViewById(R.id.video_text);
        mButtonVoice = (ImageButton) view.findViewById(R.id.button_voice);
        mButtonVoiceText = (TextView) view.findViewById(R.id.video_text);

        close = (ImageButton) view.findViewById(R.id.close);
        switchCamera = (ImageButton) view.findViewById(R.id.switch_camera);
        ptt = (ImageButton) view.findViewById(R.id.ptt);
        //ButtonUtils.setKeyListener(switchVideo, this, activity);

        ButtonUtils.setKeyListener(mButtonVideo, this, activity);
        ButtonUtils.setKeyListener(mButtonVoice, this, activity);

        ButtonUtils.setKeyListener(close, this, activity);
        ButtonUtils.setKeyListener(switchCamera, this, activity);
        ButtonUtils.setKeyListener(ptt, this, activity);
        ButtonUtils.setKeyListener(view, this, activity);
        final SharedPreferences preference = MyApplication.getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preference.edit();

        mButtonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (GlobalStatus.IsRandomChat()) {
                //    ToastR.setToast(activity, "请在九方助手中进行海聊群设置");
                //} else {
                    //editor.putInt("team_random_video", 0);
                    //editor.apply();
                    GlobalStatus.setIsVideo(true);
                    GlobalStatus.changeChatStatusInfo();
                    activity.changeTitle();
                    if (GlobalStatus.checkSpeakPhone(activity.getMyPhone(), activity.getRoomId())) {
                        VoiceHandler.startRTMP(activity, GlobalStatus.getCurRtmpAddr());
                    }
                    updateChatModeSelection();
                //}
            }
        });
        mButtonVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (GlobalStatus.IsRandomChat()) {
                //    ToastR.setToast(activity, "请在九方助手中进行海聊群设置");
                //} else {
                    //editor.putInt("team_random_video", 1);
                    //editor.apply();
                    GlobalStatus.setIsVideo(false);
                    activity.changeTitle();
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_PLAY, null);
                    updateChatModeSelection();
                //}
            }
        });

//        switchVideo.setText(GlobalStatus.isVideo() ? R.string.switch_to_voice : R.string.switch_to_video);
        /*switchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GlobalStatus.IsRandomChat()){
                    ToastR.setToast(activity, "请在九方助手中进行海聊群设置");
                }else{
                    if (GlobalStatus.isVideo()) {
                        GlobalStatus.setIsVideo(false);
                        DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                        DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_PLAY, null);
                        switchCamera.setVisibility(GONE);

                        activity.changeTitle();
                        switchVideoText.setText("语音模式");
                    } else {
                        GlobalStatus.setIsVideo(true);
                        GlobalStatus.changeChatStatusInfo();
                        activity.changeTitle();

                        if (GlobalStatus.checkSpeakPhone(activity.getMyPhone(), activity.getRoomId())) {
                            VoiceHandler.startRTMP(activity, GlobalStatus.getCurRtmpAddr());
                        }
                        switchVideoText.setText("视频模式");
//
//                    switchCamera.setVisibility(VISIBLE);
                    }

//                switchVideo.setText(GlobalStatus.isVideo() ? R.string.switch_to_voice : R.string.switch_to_video);

                }

            }
        });*/
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.HungupClick();
            }
        });
        //updateSwitchText();
        updateChatModeSelection();
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DvrService.start(activity, RESClient.ACTION_SWITCH_RTMP, null);
            }
        });
        ptt.setEnabled(true);
        ptt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.i(TAG, "keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && isUP) {
                    isUP = false;
                    lastClickDown = SystemClock.elapsedRealtime();
                } else if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    isUP = true;
                    if (SystemClock.elapsedRealtime() - lastClickDown < 1500) {
                        activity.onBackPressed();
                    }
                } else if (keyCode != KeyEvent.KEYCODE_F6) {

                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            startCountDown(false);
                            ptt.setImageResource(R.drawable.btn_ptt_pressed);
                            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pocdemo");
                            wl.setReferenceCounted(false);
                            wl.acquire(5000);
                            if (GlobalStatus.getChatRoomMsg() != null) {
                                Intent service = new Intent(activity, MyService.class);
                                service.putExtra("ptt_key_action", true);
                                activity.startService(service);
                            }
                            return true;
                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            startCountDown();
                            ptt.setImageResource(R.drawable.btn_ptt_selector);
                            //抬起操作
                            if (GlobalStatus.getChatRoomMsg() != null) {
                                Intent service = new Intent(activity, MyService.class);
                                service.putExtra("ptt_key_action", false);
                                activity.startService(service);
                            }
                            return true;
                        } else {
                            startCountDown();
                        }
                    }
                }
                return false;
            }
        });
        ptt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startCountDown(false);
                    ptt.setImageResource(R.drawable.btn_ptt_pressed);
                    PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pocdemo");
                    wl.setReferenceCounted(false);
                    wl.acquire(5000);
                    if (GlobalStatus.getChatRoomMsg() != null) {
                        Intent service = new Intent(activity, MyService.class);
                        service.putExtra("ptt_key_action", true);
                        activity.startService(service);
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    startCountDown();
                    ptt.setImageResource(R.drawable.btn_ptt_selector);
                    //抬起操作
                    if (GlobalStatus.getChatRoomMsg() != null) {
                        Intent service = new Intent(activity, MyService.class);
                        service.putExtra("ptt_key_action", false);
                        activity.startService(service);
                    }
                    v.requestFocusFromTouch();
                    return true;
                } else {
                    startCountDown();
                }
                return false;
            }
        });
        titleText = (TextView) titleView.findViewById(R.id.title_text);
    }



    public void setPttEnable(boolean enable) {
        if (ptt != null) {
            ptt.setEnabled(enable);
        }
    }

    public void switchVoiceOrVideo(boolean isVideo){
        Log.d(TAG,"switchVoiceOrVideo isVideo="+isVideo);
        if(isVideo){
            GlobalStatus.setIsVideo(true);
            GlobalStatus.changeChatStatusInfo();
            activity.changeTitle();
            if (GlobalStatus.checkSpeakPhone(activity.getMyPhone(), activity.getRoomId())) {
                VoiceHandler.startRTMP(activity, GlobalStatus.getCurRtmpAddr());
            }
            //switchVideoText.setText("视频模式");
        }else{
            GlobalStatus.setIsVideo(false);
            switchCamera.setVisibility(GONE);
            activity.changeTitle();
            //switchVideoText.setText("语音模式");
        }
    }

    public void updateSwitchText() {
        if (GlobalStatus.isVideo()) {
            //switchVideoText.setText("视频模式");
//            switchCamera.setVisibility(VISIBLE);
        } else {
            //switchCamera.setVisibility(GONE);
            //switchVideoText.setText("语音模式");
        }
    }

    public void updateChatModeSelection() {
        if (GlobalStatus.isVideo()) {
            mButtonVideo.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_video_select_selector));
            mButtonVoice.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_voice_default_selector));
        } else {
            mButtonVideo.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_video_default_selector));
            mButtonVoice.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_voice_select_selector));
        }
        if (GlobalStatus.IsRandomChat()) {
            if (MyApplication.getContext().getSharedPreferences("token", Context.MODE_PRIVATE).getInt("team_random_video", 0) == 0) {
                mButtonVideo.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_video_select_selector));
                mButtonVoice.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_voice_default_selector));
            } else {
                mButtonVideo.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_video_default_selector));
                mButtonVoice.setImageDrawable(this.activity.getResources().getDrawable(R.drawable.btn_voice_select_selector));
            }
        }
    }

    public void show(boolean isLog) {
        if(activity != null && titleText != null){
            if(!TextUtils.isEmpty(activity.getChatName())){
                titleText.setText(activity.getChatName());
            } else {
                titleText.setText("");
            }
        }

        view.setVisibility(VISIBLE);
        titleView.setVisibility(VISIBLE);
        if (titleO2 != null && o2 != null && !isShow) {
            // 设置窗口类型，一共有三种Application windows, Sub-windows, System windows
            WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
            // API中以TYPE_开头的常量有23个
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            mWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            // 设置期望的bitmap格式
            mWindowParams.format = PixelFormat.TRANSLUCENT; //设置背景透明
            // 以下属性在Layout Params中常见重力、坐标，宽高
            mWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            float height = MyApplication.getContext().getResources().getDimension(R.dimen.bottom_layout_height);
            mWindowParams.height = (int) height;
            // 添加指定视图
            mWindowManager.addView(view, mWindowParams);

            WindowManager.LayoutParams mWindowParams2 = new WindowManager.LayoutParams();
            mWindowParams2.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            mWindowParams2.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            mWindowParams2.format = PixelFormat.TRANSLUCENT; //设置背景透明
            mWindowParams2.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            mWindowParams2.width = WindowManager.LayoutParams.MATCH_PARENT;
            float titleHeight = MyApplication.getContext().getResources().getDimension(R.dimen.title_layout_height);
            mWindowParams2.height = (int) titleHeight;
            mWindowManager.addView(titleView, mWindowParams2);

            close.post(new Runnable() {
                @Override
                public void run() {
                    close.requestFocusFromTouch();
                }
            });

            isShow = true;
            o2.setDuration(500).start();
            titleO2.setDuration(500).start();
        } else if(isLog){
            startCountDown(false);
            hide();
        }
    }


    public void hide() {
        if (titleO != null) {
            titleO.setDuration(500).start();
        }

        if (o != null) {
            o.setDuration(500).start();
        }
    }

    public void removeView() {
        if (isShow) {
            try {
                mWindowManager.removeView(view);
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
                mWindowManager.removeView(titleView);
            } catch (Exception e){
                e.printStackTrace();
            }
            isShow = false;
        }
        if (o != null) {
            o.cancel();
        }

        if (o2 != null) {
            o2.cancel();
        }

        if (titleO != null) {
            titleO.cancel();
        }

        if (titleO2 != null) {
            titleO2.cancel();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    public void startCountDown() {
        startCountDown(true);
    }

    public void startCountDown(boolean isCountDown) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (isCountDown) {
            countDownTimer = new CountDown(5000, 1000) {
                public void onTick(long millisUntilFinished) {
//                if(millisUntilFinished % 1000 != 0) {
//                    countText.setText((1 + millisUntilFinished / 1000) + "s");
//                } else {
//                    countText.setText(millisUntilFinished / 1000 + "s");
//                }
                }

                public void onFinish() {
                    hide();
                }
            }.start();
        }
    }
}
