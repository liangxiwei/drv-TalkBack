package com.example.jrd48.chat.VersionUpdate;

import android.app.Activity;

/**
 * Created by Administrator on 2017/1/13.
 */

public class MyGetActivity {
    public static Activity getParent(Activity frag) throws GetActivityNullException {
//        if (frag==null){
//            throw new RuntimeException("Activity is null");
//        }

        Activity act = frag;
        if (act == null) {
            throw new GetActivityNullException("Activity  is null");
        }
        return act;
    }
}