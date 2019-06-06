package com.example.jrd48.chat.group.cache;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.jrd48.chat.friend.AppliedFriends;

/**
 * Created by Administrator on 2017/1/18.
 */

public class MemberCache implements Parcelable {
    private String phoneNum;// 手机号
    private String nickName;// 好友的备注名

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getNickName() {
        return nickName;
    }

    public static final Creator<MemberCache> CREATOR = new Creator<MemberCache>() {
        @Override
        public MemberCache createFromParcel(Parcel in) {
            MemberCache ap = new MemberCache();
            ap.setPhoneNum(in.readString());
            ap.setNickName(in.readString());
            return ap;
        }

        @Override
        public MemberCache[] newArray(int size) {
            return new MemberCache[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(phoneNum);
        parcel.writeString(nickName);
    }
}