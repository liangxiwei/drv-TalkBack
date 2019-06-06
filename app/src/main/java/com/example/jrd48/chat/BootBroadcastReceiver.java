package com.example.jrd48.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 实现该app开机自动运行
 *
 * @author luo
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    /**
     * 可以实现开机自动打开软件并运行。
     */
    @Override
    public void onReceive(Context context, Intent intent) {

      /*
      try {
            SettingRW mSettings = new SettingRW(context);
            mSettings.load();
            Log.i("jim", "开机是否启动珞宾对讲：" + mSettings.isAutoStart());
            if (mSettings.isAutoStart() && intent.getAction().equals(ACTION)) {
                Intent mBootIntent = new Intent(context, WelcomeActivity.class);
                // 下面这句话必须加上才能开机自动运行app的界面
                mBootIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mBootIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}  