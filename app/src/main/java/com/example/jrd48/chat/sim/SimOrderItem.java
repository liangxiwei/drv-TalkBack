package com.example.jrd48.chat.sim;


/**
 * Created by zhouyuhuan on 2018/3/13.
 */

public class SimOrderItem {
    private String id;
    private int usingStatus;
    private String cid;
    private String pid;
    private int count;
    private String activeStartTime;
    private String activeEndTime;
    private String notActiveLostTime;
    private int periodTotal;
    private int periodType;
    private int flowTotal;
    private int subscribeType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUsingStatus() {
        return usingStatus;
    }

    public void setUsingStatus(int usingStatus) {
        this.usingStatus = usingStatus;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getActiveStartTime() {
        return activeStartTime;
    }

    public void setActiveStartTime(String activeStartTime) {
        this.activeStartTime = activeStartTime;
    }

    public String getActiveEndTime() {
        return activeEndTime;
    }

    public void setActiveEndTime(String activeEndTime) {
        this.activeEndTime = activeEndTime;
    }

    public String getNotActiveLostTime() {
        return notActiveLostTime;
    }

    public void setNotActiveLostTime(String notActiveLostTime) {
        this.notActiveLostTime = notActiveLostTime;
    }

    public int getPeriodTotal() {
        return periodTotal;
    }

    public void setPeriodTotal(int periodTotal) {
        this.periodTotal = periodTotal;
    }

    public int getPeriodType() {
        return periodType;
    }

    public void setPeriodType(int periodType) {
        this.periodType = periodType;
    }

    public int getFlowTotal() {
        return flowTotal;
    }

    public void setFlowTotal(int flowTotal) {
        this.flowTotal = flowTotal;
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(int subscribeType) {
        this.subscribeType = subscribeType;
    }

    @Override
    public String toString() {
        return "SimOrderItem{" +
                "id='" + id + '\'' +
                ", usingStatus=" + usingStatus +
                ", cid='" + cid + '\'' +
                ", pid='" + pid + '\'' +
                ", count=" + count +
                ", activeStartTime='" + activeStartTime + '\'' +
                ", activeEndTime='" + activeEndTime + '\'' +
                ", notActiveLostTime='" + notActiveLostTime + '\'' +
                ", periodTotal=" + periodTotal +
                ", periodType=" + periodType +
                ", flowTotal=" + flowTotal +
                ", subscribeType=" + subscribeType +
                '}';
    }
}
