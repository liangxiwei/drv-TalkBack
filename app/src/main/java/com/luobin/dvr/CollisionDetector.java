package com.luobin.dvr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CollisionDetector implements SensorEventListener {
    private static final String TAG = "CollisionDetector";

    private static final double GRAVITY_EARTH = 9.80665;
    private static final double MULTIPLE_GRAVITY = (4 * GRAVITY_EARTH);
    // private static final double CLLIDE_THREHOLD = (MULTIPLE_GRAVITY*MULTIPLE_GRAVITY);
    private static final double CLLIDE_THREHOLD = 1000;
//    private static final double CLLIDE_THREHOLD = 100;
    private SensorManager mSensorManager = null;
    private CollisionListener mListener = null;
    public interface CollisionListener {
        void onCollide();
    }

    public CollisionDetector(Context context, CollisionListener listener) {
        mListener = listener;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void release() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged accuracy=" + accuracy);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            double combinedacc = x * x + y * y + z * z;
            if (combinedacc > CLLIDE_THREHOLD / 2) {
                Log.d(TAG, "onSensorChanged x=" + x + ", y=" + y + ", z=" + z + ", com=" + combinedacc);
            }
            if (combinedacc > CLLIDE_THREHOLD && mListener != null) {
                mListener.onCollide();
            }
        }
    }

    ;

}
