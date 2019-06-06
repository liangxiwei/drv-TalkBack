package com.example.jrd48.service.notify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.Utils;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.VoiceAcceptProcesser;
import com.luobin.timer.CountDown;
import com.luobin.dvr.R;
import com.luobin.ui.OtherVideoSetting;
import com.luobin.utils.ButtonUtils;
import com.luobin.utils.VideoRoadUtils;
import com.luobin.voice.SoundPoolTool;
import com.luobin.widget.MaxHeightListView;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/8/3.
 */

public class NotifyManager {
    private final static String TAG = "NotifyManager";
    public final static int BASE_REQUEST_CODE = 100;
    public final static int VIDEO_CANCEL_REQUEST_CODE = BASE_REQUEST_CODE + 1;
    public final static String ACTION_REQUEST_TIMEOUT = "android.luobin.action.ACTION_REQUEST_TIMEOUT";
    private static NotifyManager notifyManager;
    private Handler handler;
    private WindowManager mWindowManager;
    private View view;
    private GridView gridView;
    private TextView countText;
    private NotifyAdapter notifyAdapter;
    private ObjectAnimator o;
    private ObjectAnimator o2;
    private CountDown countDownTimer;

    public synchronized static NotifyManager getInstance() {
        if (notifyManager == null) {
            notifyManager = new NotifyManager();
        }
        return notifyManager;
    }

    public void changeName(String phone, AppliedFriends af){
        if(notifyAdapter != null && notifyAdapter.getShowNames().containsKey("0" + phone)){
            String linkmanName = "";
            if(af != null){
                linkmanName = af.getNickName();
                if(TextUtils.isEmpty(linkmanName)){
                    linkmanName = af.getUserName();
                }
            }
            notifyAdapter.getShowNames().put("0" + phone,linkmanName);
        }
    }

    public NotifyManager() {
        handler = new Handler(Looper.getMainLooper());
        mWindowManager = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = LayoutInflater.from(MyApplication.getContext());
        view = layoutInflater.inflate(R.layout.layout_notify, null);
        gridView = (GridView) view.findViewById(R.id.notify_list);
        countText = (TextView) view.findViewById(R.id.time_count_down);
        if (notifyAdapter == null) {
            notifyAdapter = new NotifyAdapter();
        }
        gridView.setAdapter(notifyAdapter);
        gridView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        startCountDown();
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        onFinish();
                    case KeyEvent.KEYCODE_ENTER:
                        Log.e(TAG,"position:" + gridView.getSelectedItemPosition());
                    default:
                        break;

                }
                return false;
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "onItemClick position:" + position);
                Context context = MyApplication.getContext();
                if (notifyAdapter != null) {
                    String temp = notifyAdapter.getNames().get(position);
                    if (notifyAdapter.getTypes().get(position) == 0 || notifyAdapter.getTypes().get(position) == 2) {
                        Intent intent = new Intent(MyApplication.getContext(), FirstActivity.class);
                        intent.putExtra("callType", 1);
                        if (temp.startsWith("0")) {
                            intent.putExtra("data", 1);
                            intent.putExtra("callType", 1);
                            String linkmanName = notifyAdapter.getShowNames().get(temp);
                            String linkmanPhone = temp.substring(1);
                            String name = TextUtils.isEmpty(linkmanName) ? linkmanPhone:linkmanName;
                            intent.putExtra("linkmanName", name);
                            intent.putExtra("linkmanPhone", linkmanPhone);
                        } else {
                            intent.putExtra("data", 1);
                            intent.putExtra("group", Long.valueOf(temp.substring(1)));
                            DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                            TeamInfo t = db.getTeamInfo(Long.valueOf(temp.substring(1)));
                            db.closeDB();
                            if(t != null) {
                                intent.putExtra("type", t.getMemberRole());
                            }
                            intent.putExtra("callType", 1);
                            intent.putExtra("group_name", notifyAdapter.getShowNames().get(temp));
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MyApplication.getContext().startActivity(intent);
                    } else if (notifyAdapter.getTypes().get(position) == 1) {
                        if (GlobalStatus.getChatRoomMsg() != null) {
                            showRoadNotifyDialog(context, temp);
//                            return;
                        } else {
                            VideoRoadUtils.AcceptLiveCall(context, temp.substring(1));
                            if(!Utils.getRoadVideoActivity(context)){
                                Intent intent = new Intent(context, OtherVideoSetting.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        }
                    }
                    removeNames(temp);
                }
            }
        });
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    startCountDown();
                }
                return false;
            }
        });
        float height = MyApplication.getContext().getResources().getDimension(R.dimen.notify_layout_height);
        o2 = ObjectAnimator.ofFloat(view, "translationY", 0f, -height);
        o2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "o2 onAnimationEnd");
                view.setVisibility(View.GONE);
                countText.setText("10s");
                try {
                    mWindowManager.removeView(view);
                } catch (Exception e){
                    e.printStackTrace();
                }
                notifyAdapter.getNames().clear();
                notifyAdapter.getTypes().clear();
                notifyAdapter.notifyDataSetChanged();
//                o2.cancel();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                super.onAnimationEnd(animation);
            }
        });
        o = ObjectAnimator.ofFloat(view, "translationY", -height, 0f);
        o.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startCountDown();
