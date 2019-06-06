package com.example.jrd48.chat;

/**
 * Created by Administrator on 2016/12/6.
 */

public class UserInfo  {
    private int errorCode;
    private String phoneNum;// 手机号
    private String userName;// 好友的名字
    private String nickName;// 好友的备注名
    private int friendStar ;// 是否星标 1 表示true
    private byte[] userPic;// 用户头像
    private int userSex;// 性别
    private String applyInfo;// 申请消息

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getFriendStar() {
        return friendStar;
    }

    public int isFriendStar() {
        return friendStar;
    }

    public void setFriendStar(int friendStar) {
        this.friendStar = friendStar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getApplyInfo() {
        return applyInfo;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }

    public int getUserSex() {
        return userSex;
    }

    public void setUserSex(int userSex) {
        this.userSex = userSex;
    }

    public byte[] getUserPic() {
        return userPic;
    }

    public void setUserPic(byte[] userPic) {
        this.userPic = userPic;
    }
}
