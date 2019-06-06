package com.luobin.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jrd48.service.MyLogger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DBMyLogHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "log.db";
    public static final String TABLE_NAME = "log";
    private static final int DATABASE_VERSION = 1;
    private static final int MAX_LOG_COUNT = 1000;
    MyLogger mLog = MyLogger.jLog();
    private static final int MAX_DESC = 128;

    Context context;

    public DBMyLogHelper(Context context, String databaseName) {
        // CursorFactory设置为null,使用默认值
        super(new SDDatabaseContext(context), databaseName, null, DATABASE_VERSION);
        this.context = context;

    }

    // 数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "( " +
                    " log_id integer PRIMARY KEY autoincrement, " +
                    " log_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    " log_code integer," +
                    " log_desc varchar(" + MAX_DESC + "))"
            );
            mLog.i("创建数据库成功");
        } catch (Exception e) {
            e.printStackTrace();
            mLog.e("创建数据库失败：" + e.getMessage());
        }
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

    public synchronized static void insertLog(Context context, int err_code, String err_desc, java.util.Date dt) {
        MyLogger mLog = MyLogger.jLog();

        BootLog.touch(context);

        try {
            if (err_desc == null) {
                err_desc = "";
            }

            if (err_desc.length() > MAX_DESC) {
                err_desc = err_desc.substring(0, MAX_DESC);
            }

            DBMyLogHelper dbHelper = new DBMyLogHelper(context, DB_NAME);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                ContentValues cv = new ContentValues();

                cv.put("log_code", err_code);
                cv.put("log_desc", err_desc);

                if (dt != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cv.put("log_date", sdf.format(dt));
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cv.put("log_date", sdf.format(new Date()));
                }

                db.insert(TABLE_NAME, null, cv);
                mLog.i("保存日志记录成功");
            } finally {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLog.e("插入日志出错：" + e.getMessage());
        }
    }

    public static ArrayList<LogItem> queryAll(Context context) {
        ArrayList ret = new ArrayList<LogItem>();
        MyLogger mLog = MyLogger.jLog();
        try {
            DBMyLogHelper dbHelper = new DBMyLogHelper(context, DB_NAME);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Cursor c = db.rawQuery("select log_id, datetime(log_date,'localtime'),log_code, log_desc from " + TABLE_NAME + " order by log_id", null);
                try {
                    while (c.moveToNext()) {
                        LogItem i = new LogItem();
                        try {
                            i.setDate(sdf.parse(c.getString(1)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            mLog.w("日期转换错误：" + e.getMessage());
                        }

                        i.setCode(c.getInt(2));
                        i.setDesc(c.getString(3));
                        ret.add(i);
                    }
                } finally {
                    c.close();
                }
            } finally {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLog.e("读取日志历史出错");
        }
        return ret;

    }

    public synchronized static void shrinkLog(Context context) {
        MyLogger mLog = MyLogger.jLog();
        try {
            DBMyLogHelper dbHelper = new DBMyLogHelper(context, DB_NAME);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                Cursor c = db.rawQuery("select count(*) from " + TABLE_NAME, null);
                int rowCount = 0;
                try {
                    if (c.moveToFirst()) {
                        rowCount = c.getInt(0);
                    }
                } finally {
                    c.close();
                }
                int n = rowCount - MAX_LOG_COUNT;
                if (n > 0) {
                    mLog.w("删除事件表中的前 " + n + " 行");
                    String str = String.format("delete from %s " +
                                    "where rowid in (select rowid from %s order by log_id limit %d)"
                            , TABLE_NAME, TABLE_NAME, n);
                    mLog.d("delete old data sql: " + str);
                    db.execSQL(str);

                    // 更新ID
                    str = String.format("update %s set log_id = log_id - %d", TABLE_NAME, n);
                    mLog.d("update sql: " + str);
                    db.execSQL(str);
                }
            } finally {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLog.e("删除旧日志出错: " + e.getMessage());
        }
    }

    public static void insertLog(Context context, int errCode, String errDesc) {
        insertLog(context, errCode, errDesc, null);
    }
}