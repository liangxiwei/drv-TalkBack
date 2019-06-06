package com.example.jrd48.chat.FileTransfer;

/**
 * Created by jrd48 on 2017/2/13.
 */

public class TansferFileDown {
    private long teamID;
    private String phone;
    private long msgID;
    private long msgSize; // 附件的字节数
    private int packSize; // 暂时固定为 64K （即：64*1024个字节）
    private int packCnt; // 总包数
    private int packNum; // 上传时：包号： 范围 ： [0..packCnt], 当packNum = 0 时，仅设置 msgSum, 而不需要传递 packData,
    // 服务器会返回上次成功的 packNum.
    // 下载时：包号： 范围 ： [0..packCnt]
    private byte[] msgSum;   // 校验字，仅当 packNum = 0 时发送， MD5SUM，
    private byte[] packData; // 当 packNum == [1.. packCnt] 时有效
    private String fileAddress;


    public TansferFileDown(long teamID, String phone, long msgID, String fileAddress) {
        this.teamID = teamID;
        this.phone = phone;
        this.msgID = msgID;
        this.fileAddress = fileAddress;
        packNum = 0;
    }

    public String getFileAddress() {
        return fileAddress;
    }

    public long getMsgID() {
        return msgID;
    }

    public long getMsgSize() {
        return msgSize;
    }

    public void setMsgSize(long msgSize) {
        this.msgSize = msgSize;
    }

    public int getPackSize() {
        return packSize;
    }

    public void setPackSize(int packSize) {
        this.packSize = packSize;
    }

    public int getPackCnt() {
        return packCnt;
    }

    public void setPackCnt(int packCnt) {
        this.packCnt = packCnt;
    }

    public int getPackNum() {
        return packNum;
    }

    public void setPackNum(int packNum) {
        this.packNum = packNum;
    }

    public byte[] getMsgSum() {
        return msgSum;
    }

    public void setMsgSum(byte[] msgSum) {
        this.msgSum = msgSum;
    }

    public byte[] getPackData() {
        return packData;
    }

    public void setPackData(byte[] packData) {
        this.packData = packData;
    }

    public long getTeamID() {
        return teamID;
    }

    public String getPhone() {
        return phone;
    }
}
