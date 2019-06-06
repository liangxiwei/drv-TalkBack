package com.luobin.model;

import java.util.ArrayList;

/**
 * 省份
 * <br/>
 * DateTime:2016-10-15 19:06
 * Builder:Android Studio
 */
public class Province extends Area {
    private ArrayList<City> cities = new ArrayList<City>();

    public ArrayList<City> getCities() {
        return cities;
    }

    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }

}