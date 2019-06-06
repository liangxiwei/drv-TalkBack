package com.example.jrd48.chat.group.sqlite;


import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.util.ArrayList;

/**
 * Copyright: Copyright (c) 2017-2025
 * Company:
 * @date: 2017/8/21
 * describe:
 */
public class TeamMemberInfoManager {
    private ArrayList<ProtoMessage.TeamMember> teamMemberInfoList;
    private long teamId;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public ArrayList<ProtoMessage.TeamMember> getTeamMemberInfoList() {
        return teamMemberInfoList;
    }

    public void setTeamMemberInfoList(ArrayList<ProtoMessage.TeamMember> teamMemberInfoList) {
        this.teamMemberInfoList = teamMemberInfoList;
    }

    /**
     *
     * @param teamMemberInfo
     * @param teamId
     */
    public TeamMemberInfoManager(ProtoMessage.TeamMember teamMemberInfo, long teamId) {
        teamMemberInfoList = new ArrayList<>();
        teamMemberInfoList.add(teamMemberInfo);
        this.teamId = teamId;
    }

    public void addTeamMemberInfo(ProtoMessage.TeamMember teamMemberInfo) {
        teamMemberInfoList.add(teamMemberInfo);
    }
}
