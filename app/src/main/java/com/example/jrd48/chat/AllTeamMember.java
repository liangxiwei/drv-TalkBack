package com.example.jrd48.chat;

public class AllTeamMember {
    public static final int NOSPEAK = 0;
    public static final int SPEAKING = 1;
    public static final String ADD = "邀请好友";
    public static final String ADDITIONAL = "删除或者退出";
    public static final String MYNICKNAME = "自己在群中昵称";
    public static final String LOOK_MAP = "[地图查看]";
    private int state;
    private String name;
    private String phone;
    private int userSex;// 性别
    private String nickName;
    private int role;
    private int memberPriority;
    private boolean online;//判断是否在线
    private boolean friend;//判断是否是好友
    private boolean onChat = false;//判断是否在群组对话中

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getMemberPriority() {
        return memberPriority;
    }

    public void setMemberPriority(int memberPriority) {
        this.memberPriority = memberPriority;
    }

    public AllTeamMember() {

    }

    public AllTeamMember(int state,boolean isFriend, String name, String phone, int userSex, int memberPriority, String nickName, int role) {
        this.state = state;
        this.name = name;
        this.phone = phone;
        this.userSex = userSex;
        this.memberPriority = memberPriority;
        this.nickName = nickName;
        this.role = role;
        setFriend(isFriend);
    }

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

    public boolean isOnChat() {
        return onChat;
    }

    public void setOnChat(boolean onChat) {
        this.onChat = onChat;
    }

}
