package com.example.jrd48.chat.location;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyLogger;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.ReportLocationProcesser;

public class ServiceCheckUserEvent extends Service {
    private MyLogger mLog = MyLogger.jLog();

    public static String ACTION = "com.luobin.pocdemo.ServiceCheckUserEvent";
    public static String TAG = "pocdemo";
    public static int CHANGE_TIME = 3;
    public static int START_AMAP = 4;
    MyLocationInterface myLocationInterface = null;
    SettingChangedBroadcast settingChangedBroadcast;
    protected PermissionUtil mPermissionUtil;

    private int mInterval = 1;

    //	private RateChangeBroadcast mRateChangeBroadcast=null;
    private MyLocation mLocation = null;
    public TimeoutBroadcastManager mBroadcastManger = new TimeoutBroadcastManager();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        super.onCreate();

        initial();

        Log.i(TAG, "启动定位服务 ...");
        mPermissionUtil = PermissionUtil.getInstance();
        myLocationInterface = new GaodeLocation(this);
        myLocationInterface.setOnGotListener(new MyLocationListener() {
            @Override
            void onGotLocation(MyLocation location) {
                mLocation = location;
                toSendLocation();
            }
        });

        startAmapLocation();

    }

    private void startAmapLocation() {

        SettingRW config = new SettingRW(this);
        config.load();
        if (config.isEnableLocation() == false) {
            mLog.i("禁用定位功能");
            return;
        }

        reAlarm(true);
        mLog.i("开始定位");

        boolean bHasPermission = true;
        if (mPermissionUtil.isOverMarshmallow()) {
            if (PackageManager.PERMISSION_GRANTED !=
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                bHasPermission = false;
            }
        }

        if (bHasPermission == false) {
            Log.w(TAG, "没有定位权限，无法定位");
            return;
        }

        if (myLocationInterface != null) {
            myLocationInterface.stop();
            myLocationInterface.start(mInterval);
        }
    }

    private int getlocalInterval() {
        SettingRW mSettings = new SettingRW(ServiceCheckUserEvent.this);
        mSettings.load();
        int x = mSettings.getIntervalTime();
        x = x <= 0 ? 1 : x;
        mLog.i("获取定位时间间隔：" + x + "(分钟)");
        return x;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        parseIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void parseIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        if (intent.hasExtra("auto_report")) {
            reAlarm(true);
            myLocationInterface.stop();
            myLocationInterface.start(mInterval);
        }
    }

    /**
     * 定时发送事件请求，比如位置更新等事件
     */
    void reAlarm(boolean bContinue) {
        Log.i(TAG, "realarm(auto report interval): " + mInterval + "(minute)");

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ServiceCheckUserEvent.class);
        i.putExtra("auto_report", true);
        PendingIntent pi = PendingIntent.getService(this, 1, i, 0);
        am.cancel(pi);

        if (bContinue) {
            mLog.i("启动定位Alarm一次");
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + mInterval * 60 * 1000, pi);
        } else {
            mLog.i("未启动定位Alarm");
        }

    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "停止定位服务 ...");

        reAlarm(false);

        // 停止定位
        if (myLocationInterface != null) {
            myLocationInterface.stop();
        }

        if (settingChangedBroadcast != null) {
            unregisterReceiver(settingChangedBroadcast);
        }

        mBroadcastManger.stopAll();

        super.onDestroy();
    }

    void initial() {
        try {
            mInterval = getlocalInterval();
            mLog.i("定位间隔：" + mInterval + "（分钟）");
            IntentFilter filter = new IntentFilter();
            settingChangedBroadcast = new SettingChangedBroadcast();
            filter.addAction(ACTION);
            registerReceiver(settingChangedBroadcast, filter);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CART", "initial thread failed.");
        }
    }

    private void toSendLocation() {
        try {
            mLog.i("check get location：lat：" + mLocation.getLat() + ", lng：" + mLocation.getLng());
            ProtoMessage.LocationMsg.Builder builder = ProtoMessage.LocationMsg.newBuilder();
            builder.setLat(mLocation.getLat());
            builder.setLng(mLocation.getLng());
            builder.setRadius(mLocation.getRadius());
            builder.setIsAccurate(mLocation.getIsAccurate());
            builder.setTime(mLocation.getTime());
            MyService.start(this, ProtoMessage.Cmd.cmdReportLocation.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(ReportLocationProcesser.ACTION);

            new TimeoutBroadcast(ServiceCheckUserEvent.this, filter, mBroadcastManger)
                    .startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

                        @Override
                        public void onTimeout() {
                            Log.d(TAG, "汇报位置超时");
                        }

                        @Override
                        public void onGot(Intent i) {
                            if (i.getIntExtra("error_code", -1) ==
                                    ProtoMessage.ErrorCode.OK.getNumber()) {
                                Log.d(TAG, "汇报位置成功");
                            } else {
                                Log.d(TAG, "汇报位置出错 " + i.getIntExtra("error_code", -1));
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "send error: " + e.getMessage());
            e.printStackTrace();
        }

    }


    class SettingChangedBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int paramType = intent.getIntExtra("type", 0);
            if (paramType == START_AMAP) {
                // 具有定位权限以后，
                startAmapLocation();
            } else if (paramType == CHANGE_TIME) {
                mInterval = getlocalInterval();
                startAmapLocation();
            }
        }
    }

    public static void restart(Context context) {
        MyLogger log = MyLogger.jLog();
        SettingRW mSettings = new SettingRW(context);
        mSettings.load();
        if (mSettings.isEnableLocation()) {
            SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
            String token = preferences.getString("token", "");
            if (!token.equals("")) {
                log.i("启用定位服务");
                Intent i2 = new Intent(context, ServiceCheckUserEvent.class);
                context.startService(i2);
            } else {
                Log.i(TAG, "用户未登录,不能启用定位服务");
            }
        } else {
            log.w("未启用定位服务");
        }
    }

    public static void stop(Context context) {
        Intent i = new Intent(context, ServiceCheckUserEvent.class);
        context.stopService(i);
    }
}
