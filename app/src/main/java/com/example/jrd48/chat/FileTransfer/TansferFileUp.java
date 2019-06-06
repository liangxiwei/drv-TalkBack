package com.example.jrd48.chat.FileTransfer;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;


/**
 * Created by jrd48 on 2017/2/13.
 */

public class TansferFileUp {
    public static final int FILE_PACK_LEN = 128 * 1024;
    private long teamID;
    private String phone;
    private long msgID;
    private int msgSize; // 附件的字节数
    private int packSize; // 暂时固定为 64K （即：64*1024个字节）
    private int packCnt; // 总包数
    private int packNum; // 上传时：包号： 范围 ： [0..packCnt], 当packNum = 0 时，仅设置 msgSum, 而不需要传递 packData,
    // 服务器会返回上次成功的 packNum.
    // 下载时：包号： 范围 ： [0..packCnt]
    private byte[] msgSum;   // 校验字，仅当 packNum = 0 时发送， MD5SUM，
    private byte[] packData = new byte[FILE_PACK_LEN]; // 当 packNum == [1.. packCnt] 时有效
    private String fileAddress;
    private int mReadLen;

    public TansferFileUp(long _teamID, String _phone, long _msgID, String _fileAddress) {
        Log.i("jrdchat", "msgid:" + _msgID);
        teamID = _teamID;
        phone = _phone;
        msgID = _msgID;
        packSize = FILE_PACK_LEN;
        packNum = 0;
        fileAddress = _fileAddress;
        File file = new File(_fileAddress);
        msgSum = SHA256Tool.getFileSHA256(file);
        //Log.i("jrd", "校验字：" + msgSum);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                long size = fileInputStream.available();
                fileInputStream.close();
                msgSize = (int) size;
                packCnt = msgSize / packSize + 1;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        return;
    }

    public int getReadLen() {
        return mReadLen;
    }

    public boolean setPackNum(int _packNum) {
        mReadLen = 0;
        packNum = _packNum + 1;
        if (_packNum >= 0 && _packNum < packCnt) {
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(fileAddress));
                fileInputStream.skip(_packNum * FILE_PACK_LEN);
                mReadLen = fileInputStream.read(packData, 0, FILE_PACK_LEN);
                fileInputStream.close();
                Log.i("jrdchat", "文件第" + packNum + "包数据");
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("jrdchat", "文件可能被删除");
                return false;
            }
        } else {
            Log.i("jrdchat", "文件上传完成");
            return false;
        }

        return true;
    }

    public long getMsgID() {
        return msgID;
    }

    public int getMsgSize() {
        return msgSize;
    }

    public int getPackSize() {
        return packSize;
    }

    public int getPackCnt() {
        return packCnt;
    }

    public int getPackNum() {
        return packNum;
    }

    public byte[] getMsgSum() {
        return msgSum;
    }

    public byte[] getPackData() {
        return packData;
    }

    public long getTeamID() {
        return teamID;
    }

    public String getPhone() {
        return phone;
    }
}
