package com.luobin.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ConnectionChangeReceiver;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.LiveCallAnsProcesser;
import com.luobin.dvr.DvrService;
import com.qihoo.linker.logcollector.LogCollector;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.regex.Pattern;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by Administrator on 2017/8/24.
 */

public class VideoRoadUtils {
    public static void startUSBCamera() {
        Log.i("wsDvr", "startUSBCamera");
        File file = new File("/dev/video2");
        if(file.exists()){
            Log.i("wsDvr", "startUSBCamera /dev/video2 exist return");
            return;
        }
        try {
            Class SystemService = Class.forName("android.os.SystemService");
            Method method = SystemService.getMethod("start", String.class);
            method.invoke(SystemService.newInstance(), "usbcam_start");
//            ToastR.setToast(MyApplication.getContext(),"startUSBCamera sucess");
        } catch (Exception e) {
            e.printStackTrace();
//            ToastR.setToast(MyApplication.getContext(),"startUSBCamera failed");
            Log.e("wsDvr", "startUSBCamera failed");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Log.e("wsDvr", "VideoRoadUtils catch startUSBCamera");
            if (GlobalStatus.getShutDownType(MyApplication.getContext()) == 1) {
                startUSBCamera();
            }
        }
    }

    public static synchronized void stopUSBCamera() {
        Log.i("wsDvr", "stopUSBCamera");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        Log.i("wsDvr", "stopUSBCamera status="+GlobalStatus.getShutDownType(MyApplication.getContext()));
        if (GlobalStatus.getShutDownType(MyApplication.getContext()) == 1) {
            return;
        }
        File file = new File("/dev/video2");
        if(!file.exists()){
            Log.i("wsDvr", "stopUSBCamera /dev/video2 not exist return");
            return;
        }
        try {
            Class SystemService = Class.forName("android.os.SystemService");
            Method method = SystemService.getMethod("start", String.class);
            method.invoke(SystemService.newInstance(), "usbcam_stop");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("wsDvr", "stopUSBCamera failed");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            stopUSBCamera();
        }
        Log.i("wsDvr", "stopUSBCamera end");
    }

    public static void AcceptLiveCall(final Context context, final String phone) {
        UUID uuid = UUID.randomUUID();
        final String radom = uuid.toString();
        ProtoMessage.LiveVideoCallAns.Builder builder = ProtoMessage.LiveVideoCallAns.newBuilder();
        builder.setAccept(ProtoMessage.AcceptType.atAccept);
        builder.setPhone(phone);
        builder.setVideoUrl(radom);
        Log.v("wsDvr", "AcceptLiveCall phone:" + phone);
        MyService.start(context, ProtoMessage.Cmd.cmdLiveCallAns.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveCallAnsProcesser.ACTION);
        new TimeoutBroadcast(context, filter, new TimeoutBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "接收路况请求超时");
                Log.i("AcceptLiveCall", "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    String myPhone = preferences.getString("phone", "");
                    String selfAddr = null;
                    selfAddr = RESClient.SERVER_URL + myPhone + "---" + phone;
                    if(radom != null){
                        selfAddr = selfAddr + "_" + radom;
                    }
                    Log.v("wsDvr", "AcceptLiveCall selfAddr=" + selfAddr);
                    DvrService.start(context, RESClient.ACTION_START_RTMP, selfAddr, false);
                    GlobalStatus.addViewRoadPhone(phone);
                    GlobalStatus.setCurViewPhone(phone);
                    String showPhone = DBManagerFriendsList.getAFriendNickName(MyApplication.getContext(),phone);
                    if(Pattern.matches("\\d{11}",showPhone)){
                        showPhone = showPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                    }
                    ToastR.setToast(context, "正在向用户" + showPhone + "分享路况");
                } else {
                    Log.i("AcceptLiveCall", "接收失败");
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public static void DenyLiveCall(final Context context, final String phone) {

        boolean bConnected = false;
        switch (GlobalStatus.getPolicyStatus()) {
            case ConnectionChangeReceiver.TYPE_LIMIT:
                bConnected = false;
                break;
            case ConnectionChangeReceiver.TYPE_LIMIT_SNOOZED:
            case ConnectionChangeReceiver.TYPE_WARNING:
            default:
                bConnected = true;
                break;
        }
        if (bConnected) {
            bConnected = ConnUtil.isConnected(context);
        }
        if (!bConnected) {
            return;
        }

        ProtoMessage.LiveVideoCallAns.Builder builder = ProtoMessage.LiveVideoCallAns.newBuilder();
        builder.setAccept(ProtoMessage.AcceptType.atDeny);
        builder.setPhone(phone);
        MyService.start(context, ProtoMessage.Cmd.cmdLiveCallAns.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveCallAnsProcesser.ACTION);
        new TimeoutBroadcast(context, filter, new TimeoutBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                //ToastR.setToast(context, "拒绝路况请求超时");
                Log.i("DenyLiveCall", "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                } else {
                    Log.i("DenyLiveCall", "失败");
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }
}
