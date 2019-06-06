package com.luobin.voice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.Utils;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.notify.NotifyManager;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.ui.MainActivity;

import java.util.Iterator;
import java.util.Map;

import me.lake.librestreaming.client.RESClient;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import static com.android.internal.app.IntentForwarderActivity.TAG;

public class MyCommonReceiver extends BroadcastReceiver {
    public MyCommonReceiver() {
    }
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        mContext = context;
        Log.v("MyCommonReceiver","receive Intent action:" + intent.getAction());
        if (intent.getAction().equals("com.luobin.dvr.action.ACTION_SWITCH_CAM")) {
            DvrService.start(context, RESClient.ACTION_SWITCH_RTMP, null);
        } else if(intent.getAction().equals(RESClient.ACTION_ONCLICK_LEFT_TOP)){
            if(GlobalStatus.getChatRoomMsg() != null) {
                if(Utils.getFirstActivity(context)){
                    return;
                }
                Intent firstIntent = new Intent(MyApplication.getContext(), FirstActivity.class);
                intent.putExtra("callType", 0);
                if (GlobalStatus.getChatTeamId() > 0) {
                    firstIntent.putExtra("data", 1);
                    firstIntent.putExtra("group", GlobalStatus.getChatTeamId());
                    DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                    TeamInfo t = db.getTeamInfo(GlobalStatus.getChatTeamId());
                    if (t != null) {
                        firstIntent.putExtra("type", t.getMemberRole());
                        firstIntent.putExtra("group_name", t.getTeamName());
                    }
                    db.closeDB();
                } else if (!TextUtils.isEmpty(GlobalStatus.getSingleLinkManPhone())) {
                    firstIntent.putExtra("data", 1);
                    DBManagerFriendsList db = new DBManagerFriendsList(context, true, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                    AppliedFriends linkman = db.getFriend(GlobalStatus.getSingleLinkManPhone());
                    db.closeDB();
                    String linkmanName = GlobalStatus.getSingleLinkManPhone();
                    if(linkman != null){
                        linkmanName = linkman.getNickName();
                        if(TextUtils.isEmpty(linkmanName)){
                            linkmanName = linkman.getUserName();
                        }
                    }
                    firstIntent.putExtra("linkmanName", linkmanName);
                    firstIntent.putExtra("linkmanPhone", GlobalStatus.getSingleLinkManPhone());
                }

                firstIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                firstIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApplication.getContext().startActivity(firstIntent);
            }
        } else if(intent.getAction().equals(NotifyManager.ACTION_REQUEST_TIMEOUT)){
            Map<String,Long> viewPhones = GlobalStatus.getViewRoadPhones();
            Iterator<Map.Entry<String, Long>> it = viewPhones.entrySet().iterator();
            long minTime = Long.MAX_VALUE;
            String phone = intent.getStringExtra("phone");
            try {
                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    long time = entry.getValue();
                    if (time <= SystemClock.elapsedRealtime()) {
                        String temp = entry.getKey();
                        if (GlobalStatus.getCurViewPhone() == null || !GlobalStatus.getCurViewPhone().equals(temp)) {
                            GlobalStatus.removeViewRoadPhone(temp);
                        }
                    } else if (time < minTime) {
                        minTime = time;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            if(minTime != Long.MAX_VALUE) {
                sendAlarm(phone, minTime);
            }
        } else if(intent.getAction().equals(RESClient.ACTION_ONCLICK_DVR)){
            if(GlobalStatus.getChatRoomMsg() != null && !Utils.getDvrTopActivity(context)) {
                if(Utils.getFirstActivity(context)){
                    return;
                }
                Intent firstIntent = new Intent(MyApplication.getContext(), FirstActivity.class);
                intent.putExtra("callType", 0);
                if (GlobalStatus.getChatTeamId() > 0) {
                    firstIntent.putExtra("data", 1);
                    firstIntent.putExtra("group", GlobalStatus.getChatTeamId());
                    DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                    TeamInfo t = db.getTeamInfo(GlobalStatus.getChatTeamId());
                    db.closeDB();
                    firstIntent.putExtra("type", t.getMemberRole());
                    firstIntent.putExtra("group_name", t.getTeamName());
                } else if (!TextUtils.isEmpty(GlobalStatus.getSingleLinkManPhone())) {
                    firstIntent.putExtra("data", 1);
                    DBManagerFriendsList db = new DBManagerFriendsList(context, true, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                    AppliedFriends linkman = db.getFriend(GlobalStatus.getSingleLinkManPhone());
                    db.closeDB();
                    String linkmanName = GlobalStatus.getSingleLinkManPhone();
                    if(linkman != null){
                        linkmanName = linkman.getNickName();
                        if(TextUtils.isEmpty(linkmanName)){
                            linkmanName = linkman.getUserName();
                        }
                    }
                    firstIntent.putExtra("linkmanName", linkmanName);
                    firstIntent.putExtra("linkmanPhone", GlobalStatus.getSingleLinkManPhone());
                }

                firstIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                firstIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApplication.getContext().startActivity(firstIntent);
            } else {
                if (0 == Settings.System.getInt(MyApplication.getContext().getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0)) {
                    Intent newIntent = new Intent();
                    newIntent.setClassName("com.luobin.dvr", "com.luobin.dvr.ui.MainActivity");
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getContext().startActivity(newIntent);
                } else if (1 == Settings.System.getInt(MyApplication.getContext().getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0)) {
                    Settings.System.putInt(MyApplication.getContext().getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0);
                }
            }
        } else if ("com.benshikj.ht.action.RESUME".equals(intent.getAction())) {
            Log.d(TAG, "com.benshikj.ht.action.RESUME--RadioChat--HungupClick");
            HungupClick();
        }
    }

    public void HungupClick() {
        ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
        if (GlobalStatus.isStartRooming() && GlobalStatus.getChatRoomtempId() != 0) {
            builder.setRoomID(GlobalStatus.getChatRoomtempId());
        } else {
            builder.setRoomID(GlobalStatus.getRoomID());
        }
        if (builder.getRoomID() == -1) {
            GlobalStatus.setChatRoomtempId(-1);
        } else {
            GlobalStatus.setChatRoomtempId(0);
        }
        builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
        MyService.start(mContext, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
        VoiceHandler.doVoiceAction(mContext, false);
        GlobalStatus.setOldChat(0, "", 0);
        GlobalStatus.clearChatRoomMsg();
    }

    /**
     * 发送超时拒绝的定时广播
     * 08-22 10:12:10.993 8488-8488/? V/wsDvr: curTime:693794,minTime:723767
     */
    private void sendAlarm(String phone,long minTime) {
        //启动Alarm 发送心跳包
        Context context = MyApplication.getContext();
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(NotifyManager.ACTION_REQUEST_TIMEOUT);
        intent.putExtra("phone", phone);
        PendingIntent pi = PendingIntent.getBroadcast(context, NotifyManager.VIDEO_CANCEL_REQUEST_CODE, intent, 0);
        // 先取消，再开始
        alarm.cancel(pi);

        Log.v("wsDvr","curTime:" + SystemClock.elapsedRealtime() + ",minTime:" + minTime);
        alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, minTime, pi);
    }
}
