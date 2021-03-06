package com.luobin.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.jrd48.GlobalNotice;
import com.example.jrd48.PolyphonePinYin;
import com.example.jrd48.chat.BadgeView;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.TabFragmentLinkGroup;
import com.example.jrd48.chat.TabFragmentLinkmans;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.group.AppliedTeamsList;
import com.example.jrd48.chat.group.CreateGroupActivity;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.receiver.NotifyFriendBroadcast;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.AppliedGroupListProcesser;
import com.example.jrd48.service.protocol.root.AppliedListProcesser;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.R;
import com.luobin.notice.NotificationActivity;
import com.luobin.tool.MyInforTool;
import com.luobin.tool.OnlineSetTool;
import com.luobin.ui.TalkBackSearch.TalkbackSearchActivity;
import com.luobin.utils.ShellUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class DvrMainActivity extends BaseActivity implements View.OnClickListener, PermissionUtil.PermissionCallBack {
    private static final String TAG = "DvrMainActivity";
    private static final int MSG_USER_ICON_COPY = 1;
    private Context context;
    protected PermissionUtil mPermissionUtil;
    private LinearLayout actionbarMessage, actionbarAdd, actionbarSearch;
    private TabFragmentLinkGroup tabFragmentLinkGroup;
    private TabFragmentLinkmans tabFragmentLinkmans;
    public static final int FRAGMENT_POSITION_GROUP = 0;
    public static final int FRAGMENT_POSITION_MANS = 1;
    private int fragmentPostion = FRAGMENT_POSITION_GROUP;
    Button btnChange;
    Button btnReturn;
    BadgeView badgeView;
    ImageView btnImage;
    NotifyFriendBroadcast mNotifyFriendBroadcast;
    private int mCountMessage;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "mHandler what = " + msg.what);
            switch (msg.what) {
                case MSG_USER_ICON_COPY:
                    ShellUtils.execCommand("cp -r /data/data/com.luobin.dvr/files/friend_face/*.jpg " + DvrConfig.getStoragePath() + "/friend_face", false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_dvr_main);
        actionbarMessage = (LinearLayout) findViewById(R.id.actionbar_message);
        actionbarMessage.setOnClickListener(this);
        btnImage = (ImageView) findViewById(R.id.message_icon);
        btnChange = (Button) findViewById(R.id.btn_change);
        btnChange.setOnClickListener(this);
        btnReturn = (Button) findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(this);
        actionbarAdd = (LinearLayout) findViewById(R.id.actionbar_add);
        actionbarAdd.setOnClickListener(this);
        actionbarSearch = (LinearLayout) findViewById(R.id.actionbar_search);
        actionbarSearch.setOnClickListener(this);
        // PolyphonePinYin.initPinyin();
        requestAllPermisson();
        initBroadCast();


        //TODO 在这添加数据 个人信息
        //rs added for LBCJW-200:toast for username invalid
        /*
        MyInforTool myInforTool = new MyInforTool(DvrMainActivity.this, true);
        Log.i("myInforTool", myInforTool.toString());
		
        if (myInforTool.getUserName() == null || "".equals(myInforTool.getUserName()) || myInforTool.getUserName().equals(myInforTool.getPhone())) {
			ToastR.setToastLong(DvrMainActivity.this, "昵称无效，请重新设置！");
            startActivity(new Intent(DvrMainActivity.this, RegisterInfoActivity.class));
        }
		*/
		//end

        Log.d("DvrMainActivity", "onCreate");
        tabFragmentLinkGroup = new TabFragmentLinkGroup();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts, tabFragmentLinkGroup)
                .commitAllowingStateLoss();

        //ShellUtils.execCommand("cp -r /data/data/com.luobin.dvr/files/friend_face/*.jpg " + DvrConfig.getStoragePath() + "/friend_face", false);
        mHandler.sendEmptyMessageDelayed(MSG_USER_ICON_COPY, 6 * 1000);
    }

    private void initBroadCast() {
        //注册好友在线状态变化广播
        IntentFilter filterStatus = new IntentFilter();
        filterStatus.addAction(NotifyProcesser.FRIEND_STATUS_ACTION);
        registerReceiver(friendStatus, filterStatus);

        //注册获取申请加好友广播
        mNotifyFriendBroadcast = new NotifyFriendBroadcast(mContext);
        mNotifyFriendBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                loadFriendsListFromNet();
                loadGroupListFromNet();
            }
        });
        mNotifyFriendBroadcast.start();
    }

    @Override
    protected void onResume() {
        loadFriendsListFromNet();
        loadGroupListFromNet();
        SharedPreferencesUtils.put(this, "isBBS", false);
        super.onResume();
    }


    private void requestAllPermisson() {
        mPermissionUtil = PermissionUtil.getInstance();
        if (mPermissionUtil.checkPermissions(this)) {
            List<String> list = mPermissionUtil.findDeniedPermissions(this, mPermissionUtil.getPermissionsList());
            if (list.size() > 0) {
                mPermissionUtil.requestPermission(this, PermissionUtil.MY_PERMISSIONS_CHECK_ALL, this, list.get(0), list.get(0));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.TYPE);
    }

    @Override
    public void onPermissionSuccess(String type) {
        requestAllPermisson();
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(DvrMainActivity.this, "该权限已经拒绝，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToast(DvrMainActivity.this, "权限设置失败");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            MyService.goHome(this);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_message:
                Intent messageIntent = new Intent();
                messageIntent.setClass(mContext, NotificationActivity.class);
                startActivity(messageIntent);
                // logoutDialog(context);
                break;

            case R.id.actionbar_add:
                //TODO 添加群组
                Intent addIntent = new Intent(context, CreateGroupActivity.class);
                startActivity(addIntent);
                break;

            case R.id.actionbar_search:
                //TODO 搜索
                startActivity(new Intent(this, TalkbackSearchActivity.class));
                break;

            case R.id.btn_return:
                MyService.goHome(this);
                break;

            case R.id.btn_change:
                if (fragmentPostion == FRAGMENT_POSITION_MANS) {
                    fragmentPostion = FRAGMENT_POSITION_GROUP;
                    btnChange.setText("通讯录");
                    if (tabFragmentLinkGroup == null) {
                        tabFragmentLinkGroup = new TabFragmentLinkGroup();
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts, tabFragmentLinkGroup)
                            .commitAllowingStateLoss();
                } else {
                    if (tabFragmentLinkGroup != null) {
                        if (tabFragmentLinkGroup.isPullRefresh()) {
                            return;
                        }
                    }
                    if (fragmentPostion == FRAGMENT_POSITION_GROUP) {
                        fragmentPostion = FRAGMENT_POSITION_MANS;
                        btnChange.setText("对讲群组");
                        if (tabFragmentLinkmans == null) {
                            tabFragmentLinkmans = new TabFragmentLinkmans();
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts, tabFragmentLinkmans)
                                .commitAllowingStateLoss();
                    }
                }
                break;
                default:
                    break;
        }
    }


    private BroadcastReceiver friendStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra(NotifyProcesser.NUMBER);
            boolean online = intent.getBooleanExtra(NotifyProcesser.ONLINE_KEY, false);
            if (phone != null) {
                if (online) {
                    OnlineSetTool.add(phone);
                } else {
                    OnlineSetTool.remove(phone);
                }
            } else {
                Log.w("jim", "获取号码为空");
            }
        }
    };


    @Override
    protected void onDestroy() {
        try {
            if (friendStatus != null) {
                unregisterReceiver(friendStatus);
            }
            if (mNotifyFriendBroadcast != null) {
                mNotifyFriendBroadcast.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    AlertDialog simplelistdialog = null;

    private void logoutDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.toast_tip));
        builder.setMessage(context.getResources().getString(R.string.toast_logout));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent("com.example.jrd48.chat.FORCE_OFFLINE");
                intent.putExtra("toast", false);
                sendBroadcastAsUser(intent, UserHandle.ALL);
                finish();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (simplelistdialog != null && simplelistdialog.isShowing()) {
            simplelistdialog.dismiss();
        }
        simplelistdialog = builder.create();
        simplelistdialog.show();
    }

    public void loadFriendsListFromNet() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdAppliedList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedListProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                mCountMessage = 0;
                btnImage.setImageResource(R.drawable.message_icon);
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriendsList list = i.getParcelableExtra("get_applied_msg");
                    if ((list != null) && (list.getAppliedFriends() != null)) {
                        Log.i(DvrMainActivity.class.getSimpleName(), "friends list = " + list.getAppliedFriends().size());
                    }
                    if ((list != null) && (list.getAppliedFriends() != null)
                            && list.getAppliedFriends().size() > 0) {
                        mCountMessage = list.getAppliedFriends().size();
                    } else {
                        mCountMessage = 0;
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                    mCountMessage = 0;
                }
                if (mCountMessage == 0) {
                    btnImage.setImageResource(R.drawable.message_icon);
                } else {
                    btnImage.setImageBitmap(generatorMesssageCountIcon(mCountMessage));
                }
            }
        });
    }

    private void loadGroupListFromNet() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdAppliedTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedGroupListProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                mCountMessage = mCountMessage + 0;
                if (mCountMessage == 0) {
                    btnImage.setImageResource(R.drawable.message_icon);
                } else {
                    btnImage.setImageBitmap(generatorMesssageCountIcon(mCountMessage));
                }
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedTeamsList list = i.getParcelableExtra("get_applied_group_list");
                    if ((list != null) && (list.getAppliedTeams() != null)) {
                        Log.i(DvrMainActivity.class.getSimpleName(), "team list = " + list.getAppliedTeams().size());
                    }
                    if (list != null && list.getAppliedTeams() != null && list.getAppliedTeams().size() > 0) {
                        mCountMessage = mCountMessage + list.getAppliedTeams().size();
                    } else {
                        mCountMessage = mCountMessage + 0;
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                    mCountMessage = mCountMessage + 0;
                }
                if (mCountMessage == 0) {
                    btnImage.setImageResource(R.drawable.message_icon);
                } else {
                    btnImage.setImageBitmap(generatorMesssageCountIcon(mCountMessage));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(mContext, i);
    }

    private Bitmap generatorMesssageCountIcon(int count){
        //初始化画布
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.message_icon);
        int iconSize=(int)getResources().getDimension(android.R.dimen.app_icon_size);
        Log.d(DvrMainActivity.class.getSimpleName(), "the icon size is "+iconSize);
        Bitmap contactIcon=Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(contactIcon);

        //拷贝图片
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);//防抖动
        iconPaint.setFilterBitmap(true);//用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
        Rect src=new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect dst=new Rect(0, 0, iconSize, iconSize);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        //启用抗锯齿和使用设备的文本字距
        Paint countPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);
        countPaint.setColor(Color.RED);
        countPaint.setTextSize(40f);
        countPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(String.valueOf(count), iconSize-30, 30, countPaint);
        return contactIcon;
    }
}
