package com.example.jrd48.chat.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jrd48 on 2016/12/13.
 */

public class MsgRecordHelper extends SQLiteOpenHelper {
    public static final String MSG_RECORD = "create table Msg("
            + "new_msg integer,"
            + "year integer,"
            + "month integer,"
            + "date integer,"
            + "hour integer,"
            + "minute integer,"
            + "second integer,"
            + "msg_from integer,"//0:联系人; 1:群
            + "msg_type integer,"// 0:文字; 1:图片
            + "group_id integer,"
            + "phone text,"
            + "top integer,"
            + "msg text)";
    private Context mContext;
    private static final int V = 5;
    public MsgRecordHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, V);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MSG_RECORD);
        Log.i("chatJrd", "Create SQLite TeamRecord");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        switch (i){
            case 1:
                sqLiteDatabase.execSQL("alter table Msg add column year integer");
                sqLiteDatabase.execSQL("alter table Msg add column month integer");
                sqLiteDatabase.execSQL("alter table Msg add column date integer");
                sqLiteDatabase.execSQL("alter table Msg add column hour integer");
                sqLiteDatabase.execSQL("alter table Msg add column minute integer");
                sqLiteDatabase.execSQL("alter table Msg add column second integer");
            case 2:
                sqLiteDatabase.execSQL("alter table Msg add column new_msg integer");
            case 3:
                sqLiteDatabase.execSQL("alter table Msg add column top integer");
            case 4:
                sqLiteDatabase.execSQL("drop table if exists Msg");
                onCreate(sqLiteDatabase);
        }
    }
}
