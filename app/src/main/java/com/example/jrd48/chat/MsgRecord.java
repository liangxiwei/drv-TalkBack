package com.example.jrd48.chat;

import android.graphics.Bitmap;

/**
 * Created by jrd48 on 2016/11/11.
 */

public class MsgRecord {
    private Bitmap headImage;
    private String groupName;
    private String recentRecord;
    private String recordTime;
    private boolean sg;
    private int new_msg;
    private long teamId;
    private String phone;
    private int memberRole;
    private int top = 0;

    public MsgRecord(Bitmap headImage, String groupName, String recentRecord, String recordTime, boolean sg, int new_msg) {
        this.headImage = headImage;
        this.groupName = groupName;
        this.recentRecord = recentRecord;
        this.recordTime= recordTime;
        this.sg = sg;
        this.new_msg =new_msg;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(int memberRole) {
        this.memberRole = memberRole;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getNew_msg(){
        return new_msg;
    }

    public Bitmap getHeadImage() {
        return headImage;
    }
    public String getGroupName(){ return groupName; }
    public String getRecentRecord(){
        return recentRecord;
    }
    public String getRecordTime(){
        return recordTime;
    }
    public boolean getSg(){
        return sg;
    }
}