//                o.cancel();
            }
        });
    }

    public void removeNames(String temp) {
        if (notifyAdapter != null && notifyAdapter.getNames().contains(temp)) {
            int position = notifyAdapter.getNames().indexOf(temp);
            notifyAdapter.getNames().remove(position);
            notifyAdapter.getTypes().remove(position);
            notifyAdapter.notifyDataSetChanged();

            if (notifyAdapter.getNames().size() == 0) {
                onFinish();
            }
        }
    }

    public void showRoadNotifyDialog(final Context context, final String temp) {
        String title;
        String message;
        title = "当前正在对讲";
        String showPhone = DBManagerFriendsList.getAFriendNickName(MyApplication.getContext(),temp.substring(1));
        if(Pattern.matches("\\d{11}",showPhone)){
            showPhone = showPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        message = "请确定是否要结束对讲并开启对用户 " + showPhone + " 的路况分享";
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.CustomDialog);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endCall(context);
                VideoRoadUtils.AcceptLiveCall(context, temp.substring(1));
                removeNames(temp);
                if(!Utils.getRoadVideoActivity(context)){
                    Intent intent = new Intent(context, OtherVideoSetting.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog simplelistdialog = builder.create();
        simplelistdialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        simplelistdialog.show();

        Button ok = simplelistdialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if(ok != null){
            ok.setTextSize(24);
        }

        Button cancel = simplelistdialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if(cancel != null){
            cancel.setTextSize(24);
        }
    }

    public static void endCall(final Context context) {
        final long roomId = GlobalStatus.getRoomID();
        ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
        builder.setRoomID(roomId);
        builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
        MyService.start(context, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(VoiceAcceptProcesser.ACTION);
        new TimeoutBroadcast(context, filter, new TimeoutBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    saveMsgRemind("房间被成功挂断");
                    Intent intent = new Intent(AutoCloseProcesser.ACTION);

                    GlobalStatus.equalRoomID(roomId);
                    intent.putExtra("error_code", ProtoMessage.ErrorCode.OK.getNumber());
                    intent.putExtra("roomID", roomId);
                    context.sendBroadcast(intent);
                    NotificationManager nm = (NotificationManager) (context.getSystemService(Context.NOTIFICATION_SERVICE));
                    nm.cancel(-1);//消除对应ID的通知
                }
            }
        });
    }

    public void onFinish() {
        if (o2 != null && !o2.isStarted()) {
            o2.setDuration(500);//执行0.5秒 ， 延迟3秒
            o2.start();
            Log.v(TAG, "o2 onAnimation start");
        }
    }

    public void startCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDown(10 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished % 1000 != 0) {
                    countText.setText((1 + millisUntilFinished / 1000) + "s");
                } else {
                    countText.setText(millisUntilFinished / 1000 + "s");
                }
            }

            public void onFinish() {
                getInstance().onFinish();
            }
        }.start();

    }

    /**
     * @param name 0开头是单人，1开头是群组
     * @param type 0代表视频对讲，1代表路况查询
     */
    public void showNotification(String name, int type) {
        ConnUtil.screenOn(MyApplication.getContext());
        o2.cancel();
        showSound();
        if (type == 1) {
            MyApplication.getContext().sendBroadcast(new Intent(ACTION_REQUEST_TIMEOUT).putExtra("phone", name.substring(1)));
        }
        if (notifyAdapter == null) {
            notifyAdapter = new NotifyAdapter();
            gridView.setAdapter(notifyAdapter);
        }
        if (notifyAdapter != null && notifyAdapter.getNames().size() > 2) {
            notifyAdapter.getNames().remove(0);
            notifyAdapter.getTypes().remove(0);
            notifyAdapter.addName(name, type);
            startCountDown();
        } else if (notifyAdapter != null && notifyAdapter.getNames().size() > 0) {
            notifyAdapter.addName(name, type);
            startCountDown();
        } else if (notifyAdapter != null) {
            notifyAdapter.addName(name, type);
            // 设置窗口类型，一共有三种Application windows, Sub-windows, System windows
            final WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
            // API中以TYPE_开头的常量有23个
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            mWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            // 设置期望的bitmap格式
            mWindowParams.format = PixelFormat.TRANSLUCENT; //设置背景透明

            // 以下属性在Layout Params中常见重力、坐标，宽高
            mWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

            mWindowParams.x = 0;
            mWindowParams.y = 0;

            mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            ;//WindowManager.LayoutParams. WRAP_CONTENT;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    // 添加指定视图
                    try {
                        mWindowManager.addView(view, mWindowParams);
                        Log.e(TAG,"addView");
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    view.setVisibility(View.VISIBLE);
                    o.setDuration(500).start();
                    gridView.setFocusable(true);
                    gridView.setFocusableInTouchMode(true);
                    gridView.requestFocusFromTouch();
                    gridView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ButtonUtils.sendLeft();
                        }
                    },200);
                }
            });
        } else {
            Log.e(TAG,"showNotification 异常");
        }
    }

    public void showSound() {
        SoundPoolTool.getInstance(MyApplication.getContext()).play_voice();
    }
}
