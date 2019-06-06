package com.example.jrd48.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FileTransfer.TransferService;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.tool.OnlineSetTool;
import com.qihoo.linker.logcollector.LogCollector;

import me.lake.librestreaming.client.RESClient;

public class ConnectionChangeReceiver extends BroadcastReceiver {
	public static final String TAG = "ConnectionChangeRec";
	public static final int TYPE_WARNING = 0x1;
	public static final int TYPE_LIMIT = 0x2;
	public static final int TYPE_LIMIT_SNOOZED = 0x3;
	public static final String NETWORK_CHANGE_ACTION = "com.example.jrd48.service.action.ConnectionChangeReceiver";
	public static final String NETWORK_CHANGE_KEY = "network";
	Handler mHandler = new Handler();
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("wsDvr","ConnectionChangeReceiver onReceive:" + intent.getAction());
		if (intent.getAction().equals("android.intent.action.ANY_DATA_STATE")) {
			try {
				checkNet(context);
//				Intent intent1 = new Intent(MainActivity.CHECK_CONNET);
//				intent1.putExtra("connect", bConnected);
//				context.sendBroadcast(intent1);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (intent.getAction().equals("android.intent.action.USER_PRESENT")){
			try {
				//boolean bConnected = ConnUtil.isConnected(context);
				//if (bConnected) {
					// 发送心跳包
				if(GlobalStatus.isReceiveLauncher()) {
					Intent i = new Intent(context, MyService.class);
					i.putExtra("heart", "");
					context.startService(i);
				}
				//}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(intent.getAction().equalsIgnoreCase("com.erobbing.NetworkPolicyManagerService")){
			int type = intent.getIntExtra("type",TYPE_WARNING);
			Log.v("wsDvr","NetworkPolicyManagerService type onReceive:" + type);
			GlobalStatus.setPolicyStatus(type);
			switch (type){
				case TYPE_LIMIT:
					disconnectHandle(context);
					break;
				case TYPE_LIMIT_SNOOZED:
				case TYPE_WARNING:
				default:
					checkNet(context);
					break;
			}
		}
	}

	public void checkNet(final Context context){
		boolean bConnected = false;
		switch (GlobalStatus.getPolicyStatus()){
			case TYPE_LIMIT:
				bConnected = false;
				break;
			case TYPE_LIMIT_SNOOZED:
			case TYPE_WARNING:
			default:
				bConnected = true;
				break;
		}
		Log.d(TAG, "checkNet.GlobalStatus.getPolicyStatus() = " + GlobalStatus.getPolicyStatus() + " -- bConnected = " + bConnected);
		if(bConnected) {
			bConnected = ConnUtil.isConnected(context);
		}

		if (bConnected) {
			Intent i = new Intent(context, MyService.class);
			LogCollector.upload(false);
			context.startService(i);
		} else {
			disconnectHandle(context);
		}
		if (!bConnected) {
			OnlineSetTool.removeAll();
			Intent intent = new Intent(NETWORK_CHANGE_ACTION);
			intent.putExtra(NETWORK_CHANGE_KEY, ConnUtil.isConnected(context));
			context.sendBroadcast(intent);
		}

	}

	public void disconnectHandle(Context context){

		if (GlobalStatus.msg_null()) {
			ToastR.setToast(context, "网络断开，对讲暂停");//receiver
			Log.d(TAG, "disconnectHandle--network disconnect, stop chating");
			Intent i = new Intent(AutoCloseProcesser.ACTION);
			i.putExtra("roomID", GlobalStatus.getRoomID());
			context.sendBroadcast(i);
			GlobalStatus.clearChatRoomMsg();
			NotificationManager nm = (NotificationManager) (context.getSystemService(context.NOTIFICATION_SERVICE));
			nm.cancel(-1);//消除对应ID的通知
		}

		if(!TextUtils.isEmpty(GlobalStatus.getCurViewPhone())) {
			DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
			ToastR.setToast(context, "网络断开，路况分享停止");
			Log.d(TAG, "disconnectHandle--network disconnect, stop route sharing");
			GlobalStatus.setCurViewPhone(null);
		}
		Log.d("chat", "网络断开");
		Intent i = new Intent(context, MyService.class);
		context.stopService(i);

		Intent i2 = new Intent(context, TransferService.class);
		context.stopService(i2);
	}
}
