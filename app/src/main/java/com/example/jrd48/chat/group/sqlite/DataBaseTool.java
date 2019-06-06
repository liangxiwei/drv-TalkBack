package com.example.jrd48.chat.group.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/6/27.
 */

public class DataBaseTool {

    private static String tableName = "LinkmanMember";

    private static String getTeamMemberHelperName(long teamID){
        return teamID + "TeamMember.dp";
    }

    public synchronized static void saveData(List<ProtoMessage.TeamMember> list, long teamID, Context context) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, getTeamMemberHelperName(teamID), null);
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
//        Log.d("tttt","保存数组："+teamID);
            //删除旧数据
            db.delete(tableName, null, null);
            ContentValues values = new ContentValues();
            for (ProtoMessage.TeamMember te : list) {
                values.put("user_phone", te.getUserPhone());
                values.put("user_name", te.getUserName());
                values.put("nick_name", te.getNickName());
                values.put("role", te.getRole());
                values.put("member_priority", te.getMemberPriority());
                //TODO  注意，暂时服务器还没有添加车牌相关消息，默认设置为空，防止空指针
                values.put("userSex", 0);
                values.put(DBHelperFriendsList.CAR_ID, "");
                values.put(DBHelperFriendsList.CITY,"");
                values.put(DBHelperFriendsList.PROV, "");
                values.put(DBHelperFriendsList.TOWN, "");
                values.put(DBHelperFriendsList.BIRTHDAY, "");
                values.put(DBHelperFriendsList.CAR_NUM, "");
                values.put(DBHelperFriendsList.CAR_BAND, "");
                values.put(DBHelperFriendsList.CAR_TYPE2, "");
                values.put(DBHelperFriendsList.CAR_TYPE3, "");
                db.insert(tableName, null, values);
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    public synchronized static List<TeamMemberInfo> getTeamMembers(Context mContext, long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, getTeamMemberHelperName(teamId), null);
        List<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
        SQLiteDatabase db = teamMemberHelper.getReadableDatabase();
        try {
            Cursor c = db.query(tableName, null, null, null, null, null, null);
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
