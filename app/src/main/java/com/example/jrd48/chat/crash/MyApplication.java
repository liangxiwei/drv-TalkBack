package com.example.jrd48.chat.crash;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Choreographer;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ActivityCollector;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.service.MyService;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.R;
import com.luobin.utils.ShellUtils;
import com.luobin.utils.VideoRoadUtils;
import com.qihoo.linker.logcollector.LogCollector;
import com.qihoo.linker.logcollector.upload.HttpParameters;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
public class MyApplication extends MultiDexApplication {
    CrashHandler mHander = null;
    private ShutDownObserver shutDownObserver;
    private static String videoPhone = null;
    private static long videoTeam = 0;
    private static Context context;
    private static Choreographer choreographer;
    private RefWatcher mRefWatcher;
    private static final int MSG_VIDEO_RADIO_SWITCH_START_VIDEO = 2;
    private static final int MSG_VIDEO_RADIO_SWITCH_START_RADIO = 3;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_VIDEO_RADIO_SWITCH_START_VIDEO:
                    startVideoChat();
                    break;
                case MSG_VIDEO_RADIO_SWITCH_START_RADIO:
                    startRadioChat();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private BroadcastReceiver shutDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyApplication", "shutDownReceiver  action="+intent.getAction());
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                if (DvrConfig.getAccOffStateWorkingEnabled()) {
                    int type = GlobalStatus.getShutDownType(getContext());
                    if (type == 0 && !(boolean) SharedPreferencesUtils.get(getContext(), "isScreenOn", false)) {
                        SharedPreferencesUtils.put(getContext(), "isScreenOn", true);
                        checkStatus(true);
                    }
                }
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                if (DvrConfig.getAccOffStateWorkingEnabled()) {
                    int type = GlobalStatus.getShutDownType(getContext());
                    if (type == 0) {
                        SharedPreferencesUtils.put(getContext(), "isScreenOn", false);
                        ActivityCollector.finishAll();
                    }
                }
            }
        }
    };

    public static Context getContext() {
        return context;
    }

    public static Choreographer getChoreographer() {
        return choreographer;
    }

    public static void setChoreographer(Choreographer choreographer) {
        MyApplication.choreographer = choreographer;
    }
    @Override
    public void onCreate() {
        Log.i("Application", "before create");
        super.onCreate();
        context = this;
        mHander = CrashHandler.getInstance();
        mHander.init(getApplicationContext());
        choreographer = Choreographer.getInstance();
        Log.i("Application", "after create");
        DvrConfig.init(getApplicationContext());
        shutDownObserver = new ShutDownObserver(new Handler());
        shutDownObserver.startObserving();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(shutDownReceiver, intentFilter);
        //upload logfile , post params.
        checkPermission();
        //initLeakCanary();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static String getVideoPhone() {
        return videoPhone;
    }

    public static long getVideoTeam() {
        return videoTeam;
    }

    public static void setCurVideo(String videoPhone, long videoTeam) {
        MyApplication.videoPhone = videoPhone;
        MyApplication.videoTeam = videoTeam;
    }


    public void checkPermission() {
        try {
            HttpParameters params = new HttpParameters();

            SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
            params.add("account", preferences.getString("phone", ""));
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = tm.getDeviceId();
                params.add("deviceid", deviceId);
                Log.i("Application", "deviceId:" + deviceId);
            } catch (Exception e) {
                e.printStackTrace();
                params.add("deviceid", "null");
            }
            params.add("app", getPackageName());
            params.add("androidver", Build.VERSION.SDK_INT);
            params.add("appver", SysUtil.getVersionCode(context));
            params.add("androidbuild", SysUtil.getSysVersion());
            LogCollector.setDebugMode(false);
            LogCollector.init(getApplicationContext(), getString(R.string.upload_log_url), params);
            LogCollector.upload(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ShutDownObserver extends ContentObserver {
        private final Uri NAVI_START_STOP_URI =
                Settings.System.getUriFor(GlobalStatus.NAVI_START_STOP);
        private final Uri CHAT_VIDEO_RADIO_SWITCH_URI =
                Settings.System.getUriFor(GlobalStatus.CHAT_VIDEO_RADIO_SWITCH);

        public ShutDownObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (NAVI_START_STOP_URI.equals(uri)) {
                int type = GlobalStatus.getShutDownType(getContext());
                Log.d("Application", "ShutDownObserver type:" + type);
                if (0 == type) {
                    ActivityCollector.finishAll();
                } else {
                    Log.d("Application", "onChange checkStatus ");
                    checkStatus(false);
                }
            } else if (CHAT_VIDEO_RADIO_SWITCH_URI.equals(uri)) {
                int currentMode = GlobalStatus.getChatVideoMode(getContext());
                Log.d("Application", "ChatVideoMode switch=" + currentMode);
                if (currentMode == 1) {
                    mHandler.removeMessages(MSG_VIDEO_RADIO_SWITCH_START_RADIO);
                    mHandler.sendEmptyMessageDelayed(MSG_VIDEO_RADIO_SWITCH_START_RADIO, 800);
                } else {
                    stopRadioChat();
                    mHandler.removeMessages(MSG_VIDEO_RADIO_SWITCH_START_VIDEO);
                    mHandler.sendEmptyMessageDelayed(MSG_VIDEO_RADIO_SWITCH_START_VIDEO, 800);
                }
            }
        }

        public void startObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    NAVI_START_STOP_URI,
                    false, this);
            cr.registerContentObserver(
                    CHAT_VIDEO_RADIO_SWITCH_URI,
                    false, this);
        }

        public void stopObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }

    public synchronized void checkStatus(final boolean screenOn){
        String curProcessName = getCurProcessName(context);
        if(!TextUtils.isEmpty(curProcessName) && curProcessName.contains("DvrServiceProc")){
            return;
        }
        int type = GlobalStatus.getShutDownType(getContext());
//        if(screenOn){
//            ToastR.setToast(MyApplication.getContext(),(boolean) SharedPreferencesUtils.get(getContext(),"isScreenOn",false)+"screenOn");
//        } else {
//            ToastR.setToast(MyApplication.getContext(),(boolean) SharedPreferencesUtils.get(getContext(),"isScreenOn",false)+"naviOn");
//        }
        if (type == 1) {
            if (DvrConfig.getAccOffStateWorkingEnabled()) {
                if ((boolean) SharedPreferencesUtils.get(getContext(), "isScreenOn", false)) {
                    SharedPreferencesUtils.put(getContext(), "isScreenOn", false);
                    return;
                } else {
                    SharedPreferencesUtils.put(getContext(), "isScreenOn", false);
                }
            }
        }
        Log.d("wsDvr", "checkStatus type="+type +",screenOn="+screenOn);
        if (1 == type || screenOn) {
            //TODO 打火
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (DvrConfig.getAccOffStateWorkingEnabled()) {
                        if (0 == GlobalStatus.getShutDownType(MyApplication.getContext()) && !((boolean) SharedPreferencesUtils.get(MyApplication.getContext(), "isScreenOn", false))) {
                            return;
                        }
                    } else {
                        if (0 == GlobalStatus.getShutDownType(MyApplication.getContext())) {
                            return;
                        }
                    }

//                    ToastR.setToast(MyApplication.getContext(),screenOn+"");
                    Log.d("wsDvr", "checkStatus startUSBCamera");
                    VideoRoadUtils.startUSBCamera();
                }
            }).start();
            //TODO 打火
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MyService.restart(getContext());
        }
    }

    String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "none";
    }

    private void startVideoChat() {
        Intent intentVideo = new Intent();
        intentVideo.setClassName("com.luobin.dvr",
                "com.example.jrd48.chat.WelcomeActivity");
        intentVideo.putExtra("className", "bbs");
        context.startActivity(intentVideo);
    }

    private void startRadioChat() {
        //goHome(context);
        Intent intentRadio = new Intent();
        intentRadio.setClassName("com.benshikj.ht.jf",
                "com.dw.ht.JFMainActivity");
        context.startActivity(intentRadio);
    }

    private void stopRadioChat() {
        Intent intent = new Intent("com.benshikj.ht.jf.action.RX_STOP");
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public static void goHome(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory("android.intent.category.HOME");
        context.startActivity(intent);
    }

    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            mRefWatcher = RefWatcher.DISABLED;
            return;
        }
        mRefWatcher = LeakCanary.install(this);
    }
}
