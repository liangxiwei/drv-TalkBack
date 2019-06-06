package com.example.jrd48.chat.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jrd48 on 2016/12/13.
 */

public class LinkmanRecordHelper extends SQLiteOpenHelper {
    public static final String LINKMAN_RECORD = "create table LinkmanRecord("
            + "id integer primary key autoincrement,"
            + "service_id integer,"
            + "year integer,"
            + "month integer,"
            + "date integer,"
            + "hour integer,"
            + "minute integer,"
            + "second integer,"
            + "msg_send integer,"// 0:发送; 1:接收
            + "msg_type integer,"// 0:文字; 1:图片
            + "send_state integer,"
            + "pic_address text,"
            + "pic_address_true text,"
            + "percent integer,"
            + "msg text)";
    /**
     * 5 重建
     * 6
     */
    private static final int currVersion = 5;
    private Context mContext;


    public LinkmanRecordHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, currVersion);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(LINKMAN_RECORD);
        Log.i("chatJrd", "Create SQLite TeamRecord");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 以前的版本
        switch (oldVersion) {
            case 1:
            case 2:
            case 3:
            case 4:
                updateVersion4(sqLiteDatabase);
            default:
            // update 
        }

    }

    private void updateVersion4(SQLiteDatabase db) {
        db.execSQL("drop table if exists LinkmanRecord");
        onCreate(db);
    }
}
