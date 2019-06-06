package com.example.jrd48.chat;

public class ViewFriends {
	
	private long userID;
	
	private String friendRemark;
	
	private long groupID;
	
	private String friendUserName;
	
	private long friendUserID;
	
	private byte[] friendUserFace;

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getFriendRemark() {
		return friendRemark;
	}

	public void setFriendRemark(String friendRemark) {
		this.friendRemark = friendRemark;
	}

	public long getGroupID() {
		return groupID;
	}

	public void setGroupID(long groupID) {
		this.groupID = groupID;
	}

	public String getFriendUserName() {
		return friendUserName;
	}

	public void setFriendUserName(String friendUserName) {
		this.friendUserName = friendUserName;
	}

	public long getFriendUserID() {
		return friendUserID;
	}

	public void setFriendUserID(long friendUserID) {
		this.friendUserID = friendUserID;
	}

	public byte[] getFriendUserFace() {
		return friendUserFace;
	}

	public void setFriendUserFace(byte[] friendUserFace) {
		this.friendUserFace = friendUserFace;
	}
	
	
	
	

}
