package com.example.jrd48;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBHelperChatTimeList;
import com.example.jrd48.chat.group.cache.DBManagerChatTimeList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ConnectionChangeReceiver;
import com.example.jrd48.service.notify.NotifyManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.grafika.gles.EglCore;
import com.luobin.log.DBMyLogHelper;
import com.luobin.log.LogCode;
import com.luobin.model.CallState;
import com.luobin.musbcam.UsbCamera;
import com.luobin.timer.ChatManager;
import com.luobin.utils.VideoRoadUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import me.lake.librestreaming.client.CameraVideo;
import me.lake.librestreaming.client.RESClient;

/**
 * Created by qhb on 17-1-16.
 */

public class GlobalStatus {
    public static final int MAX_TEXT_BYTE_COUNT = 32;
    public static final int MIN_TEXT_BYTE_COUNT = 4;
    public static final String TAG = "GlobalStatus";
    public static final int MAX_PLATE_NUMBER = 9;
    public static final int MAX_TEXT_COUNT = 16;
    public static final int MIN_NUMBER_NAME_COUNT = 3;
    public static final String NOTIFY_CALL_ACTION = "com.android.luobin.notify_call_action";
    public static final String REQUEST_CALL_ACTION = "com.android.luobin.request_call_action";
    public static final int STATE_CLOSE = 0;
    public static final int STATE_CALL = 1;
    public static final int STATE_ACCEPT = 2;
    private static String tempChat;
    private static ProtoMessage.ChatRoomMsg msg;
    private static Map<String,TeamMemberInfo> teamMemberInfoMap;
    private static String singleLinkManName;
    private static long chatRoomtempId;
    private static boolean firstCreating;
    private static EglCore eglCore;
    private static boolean isUSBVideoShow;
    private static boolean isDvrCamShow;
    private static ProtoMessage.CarRegister carRegister;
    private static Map<String, CallState> callCallStatus;
    private static Map<String,Long> viewRoadPhones;
    private static String curViewPhone;
    private static String singleLinkManPhone = "";

    private static String oldPhone = null;
    private static long oldTeam = -1;
    private static long oldRoom = -1;
    private static String curPlayAddr = null;
    private static String curRtmpAddr = null;
    private static boolean pttBroadCast = false;
    private static boolean pttKeyDown = false;
    private static boolean isMediaCodec = false;
    private static boolean isVideo = true;
    private static boolean isRandomChat = false;
    private static boolean isRandomChatVideo = false;
    private static UsbCamera usbVideo1;
    private static UsbCamera usbVideo2;
    private static Camera camera;
    public static CameraVideo mCameraVideo;
    private static boolean isStartRooming;
    private static boolean isAcceptRooming;
    private static int policyStatus = ConnectionChangeReceiver.TYPE_WARNING;
    private static int retryCount = 0;
    private static Handler handler;
    private static List<Long> predefineTeams;
    private static List<TeamInfo> teamsInfoList;
    private static boolean isReceiveLauncher;
    private static int changeHeartbeat = -1;
    private static boolean isFirstPause = false;
    private static String curRoadPhone = null;
    public static boolean isPttKeyDown() {
        return pttKeyDown;
    }

    public static void setPttKeyDown(boolean pttKeyDown) {
        Log.v("wsdvr", "setPttKeyDown:" + pttKeyDown);
        GlobalStatus.pttKeyDown = pttKeyDown;
    }

