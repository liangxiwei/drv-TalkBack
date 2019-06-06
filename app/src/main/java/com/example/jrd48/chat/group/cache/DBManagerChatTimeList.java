package com.example.jrd48.chat.group.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.jrd48.chat.MyFileUtil;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManagerChatTimeList {
    private DBHelperChatTimeList helper;
    private SQLiteDatabase db;
    Context mContext;
    public final static String TABLE_NAME = "chatTimeList";

    public DBManagerChatTimeList(Context context, String databaseName) {
        helper = new DBHelperChatTimeList(context, databaseName);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        this.mContext = context;
    }

    public DBManagerChatTimeList(Context context, boolean bReadOnly, String databaseName) {
        helper = new DBHelperChatTimeList(context, databaseName);
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
     * 更新数据
     */
    public void updateData(long teamId,long chatTime) {
        ContentValues cv = new ContentValues();
        cv.put("teamID", teamId);
        cv.put("chatTime", chatTime);
        db.replace(TABLE_NAME, null, cv);
    }

    public Map<Long,Long> getTimeList() {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        Map<Long,Long> chatList = new HashMap<Long,Long>();
        try {
            while (c.moveToNext()) {
                chatList.put(c.getLong(c.getColumnIndex("teamID")),c.getLong(c.getColumnIndex("chatTime")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return chatList;
    }


    /**
     * close database
     */
    public void closeDB() {
        if (db.isOpen())
            db.close();
    }
}
