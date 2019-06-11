package com.luobin.voice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.location.Utils;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyLogger;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.SpeakerBeginProcesser;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.DvrService;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by qhb on 17-1-5.
 */

public class VoiceHandler {


    private final MyLogger mLog = MyLogger.jLog();
    private final Handler mHandler;
    MyAACRecorder mVoiceRecorder;
    Timer mTimerCheckVoiceStop;
    Thread mVoiceRecorderThread = null;
    private Context context;
    private NetPlayer mVoicePlayer;
    private Thread mPlayerThread;
    private boolean isPreviewing;
    public VoiceHandler(MyService context, Handler handler) {
        this.context = context;
        mVoicePlayer = new NetPlayer(context);
        mVoiceRecorder = new MyAACRecorder();
        mVoiceRecorder.setContext(context);
        this.mHandler = handler;



        synchronized (mPlayerLocker) {
            //启动播放语音线程
            if (mPlayerThread == null) {
                mPlayerThread = new Thread(mVoicePlayer);
                mVoicePlayer.setRunning(false);
                mPlayerThread.start();
            }

        }

        synchronized (thread_locker) {
            // 2. 启动录音线程
            try {
                if (mVoiceRecorderThread != null && !mVoiceRecorder.isRunnableThread()) {
                    Log.w("onVoiceRecordStart", "debug for start too fast");
                } else {
                    // 设置标志
                    mVoiceRecorder.setStopFlag(true);
                    mVoiceRecorderThread = new Thread(mVoiceRecorder);
                    mVoiceRecorderThread.setPriority(Thread.MAX_PRIORITY);
                    mVoiceRecorderThread.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * start/restart speak
     *
     * @param context
     * @param bStart
     */
    public static void doVoiceAction(Context context, boolean bStart) {
        //InputTools.HideKeyboard(mEditText);
        Log.v("wsDvr","doVoiceAction:" + bStart);
        if (bStart) {

//            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
//            vibrator.vibrate(50);
            Intent i = new Intent(context, MyService.class);
            i.putExtra("start_voice", PttKeyReceiver.KEY_DOWN);
            i.putExtra("no_popup", true);
            context.startService(i);

//            startRTMP(context);
            //mBtnVoice.setText(R.string.action_stop_voice);
        } else {
//            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
//            vibrator.vibrate(50);
            DvrService.start(context, RESClient.ACTION_STOP_RTMP, null);
            Intent i = new Intent(context, MyService.class);
            i.putExtra("start_voice", PttKeyReceiver.KEY_UP);
            i.putExtra("no_popup", true);
            context.startService(i);
            //mBtnVoice.setText(R.string.action_start_voice);
        }
    }

    public static void startRTMP(Context context,String selfAddr) {
        if(GlobalStatus.isVideo()) {
            SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
            String myPhone = preferences.getString("phone", "");
            String temp = null;
            if(GlobalStatus.getChatTeamId() != 0) {
                temp = RESClient.SERVER_URL + "group---" + GlobalStatus.getRoomID();
                Log.i("wsDvr", "ACTION_START_RTMP group");
            } else {
                temp = RESClient.SERVER_URL + GlobalStatus.getRoomID();
                Log.i("wsDvr", "ACTION_START_RTMP single");
            }
            if(!TextUtils.isEmpty(selfAddr)) {
                DvrService.start(context, RESClient.ACTION_START_RTMP, temp + "_" + selfAddr);
//                DvrService.start(context, RESClient.ACTION_START_RTMP,isFirst, temp);
            } else {
                DvrService.start(context, RESClient.ACTION_START_RTMP,temp);
            }
        }

    }

    public static void speakBeginAndRecording(final Context context) {
        UUID uuid = UUID.randomUUID();
            final String radom = uuid.toString();
            ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
            if (chatRoomMsg != null) {
                if (!Utils.getTopActivity(context)) {
                    context.sendBroadcast(new Intent(RESClient.ACTION_ONCLICK_LEFT_TOP));
                }
        }
        doVoiceAction(context, true);
        GlobalStatus.setCurRtmpAddr(radom);
        try {
            ProtoMessage.MsgSpeakBegin.Builder builder = ProtoMessage.MsgSpeakBegin.newBuilder();
            builder.setVideoUrl(radom);
            MyService.start(context, ProtoMessage.Cmd.cmdSpeakBegin.getNumber(), builder.build());

            IntentFilter filter = new IntentFilter();
            filter.addAction(SpeakerBeginProcesser.ACTION);
            new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                }

                @Override
                public void onGot(Intent i) {
                    if (i.getIntExtra("error_code", -1) ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {

                    } else {
                        Log.i("poc demo", "呼叫失败错误码" + i.getIntExtra("error_code", -1));
//                        ToastR.setToast(context, "呼叫错误");
                        new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                        doVoiceAction(context, false);
                        DvrService.start(context, RESClient.ACTION_STOP_RTMP, null);
//                        GlobalStatus.setCurRtmpAddr(null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void speakBeginFailed(int error_code) {
        Log.w("pocdemo", "启动说话失败：" + error_code);
    }


    public static void speakEndAndRecroding(Context context) {

        try {
            if (GlobalStatus.getRoomID() <= 0) {
                Log.w("pocdemo", "停止讲话：不在任何房间");
                return;
            }
            doVoiceAction(context, false);
//            GlobalStatus.setCurRtmpAddr(null);
            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
            MyService.start(context, ProtoMessage.Cmd.cmdSpeakEnd.getNumber(), builder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放语音
     */
    public void speak(byte[] packData) {
        synchronized (mPlayerLocker) {
            //MyLogger.jLog().i("get speak data size: " + packData.length);

            if (mPlayerThread == null || !mVoicePlayer.isRunnableThread()) {
                mPlayerThread = new Thread(mVoicePlayer);
                mVoicePlayer.setRunning(false);
                mPlayerThread.start();
            }

            if (!mVoicePlayer.isRunning()) {
                mVoicePlayer.setRunning(true);
            }

        }

        //mLog.i("got voice data: " +HexTools.byteArrayToHex(packData));
        //int n = (packData[2] | (packData[3] << 8)) - 1;
        // mLog.i("mychat voice size: " + n);
        mVoicePlayer.pushRawData(packData, 0, packData.length);
//        for (int i = 0; (i + 38) <= packData.length; i += 38) {
//            mVoicePlayer.pushRawData(packData, i, 38);
//         }
        //mVoicePlayer.pushRawData(packData, 0, packData.length);
        //mLastVoiceTime.set(new Date());

        // 先停止原来的时间检查
        if (mTimerCheckVoiceStop != null) {
            try {
                mTimerCheckVoiceStop.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTimerCheckVoiceStop = null;
        }

        mTimerCheckVoiceStop = new Timer();
        mTimerCheckVoiceStop.schedule(new TimerTask() {
            @Override
            public void run() {
                stopVoicePlayerThread();
            }
        }, 2000);
    }

    private Object mPlayerLocker = new Object();
    private void stopVoicePlayerThread() {

        synchronized (mPlayerLocker) {
            mLog.w("stop player thread [begin] >>>>>>>>>>>>>>>>>>>>> ");
            mVoicePlayer.stop();
            mLog.w("stop player thread [end] <<<<<<<<<<<<<<<<<<<<<<< ");
        }
    }

    PowerManager.WakeLock wakeLock = null;

    /**
     * @param intent
     * @return true when intent done, false when not my message
     */
    public boolean doWithIntent(Intent intent) {
        if (intent.hasExtra("ptt_key_action")) {
            boolean bKeyDown = intent.getBooleanExtra("ptt_key_action", false);
            if(bKeyDown && GlobalStatus.isPttKeyDown()){
                mLog.i("cur is ptt down");
                return true;
            }
            if (bKeyDown) {
                // 获取电源管理器对象
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pocdemo");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire(60 * 1000); // 最多1分钟

                // 按下 PTT键
                GlobalStatus.setPttKeyDown(true);
                VoiceHandler.speakBeginAndRecording(context);
            } else {
                try {
                    // 弹起 PTT键
                    GlobalStatus.setPttKeyDown(false);
                    VoiceHandler.speakEndAndRecroding(context);
                } finally {
                    if (wakeLock != null) {
                        wakeLock.release();
                    }
                }

            }
            return true;

        } else if (intent.hasExtra("stop_player")) {
            stopVoicePlayerThread();
            return true;

        } else if (intent.hasExtra("start_voice")) {

            //startActivity(new Intent(this, MainActivity.class));
            int x = intent.getIntExtra("start_voice", -1);
            mLog.i("got start_voice start mode: " + x);

            if (x == PttKeyReceiver.KEY_DOWN) {
                stopVoicePlayerThread();
                if (!intent.hasExtra("no_popup")) {
//                    lock = ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE)).newKeyguardLock(KEYGUARD_SERVICE);
//                    lock.disableKeyguard();

//                    PowerManager powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
//                    wake = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
//
//                    wake.setReferenceCounted(false);
//                    wake.acquire();
//
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            wake.release();
//                        }
//                    }, 1000);
//

//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent i = new Intent(context, ShowVoiceActivity.class);
//                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(i);
//
//                        }
//                    }, 0);

                }

                doVoiceClicked(true);
                return true;
            } else {
                doVoiceClicked(false);
                if (!intent.hasExtra("no_popup")) {
                    new PttKeyBroadcast(context).sendBroadcast("2");
                }
                return true;
            }
        } else if(intent.hasExtra("isPreviewing")){
            isPreviewing = intent.getBooleanExtra("isPreviewing",false);
        }
        return false;
    }
    private Object thread_locker = new Object();
    /**
     * 发送语音消息
     */
    public synchronized void doVoiceClicked(boolean bStart) {
        Log.v("wsDvr","doVoiceClicked isPreviewing:" + isPreviewing);
        if (bStart) {
            Intent i = new Intent(context, MyService.class);
            i.putExtra("stop_player", true);
            context.startService(i);

            try {

                synchronized (thread_locker) {
                        // 2. 启动录音线程
                        try {

                            if (mVoiceRecorderThread != null && mVoiceRecorder.isRunning()) {
                                Log.w("onVoiceRecordStart", "debug for start too fast");
                            } else {
                                // 设置启动标志
                                mVoiceRecorder.setStopFlag(false);
//                                mVoiceRecorderThread = new Thread(mVoiceRecorder);
//                                mVoiceRecorderThread.start();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                mVoiceRecorder.startRecording();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                if(mVoiceRecorder != null) {
                    mVoiceRecorder.setStopFlag(true);

                    mVoiceRecorder.stop();
                    mVoiceRecorder.release();
                }
//                if (mVoiceRecorderThread != null) {
//                    try {
//                        mLog.i("--------------------------------- terminate");
//                        mVoiceRecorderThread.interrupt();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        mLog.i("---------------------------- join");
//                        mVoiceRecorderThread.join(1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
                DvrService.start(context,RESClient.ACTION_VOICE_STOP,null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public void destroy() {
        mVoiceRecorder.setRunnableThread(false);
        if (mVoiceRecorderThread != null) {
            try {
                mLog.i("---------------------------- join");
                mVoiceRecorderThread.join(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mLog.i("--------------------------------- terminate");
                mVoiceRecorderThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mVoiceRecorder.stop();
        mVoiceRecorder.release();

        if (mPlayerThread != null) {
            mVoicePlayer.setRunnableThread(false);
            try {
                mPlayerThread.join(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                mPlayerThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayerThread = null;
        }
    }
}
