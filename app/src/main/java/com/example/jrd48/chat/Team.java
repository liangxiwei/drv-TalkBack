package com.example.jrd48.chat;

/**
 * Created by jrd48
 */

public class Team {
    private String linkmanName;
    private String linkmanDesc;


    private long teamID;
    private int memberRole;
    private boolean top;
    private int teamType;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    private boolean isSelect = false;

    Team(String linkmanName, String linkmanDesc, long teamID, int memberRole, boolean top, int teamType) {
        this.linkmanName = linkmanName;
        this.linkmanDesc = linkmanDesc;
        this.teamID = teamID;
        this.memberRole = memberRole;
        this.top = top;
        this.teamType = teamType;
    }

    int getMemberRole() {
        return memberRole;
    }

    public void setTeamID(long teamID) {
        this.teamID = teamID;
    }


    public long getTeamID() {
        return teamID;
    }
   public String getLinkmanName(){ return linkmanName; }
    String getLinkmanDesc() {
        return linkmanDesc;
    }

    public boolean isTop() {
        return top;
    }

    public int getTeamType() {
        return teamType;
    }

    public void setTeamType(int teamType) {
        this.teamType = teamType;
    }

    @Override
    public String toString() {
        return linkmanName;
    }
}
