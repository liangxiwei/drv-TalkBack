package com.luobin.model;


import java.util.ArrayList;

/**
 * 地市
 * <br/>
 * DateTime:2016-10-15 19:07
 * Builder:Android Studio
 */
public class City extends Area {
    private ArrayList<County> counties = new ArrayList<County>();

    public ArrayList<County> getCounties() {
        return counties;
    }

    public void setCounties(ArrayList<County> counties) {
        this.counties = counties;
    }

}