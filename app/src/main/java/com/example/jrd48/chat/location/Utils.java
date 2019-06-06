package com.example.jrd48.chat.location;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.example.jrd48.GlobalStatus;

public class Utils {

    /**
     * 判断进程是否运行
     *
     * @return
     */
    public static boolean isProcessRunning(Context context, String proessName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (RunningAppProcessInfo info : lists) {
            if (info.processName.compareToIgnoreCase(proessName) == 0) {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

    public static String RandCode(int n) {

        char[] arrChar = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        StringBuilder num = new StringBuilder();

        Random rnd = new Random(new Date().getTime());

        for (int i = 0; i < n; i++) {

            num.append(arrChar[rnd.nextInt(arrChar.length)]);
        }
        return num.toString();
    }

    public static String RandImei() {
        return RandCode(15);
    }

    public static boolean getTopActivity(Context mContext){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if(taskInfo != null && taskInfo.size() > 0) {
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            Log.e("wsDvr", "componentInfo.getClassName();" + componentInfo.getClassName());
            switch (componentInfo.getClassName()) {
                case "com.example.jrd48.chat.FirstActivity":
                case "com.luobin.ui.GroupActivity":
                case "com.luobin.ui.ContactsActivity":
                case "com.example.jrd48.chat.group.ShowAllTeamMemberActivity":
                    if(GlobalStatus.isIsFirstPause()){
                        return false;
                    }
                    return true;
                default:
                    break;

            }
        }
        return false;
    }

    public static boolean getFirstActivity(Context mContext){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if(taskInfo != null && taskInfo.size() > 0) {
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            Log.e("wsDvr", "componentInfo.getClassName();" + componentInfo.getClassName());
            switch (componentInfo.getClassName()) {
                case "com.example.jrd48.chat.FirstActivity":
                    if(GlobalStatus.isIsFirstPause()){
                        return false;
                    }
                    return true;
                default:
                    break;

            }
        }
        return false;
    }

    public static boolean getDvrTopActivity(Context mContext){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if(taskInfo != null && taskInfo.size() > 0) {
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            Log.e("wsDvr","getDvrTopActivity componentInfo.getClassName();" + componentInfo.getClassName());
            switch (componentInfo.getClassName()){
                case "com.luobin.dvr.ui.MainActivity":
                    return true;
                default:
                    break;

            }
        }
        return false;
    }
    public static boolean getRoadVideoActivity(Context mContext){
        ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if(taskInfo != null && taskInfo.size() > 0) {
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            Log.e("wsDvr","getRoadVideoActivity componentInfo.getClassName();" + componentInfo.getClassName());
            switch (componentInfo.getClassName()){
                case "com.luobin.ui.OtherVideoSetting":
                case "com.video.VideoCallActivity":
                    return true;
                default:
                    break;

            }
        }
        return false;
    }
}
