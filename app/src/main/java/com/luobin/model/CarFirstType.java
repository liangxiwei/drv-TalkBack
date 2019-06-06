package com.luobin.model;

/**
 * 车的首型号
 *
 * @Description :
 * @Author HuangJinWen
 * @CreateTime 2016-6-17 下午5:23:42
 */
public class CarFirstType {

    private int carFirstTypeID;

    private String carFirstTypeName;

    private int carBrandID;

    private int version;

    public int getCarFirstTypeID() {
        return carFirstTypeID;
    }

    public void setCarFirstTypeID(int carFirstTypeID) {
        this.carFirstTypeID = carFirstTypeID;
    }

    public String getCarFirstTypeName() {
        return carFirstTypeName;
    }

    public void setCarFirstTypeName(String carFirstTypeName) {
        this.carFirstTypeName = carFirstTypeName;
    }

    public int getCarBrandID() {
        return carBrandID;
    }

    public void setCarBrandID(int carBrandID) {
        this.carBrandID = carBrandID;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


}
