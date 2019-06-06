package com.luobin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.example.jrd48.PolyphonePinYin;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.TabFragmentLinkGroup;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.luobin.dvr.R;
import com.luobin.tool.OnlineSetTool;
import java.util.List;

import uk.co.senab.photoview.log.Logger;

/**
 * Created by Administrator on 2017/8/8.
 */

public class DvrMainActivity extends BaseActivity implements View.OnClickListener, PermissionUtil.PermissionCallBack {

    private Context context;
    protected PermissionUtil mPermissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_dvr_main);
        PolyphonePinYin.initPinyin();
        requestAllPermisson();
        initBroadCast();

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_contacts,new TabFragmentLinkGroup())
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
       /* switch (v.getId()) {
            case R.id.group: {
                Intent intent = new Intent(context, GroupActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.contacts: {
                Intent intent = new Intent(context, ContactsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.self: {
                Intent intent = new Intent(context, MyInforActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.road_share: {
                Intent intent = new Intent(context, OtherVideoSetting.class);
                startActivity(intent);
                break;
            }
            case R.id.search: {
                Intent intent = new Intent(context, SearchActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.notice: {
                Intent intent = new Intent(context, NotificationActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.road_setting: {
                Intent intent = new Intent(context, SettingActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }*/
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
}
