package com.example.jrd48.service.protocol.root;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.service.MyLogger;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SQLite.LinkmanRecordHelper;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.SQLite.TeamRecordHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CancelNotify;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class GetHistoryMsgProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.ReceiverProcesser";//故意使用ReceiverProcesser的action,为了配合activity中的消息监听
    private Handler mHandler = null;

    public GetHistoryMsgProcesser(Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private Runnable mRefreshTeamRunnable = new Runnable() {
        @Override
        public void run() {
            MyLogger.jLog().i("refresh team and team list");
            // 遍历群列表和群成员
            new AsyncTask<String, Integer, Integer>() {
                @Override
                protected Integer doInBackground(String... strings) {
                    try {
                        ProtoMessage.CommonRequest.Builder builderTeamList = ProtoMessage.CommonRequest.newBuilder();
                        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builderTeamList.build());

                        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
                        MyService.start(context, ProtoMessage.Cmd.cmdGetAllTeamMember.getNumber(), builder.build());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute("");
        }
    };

    /*private long createNewSn(SQLiteDatabase db, String tableName) {
        //"select seq from sqlite_sequence where name = ?"
        long seq = 0;
        Cursor cursor = db.rawQuery("select seq from sqlite_sequence where name = ?", new String[]{tableName});
        if (cursor.moveToNext()) {
            seq = cursor.getInt(0); //获取第一列的值,第一列的索引从0开始
        } else {
            Log.i("jrdchat", "查找DB SN失败，打开数据库失败");
            //throw new RuntimeException("createNewSn failed");
        }
        cursor.close();
        seq++;
        Log.i("jrdchat", "获取新的SN：" + seq);

        return seq;
    }*/
    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonMsgList resp = ProtoMessage.CommonMsgList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Intent i = new Intent(ACTION);

            try {
                if (resp == null) {
                    throw new Exception("unknown response.");
                }
                i.putExtra("error_code", resp.getErrorCode());
                if (resp.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("jim", "获得历史消息正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    // i.putExtra(other useful value);
                    List<ProtoMessage.CommonMsg> list = resp.getMsgListList();
                    ProtoMessage.CommonMsgConfirm.Builder builder = ProtoMessage.CommonMsgConfirm.newBuilder();

                    // 取自己的手机号
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    final String myphone = preferences.getString("phone", "");

                    MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myphone + "MsgShow.dp", null);
                    SQLiteDatabase db = msgRecordHelper.getWritableDatabase();

                    final String picAddress1 = ReceiverProcesser.getMyDataRoot(context);

                    try {
                        Intent notificationIntent;
                        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification notification = null;
                        for (ProtoMessage.CommonMsg commonMsg : list) {
                            int msgType;
                            int group;
                            final long groupId = commonMsg.getToTeamID();
                            String phone = commonMsg.getFromUserPhone();
                            String msg;
                            String titleMsg;
                            int new_msg;
                            int top = 0;


                            // 打开连系人消息记录数据库
                            LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context, myphone + phone + "LinkmanMsgShow.dp", null);
                            SQLiteDatabase dbMan = null;

                            // 打开群组消息记录数据库
                            TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + groupId + "TeamMsgShow.dp", null);
                            SQLiteDatabase dbTeam = null;

                            String picAddress2;
                            String picAddress3;
                            String picAddress4;
                            String picAddress5;
                            String picAddress6;
//                            long l;
                            if (groupId == 0) {
                                dbMan = linkmanRecordHelper.getWritableDatabase();
//                                l = createNewSn(dbMan, "LinkmanRecord");
                            } else {
                                dbTeam = teamRecordHelper.getWritableDatabase();
//                                l = createNewSn(dbTeam, "TeamRecord");
                            }

                            picAddress2 = "/" + myphone;
                            picAddress3 = "/" + groupId;
                            picAddress4 = "/" + phone;
//                            picAddress5 = "/" + l + ".jpg";
//                            picAddress6 = "/" + l + "_true" + ".jpg";

//                            if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtText.getNumber()) {
//                                msgType = 0;
//                                msg = commonMsg.getMsgContent().toStringUtf8();
//                                i.putExtra("text", msg);
//
//                            } else if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtImage.getNumber()
//                                    || commonMsg.getMsgType() == ProtoMessage.MsgType.mtVideoFile_VALUE) {
//                                msgType = commonMsg.getMsgType();
//                                if (msgType == ProtoMessage.MsgType.mtImage_VALUE) {
//                                    msg = "[图片]";
//                                } else {
//                                    msg = "[视频]";
//                                    picAddress6 = "/" + l + "_true" + ".mp4";
//                                }
//                                byte[] bitmapByte = commonMsg.getMsgContent().toByteArray();
//                                try {
//
//                                    File outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4);
//                                    if (!outputImage.exists()) {
//                                        outputImage.mkdirs();
//                                    }
//                                    File tempFile = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress6);
//                                    if (tempFile.exists()) {
//                                        tempFile.delete();
//                                    }
//                                    outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress5);
//                                    if (!outputImage.exists()) {
//                                        outputImage.createNewFile();
//                                    }
//                                    FileOutputStream imageOutput = new FileOutputStream(outputImage);//打开输入流
//                                    imageOutput.write(bitmapByte, 0, bitmapByte.length);//将byte写入硬盘
//                                    imageOutput.close();
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                            } else if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtCancel_VALUE) {
//                                ContentValues values = new ContentValues();
//                                values.put("msg_type", ProtoMessage.MsgType.mtCancel_VALUE);
//                                ContentValues values_msg = new ContentValues();
//                                values_msg.put("msg", "撤回了一条消息");
//                                String origServiceID = commonMsg.getMsgContent().toStringUtf8();
//
//                                Cursor cursor;
//                                if (groupId == 0) {
//                                    dbMan.update("LinkmanRecord", values, "service_id = ?", new String[]{origServiceID});
//                                    db.update("Msg", values_msg, "phone = ? and msg_from = ?", new String[]{phone, 0 + ""});
//                                    cursor = dbMan.query("LinkmanRecord", null, "service_id = ?", new String[]{origServiceID}, null, null, null);
//                                    FirstActivity.deletePic(context, cursor);
//                                    cursor.close();
//                                    dbMan.close();
//                                } else {
//                                    dbTeam.update("TeamRecord", values, "service_id = ?", new String[]{origServiceID});
//                                    db.update("Msg", values_msg, "group_id = ?", new String[]{groupId + ""});
//                                    cursor = dbTeam.query("TeamRecord", null, "service_id = ?", new String[]{origServiceID}, null, null, null);
//                                    FirstActivity.deletePic(context, cursor);
//                                    cursor.close();
//                                    dbTeam.close();
//                                }
//
//                                i.putExtra("group", groupId);
//                                i.putExtra("phone", phone);
//                                i.putExtra("msg_type", 3);
//                                context.sendBroadcast(i);
//                                builder.addMsgIDs(commonMsg.getMsgID());
//
//                                continue;
//                            } else
                            if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtEnterTeam_VALUE
                                    || commonMsg.getMsgType() == ProtoMessage.MsgType.mtLeaveTeam_VALUE) {
                                long groupIdTemp = groupId;
                                String teamName = "";
                                if (groupId == 0) {
                                    groupIdTemp = Long.valueOf(commonMsg.getMsgContent().toStringUtf8());
                                    try {
                                        DBManagerTeamList dblist = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                                        teamName = dblist.getTeamName(groupIdTemp);
                                        dblist.closeDB();
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    if (TextUtils.isEmpty(teamName)) {
                                        Log.d("team","您退出了某群 groupIdTemp:"+groupIdTemp);
                                      //  ToastR.setToast(context, "您退出了该群");
                                    } else {
                                        ToastR.setToast(context, "您退出了["+teamName +"]群组");
                                    }
                                    new CancelNotify(context, groupIdTemp, manager, phone);//取消通知
                                    MsgTool.deleteTeamMsg(context, groupIdTemp);
                                    Log.i("chatjrd", "自己被踢出群或者加入群");
                                    Intent in = new Intent("ACTION.refreshTeamList");
                                    in.putExtra("singout", FirstActivity.SING_OUT);
                                    in.putExtra("teamid", groupIdTemp);
                                    context.sendBroadcast(in);
                                }
                                if (commonMsg.getMsgContent().toStringUtf8().equals(myphone)) {
                                    Intent in = new Intent("ACTION.refreshTeamList");
                                    context.sendBroadcast(in);
                                    Log.d("team","您进入了某群 groupIdTemp:"+groupIdTemp);
                                    //ToastR.setToast(context, "您进入了该群");
                                }
                                i.putExtra("group", groupIdTemp);
                                i.putExtra("phone", phone);
                                i.putExtra("msg_type", 4);
                                context.sendBroadcast(i);
                                mHandler.removeCallbacks(mRefreshTeamRunnable);
                                mHandler.postDelayed(mRefreshTeamRunnable, 2000);
                                if (groupId == 0) {
                                    dbMan.close();
                                } else {
                                    dbTeam.close();
                                }
                                builder.addMsgIDs(commonMsg.getMsgID());
                                continue;
                            } else if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtChangeHeadImage_VALUE) {

                                final String changePhone = commonMsg.getMsgContent().toStringUtf8();
                                ProtoMessage.SearchUser.Builder builderTemp = ProtoMessage.SearchUser.newBuilder();
//                                Log.d("jim","历史消息头像修改"+ProtoMessage.MsgType.mtChangeHeadImage_VALUE);
//                                Log.d("jim","历史消息 changePhone："+changePhone + "  phone:"+phone);
                                builderTemp.setPhoneNum(changePhone);
                                builderTemp.setOnlyPhoneNum(true);
                                MyService.start(context, ProtoMessage.Cmd.cmdSearchUser.getNumber(), builderTemp.build());
                                IntentFilter filter = new IntentFilter();
                                filter.addAction(SearchFriendProcesser.ACTION);
                                new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
                                    @Override
                                    public void onTimeout() {
                                    }

                                    @Override
                                    public void onGot(Intent i) {
                                        GlobalImg.reloadImg(context, changePhone);
                                        Intent in = new Intent("ACTION.changeImage");
                                        in.putExtra(ReceiverProcesser.PHONE_NUMBER,changePhone);
                                        context.sendBroadcast(in);
                                    }
                                });
                                if (groupId == 0) {
                                    dbMan.close();
                                } else {
                                    dbTeam.close();
                                }
                                builder.addMsgIDs(commonMsg.getMsgID());
                                continue;
                            } else if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtAcceptFriend_VALUE) {
                                final String acceptPhone = commonMsg.getMsgContent().toStringUtf8();
                                new MsgTool().getAcceptInfo(acceptPhone, context);
                                if (groupId == 0) {
                                    dbMan.close();
                                } else {
                                    dbTeam.close();
                                }
                                builder.addMsgIDs(commonMsg.getMsgID());
                                continue;
                            } else if (commonMsg.getMsgType() == ProtoMessage.MsgType.mtDeleteFriend_VALUE) {
                                final String deletePhone = commonMsg.getMsgContent().toStringUtf8();
                                new CancelNotify(context, groupId, manager, deletePhone);//取消通知
                                MsgTool.deleteFriendsMsg(context, deletePhone);
                                Intent intent = new Intent(MainActivity.FRIEND_ACTION);
                                intent.putExtra("phone", phone);
                                intent.putExtra("delete", MainActivity.deleteStr);
                                context.sendBroadcast(intent);
                                if (groupId == 0) {
                                    dbMan.close();
                                } else {
                                    dbTeam.close();
                                }
                                builder.addMsgIDs(commonMsg.getMsgID());
                                continue;
                            } else {
                                // 未知处理
                                if (groupId == 0) {
                                    dbMan.close();
                                } else {
                                    dbTeam.close();
                                }
                                builder.addMsgIDs(commonMsg.getMsgID());
                                continue;
                            }
