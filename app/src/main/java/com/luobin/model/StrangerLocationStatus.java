package com.luobin.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class StrangerLocationStatus implements Serializable {

    private double lat;
    private double lng;
    private int radius;  // 半径(米)
    private int isAccurate; // 是否准确：1：准确， 0：模糊
    private long time; // 位置时间
    private String phoneNum; // 好友手机号
    private int latlngType; // 位置类型（0：高德，）
    private String userName;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getIsAccurate() {
        return isAccurate;
    }

    public void setIsAccurate(int isAccurate) {
        this.isAccurate = isAccurate;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public int getLatlngType() {
        return latlngType;
    }

    public void setLatlngType(int latlngType) {
        this.latlngType = latlngType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
