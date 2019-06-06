package com.example.jrd48.chat.friend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.example.jrd48.chat.MyFileUtil;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;

public class DBManagerFriendsList {
    private DBHelperFriendsList helper;
    private SQLiteDatabase db;
    Context mContext;
    public final static String TABLE_NAME = "friendlist";

    public DBManagerFriendsList(Context context, String databaseName) {
        helper = new DBHelperFriendsList(context, databaseName);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        this.mContext = context;
    }

    public DBManagerFriendsList(Context context, boolean bReadOnly, String databaseName) {
        helper = new DBHelperFriendsList(context, databaseName);
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
     * 添加用户信息
     */
    public void add(final List<AppliedFriends> friends, boolean check) {
        Log.i("CHAT", "Save friends to cache.");
        db.beginTransaction(); // 开始事务
        try {
            try {
                if (check) {
                    db.delete(TABLE_NAME, null, null);
                }
                for (AppliedFriends c : friends) {
                    try {
                        ContentValues values = new ContentValues();
                        values.put("phoneNum", c.getPhoneNum());
                        values.put("nickName", c.getNickName());
                        values.put("userName", c.getUserName());
                        values.put("friendStar", c.getFriendStar());
                        values.put("userSex", c.getUserSex());

                        values.put(DBHelperFriendsList.CAR_ID, c.getCarID());
                        values.put(DBHelperFriendsList.CITY, c.getCity());
                        values.put(DBHelperFriendsList.PROV, c.getProv());
                        values.put(DBHelperFriendsList.TOWN, c.getTown());
                        values.put(DBHelperFriendsList.BIRTHDAY, c.getBirthday());
                        values.put(DBHelperFriendsList.CAR_NUM, c.getCarNum());
                        values.put(DBHelperFriendsList.CAR_BAND, c.getCarBand());
                        values.put(DBHelperFriendsList.CAR_TYPE2, c.getCarType2());
                        values.put(DBHelperFriendsList.CAR_TYPE3, c.getCarType3());
                        db.replace(TABLE_NAME,null,values);
//                        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES( ?, ?, ?, ?, ? )",
//
//                                new Object[]{c.getPhoneNum(), c.getNickName(),
//                                        c.getUserName(), c.getFriendStar(), c.getUserSex()});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                new AsyncTask<String, Integer, Integer>() {
                    @Override
                    protected Integer doInBackground(String... strings) {
                        for (AppliedFriends c : friends) {
                            try {
                                FriendFaceUtill.saveFriendFaceImg(c.getUserName(), c.getPhoneNum(), c.getUserPic(), mContext);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return 0;
                    }
                }.execute("");

                db.setTransactionSuccessful(); // 设置事务成功完成
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    /**
     * update person's age
     *
     */
    // public void updateAge(TrackIem person) {
    // ContentValues cv = new ContentValues();
    // cv.put("age", person.age);
    // db.update("person", cv, "name = ?", new String[]{person.name});
    // }

    /**
     * 删除用户数据库里的数据
     *
     * @param
     */
    public void deleteFriend(String phoneNum) {
        db.delete(TABLE_NAME, "phoneNum == ?", new String[]{phoneNum});
    }

    public void deleteSomeFriends(List<String> phoneNums) {
        db.beginTransaction(); // 开始事务
        try {
            for (String phoneNum : phoneNums) {
                deleteFriend(phoneNum);
            }
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    /**
     * update friend's star
     */
    public void updateFriendStar(AppliedFriends af) {
        ContentValues cv = new ContentValues();
        cv.put("friendStar", af.getFriendStar());
        db.update(TABLE_NAME, cv, "phoneNum == ?", new String[]{af.getPhoneNum()});
    }

    /**
     * update friend's star
     */
    public void updateFriendNickName(AppliedFriends af) {
        ContentValues cv = new ContentValues();
        cv.put("nickName", af.getNickName());
        db.update(TABLE_NAME, cv, "phoneNum == ?", new String[]{af.getPhoneNum()});
    }

    /**
     * 查找用户信息的Cursor
     *
     * @return
     */
    public Cursor queryFriendTheCursor() {
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

    public List<AppliedFriends> getFriends(boolean needPic) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        List<AppliedFriends> friendsMsgs = new ArrayList<AppliedFriends>();
        AppliedFriends friendsMsg = null;
        try {
            while (c.moveToNext()) {
                friendsMsg = new AppliedFriends();
                /*
                    db.execSQL("CREATE TABLE IF NOT EXISTS " + DBManagerFriendsList.TABLE_NAME
				+ "(poneNumber TEXT PRIMARY KEY， nickName TEXT,userName TEXT,"
				+ "friendStar boolean ,userSex bigint)");
                 */

                friendsMsg.setUserName(c.getString(c.getColumnIndex("userName")));
                friendsMsg.setUserSex(c.getInt(c.getColumnIndex("userSex")));
                friendsMsg.setNickName(c.getString(c.getColumnIndex("nickName")));
                friendsMsg.setFriendStar(c.getInt(c.getColumnIndex("friendStar")));
//				friendsMsg.setUserPic(c.getBlob(c.getColumnIndex("friendUserFace")));
                friendsMsg.setPhoneNum(c.getString(c.getColumnIndex("phoneNum")));


                friendsMsg.setCarID(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_ID)));//String
                friendsMsg.setCity(c.getString(c.getColumnIndex(DBHelperFriendsList.CITY)));//String
                friendsMsg.setProv(c.getString(c.getColumnIndex(DBHelperFriendsList.PROV)));//String
                friendsMsg.setTown(c.getString(c.getColumnIndex(DBHelperFriendsList.TOWN)));//String
                friendsMsg.setBirthday(c.getString(c.getColumnIndex(DBHelperFriendsList.BIRTHDAY)));//Long
                friendsMsg.setCarNum(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_NUM)));//String
                friendsMsg.setCarBand(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_BAND)));//String
                friendsMsg.setCarType2(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_TYPE2)));//String
                friendsMsg.setCarType3(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_TYPE3)));//String

//				try {
//					File file = getFriendFaceImgFile(mContext, friendsMsg.getPhoneNum());

//					friendsMsg.setUserPic(MyFileUtil.readFile(file));
//				} catch (Exception e) {
//					Log.d("CHAT", e.getMessage());
//				}

                /** 永远不需要加载图片 */
//                if (needPic) {
//                    friendsMsg.setUserPic(FriendFaceUtill.getFriendsFace(mContext, friendsMsg.getPhoneNum()));
//                }
                friendsMsgs.add(friendsMsg);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return friendsMsgs;
    }

    public String getFriendName(String phone) {
        Cursor c = db.rawQuery("SELECT userName FROM " + TABLE_NAME + " where phoneNum = ?", new String[]{phone});
        try {
            if (c.moveToNext()) {
                return c.getString(c.getColumnIndex("userName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }

        return "";
    }

    public Map<String, String> getFriendsNickName() {
        Cursor c = db.rawQuery("SELECT phoneNum, nickName, userName FROM " + TABLE_NAME + " order by phoneNum", null);
        Map<String, String> ret = new HashMap<String, String>();
        try {
            while (c.moveToNext()) {
                String nickName = c.getString(c.getColumnIndex("nickName"));
                if (nickName == null || nickName.equals("")) {
                    nickName = c.getString(c.getColumnIndex("userName"));
                }
                ret.put(c.getString(c.getColumnIndex("phoneNum")), nickName);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }

        return null;
    }

    public static String getAFriendNickName(Context context,String phone) {
        DBHelperFriendsList helper = new DBHelperFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
        SQLiteDatabase db = helper.getWritableDatabase();
        String nickName = null;
        String userName = null;
        Cursor c = db.rawQuery("SELECT phoneNum, nickName, userName FROM " + TABLE_NAME + " where phoneNum = ?", new String[]{phone});
        try {
            while (c.moveToNext()) {
                nickName = c.getString(c.getColumnIndex("nickName"));
                userName = c.getString(c.getColumnIndex("userName"));
                if (nickName != null && !nickName.equals("")) {
                    return nickName;
                } else if (userName != null && !userName.equals("")) {
                    return userName;
                } else {
                    return phone;
                }
            }
            return phone;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            if (db.isOpen())
                db.close();
        }
        return phone;
    }
    public AppliedFriends getFriend(String phoneNumber) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME+ " where phoneNum = ?", new String[]{phoneNumber});
        AppliedFriends friendsMsg = null;
        try {
            if (c.moveToNext()) {
                friendsMsg = new AppliedFriends();
                friendsMsg.setUserName(c.getString(c.getColumnIndex("userName")));
                friendsMsg.setUserSex(c.getInt(c.getColumnIndex("userSex")));
                friendsMsg.setNickName(c.getString(c.getColumnIndex("nickName")));
                friendsMsg.setFriendStar(c.getInt(c.getColumnIndex("friendStar")));
                friendsMsg.setPhoneNum(c.getString(c.getColumnIndex("phoneNum")));
                friendsMsg.setCarID(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_ID)));//String
                friendsMsg.setCity(c.getString(c.getColumnIndex(DBHelperFriendsList.CITY)));//String
                friendsMsg.setProv(c.getString(c.getColumnIndex(DBHelperFriendsList.PROV)));//String
                friendsMsg.setTown(c.getString(c.getColumnIndex(DBHelperFriendsList.TOWN)));//String
                friendsMsg.setBirthday(c.getString(c.getColumnIndex(DBHelperFriendsList.BIRTHDAY)));//Long
                friendsMsg.setCarNum(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_NUM)));//String
                friendsMsg.setCarBand(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_BAND)));//String
                friendsMsg.setCarType2(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_TYPE2)));//String
                friendsMsg.setCarType3(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_TYPE3)));//String
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return friendsMsg;
    }
    public String getCarNum(String phoneNumber) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME+ " where phoneNum = ?", new String[]{phoneNumber});
        String carNum = null;
        try {
            if (c.moveToNext()) {
                carNum = c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_NUM));//String
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        return carNum;
    }
}