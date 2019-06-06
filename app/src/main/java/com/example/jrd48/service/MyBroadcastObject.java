package com.example.jrd48.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class MyBroadcastObject extends StartableObject {
	protected boolean bRegistered = false;
	protected Context context;

	private String actionName = "";
	private String packageName = "";
	private MyBroadcastReceiver mReceiver;

	public MyBroadcastObject(Context context) {
		this.context = context;
	}

	public MyBroadcastReceiver getReceiver() {
		return mReceiver;
	}

	public void setReceiver(MyBroadcastReceiver mReceiver) {
		this.mReceiver = mReceiver;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * 发送广播
	 * 
	 * @param context
	 * @param response
	 *            json 字符串
	 */
	public void sendBroadcast(String response) {
		if (context == null) {
			Log.e("MOBASSIST", this.getClass().getName() + ".context is null!");
			return;
		}

		Intent intent = new Intent();
		intent.setPackage(packageName);
		intent.setAction(actionName);
		intent.putExtra("param", response);
		context.sendBroadcast(intent);
	}

	@Override
	protected void onStart() {
		try {
			context.registerReceiver(mReceiver, new IntentFilter(actionName));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onStop() {
		try {
			if (mReceiver != null) {
				context.unregisterReceiver(mReceiver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
