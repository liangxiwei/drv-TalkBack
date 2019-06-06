package com.example.jrd48.chat.group;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.jrd48.GlobalNotice;
import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.receiver.NotifyFriendBroadcast;
import com.example.jrd48.service.protocol.root.AcceptFriendProcesser;
import com.example.jrd48.service.protocol.root.AppliedListProcesser;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SQLite.LinkmanRecordHelper;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.SQLite.TeamRecordHelper;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.GetFriendInfoProcesser;

import java.io.File;

import static com.example.jrd48.service.protocol.root.ReceiverProcesser.getMyDataRoot;

/**
 * Created by Administrator on 2017/1/22.
 */

public class MsgTool {
    public static final String NOT_SHOW = "notshow";
    public static String getTeamName(Context context, long teamID) {
        try {
            DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
            String teamName = db.getTeamName(teamID);
            db.closeDB();
            return teamName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFriendName(Context context, String phone) {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(context, true, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
            String name = db.getFriendName(phone);
            db.closeDB();
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void deleteTeamMsg(Context context, long teamID) {
        if (GlobalStatus.equalTeamID(teamID)) {
            NotificationManager nm = (NotificationManager) (context.getSystemService(context.NOTIFICATION_SERVICE));
            nm.cancel(-1);//消除对应ID的通知
        }
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myphone = preferences.getString("phone", "");
        TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamID + "TeamMsgShow.dp", null);
        SQLiteDatabase dbTeam = teamRecordHelper.getWritableDatabase();
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myphone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        db.delete("Msg", "group_id = ?", new String[]{teamID + ""});
        dbTeam.delete("TeamRecord", null, null);
        db.close();
        dbTeam.close();
        DBManagerTeamList dblist = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
        dblist.deleteTeam(teamID);
        dblist.closeDB();

        String rootAddress = getMyDataRoot(context) + "/" + myphone + "/" + teamID;
        deleteDirectory(rootAddress);
        File file = new File(rootAddress);
        if (file.exists()) {
            Log.i("jrdchat", "删除好像失败了");
        } else {
            Log.i("jrdchat", "删除成功了");
        }
    }

    public static void deleteTeam_OnlyMsg(Context context, long teamID) {
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myphone = preferences.getString("phone", "");
        TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamID + "TeamMsgShow.dp", null);
        SQLiteDatabase dbTeam = teamRecordHelper.getWritableDatabase();
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myphone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        db.delete("Msg", "group_id = ?", new String[]{teamID + ""});
        dbTeam.delete("TeamRecord", null, null);
        db.close();
        dbTeam.close();

        String rootAddress = getMyDataRoot(context) + "/" + myphone + "/" + teamID;
        deleteDirectory(rootAddress);
        File file = new File(rootAddress);
        if (file.exists()) {
            Log.i("jrdchat", "删除好像失败了");
        } else {
            Log.i("jrdchat", "删除成功了");
        }
    }

    public static void deleteFriendsMsg(Context context, String phone) {
        if (GlobalStatus.equalPhone(phone)) {
            NotificationManager nm = (NotificationManager) (context.getSystemService(context.NOTIFICATION_SERVICE));
            nm.cancel(-1);//消除对应ID的通知
        }
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myphone = preferences.getString("phone", "");
        LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context, myphone + phone + "LinkmanMsgShow.dp", null);
        SQLiteDatabase dbMan = linkmanRecordHelper.getWritableDatabase();
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myphone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        db.delete("Msg", "phone = ? and msg_from = ?", new String[]{phone, 0 + ""});
        dbMan.delete("LinkmanRecord", null, null);
        db.close();
        dbMan.close();
        DBManagerFriendsList dblist = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
        dblist.deleteFriend(phone);
        dblist.closeDB();

        String rootAddress = getMyDataRoot(context) + "/" + myphone + "/" + 0 + "/" + phone;
        deleteDirectory(rootAddress);
        File file = new File(rootAddress);
        if (file.exists()) {
            Log.i("jrdchat", "删除好像失败了");
        } else {
            Log.i("jrdchat", "删除成功了");
        }
    }

    public static void deleteFriends_OnlyMsg(Context context, String phone) {
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myphone = preferences.getString("phone", "");
        LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context, myphone + phone + "LinkmanMsgShow.dp", null);
        SQLiteDatabase dbMan = linkmanRecordHelper.getWritableDatabase();
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myphone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        db.delete("Msg", "phone = ? and msg_from = ?", new String[]{phone, 0 + ""});
        dbMan.delete("LinkmanRecord", null, null);
        db.close();
        dbMan.close();

        String rootAddress = getMyDataRoot(context) + "/" + myphone + "/" + 0 + "/" + phone;
        deleteDirectory(rootAddress);
        File file = new File(rootAddress);
        if (file.exists()) {
            Log.i("jrdchat", "删除好像失败了");
        } else {
            Log.i("jrdchat", "删除成功了");
        }
    }

