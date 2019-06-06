package com.example.jrd48.chat.group;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/12/6.
 */

public class AppliedTeams implements Parcelable {
    private Long teamID;// 组ID
    private String teamName;// 组的名字
    private String userPhone;// 组手机号
    private String userName;// 用户名
    private int applyType;// 申请或者邀请
    private int inviteUserName;//
    private String inviteUserPhone;


    public static final Creator<AppliedTeams> CREATOR = new Creator<AppliedTeams>() {
        @Override
        public AppliedTeams createFromParcel(Parcel in) {
            AppliedTeams at = new AppliedTeams();
            at.setTeamID(in.readLong());
            at.setTeamName(in.readString());
            at.setUserPhone(in.readString());
            at.setUserName(in.readString());
            at.setApplyType(in.readInt());
            at.setInviteUserName(in.readInt());
            at.setInviteUserPhone(in.readString());
            return at;
        }

        @Override
        public AppliedTeams[] newArray(int size) {
            return new AppliedTeams[size];
        }
    };

    public Long getTeamID() {
        return teamID;
    }

    public void setTeamID(Long teamID) {
        this.teamID = teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

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

    public int getApplyType() {
        return applyType;
    }

    public void setApplyType(int applyType) {
        this.applyType = applyType;
    }

    public int getInviteUserName() {
        return inviteUserName;
    }

    public void setInviteUserName(int inviteUserName) {
        this.inviteUserName = inviteUserName;
    }

    public String getInviteUserPhone() {
        return inviteUserPhone;
    }

    public void setInviteUserPhone(String inviteUserPhone) {
        this.inviteUserPhone = inviteUserPhone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(teamID);
        parcel.writeString(teamName);
        parcel.writeString(userPhone);
        parcel.writeString(userName);
        parcel.writeInt(applyType);
        parcel.writeInt(inviteUserName);
        parcel.writeString(inviteUserPhone);
    }
}
