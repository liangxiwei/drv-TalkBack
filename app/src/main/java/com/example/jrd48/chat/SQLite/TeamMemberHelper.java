package com.example.jrd48.chat.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jrd48.chat.friend.DBHelperFriendsList;

/**
 * Created by jrd48 on 2016/12/13.
 */

public class TeamMemberHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 2;
    public static final String LINKMAN_RECORD = "create table LinkmanMember("
            + "id integer primary key autoincrement,"
            + "user_phone text,"
            + "user_name text,"
            + "nick_name text,"
            + "role integer,"
            + "member_priority integer,"
            + "userSex INT," + DBHelperFriendsList.CAR_ID + " TEXT," + DBHelperFriendsList.CITY + " TEXT," + DBHelperFriendsList.PROV + " TEXT,"
            + DBHelperFriendsList.TOWN + " TEXT," + DBHelperFriendsList.BIRTHDAY + " TEXT," + DBHelperFriendsList.CAR_NUM + " TEXT," + DBHelperFriendsList.CAR_BAND + " TEXT," + DBHelperFriendsList.CAR_TYPE2 + " TEXT,"
            + DBHelperFriendsList.CAR_TYPE3 + " TEXT)";
    private Context mContext;

    public TeamMemberHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(LINKMAN_RECORD);
        Log.i("chatJrd", "Create SQLite TeamMenber");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
/*        switch (i){
        }*/
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS LinkmanMember");
        onCreate(sqLiteDatabase);
    }
}
