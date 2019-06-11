package com.luobin.ui;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.PolyphonePinYin;
import com.example.jrd48.chat.BadgeView;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.TabFragmentLinkGroup;
import com.example.jrd48.chat.TabFragmentLinkmans;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.CreateGroupActivity;
import com.example.jrd48.chat.group.InviteJoinGroupActivity;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.receiver.NotifyFriendBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.luobin.dvr.R;
import com.luobin.notice.NotificationActivity;
import com.luobin.tool.MyInforTool;
import com.luobin.tool.OnlineSetTool;
import com.luobin.ui.TalkBackSearch.TalkbackSearchActivity;

import java.util.List;

import uk.co.senab.photoview.log.Logger;

/**
 * Created by Administrator on 2017/8/8.
 */

public class DvrMainActivity extends BaseActivity implements View.OnClickListener, PermissionUtil.PermissionCallBack {

    private Context context;
    protected PermissionUtil mPermissionUtil;
    private LinearLayout actionbarMessage, actionbarAdd, actionbarSearch;
    private TabFragmentLinkGroup tabFragmentLinkGroup;
    private TabFragmentLinkmans tabFragmentLinkmans;
    public static final int FRAGMENT_POSITION_GROUP = 0;
    public static final int FRAGMENT_POSITION_MANS = 1;
    private int fragmentPostion = FRAGMENT_POSITION_GROUP;
    Button btnChange;
    BadgeView  badgeView;
    NotifyFriendBroadcast mNotifyFriendBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_dvr_main);
        actionbarMessage = (LinearLayout) findViewById(R.id.actionbar_message);
        actionbarMessage.setOnClickListener(this);
        btnChange = (Button) findViewById(R.id.btn_change);
        btnChange.setOnClickListener(this);
        actionbarAdd = (LinearLayout) findViewById(R.id.actionbar_add);
        actionbarAdd.setOnClickListener(this);
        actionbarSearch = (LinearLayout) findViewById(R.id.actionbar_search);
        actionbarSearch.setOnClickListener(this);
        PolyphonePinYin.initPinyin();
        requestAllPermisson();
        initBroadCast();


        //TODO 在这添加数据 个人信息
        MyInforTool myInforTool = new MyInforTool(DvrMainActivity.this, true);
        Log.i("myInforTool", myInforTool.toString());
        if (myInforTool.getUserName() == null || "".equals(myInforTool.getUserName()) || myInforTool.getUserName().equals(myInforTool.getPhone())) {
            startActivity(new Intent(DvrMainActivity.this, RegisterInfoActivity.class));
        }

        tabFragmentLinkGroup = new TabFragmentLinkGroup();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts, tabFragmentLinkGroup)
                .commitAllowingStateLoss();


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

            }
        });
        mNotifyFriendBroadcast.start();

    }

    @Override
    protected void onResume() {
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
                        btnChange.setText("群组");
                        if (tabFragmentLinkmans == null) {
                            tabFragmentLinkmans = new TabFragmentLinkmans();
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts, tabFragmentLinkmans)
                                .commitAllowingStateLoss();
                    }
                }
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
                sendBroadcast(intent);
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


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                   if (fragmentPostion == FRAGMENT_POSITION_GROUP){
                        tabFragmentLinkGroup.moveLeft();
                       return true;
                   }
                    return super.dispatchKeyEvent(event);
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (fragmentPostion == FRAGMENT_POSITION_GROUP){
                        tabFragmentLinkGroup.moveRight();
                        return true;
                    }
                    return super.dispatchKeyEvent(event);
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (fragmentPostion == FRAGMENT_POSITION_GROUP){
                        tabFragmentLinkGroup.moveUp();
                        return true;
                    }
                    return super.dispatchKeyEvent(event);
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (fragmentPostion == FRAGMENT_POSITION_GROUP){
                        tabFragmentLinkGroup.moveDown();
                        return true;
                    }
                    return super.dispatchKeyEvent(event);
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    tabFragmentLinkGroup.clickCenter();
                    return super.dispatchKeyEvent(event);
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
                case KeyEvent.KEYCODE_F6:
                    tabFragmentLinkGroup.clickPTT();
                    return super.dispatchKeyEvent(event);

            }
        }
        return super.dispatchKeyEvent(event);
    }

}
