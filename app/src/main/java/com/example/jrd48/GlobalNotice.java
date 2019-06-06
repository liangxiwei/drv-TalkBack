package com.example.jrd48;

import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by qhb on 17-1-16.
 */

public class GlobalNotice {

    static List<AppliedFriends> appliedFriends = new ArrayList<>();

    public synchronized static void setNotice(AppliedFriendsList list) {
        if (list != null && list.getAppliedFriends().size() > 0) {
            appliedFriends.clear();
            for (AppliedFriends apply : list.getAppliedFriends()) {
                appliedFriends.add(apply);
            }
        }
    }

    public synchronized static void clearAppliedFriends() {
        appliedFriends.clear();
    }

    public synchronized static void clearAppliedFriends(String phoneNumber) {
        Iterator<AppliedFriends> iterator = appliedFriends.iterator();
        while (iterator.hasNext()) {
            AppliedFriends k = iterator.next();
            if (phoneNumber != null && phoneNumber.equals(k.getPhoneNum())) {
                iterator.remove();
                break;
            }
        }
    }

    public synchronized static List<AppliedFriends> getAppliedFriends() {
        return appliedFriends;
    }

    public synchronized static boolean isSameNumber(String phoneNumber) {
        for (AppliedFriends apply : appliedFriends) {
            if (phoneNumber.equals(apply.getPhoneNum())) {
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean isHasNumber() {
        return (appliedFriends.size() > 0);
    }
}
