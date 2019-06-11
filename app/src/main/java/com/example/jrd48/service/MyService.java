package com.example.jrd48.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.WelcomeActivity;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.chat.sim.SimInfoService;
import com.example.jrd48.service.Socket.SocketRunable;
import com.example.jrd48.service.parser.ByteBufferLE;
import com.example.jrd48.service.parser.DiagramParser;
import com.example.jrd48.service.parser.Item;
import com.example.jrd48.service.parser.ParserListener;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.example.jrd48.service.protocol.Data;
import com.example.jrd48.service.protocol.root.AcceptFriendProcesser;
import com.example.jrd48.service.protocol.root.AcceptGroupProcesser;
import com.example.jrd48.service.protocol.root.AnsRemoteControlProcesser;
import com.example.jrd48.service.protocol.root.AppliedGroupListProcesser;
import com.example.jrd48.service.protocol.root.AppliedListProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;
import com.example.jrd48.service.protocol.root.AssignTeamAdminProcesser;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.BBSListProcesser;
import com.example.jrd48.service.protocol.root.CarRegisterProcesser;
import com.example.jrd48.service.protocol.root.ChangePasswordProcesser;
import com.example.jrd48.service.protocol.root.ChangeTeamMemberNickNameProcesser;
import com.example.jrd48.service.protocol.root.CloseRoomProcesser;
import com.example.jrd48.service.protocol.root.CreateGroupProcesser;
import com.example.jrd48.service.protocol.root.DeleteFriendProcesser;
import com.example.jrd48.service.protocol.root.DeleteTeamMemberProcesser;
import com.example.jrd48.service.protocol.root.DismissTeamProcesser;
import com.example.jrd48.service.protocol.root.DownloadProcesser;
import com.example.jrd48.service.protocol.root.FriendLocationChangedProcesser;
import com.example.jrd48.service.protocol.root.FriendsListProcesser;
import com.example.jrd48.service.protocol.root.GetCarLocationProcesser;
import com.example.jrd48.service.protocol.root.GetFriendInfoProcesser;
import com.example.jrd48.service.protocol.root.GetFriendLocationProcesser;
import com.example.jrd48.service.protocol.root.GetFriendsStatusProcesser;
import com.example.jrd48.service.protocol.root.GetHistoryMsgProcesser;
import com.example.jrd48.service.protocol.root.GetTeamMemberListProcesser;
import com.example.jrd48.service.protocol.root.GotRemoteControlProcesser;
import com.example.jrd48.service.protocol.root.GotSpeakMsgProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.HeartSend;
import com.example.jrd48.service.protocol.root.IccidGetOrSetProcesser;
import com.example.jrd48.service.protocol.root.LiveCallAnsProcesser;
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.example.jrd48.service.protocol.root.LoginProcesser;
import com.example.jrd48.service.protocol.root.MatchContactsProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamInfoProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamMemberPriorityProcesser;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.ReportLocationProcesser;
import com.example.jrd48.service.protocol.root.ScanCarOkProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.SearchGroupProcesser;
import com.example.jrd48.service.protocol.root.SearchStrangerProcesser;
import com.example.jrd48.service.protocol.root.SendMsgProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.example.jrd48.service.protocol.root.SetUserInfoProcesser;
import com.example.jrd48.service.protocol.root.SmsProcesser;
import com.example.jrd48.service.protocol.root.SpeakerBeginProcesser;
import com.example.jrd48.service.protocol.root.SpeakerEndProcesser;
import com.example.jrd48.service.protocol.root.SpeakerMsgProcesser;
import com.example.jrd48.service.protocol.root.StopGetLocationProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.example.jrd48.service.protocol.root.TrackListProcesser;
import com.example.jrd48.service.protocol.root.TypeSearchFriendsProcesser;
import com.example.jrd48.service.protocol.root.UploadProcesser;
import com.example.jrd48.service.protocol.root.UserInfoProcesser;
import com.example.jrd48.service.protocol.root.UserRegProcesser;
import com.example.jrd48.service.protocol.root.VoiceAcceptProcesser;
import com.example.jrd48.service.protocol.root.VoiceStartProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.log.DBMyLogHelper;
import com.luobin.log.LogCode;
import com.luobin.voice.SoundPoolTool;
import com.luobin.voice.VoiceHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.lake.librestreaming.client.RESClient;

import static com.amap.api.col.sln3.dj.m;

public class MyService extends Service {
    private static final String TAG = "MyService";
    private final static int GET_DEVICE_ID_DELAY_TIME = 3000;
    private final static int GET_DEVICE_ID_DELAY = 0x01;
    private static final String CHECK_HEART_RESP_ACTION = "check_heart_resp_action";
    private final static int HEART_INTERVAL = 4 * 60 * 1000;
    //    private MyConnectionBroadcast myConnectionBroadcast;
    private String myPhone;
    boolean isLocation;

    public TimeoutBroadcastManager mBroadcastManger = new TimeoutBroadcastManager();
    private SoundPoolTool mNotifyPlayer;

    public TimeoutBroadcastManager getBroadcastManager() {
        return mBroadcastManger;
    }

