package com.example.jrd48.service.protocol.root;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.voice.VoiceHandler;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class SpeakerMsgProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.SpeakerMsgProcesser";

    public SpeakerMsgProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.v("wsDvr","SpeakerMsgProcesser:" + resp.getErrorCode());
            if (resp.getErrorCode() == ProtoMessage.ErrorCode.NOT_FOUND_THIS_ROOM_VALUE) {
                VoiceHandler.doVoiceAction(context, false);
                closeRoom();
                ToastR.setToast(context, "没有找到聊天室或被解散");
            } else if (resp.getErrorCode() == ProtoMessage.ErrorCode.NOT_FOUND_THIS_ROOM_USER_VALUE) {
                VoiceHandler.doVoiceAction(context, false);
                closeRoom();
//                ToastR.setToast(context, "当前用户已离开当前聊天室");
            } else if (resp.getErrorCode() == ProtoMessage.ErrorCode.NOT_IN_ANY_CHAT_ROOM_VALUE) {
                VoiceHandler.doVoiceAction(context, false);
                closeRoom();
//                ToastR.setToast(context, "不在任何聊天室");
            } else if (resp.getErrorCode() == ProtoMessage.ErrorCode.OTHER_SPEAKING_NOW_VALUE) {
                VoiceHandler.doVoiceAction(context, false);
                ToastR.setToast(context, "他人正在说话，没有抢到话语权，停止说话");
            } else if(resp.getErrorCode()==ProtoMessage.ErrorCode.DIDNOT_SPEAK_BEGIN_VALUE) {
                List<Integer> statusList = GlobalStatus.getStatusList();
                ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
                if(chatRoomMsg != null && statusList != null && statusList.contains(ProtoMessage.ChatStatus.csSpeaking_VALUE)){
                    int speakPosition = statusList.indexOf(ProtoMessage.ChatStatus.csSpeaking_VALUE);
                    ProtoMessage.ChatRoomMemberMsg memberMsg = chatRoomMsg.getMembers(speakPosition);
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    String myPhone = preferences.getString("phone", "");
                    if(!memberMsg.getPhoneNum().equals(myPhone)) {
                        VoiceHandler.doVoiceAction(context, false);
                        ToastR.setToast(context, "话权被抢，停止说话");
                    }
                }
            }

            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void closeRoom() {
        if (GlobalStatus.msg_null()) {
            Intent i;
//            ToastR.setToast(context, "对讲异常，停止对讲");
            i = new Intent(AutoCloseProcesser.ACTION);
            i.putExtra("roomID", GlobalStatus.getRoomID());
            context.sendBroadcast(i);
            GlobalStatus.clearChatRoomMsg();
            NotificationManager nm = (NotificationManager) (context.getSystemService(context.NOTIFICATION_SERVICE));
            nm.cancel(-1);//消除对应ID的通知
        }
    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
