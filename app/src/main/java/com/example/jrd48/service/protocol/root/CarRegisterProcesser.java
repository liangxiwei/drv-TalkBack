package com.example.jrd48.service.protocol.root;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2017/8/22.
 */

public class CarRegisterProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.CarRegisterProcesser";

    public CarRegisterProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CarRegister resp = ProtoMessage.CarRegister.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            GlobalStatus.setCarRegister(resp);
            Intent i = new Intent(ACTION);
            context.sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
