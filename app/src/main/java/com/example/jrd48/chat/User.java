package com.example.jrd48.chat;

public class User {
	public static final int NOSPEAK = 0;
	public static final int SPEAKING = 1;
	public static final String ADD = "邀请好友";

	private int state;
	private String name;
	private String phone;
	private int userSex;// 性别
	private String nickName;
	private String pinYin;
	private boolean bAddIcon = false;

	public boolean isAddIcon() {
		return bAddIcon;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPinYin() {
		return pinYin;
	}

	public void setPinYin(String pinYin) {
		this.pinYin = pinYin;
	}

	public User(int state, String name, String phone, int userSex) {
		this.state = state;
		this.name = name;
		this.phone = phone;
		this.userSex = userSex;

		if (phone.equals("")) {
			bAddIcon = true;
		}
	}

	public User(int state, String name, String phone, int userSex,String nickName) {
		this.state = state;
		this.name = name;
		this.phone = phone;
		this.userSex = userSex;
		this.nickName = nickName;
		if (phone.equals("")) {
			bAddIcon = true;
		}
	}

	/**
	 * 如果是加号的话，返回 为 null
	 */
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getUserSex() {
		return userSex;
	}

	public void setUserSex(int userSex) {
		this.userSex = userSex;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
