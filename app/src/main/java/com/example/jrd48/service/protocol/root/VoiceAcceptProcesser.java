package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.service.notify.NotifyManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class VoiceAcceptProcesser extends CommonProcesser {
    private static final String TAG = "VoiceAcceptProcesser";
    public final static String ACTION = "ACTION.VoiceAcceptProcesser";

    public VoiceAcceptProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {

        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.ChatRoomMsg resp = ProtoMessage.ChatRoomMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("pocdemo", "VoiceAcceptProcesser 获取接受呼叫应答: " + resp.toString());
            i.putExtra("error_code", resp.getErrorCode());
            if(resp.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                GlobalStatus.clearChatRoomMsg();
                i.putExtra("member", resp);
                SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                String myPhone = preferences.getString("phone", "");
                if (resp.getAcceptType() == ProtoMessage.AcceptType.atAccept_VALUE) {
                    if (resp.getTeamID() > 0) {
                        GlobalStatus.setChatRoomMsg(resp);
                    } else {
                        if (resp.getPhoneNum().equals(myPhone)) {
                            for (ProtoMessage.ChatRoomMemberMsg chatRoomMemberMsg : resp.getMembersList()) {
                                if (!chatRoomMemberMsg.getPhoneNum().equals(myPhone)) {
                                    GlobalStatus.setChatRoomMsg(resp, chatRoomMemberMsg.getPhoneNum());
                                }
                            }

                        } else {
                            GlobalStatus.setChatRoomMsg(resp, resp.getPhoneNum());
                        }
                    }
                } else {
                    Log.d(TAG,"onGot-else-accept=OldTeam()=" + GlobalStatus.getOldTeam() + "--oldPhone=" + GlobalStatus.getOldPhone() + "--oldRoom=" + GlobalStatus.getOldRoom());
                    //GlobalStatus.setOldChat(0,"",0);
                    //GlobalStatus.setOldChatRoom(GlobalStatus.getOldRoom());
                    GlobalStatus.setOldChat(GlobalStatus.getOldTeam(), GlobalStatus.getOldPhone(), GlobalStatus.getOldRoom());
                    //GlobalStatus.clearChatRoomMsg();
                    //GlobalStatus.setChatRoomMsg(resp);
                }
            }
            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
            i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
        }
        GlobalStatus.setIsAcceptRooming(false);
        context.sendBroadcast(i);
    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