    VoiceHandler mVoiceHandler;
    PowerManager.WakeLock sCpuWakeLock;
    private SocketRunable mSocketRunable;
    private MyLogger mLog = MyLogger.jLog();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case GET_DEVICE_ID_DELAY:
                    GlobalStatus.setRetryCount(GlobalStatus.getRetryCount() + 1);
                    String imei = ConnUtil.getDeviceId(MyService.this);
                    if (TextUtils.isEmpty(imei) || imei.length() < 10) {
                        Message message = Message.obtain(mHandler, GET_DEVICE_ID_DELAY, null);
                        mHandler.sendMessageDelayed(message, GET_DEVICE_ID_DELAY_TIME);
                    } else {
                        register(imei);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private volatile boolean mSocketConnectStatus = false;
    /**
     * 定义协议命令处理方式
     */
    private Map<Integer, CommonProcesser> mCmdFuncs = new HashMap<Integer, CommonProcesser>() {
        {
            put(ProtoMessage.Cmd.cmdSmsCode.getNumber(), new SmsProcesser(MyService.this));  // 短信验证码
            put(ProtoMessage.Cmd.cmdReg.getNumber(), new UserRegProcesser(MyService.this));  // 注册
            put(ProtoMessage.Cmd.cmdLogin.getNumber(), new LoginProcesser(MyService.this));  // 登录
            put(ProtoMessage.Cmd.cmdChangePasswordBySms.getNumber(), new ChangePasswordProcesser(MyService.this));  // 修改密码

            put(ProtoMessage.Cmd.cmdApplyFriend.getNumber(), new ApplyFriendProcesser(MyService.this));  // 申请加好友
            put(ProtoMessage.Cmd.cmdAcceptFriend.getNumber(), new AcceptFriendProcesser(MyService.this));  // 接受加好友
            put(ProtoMessage.Cmd.cmdAppliedList.getNumber(), new AppliedListProcesser(MyService.this));  // 查询加有哪些User加自己好友
            put(ProtoMessage.Cmd.cmdGetFriendList.getNumber(), new FriendsListProcesser(MyService.this));  // 获取好友
            put(ProtoMessage.Cmd.cmdSearchUser.getNumber(), new SearchFriendProcesser(MyService.this));  // 搜索好友
            put(ProtoMessage.Cmd.cmdCreateTeam.getNumber(), new CreateGroupProcesser(MyService.this));  // 建群
            put(ProtoMessage.Cmd.cmdApplyTeam.getNumber(), new ApplyGroupProcesser(MyService.this));  // 加群
            put(ProtoMessage.Cmd.cmdAppliedTeamList.getNumber(), new AppliedGroupListProcesser(MyService.this));  // 查询给我的群加入列表（可批准的，可接受的）
            put(ProtoMessage.Cmd.cmdSearchTeam.getNumber(), new SearchGroupProcesser(MyService.this));  // 搜索群
            put(ProtoMessage.Cmd.cmdAcceptTeam.getNumber(), new AcceptGroupProcesser(MyService.this));  // 接受加入群
            put(ProtoMessage.Cmd.cmdGetTeamList.getNumber(), new GroupsListProcesser(MyService.this));  // 读群列表
            put(ProtoMessage.Cmd.cmdGetBBSList.getNumber(), new BBSListProcesser(MyService.this));  // 读群海聊列表
            put(ProtoMessage.Cmd.cmdGetTrackList.getNumber(), new TrackListProcesser(MyService.this));  // 读群轨迹列表

            put(ProtoMessage.Cmd.cmdAssignTeamAdmin.getNumber(), new AssignTeamAdminProcesser(MyService.this));  // 指定或取消群中的某一个管理员
            put(ProtoMessage.Cmd.cmdDismissTeam.getNumber(), new DismissTeamProcesser(MyService.this));  // 解散群（仅群主可以操作）
            put(ProtoMessage.Cmd.cmdQuitTeam.getNumber(), new DismissTeamProcesser(MyService.this));  // 退出群
            put(ProtoMessage.Cmd.cmdDeleteTeamMember.getNumber(), new DeleteTeamMemberProcesser(MyService.this));  // 删除一个群成员（仅群主和管理员有效）
            put(ProtoMessage.Cmd.cmdDeleteFriend.getNumber(), new DeleteFriendProcesser(MyService.this));  // 删除好友
            put(ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), new TeamMemberProcesser(MyService.this));  // 获取群成员
            put(ProtoMessage.Cmd.cmdNotify.getNumber(), new NotifyProcesser(MyService.this));  // 通知（好友添加删除）
            put(ProtoMessage.Cmd.cmdGetMyInfo.getNumber(), new UserInfoProcesser(MyService.this));  // 获取用户信息
            put(ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), new SetUserInfoProcesser(MyService.this));  // 设置用户信息
            put(ProtoMessage.Cmd.cmdCommonMsg.getNumber(), new SendMsgProcesser(MyService.this));  // 发送消息
            put(ProtoMessage.Cmd.cmdGotCommonMsg.getNumber(), new ReceiverProcesser(MyService.this));  // 接受发送消息
            put(ProtoMessage.Cmd.cmdGetMsg.getNumber(), new GetHistoryMsgProcesser(MyService.this));  // 接受发送消息
            //      put(ProtoMessage.Cmd.cmdApplyTeam.getNumber(), new InviteJoinGroupProcesser(MyService.this));  // 邀请加入群组
            put(ProtoMessage.Cmd.cmdModifyTeamInfo.getNumber(), new ModifyTeamInfoProcesser(MyService.this));//修改群信息
            put(ProtoMessage.Cmd.cmdSetFriendInfo.getNumber(), new SetFriendInfoProcesser(MyService.this));//设置好友资料
            put(ProtoMessage.Cmd.cmdModifyTeamMemberPriority.getNumber(), new ModifyTeamMemberPriorityProcesser(MyService.this));//修改成员优先级
            put(ProtoMessage.Cmd.cmdChangeMyTeamName.getNumber(), new ChangeTeamMemberNickNameProcesser(MyService.this));//修改我在群中的名称
            put(ProtoMessage.Cmd.cmdGotSpeakMsg_VALUE, new GotSpeakMsgProcesser(MyService.this)); // 获得了语音数据
            put(ProtoMessage.Cmd.cmdStartVoice_VALUE, new VoiceStartProcesser(MyService.this)); // 发起呼叫
            put(ProtoMessage.Cmd.cmdAcceptVoice_VALUE, new VoiceAcceptProcesser(MyService.this)); // 接受呼叫（挂断）
            put(ProtoMessage.Cmd.cmdSpeakBegin_VALUE, new SpeakerBeginProcesser(MyService.this)); // 开始说话
            put(ProtoMessage.Cmd.cmdSpeakEnd_VALUE, new SpeakerEndProcesser(MyService.this)); // 结束说话
            put(ProtoMessage.Cmd.cmdSpeakMsg_VALUE, new SpeakerMsgProcesser(MyService.this)); // 结束说话
            put(ProtoMessage.Cmd.cmdAutoCloseRoom_VALUE, new AutoCloseProcesser(MyService.this)); // 关闭房间
            put(ProtoMessage.Cmd.cmdSearchPhoneContact.getNumber(), new MatchContactsProcesser(MyService.this)); // 搜索号码
            put(ProtoMessage.Cmd.cmdGetFriendInfo.getNumber(), new GetFriendInfoProcesser(MyService.this)); // 搜索新的好友
            put(ProtoMessage.Cmd.cmdUploadAttachment_VALUE, new UploadProcesser(MyService.this)); // 搜索新的好友
            put(ProtoMessage.Cmd.cmdDownloadAttachment_VALUE, new DownloadProcesser(MyService.this)); // 搜索新的好友
            put(ProtoMessage.Cmd.cmdCloseChatRoom_VALUE, new CloseRoomProcesser(MyService.this)); // 关闭房间
            put(ProtoMessage.Cmd.cmdStartGetLocation_VALUE, new GetFriendLocationProcesser(MyService.this)); //获取好友或团队位置
            put(ProtoMessage.Cmd.cmdLiveVideoCall_VALUE, new LiveVideoCallProcesser(MyService.this)); // 发起视频对讲
            put(ProtoMessage.Cmd.cmdStopGetLocation_VALUE, new StopGetLocationProcesser(MyService.this)); //停止获取好友或团队位置
            put(ProtoMessage.Cmd.cmdFriendLocationChanged_VALUE, new FriendLocationChangedProcesser(MyService.this)); //服务器推送好友位置
            put(ProtoMessage.Cmd.cmdReportLocation_VALUE, new ReportLocationProcesser(MyService.this)); // 汇报位置
            put(ProtoMessage.Cmd.cmdCarRegister_VALUE, new CarRegisterProcesser(MyService.this));
            put(ProtoMessage.Cmd.cmdLiveCallAns_VALUE, new LiveCallAnsProcesser(MyService.this));
            put(ProtoMessage.Cmd.cmdScanCarOk_VALUE, new ScanCarOkProcesser(MyService.this));
            put(ProtoMessage.Cmd.cmdSearchUser2_VALUE, new SearchStrangerProcesser(MyService.this)); // 按类型搜索好友
            put(ProtoMessage.Cmd.cmdSearchAround_VALUE, new GetCarLocationProcesser(MyService.this)); // 获取某个点周围车
            put(ProtoMessage.Cmd.cmdSearchCar_VALUE, new TypeSearchFriendsProcesser(MyService.this)); // 按类型搜索好友
            put(ProtoMessage.Cmd.cmdGetFriendsStatus_VALUE, new GetFriendsStatusProcesser(MyService.this)); // 获取在线好友
            put(ProtoMessage.Cmd.cmdIccidGetOrSet_VALUE, new IccidGetOrSetProcesser(MyService.this)); // 获取上传ICCID结果
            put(ProtoMessage.Cmd.cmdGetAllTeamMember.getNumber(), new GetTeamMemberListProcesser(MyService.this));//获取所有群的成员
            put(ProtoMessage.Cmd.cmdGotRemoteControl_VALUE, new GotRemoteControlProcesser(MyService.this)); // 接收到遥控指令
            put(ProtoMessage.Cmd.cmdAnsRemoteControl_VALUE, new AnsRemoteControlProcesser(MyService.this)); // 应答遥控指令
        }
    };
    private int mSentHeart = 0;
    private CheckHeartRespBroadcast mCheckHeartBroadcast;

    /**
     * 通讯线程
     */
    private Thread mSocketThread = null;

    public MyService() {
        Log.d("chatjrd", "MyService");
    }

    public static void start(Context context) {
        Log.d("chatjrd", "start(c)");
        Intent i = new Intent(context, MyService.class);
        context.startService(i);
    }

    public static void restart(Context context) {
        Log.d("chatjrd", "restart(c)");
        Intent i = new Intent(context, MyService.class);
        context.stopService(i);

        Intent i2 = new Intent(context, MyService.class);
        context.startService(i2);
    }

    /**
     * 发送网络请求
     *
     * @param context
     * @param cmd
     * @param p       必须是 ProtoMessage.proto 中定义的对象
     */
    public static void start(Context context, int cmd, com.google.protobuf.GeneratedMessageV3 p) {
//        Log.d("chatjrd", "start(c,c,p):cmp = " + cmd);
        if (cmd == ProtoMessage.Cmd.cmdAcceptVoice_VALUE) {
            GlobalStatus.setIsAcceptRooming(true);
        }
        Intent i = new Intent(context, MyService.class);
        if (p != null) {
            byte[] bTemp = DiagramParser.packData(cmd, p);
            i.putExtra("xxx", new SendDataParcelable(bTemp, cmd));
        }
        context.startService(i);
    }

    public static void start(Context context, SendDataParcelable p) {
        Log.d("chatjrd", "start(c,s)");
        Intent i = new Intent(context, MyService.class);
        if (p != null) {
            i.putExtra("xxx", p);
        }
        context.startService(i);
    }

    public static void start(Context context, boolean isPreviewing) {
        Log.d("chatjrd", "start(c,s)");
        Intent i = new Intent(context, MyService.class);
        i.putExtra("isPreviewing", isPreviewing);
        context.startService(i);
    }

    public VoiceHandler getVoiceHandler() {
        return mVoiceHandler;
    }

    void test() {
    }

    private ShutDownObserver shutDownObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        shutDownObserver = new ShutDownObserver(new Handler());
        shutDownObserver.startObserving();

        if (0 == GlobalStatus.getShutDownType(this) && !((boolean) SharedPreferencesUtils.get(MyService.this, "isScreenOn", false))) {
//            ToastR.setToastLong(this,"当前处于熄火状态");
            return;
        }
        test();
        Log.d("chatjrd", "onCreate");
        setServerForeground();
        mCheckHeartBroadcast = new CheckHeartRespBroadcast(this);
        mCheckHeartBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                mLog.i("检查心跳包返回结果...");
                if (mSentHeart == 1) {
                    mLog.i("检查心跳包返回结果 [ 失败 ]，将重启服务。。。");
                    MyService.restart(MyService.this);
                } else {
                    mLog.i("检查心跳包返回结果 [ ok ]");
                }
            }
        });
        mCheckHeartBroadcast.start();

        mVoiceHandler = new VoiceHandler(this, mHandler);
        mSocketRunable = new SocketRunable(this, mHandler);

        mSocketRunable.setParseListener(new ParserListener() {
            @Override
            public void onGotPackage(final ArrayList<Item> packItems) {
                byte[] data = packItems.get(DiagramParser.ITEM_DATA).get_recognized();
                //mLog.i("got pack: " + HexTools.byteArrayToHex(data, 0, data.length, 50));
                ByteBuffer bb = ByteBufferLE.wrap(data);
                Data cmd = Data.from(bb);

                if (cmd.getCmd() == ProtoMessage.Cmd.cmdHeartbeat_VALUE) {
                    // 心跳包
                    mLog.i("收到心跳包.");
                    checkHeartResp(false, 0);
                } else {
                    Log.i("MyService","onGotPackage cmd="+cmd.getCmd());
                    CommonProcesser p = mCmdFuncs.get(cmd.getCmd());
                    if (p != null) {
                        try {
                            p.onGot(data);
                        } catch (Exception e) {
                            mLog.w("exception to do pack cmd: " + cmd + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        Log.w(TAG, "cmd not recoginized: " + cmd.getCmd());
                    }
                }
            }
        });

        mSocketRunable.setListener(new SocketRunable.Listener() {
            @Override
            public void onConnected() {
                Log.w(TAG, "socket connected.");
                mSocketConnectStatus = true;
                mLog.i("socket connection ok");
                DBMyLogHelper.insertLog(MyService.this, LogCode.CONNECT_OK, "socket connection ok", null);
                ConnectionBroadcast b = new ConnectionBroadcast(MyService.this);
                b.sendBroadcast("1");

                reHeartAlarm(true);

                if (GlobalStatus.msg_null()) {
                    Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_NAME, "");
                    Settings.System.putString(MyApplication.getContext().getContentResolver(), Settings.System.CALL_INFO, "");
                }
                String imei = ConnUtil.getDeviceId(MyService.this);
                if (TextUtils.isEmpty(imei) || imei.length() < 10) {
                    mHandler.sendEmptyMessageDelayed(GET_DEVICE_ID_DELAY, GET_DEVICE_ID_DELAY_TIME);
                } else {
                    register(imei);
                }
            }

            @Override
            public void onDisconnected() {
                Log.w(TAG,"socket disconnect.");
                mSocketConnectStatus = false;
                // 取消心跳定时器
                reHeartAlarm(false);

                // 取消心跳应答检查
                checkHeartResp(false, 0);

                if (mVoiceHandler != null) {
                    mVoiceHandler.doVoiceClicked(false);
                }
                GlobalStatus.setCarRegister(null);
                mHandler.removeMessages(GET_DEVICE_ID_DELAY);
                mLog.i("socket disconnect.");
                DBMyLogHelper.insertLog(MyService.this, LogCode.CONNECT_STOP, "socket disconnect.", null);

                GlobalStatus.closeCurChat();
                ConnectionBroadcast b = new ConnectionBroadcast(MyService.this);
                b.sendBroadcast("0");

                //关闭广播监听
                mBroadcastManger.stopAll();
                // 关闭定位服务
                ServiceCheckUserEvent.stop(getBaseContext());
            }

            @Override
            public void onPackSent(SendDataParcelable temp) {
                //mLog.i("pack sent: pack type: " + temp.getDataType() + ": len: " + temp.getData().length);
//                int i = 0;
//                int k = 40;
//                for (; i < temp.getDataLen() / k; ++i) {
//                    //Log.i("chat", "---- " + HexTools.byteArrayToHex(temp.getData(), i * k, k));
//                }
//                if (temp.getDataLen() % k != 0) {
//                    //Log.i("chat", "---- " + HexTools.byteArrayToHex(temp.getData(), i * k, temp.getDataLen() - i * k));
//                }

                CommonProcesser p = mCmdFuncs.get(Data.from(temp.getDataType()));
                reHeartAlarm(true);
                if (p != null) {
                    try {
                        p.onSent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        startThread();

        //启动监听蓝牙ptt按键广播
        bluetoothMonitor = new BluetoothMonitor(this);

        mNotifyPlayer = SoundPoolTool.getInstance(this);
        mNotifyPlayer.initVoice(mHandler);
        // 启动定位服务
//        ServiceCheckUserEvent.restart(getBaseContext());
        queryGroup();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.TEAM_ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        intentFilter.addAction("ACTION.refreshTeamList");
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(RESClient.ACTION_VIDEO_VOIDE_SWITCH);
        intentFilter.addAction("com.erobbing.ACTION_START_THREAD");
        intentFilter.addAction("com.erobbing.ACTION_STOP_THREAD");
        registerReceiver(contentObserver, intentFilter);
        // start sim check
        startSimInfoService();
    }

    private BroadcastReceiver contentObserver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Log.d(TAG, "MyService=ACTION_SHUTDOWN");
                if (mSocketRunable != null) {
                    ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
                    if (GlobalStatus.isStartRooming() && GlobalStatus.getChatRoomtempId() != 0) {
                        builder.setRoomID(GlobalStatus.getChatRoomtempId());
                    } else {
                        builder.setRoomID(GlobalStatus.getRoomID());
                    }
                    if (builder.getRoomID() == -1) {
                        GlobalStatus.setChatRoomtempId(-1);
                    } else {
                        GlobalStatus.setChatRoomtempId(0);
                    }
                    //for reset
                    if(GlobalStatus.getOldTeam() > 0){
                        GlobalStatus.setOldChatRoom(GlobalStatus.getRoomID());
                    }else{
                        GlobalStatus.setOldChatRoom(0);
                    }
                    builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
                    Log.d(TAG, " ACTION_SHUTDOWN setAcceptType ="+ProtoMessage.AcceptType.atDeny_VALUE);
                    byte[] bTemp = DiagramParser.packData(ProtoMessage.Cmd.cmdAcceptVoice_VALUE, builder.build());
                    mSocketRunable.putData(new SendDataParcelable(bTemp, ProtoMessage.Cmd.cmdAcceptVoice_VALUE));
                }
                stopThread();
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                Log.d(TAG, "MyService=ACTION_SCREEN_OFF");
                int type = GlobalStatus.getShutDownType(MyService.this);
                if (type == 0) {
                    SharedPreferencesUtils.put(MyService.this, "isScreenOn", false);
                    stopService(context);
                }
            } else if(intent.getAction().equalsIgnoreCase(RESClient.ACTION_VIDEO_VOIDE_SWITCH)) {
                //Log.i("MyService", "updateViewReceiver：" + intent.getAction());
                boolean isVideo = intent.getBooleanExtra("isVideo", false);
                //if (!GlobalStatus.IsRandomChat() && !isVideo) {
                if (!isVideo) {
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_PLAY, null);
                    ToastR.setToastLong(MyApplication.getContext(), "当前网络质量不好，切换为语音对讲模式");
                    GlobalStatus.setIsVideo(false);
                    if (mVoiceHandler != null) {
                        mVoiceHandler.speakEndAndRecroding(MyService.this);
                        if (GlobalStatus.isPttKeyDown()) {
                            Log.i(TAG, "cur is ptt down");
                            mVoiceHandler.speakBeginAndRecording(MyService.this);
                        }
                    }
                }
            } else if ("com.erobbing.ACTION_START_THREAD".equals(intent.getAction())) {
                startThread();
                Log.d(TAG, "startVoice failed-ACTION_START_THREAD");
            } else if ("com.erobbing.ACTION_STOP_THREAD".equals(intent.getAction())) {
                stopThread();
                Log.d(TAG, "startVoice failed-ACTION_STOP_THREAD");
            } else {
                queryGroup();
            }
        }
    };

//    private void cancelHeartAlarm() {
//        // 取消定时器
//        AlarmManager alarm = (AlarmManager) MyService.this.getSystemService(Context.ALARM_SERVICE);
//        long nextTime = SystemClock.elapsedRealtime() + HEART_INTERVAL;
//        Intent intent = new Intent(MyService.this, MyService.class);
//        intent.putExtra("heart", true);
//        PendingIntent pi = PendingIntent.getService(MyService.this, 0, intent, 0);
//        alarm.cancel(pi);
//    }

    private void OnLoginSucc(String str) {
        /*已经在loginProcesser里面处理了
        ToastR.setToast(getBaseContext(), str);
        ProtoMessage.CommonMsgRequest.Builder builder = ProtoMessage.CommonMsgRequest.newBuilder();
        MyService.start(getBaseContext(), ProtoMessage.Cmd.cmdGetMsg_VALUE, builder.build());*/

    }

    @Override
    public void onDestroy() {
        Log.d("chatjrd", "onDestroy");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        try {
            if (shutDownObserver != null) {
                shutDownObserver.stopObserving();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //关闭监听蓝牙ptt按键广播
        if (bluetoothMonitor != null) {
            bluetoothMonitor.onDestroy();
        }

        stopThread();
        stopForeground(true);

        if (mVoiceHandler != null) {
            mVoiceHandler.doVoiceClicked(false);
        }

        reHeartAlarm(false);
        checkHeartResp(false, 0);
        try {
            mCheckHeartBroadcast.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBroadcastManger.stopAll();
        try {
            unregisterReceiver(contentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSimInfoService();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("chatjrd", "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("chatjrd", "onStartCommand");
        if (0 == GlobalStatus.getShutDownType(this) && !((boolean) SharedPreferencesUtils.get(MyService.this, "isScreenOn", false))) {
//            ToastR.setToastLong(this,"当前处于熄火状态");
            return super.onStartCommand(intent, flags, startId);
        }
        parseIntent(intent);
        return START_STICKY;
    }

    private void parseIntent(Intent intent) {
        //Log.d("chatjrd", "parseIntent");
        if (intent == null) {
            return;
        }

        SendDataParcelable p = intent.getParcelableExtra("xxx");
        if (p != null) {
            mSocketRunable.putData(p);
        } else if (intent.hasExtra("heart")) {
            if (ConnUtil.isConnected(this)) {
                mLog.i("发送心跳");
                // 唤醒
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "pocdemo_heart");
                wl.setReferenceCounted(false);
                try {
                    wl.acquire(5000);
                    mLog.i("手机唤醒");
                } catch (Exception e) {
                    e.printStackTrace();
                    mLog.e("手机唤醒异常");
                }

                // 准备心跳包数据
                HeartSend h = new HeartSend();
                h.setCmd(ProtoMessage.Cmd.cmdHeartbeat.getNumber());

                p = new SendDataParcelable(h);
                mSocketRunable.putData(p);

                // 检查发送的心跳包有没有正常返回
                checkHeartResp(true, 30);

                // 启动下一次发送
                reHeartAlarm(true);
            }

        }


        startThread();

        if (mVoiceHandler != null) {
            mVoiceHandler.doWithIntent(intent);
        }
    }

    public BluetoothMonitor bluetoothMonitor;

    private Object mThreadLocker = new Object();
    void startThread() {
        synchronized (mThreadLocker) {
            if (mSocketRunable != null) {
                if (ConnUtil.isConnected(this)) {
                    if (!mSocketRunable.isRunning()) {
                        Log.d("chatjrd", "startThread");
                        mSocketThread = new Thread(mSocketRunable);
                        mSocketRunable.setRuning(true);
                        mSocketThread.start();
                    } else {
                        //Log.w(TAG, "thread already started.");
                    }
                }
            }
        }
    }

    void stopThread() {
        synchronized (mThreadLocker) {
            if (mSocketRunable != null) {
                if (mSocketThread != null) {
                    mLog.i("线程正在停止");
                    Log.d("chatjrd", "stopThread");
                    mSocketRunable.setRuning(false);
                    try {
                        mSocketThread.interrupt();
                    }catch (Exception e){

                    }

                    try {
                        mSocketThread.join(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    if (mSocketThread.getState() != Thread.State.TERMINATED) {
//                        mLog.w("线程停止失败, 尝试中止线程");
//                        try {
//                            mSocketThread.interrupt();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }

                    mSocketThread = null;
                }
            }
        }
    }

    private int getHeartbeat() {
        int x = GlobalStatus.getChangeHeartbeat();
//        mLog.i("获取心跳时间间隔：" + x + "(秒)");
        return x;
    }


    /**
     * 定时发送心跳包
     */
    private void reHeartAlarm(boolean bEanble) {
        // 启动Alarm 发送心跳包
//        Log.d("chatjrd", "reHeartAlarm");
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        long nextTime = SystemClock.elapsedRealtime() + (getHeartbeat() * 1000);
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("heart", true);
        PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
        // 先取消，再开始
        alarm.cancel(pi);

        if (bEanble) {
//            mLog.i("启动心跳定时器");
            alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pi);
        } else {
//            mLog.i("取消心跳定时器");
        }
    }

    /**
     * 定时发送心跳包
     *
     * @param sec 秒
     */
    private void checkHeartResp(boolean enable, int sec) {
        // 启动Alarm 发送心跳包
        Log.d("chatjrd", "checkHeartResp");
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(new CheckHeartRespBroadcast(this).getActionName());
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        // 先取消，再开始
        alarm.cancel(pi);

        if (enable) {
            mSentHeart = 1;
            mLog.i("[" + sec + "] 秒后，检查心跳包结果。");
            long nextTime = SystemClock.elapsedRealtime() + sec * 1000;
            alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pi);
            DvrService.start(this);
        } else {
            mSentHeart = 0;
            mLog.i("已取消检查心跳包结果。");
        }
    }

    /**
     * 设置为前台服务
     */
    private void setServerForeground() {
        Log.d("chatjrd", "setServerForeground");
        Intent notificationIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(this, WelcomeActivity.class))
                .setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.btn_call)//必须要先setSmallIcon，否则会显示默认的通知，不显示自定义通知
                .setTicker("视频对讲服务正在运行")
                .setContentTitle("视频对讲服务正在运行")
                .setContentText("")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2323, notification);
    }


    public void register(String imei) {
        ProtoMessage.CarRegister.Builder cardBuilder = ProtoMessage.CarRegister.newBuilder();
        mLog.i("imei:" + imei);
        cardBuilder.setCarID(imei);
        MyService.start(MyService.this, ProtoMessage.Cmd.cmdCarRegister.getNumber(), cardBuilder.build());
        //TODO: 加入重新登录代码，成功时不操作，不成功时提示用户登录失败，并打开登录窗口
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");
        if (token.equals("")) {
        } else {
            String phone = preferences.getString("phone", "");
            ProtoMessage.UserLogin.Builder builder = ProtoMessage.UserLogin.newBuilder();
            builder.setPhoneNum(phone);
            builder.setToken(token);
            builder.setCarID(imei);
            builder.setAppType(ProtoMessage.AppType.appCar_VALUE);
            MyService.start(MyService.this, ProtoMessage.Cmd.cmdLogin.getNumber(), builder.build());

            IntentFilter filter = new IntentFilter();
            filter.addAction(LoginProcesser.ACTION);
            TimeoutBroadcast x = new TimeoutBroadcast(MyService.this, filter, MyService.this.getBroadcastManager());

            x.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                    restart(MyService.this);
                    ToastR.setToast(getBaseContext(), "超时");
                }

                @Override
                public void onGot(Intent i) {
                    if (i.getIntExtra("error_code", 0) ==
                            ProtoMessage.ErrorCode.LOGIN_PHONE_OR_PASS_WRONG_VALUE
                            || i.getIntExtra("error_code", 0) ==
                            ProtoMessage.ErrorCode.LOGIN_TOKEN_WRONG_VALUE
                            || i.getIntExtra("error_code", 0) ==
                            ProtoMessage.ErrorCode.LOGIN_TOKEN_NOT_EXIST_VALUE) {
                        ToastR.setToast(getBaseContext(), "连接失败");
                        Intent intent = new Intent("com.example.jrd48.chat.FORCE_OFFLINE");//测试强制下线功能
                        sendBroadcast(intent);
                    } else if (i.getIntExtra("error_code", 0) ==
                            ProtoMessage.ErrorCode.OK_VALUE) {
                        //
                        resetOldChat();
                    }
                }
            });
        }

    }

    private void resetOldChat() {
        long oldChatRoom = GlobalStatus.getOldChatRoom();
        Log.d(TAG,"resetOldChat oldChatRoom =" + oldChatRoom + "--GlobalStatus.getOldTeam()=" + GlobalStatus.getOldTeam());
        long oldRoom = GlobalStatus.getOldRoom();
        long oldTeam = GlobalStatus.getOldTeam();
        String oldPhone = GlobalStatus.getOldPhone();
        if(oldChatRoom == 0 && oldRoom == 0 && oldTeam == 0 ) {
            Log.d(TAG,"old chat is null");
            return ;
        }
        //ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();//发送接受
        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        /*if(oldChatRoom != 0){
            builder.setRoomID(oldChatRoom);
        }else if(oldTeam > 0){
            builder.setRoomID(oldRoom);
        }*/
        if (oldTeam != 0) {
            builder.setTeamID(oldTeam);
        }
        // recovery single poc after network reconnected
        if (oldTeam == 0 && oldPhone.length() > 5) {
            //builder.setRoomID(oldRoom);
        }
        Log.v(TAG, " resetOldChat start team=" + builder.getTeamID());
        //builder.setAcceptType(ProtoMessage.AcceptType.atAccept_VALUE);//
        //MyService.start(MyService.this, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
        MyService.start(MyService.this, ProtoMessage.Cmd.cmdStartVoice.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(VoiceAcceptProcesser.ACTION);
        new TimeoutBroadcast(MyService.this, filter, new TimeoutBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.e(TAG, "resetOldChat accept timeout");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.e(TAG, " resetOldChat  accept OK");
                    GlobalStatus.setOldChatRoom(0);
                } else if (i.getIntExtra("error_code", -1) == ProtoMessage.ErrorCode.NOT_FOUND_THIS_ROOM.getNumber()) {
                    Log.e(TAG, " resetOldChat  accept NOT_FOUND_THIS_ROOM");
                } else {
                    Log.e(TAG, " resetOldChat accept error");
                }
            }
        });

    }


    private void queryGroup() {
        try {
            DBManagerTeamList db = new DBManagerTeamList(this, true, DBTableName.getTableName(this, DBHelperTeamList.NAME));
            List<TeamInfo> mTeamInfo = db.getTeams();
            db.closeDB();
            if (mTeamInfo.size() <= 0) {
                Log.i(ServiceCheckUserEvent.TAG, "get team list = 0");
            } else {
                GlobalStatus.setTeamsInfoList(mTeamInfo);
                List<Long> predefineTeams = new ArrayList<Long>();
                for (TeamInfo group : mTeamInfo) {
                    predefineTeams.add(group.getTeamID());
                }
                GlobalStatus.setPredefineTeams(predefineTeams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ShutDownObserver extends ContentObserver {
        private final Uri NAVI_START_STOP_URI =
                Settings.System.getUriFor(GlobalStatus.NAVI_START_STOP);

        public ShutDownObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (NAVI_START_STOP_URI.equals(uri)) {
                Context context = MyService.this;
                int type = GlobalStatus.getShutDownType(context);
                Log.v("MyService", "ShutDownObserver type:" + type);
                if (1 == type) {
                    //TODO 打火
                    Log.d(TAG, "dahuo-GlobalStatus.getOldTeam()=" + GlobalStatus.getOldTeam());
                    MyService.restart(context);
                } else if (0 == type) {
                    Log.d(TAG, "xihuo-GlobalStatus.getOldTeam()=" + GlobalStatus.getOldTeam());
                    stopService(context);
                }
            }
        }

        public void startObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    NAVI_START_STOP_URI,
                    false, this);
        }

        public void stopObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }

    public void stopService(Context context) {
        //TODO 熄火
        if (GlobalStatus.msg_null()) {
            if (mSocketRunable != null) {
                ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
                if (GlobalStatus.isStartRooming() && GlobalStatus.getChatRoomtempId() != 0) {
                    builder.setRoomID(GlobalStatus.getChatRoomtempId());
                } else {
                    builder.setRoomID(GlobalStatus.getRoomID());
                }
                if (builder.getRoomID() == -1) {
                    GlobalStatus.setChatRoomtempId(-1);
                } else {
                    GlobalStatus.setChatRoomtempId(0);
                }
                //for reset
                if(GlobalStatus.getOldTeam() > 0){//save team
                    GlobalStatus.setOldChatRoom(GlobalStatus.getRoomID());
                }else{
                    GlobalStatus.setOldChatRoom(0);
                }
                builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
                Log.d(TAG, "stopService setAcceptType ="+ProtoMessage.AcceptType.atDeny_VALUE);
                byte[] bTemp = DiagramParser.packData(ProtoMessage.Cmd.cmdAcceptVoice_VALUE, builder.build());
                mSocketRunable.putData(new SendDataParcelable(bTemp, ProtoMessage.Cmd.cmdAcceptVoice_VALUE));
            }
            ToastR.setToast(context, "网络断开，对讲暂停");
            Log.d(TAG, "stopService--network disconnected, stop chating");
            Intent i = new Intent(AutoCloseProcesser.ACTION);
            i.putExtra("roomID", GlobalStatus.getRoomID());
            context.sendBroadcast(i);
            GlobalStatus.clearChatRoomMsg();
            NotificationManager nm = (NotificationManager) (context.getSystemService(context.NOTIFICATION_SERVICE));
            nm.cancel(-1);//消除对应ID的通知
        }
        Intent i = new Intent(context, MyService.class);
        context.stopService(i);
    }
    private void startSimInfoService() {
        Log.d(TAG, " startSimInfoService");
        Intent i = new Intent(this,SimInfoService.class);
        startService(i);
    }

    private void stopSimInfoService() {
        Log.d(TAG, " stopSimInfoService");
        Intent intent = new Intent(this, SimInfoService.class);
        stopService(intent);
    }
}
