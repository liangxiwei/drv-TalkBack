package com.example.jrd48.chat;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    private static List<Activity> activities = new ArrayList<>();

    public static void addAct(Activity activity) {
        activities.add(activity);
    }

    public static void removeAct(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAct() {
        for (int i = 0; i < activities.size();) {
            Activity activity = activities.get(i);
            if (!activity.isFinishing() && !(activity instanceof com.luobin.dvr.ui.MainActivity)) {
                activity.finish();
            }

            if(!(activity instanceof com.luobin.dvr.ui.MainActivity)){
                activities.remove(activity);
            } else {
                i++;
            }
        }
    }

    public synchronized static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activities.clear();
    }
}
