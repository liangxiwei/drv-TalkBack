package com.example.jrd48.chat;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.example.jrd48.service.TimeoutBroadcastManager;
import com.luobin.utils.ButtonUtils;

public class BaseActivity extends AppCompatActivity {

	public static final int REQUEST_TIME_OUT = 15;
	private TimeoutBroadcastManager mTimeoutBroadcastManager = new TimeoutBroadcastManager();
	protected Context mContext;
	public TimeoutBroadcastManager getBroadcastManager() {
		return mTimeoutBroadcastManager;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mContext = this;
		String className = this.getPackageName();
		Log.d("BaseActivity",className);
		ActivityCollector.addAct(this);
		if (false) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects()
					.detectLeakedClosableObjects()
					.penaltyLog()
					.penaltyDeath()
					.build());
		}
	}

	@Override
	protected void onPause() {
		mTimeoutBroadcastManager.stopAll();
		super.onPause();
	}

	protected void onDestroy(){
		ActivityCollector.removeAct(this);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				ButtonUtils.changeLeftOrRight(true);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				ButtonUtils.changeLeftOrRight(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
