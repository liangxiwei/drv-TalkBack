package com.video;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;

import com.example.jrd48.chat.crash.MyApplication;

/**
 * Created by Administrator on 2018/1/4.
 */


public class LightSensorListener implements SensorEventListener {
    private static final String TAG = "LightSensorListener";
    private float lux; // 光线强度
    private long lastToastTime; // 光线强度

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        Context context = MyApplication.getContext();
        if (event.sensor.getType() == Sensor.TYPE_LIGHT && SystemClock.elapsedRealtime() - lastToastTime > 10 * 1000) {
            // 获取光线强度
            lux = event.values[0];
            if (lux < 10) {
                Log.e(TAG,"lux:" + lux);
                lastToastTime = SystemClock.elapsedRealtime();
                //ToastR.setToastLong(context, "当前光线不好，最好切换为语音对讲模式");
            }
        }
    }
}