    public synchronized static void setChatRoomMsg(ProtoMessage.ChatRoomMsg amsg, String friendPhone) {
        Log.v("wsdvr", "old:" + singleLinkManPhone + "," + friendPhone);
        String oldman = singleLinkManPhone;
        msg = amsg.toBuilder().build();
        setOldChat(0,friendPhone,msg.getRoomID());
        singleLinkManPhone = friendPhone;
        Context context = MyApplication.getContext();
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        if (myPhone.equals(singleLinkManPhone) || (oldman != null && oldman.equals(friendPhone))) {
            return;
        }

        try {
            DBManagerFriendsList db = new DBManagerFriendsList(context, true, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
            String linkmanName = db.getFriendName(friendPhone);
            db.closeDB();

            if (TextUtils.isEmpty(linkmanName)) {
                singleLinkManName = friendPhone;
            } else {
                singleLinkManName = linkmanName;
            }
            Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_NAME, singleLinkManName);
            //Log.d(TAG,"setIsRandomChat 145");
            setIsRandomChat(false);
        } catch (Exception e){
            e.printStackTrace();
        }
        DvrService.start(context, RESClient.ACTION_STOP_RTMP, null);
        if(getCurViewPhone() != null) {
            setCurViewPhone(null);
        }

//        startSingleVideo(msg, friendPhone);
        String temp = 0 + getSingleLinkManPhone();
        CallState callState = GlobalStatus.getCallSatte(temp);
        if (callState == null) {
            callState = new CallState(temp, msg.getRoomID(), GlobalStatus.STATE_ACCEPT);
            GlobalStatus.putCallCallStatus(temp, callState);
        } else {
            callState.setRoomId(msg.getRoomID());
            callState.setState(GlobalStatus.STATE_ACCEPT);
            GlobalStatus.putCallCallStatus(temp, callState);
        }
        //add url
        Log.d(TAG, "getVideoUrl===" + msg.getVideoUrl());
        if(!TextUtils.isEmpty(msg.getVideoUrl())){
            GlobalStatus.setCurPlayAddr(msg.getVideoUrl());
        }
        changeChatStatusInfo();
        ChatManager.getInstance().setStartTime(SystemClock.elapsedRealtime());
//        VoiceHandler.startRTMP(MyApplication.getContext(),null,true);
    }

    public synchronized static void setChatRoomMsg(ProtoMessage.ChatRoomMsg r) {
        Log.d(TAG, "setChatRoomMsg :" + r);
        setOldChat(r.getTeamID(),"",r.getRoomID());
        ProtoMessage.ChatRoomMsg old = msg;

        msg = r.toBuilder().build();

        singleLinkManPhone = "";
        singleLinkManName = "";
        Context context = MyApplication.getContext();
        DBManagerChatTimeList chatDB = new DBManagerChatTimeList(context, DBTableName.getTableName(context, DBHelperChatTimeList.NAME));
        chatDB.updateData(msg.getTeamID(),System.currentTimeMillis());
        chatDB.closeDB();
        if (old != null && old.getTeamID() == r.getTeamID() && old.getRoomID() == r.getRoomID()) {
            return;
        }
        try {
            teamMemberInfoMap = GlobalStatus.getTeamMember(context, msg.getTeamID());
            DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
            TeamInfo t = db.getTeamInfo(msg.getTeamID());
            db.closeDB();
            if (t == null || TextUtils.isEmpty(t.getTeamName())) {
                Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_NAME, String.valueOf(msg.getTeamID()));
            } else {
                Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_NAME, t.getTeamName());
            }
            if(t != null && t.getTeamType() == ProtoMessage.TeamType.teamRandom.getNumber()){
                //Log.d(TAG,"setIsRandomChat 202");
                //setIsRandomChat(true);
                SharedPreferences preference = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                /*int teamRandom = preference.getInt("team_random", 0);
                int teamRandomVideo = preference.getInt("team_random_video", 0);
                Log.d(TAG,"teamRandom="+teamRandom+",teamRandomVideo="+teamRandomVideo);
                if (teamRandom == 1 || teamRandomVideo == 1) {
                    setIsRandomChatVideo(false);
                }else{
                    setIsRandomChatVideo(true);
                }*/
            }else{
                Log.d(TAG,"setIsRandomChat 214");
                //setIsRandomChat(false);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
        if(getCurViewPhone() != null) {
            setCurViewPhone(null);
        }
        String temp = String.valueOf(1) + String.valueOf(msg.getTeamID());
        SharedPreferencesUtils.put(MyApplication.getContext(),"cur_teamId",msg.getTeamID());
        CallState callState = GlobalStatus.getCallSatte(temp);
        if (callState == null) {
            callState = new CallState(temp, msg.getRoomID(), GlobalStatus.STATE_ACCEPT);
            GlobalStatus.putCallCallStatus(temp, callState);
        } else {
            callState.setRoomId(msg.getRoomID());
            callState.setState(GlobalStatus.STATE_ACCEPT);
            GlobalStatus.putCallCallStatus(temp, callState);
        }
        //add url
        Log.d(TAG, "getVideoUrl===" + msg.getVideoUrl());
        if(!TextUtils.isEmpty(msg.getVideoUrl())){
            GlobalStatus.setCurPlayAddr(msg.getVideoUrl());
        }
        changeChatStatusInfo();
        ChatManager.getInstance().setStartTime(SystemClock.elapsedRealtime());
//        VoiceHandler.startRTMP(MyApplication.getContext(),null,true);
    }


