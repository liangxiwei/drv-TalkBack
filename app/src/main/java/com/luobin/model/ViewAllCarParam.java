package com.luobin.model;


import java.io.Serializable;
import java.sql.Timestamp;


public class ViewAllCarParam implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1016393808842970828L;

    public long getUserCarId() {
        return userCarId;
    }

    public void setUserCarId(long userCarId) {
        this.userCarId = userCarId;
    }

    public int getCarTypeId() {
        return carTypeId;
    }

    public void setCarTypeId(int carTypeId) {
        this.carTypeId = carTypeId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCustomNum() {
        return customNum;
    }

    public void setCustomNum(String customNum) {
        this.customNum = customNum;
    }

    public String getCarProv() {
        return carProv;
    }

    public void setCarProv(String carProv) {
        this.carProv = carProv;
    }

    public String getCarCity() {
        return carCity;
    }

    public void setCarCity(String carCity) {
        this.carCity = carCity;
    }

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
    }

    public String getCarVin() {
        return carVin;
    }

    public void setCarVin(String carVin) {
        this.carVin = carVin;
    }

    public int getCarDisable() {
        return carDisable;
    }

    public void setCarDisable(int carDisable) {
        this.carDisable = carDisable;
    }

    public Timestamp getBindDate() {
        return bindDate;
    }

    public void setBindDate(Timestamp bindDate) {
        this.bindDate = bindDate;
    }

    public String getCarTypeText() {
        return carTypeText;
    }

    public void setCarTypeText(String carTypeText) {
        this.carTypeText = carTypeText;
    }

    public int getCarBrandId() {
        return carBrandId;
    }

    public void setCarBrandId(int carBrandId) {
        this.carBrandId = carBrandId;
    }

    public String getCarBrandText() {
        return carBrandText;
    }

    public void setCarBrandText(String carBrandText) {
        this.carBrandText = carBrandText;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarNumAll() {
        return carNumAll;
    }

    public void setCarNumAll(String carNumAll) {
        this.carNumAll = carNumAll;
    }

    public String getCarTypeAll() {
        return carTypeAll;
    }

    public void setCarTypeAll(String carTypeAll) {
        this.carTypeAll = carTypeAll;
    }

    public int getCarDefault() {
        return carDefault;
    }

    public void setCarDefault(int carDefault) {
        this.carDefault = carDefault;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getFriendRelaID() {
        return friendRelaID;
    }

    public void setFriendRelaID(long friendRelaID) {
        this.friendRelaID = friendRelaID;
    }

    public long getFriendCarID() {
        return friendCarID;
    }

    public void setFriendCarID(long friendCarID) {
        this.friendCarID = friendCarID;
    }

    public long getFriendUserID() {
        return friendUserID;
    }

    public void setFriendUserID(long friendUserID) {
        this.friendUserID = friendUserID;
    }

    public ViewAllCarParam(ViewAllCar v) {
        userCarId = v.getUserCarID();
        carTypeId = v.getCarTypeID();
        carName = v.getCarName();
        customNum = v.getCustomNum();
        userId = v.getUserID();
        carProv = v.getCarProv();
        carCity = v.getCarCity();
        carNum = v.getCarNum();
        carVin = v.getCarVin();
        carDisable = v.getCarDisable();
        bindDate = v.getBindDate();

        carTypeText = v.getCarFirstTypeName() + " " + v.getCarLastTypeName();
        carBrandId = v.getCarBrandID();
        carBrandText = v.getCarBrandName();
        carNumAll = v.getCarNumAll();

        carTypeAll = v.getCarTypeAll();
        carDefault = v.getCarDefault();
        friendUserID = v.getFriendUserID();
        friendCarID = v.getFriendCarID();
        friendRelaID = v.getFriendRelaID();
    }

    public ViewAllCarParam() {
        // TODO Auto-generated constructor stub
    }

    private long friendUserID;

    private long friendCarID;

    private long friendRelaID;

    private long userCarId;//用户车的ID

    private int carTypeId;//用户车的类型

    private long userId;//用户的ID

    private String customNum;//用户商号

    private String carProv;//用户车牌省份

    private String carCity;//用户车牌市级

    private String carNum;//用户车牌号码

    private String carVin;//用户的车架号

    private int carDisable;//解绑标志

    private Timestamp bindDate;//绑定日期

    private Timestamp unbindDate;//解绑日期

    private String carTypeText;//车类型说明

    private int carBrandId;//车品牌号

    private String carBrandText;//车品牌号描述

    private String carName;//车的名字

    private String carNumAll;//车的完整车牌号

    private String carTypeAll;//车的所有类型

    private int carDefault;//默认车辆

    private int mode; // 1, 添加，2：编辑
}
