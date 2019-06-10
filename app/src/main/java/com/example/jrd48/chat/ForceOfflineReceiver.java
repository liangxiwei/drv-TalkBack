package com.example.jrd48.chat;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.dvr.DvrService;
import com.luobin.ui.LoginActivity;

import me.lake.librestreaming.client.RESClient;

//import android.view.WindowManager;
//import android.content.DialogInterface;
//import android.app.AlertDialog;

public class ForceOfflineReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
//		//删除缓存的好友信息
//		DBHelperFriendsList db = new DBHelperFriendsList(context ,DBTableName.getTableName(context, DBHelperFriendsList.NAME));
//		db.deleteDatabase(DBTableName.getTableName(context, DBHelperFriendsList.NAME));
//		//删除缓存的群组信息
//		DBHelperTeamList teamHelper = new DBHelperTeamList(context, DBTableName.getTableName(context, DBHelperTeamList.NAME));
//		teamHelper.deleteDatabase(DBTableName.getTableName(context, DBHelperTeamList.NAME));

		SharedPreferences preferences=context.getSharedPreferences("token", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=preferences.edit();
		editor.putString("token", "");
		editor.putString("phone", "");
		editor.putString("name", "");
		editor.apply();
        NotificationManager nm = (NotificationManager) (context.getSystemService(context.NOTIFICATION_SERVICE));
        nm.cancel(-1);//消除对应ID的通知
		//new SettingRW(context).reDefault();
		if(GlobalStatus.getCurViewPhone() != null) {
			DvrService.start(context, RESClient.ACTION_STOP_RTMP, null);
			GlobalStatus.setCurViewPhone(null);
		}
		SharedPreferencesUtils.put(context, "data_init", true);
		ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
		MyService.start(context, ProtoMessage.Cmd.cmdLogout.getNumber(), builder.build());
		MyService.restart(context);
		ActivityCollector.finishAct();
		GlobalStatus.clearChatRoomMsg();
		GlobalStatus.setOldChat(0,"",0);
		Intent i= new Intent(context, LoginActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		if(intent.getBooleanExtra("toast",true)) {
			ToastR.setToast(context, "账户被强制下线！");
		}
		//弹出对话框版本的强制下线：↓
		/*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle("退出");
		dialogBuilder.setMessage("您已经点击退出，您的用户已经登出，请重新登录！");
		dialogBuilder.setCancelable(false);
		dialogBuilder.setPositiveButton("确定", 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						ActivityCollector.finishAct();
						Intent intent = new Intent(context, LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
					}
				});
		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();*/
		
	}

}

