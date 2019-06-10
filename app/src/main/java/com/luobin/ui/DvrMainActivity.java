package com.luobin.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.example.jrd48.PolyphonePinYin;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.TabFragmentLinkGroup;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.luobin.dvr.R;
import com.luobin.tool.MyInforTool;
import com.luobin.tool.OnlineSetTool;

import java.util.List;

import uk.co.senab.photoview.log.Logger;

/**
 * Created by Administrator on 2017/8/8.
 */

public class DvrMainActivity extends BaseActivity implements View.OnClickListener, PermissionUtil.PermissionCallBack {

    private Context context;
    protected PermissionUtil mPermissionUtil;
    LinearLayout actionbarMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_dvr_main);


        actionbarMessage = (LinearLayout) findViewById(R.id.actionbar_message);
        actionbarMessage.setOnClickListener(this);
        PolyphonePinYin.initPinyin();
        requestAllPermisson();
        initBroadCast();



        //TODO 在这添加数据 个人信息
//        MyInforTool myInforTool = new MyInforTool(DvrMainActivity.this, true);
//        Log.i("myInforTool", myInforTool.toString());
//        if (myInforTool.getUserName() == null || "".equals(myInforTool.getUserName())||myInforTool.getUserName().equals(myInforTool.getPhone())) {
//            startActivity(new Intent(DvrMainActivity.this, RegisterInfoActivity.class));
//          
//        }


        getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts, new TabFragmentLinkGroup())
                .commitAllowingStateLoss();


    }

    private void initBroadCast() {
        //注册好友在线状态变化广播
        IntentFilter filterStatus = new IntentFilter();
        filterStatus.addAction(NotifyProcesser.FRIEND_STATUS_ACTION);
        registerReceiver(friendStatus, filterStatus);

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
            case R.id.actionbar_message: {
                logoutDialog(context);
                break;
            }

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

}
