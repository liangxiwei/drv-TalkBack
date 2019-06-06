package com.example.jrd48.chat.SQLite;

/**
 * Created by Administrator on 2018/9/3.
 */

public class CommonUtil {
    public static String makeNotNull(String x){
        if (x==null){ return""; }
        return x;
    }

    public static int makeNotNull(Integer x){
        return x==null?0:x;
    }
}
