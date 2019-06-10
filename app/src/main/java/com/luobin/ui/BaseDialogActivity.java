package com.luobin.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

import com.example.jrd48.chat.ActivityCollector;
import com.example.jrd48.service.TimeoutBroadcastManager;

/**
 * @author wangjunjie
 */
public class BaseDialogActivity extends Activity{
    public static final int REQUEST_TIME_OUT = 15;
    private TimeoutBroadcastManager mTimeoutBroadcastManager = new TimeoutBroadcastManager();

    public TimeoutBroadcastManager getBroadcastManager() {
        return mTimeoutBroadcastManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Intent intent = new Intent(this, MyService.class);
        intent.putExtra("heart", true);
		startService(intent);*/
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
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

    protected void onDestroy() {
        ActivityCollector.removeAct(this);
        super.onDestroy();
    }

}
