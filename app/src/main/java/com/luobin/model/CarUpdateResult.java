package com.luobin.model;

import java.util.ArrayList;


public class CarUpdateResult extends ResultStatus {

    private ArrayList<CarBrands> carBrands;

    private ArrayList<CarFirstType> carFirstTypes;

    private ArrayList<CarLastTypes> carLastTypes;

    public ArrayList<CarBrands> getCarBrands() {
        return carBrands;
    }

    public void setCarBrands(ArrayList<CarBrands> carBrands) {
        this.carBrands = carBrands;
    }

    public ArrayList<CarFirstType> getCarFirstTypes() {
        return carFirstTypes;
    }

    public void setCarFirstTypes(ArrayList<CarFirstType> carFirstTypes) {
        this.carFirstTypes = carFirstTypes;
    }

    public ArrayList<CarLastTypes> getCarLastTypes() {
        return carLastTypes;
    }

    public void setCarLastTypes(ArrayList<CarLastTypes> carLastTypes) {
        this.carLastTypes = carLastTypes;
    }


}
