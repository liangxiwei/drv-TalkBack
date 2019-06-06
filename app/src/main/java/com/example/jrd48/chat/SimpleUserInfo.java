package com.example.jrd48.chat;

public class SimpleUserInfo {
	
	private long userID;
	
	private int sexCode;
	
	private String userProv;

	private String userCity;
	
	private String userName;
	
	private byte[] userFace;
	
	private String userPhone;

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public byte[] getUserFace() {
		return userFace;
	}

	public void setUserFace(byte[] userFace) {
		this.userFace = userFace;
	}

	public int getSexCode() {
		return sexCode;
	}

	public void setSexCode(int sexCode) {
		this.sexCode = sexCode;
	}

	public String getUserProv() {
		return userProv;
	}

	public void setUserProv(String userProv) {
		this.userProv = userProv;
	}

	public String getUserCity() {
		return userCity;
	}

	public void setUserCity(String userCity) {
		this.userCity = userCity;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}
	
	

}
