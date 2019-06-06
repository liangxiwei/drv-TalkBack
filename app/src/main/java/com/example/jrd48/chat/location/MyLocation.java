package com.example.jrd48.chat.location;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

/**
 * Created by Administrator on 2016/12/6.
 */

public class MyLocation implements Parcelable {

    private String phoneNum;// 手机号

    private double lat;//纬度

    private double lng;//经度

    private int radius;//半径(米)

    private int isAccurate;// 是否准确：1：准确， 0：模糊

    private double speed;

    private double direction;

    private long time;//定位时间

//    private Timestamp locationTime;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

//    public Timestamp getLocationTime() {
//        return locationTime;
//    }
//
//    public void setLocationTime(Timestamp locationTime) {
//        this.locationTime = locationTime;
//    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

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


    public static final Creator<MyLocation> CREATOR = new Creator<MyLocation>() {
        @Override
        public MyLocation createFromParcel(Parcel in) {
            MyLocation ap = new MyLocation();
            ap.setPhoneNum(in.readString());
            ap.setLat(in.readDouble());
            ap.setLng(in.readDouble());
            ap.setRadius(in.readInt());
            ap.setIsAccurate(in.readInt());
            ap.setDirection(in.readDouble());
            ap.setSpeed(in.readDouble());
            ap.setTime(in.readLong());
//            ap.setLocationTime(in.readTimestamp());
            return ap;
        }

        @Override
        public MyLocation[] newArray(int size) {
            return new MyLocation[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(phoneNum);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeInt(radius);
        parcel.writeInt(isAccurate);
        parcel.writeDouble(direction);
        parcel.writeDouble(speed);
        parcel.writeLong(time);
    }
}
