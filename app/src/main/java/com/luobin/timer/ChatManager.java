package com.luobin.timer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.dvr.R;

/**
 * Created by Administrator on 2017/8/10.
 */

public class ChatManager {
    private final static String TAG = "ChatManager";
    private boolean isFinishing;
    private static ChatManager chatManager;
    private WindowManager.LayoutParams mWindowParams;
    private WindowManager mWindowManager;
    private View view;
    private long startTime;
    private TextView timeView;
    private TextView onlineView;
    private boolean isShow;
    final Handler mHandler = new Handler(Looper.getMainLooper());

    public synchronized static ChatManager getInstance() {
        if (chatManager == null) {
            chatManager = new ChatManager();
        }
        return chatManager;
    }

    Runnable mUpdateTimer = new Runnable() {
        public void run() {
            updateTimerView();
        }
    };

    public ChatManager() {
        mWindowManager = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public boolean isIsFinishing() {
        return isFinishing;
    }

    public void setIsFinishing(boolean isFinishing) {
        this.isFinishing = isFinishing;
    }

    public void showChatInfo(View locationView) {
        synchronized (ChatManager.class) {
            if (view != null) {
                return;
            }
            LayoutInflater layoutInflater = LayoutInflater.from(MyApplication.getContext());
            view = layoutInflater.inflate(R.layout.chat_info, null);
            timeView = (TextView) view.findViewById(R.id.time);
            onlineView = (TextView) view.findViewById(R.id.group_online_info);
            mWindowParams = new WindowManager.LayoutParams();
            // API中以TYPE_开头的常量有23个
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            mWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            ;
            // 设置期望的bitmap格式
            mWindowParams.format = PixelFormat.TRANSLUCENT; //设置背景透明

            // 以下属性在Layout Params中常见重力、坐标，宽高
            mWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            Rect location = new Rect();
            locationView.getLocalVisibleRect(location);
            mWindowParams.x = location.left;
            mWindowParams.y = location.top;

            mWindowParams.width = location.width();
            mWindowParams.height = location.height();
            mWindowManager.addView(view, mWindowParams);
            Log.v(TAG,"addView");
            updateTimerView();
        }
    }

    public void hideView() {
        synchronized (ChatManager.class) {
            if (view != null) {
                try {
                    mWindowManager.removeView(view);
                    Log.v(TAG,"removeView");
                    view = null;
                    onlineView = null;
                    timeView = null;
                    setShow(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mHandler != null && mUpdateTimer != null) {
                mHandler.removeCallbacks(mUpdateTimer);
            }
        }
    }

    private void updateTimerView() {
        if (onlineView == null || timeView == null) {
            Log.v(TAG,"onlineView Or timeView is null");
            return;
        }

        ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
        if(!isShow){
            timeView.setVisibility(View.GONE);
            onlineView.setVisibility(View.GONE);
        } else {
            if(chatRoomMsg != null) {
                onlineView.setText("在线:" + GlobalStatus.getOnlineCount() + "/" + chatRoomMsg.getMembersCount());
                onlineView.setVisibility(View.GONE);
            } else {
                onlineView.setVisibility(View.GONE);
            }

            if (startTime != 0) {
                long time = (SystemClock.elapsedRealtime() - getStartTime()) / 1000;
                String s = time % 60 + "";
                String m = time / 60 + "";

                if (s.length() == 1) {
                    s = "0" + s;
                }

                if (m.length() == 1) {
                    m = "0" + m;
                }
                String timeStr = m + ":" + s;
                timeView.setText(timeStr);
//                timeView.setVisibility(View.VISIBLE);//hide time ,layout gone
            } else {
                timeView.setVisibility(View.GONE);
            }
        }

        mHandler.postDelayed(mUpdateTimer, 1000);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        Log.v(TAG,"setStartTime:" + startTime);
        this.startTime = startTime;
    }

    public void setShow(boolean show) {
        Log.v(TAG,"setShow:" + show);
        isShow = show;
        if(!show && timeView != null && onlineView != null){
            timeView.setVisibility(View.GONE);
            onlineView.setVisibility(View.GONE);
        }
    }
}
