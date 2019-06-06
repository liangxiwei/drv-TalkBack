package com.example.jrd48.service.protocol.root;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class AutoCloseProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.AutoCloseProcesser";
    public Handler handler;
    public AutoCloseProcesser(Context context) {
        super(context);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onGot(byte[] data) {
        try {
            final ProtoMessage.AcceptVoice resp = ProtoMessage.AcceptVoice.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("pocdemo", "获取自动关闭房间应答: " + resp);

            if(GlobalStatus.getChatRoomMsg() != null && GlobalStatus.getChatRoomMsg().getRoomID() == resp.getRoomID()) {

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if(GlobalStatus.getFirstCreating()){
                            handler.postDelayed(this,1000);
                            return;
                        }
                        Intent i = new Intent(ACTION);
                        GlobalStatus.setOldChat(0,"",0);
                        GlobalStatus.equalRoomID(resp.getRoomID());
                        i.putExtra("error_code", resp.getErrorCode());
                        i.putExtra("roomID", resp.getRoomID());
                        context.sendBroadcast(i);
                        NotificationManager nm = (NotificationManager) (context.getSystemService(Context.NOTIFICATION_SERVICE));
                        nm.cancel(-1);//消除对应ID的通知
                        // TODO: 0 OK，其他值，失败
                        //
                    }
                };
                handler.post(r);
            } else {
                GlobalStatus.closeRoom(resp.getRoomID());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
