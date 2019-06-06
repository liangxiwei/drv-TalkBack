package com.luobin.ui;


import com.luobin.model.CarFirstType;

import java.io.Serializable;
import java.util.ArrayList;

public class CarInfoBean_New implements Serializable {

    private int carBrandID;

    private String carBrandName;

    private int version;

    ArrayList<CarFirstType> carFirstTypes = new ArrayList<>();

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

    public ArrayList<CarFirstType> getCarFirstTypes() {
        return carFirstTypes;
    }

    public void setCarFirstTypes(ArrayList<CarFirstType> carFirstTypes) {
        this.carFirstTypes = carFirstTypes;
    }
}
