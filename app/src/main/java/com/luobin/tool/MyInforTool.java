package com.luobin.tool;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jrd48 on 2017/3/3.
 */

public class MyInforTool {

    Context mContext = null;
    String myPhone = "";
    private String userName;
    private String nickName;
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
    private boolean switchNickName;
    private boolean switchSex;
    private boolean switchPhoneNumber;
    private boolean switchCarNumber;
    private boolean switchCarBrand;
    private boolean switchCarType;
    private boolean switchLocationUpload;
    private boolean startSeaChatGroup;
    private boolean startSeaChatGroupVideo;

    private String interest;// 兴趣
    private String career;//行业
    private String signature;//签名

    public boolean isStartSeaChatGroup() {
        return startSeaChatGroup;
    }

    public void setStartSeaChatGroup(boolean startSeaChatGroup) {
        this.startSeaChatGroup = startSeaChatGroup;
    }

    public boolean isStartSeaChatGroupVideo() {
        return startSeaChatGroupVideo;
    }

    public void setStartSeaChatGroupVideo(boolean startSeaChatGroupVideo) {
        this.startSeaChatGroupVideo = startSeaChatGroupVideo;
    }

    public boolean isSwitchLocationUpload() {
        return switchLocationUpload;
    }

    public void setSwitchLocationUpload(boolean switchLocationUpload) {
        this.switchLocationUpload = switchLocationUpload;
    }

    public boolean isSwitchNickName() {
        return switchNickName;
    }

    public void setSwitchNickName(boolean switchNickName) {
        this.switchNickName = switchNickName;
    }

    public boolean isSwitchSex() {
        return switchSex;
    }

    public void setSwitchSex(boolean switchSex) {
        this.switchSex = switchSex;
    }

    public boolean isSwitchPhoneNumber() {
        return switchPhoneNumber;
    }

    public void setSwitchPhoneNumber(boolean switchPhoneNumber) {
        this.switchPhoneNumber = switchPhoneNumber;
    }

    public boolean isSwitchCarNumber() {
        return switchCarNumber;
    }

    public void setSwitchCarNumber(boolean switchCarNumber) {
        this.switchCarNumber = switchCarNumber;
    }

    public boolean isSwitchCarBrand() {
        return switchCarBrand;
    }

    public void setSwitchCarBrand(boolean switchCarBrand) {
        this.switchCarBrand = switchCarBrand;
    }

    public boolean isSwitchCarType() {
        return switchCarType;
    }

    public void setSwitchCarType(boolean switchCarType) {
        this.switchCarType = switchCarType;
    }

    public String getPhone() {
        return phone;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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

    public String getNickName() {
        return nickName;
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
//        initData();
        if (bLoad) {
            load();
        }
    }

    private void initData() {
        SharedPreferences preferences1 = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences1.getString("phone", "");
    }

    public void load() {
        SharedPreferences preferences = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);

        userName = preferences.getString("name", "");
        nickName = preferences.getString("nick_name", "");
        userSex = preferences.getInt("user_sex", 0);
//        carID = preferences.getString("car_id", "");
        phone = preferences.getString("phone", "");
        carID = CarType.getCarId(mContext,phone);
        city = preferences.getString("city", "");
        prov = preferences.getString("prov", "");
        town = preferences.getString("town", "");
        birthday = preferences.getString("birthday", "");
        carNum = preferences.getString("car_num", "");
        carBand = preferences.getString("car_band", "");
        carType2 = preferences.getString("car_type2", "");
        carType3 = preferences.getString("car_type3", "");
        applyInfo = preferences.getString("apply_info", "");
        switchNickName = preferences.getBoolean("switchNick",false);
        switchSex = preferences.getBoolean("switchSex",false);
        switchPhoneNumber = preferences.getBoolean("switchPhone",false);
        switchCarNumber = preferences.getBoolean("switchCarNumber",false);
        switchCarBrand = preferences.getBoolean("switchCarBrand",false);
        switchCarType = preferences.getBoolean("switchCarType",false);
        switchLocationUpload = preferences.getBoolean("switchGPS",false);
        startSeaChatGroup = preferences.getBoolean("seaChatGroup",false);
        startSeaChatGroupVideo = preferences.getBoolean("seaChatGroupVideo",false);
        interest = preferences.getString("interest","");
        career = preferences.getString("career","");
        signature = preferences.getString("signature","");
        return;
    }

    @Override
    public String toString() {
        return "MyInforTool{" +
                "mContext=" + mContext +
                ", myPhone='" + myPhone + '\'' +
                ", userName='" + userName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", userSex=" + userSex +
                ", carID='" + carID + '\'' +
                ", city='" + city + '\'' +
                ", prov='" + prov + '\'' +
                ", town='" + town + '\'' +
                ", birthday='" + birthday + '\'' +
                ", carNum='" + carNum + '\'' +
                ", carBand='" + carBand + '\'' +
                ", carType2='" + carType2 + '\'' +
                ", carType3='" + carType3 + '\'' +
                ", applyInfo='" + applyInfo + '\'' +
                ", phone='" + phone + '\'' +
                ", switchNickName=" + switchNickName +
                ", switchSex=" + switchSex +
                ", switchPhoneNumber=" + switchPhoneNumber +
                ", switchCarNumber=" + switchCarNumber +
                ", switchCarBrand=" + switchCarBrand +
                ", switchCarType=" + switchCarType +
                ", switchLocationUpload=" + switchLocationUpload +
                ", startSeaChatGroup=" + startSeaChatGroup +
                ", startSeaChatGroupVideo=" + startSeaChatGroupVideo +
                '}';
    }
}
