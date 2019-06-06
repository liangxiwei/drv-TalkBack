package com.example.jrd48.chat.group;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/12/8.
 */

public class TeamInfo implements Parcelable {
    private Long teamID;
    private String teamName;
    private String teamDesc;
    private int teamType;
    private Long groupID;
    private int memberRole; // 我在群中的角色
    private int teamPriority;//群优先级
    private String myTeamName; // 我在群中的名称
    private String pinyin;
    private String pinYinHeadChar;
    private String size;
    private String keyword;

    public String getMyTeamName() {
        return myTeamName;
    }

    public void setMyTeamName(String myTeamName) {
        this.myTeamName = myTeamName;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinYinHeadChar() {
        return pinYinHeadChar;
    }

    public void setPinYinHeadChar(String pinYinHeadChar) {
        this.pinYinHeadChar = pinYinHeadChar;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public static final Creator<TeamInfo> CREATOR = new Creator<TeamInfo>() {
        @Override
        public TeamInfo createFromParcel(Parcel in) {
            TeamInfo ti = new TeamInfo();
            ti.setTeamID(in.readLong());
            ti.setTeamName(in.readString());
            ti.setTeamDesc(in.readString());
            ti.setTeamType(in.readInt());
            ti.setGroupID(in.readLong());
            ti.setMemberRole(in.readInt());
            ti.setTeamPriority(in.readInt());
            ti.setMyTeamName(in.readString());
            ti.setPinyin(in.readString());
            ti.setPinYinHeadChar(in.readString());
            ti.setSize(in.readString());
            ti.setKeyword(in.readString());
            return ti;
        }

        @Override
        public TeamInfo[] newArray(int size) {
            return new TeamInfo[size];
        }
    };

    public Long getTeamID() {
        return teamID;
    }

    public void setTeamID(Long teamID) {
        this.teamID = teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamDesc() {
        return teamDesc;
    }

    public void setTeamDesc(String teamDesc) {
        this.teamDesc = teamDesc;
    }

    public int getTeamType() {
        return teamType;
    }

    public void setTeamType(int teamType) {
        this.teamType = teamType;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public int getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(int memberRole) {
        this.memberRole = memberRole;
    }

    public int getTeamPriority() {
        return teamPriority;
    }

    public void setTeamPriority(int teamPriority) {
        this.teamPriority = teamPriority;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(teamID);
        parcel.writeString(teamName);
        parcel.writeString(teamDesc);
        parcel.writeInt(teamType);
        parcel.writeLong(groupID);
        parcel.writeInt(memberRole);
        parcel.writeInt(teamPriority);
        parcel.writeString(myTeamName);
        parcel.writeString(pinyin);
        parcel.writeString(pinYinHeadChar);
        parcel.writeString(size);
        parcel.writeString(keyword);
    }
}
