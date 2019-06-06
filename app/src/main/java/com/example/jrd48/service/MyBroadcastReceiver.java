package com.example.jrd48.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Object oTemp = intent.getSerializableExtra("param");
			if (oTemp != null) {
				String str = (String) oTemp;
				onReceiveParam(str);
			} else {
				// Log.d("COM.LUOBIN.TOOLS.BROADCATRECEIVER", "PARAM is null");
				onReceiveParam(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	abstract protected void onReceiveParam(String str);

}
