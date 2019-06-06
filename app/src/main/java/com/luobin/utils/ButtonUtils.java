package com.luobin.utils;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.example.jrd48.chat.BottomLayoutManager;
import com.example.jrd48.chat.FirstActivity;

/**
 * Created by Administrator on 2017/8/29.
 */

public class ButtonUtils {
    private final static String TAG = "ButtonUtils";
    private static boolean isUP = true;
    private static long lastClickDown = 0;
    public static void setViewScale(final View view) {

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setScaleX(0.9f);
                    view.setScaleY(0.9f);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }
                return false;
            }
        });

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        view.setScaleX(0.9f);
                        view.setScaleY(0.9f);
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (view.hasFocus()) {
                            view.setScaleX(1.1f);
                            view.setScaleY(1.1f);
                        } else {
                            view.setScaleX(1.0f);
                            view.setScaleY(1.0f);
                        }
                    }
                }
                return false;
            }
        });
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    view.setScaleX(1.1f);
                    view.setScaleY(1.1f);
                } else {
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                }
            }
        });
    }

    public static void setKeyListener(final View view, final BottomLayoutManager bottomLayoutManager, final FirstActivity activity) {

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                bottomLayoutManager.startCountDown();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.requestFocusFromTouch();
                }
                return false;
            }
        });

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.i(TAG, "keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && isUP) {
                    isUP = false;
                    lastClickDown = SystemClock.elapsedRealtime();
                } else if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    isUP = true;
                    if(SystemClock.elapsedRealtime() - lastClickDown < 1500) {
                        activity.onBackPressed();
                    }
                } else if (keyCode != KeyEvent.KEYCODE_F6) {
                    bottomLayoutManager.startCountDown();
                }

                return false;
            }
        });
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                bottomLayoutManager.startCountDown();
            }
        });
    }

    public static void changeLeftOrRight(final boolean isLeft) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(isLeft ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
                } catch (Exception e) {
                    Log.e("Utils", "Exception when onBack:" + e.toString());
                }
            }
        }.start();
    }

    public static void sendLeft() {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
                } catch (Exception e) {
                    Log.e("Utils", "Exception when onBack:" + e.toString());
                }
            }
        }.start();
    }
}
