package com.example.jrd48.chat.friend;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelperFriendsList extends SQLiteOpenHelper {

    public static final String NAME = "friend_list.db";
    public static final String CAR_ID = "car_id";
    public static final String CITY = "city";
    public static final String PROV = "prov";
    public static final String TOWN = "town";
    public static final String BIRTHDAY = "birthday";
    public static final String CAR_NUM = "car_num";
    public static final String CAR_BAND = "car_band";
    public static final String CAR_TYPE2 = "car_type2";
    public static final String CAR_TYPE3 = "car_type3";
    private static final int DATABASE_VERSION = 2;
    Context context;

    public DBHelperFriendsList(Context context, String databaseName) {
        // CursorFactory设置为null,使用默认值
        super(context, databaseName, null, DATABASE_VERSION);
        this.context = context;
    }

    // 数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBManagerFriendsList.TABLE_NAME
                        + "( phoneNum varchar(32) PRIMARY KEY, nickName varchar(16),userName varchar(16),"
                        + "friendStar INT ,userSex INT," + CAR_ID + " TEXT," + CITY + " TEXT," + PROV + " TEXT,"
                        + TOWN + " TEXT," + BIRTHDAY + " TEXT," + CAR_NUM + " TEXT," + CAR_BAND + " TEXT," + CAR_TYPE2 + " TEXT,"
                        + CAR_TYPE3 + " TEXT)");
    }

    // 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
        db.execSQL("DROP TABLE IF EXISTS " + DBManagerFriendsList.TABLE_NAME);
        onCreate(db);
    }

    /**
     * 删除数据库
     *
     * @return
     */
    public boolean deleteDatabase(String databaseName) {
        FriendFaceUtill.deleteFriendsFace(context);
        return context.deleteDatabase(databaseName);
    }

}