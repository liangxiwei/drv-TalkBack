package com.example.jrd48.chat.friend;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/2/17.
 */

public class FriendsAndTeams implements Parcelable {
    private String phoneNum;// 手机号
    private String userName;// 好友的名字
    private String nickName;// 好友的备注名
    private int friendStar;// 是否星标 1 表示true
    private byte[] userPic;// 用户头像
    private int userSex;// 性别
    private String applyInfo;// 申请消息
    private Long teamID;// 组ID
    private String teamName;// 组的名字
    private String userPhone;// 组手机号
    private int applyType;// 申请或者邀请
    private int inviteUserName;//
    private String inviteUserPhone;
    private String type;
    private String typePic;

    public String getTypePic() {
        return typePic;
    }

    public void setTypePic(String typePic) {
        this.typePic = typePic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    /*
       private Long teamID;// 组ID
       private String teamName;// 组的名字
       private String userPhone;// 组手机号
       private int applyType;// 申请或者邀请
       private int inviteUserName;//
       private String inviteUserPhone;
        */
    public static final Creator<FriendsAndTeams> CREATOR = new Creator<FriendsAndTeams>() {
        @Override
        public FriendsAndTeams createFromParcel(Parcel in) {
            FriendsAndTeams ap = new FriendsAndTeams();
            ap.setPhoneNum(in.readString());
            ap.setUserName(in.readString());
            ap.setNickName(in.readString());
            ap.setFriendStar(in.readInt());
            ap.setUserPic(in.createByteArray());
            ap.setUserSex(in.readInt());
            ap.setApplyInfo(in.readString());
            ap.setTeamID(in.readLong());
            ap.setTeamName(in.readString());
            ap.setUserPhone(in.readString());
            ap.setApplyType(in.readInt());
            ap.setInviteUserName(in.readInt());
            ap.setInviteUserPhone(in.readString());
            ap.setType(in.readString());
            ap.setTypePic(in.readString());
            return ap;
        }

        @Override
        public FriendsAndTeams[] newArray(int size) {
            return new FriendsAndTeams[size];
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
        parcel.writeLong(teamID);
        parcel.writeString(teamName);
        parcel.writeString(userPhone);
        parcel.writeInt(applyType);
        parcel.writeInt(inviteUserName);
        parcel.writeString(inviteUserPhone);
        parcel.writeString(type);
        parcel.writeString(typePic);
    }

}
