package com.example.jrd48.chat.group.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.jrd48.chat.group.TeamInfo;

import java.util.ArrayList;
import java.util.List;

public class DBManagerCacheList {
    private DBHelperCacheList helper;
    private SQLiteDatabase db;
    Context mContext;
    private static long teamID;
    public static String TABLE_NAME = "cachelist";

    public DBManagerCacheList(Context context, String databaseName) {
        helper = new DBHelperCacheList(context, databaseName);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        this.mContext = context;
    }

    /**
     * 添加群组信息
     */
    public void add(List<MemberCache> memberCache) {
        Log.i("CHAT", "Save friends to cache.");
        db.beginTransaction(); // 开始事务
        try {
            try {
                db.delete(TABLE_NAME, null, null);
                for (MemberCache c : memberCache) {
                    try {
                        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES( ?, ?)",

                                new Object[]{c.getPhoneNum(), c.getNickName()});
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
    public void updateData(MemberCache memberCache) {
        ContentValues cv = new ContentValues();
        cv.put("nickName", memberCache.getNickName());
        db.update(TABLE_NAME, cv, "phoneNum == ?", new String[]{memberCache.getPhoneNum()});
    }

    /**
     * 删除群组数据库里的数据
     *
     * @param
     */
    public void deleteTeam(String phoneNum) {
        db.delete(TABLE_NAME, "phoneNum == ?", new String[]{phoneNum});
    }

    public void deleteMemberCache(List<String> phoneNum) {
        db.beginTransaction(); // 开始事务
        try {
            for (String phone : phoneNum) {
                deleteTeam(phone);
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

    public List<MemberCache> getMemberCache() {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        List<MemberCache> memberCache = new ArrayList<MemberCache>();
        MemberCache mc = null;
        try {
            while (c.moveToNext()) {
                mc = new MemberCache();
                mc.setPhoneNum(c.getString(c.getColumnIndex("phoneNum")));
                mc.setNickName(c.getString(c.getColumnIndex("nickName")));
                memberCache.add(mc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return memberCache;
    }

    /**
     * 删除数据库中某个表
     */
    public void deleteTable() {
        db.delete(TABLE_NAME, null, null);
    }
}