//
//
//                            /** 已知消息类型，并且非撤销 */
//                            Cursor cursor;
//                            String teamName = "";
//                            if(groupId == 0) {
//                                cursor = db.query("Msg", null, "phone=? and group_id=?", new String[]{phone, groupId + ""}, null, null, null);
//                            }else {
//                                cursor = db.query("Msg", null, "group_id=?", new String[]{groupId + ""}, null, null, null);
//                                DBManagerTeamList db1 = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
//                                teamName = db1.getTeamName(groupId);
//                                db1.closeDB();
//                                if (teamName == null) {
//                                    throw new RuntimeException("取组信息时出错");
//                                }
//                            }
//                            boolean msgAdd = true;
//                            String key = (String) SharedPreferencesUtils.get(context, ReceiverProcesser.UPDATE_KEY, "");
//                            if (key == null || key.equals("") || (!key.equals(phone) && !key.equals(teamName))) {
//                                msgAdd = true;
//                            } else {
//                                msgAdd = false;
//                            }
//                            if (cursor.moveToFirst()) {
//                                top = cursor.getInt(cursor.getColumnIndex("top"));
//                                new_msg = cursor.getInt(cursor.getColumnIndex("new_msg"));
//                                if (new_msg < 99 && new_msg >= 0 && msgAdd) {
//                                    new_msg++;
//                                }
//                            } else {
//                                new_msg = 1;
//                            }
//                            cursor.close();
//
//                            //long time = ;
//                            Date dt = new Date(commonMsg.getMsgTime() * 1000);
//                            Calendar c = Calendar.getInstance(Locale.getDefault());
//                            c.setTime(dt);
//
//                            Log.i("chatjare1", "msg time: " + dt.toString());
////                        SimpleDateFormat sdf = new SimpleDateFormat()
////                        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
////                        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
////                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
////                        SimpleDateFormat hourFormat = new SimpleDateFormat("hh");
////                        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
////                        SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
////                        int year = Integer.parseInt(yearFormat.format(new Date(time + 0)));
////                        int month = Integer.parseInt(monthFormat.format(new Date(time + 0)));
////                        int date = Integer.parseInt(dateFormat.format(new Date(time + 0)));
////                        int hour = Integer.parseInt(hourFormat.format(new Date(time + 0)));
////                        int minute = Integer.parseInt(minuteFormat.format(new Date(time + 0)));
////                        int second = Integer.parseInt(secondFormat.format(new Date(time + 0)));
//                            int year = c.get(Calendar.YEAR);
//                            int month = c.get(Calendar.MONTH);
//                            int date = c.get(Calendar.DATE);
//                            int hour = c.get(Calendar.HOUR_OF_DAY);
//                            int minute = c.get(Calendar.MINUTE);
//                            int second = c.get(Calendar.SECOND);
//                            Log.i("chatjare1", "msg time: " + year + "年" + month + "月" + date + "日" + hour + "时" + minute + "分" + second + "秒");
//
//                            if (groupId == 0) {
//                                group = 0;
//
//                                ContentValues values = new ContentValues();
//                                values.put("year", year);
//                                values.put("month", month);
//                                values.put("date", date);
//                                values.put("hour", hour);
//                                values.put("minute", minute);
//                                values.put("second", second);
//                                values.put("msg_send", 1);
//                                values.put("msg_type", msgType);
//                                values.put("msg", msg);
//                                values.put("pic_address", picAddress2 + picAddress3 + picAddress4 + picAddress5);
//                                values.put("pic_address_true", picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress6);
//                                values.put("service_id", commonMsg.getMsgID());
//                                if (dbMan != null) {
//                                    dbMan.insert("LinkmanRecord", null, values);
//
//                                    dbMan.close();
//                                }
//                                db.delete("Msg", "phone = ? and msg_from = ?", new String[]{phone, group + ""});
//                            } else {
//                                group = 1;
//
//                                ContentValues values = new ContentValues();
//                                values.put("year", year);
//                                values.put("month", month);
//                                values.put("date", date);
//                                values.put("hour", hour);
//                                values.put("minute", minute);
//                                values.put("second", second);
//                                values.put("msg_send", 1);
//                                values.put("msg_type", msgType);
//                                values.put("phone", phone);
//                                values.put("msg", msg);
//                                values.put("pic_address", picAddress2 + picAddress3 + picAddress4 + picAddress5);
//                                values.put("pic_address_true", picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress6);
//                                values.put("service_id", commonMsg.getMsgID());
//
//                                if (dbTeam != null) {
//                                    dbTeam.insert("TeamRecord", null, values);
//                                    dbTeam.close();
//                                } else {
//                                    Log.w("Pocdemo", "dbteam not opened");
//                                }
//                                db.delete("Msg", "group_id = ?", new String[]{groupId + ""});
//                            }
//
//
//                            ContentValues values = new ContentValues();
//                            values.put("new_msg", new_msg);
//                            values.put("year", year);
//                            values.put("month", month);
//                            values.put("date", date);
//                            values.put("hour", hour);
//                            values.put("minute", minute);
//                            values.put("second", second);
//                            values.put("msg_from", group);
//                            values.put("msg_type", msgType);
//                            values.put("group_id", groupId);
//                            values.put("phone", phone);
//                            values.put("msg", msg);
//                            values.put("top", top);
//                            db.insert("Msg", null, values);
//
//
//                            i.putExtra("group", groupId);
//                            i.putExtra("phone", phone);
//                            i.putExtra("msg_type", msgType);
//                            context.sendBroadcast(i);
//
//                            notificationIntent = new Intent(MainActivity.MSG_NOTIFY);
//                            notificationIntent.putExtra("data", 0);
//                            notificationIntent.putExtra("myphone", myphone);
//                            if (group == 0) {
//                                try {
//                                    DBManagerFriendsList dbM = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
//                                    titleMsg = dbM.getFriendName(phone);
//                                    dbM.closeDB();
//                                } catch (Exception e) {
//                                    titleMsg = phone + "";
//                                }
//                                notificationIntent.putExtra("linkmanName", titleMsg);
//                                notificationIntent.putExtra("linkmanPhone", phone);
//                            } else {
//                                TeamInfo team = null;
//                                int role = 0;
//                                try {
//                                    DBManagerTeamList dbT = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
//                                    team = dbT.getTeamInfo(groupId);
//                                    titleMsg = team.getTeamName();
//                                    dbT.closeDB();
//                                    role = team.getMemberRole();
//                                } catch (Exception e) {
//                                    titleMsg = groupId + "";
//                                }
//                                notificationIntent.putExtra("group", groupId);
//                                notificationIntent.putExtra("group_name", titleMsg);
//                                notificationIntent.putExtra("type", role);
//                            }
//                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 4, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                            manager.cancel(0);//取消上次的通知消息
//                            notification = new Notification.Builder(context)
//                                    .setSmallIcon(R.mipmap.ic_launcher)//必须要先setSmallIcon，否则会显示默认的通知，不显示自定义通知
//                                    .setTicker(titleMsg)
//                                    .setContentTitle(titleMsg)
//                                    .setContentText(msg)
//                                    .setContentIntent(pendingIntent)
//                                    .setWhen(System.currentTimeMillis())
//                                    .setAutoCancel(true)
//                                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
//                                    .build();
//                            SharedPreferencesUtils.put(context, ReceiverProcesser.KEY, titleMsg);
//                            builder.addMsgIDs(commonMsg.getMsgID());
                        }
                        if (notification != null) {
                            /*NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                            nm.cancel(0);//消除对应ID的通知*/
                            manager.notify(0, notification);
                        }
                        MyService.start(context, ProtoMessage.Cmd.cmdMsgConfirm_VALUE, builder.build());
                    } finally {
                        try {
                            db.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.i("chat", "错误码: " + resp.getErrorCode());
                    new ResponseErrorProcesser(context, resp.getErrorCode());
                }

            } catch (Exception e) {
                e.printStackTrace();
                new ResponseErrorProcesser(context, ProtoMessage.ErrorCode.UNKNOWN_VALUE);
            }


            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }

}
