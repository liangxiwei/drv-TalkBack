package com.example.jrd48.chat;

/**
 * Created by jrd48
 */

class Linkmans {

    private String linkmanName;
    private String realName;
    private String linkmanPhone;
    private String linkmanNamePinYin;
    private int friendStar;
    private byte[] userPic;// 用户头像
    private int linkmanSex;// 性别
    private boolean online;//是否在线
    Linkmans(String linkmanName){ this.linkmanName = linkmanName; }

    Linkmans(String linkmanName, String linkmanPhone, int friendStar, byte[] userPic, int linkmanSex, String realName) {
        this.linkmanName = linkmanName;
        this.linkmanPhone = linkmanPhone;
        this.friendStar = friendStar;
        this.userPic = userPic;
        this.linkmanSex = linkmanSex;
        this.realName = realName;
    }

    Linkmans(String linkmanName, String linkmanNamePinYin, String linkmanPhone, int friendStar, byte[] userPic, int linkmanSex, String realName) {
        this.linkmanName = linkmanName;
        this.linkmanNamePinYin = linkmanNamePinYin;
        this.linkmanPhone = linkmanPhone;
        this.friendStar = friendStar;
        this.userPic = userPic;
        this.linkmanSex = linkmanSex;
        this.realName = realName;
    }

    String getLinkmanNamePinYin(){ return linkmanNamePinYin; }

    void setLinkmanNamePinYin(String linkmanNamePinYin) {
        this.linkmanNamePinYin = linkmanNamePinYin;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    int getFriendStar(){
        return friendStar;
    }

    public void setFriendStar(int friendStar) {
        this.friendStar = friendStar;
    }

    String getLinkmanName(){ return linkmanName; }

    public void setLinkmanName(String linkmanName) {
        this.linkmanName = linkmanName;
    }

    String getLinkmanPhone() {
        return linkmanPhone;
    }



    String getRealName() {
        return realName;
    }

    public int getLinkmanSex() {
        return linkmanSex;
    }
    byte[] getUserPic() {
        return userPic;
    }
}
