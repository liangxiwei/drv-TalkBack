package com.example.jrd48.service.protocol.root;

import android.app.NotificationManager;
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

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.MyFileUtil;
import com.example.jrd48.chat.SQLite.LinkmanRecordHelper;
import com.example.jrd48.chat.SQLite.TeamRecordHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyLogger;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.notify.NotifyManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CancelNotify;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.luobin.voice.VoiceHandler;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class ReceiverProcesser extends CommonProcesser {
    public final static String KEY = "notify";
    public final static String UPDATE_KEY = "msg_number_update";
    public final static String ACTION = "ACTION.ReceiverProcesser";
    public final static String PHONE_NUMBER = "phone_number";
    private Handler mHandler = null;

    public ReceiverProcesser(Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static String getMyDataRoot(Context context) {
        return MyFileUtil.getMemoryPath(context) + "/luobingchat";
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

//    private long createNewSn(SQLiteDatabase db, String tableName) {
//        //"select seq from sqlite_sequence where name = ?"
//        long seq = 0;
//        Cursor cursor = db.rawQuery("select seq from sqlite_sequence where name = ?", new String[]{tableName});
//        if (cursor.moveToNext()) {
//            seq = cursor.getInt(0); //获取第一列的值,第一列的索引从0开始
//        } else {
//            Log.i("jrdchat", "查找DB SN失败，打开数据库失败");
//            //throw new RuntimeException("createNewSn failed");
//        }
//        cursor.close();
//        seq++;
//        Log.i("jrdchat", "获取新的SN：" + seq);
//
//        return seq;
//    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonMsg resp = ProtoMessage.CommonMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Intent i = new Intent(ACTION);

            try {
                if (resp == null) {
                    throw new Exception("unknown response.");
                }
                i.putExtra("error_code", resp.getErrorCode());
                if (resp.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("jim", "获得正确应答:" + resp.toString());
                    // TODO: 这里处理添加 其他正确的数据
                    // i.putExtra(other useful value);

                    int msgType;
                    int group;
                    long groupId = resp.getToTeamID();
                    final String phone = resp.getFromUserPhone();
                    String msg;
                    String titleMsg;
                    int new_msg;
                    int top = 0;
                    NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    String myPhone = preferences.getString("phone", "");
                    LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context, myPhone + phone + "LinkmanMsgShow.dp", null);
                    SQLiteDatabase dbMan = null;
                    TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myPhone + groupId + "TeamMsgShow.dp", null);
                    SQLiteDatabase dbTeam = null;

                    String picAddress1;
                    String picAddress2;
                    String picAddress3;
                    String picAddress4;
                    String picAddress5;
                    String picAddress6;
//                    long l;
                    if (groupId == 0) {
                        dbMan = linkmanRecordHelper.getWritableDatabase();
//                        Cursor cursor = dbMan.query("LinkmanRecord", null, null, null, null, null, null);
//                        if (cursor.moveToLast()) {
//                            // l = cursor.getLong(cursor.getColumnIndex("id")) + 1;
//                            l = createNewSn(context, myPhone + phone + "LinkmanMsgShow.dp", "LinkmanRecord");
//                        } else {
//                            l = 0;
//                        }
//                        l = createNewSn(dbMan, "LinkmanRecord");
                    } else {
                        dbTeam = teamRecordHelper.getWritableDatabase();
//                        Cursor cursor = dbTeam.query("TeamRecord", null, null, null, null, null, null);
//                        if (cursor.moveToLast()) {
//                            //l = cursor.getLong(cursor.getColumnIndex("id")) + 1;
//                            l = createNewSn(context, myPhone + groupId + "TeamMsgShow.dp", "TeamRecord");
//                        } else {
//                            l = 0;
//                        }

//                        l = createNewSn(dbTeam, "TeamRecord");
                    }

//                    MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myPhone + "MsgShow.dp", null);
//                    SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
//                    db
                    picAddress1 = getMyDataRoot(context);
                    picAddress2 = "/" + myPhone;
                    picAddress3 = "/" + groupId;
                    picAddress4 = "/" + phone;
//                    picAddress5 = "/" + l + ".jpg";
//                    picAddress6 = "/" + l + "_true" + ".jpg";
//                    Log.i("chat", "获得正确应答 Type:" + resp.getMsgType() + ", sn: " + l);
//                    if (resp.getMsgType() == ProtoMessage.MsgType.mtText.getNumber()) {
//                        msgType = 0;
//                        msg = resp.getMsgContent().toStringUtf8();
//                        i.putExtra("text", msg);
//
//                    } else
//                    if (resp.getMsgType() == ProtoMessage.MsgType.mtImage_VALUE
//                            || resp.getMsgType() == ProtoMessage.MsgType.mtVideoFile_VALUE) {
//                        msgType = resp.getMsgType();
//                        if (msgType == ProtoMessage.MsgType.mtImage_VALUE) {
//                            msg = "[图片]";
//                        } else {
//                            msg = "[视频]";
//                            picAddress6 = "/" + l + "_true" + ".mp4";
//                        }
//
//                        byte[] bitmapByte = resp.getMsgContent().toByteArray();
//                        try {
//
//                            File outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4);
//                            if (!outputImage.exists()) {
//                                outputImage.mkdirs();
//                            }
//                            File tempFile = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress6);
//                            if (tempFile.exists()) {
//                                tempFile.delete();
//                            }
//                            outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress5);
//                            if (!outputImage.exists()) {
//                                outputImage.createNewFile();
//                            }
//                            FileOutputStream imageOutput = new FileOutputStream(outputImage);//打开输入流
//                            imageOutput.write(bitmapByte, 0, bitmapByte.length);//将byte写入硬盘
//                            imageOutput.close();
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    } else if (resp.getMsgType() == ProtoMessage.MsgType.mtCancel_VALUE) {
//                        ContentValues values = new ContentValues();
//                        values.put("msg_type", ProtoMessage.MsgType.mtCancel_VALUE);
//                        ContentValues values_msg = new ContentValues();
//                        values_msg.put("msg", "撤回了一条消息");
//                        String origServiceID = resp.getMsgContent().toStringUtf8();
//                        Cursor cursor;
//                        if (groupId == 0) {
//                            dbMan.update("LinkmanRecord", values, "service_id = ?", new String[]{origServiceID});
//                            db.update("Msg", values_msg, "phone = ? and msg_from = ?", new String[]{phone, 0 + ""});
//                            cursor = dbMan.query("LinkmanRecord", null, "service_id = ?", new String[]{origServiceID}, null, null, null);
//                            FirstActivity.deletePic(context, cursor);
//                            cursor.close();
//                            dbMan.close();
//                        } else {
//                            dbTeam.update("TeamRecord", values, "service_id = ?", new String[]{origServiceID});
//                            db.update("Msg", values_msg, "group_id = ?", new String[]{groupId + ""});
//                            cursor = dbTeam.query("TeamRecord", null, "service_id = ?", new String[]{origServiceID}, null, null, null);
//                            FirstActivity.deletePic(context, cursor);
//                            cursor.close();
//                            dbTeam.close();
//                        }
//
//                        i.putExtra("group", groupId);
//                        i.putExtra("phone", phone);
//                        i.putExtra("msg_type", 3);
//                        context.sendBroadcast(i);
//                        confirm(resp.getMsgID());
//                        db.close();
//                        return;
//                    } else
                    if (resp.getMsgType() == ProtoMessage.MsgType.mtEnterTeam_VALUE
                            || resp.getMsgType() == ProtoMessage.MsgType.mtLeaveTeam_VALUE) {
                        long groupIdTemp = groupId;
                        long joinTeamId = -1;
                        String teamName = "";
                        TeamInfo teamInfo = null ;
                        if (groupId == 0 || (resp.getMsgContent().toStringUtf8().equals(myPhone) && resp.getMsgType() == ProtoMessage.MsgType.mtLeaveTeam_VALUE)) {
                            try {
                                if (groupId == 0) {
                                    groupIdTemp = Long.valueOf(resp.getMsgContent().toStringUtf8());
                                } else {
                                    groupIdTemp = groupId;
                                }
                                DBManagerTeamList dblist = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                                teamName = dblist.getTeamName(groupIdTemp);
                                dblist.closeDB();
                                if (!TextUtils.isEmpty(teamName) && resp.getMsgType() == ProtoMessage.MsgType.mtLeaveTeam_VALUE ) {
                                    Log.d("chat", "您退出了[" + teamName + "]群");
                                    ToastR.setToast(context, "您退出了[" + teamName + "]群");
                                }
                                new CancelNotify(context, groupIdTemp, manager, phone);
                                MsgTool.deleteTeamMsg(context, groupIdTemp);
                                if (GlobalStatus.equalTeamID(groupIdTemp)) {
                                    VoiceHandler.doVoiceAction(context, false);
                                    Intent intent = new Intent(AutoCloseProcesser.ACTION);
                                    long roomId = GlobalStatus.getRoomID();
                                    GlobalStatus.equalRoomID(roomId);
                                    intent.putExtra("error_code", resp.getErrorCode());
                                    intent.putExtra("roomID", roomId);
                                    context.sendBroadcast(intent);
                                }

                                Intent in = new Intent("ACTION.refreshTeamList");
                                in.putExtra("singout", FirstActivity.SING_OUT);
                                in.putExtra("teamid", groupIdTemp);
                                context.sendBroadcast(in);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (dbMan != null) {
                                dbMan.close();
                            }
                            if (dbTeam != null) {
                                dbTeam.close();
                            }
                        } else {
                            if (GlobalStatus.equalTeamID(groupIdTemp)) {
                                ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
                                List<ProtoMessage.ChatRoomMemberMsg> memberMsgs = chatRoomMsg.getMembersList();
                                int selectPosition = -1;
                                ProtoMessage.ChatRoomMsg.Builder builder = chatRoomMsg.toBuilder();
                                boolean isChange = false;
                                if (resp.getMsgType() == ProtoMessage.MsgType.mtEnterTeam_VALUE) {
                                    ProtoMessage.ChatRoomMemberMsg selectMember = null;
                                    for (int j = 0; j < memberMsgs.size(); j++) {
                                        ProtoMessage.ChatRoomMemberMsg memberMsg = memberMsgs.get(j);
                                        if (phone.equals(memberMsg.getPhoneNum())) {
                                            selectPosition = j;
                                        }
                                    }
                                    if (selectPosition == -1) {
                                        ProtoMessage.ChatRoomMemberMsg.Builder newBuilder = ProtoMessage.ChatRoomMemberMsg.newBuilder();
                                        newBuilder.setPhoneNum(phone);
                                        builder.addMembers(newBuilder.build());
                                        isChange = true;
                                    }
                                } else {
                                    ProtoMessage.ChatRoomMemberMsg selectMember = null;
                                    for (int j = 0; j < memberMsgs.size(); j++) {
                                        ProtoMessage.ChatRoomMemberMsg memberMsg = memberMsgs.get(j);
                                        if (phone.equals(memberMsg.getPhoneNum())) {
                                            selectPosition = j;
                                        }
                                    }
                                    if (selectPosition != -1) {
                                        builder.removeMembers(selectPosition);
                                        isChange = true;
                                    }
                                }

                                if (builder.getMembersCount() > 1) {
                                    if (isChange) {
                                        GlobalStatus.updateChatRoomMsg(builder.build());
                                    }
                                } else {
                                    ProtoMessage.AcceptVoice.Builder closeBuild = ProtoMessage.AcceptVoice.newBuilder();
                                    if (GlobalStatus.isStartRooming() && GlobalStatus.getChatRoomtempId() != 0) {
                                        closeBuild.setRoomID(GlobalStatus.getChatRoomtempId());
                                    } else {
                                        closeBuild.setRoomID(GlobalStatus.getRoomID());
                                    }
                                    if (builder.getRoomID() == -1) {
                                        GlobalStatus.setChatRoomtempId(-1);
                                    } else {
                                        GlobalStatus.setChatRoomtempId(0);
                                    }
                                    Log.v("wsDvr", "roomId:" + closeBuild.getRoomID());
                                    closeBuild.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
                                    MyService.start(context, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), closeBuild.build());

                                    VoiceHandler.doVoiceAction(context, false);
                                    Intent intent = new Intent(AutoCloseProcesser.ACTION);
                                    long roomId = GlobalStatus.getRoomID();
                                    GlobalStatus.equalRoomID(roomId);
                                    intent.putExtra("error_code", resp.getErrorCode());
                                    intent.putExtra("roomID", roomId);
                                    context.sendBroadcast(intent);
                                    ToastR.setToast(context, "当前成员只剩1个，对讲解散");
                                }
                            }
                            i.putExtra("group", groupIdTemp);
                            i.putExtra("phone", phone);
                            i.putExtra("msg_type", 4);
                            context.sendBroadcast(i);
                            mHandler.removeCallbacks(mRefreshTeamRunnable);
                            mHandler.postDelayed(mRefreshTeamRunnable, 2000);

//                            getGroupMan(groupIdTemp, i);
                            dbTeam.close();
                        }

                        if (resp.getMsgContent().toStringUtf8().equals(myPhone)) {
                            Log.d("chat", "equals myphone");
                            if (resp.getMsgType() == ProtoMessage.MsgType.mtEnterTeam_VALUE) {
                                Log.d("chat", "mtEnterTeam_VALUE");
                                DBManagerTeamList dblist = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                                teamName = dblist.getTeamName(groupIdTemp);
                                dblist.closeDB();
                                if (!TextUtils.isEmpty(teamName)) {
                                    Log.d("chat", "您加入了[" + teamName + "]群");
                                    ToastR.setToast(context, "您加入了[" + teamName + "]群");
                                }else{
                                    joinTeamId = groupIdTemp;
                                }
                            }
                            loadTeamListFromNet(joinTeamId);
                        }
                        confirm(resp.getMsgID());
                        return;
                    } else if (resp.getMsgType() == ProtoMessage.MsgType.mtChangeHeadImage_VALUE) {
                        final String changePhone = resp.getMsgContent().toStringUtf8();
//                        Log.d("jim","刷新头像"+ProtoMessage.MsgType.mtChangeHeadImage_VALUE);
//                        Log.d("jim","changePhone："+changePhone + "  phone:"+phone);
                        if (phone.equals(myPhone)) {
                            ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
                            MyService.start(context, ProtoMessage.Cmd.cmdGetMyInfo.getNumber(), builder.build());
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(UserInfoProcesser.ACTION);
                            final TimeoutBroadcast b = new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager());
                            b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                                @Override
                                public void onTimeout() {

                                }
                                @Override
                                public void onGot(Intent i) {
                                    SharedPreferences preference = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                                    /*int teamRandom = preference.getInt("team_random", 0);
                                    int teamRandomVideo = preference.getInt("team_random_video", 0);
                                    Log.d("chat","teamRandom="+teamRandom+",teamRandomVideo="+teamRandomVideo);
                                    if (teamRandom == 1 || teamRandomVideo == 1) {
                                        GlobalStatus.setIsRandomChatVideo(false);
                                    }else{
                                        GlobalStatus.setIsRandomChatVideo(true);
                                    }
                                    if(GlobalStatus.IsRandomChat()){
                                        Intent ii = new Intent(RESClient.ACTION_VIDEO_VOIDE_UPDATE);
                                        context.sendBroadcast(ii);
                                    }*/
                                    GlobalImg.reloadImg(context, changePhone);
                                    Intent in = new Intent("ACTION.changeImage");
                                    in.putExtra(PHONE_NUMBER, changePhone);
                                    context.sendBroadcast(in);
                                }
                            });
                        } else {
                            ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
                            builder.setPhoneNum(changePhone);
                            builder.setOnlyPhoneNum(true);
                            MyService.start(context, ProtoMessage.Cmd.cmdSearchUser.getNumber(), builder.build());
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(SearchFriendProcesser.ACTION);
                            new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                                @Override
                                public void onTimeout() {
                                }

                                @Override
                                public void onGot(Intent i) {
                                    GlobalImg.reloadImg(context, changePhone);
                                    Intent in = new Intent("ACTION.changeImage");
                                    in.putExtra(PHONE_NUMBER, changePhone);
                                    context.sendBroadcast(in);
                                }
                            });
                        }
                        if (groupId == 0) {
                            dbMan.close();
                        } else {
                            dbTeam.close();
                        }
                        confirm(resp.getMsgID());
                        return;
                    } else if (resp.getMsgType() == ProtoMessage.MsgType.mtAcceptFriend_VALUE) {
                        final String acceptPhone = resp.getMsgContent().toStringUtf8();
                        new MsgTool().getAcceptInfo(acceptPhone, context);
                        if (!myPhone.equals(phone)) {
                            ToastR.setToast(context, "电话号码为：" + phone + " 的用户，接受了您的好友请求");
                        }
                        if (groupId == 0) {
                            dbMan.close();
                        } else {
                            dbTeam.close();
                        }
                        confirm(resp.getMsgID());
                        return;
                    } else if (resp.getMsgType() == ProtoMessage.MsgType.mtDeleteFriend_VALUE) {
                        final String deletePhone = resp.getMsgContent().toStringUtf8();
                        new CancelNotify(context, groupId, manager, deletePhone);
                        Log.v("wsDvr", "delete:" + deletePhone);
                        MsgTool.deleteFriendsMsg(context, deletePhone);
                        Intent intent = new Intent(MainActivity.FRIEND_ACTION);
                        intent.putExtra("phone", deletePhone);
                        intent.putExtra("delete", MainActivity.deleteStr);
                        context.sendBroadcast(intent);
                        if (deletePhone.equals(GlobalStatus.getSingleLinkManPhone())) {
                            NotifyManager.endCall(context);
                        }

                        if (!myPhone.equals(phone)) {
                            ToastR.setToast(context, "电话号码为：" + phone + " 的用户和您解除好友关系");
                        }
                        if (groupId == 0) {
                            dbMan.close();
                        } else {
                            dbTeam.close();
                        }
                        confirm(resp.getMsgID());
                        return;
                    } else {
                        if (groupId == 0) {
                            dbMan.close();
                        } else {
                            dbTeam.close();
                        }
                        confirm(resp.getMsgID());
                        return;
                    }
