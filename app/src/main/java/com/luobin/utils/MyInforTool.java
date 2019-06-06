package com.luobin.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jrd48 on 2017/3/3.
 */

public class MyInforTool {


    Context mContext = null;
    private String userName;
    private int userSex;
    private String carID;
    private String city;
    private String prov;
    private String town;
    private String birthday;
    private String carNum;
    private String carBand;
    private String carType2;
    private String carType3;
    private String applyInfo;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserSex(int userSex) {
        this.userSex = userSex;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
    }

    public void setCarBand(String carBand) {
        this.carBand = carBand;
    }

    public void setCarType2(String carType2) {
        this.carType2 = carType2;
    }

    public void setCarType3(String carType3) {
        this.carType3 = carType3;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserSex() {
        return userSex;
    }

    public String getCarID() {
        return carID;
    }

    public String getCity() {
        return city;
    }

    public String getProv() {
        return prov;
    }

    public String getTown() {
        return town;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getCarNum() {
        return carNum;
    }

    public String getCarBand() {
        return carBand;
    }

    public String getCarType2() {
        return carType2;
    }

    public String getCarType3() {
        return carType3;
    }

    public String getApplyInfo() {
        return applyInfo;
    }

    public MyInforTool(Context context) {
        mContext = context;
    }

    public MyInforTool(Context context, boolean bLoad) {
        mContext = context;
        if (bLoad) {
            load();
        }
    }

    public void load() {
        SharedPreferences preferences = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);

        userName = preferences.getString("name", "");
        userSex = preferences.getInt("user_sex", 0);
        carID = preferences.getString("car_id", "");
        city = preferences.getString("city", "");
        prov = preferences.getString("prov", "");
        town = preferences.getString("town", "");
        birthday = preferences.getString("birthday", "");
        carNum = preferences.getString("car_num", "");
        carBand = preferences.getString("car_band", "");
        carType2 = preferences.getString("car_type2", "");
        carType3 = preferences.getString("car_type3", "");
        applyInfo = preferences.getString("apply_info", "");
        phone = preferences.getString("phone", "");
        return;
    }

//    public synchronized void save() {
//        SharedPreferences preferences = mContext.getSharedPreferences("my_info", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean("handFree", handFree);
//        editor.putBoolean("max_volume", autoMaxVolume);
//        editor.putBoolean("enableLocation", enableLocation);
//        editor.putBoolean("notify_user", notifyUser);
//        editor.putBoolean("auto_start", autoStart);
//        editor.putBoolean("view_video", viewVideo);
//        editor.putBoolean("transfer_video", transferVideo);
//        editor.putInt("mic_way", micWay);
//        editor.putInt("interval_time", intervalTime);
//        editor.putFloat("map_zoom_level", mapZoomLevel);
//        editor.commit();
//    }
}
