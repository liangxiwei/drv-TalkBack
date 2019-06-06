package com.example.jrd48.chat.search;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Administrator on 2016/12/6.
 */

public class SearchFriends implements Parcelable {
    private String phoneNum;// 手机号
    private String userName;// 好友的名字
    private String nickName;// 好友的备注名
    private int friendStar;// 是否星标 1 表示true
    private byte[] userPic;// 用户头像
    private int userSex;// 性别
    private String applyInfo;// 申请消息
    private String type;
    private String searchType;
    private Long teamID;
    private String teamName;
    private int memberRole; // 我在群中的角色
    private String pinyin;
    private String pinYinHeadChar;
    private String size;
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinYinHeadChar() {
        return pinYinHeadChar;
    }

    public void setPinYinHeadChar(String pinYinHeadChar) {
        this.pinYinHeadChar = pinYinHeadChar;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    public int getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(int memberRole) {
        this.memberRole = memberRole;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
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


    public static final Creator<SearchFriends> CREATOR = new Creator<SearchFriends>() {
        @Override
        public SearchFriends createFromParcel(Parcel in) {
            SearchFriends ap = new SearchFriends();
            ap.setPhoneNum(in.readString());
            ap.setUserName(in.readString());
            ap.setNickName(in.readString());
            ap.setFriendStar(in.readInt());
            ap.setUserPic(in.createByteArray());
            ap.setUserSex(in.readInt());
            ap.setApplyInfo(in.readString());
            ap.setType(in.readString());
            ap.setSearchType(in.readString());
            ap.setTeamName(in.readString());
            ap.setMemberRole(in.readInt());
            ap.setTeamID(in.readLong());
            ap.setPinyin(in.readString());
            ap.setPinYinHeadChar(in.readString());
            ap.setSize(in.readString());
            ap.setKeyword(in.readString());
            return ap;
        }

        @Override
        public SearchFriends[] newArray(int size) {
            return new SearchFriends[size];
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
        parcel.writeString(type);
        parcel.writeString(searchType);
        parcel.writeLong(teamID);
        parcel.writeString(teamName);
        parcel.writeInt(memberRole);
        parcel.writeString(pinyin);
        parcel.writeString(pinYinHeadChar);
        parcel.writeString(size);
        parcel.writeString(keyword);
    }
}