//                    Cursor cursor;
//                    String teamName = "";
//                    if(groupId == 0) {
//                        cursor = db.query("Msg", null, "phone=? and group_id=?", new String[]{phone, groupId + ""}, null, null, null);
//                    }else {
//                        cursor = db.query("Msg", null, "group_id=?", new String[]{groupId + ""}, null, null, null);
//
//                        DBManagerTeamList db1 = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
//                        teamName = db1.getTeamName(groupId);
//                        db1.closeDB();
//                        if (teamName == null) {
//                            throw new RuntimeException("取组信息时出错");
//                        }
//                    }
//                    boolean msgAdd = true;
//                    String key = (String) SharedPreferencesUtils.get(context, UPDATE_KEY, "");
//                    if (key == null || key.equals("") || (!key.equals(phone) && !key.equals(teamName))) {
//                        msgAdd = true;
//                    } else {
//                        msgAdd = false;
//                    }
//                    if (cursor.moveToFirst()) {
//                        top = cursor.getInt(cursor.getColumnIndex("top"));
//                        new_msg = cursor.getInt(cursor.getColumnIndex("new_msg"));
//                        if (new_msg < 99 && new_msg >= 0 && msgAdd) {
//                            new_msg++;
//                        }
//                    } else {
//                        new_msg = 1;
//                    }
//                    cursor.close();
//
//                    Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
//                    int year = c.get(Calendar.YEAR);
//                    int month = c.get(Calendar.MONTH);
//                    int date = c.get(Calendar.DATE);
//                    int hour = c.get(Calendar.HOUR_OF_DAY);
//                    int minute = c.get(Calendar.MINUTE);
//                    int second = c.get(Calendar.SECOND);
//                    if (groupId == 0) {
//                        group = 0;
//
//                        ContentValues values = new ContentValues();
//                        values.put("service_id", resp.getMsgID());
//                        values.put("year", year);
//                        values.put("month", month);
//                        values.put("date", date);
//                        values.put("hour", hour);
//                        values.put("minute", minute);
//                        values.put("second", second);
//                        values.put("msg_send", 1);
//                        values.put("msg_type", msgType);
//                        values.put("msg", msg);
//                        values.put("pic_address", picAddress2 + picAddress3 + picAddress4 + picAddress5);
//                        values.put("pic_address_true", picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress6);
//                        dbMan.insert("LinkmanRecord", null, values);
//                        dbMan.close();
//                        db.delete("Msg", "phone = ? and msg_from = ?", new String[]{phone, group + ""});
//                    } else {
//                        group = 1;
//
//                        ContentValues values = new ContentValues();
//                        values.put("service_id", resp.getMsgID());
//                        values.put("year", year);
//                        values.put("month", month);
//                        values.put("date", date);
//                        values.put("hour", hour);
//                        values.put("minute", minute);
//                        values.put("second", second);
//                        values.put("msg_send", 1);
//                        values.put("msg_type", msgType);
//                        values.put("phone", phone);
//                        values.put("msg", msg);
//                        values.put("pic_address", picAddress2 + picAddress3 + picAddress4 + picAddress5);
//                        values.put("pic_address_true", picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress6);
//                        dbTeam.insert("TeamRecord", null, values);
//                        dbTeam.close();
//                        db.delete("Msg", "group_id = ?", new String[]{groupId + ""});
//                    }
//
//
//                    ContentValues values = new ContentValues();
//                    values.put("new_msg", new_msg);
//                    values.put("year", year);
//                    values.put("month", month);
//                    values.put("date", date);
//                    values.put("hour", hour);
//                    values.put("minute", minute);
//                    values.put("second", second);
//                    values.put("msg_from", group);
//                    values.put("msg_type", msgType);
//                    values.put("group_id", groupId);
//                    values.put("phone", phone);
//                    values.put("msg", msg);
//                    values.put("top", top);
//                    db.insert("Msg", null, values);
//                    db.close();
//                    i.putExtra("group", groupId);
//                    i.putExtra("phone", phone);
//                    i.putExtra("msg_type", msgType);
//                    context.sendBroadcast(i);
//
//                    Intent notificationIntent = new Intent(MainActivity.MSG_NOTIFY);
//                    notificationIntent.putExtra("data", 0);
//                    notificationIntent.putExtra("myPhone", myPhone);
//                    if (group == 0) {
//                        DBManagerFriendsList dbM = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
//                        titleMsg = dbM.getFriendName(phone);
//                        dbM.closeDB();
//                        notificationIntent.putExtra("linkmanName", titleMsg);
//                        notificationIntent.putExtra("linkmanPhone", phone);
//                    } else {
//                        DBManagerTeamList dbT = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
//                        TeamInfo team = dbT.getTeamInfo(groupId);
//                        titleMsg = team.getTeamName();
//                        dbT.closeDB();
//                        ;
//                        notificationIntent.putExtra("group", groupId);
//                        notificationIntent.putExtra("group_name", titleMsg);
//                        notificationIntent.putExtra("type", team.getMemberRole());
//                    }
//                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 3, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    manager.cancel(0);//取消上次的通知消息
////                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                    Notification notification;
//                    notification = new Notification.Builder(context)
//                            .setSmallIcon(R.mipmap.ic_launcher)//必须要先setSmallIcon，否则会显示默认的通知，不显示自定义通知
//                            .setTicker(titleMsg)
//                            .setContentTitle(titleMsg)
//                            .setContentText(msg)
//                            .setContentIntent(pendingIntent)
//                            .setWhen(System.currentTimeMillis())
//                            .setAutoCancel(true)
//                            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
//                            .build();
//                    NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//                    nm.cancel(0);//消除对应ID的通知
//                    manager.notify(0, notification);
//                    SharedPreferencesUtils.put(context, KEY, titleMsg);
//                    confirm(resp.getMsgID());


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

    private void confirm(long msgID) {
        ProtoMessage.CommonMsgConfirm.Builder builder = ProtoMessage.CommonMsgConfirm.newBuilder();
        builder.addMsgIDs(msgID);
        MyService.start(context, ProtoMessage.Cmd.cmdMsgConfirm_VALUE, builder.build());
    }


    public void getGroupMan(long id, final Intent intent) {
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(id);
        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TeamMemberProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                Log.e("code", "连接超时");
//                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    context.sendBroadcast(intent);
                } else {
                    Log.e("code", "error code:" + code);
                    //  new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    /**
     * 获取群组
     */
    private void loadTeamListFromNet(final long joinTeamId) {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_group_list");

                    SharedPreferencesUtils.put(context, "data_init", true);

                    Intent in = new Intent("ACTION.refreshTeamList");
                    context.sendBroadcast(in);
                    Log.d("chat", "joinTeamId="+joinTeamId);
                    if(joinTeamId >0){
                        DBManagerTeamList dblist = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                        String teamName = dblist.getTeamName(joinTeamId);
                        dblist.closeDB();
                        if (!TextUtils.isEmpty(teamName)) {
                            Log.d("chat", "您加入了[" + teamName + "]群1");
                            ToastR.setToast(context, "您加入了[" + teamName + "]群");
                        }
                        Log.d("chat", "joinTeamId end");
                    }
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }

}
