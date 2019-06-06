package com.example.jrd48.chat.group.cache;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/1/22.
 */

public class DBTableName {
    public static String getTableName(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        return myPhone + name;
    }
}
