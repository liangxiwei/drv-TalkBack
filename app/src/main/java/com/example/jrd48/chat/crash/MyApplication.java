package com.example.jrd48.chat.crash;

import android.app.ActivityManager;
import android.app.Application;
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
import android.os.Process;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Choreographer;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ActivityCollector;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.MyService;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.log.DBMyLogHelper;
import com.luobin.log.LogCode;
import com.luobin.utils.VideoRoadUtils;
import com.qihoo.linker.logcollector.LogCollector;
import com.qihoo.linker.logcollector.upload.HttpParameters;

import me.lake.librestreaming.client.RESClient;

public class MyApplication extends Application {
    CrashHandler mHander = null;
    private ShutDownObserver shutDownObserver;
    private static String videoPhone = null;
    private static long videoTeam = 0;
    private static Context context;
    private static Choreographer choreographer;
    private BroadcastReceiver shutDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyApplication", "shutDownReceiver  action="+intent.getAction());
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                int type = GlobalStatus.getShutDownType(getContext());
                if (type == 0 && !(boolean) SharedPreferencesUtils.get(getContext(), "isScreenOn", false)) {
                    SharedPreferencesUtils.put(getContext(), "isScreenOn", true);
                    checkStatus(true);
                }
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                int type = GlobalStatus.getShutDownType(getContext());
                if (type == 0) {
                    SharedPreferencesUtils.put(getContext(), "isScreenOn", false);
                    ActivityCollector.finishAll();
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
                Log.v("Application", "ShutDownObserver type:" + type);
                if (0 == type) {
                    ActivityCollector.finishAll();
                } else {
                    Log.d("Application", "onChange checkStatus ");
                    checkStatus(false);
                }
            }
        }

        public void startObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    NAVI_START_STOP_URI,
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
            if((boolean) SharedPreferencesUtils.get(getContext(),"isScreenOn",false)){
                SharedPreferencesUtils.put(getContext(),"isScreenOn",false);
                return;
            } else {
                SharedPreferencesUtils.put(getContext(),"isScreenOn",false);
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
                    if (0 == GlobalStatus.getShutDownType(MyApplication.getContext()) && !((boolean) SharedPreferencesUtils.get(MyApplication.getContext(), "isScreenOn", false))) {
                        return;
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



}
