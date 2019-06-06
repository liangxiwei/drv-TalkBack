package com.example.jrd48.chat.group;

import android.content.Context;

import com.example.jrd48.chat.group.cache.DBTableName;

import java.util.List;

/**
 * Created by Administrator on 2017/1/22.
 */

public class GetTeamMsg {
    public static String getTeamName(Context context, long teamID) {
        try {
            DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
            String teamName = db.getTeamName(teamID);
            db.closeDB();
            return teamName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
