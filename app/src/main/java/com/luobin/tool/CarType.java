package com.luobin.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Administrator on 2017/11/13.
 */

public class CarType {
    public static final String CAR_ID = "isbundcar";
    public static final String CAR_ICCID = "iccidcar";
    private static final boolean isLuoBinCar = false;
    private static final boolean isBundCar = false;

    public static boolean isLuoBinCar() {
        return isLuoBinCar;
    }

    public static boolean isBundCar(Context context) {
        SharedPreferences preference1 = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String phone = preference1.getString("phone", "");
        SharedPreferences preferences = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        String carid = preferences.getString(CAR_ID, "");
        //TODO 不隐藏附近车辆 和 地图查看
//        return TextUtils.isEmpty(carid) ? false :true;
        return true;
    }

    public static boolean isHasBundCar(Context context) {
        SharedPreferences preference1 = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String phone = preference1.getString("phone", "");
        SharedPreferences preferences = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        String carid = preferences.getString(CAR_ID, "");
        //TODO 不隐藏附近车辆 和 地图查看
        return TextUtils.isEmpty(carid) ? false :true;
    }

    public static synchronized void setCarId(String CAR_ID,Context context,boolean clearIccid){
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String phone = preferences.getString("phone", "");
        SharedPreferences preference = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(CarType.CAR_ID, CAR_ID);
        editor.commit();
        if (clearIccid){
            setCarIccid("",context,phone);
        }
    }

    public static synchronized void setCarId(String CAR_ID,Context context,String phone){
        SharedPreferences preference = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(CarType.CAR_ID, CAR_ID);
        editor.commit();
    }
    public static synchronized String getCarId(Context context,String phone){
        SharedPreferences preference = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        String carID = preference.getString(CarType.CAR_ID, "");
        return carID;
    }

    public static synchronized void setCarIccid(String carIccid,Context context,String phone){
        SharedPreferences preference = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(CarType.CAR_ICCID, carIccid);
        editor.commit();
    }
    public static synchronized String getCarIccid(Context context,String phone){
        SharedPreferences preference = context.getSharedPreferences(phone, Context.MODE_PRIVATE);
        String carIccid = preference.getString(CarType.CAR_ICCID, "");
        return carIccid;
    }

}
