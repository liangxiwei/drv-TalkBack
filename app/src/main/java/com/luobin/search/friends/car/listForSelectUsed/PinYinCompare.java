package com.luobin.search.friends.car.listForSelectUsed;

import com.example.jrd48.chat.ChineseToHanYuPYTest;

public class PinYinCompare {
    public static int compare(String sz1, String sz2) {
        String x = sz1;
        String y = sz2;
        if (x == null) {
            x = "";
        }
        if (y == null) {
            y = "";
        }

        String hzX = ChineseToHanYuPYTest.convertChineseToPinyin(x, false);
        String hzY = ChineseToHanYuPYTest.convertChineseToPinyin(y, false);
        if (hzX == null) {
            hzX = x;
        }
        if (hzY == null) {
            hzY = y;
        }
        return hzX.compareTo(hzY);
    }
}
