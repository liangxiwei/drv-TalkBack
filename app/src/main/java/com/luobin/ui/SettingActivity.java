package com.luobin.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * @author wangjunjie
 */
public class SettingActivity extends BaseActivity {

    @BindView(R.id.btnBack)
    Button btnBack;

    @BindView(R.id.gvItem)
    GridView gvItem;

    SettingAdapter adapter = null;

    String[] data = {"账号", "背景墙", "蓝牙手咪", "系统升级",
            "轨迹设置", "画中画", "随手拍照片", "随手拍视频", "无线电设置",
            "离线地图"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        adapter = new SettingAdapter(this, Arrays.asList(data));
        gvItem.setAdapter(adapter);
    }


    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    @OnItemClick(R.id.gvItem)
    void itemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            //账号
            case 0:
                if (checkLogin()) {
                    Intent intent = new Intent(this, RegisterInfoActivity.class);
                    intent.putExtra("tuichu","set");
                    startActivity(intent);
                    //startActivity(new Intent(this, RegisterInfoActivity.class));class
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }

                break;
            //背景墙
            case 1:
                break;
            //蓝牙手咪
            case 2:
                break;
            //系统升级
            case 3:
                break;
            //轨迹设置
            case 4:
                break;
            //画中画
            case 5:
                break;
            //随手拍照片
            case 6:
                break;
            //随手拍视频
            case 7:
                break;
            //无线电设置
            case 8:
                break;
            //离线地图
            case 9:
                break;
            default:
                break;
        }

    }

    private boolean checkLogin() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");
        if (token.equals("")) {
            return false;
        }
        return true;
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
