package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.voice.VoiceHandler;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class SpeakerBeginProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.SpeakerBeginProcesser";

    public SpeakerBeginProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (GlobalStatus.isPttKeyDown() && resp.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {

                VoiceHandler.startRTMP(context,GlobalStatus.getCurRtmpAddr());
                if(GlobalStatus.getOnlineCount() > 1) {
                    if (!Build.PRODUCT.contains("LB1728")) {
                        ToastR.setToast(context, "请开始讲话 :-)", Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    }
                } else {
                    ToastR.setToast(context, "当前在线成员只有一人", Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                }
            }
            Intent i = new Intent(ACTION);
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
