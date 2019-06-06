package com.example.jrd48.service.protocol;

import android.app.NotificationManager;
import android.content.Context;

import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;

/**
 * Created by Administrator on 2017/4/17.
 */

public class CancelNotify {
    Context context;

    public CancelNotify(Context context, long groupId, NotificationManager manager, String phone) {
        this.context = context;
        cancelNotify(groupId, manager, phone);
    }

    private void cancelNotify(long groupId, NotificationManager manager, String phone) {
        String str = (String) SharedPreferencesUtils.get(context, ReceiverProcesser.KEY, "");
        String notifyTitle = getTitleName(groupId, phone);
        if (str.equals(notifyTitle) && str.length() > 0) {
            manager.cancel(0);
        }
    }

    private String getTitleName(long groupId, String phone) {
        String notifyTitle = "";
        if (groupId == 0) {
            DBManagerFriendsList dbM = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
            notifyTitle = dbM.getFriendName(phone);
            dbM.closeDB();
        } else {
            DBManagerTeamList dbT = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
            notifyTitle = dbT.getTeamName(groupId);
            dbT.closeDB();
        }
        return notifyTitle;
    }
}
