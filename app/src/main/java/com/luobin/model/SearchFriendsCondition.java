package com.luobin.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/29.
 */

public class SearchFriendsCondition implements Serializable {
    private String mProvince = "";
    private String mCity = "";
    private String mTown = "";
    private String carType1 = "";
    private String carType2 = "";
    private String carBrand = "";
    private String carPlateNumber = "";
    private int mSex = 0;

    public String getCarPlateNumber() {
        return carPlateNumber;
    }

    public void setCarPlateNumber(String carPlateNumber) {
        this.carPlateNumber = carPlateNumber;
    }

    public int getmSex() {
        return mSex;
    }

    public void setmSex(int mSex) {
        this.mSex = mSex;
    }

    public String getmProvince() {
        return mProvince;
    }

    public void setmProvince(String mProvince) {
        this.mProvince = mProvince;
    }

    public String getmCity() {
        return mCity;
    }

    public void setmCity(String mCity) {
        this.mCity = mCity;
    }

    public String getmTown() {
        return mTown;
    }

    public void setmTown(String mTown) {
        this.mTown = mTown;
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

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }
}
