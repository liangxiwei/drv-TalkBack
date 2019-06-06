package com.example.jrd48.chat;

import java.io.Serializable;

public class SimpleUser implements Serializable{
	private static final long serialVersionUID = -1883391808842970821L;
	
	private long userID;
	
	private int sexCode;
	
	private String userProv;

	private String userCity;
	
	private String userName;
	
	private byte[] userFace;
	
	private String phoneName;
	
	private String userPhone;
	
	private String fromActivity;

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

	public String getPhoneName() {
		return phoneName;
	}

	public void setPhoneName(String phoneName) {
		this.phoneName = phoneName;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getFromActivity() {
		return fromActivity;
	}

	public void setFromActivity(String fromActivity) {
		this.fromActivity = fromActivity;
	}
	
	

}