    public static Map<String,TeamMemberInfo> getTeamMember(Context mContext,Long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        Map<String,TeamMemberInfo> teamMemberInfo = new HashMap<String,TeamMemberInfo>();
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
            Cursor c = db.query("LinkmanMember", null, null, null, null, null, null);
            try {
                TeamMemberInfo af = null;
                while (c.moveToNext()) {
                    af = new TeamMemberInfo();
                    af.setUserName(c.getString(c.getColumnIndex("user_name")));
                    af.setUserPhone(c.getString(c.getColumnIndex("user_phone")));
                    af.setNickName(c.getString(c.getColumnIndex("nick_name")));
                    af.setRole(c.getInt(c.getColumnIndex("role")));
                    af.setMemberPriority(c.getInt(c.getColumnIndex("member_priority")));
                    teamMemberInfo.put(af.getUserPhone(),af);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        } finally {
            db.close();
        }
        return teamMemberInfo;
    }

    public synchronized static void updateChatRoomMsg(ProtoMessage.ChatRoomMsg r) {
        msg = r.toBuilder().build();
        if(msg.getTeamID() > 0){
            teamMemberInfoMap = GlobalStatus.getTeamMember(MyApplication.getContext(),msg.getTeamID());
        }
        changeChatStatusInfo();
    }

    public synchronized static ProtoMessage.ChatRoomMsg getChatRoomMsg() {
        return msg;
    }


    private static List<Integer> statusList;
    private static int onlineCount;
    public static Set<String> onChatSet = new HashSet<String>();
    public synchronized static void changeChatStatusInfo() {
        Context context = MyApplication.getContext();

        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        if (msg != null) {
            if (statusList == null) {
                statusList = new ArrayList<Integer>();
            } else {
                statusList.clear();
            }
            onlineCount = 0;
            onChatSet.clear();
            for (ProtoMessage.ChatRoomMemberMsg memberMsg : msg.getMembersList()) {
                if (memberMsg.getPhoneNum().equals(myPhone)
                        || memberMsg.getStatus() == ProtoMessage.ChatStatus.csOk_VALUE
                        || memberMsg.getStatus() == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
                    onlineCount++;
                    onChatSet.add(memberMsg.getPhoneNum());
                }
                statusList.add(memberMsg.getStatus());
            }
            Log.d(TAG, "onChatSet:size=" + onChatSet.size());
            Log.d(TAG, "chatRoomMsg:" + msg);
            Log.d(TAG, "statusList:" + statusList.toString());

            if(statusList.contains(ProtoMessage.ChatStatus.csSpeaking_VALUE)){
                int speakPosition = statusList.indexOf(ProtoMessage.ChatStatus.csSpeaking_VALUE);
                ProtoMessage.ChatRoomMemberMsg memberMsg = msg.getMembers(speakPosition);
                String selfAddr = null;
                if (msg.getTeamID() > 0) {
                    selfAddr = RESClient.PLAY_SERVER_URL + "group---" + GlobalStatus.getRoomID() + "---" + memberMsg.getPhoneNum();
                } else {
                    selfAddr = RESClient.PLAY_SERVER_URL + GlobalStatus.getRoomID() + "---" + singleLinkManPhone;
                }
                if(!TextUtils.isEmpty(GlobalStatus.getCurPlayAddr())){
                    selfAddr = selfAddr + "_" + GlobalStatus.getCurPlayAddr();
                }
                if(isVideo()){
                    if (!memberMsg.getPhoneNum().equals(myPhone)) {
                        DvrService.start(context, RESClient.ACTION_START_PLAY, selfAddr);
                    }
                } else {
                    DvrService.start(context, RESClient.ACTION_STOP_PLAY, null);
                }

                if(memberMsg.getPhoneNum().equals(myPhone)){
                    Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_INFO, "本机用户");
                } else {
                    if (msg.getTeamID() == 0) {
                        Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_INFO, singleLinkManName);
                    } else {
                        String showName = memberMsg.getPhoneNum();
                        if(teamMemberInfoMap != null) {
                            TeamMemberInfo info = teamMemberInfoMap.get(memberMsg.getPhoneNum());
                            if (info != null) {
                                showName = info.getNickName();
                                if (TextUtils.isEmpty(showName)) {
                                    showName = info.getUserName();
                                }

                                if (TextUtils.isEmpty(showName)) {
                                    showName = memberMsg.getPhoneNum();
                                }
                            }
                        }

                        Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_INFO, showName);
                    }
                }
            } else {
                Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_INFO, "");
                DvrService.start(context, RESClient.ACTION_STOP_PLAY, null);
            }
        }
    }

    public static boolean checkSpeakPhone(String phone,long roomId){
        if(msg == null || msg.getRoomID() != roomId){
            return false;
        }
        if (statusList.contains(ProtoMessage.ChatStatus.csSpeaking_VALUE)) {
            int speakPosition = statusList.indexOf(ProtoMessage.ChatStatus.csSpeaking_VALUE);
            ProtoMessage.ChatRoomMemberMsg memberMsg = msg.getMembers(speakPosition);
            if (memberMsg.getPhoneNum().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    public synchronized static void clearChatRoomMsg() {
        Log.v(TAG, "clearChatRoomMsg");

        if (msg != null) {
            DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
            DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_PLAY, null);
            GlobalStatus.setPttKeyDown(false);
            String temp = null;
            if (msg.getTeamID() == 0) {
                temp = 0 + getSingleLinkManPhone();
            } else {
                temp = String.valueOf(1) + String.valueOf(msg.getTeamID());
            }
            CallState callState = GlobalStatus.getCallSatte(temp);
            int changeStatus;
            if(onlineCount > 2) {
                changeStatus = GlobalStatus.STATE_CALL;
            } else {
                changeStatus = GlobalStatus.STATE_CLOSE;
            }
            if (callState == null) {
                callState = new CallState(temp, 0, changeStatus);
                GlobalStatus.putCallCallStatus(temp, callState);
            } else {
//                callState.setRoomId(0);
                callState.setState(changeStatus);
                GlobalStatus.putCallCallStatus(temp, callState);
            }
        }
        msg = null;
        NotificationManager nm = (NotificationManager) (MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE));
        nm.cancel(-1);//消除对应ID的通知
        Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_NAME, "");
        Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_INFO, "");
        singleLinkManPhone = "";
        singleLinkManName = "";
        isVideo = true;
        setIsRandomChat(false);
        ChatManager.getInstance().setStartTime(0);
        onlineCount = 0;
        onChatSet.clear();
    }

    public static long getRoomID() {
        if (msg != null) {
            return msg.getRoomID();
        }
        return -1;
    }

    public static boolean msg_null() {
        if (msg != null) {
            return true;
        }
        return false;
    }

    public static void closeCurChat(){
        if (GlobalStatus.msg_null()) {
            Context context = MyApplication.getContext();
            ToastR.setToast(context, "网络断开，对讲暂停");//status
            Log.d(TAG, "closeCurChat--network disconnected, stop chating");
            Intent i = new Intent(AutoCloseProcesser.ACTION);
            i.putExtra("roomID", GlobalStatus.getRoomID());
            context.sendBroadcast(i);
            GlobalStatus.clearChatRoomMsg();
            NotificationManager nm = (NotificationManager) (context.getSystemService(Context.NOTIFICATION_SERVICE));
            nm.cancel(-1);//消除对应ID的通知
        }
    }

    public static boolean equalTeamID(long teamID) {
        if (msg == null) return false;
        if (teamID == 0) return false;
        return msg.getTeamID() == teamID;
    }

    public static boolean equalPhone(String phone) {
        if (msg == null) return false;
        if (TextUtils.isEmpty(phone)) return false;
        if (msg.getTeamID() == 0) {
            return singleLinkManPhone.equals(phone);
        }
        return false;
    }

    public static void equalRoomID(long roomID) {
        if (msg != null && msg.getRoomID() == roomID) {
            clearChatRoomMsg();
        } else {
            closeRoom(roomID);
        }
    }

    public static boolean isMediaCodec() {
        return isMediaCodec;
    }

    public static void setIsMediaCodec(boolean isMediaCodec) {
        GlobalStatus.isMediaCodec = isMediaCodec;
    }

    public static long getChatTeamId() {
        if (msg == null) return 0;
        return msg.getTeamID();
    }

    public static String getSingleLinkManPhone() {
        return singleLinkManPhone;
    }

    public static int getOnlineCount() {
        if (onlineCount < 0) {
            onlineCount = 0;
        }
        return onlineCount;
    }
    public static Set<String>  getChatList() {
        if (onChatSet == null) {
            Log.w(TAG,"getChatList onChatSet == null");
            onChatSet = new HashSet<String>();
        }
        return onChatSet;
    }
    public static List<Integer> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<Integer>();
        }
        return statusList;
    }

    public static boolean isVideo() {
        Log.d(TAG,"Global,isVideo="+isVideo);
        Log.d(TAG,"Global,isRandom="+IsRandomChat());
        Log.d(TAG,"Global,IsRandomChatVideo="+IsRandomChatVideo());
        if(IsRandomChat()){
            return IsRandomChatVideo();
        }
        return isVideo;
    }

    public static void setIsVideo(boolean isVideo) {
        GlobalStatus.isVideo = isVideo;
    }
    public static void setIsRandomChat(boolean isRandom) {
        //Log.d(TAG,"Global,setIsRandomChat="+isRandom);
        GlobalStatus.isRandomChat = isRandom;
    }
    public static boolean IsRandomChat() {
        return GlobalStatus.isRandomChat;
    }
    public static void setIsRandomChatVideo(boolean isRandomVideo) {
        //Log.d(TAG,"Global,setIsRandomChatVideo="+isRandomVideo);
        GlobalStatus.isRandomChatVideo = isRandomVideo;
    }
    public static boolean IsRandomChatVideo() {
        return GlobalStatus.isRandomChatVideo;
    }
    public static Map<String, CallState> getCallCallStatus() {
        if (callCallStatus == null) {
            callCallStatus = new HashMap<String, CallState>();
        }
        return callCallStatus;
    }

    public static void putCallCallStatus(String number, CallState state) {
        Log.e("wsDvr","putCallCallStatus:" + state.toString());
        getCallCallStatus().put(number, state);
        MyApplication.getContext().sendBroadcast(new Intent(NOTIFY_CALL_ACTION));
    }

    public static CallState getCallSatte(String number) {
        return getCallCallStatus().get(number);
    }

    public static void closeRoom(long roomId) {
        if (callCallStatus != null) {
            Iterator<Map.Entry<String, CallState>> it = callCallStatus.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,CallState> entry = it.next();
                CallState callState = entry.getValue();
                if(callState.getRoomId() == roomId){
                    callState.setState(STATE_CLOSE);
                    callState.setRoomId(0);
                    putCallCallStatus(callState.getNumber(),callState);
                }
            }
        }
    }

    public static Map<String,Long> getViewRoadPhones() {
        if(viewRoadPhones == null){
            viewRoadPhones = new HashMap<String, Long>();
        }
        return viewRoadPhones;
    }

    public static void addViewRoadPhone(String viewRoadPhone) {
        Log.v(TAG,"addViewRoadPhone:" + viewRoadPhone);
        getViewRoadPhones().put(viewRoadPhone,SystemClock.elapsedRealtime() + 30000);
        MyApplication.getContext().sendBroadcast(new Intent(REQUEST_CALL_ACTION));
    }

    public static void removeViewRoadPhone(String viewRoadPhone) {
        Log.v(TAG,"removeViewRoadPhone:" + viewRoadPhone);
        if(getViewRoadPhones().containsKey(viewRoadPhone)){
            getViewRoadPhones().remove(viewRoadPhone);
            NotifyManager.getInstance().removeNames("0" + viewRoadPhone);
            VideoRoadUtils.DenyLiveCall(MyApplication.getContext(),viewRoadPhone);
        }
        MyApplication.getContext().sendBroadcast(new Intent(REQUEST_CALL_ACTION));
    }

    public static String getCurViewPhone() {
        return curViewPhone;
    }

    public static void setCurViewPhone(String curViewPhone) {
        if(GlobalStatus.curViewPhone != null && !GlobalStatus.curViewPhone.equals(curViewPhone)){
            removeViewRoadPhone(GlobalStatus.curViewPhone);
        }
        if(curViewPhone != null){
            NotifyManager.getInstance().removeNames("0" + curViewPhone);
        }
        if(curViewPhone == null && GlobalStatus.curViewPhone != null){
            String showPhone = DBManagerFriendsList.getAFriendNickName(MyApplication.getContext(),GlobalStatus.curViewPhone);
            if(Pattern.matches("\\d{11}",showPhone)){
                showPhone = showPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            }
            ToastR.setToast(MyApplication.getContext(), "结束了对用户 " + showPhone + "的路况分享");
        }
        GlobalStatus.curViewPhone = curViewPhone;
        MyApplication.getContext().sendBroadcast(new Intent(REQUEST_CALL_ACTION));
    }

    public static ProtoMessage.CarRegister getCarRegister() {
        return carRegister;
    }

    public static void setCarRegister(ProtoMessage.CarRegister carRegister) {
        GlobalStatus.carRegister = carRegister;
    }

    public static Camera getCamera() {
        return camera;
    }

    public static void setCamera(Camera camera) {
        GlobalStatus.camera = camera;
    }

    public static CameraVideo getCameraVideo() {
        return mCameraVideo;
    }

    public static void setCameraVideo(CameraVideo cameraVideo) {
        GlobalStatus.mCameraVideo = cameraVideo;
    }

    public static UsbCamera getUsbVideo1() {
        return usbVideo1;
    }

    public static void setUsbVideo1(UsbCamera usbVideo1) {
        Log.v("wsDvr","setUsbVideo1,"+(usbVideo1==null));
        GlobalStatus.usbVideo1 = usbVideo1;
    }
    public static UsbCamera getUsbVideo2() {
        return usbVideo2;
    }

    public static void setUsbVideo2(UsbCamera usbVideo2) {
        GlobalStatus.usbVideo2 = usbVideo2;
    }

    public static EglCore getEglCore() {
        return eglCore;
    }

    public static void setEglCore(EglCore eglCore) {
        GlobalStatus.eglCore = eglCore;
    }

    public static String getCurRtmpAddr() {
        Log.v("wsDvr","getCurRtmpAddr:" + curRtmpAddr);
        return curRtmpAddr;
    }

    public static void setCurRtmpAddr(String curRtmpAddr) {
        Log.v("wsDvr","setCurRtmpAddr:" + curRtmpAddr);
        GlobalStatus.curRtmpAddr = curRtmpAddr;
    }

    public static boolean isStartRooming() {
        return isStartRooming;
    }

    public static void setIsStartRooming(boolean isStartRooming) {
        GlobalStatus.isStartRooming = isStartRooming;
    }

    public static long getChatRoomtempId() {
        return chatRoomtempId;
    }

    public static void setChatRoomtempId(long chatRoomtempId) {
        GlobalStatus.chatRoomtempId = chatRoomtempId;
    }

    public static boolean getFirstCreating() {
        return firstCreating;
    }

    public static void setFirstCreating(boolean firstCreating) {
        GlobalStatus.firstCreating = firstCreating;
    }

    public static int getPolicyStatus() {
        return policyStatus;
    }

    public static void setPolicyStatus(int policyStatus) {
        GlobalStatus.policyStatus = policyStatus;
    }

    public static boolean isUSBVideoShow() {
        return isUSBVideoShow;
    }

    public static void setIsUSBVideoShow(boolean isUSBVideoShow) {
        GlobalStatus.isUSBVideoShow = isUSBVideoShow;
    }

    public static boolean isDvrCamShow() {
        return isDvrCamShow;
    }

    public static void setIsDvrCamShow(boolean isDvrCamShow) {
        GlobalStatus.isDvrCamShow = isDvrCamShow;
    }

    public static int getRetryCount() {
        return retryCount;
    }

    public static void setRetryCount(int retryCount) {
        GlobalStatus.retryCount = retryCount;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        GlobalStatus.handler = handler;
    }

    public static List<Long> getPredefineTeams() {
        if(predefineTeams == null){
            predefineTeams = new ArrayList<Long>();
        }
        return predefineTeams;
    }

    public static void setPredefineTeams(List<Long> predefineTeams) {
        GlobalStatus.predefineTeams = predefineTeams;
    }

    public static List<TeamInfo> getTeamsInfoList() {
        if(teamsInfoList == null){
            teamsInfoList = new ArrayList<TeamInfo>();
        }
        return teamsInfoList;
    }

    public static void setTeamsInfoList(List<TeamInfo> teamsInfoList) {
        GlobalStatus.teamsInfoList = teamsInfoList;
    }

    public static boolean isReceiveLauncher() {
        return isReceiveLauncher;
    }

    public static void setIsReceiveLauncher(boolean isReceiveLauncher) {
        GlobalStatus.isReceiveLauncher = isReceiveLauncher;
    }

    public static int getChangeHeartbeat() {
        if(changeHeartbeat == -1){
            SettingRW mSettings = new SettingRW(MyApplication.getContext());
            mSettings.load();
            changeHeartbeat = mSettings.getChangeHeartbeat();
        }
        return changeHeartbeat;
    }

    public static void setChangeHeartbeat(int changeHeartbeat) {
        GlobalStatus.changeHeartbeat = changeHeartbeat;
    }
    public static final String NAVI_START_STOP = Settings.System.NAVI_START_STOP;
    public static int getShutDownType(Context context) {
        return Settings.System.getInt(context.getContentResolver(), NAVI_START_STOP, 1);
    }

    public static boolean isPttBroadCast() {
        return pttBroadCast;
    }

    public static void setPttBroadCast(boolean pttBroadCast) {
        GlobalStatus.pttBroadCast = pttBroadCast;
    }

    public static String getOldPhone() {
        if(oldPhone == null){
            oldPhone = (String) SharedPreferencesUtils.get(MyApplication.getContext(),"oldPhone","");
        }
        Log.v("wsDvr","getOldPhone:" + oldPhone);
        return oldPhone;
    }

    public static void setOldChat(long oldTeam,String oldPhone, long oldRoom) {
        Log.v("wsDvr","oldTeam:" + oldTeam + ",oldPhone:" + oldPhone);
        GlobalStatus.oldTeam = oldTeam;
        GlobalStatus.oldPhone = oldPhone;
        GlobalStatus.oldRoom = oldRoom;
        if(oldTeam != 0){
            NotifyManager.getInstance().removeNames(String.valueOf(1) + String.valueOf(oldTeam));
        } else if(!TextUtils.isEmpty(oldPhone)){
            NotifyManager.getInstance().removeNames(0 + oldPhone);
        }
        SharedPreferencesUtils.put(MyApplication.getContext(),"oldTeam",oldTeam);
        SharedPreferencesUtils.put(MyApplication.getContext(),"oldPhone",oldPhone);
        SharedPreferencesUtils.put(MyApplication.getContext(),"oldRoom",oldRoom);
    }

    public static long getOldTeam() {
        if(oldTeam == -1){
            oldTeam = (Long) SharedPreferencesUtils.get(MyApplication.getContext(),"oldTeam", (Long)0L);
        }
        Log.v("wsDvr","getOldTeam:" + oldTeam);
        return oldTeam;
    }
    public static long getOldRoom() {
        if(oldRoom == -1){
            oldRoom = (Long) SharedPreferencesUtils.get(MyApplication.getContext(),"oldRoom",(Long)0L);
        }
        Log.v("wsDvr","oldRoom:" + oldRoom);
        return oldRoom;
    }
    /**
     * 重新恢复对话*/

    public static void setOldChatRoom(long oldChat) {
        Log.v("wsDvr","setOldChatRoom oldChat:" + oldChat);
        SharedPreferencesUtils.put(MyApplication.getContext(),"oldChatRoom",oldChat);
    }
    public static long getOldChatRoom() {
        Log.v("wsDvr","getOldChatRoom");
        return (long)SharedPreferencesUtils.get(MyApplication.getContext(),"oldChatRoom",(Long)0L);
    }
    public static String getCurPlayAddr() {
        return curPlayAddr;
    }

    public static void setCurPlayAddr(String curPlayAddr) {
        GlobalStatus.curPlayAddr = curPlayAddr;
    }

    public static String getTempChat() {
        return tempChat;
    }

    public static void setTempChat(String tempChat) {
        Log.v("wsDvr","setTempChat:" + tempChat);
        GlobalStatus.tempChat = tempChat;
    }

    public static boolean isIsFirstPause() {
        Log.e("wsDvr","isFirstPause:" + isFirstPause);
        return isFirstPause;
    }

    public static void setIsFirstPause(boolean isFirstPause) {
        GlobalStatus.isFirstPause = isFirstPause;
    }

    public static String getCurRoadPhone() {
        return curRoadPhone;
    }

    public static void setCurRoadPhone(String curRoadPhone) {
        GlobalStatus.curRoadPhone = curRoadPhone;
    }

    public static boolean isAcceptRooming() {
        return isAcceptRooming;
    }

    public static void setIsAcceptRooming(boolean isAcceptRooming) {
        GlobalStatus.isAcceptRooming = isAcceptRooming;
    }

    public static void findError(String str){
        Log.e("wsDvr","findError:" + str);
        DBMyLogHelper.insertLog(MyApplication.getContext(), LogCode.BUG, str, null);
        DvrService dvrService = RESClient.getInstance().getDvrService();
        if(dvrService != null) {
            dvrService.stopRecord();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (GlobalStatus.getUsbVideo1() != null) {
            GlobalStatus.getUsbVideo1().close();
            GlobalStatus.setUsbVideo1(null);
        }
        if (GlobalStatus.getUsbVideo2() != null) {
            GlobalStatus.getUsbVideo2().close();
            GlobalStatus.setUsbVideo2(null);
        }
        if (GlobalStatus.getCamera() != null) {
            GlobalStatus.getCamera().stopPreview();
            GlobalStatus.getCamera().release();
            GlobalStatus.setCamera(null);
        }
        if (GlobalStatus.getCameraVideo() != null) {
            GlobalStatus.getCameraVideo().release();
            GlobalStatus.setCameraVideo(null);
        }
        System.exit(0);
    }
}
