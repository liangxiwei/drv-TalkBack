package com.example.jrd48.chat.group;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.chat.MyFileUtil;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBManagerTeamList {
    private DBHelperTeamList helper;
    private SQLiteDatabase db;
    Context mContext;
    public final static String TABLE_NAME = "teamlist";

    public DBManagerTeamList(Context context, String databaseName) {
        helper = new DBHelperTeamList(context, databaseName);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        this.mContext = context;
    }

    public DBManagerTeamList(Context context, boolean bReadOnly, String databaseName) {
        helper = new DBHelperTeamList(context, databaseName);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        if (bReadOnly) {
            db = helper.getReadableDatabase();
        } else {
            db = helper.getWritableDatabase();
        }
        this.mContext = context;
    }

    /**
     * 添加群组信息
     */
    public void add(List<TeamInfo> teaminfo) {
        Log.i("CHAT", "Save friends to cache.");
        db.beginTransaction(); // 开始事务
        try {
            try {
                db.delete(TABLE_NAME, null, null);
                for (TeamInfo c : teaminfo) {
                    try {
                        String teamName  = c.getTeamName();
                        if (!TextUtils.isEmpty(teamName)) {
                            if(c.getTeamType() == ProtoMessage.TeamType.teamRandom_VALUE && teamName.contains("海聊群")){
                                Log.i("CHAT", "Save friends teamName="+teamName);
                                teamName = "海聊群";
                            }
                        }
                        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES( ?, ?, ?, ?, ? ,? ,?)",

                                new Object[]{c.getTeamID(), teamName, c.getTeamDesc(),
                                        c.getTeamType(), c.getGroupID(), c.getMemberRole(), c.getTeamPriority()});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                db.setTransactionSuccessful(); // 设置事务成功完成
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    /**
     * 更新数据
     */
    public void updateData(TeamInfo info) {
        ContentValues cv = new ContentValues();
        cv.put("teamName", info.getTeamName());
        cv.put("teamDesc", info.getTeamDesc());
        cv.put("teamPriority", info.getTeamPriority());
        db.update(TABLE_NAME, cv, "teamID == ?", new String[]{String.valueOf(info.getTeamID())});
    }

    /**
     * 删除群组数据库里的数据
     *
     * @param
     */
    public void deleteTeam(Long teamID) {
        db.delete(TABLE_NAME, "teamID == ?", new String[]{String.valueOf(teamID)});
    }

    public void deleteSomeTeams(List<Long> teamIDs) {
        db.beginTransaction(); // 开始事务
        try {
            for (Long teamID : teamIDs) {
                deleteTeam(teamID);
            }
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    /**
     * 查找群组信息的Cursor
     *
     * @return
     */
    public Cursor queryTeamTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        if (db.isOpen())
            db.close();
    }

    public List<TeamInfo> getTeams() {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        List<TeamInfo> TeamsMsgs = new ArrayList<TeamInfo>();
        TeamInfo teamsMsg = null;
        try {
            while (c.moveToNext()) {
                teamsMsg = new TeamInfo();
                teamsMsg.setTeamID(c.getLong(c.getColumnIndex("teamID")));
                teamsMsg.setTeamName(c.getString(c.getColumnIndex("teamName")));
                teamsMsg.setTeamDesc(c.getString(c.getColumnIndex("teamDesc")));
                teamsMsg.setTeamType(c.getInt(c.getColumnIndex("teamType")));
                teamsMsg.setGroupID(c.getLong(c.getColumnIndex("groupID")));
                teamsMsg.setMemberRole(c.getInt(c.getColumnIndex("memberRole")));
                teamsMsg.setTeamPriority(c.getInt(c.getColumnIndex("teamPriority")));
                TeamsMsgs.add(teamsMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return TeamsMsgs;
    }

    public TeamInfo getTeamInfo(long teamID) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " where teamID = ?", new String[]{String.valueOf(teamID)});
        TeamInfo teamsMsg = null;
        try {
            if (c.moveToNext()) {
                teamsMsg = new TeamInfo();
                teamsMsg.setTeamID(c.getLong(c.getColumnIndex("teamID")));
                teamsMsg.setTeamName(c.getString(c.getColumnIndex("teamName")));
                teamsMsg.setTeamDesc(c.getString(c.getColumnIndex("teamDesc")));
                teamsMsg.setTeamType(c.getInt(c.getColumnIndex("teamType")));
                teamsMsg.setGroupID(c.getLong(c.getColumnIndex("groupID")));
                teamsMsg.setMemberRole(c.getInt(c.getColumnIndex("memberRole")));
                teamsMsg.setTeamPriority(c.getInt(c.getColumnIndex("teamPriority")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return teamsMsg;

    }

    public String getTeamName(long teamID) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " where teamID = ?", new String[]{String.valueOf(teamID)});
        String name = null;
        try {
            if (c.moveToNext()) {
                name = c.getString(c.getColumnIndex("teamName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return name;
    }
}