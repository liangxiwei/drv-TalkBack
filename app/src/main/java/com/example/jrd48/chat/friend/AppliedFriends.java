package com.example.jrd48.chat.friend;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/12/6.
 */

public class AppliedFriends implements Parcelable {
    private String phoneNum;// 手机号
    private String userName;// 好友的名字
    private String nickName;// 好友的备注名
    private int friendStar;// 是否星标 1 表示true
    private byte[] userPic;// 用户头像
    private int userSex;// 性别
    private String applyInfo;// 申请消息
    private String carID; //绑定车机ID
    private String city; //城市
    private String prov; //城市
    private String town; //城市
    private String birthday; //生日
    private String carNum; //车牌号
    private String carBand; //品牌
    private String carType2; //车型
    private String carType3; //车型

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getFriendStar() {
        return friendStar;
    }

    public void setFriendStar(int friendStar) {
        this.friendStar = friendStar;
    }

    public byte[] getUserPic() {
        return userPic;
    }

    public void setUserPic(byte[] userPic) {
        this.userPic = userPic;
    }

    public int getUserSex() {
        return userSex;
    }

    public void setUserSex(int userSex) {
        this.userSex = userSex;
    }

    public String getApplyInfo() {
        return applyInfo;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }


    public static final Creator<AppliedFriends> CREATOR = new Creator<AppliedFriends>() {
        @Override
        public AppliedFriends createFromParcel(Parcel in) {
            AppliedFriends ap = new AppliedFriends();
            ap.setPhoneNum(in.readString());
            ap.setUserName(in.readString());
            ap.setNickName(in.readString());
            ap.setFriendStar(in.readInt());
            ap.setUserPic(in.createByteArray());
            ap.setUserSex(in.readInt());
            ap.setApplyInfo(in.readString());


            ap.setCarID(in.readString());//String
            ap.setCity(in.readString());//String
            ap.setProv(in.readString());//String
            ap.setTown(in.readString());//String
            ap.setBirthday(in.readString());//Long
            ap.setCarNum(in.readString());//String
            ap.setCarBand(in.readString());//String
            ap.setCarType2(in.readString());//String
            ap.setCarType3(in.readString());//String
            return ap;
        }

        @Override
        public AppliedFriends[] newArray(int size) {
            return new AppliedFriends[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(phoneNum);
        parcel.writeString(userName);
        parcel.writeString(nickName);
        parcel.writeInt(friendStar);
        parcel.writeByteArray(userPic);
        parcel.writeInt(userSex);
        parcel.writeString(applyInfo);
        parcel.writeString(carID);
        parcel.writeString(city);
        parcel.writeString(prov);
        parcel.writeString(town);
        parcel.writeString(birthday);
        parcel.writeString(carNum);
        parcel.writeString(carBand);
        parcel.writeString(carType2);
        parcel.writeString(carType3);
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
    }

    public String getCarBand() {
        return carBand;
    }

    public void setCarBand(String carBand) {
        this.carBand = carBand;
    }

    public String getCarType2() {
        return carType2;
    }

    public void setCarType2(String carType2) {
        this.carType2 = carType2;
    }

    public String getCarType3() {
        return carType3;
    }

    public void setCarType3(String carType3) {
        this.carType3 = carType3;
    }
}
