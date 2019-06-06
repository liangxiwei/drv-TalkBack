package com.example.jrd48.chat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.ShowAllTeamMemberActivity;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by quhuabo on 2017/1/19 0019.
 */

public class GlobalImg {
    private static Map<String, Bitmap> mImages = new HashMap<String, Bitmap>();

    public static Bitmap getImage(Context context, String phone) {

        Bitmap b = mImages.get(phone);

        if (b == null) {
            b = FriendFaceUtill.getUserFace(context, phone);
            if (b != null) {
                mImages.put(phone, b);
            }
        }
        if(b == null && !phone.contains("team")){
            getUserFace(phone);
        }
        return b;
    }


    public static void getUserFace(String memberName) {
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(memberName);
        builder.setOnlyPhoneNum(true);
        MyService.start(MyApplication.getContext(), ProtoMessage.Cmd.cmdSearchUser.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchFriendProcesser.ACTION);
        new TimeoutBroadcast(MyApplication.getContext(), filter,new TimeoutBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriends aply = i.getParcelableExtra("search_user");
                    if (aply.getPhoneNum() == null || aply.getPhoneNum().length() <= 0) {
//                        ToastR.setToast(mContext, "未找到该用户");
                    } else {
//                        msgDB_Team();
                    }
                } else {
                    Log.e("jim","globaling userFace  code:"+i.getIntExtra("error_code", -1));
                  //  new ResponseErrorProcesser(MyApplication.getContext(), i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public static void reloadImg(Context context, String phone) {
        Bitmap b = FriendFaceUtill.getUserFace(context, phone);
        mImages.put(phone, b);

    }
    public static void clear() {
//        try {
//            throw new Exception("[Warning] clear all images");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Log.w("GlobalImg","[Warning] clear all images");
        mImages.clear();
    }

}
