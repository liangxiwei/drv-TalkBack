package com.example.jrd48.chat.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.jrd48.chat.TeamMemberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrd48 on 2017/3/16.
 */

public class SQLiteTool {
    public synchronized static List<TeamMemberInfo> getTeamMembers(Context mContext, long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        List<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
            Cursor c = db.query("LinkmanMember", null, null, null, null, null, null);
            try {
                TeamMemberInfo af = null;
                while (c.moveToNext()) {
                    af = new TeamMemberInfo();
                    af.setUserName(c.getString(c.getColumnIndex("user_name")));
                    af.setUserPhone(c.getString(c.getColumnIndex("user_phone")));
                    af.setNickName(c.getString(c.getColumnIndex("nick_name")));
                    af.setRole(c.getInt(c.getColumnIndex("role")));
                    af.setMemberPriority(c.getInt(c.getColumnIndex("member_priority")));
                    teamMemberInfo.add(af);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        } finally {
            db.close();
        }
        return teamMemberInfo;
    }

    public synchronized static ArrayList<TeamMemberInfo> getAllTeamMembers(Context mContext, long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        ArrayList<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
            Cursor c = db.query("LinkmanMember", null, null, null, null, null, null);
            try {
                TeamMemberInfo af = null;
                while (c.moveToNext()) {
                    af = new TeamMemberInfo();
                    af.setUserName(c.getString(c.getColumnIndex("user_name")));
                    af.setUserPhone(c.getString(c.getColumnIndex("user_phone")));
                    af.setNickName(c.getString(c.getColumnIndex("nick_name")));
                    af.setRole(c.getInt(c.getColumnIndex("role")));
                    af.setMemberPriority(c.getInt(c.getColumnIndex("member_priority")));
                    teamMemberInfo.add(af);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        } finally {
            db.close();
        }
        return teamMemberInfo;
    }
}
