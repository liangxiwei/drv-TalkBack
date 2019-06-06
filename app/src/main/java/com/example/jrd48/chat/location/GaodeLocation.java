package com.example.jrd48.chat.location;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.jrd48.service.protocol.Data;

import java.util.Date;


public class GaodeLocation extends MyLocationBase {

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    public GaodeLocation(Context context) {
        super(context);
    }

    @Override
    public void start(int intervar) {
        try {

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
            wakeLock.acquire(5000);
            locationClient = new AMapLocationClient(context.getApplicationContext());
            locationOption = new AMapLocationClientOption();

            Log.i(ServiceCheckUserEvent.TAG, "正在启动高德(gaode)定位服务..." + " 定位间隔时间：" + intervar + "分钟");

            //设置定位模式 Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOption.setGpsFirst(true);
            locationOption.setNeedAddress(false);
            locationOption.setOnceLocation(true);
            //locationOption.setOnceLocationLatest(true);
            //locationOption.setInterval(intervar * 60 * 1000);

            //locationOption.setInterval(10 * 1000);
            //设置定位参数
            locationClient.setLocationOption(locationOption);
            // 设置定位监听
            locationClient.setLocationListener(new AMapLocationListener() {

                @Override
                public void onLocationChanged(AMapLocation arg0) {
                    MyLocation myLocation = new MyLocation();
                    myLocation.setLat(arg0.getLatitude());
                    myLocation.setLng(arg0.getLongitude());
                    myLocation.setDirection(arg0.getBearing());
                    myLocation.setSpeed(arg0.getSpeed());
                    myLocation.setTime((new Date().getTime())/1000);
                    myLocation.setIsAccurate(arg0.getLocationType() == AMapLocation.LOCATION_TYPE_GPS ? 1 : 0);
                    myLocation.setRadius((int) arg0.getAccuracy());
//				    Log.e(ServiceCheckUserEvent.TAG, "获取定位数据 " +"纬度："+ arg0.getLatitude()+"经度："+arg0.getLongitude());
//                    ToastR.setToast(context, "获取定位数据 " + "纬度：" + arg0.getLatitude() + ",经度：" + arg0.getLongitude());
                    onGotLocation(myLocation, MyCoordType.GAODE);
                }


            });

            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            if (null != locationClient) {
                Log.i(ServiceCheckUserEvent.TAG, "高德 （gaode） location restart");
                locationClient.stopLocation();
                /**
                 * 如果AMapLocationClient是在当前Activity实例化的，
                 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
                 */
                locationClient.onDestroy();
                locationClient = null;
                locationOption = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
