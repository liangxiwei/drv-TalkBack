package com.luobin.model;

/**
 * Created by Administrator on 2017/8/16.
 */

public class CallState {
    private String number;
    private long roomId;
    private int state;
    public CallState(String number,long roomId,int state){
        this.number = number;
        this.roomId = roomId;
        this.state = state;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String toString(){
        return "number:" + number + ",roomId" + roomId + ",state" + state;
    }
}
