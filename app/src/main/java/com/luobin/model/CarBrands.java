package com.luobin.model;

import java.io.Serializable;

/**
 * 车品牌测试类
 *
 * @Description :
 * @Author HuangJinWen
 * @CreateTime 2016-6-17 下午5:21:05
 */
public class CarBrands implements Serializable{

    private int carBrandID;

    private String carBrandName;

    private int version;

    public int getCarBrandID() {
        return carBrandID;
    }

    public void setCarBrandID(int carBrandID) {
        this.carBrandID = carBrandID;
    }

    public String getCarBrandName() {
        return carBrandName;
    }

    public void setCarBrandName(String carBrandName) {
        this.carBrandName = carBrandName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


}
