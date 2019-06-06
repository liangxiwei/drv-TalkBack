package com.luobin.tool;

import android.util.Log;

import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/2/5.
 */

public class OnlineSetTool {
    //好友在线状态
    public static Set<String> onlineSet = new HashSet<String>();
    public static synchronized void add(String phone){
        onlineSet.add(phone);
    }
    public static synchronized void remove(String phone){
        onlineSet.remove(phone);
    }
    public static synchronized void addList(List<ProtoMessage.FriendStatus> phoneList){
        if (phoneList != null) {
            for (ProtoMessage.FriendStatus list:phoneList) {
                onlineSet.add(list.getPhoneNum());
            }
        } else {
            Log.e("jim","phoneList is null");
        }
    }
    public static synchronized Set<String> getOnlineSet(){
        return onlineSet;
    }
    public static synchronized void removeAll(){
        onlineSet.clear();
    }
}
