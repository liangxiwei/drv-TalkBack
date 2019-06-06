package com.luobin.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/29.
 */

public class SearchStrangers implements Serializable {
    private String carID;
    private String carNum;
    private String carType1;
    private String carType2;
    private String carType3;
    private String applyInfo;
    private String prov;
    private String city;
    private String town;
    private String phoneNum;
    private String birthday;
    private int userSex;
    private String userName;
    private String nickName;
    private boolean friendStar;

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
    }

    public String getCarType1() {
        return carType1;
    }

    public void setCarType1(String carType1) {
        this.carType1 = carType1;
    }

    public String getCarType2() {
        return carType2;
    }

    public void setCarType2(String carType2) {
        this.carType2 = carType2;
    }

    public String getCarType3() {
        return carType3;
    }

    public void setCarType3(String carType3) {
        this.carType3 = carType3;
    }

    public String getApplyInfo() {
        return applyInfo;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getUserSex() {
        return userSex;
    }

    public void setUserSex(int userSex) {
        this.userSex = userSex;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isFriendStar() {
        return friendStar;
    }

    public void setFriendStar(boolean friendStar) {
        this.friendStar = friendStar;
    }
}