    private static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                files[i].delete();
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

//    public static void LoadTeamList(final Context context) {
//        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
//        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(GroupsListProcesser.ACTION);
//        new TimeoutBroadcast(context, filter).startReceiver(20, new ITimeoutBroadcast() {
//            @Override
//            public void onTimeout() {
//                Log.i("LoadTeamList", "连接超时");
//            }
//
//            @Override
//            public void onGot(Intent i) {
//
//                if (i.getIntExtra("error_code", -1) ==
//                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    TeamInfoList list = i.getParcelableExtra("get_group_list");
//
//                    Log.i("LoadTeamList", "获取群组成功");
//                } else {
//                    Log.i("LoadTeamList", "获取群组失败" + i.getIntExtra("error_code", -1));
//                }
//            }
//        });
//    }

    public void getAcceptInfo(final String phone, final Context context) {

        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setPhoneNum(phone);
        MyService.start(context, ProtoMessage.Cmd.cmdGetFriendInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((MyService) context).mBroadcastManger).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
            }

            @Override
            public void onGot(Intent i) {
                GlobalImg.reloadImg(context, phone);
                Intent intent = new Intent(MainActivity.FRIEND_ACTION);
                intent.putExtra("phone", phone);
                context.sendBroadcast(intent);
                if (GlobalNotice.isSameNumber(phone)){
                    Log.d("refuse","refuse success ...");
                  //  refuseAddFriend(phone,context);
                }
            }
        });
    }

    /*
 * 拒绝加好友申请请求
 * */
    public void refuseAddFriend(final String phone, final Context context) {

        ProtoMessage.AcceptFriend.Builder builder = ProtoMessage.AcceptFriend.newBuilder();
        builder.setFriendPhoneNum(phone);
        builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
        MyService.start(context, ProtoMessage.Cmd.cmdAcceptFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AcceptFriendProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((MyService) context).mBroadcastManger).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.d("refuse","refuse 连接超时 ...");
            }
            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.d("refuse","refuse success ...");
                    sendBroadcast(phone,context);
                } else {
                    if (i.getIntExtra("error_code", -1) == ProtoMessage.ErrorCode.FRIEND_NOT_INVITIED_VALUE){
                        GlobalNotice.clearAppliedFriends(phone);
                        sendBroadcast(phone,context);
                    }
                    Log.e("refuse","error_code:"+i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void sendBroadcast(String phone,Context context){
        GlobalNotice.clearAppliedFriends(phone);
        if (GlobalNotice.isHasNumber()) {
            new NotifyFriendBroadcast(context).sendBroadcast("");
        } else {
            new NotifyFriendBroadcast(context).sendBroadcast(NOT_SHOW);
        }
    }

    /**
     * 更新单个群成员消息
     */
    public static synchronized void updateMemberMsg(Context mContext, TeamMemberInfo teamMemberInfo, ProtoMessage.UserInfo userInfo, long teamID) {
        try {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamID + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_phone", teamMemberInfo.getUserPhone());
            values.put("user_name", teamMemberInfo.getUserName());
            values.put("nick_name", teamMemberInfo.getNickName());
            values.put("role", teamMemberInfo.getRole());
            values.put("member_priority", teamMemberInfo.getMemberPriority());
            values.put("userSex", userInfo.getUserSex());
            values.put(DBHelperFriendsList.CAR_ID, userInfo.getCarID());
            values.put(DBHelperFriendsList.CITY, userInfo.getCity());
            values.put(DBHelperFriendsList.PROV, userInfo.getProv());
            values.put(DBHelperFriendsList.TOWN, userInfo.getTown());
            values.put(DBHelperFriendsList.BIRTHDAY, userInfo.getBirthday());
            values.put(DBHelperFriendsList.CAR_NUM, userInfo.getCarNum());
            values.put(DBHelperFriendsList.CAR_BAND, userInfo.getCarType1());
            values.put(DBHelperFriendsList.CAR_TYPE2, userInfo.getCarType2());
            values.put(DBHelperFriendsList.CAR_TYPE3, userInfo.getCarType3());
            db.update("LinkmanMember", values, "user_phone == ?", new String[]{teamMemberInfo.getUserPhone()});
            db.close();
            Log.i("msgtool","member msg update success ...");
        }catch (Exception e){
            e.printStackTrace();
            Log.e("msgtool",e.getMessage());
        }
    }

    public void refreshFriendsMenu(Context mContext) {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(mContext, ProtoMessage.Cmd.cmdAppliedList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedListProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, ((MyService) mContext).mBroadcastManger).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                Log.i("downFriendsApplied", "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("downFriendsApplied", "获取最新好友申请");
                } else {
                    Log.e("downFriendsApplied", "获取失败" + i.getIntExtra("error_code", -1));
                }
            }
        });
    }
}
