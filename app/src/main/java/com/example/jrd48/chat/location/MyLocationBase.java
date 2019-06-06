package com.example.jrd48.chat.location;

import android.content.Context;
import android.util.Log;
//import com.google.gson.Gson;

public abstract class MyLocationBase implements MyLocationInterface {
    private MyLocationListener mFunc;
    protected Context context;

    public MyLocationBase(Context context) {
        this.context = context;
    }

    protected void onGotLocation(MyLocation location, int coordType) {

        try {
            // 发送广播给 threadCheckUserEvent
            Log.i(ServiceCheckUserEvent.TAG, "获取到定位信息: " + "纬度：" + location.getLat() + ", 经度：" + location.getLng());
//
//            //用序列化发送广播
//            Intent intent = new Intent(ServiceCheckUserEvent.ACTION);
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("location", location);
//            intent.putExtra("type", ServiceCheckUserEvent.LOCATION);
//            intent.putExtras(bundle);
//            context.sendBroadcast(intent);

            mFunc.onGotLocation(location);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(ServiceCheckUserEvent.TAG, "send location broadcast failed!");
        }
    }

    @Override
    public void setOnGotListener(MyLocationListener func) {
        mFunc = func;
    }
}
