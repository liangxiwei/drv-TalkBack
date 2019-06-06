package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.RestartLocationBroadcast;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Administrator on 2017/8/24.
 */

public class ScanCarOkProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.ScanCarOkProcesser";

    public ScanCarOkProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.LoginResponse resp = ProtoMessage.LoginResponse.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.e("ScanCarOkProcesser",resp.toString());
            Intent i = new Intent(ACTION);
            int errorCode = resp.getErrorCode();

            try {
                if (resp == null) {
                    throw new Exception("unknown response.");
                }
                i.putExtra("error_code", errorCode);
                if (resp.getErrorCode() == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ProtoMessage.UserLogin.Builder builder = ProtoMessage.UserLogin.newBuilder();
                    String imei = ConnUtil.getDeviceId(context);
                    if(TextUtils.isEmpty(imei) || imei.length() < 10){
                        imei = "12345678901234";
                    }
                    builder.setPhoneNum(resp.getPhone());
                    builder.setToken(resp.getToken());
                    builder.setCarID(imei);
                    builder.setAppType(ProtoMessage.AppType.appCar_VALUE);
                    MyService.start(context, ProtoMessage.Cmd.cmdLogin.getNumber(), builder.build());

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(LoginProcesser.ACTION);
                    TimeoutBroadcast x = new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager());

                    x.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                        @Override
                        public void onTimeout() {
                            MyService.restart(context);
                            ToastR.setToast(context, "超时");
                        }

                        @Override
                        public void onGot(Intent i) {
                            if (i.getIntExtra("error_code", 0) ==
                                    ProtoMessage.ErrorCode.LOGIN_PHONE_OR_PASS_WRONG_VALUE
                                    || i.getIntExtra("error_code", 0) ==
                                    ProtoMessage.ErrorCode.LOGIN_TOKEN_WRONG_VALUE
                                    || i.getIntExtra("error_code", 0) ==
                                    ProtoMessage.ErrorCode.LOGIN_TOKEN_NOT_EXIST_VALUE) {
                                ToastR.setToast(context, "连接失败");
                                Intent intent = new Intent("com.example.jrd48.chat.FORCE_OFFLINE");//测试强制下线功能
                                context.sendBroadcast(intent);
                            } else if (i.getIntExtra("error_code", 0) ==
                                    ProtoMessage.ErrorCode.OK_VALUE) {
                            }
                        }
                    });

                }
                i.putExtra("token", resp.getToken());
            } catch (Exception e) {
                e.printStackTrace();
                i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
            }
            context.sendBroadcast(i);

            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {

    }
}
