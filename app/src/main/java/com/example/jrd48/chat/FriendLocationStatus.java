package com.example.jrd48.chat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class FriendLocationStatus implements Parcelable {

    private double lat;
    private double lng;
    private int radius;  // 半径(米)
    private int isAccurate; // 是否准确：1：准确， 0：模糊
    private long time; // 位置时间
    private String phoneNum; // 好友手机号
    private int latlngType; // 位置类型（0：高德，）

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
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

    public int getIsAccurate() {
        return isAccurate;
    }

    public void setIsAccurate(int isAccurate) {
        this.isAccurate = isAccurate;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getLatlngType() {
        return latlngType;
    }

    public void setLatlngType(int latlngType) {
        this.latlngType = latlngType;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public FriendLocationStatus(){
    }

    protected FriendLocationStatus(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        radius = in.readInt();
        isAccurate = in.readInt();
        time = in.readLong();
        phoneNum = in.readString();
        latlngType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeInt(radius);
        parcel.writeInt(isAccurate);
        parcel.writeLong(time);
        parcel.writeString(phoneNum);
        parcel.writeInt(latlngType);
    }
    
    public static final Creator<FriendLocationStatus> CREATOR = new Creator<FriendLocationStatus>() {
        @Override
        public FriendLocationStatus createFromParcel(Parcel in) {
            return new FriendLocationStatus(in);
        }

        @Override
        public FriendLocationStatus[] newArray(int size) {
            return new FriendLocationStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


}
