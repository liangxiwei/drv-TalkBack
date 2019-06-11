package com.example.jrd48.service.protocol.root;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.dvr.DvrConfig;
import com.luobin.utils.MyInforTool;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.jrd48.service.protocol.root.ReceiverProcesser.PHONE_NUMBER;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class UserInfoProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.UserInfoProcesser";
    private String[] sexItems = new String[]{" ", "男", "女", "未设置"};

    private MySQLiteOpenHelper myOpenHelper;
    private SQLiteDatabase mSQLiteDatabase;

    public UserInfoProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "got reg answer: "+ HexTools.byteArrayToHex(data));
        try {
            ProtoMessage.UserInfo resp = ProtoMessage.UserInfo.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Intent i = new Intent(ACTION);
            try {
                //ProtoMessage.TeamInfo re = ProtoMessage.TeamInfo.parseFrom(ArrayUtils.subarray(data, 4, data.length));
                if (resp == null) {
                    throw new Exception("unknown response.");
                }
                i.putExtra("error_code", resp.getErrorCode());
                if (resp.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得正确应答：" + resp.toString());
                    // TODO: 这里处理添加 其他正确的数据
                    // i.putExtra(other useful value);
                    byte[] bitmapByte = resp.getUserPic().toByteArray();
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    String phone = preferences.getString("phone", "");

                    FriendFaceUtill.saveFriendFaceImg(resp.getUserName(), phone, bitmapByte, context);
                    GlobalImg.reloadImg(context, phone);
                    Intent in = new Intent("ACTION.changeImage");
                    in.putExtra(PHONE_NUMBER,phone);
                    context.sendBroadcast(in);
                    try {
                        String address = ReceiverProcesser.getMyDataRoot(context) + "/" + phone + "/";
                        File outputDir = new File(address);
                        if (!outputDir.exists()) {
                            outputDir.mkdirs();
                        }
                        //Log.i("chatjrd","保存头像地址:"+address);

                        File outputImage = new File(address, "head2.jpg");
                        Log.i("pocdemo", "save head img path: " + outputImage);
                        if (!outputImage.exists()) {
                            outputImage.createNewFile();
                        }
                        FileOutputStream imageOutput = new FileOutputStream(outputImage);//打开输入流
                        imageOutput.write(bitmapByte, 0, bitmapByte.length);//将byte写入硬盘
                        imageOutput.close();
                        Log.i("pocdemo", "save head img path [ok]: " + outputImage);
                    } catch (Exception ex) {

                        ex.printStackTrace();
                        Log.w("pocdemo", "save head img path [fail]：" + ex.getMessage());
                    }
                    i.putExtra("name", resp.getUserName());//String
                    i.putExtra("sex", resp.getUserSex());//int
                    i.putExtra("car_id", resp.getCarID());//String
                    Log.d("pangtao","interest = " + resp.getInterest());
                    Log.d("pangtao","career = " + resp.getCareer());
                    Log.d("pangtao","signature = " + resp.getSignature());
                    i.putExtra("interest",resp.getInterest());
                    i.putExtra("career",resp.getCareer());
                    i.putExtra("signature",resp.getSignature());
//                    i.putExtra("city",resp.getCity());//String
//                    i.putExtra("prov",resp.getProv());//String
//                    i.putExtra("town",resp.getTown());//String
//                    i.putExtra("nick_name",resp.getNickName());//String
//                    i.putExtra("birthday",resp.getBirthday());//Long
//                    i.putExtra("car_num",resp.getCarNum());//String
//                    i.putExtra("car_type1",resp.getCarType1());//String
//                    i.putExtra("car_type2",resp.getCarType2());//String
//                    i.putExtra("car_type3",resp.getCarType3());//String
//                    i.putExtra("apply_info",resp.getApplyInfo());//String


                    SharedPreferences preference = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    editor.putString("name", resp.getUserName());//String
                    editor.putString("phone", phone);
                    editor.putInt("user_sex", resp.getUserSex());//int
                    editor.putString("car_id", resp.getCarID());//String
                    editor.putString("city", resp.getCity().trim());//String
                    editor.putString("prov", resp.getProv().trim());//String
                    editor.putString("town", resp.getTown().trim());//String
                    editor.putString("birthday", resp.getBirthday());//Long
                    editor.putString("car_num", resp.getCarNum());//String
                    editor.putString("car_band", resp.getCarType1());//String
                    editor.putString("car_type2", resp.getCarType2());//String
                    editor.putString("car_type3", resp.getCarType3());//String
                    editor.putString("apply_info", resp.getApplyInfo());//String
                    editor.putString("sex", sexItems[resp.getUserSex()]);
                    editor.putInt("team_random", resp.getHideField8());
                    //editor.putInt("team_random_video", resp.getHideField9());
                    //0是打开，1是关闭
                    Log.d("UserInfo","team_random ="+resp.getHideField8()+",team_random_video="+resp.getHideField9());
                    editor.apply();
                    // add for radio chat sharing info
                    if (Build.PRODUCT.contains("LB1728")) {
                        MyInforTool myInforTool = new MyInforTool(context, true);
                        Bitmap bmp = GlobalImg.getImage(context, myInforTool.getPhone());
                        if (bmp != null) {
                            saveBitmapToSDCard(bmp);
                        }
                        File dbFile = new File(DvrConfig.getStoragePath() + "/userinfo.db");
                        try {
                            if (!dbFile.exists()) {
                                dbFile.createNewFile();
                            } else {
                                dbFile.delete();
                                dbFile.createNewFile();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        myOpenHelper = new MySQLiteOpenHelper(context);
                        mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

                        String TABLE_NAME = "userinfo";
                        String ID = "id";
                        String USER_ID = "phone";
                        String USER_NAME = "name";
                        String USER_ICON = "icon";

                        mSQLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" + ID
                                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + USER_ID + " text,"
                                + USER_NAME + " text,"
                                + USER_ICON + " blob not null );");
                        ContentValues values = new ContentValues();
                        values.put(MySQLiteOpenHelper.USER_ID, phone);
                        values.put(MySQLiteOpenHelper.USER_NAME, resp.getUserName());
                        if (bmp != null) {
                            values.put(MySQLiteOpenHelper.USER_ICON, bitmap2ByteArray(bmp));
                        }
                        mSQLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, values);

                        File file = new File("/data/data/com.luobin.dvr/shared_prefs", "token.xml");
                        try {
                            if (file.exists()) {
                                Runtime.getRuntime().exec("cp /data/data/com.luobin.dvr/shared_prefs/token.xml " + DvrConfig.getStoragePath());
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Log.i("chat", "错误码: " + resp.getErrorCode());
                }

            } catch (Exception e) {
                e.printStackTrace();
                i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
            }

            context.sendBroadcast(i);


            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String saveBitmapToSDCard(Bitmap bitmap) {
        String path = DvrConfig.getStoragePath() + "/icon.jpg";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] bitmap2ByteArray(Bitmap bitmap) {
        Bitmap bm = bitmap;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        return data;
    }

    private class MySQLiteOpenHelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "userinfo.db";
        public static final int VERSION = 1;
        public static final String TABLE_NAME = "userinfo";
        public static final String ID = "id";
        public static final String USER_ID = "phone";
        public static final String USER_NAME = "name";
        public static final String USER_ICON = "icon";

        public MySQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" + ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + USER_ID + " text,"
                    + USER_NAME + " text,"
                    + USER_ICON + " blob not null );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        }
    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }

}
