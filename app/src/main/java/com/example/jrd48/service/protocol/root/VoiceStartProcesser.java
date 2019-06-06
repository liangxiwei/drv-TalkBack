package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class VoiceStartProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.VoiceStartProcesser";

    public VoiceStartProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.ChatRoomMsg resp = ProtoMessage.ChatRoomMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("pocdemo", "获取发起呼叫应答: " + resp);
            Intent i = new Intent(ACTION);

            long id = resp.getRoomID();
            if(resp.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                if(GlobalStatus.getChatRoomtempId() == -1){
                    GlobalStatus.setChatRoomtempId(0);
                    ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
                    builder.setRoomID(id);
                    Log.v("wsDvr","roomId:" + id);
                    builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
                    MyService.start(context, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
                } else {
                    GlobalStatus.setChatRoomtempId(id);
                    if (resp.getTeamID() > 0) {
                        GlobalStatus.setChatRoomMsg(resp);
                    } else {
                        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                        String myPhone = preferences.getString("phone", "");
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
                }
            }
            GlobalStatus.setIsStartRooming(false);
            i.putExtra("room_id", id);
            i.putExtra("member", resp);
            i.putExtra("error_code", resp.getErrorCode());
            context.sendBroadcast(i);

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
