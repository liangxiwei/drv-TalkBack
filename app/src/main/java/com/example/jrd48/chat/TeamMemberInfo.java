package com.example.jrd48.chat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/12/8.
 */

public class TeamMemberInfo implements Parcelable{

    public static final Creator<TeamMemberInfo> CREATOR = new Creator<TeamMemberInfo>() {
        @Override
        public TeamMemberInfo createFromParcel(Parcel in) {
            TeamMemberInfo ti = new TeamMemberInfo();
            ti.setUserPhone(in.readString());
            ti.setUserName(in.readString());
            ti.setNickName(in.readString());
            ti.setRole(in.readInt());
            ti.setMemberPriority(in.readInt());
            return ti;
        }

        @Override
        public TeamMemberInfo[] newArray(int size) {
            return new TeamMemberInfo[size];
        }
    };
    private String userPhone;
    private String userName;
    private String nickName;
    private int role;
    private int memberPriority;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    private boolean isSelect = false;

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
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


    public int getMemberPriority() {
        return memberPriority;
    }

    public void setMemberPriority(int memberPriority) {
        this.memberPriority = memberPriority;
    }


    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userPhone);
        parcel.writeString(userName);
        parcel.writeString(nickName);
        parcel.writeInt(role);
        parcel.writeInt(memberPriority);
    }
}
