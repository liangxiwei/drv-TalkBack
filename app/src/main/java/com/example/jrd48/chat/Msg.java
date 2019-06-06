package com.example.jrd48.chat;

import android.graphics.Bitmap;

public class Msg {

	public static final int TYPE_RECEIVED = 0;
	public static final int TYPE_SENT = 1;
	public static final int TYPE_RECEIVED_IMAGE = 2;
	public static final int TYPE_SENT_IMAGE = 3;
	public static final int TYPE_MSG_RECORD = 4;
	public static final int TYPE_MSG_CANCEL = 5;
	public static final int TYPE_MSG_MY_CANCEL = 6;
	public static final int TYPE_RECEIVED_VIDEO = 7;
	public static final int TYPE_SENT_VIDEO = 8;

	public static final int SENTS = 0;
	public static final int SENDING = 1;
	public static final int SENTF = 2;

	private Bitmap bitmap;
	private int percent;
	private String content;
	private int type;
	private String time;
	private int sentState;
	private long sn;
	private long msgID;
	private long teamID;
	private String phone;
	private String address;
	private String allTime;

	public Msg(String content, int type, String time, int sentState, long sn, String phone, long msgID, long teamID) {
		this.content = content;
		this.type = type;
		this.time = time;
		this.sentState = sentState;
		this.sn = sn;
		this.phone = phone;
		this.msgID = msgID;
		this.teamID = teamID;
	}

	public Msg(Bitmap bitmap, int type, String time, String allTime, int sentState, long sn, String phone, long msgID, long teamID) {
		percent = 0;
		this.bitmap = bitmap;
		this.type = type;
		this.time = time;
		this.sentState = sentState;
		this.sn = sn;
		this.phone = phone;
		this.msgID = msgID;
		this.teamID = teamID;
		this.allTime = allTime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getTeamID() {
		return teamID;
	}

	public long getMsgID() {
		return msgID;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}
	public String getPhone() {
		return phone;
	}
	public int getSentState() {
		return sentState;
	}
	public void setSentState(int sentState) {
		this.sentState = sentState;
	}
	public Bitmap getBitmap(){
		return bitmap;
	}
	public String getContent(){
		return content;
	}
	public int getType(){
		return type;
	}
	public long getSn() {
		return sn;
	}
	public String getTime() {
		return time;
	}

	public String getAllTime() {
		return allTime;
	}
}
