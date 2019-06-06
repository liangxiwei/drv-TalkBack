package com.example.jrd48.chat;

import java.io.Serializable;

public class MsgPicInfo implements Serializable {

    private long sn;
    private long msgID;
    private long teamID;
    private String phone;
    private String address;
    private boolean download;
    private byte[] pictures;

    public MsgPicInfo(Msg msg) {
        sn = msg.getSn();
        msgID = msg.getMsgID();
        teamID = msg.getTeamID();
        phone = msg.getPhone();
        address = msg.getAddress();
        download = false;
    }

    public byte[] getPictures() {
        return pictures;
    }

    public void setPictures(byte[] pictures) {
        this.pictures = pictures;
    }

    public boolean getDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public long getSn() {
        return sn;
    }

    public long getMsgID() {
        return msgID;
    }

    public long getTeamID() {
        return teamID;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
