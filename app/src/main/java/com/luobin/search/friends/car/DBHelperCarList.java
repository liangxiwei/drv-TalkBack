package com.luobin.search.friends.car;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelperCarList extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "car_msg.db";
    private static final int DATABASE_VERSION = 3;
    Context context;

    public DBHelperCarList(Context context) {
        //CursorFactory设置为null,使用默认值  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    //数据库第一次被创建时onCreate会被调用  
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS carlist"
                + "(userCarId BIGINT PRIMARY KEY,"
                + " carNumAll varchar(14),carFirstTypeName varchar(32) NOT NULL,carLastTypeName varchar(64) NOT NULL,carDefault INT,"
                + " friend_user_id BIGINT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS carBands(carBrandId int(11) NOT NULL,carBrandName varchar(32) NOT NULL,version int not null)");
        db.execSQL("CREATE TABLE IF NOT EXISTS carFirstTypes(carFirstTypeId int(11) NOT NULL,carBrandId int(11) NOT NULL,carFirstTypeName varchar(32) NOT NULL,version int not null)");
        db.execSQL("CREATE TABLE IF NOT EXISTS carLastTypes(carLastTypeId int(11) NOT NULL,carFirstTypeId int(11) NOT NULL,carLastTypeName varchar(64) NOT NULL,version int not null)");
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade  
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion==1 && newVersion==2){
//        	db.execSQL("ALTER TABLE carlist ADD COLUMN friend_user_id BIGINT");
//        }
//        if(oldVersion==2 && newVersion==3){
//        	db.execSQL("CREATE TABLE IF NOT EXISTS carBands(carBrandId int(11) NOT NULL,carBrandText varchar(32) NOT NULL,version int not null)");
//        	db.execSQL("CREATE TABLE IF NOT EXISTS carTypes(carTypeId int(11) NOT NULL,carBrandId int(11) NOT NULL,carTypeText varchar(64) NOT NULL,carEc decimal,version int not null)");
//        }
//        if(oldVersion==1 && newVersion==3){
//        	db.execSQL("ALTER TABLE carlist ADD COLUMN friend_user_id BIGINT");
//        	db.execSQL("CREATE TABLE IF NOT EXISTS carBands(carBrandId int(11) NOT NULL,carBrandText varchar(32) NOT NULL,version int not null)");
//        	db.execSQL("CREATE TABLE IF NOT EXISTS carTypes(carTypeId int(11) NOT NULL,carBrandId int(11) NOT NULL,carTypeText varchar(64) NOT NULL,carEc decimal,version int not null)");
//        }
    }

    /**
     * 删除数据库
     *
     * @return
     */
    public boolean deleteDatabase() {
        return context.deleteDatabase(DATABASE_NAME);
    }


}  