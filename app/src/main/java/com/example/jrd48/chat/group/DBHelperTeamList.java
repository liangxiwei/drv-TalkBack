package com.example.jrd48.chat.group;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelperTeamList extends SQLiteOpenHelper {

    public static final String NAME = "team_list.db";
    private static final int DATABASE_VERSION = 1;
    Context context;

    public DBHelperTeamList(Context context, String databaseName) {
        // CursorFactory设置为null,使用默认值
        super(context, databaseName, null, DATABASE_VERSION);
        this.context = context;
    }

    // 数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBManagerTeamList.TABLE_NAME
                + "( teamID bigint PRIMARY KEY, teamName varchar(16),teamDesc varchar(32),"
                + "teamType int ,groupID bigint ,memberRole int,teamPriority int)");
    }

    // 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
    }

    /**
     * 删除数据库
     *
     * @return
     */
    public boolean deleteDatabase(String databaseName) {
        return context.deleteDatabase(databaseName);
    }

}