package com.example.jrd48.chat.filemanagement;


import com.example.jrd48.chat.Msg;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/6/6.
 */

public class TimeComparator implements Comparator {
    private String type;

    public TimeComparator(String type) {
        this.type = type;
    }

    @Override
    public int compare(Object o1, Object o2) {
        Msg contact1 = (Msg) o1;
        Msg contact2 = (Msg) o2;

        String str = contact1.getAllTime();
        String str3 = contact2.getAllTime();
        int flag;
        if (type.equals("order")) {
            flag = str.compareTo(str3);
        } else {
            flag = str3.compareTo(str);
        }


        return flag;
    }

}
