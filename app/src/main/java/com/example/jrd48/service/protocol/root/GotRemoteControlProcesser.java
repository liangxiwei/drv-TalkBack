package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2016/12/5.
 */

public class GotRemoteControlProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.SearchStrangerProcesser";

    public GotRemoteControlProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得查询好友应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.MsgRemoteControl re = ProtoMessage.MsgRemoteControl.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("tttt", "获得接收到遥控指令");
                    int type = re.getRemoteCmd();
                    if (type == ProtoMessage.RemoteCmd.rcNavigate_VALUE) {
                        String address = re.getCmdStr();
                        address = (TextUtils.isEmpty(address) ? "该位置" : address);
                        ToastR.setToast(context, "获得遥控指令，导航到【 "+address+" 】");
                        toAnsRemoteControl();

                        //发送导航指令
                        Intent intent = new Intent();
                        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                        intent.putExtra("KEY_TYPE", 10032);
                        intent.putExtra("EXTRA_DNAME",re.getCmdStr());
                        intent.putExtra("EXTRA_DLON",re.getLng());
                        intent.putExtra("EXTRA_DLAT",re.getLat());
                        intent.putExtra("EXTRA_DEV",0);
                        intent.putExtra("EXTRA_M",0);
                        context.sendBroadcast(intent);

                    } else {
                        Log.w("tttt","暂时不支持的远程指令："+type);
                    }
                } else {
                    Log.i("tttt", "获得接收到遥控指令错误码: " + re.getErrorCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
        }
        context.sendBroadcast(i);
    }


    /*
    * 向服务器发送应答模式
    * */
    private void toAnsRemoteControl() {
        ProtoMessage.MsgRemoteControl.Builder builder = ProtoMessage.MsgRemoteControl.newBuilder();
        builder.setToPhone("");
        builder.setFromAppType(ProtoMessage.AppType.appCar_VALUE);
        MyService.start(context, ProtoMessage.Cmd.cmdAnsRemoteControl.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AnsRemoteControlProcesser.ACTION);
        new TimeoutBroadcast(context, filter,  ((MyService) context).getBroadcastManager()).startReceiver(15, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("tttt", "获得应答遥控指令 连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("tttt", "获得应答遥控指令");
                } else {
                    Log.i("tttt", "获得应答遥控指令错误码");
                }
            }
        });
    }

    @Override
    public void onSent() {

    }
}
