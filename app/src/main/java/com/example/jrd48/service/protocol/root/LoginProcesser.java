package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ConnectionChangeReceiver;
import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.RestartLocationBroadcast;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.luobin.tool.OnlineSetTool;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class LoginProcesser extends CommonProcesser{
    public final static String ACTION = "ACTION.LoginProcesser";
    private String[] sexItems = new String[]{"", "男", "女", "未设置"};
    public LoginProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "got login answer: ");
        try {
            ProtoMessage.LoginResponse resp = ProtoMessage.LoginResponse.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("chat", "got resp code: "+resp.getErrorCode());
            Log.i("chat", "token = "+resp);
            Intent i = new Intent(ACTION);
            int errorCode = resp.getErrorCode();

            try {
                if (resp == null) {
                    throw new Exception("unknown response.");
                }
                i.putExtra("error_code", errorCode);
                if (resp.getErrorCode() == ProtoMessage.ErrorCode.OK.getNumber()) {
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("token", resp.getToken());
                    editor.putString("phone", resp.getPhone());
                    editor.apply();

                    ProtoMessage.CommonMsgRequest.Builder builder = ProtoMessage.CommonMsgRequest.newBuilder();
                    MyService.start(context, ProtoMessage.Cmd.cmdGetMsg.getNumber(), builder.build());

                    // 登录成功以后
                    new RestartLocationBroadcast(context).sendBroadcast("");

                    //启动定位服务
                    ServiceCheckUserEvent.restart(context);

                    Infor();
                    initFriendList();

                    loadFriendStatus();
                    //发送获取在线状态广播
                    Intent intent = new Intent(ConnectionChangeReceiver.NETWORK_CHANGE_ACTION);
                    intent.putExtra(ConnectionChangeReceiver.NETWORK_CHANGE_KEY,true);
                    context.sendBroadcast(intent);
                }
                i.putExtra("token", resp.getToken());
            } catch (Exception e) {
                e.printStackTrace();
                i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
            }
            context.sendBroadcast(i);

            // TODO: 0 OK，其他值，失败
            //
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }



    private void initFriendList() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdGetFriendList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsListProcesser.ACTION);
        new TimeoutBroadcast(context, filter,  ((MyService) context).getBroadcastManager()).startReceiver(15, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("downFriendsThread", "连接超时");
            }

            @Override
            public void onGot(Intent i) {

                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("downFriendsThread", "获取好友成功");
                    initTeamList();
                } else {
                    Log.i("downFriendsThread", "获取好友失败");
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void initTeamList() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("downGroupThread", "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_group_list");
                    try {
                        SharedPreferencesUtils.put(context, "data_init", true);
                        Log.i("downFriendsThread", "获取群组成功");
//                        MyService.restart(context);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("downFriendsThread", "获取群组失败");
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    //***********************************读取个人信息*************************************
    private void Infor() {
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String str = preferences.getString("name", "");
        String myPhone = preferences.getString("phone", "");

        String address = ReceiverProcesser.getMyDataRoot(context) + "/" + myPhone + "/";
        String img_path = address + "head2.jpg";
        getInfor();
//        if (str == null || str.equals("")) {
//            getInfor();
//        } else {
//            Bitmap bmp = BitmapFactory.decodeFile(img_path);
////            if (bmp == null) {
////                headimage.setImageResource(R.drawable.default_useravatar);
////            } else {
////                headimage.setImageBitmap(bmp);
////            }
////            name.setText(str);
//        }
    }

    //***********************************获取个人信息*************************************
    private void getInfor() {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdGetMyInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserInfoProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    String myPhone;
                    SharedPreferences preferences1 = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    myPhone = preferences1.getString("phone", "");
                    String address = ReceiverProcesser.getMyDataRoot(context) + "/" + myPhone + "/";
                    String img_path = address + "head2.jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(img_path);
                    String myName = i.getStringExtra("name");
//                    if (bmp == null) {
//                        headimage.setImageResource(R.drawable.default_useravatar);
//                    } else {
//                        headimage.setImageBitmap(bmp);
//                    }
//                    name.setText(myName);
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("name", i.getStringExtra("name"));
                    int sexdata = i.getIntExtra("sex", 0);
                    editor.putString("sex", sexItems[sexdata]);
                    editor.apply();
//                    if (myPhone.equals(myName)) {
//                        Intent intent = new Intent(context, MyInforActivity.class);
//                        startActivity(intent);
//                        ToastR.setToast(context, "请点击姓名，设置你的名字");
//                    }
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }

            }
        });
    }

    //获取好友状态
    private void loadFriendStatus() {

        if (ConnUtil.isConnected(context)) {
            Log.d("drv", "获取全部好友在线状态");
            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
            MyService.start(context, ProtoMessage.Cmd.cmdGetFriendsStatus.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(GetFriendsStatusProcesser.ACTION);
            new TimeoutBroadcast(context, filter, ((MyService) context).getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

                @Override
                public void onTimeout() {
                    Log.e("drv","获取在线好友超时");
                    //  ToastR.setToast(context, "连接超时");
                }

                @Override
                public void onGot(Intent i) {

                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                        ProtoMessage.MsgFriendsStatus re =
                                (ProtoMessage.MsgFriendsStatus) i.getSerializableExtra(GetFriendsStatusProcesser.STATUS_KEY);
                        OnlineSetTool.addList(re.getFriendsList());
                    } else {
                        Log.e("drv","error code: "+code);
                    }
                }
            });
        }
    }

}
