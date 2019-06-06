package com.example.jrd48.service.protocol.root;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.crash.MyLog;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.Utils;
import com.example.jrd48.chat.receiver.NotifyFriendBroadcast;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.notify.NotifyManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.log.DBMyLogHelper;
import com.luobin.log.LogCode;
import com.luobin.model.CallState;
import com.luobin.utils.VideoRoadUtils;
import com.luobin.voice.MediaPlayerTool;
import com.video.VideoCallActivity;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import me.lake.librestreaming.client.RESClient;

import static com.video.VideoCallActivity.ACTION_LIVE_CALL_ANS_DENY;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class NotifyProcesser extends CommonProcesser {
    //public final static String ACTION = "ACTION.NotifyProcesser";
    private static volatile boolean mSpeaking = false;
    public final static String Call_ACTION = "ACTION.NotifyCallProcesser";
    public static String Call_KEY = "call_key";
    public static String NUMBER = "phoneNumber";
    public static String ONLINE_KEY = "online";
    public static final String FRIEND_STATUS_ACTION = "com.example.jrd48.service.protocol.root.action.Status";
    public Handler mHandler = null;

    public NotifyProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "got 通知响应包: ");
        try {
            final ProtoMessage.NotifyMsg resp = ProtoMessage.NotifyMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            final String phone = resp.getFriendPhoneNum();
            final Long teamID = resp.getTeamID();
            SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
            String myPhone = preferences.getString("phone", "");
            if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyApplyFriend.getNumber()) {
                new NotifyFriendBroadcast(context).sendBroadcast("");
                new MsgTool().refreshFriendsMenu(context);
                if (!myPhone.equals(phone)) {
                    ToastR.setToast(context, "电话号码为：" + phone + " 的用户，请求加您为好友");
                }
            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyAcceptFriend.getNumber()) {
                if (!myPhone.equals(phone)) {
                    ToastR.setToast(context, "电话号码为：" + phone + " 的用户，接受了您的好友请求");
                }
                context.sendBroadcast(new Intent(MainActivity.FRIEND_ACTION));
            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyDeleteFriend.getNumber()) {

//                DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
//                db.deleteFriend(phone);
//                db.closeDB();
                if (!myPhone.equals(phone)) {
                    ToastR.setToast(context, "电话号码为：" + phone + " 的用户和您解除好友关系");
                }
                MsgTool.deleteFriendsMsg(context, phone);
                Intent intent = new Intent(MainActivity.FRIEND_ACTION);
                intent.putExtra("phone", phone);
                context.sendBroadcast(intent);

            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyApplyTeam.getNumber()) {

                int type = resp.getApplyType();
                if (!myPhone.equals(phone)) {
                    if (type == ProtoMessage.ApplyTeamType.attApply_VALUE) {
                        ToastR.setToast(context, "电话号码为：" + phone + " 的用户申请加入 [" + MsgTool.getTeamName(context, teamID) + "] 群组");
                    } else {
                        ToastR.setToast(context, "电话号码为：" + phone + " 的用户邀请您加入[" + resp.getTeamName() + "]群组");
                    }
                }
                new NotifyFriendBroadcast(context).sendBroadcast("");
//                new NotifyAppliedAddTeamBroadcast(context).sendBroadcast("");

            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyDismissTeam.getNumber()) {

                ToastR.setToast(context, "群主解散了[" + MsgTool.getTeamName(context, teamID) + "]群组");
                DBManagerTeamList db = new DBManagerTeamList(context, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                db.deleteTeam(teamID);
                db.closeDB();
                MsgTool.deleteTeamMsg(context, teamID);
                Intent intent = new Intent(MainActivity.TEAM_ACTION);
                intent.putExtra("teamID", teamID);
                context.sendBroadcast(intent);

            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyAcceptTeam.getNumber()) {

                ToastR.setToast(context, "电话号码为：" + phone + " 的用户接受加入或者同意加入群组");
                context.sendBroadcast(new Intent(MainActivity.TEAM_ACTION));

            }else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyFriendStatus.getNumber()) {
//                ToastR.setToast(context, "上线或者下线通知：电话号码为：" + phone + " online :"+resp.getOnline() );
                Intent x = new Intent(FRIEND_STATUS_ACTION);
                x.putExtra(NUMBER, resp.getFriendPhoneNum());
                x.putExtra(ONLINE_KEY, resp.getOnline());
                context.sendBroadcast(x);

            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyCall_VALUE) {
                Log.v("wsDvr", "resp:" + resp.toString());
                boolean isAccept = false;
                /*if(GlobalStatus.getOldTeam() == 0 && GlobalStatus.getOldPhone() != null && GlobalStatus.getOldPhone().equals(phone)){
                    isAccept = true;
                } else */if(GlobalStatus.getOldTeam() > 0 && GlobalStatus.getOldTeam() == teamID){
                    isAccept = true;
                }
                Log.v("wsDvr", "isAccept:" + isAccept);
//                if (GlobalStatus.getChatRoomMsg() == null || (GlobalStatus.equalPhone(phone) || GlobalStatus.equalTeamID(teamID))) {
                if(isAccept){
                    final long roomID = resp.getRoomID();
                    ConnUtil.screenOn(MyApplication.getContext());
                    ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();//发送接受
                    builder.setAcceptType(ProtoMessage.AcceptType.atAccept_VALUE);
                    builder.setRoomID(roomID);
                    MyService.start(context, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(VoiceAcceptProcesser.ACTION);
                    new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                        @Override
                        public void onTimeout() {

                        }

                        @Override
                        public void onGot(Intent i) {
                            if (i.getIntExtra("error_code", -1) ==
                                    ProtoMessage.ErrorCode.OK.getNumber()) {
                                String linkmanName = "";
                                String groupName = "";
                                int memberRole = 0;
                                Intent intent = new Intent(Call_ACTION);
                                if (teamID == 0) {
                                    DBManagerFriendsList db = new DBManagerFriendsList(context, true, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                                    AppliedFriends linkman = db.getFriend(phone);
                                    db.closeDB();
                                    linkmanName = phone;
                                    if(linkman != null){
                                        linkmanName = linkman.getNickName();
                                        if(TextUtils.isEmpty(linkmanName)){
                                            linkmanName = linkman.getUserName();
                                        }
                                    }
                                    intent.putExtra("linkmanName", linkmanName);
                                    intent.putExtra("linkmanPhone", phone);
                                } else {
                                    intent.putExtra("group", teamID);
                                    DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                                    TeamInfo t = db.getTeamInfo(teamID);
                                    db.closeDB();
                                    if (t == null) {
                                        throw new RuntimeException("取组信息时出错");
                                    }
                                    groupName = t.getTeamName();
                                    memberRole = t.getMemberRole();

                                    intent.putExtra("group_name", groupName);
                                    intent.putExtra("type", memberRole);

                                }
                                intent.putExtra("data", 1);
                                intent.putExtra("room_id", roomID);
                                ProtoMessage.ChatRoomMsg r = (ProtoMessage.ChatRoomMsg) i.getSerializableExtra("member");
                                if (r != null) {
                                    intent.putExtra("member", r);
                                }

                                // context.startActivity(intent);
                                String key = (String) SharedPreferencesUtils.get(context, Call_KEY, "");
                                boolean isNotification = true;
                                if (teamID == 0 && key.equals(phone)) {
                                    isNotification = false;
                                } else if (key.equals(groupName)) {
                                    isNotification = false;
                                }
                                Log.i("chatjrd", "测试数据  key:" + key + " phone:" + phone + " groupName:" + groupName + " teamID:" + teamID
                                        + " roomID:" + roomID);
                                if (key.equals("") || isNotification) {
                                    Log.i("jim", "通知");
//                                    NotifyProcesser.this.showNotification(teamID == 0, linkmanName, phone, groupName, teamID, memberRole, roomID);
                                    Intent intent1 = new Intent("call.refreshReceiiver");
                                    intent1.putExtra("room_id", roomID);
                                    context.sendBroadcast(intent1);
                                } else {
                                    Log.i("jim", "广播");
                                    context.sendBroadcast(intent);
                                }

                                Intent mIntent = new Intent(RESClient.ACTION_ONCLICK_LEFT_TOP);
                                mIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                context.sendBroadcast(mIntent);
                                //context.overridePendingTransition(R.anim.fade, R.anim.hold);
                            } else {
                                //fail(i.getIntExtra("error_code", -1));
                            }
                        }
                    });
                    return;
                }
                String temp = null;
                if (teamID == 0) {
                    temp = 0 + phone;
                } else {
                    temp = String.valueOf(1) + String.valueOf(teamID);
                }
                CallState callState = GlobalStatus.getCallSatte(temp);
                if (callState == null) {
                    callState = new CallState(temp, resp.getRoomID(), GlobalStatus.STATE_CALL);
                    GlobalStatus.putCallCallStatus(temp, callState);
                } else {
                    callState.setRoomId(resp.getRoomID());
                    callState.setState(GlobalStatus.STATE_CALL);
                    GlobalStatus.putCallCallStatus(temp, callState);
                }
                NotifyManager.getInstance().showNotification(temp, 0);

            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyChatStatus_VALUE) {
                Log.i("wsDvr", "notify chat status msg: " + resp.toString());
                int waitTime = 0;
                if(GlobalStatus.isStartRooming() || GlobalStatus.isAcceptRooming()){
                    waitTime = 500;
                }

                if(mHandler == null){
                    mHandler = new Handler(Looper.getMainLooper());
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("NotifyProcesser.ChatStatus");
                        intent.setPackage(MyApplication.getContext().getPackageName());
                        intent.putExtra("chat_status", resp);
                        context.sendBroadcast(intent);

                        ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
                        if (chatRoomMsg != null && chatRoomMsg.getRoomID() == resp.getRoomID()) {
                            if(!TextUtils.isEmpty(resp.getVideoUrl())){
                                GlobalStatus.setCurPlayAddr(resp.getVideoUrl());
                            }
                            if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyChatStatus_VALUE) {
                                SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                                String myPhone = preferences.getString("phone", "");
                                // 给自己的通知，不需要播放提示音
                                if (!myPhone.equals(resp.getFriendPhoneNum())) {
                                    Log.i("wsDvr", "--------- chat status changed ----------");
                                    if (resp.getChatStatus() == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
                                        MediaPlayerTool.getInstance().play(context, R.raw.notify_ptt2);
                                        mSpeaking = true;
                                        ConnUtil.screenOn(MyApplication.getContext());
                                    } else if (mSpeaking && resp.getChatStatus() == ProtoMessage.ChatStatus.csOk_VALUE) {
                                        mSpeaking = false;
                                        MediaPlayerTool.getInstance().play(context, R.raw.notify_ptt2);
                                    }
                                }
                            }

                            List<ProtoMessage.ChatRoomMemberMsg> memberMsgs = chatRoomMsg.getMembersList();

                            String friendPhoneNum = resp.getFriendPhoneNum();
                            int selectPosition = -1;
                            ProtoMessage.ChatRoomMemberMsg selectMember = null;
                            for (int i = 0; i < memberMsgs.size(); i++) {
                                ProtoMessage.ChatRoomMemberMsg memberMsg = memberMsgs.get(i);
                                if (friendPhoneNum.equals(memberMsg.getPhoneNum())) {
                                    selectPosition = i;
                                    selectMember = memberMsg;
                                }
                            }

                            if (selectPosition != -1 && chatRoomMsg.getRoomID() == resp.getRoomID()) {
                                ProtoMessage.ChatRoomMsg.Builder builder = chatRoomMsg.toBuilder();
                                builder.setMembers(selectPosition, selectMember.toBuilder().setStatus(resp.getChatStatus()));
                                GlobalStatus.updateChatRoomMsg(builder.build());
                            }
                        } else if (resp.getChatStatus() == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
                            String temp = null;
                            SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                            String myPhone = preferences.getString("phone", "");
                            Log.v("wsDvr","chatstatus phone:" + phone);
                            Log.v("wsDvr","chatstatus getTempChat:" + GlobalStatus.getTempChat());
                            if (teamID == 0 && !phone.equals(myPhone) && !phone.equals(GlobalStatus.getSingleLinkManPhone()) && !phone.equals(GlobalStatus.getTempChat())) {
                                temp = 0 + phone;
                                CallState callState = GlobalStatus.getCallSatte(temp);
                                if (callState == null) {
                                    callState = new CallState(temp, resp.getRoomID(), GlobalStatus.STATE_CALL);
                                    GlobalStatus.putCallCallStatus(temp, callState);
                                } else {
                                    callState.setRoomId(resp.getRoomID());
                                    if(callState.getState() == GlobalStatus.STATE_CLOSE) {
                                        callState.setState(GlobalStatus.STATE_CALL);
                                    }
                                    GlobalStatus.putCallCallStatus(temp, callState);
                                }
                                NotifyManager.getInstance().showNotification(temp, 0);
                            }
//                            else {
//                                temp = String.valueOf(1) + String.valueOf(teamID);
//                            }
//                            NotifyManager.getInstance().showNotification(temp, 2);
                        }
                    }
                },waitTime);
            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyLiveVideoCall_VALUE) {
                Log.v("VideoCall", resp.toString());
//                if ((MyApplication.getVideoTeam() == 0 || MyApplication.getVideoTeam() != resp.getTeamID())
//                        && (MyApplication.getVideoPhone() == null || !MyApplication.getVideoPhone().equals(resp.getFriendPhoneNum()))) {
//                    VideoInvitedActivity.startActivity(context, resp.getFriendPhoneNum(), teamID);
//                }
                DBMyLogHelper.insertLog(context, LogCode.VIDEO_CALL, resp.toString(), null);
                if (resp.getApplyType() == ProtoMessage.AcceptType.atAccept_VALUE) {
                    if (!TextUtils.isEmpty(phone)) {
//                        if (teamID == 0 && GlobalStatus.getChatRoomMsg() == null) {
                        if(phone.equals(GlobalStatus.getCurViewPhone())){
                            VideoRoadUtils.AcceptLiveCall(context, phone);
                        } else if (teamID == 0){
                            GlobalStatus.addViewRoadPhone(phone);
                            NotifyManager.getInstance().showNotification("0" + phone, 1);
                        }
//                        else if (teamID == 0) {
//                            Log.v("VideoCall", "当前正在对讲自动屏蔽路况分享请求");
//                            VideoRoadUtils.DenyLiveCall(context, phone);
//                        }
                        else {
                            Log.v("VideoCall", "暂时不支持群组");
                        }
                    }
                } else if(resp.getFriendPhoneNum().equals(GlobalStatus.getCurViewPhone())){
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                    ToastR.setToast(context, "用户 " + GlobalStatus.getCurViewPhone() + " 结束路况查询");
                    GlobalStatus.setCurViewPhone(null);
                }
            } else if (resp.getNotifyType() == ProtoMessage.NotifyType.NotifyLiveVideoCallAns_VALUE) {
                Log.v("VideoCall ANS", resp.toString());
                DBMyLogHelper.insertLog(context, LogCode.VIDEO_CALL_ANS, resp.toString(), null);
                if (resp.getApplyType() == ProtoMessage.AcceptType.atAccept_VALUE) {
                    if(phone.equals(GlobalStatus.getCurRoadPhone())){
                        return;
                    }
                    PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);// init powerManager
                    PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|
                            PowerManager.SCREEN_DIM_WAKE_LOCK,"target");
                    mWakelock.acquire(4000); // Wake up Screen and keep screen lighting
                    if(mHandler == null){
                        mHandler = new Handler(Looper.getMainLooper());
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            VideoCallActivity.startActivity(context, resp.getFriendPhoneNum(),resp.getVideoUrl());
                        }
                    },300);
                } else {
                    context.sendBroadcast(new Intent(ACTION_LIVE_CALL_ANS_DENY).putExtra("phone", resp.getFriendPhoneNum()));
                }
            }

           /* Intent i = new Intent(ACTION);

            i.putExtra("error_code", resp.getErrorCode());
            i.putExtra("token",resp.getToken());
            context.sendBroadcast(i);*/

            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //正在对讲
    private void showNotification(boolean single, String linkman, String linkmanPhone,
                                  String groupName, long group, int type, long roomId) {
        Intent notificationIntent = new Intent(context, FirstActivity.class);
        String titleMsg;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(-1);
        if (single) {
            titleMsg = linkman;
            notificationIntent.putExtra("linkmanName", linkman);
            notificationIntent.putExtra("linkmanPhone", linkmanPhone);
            notificationIntent.putExtra("data", 1);
        } else {
            titleMsg = groupName;
            notificationIntent.putExtra("group", group);
            notificationIntent.putExtra("data", 1);
            notificationIntent.putExtra("group_name", groupName);
            notificationIntent.putExtra("type", type);
        }
        notificationIntent.putExtra("room_id", roomId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification;
        notification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)//必须要先setSmallIcon，否则会显示默认的通知，不显示自定义通知
                .setTicker(titleMsg)
                .setContentTitle(titleMsg)
                .setContentText("正在对讲")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .build();
//        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
        //notification.flags += Notification.FLAG_AUTO_CANCEL|Notification.FLAG_ONGOING_EVENT;
        manager.notify(-1, notification);
    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
