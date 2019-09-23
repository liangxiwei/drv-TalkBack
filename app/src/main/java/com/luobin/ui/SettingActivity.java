package com.luobin.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;
import com.luobin.ui.settingitem.SettingDrawVideoActivity;
import com.luobin.ui.settingitem.SettingPhotoActivity;
import com.luobin.ui.settingitem.SettingTrajectoryActivity;
import com.luobin.ui.settingitem.SettingVideoActivity;

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
            /*"轨迹设置"*/"恢复出厂设置", "画中画", "随手拍照片", "随手拍视频", "无线电设置",
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
                    intent.putExtra("tuichu", "set");
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
				startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            //系统升级
            case 3:
				try{
					Intent i = new Intent();
					i.setClassName("com.qualcomm.update", "com.qualcomm.update.UpdateDialog");
				    startActivity(i);
					//Runtime.getRuntime().exec("am start -n com.qualcomm.update/.UpdateDialog");
				}catch (Exception e){
            		e.printStackTrace();
					Log.d("rs","found system update exception: "+e.toString());
        		}
                break;
			/*
            //轨迹设置
            case 4:
                startActivity(new Intent(this, SettingTrajectoryActivity.class));
                break;
            */
            //恢复出厂设置
            case 4:
				forceRecoveryDialog(this);
				break;
            //画中画
            case 5:
                startActivity(new Intent(this, SettingDrawVideoActivity.class));
                break;
            //随手拍照片
            case 6:
                startActivity(new Intent(this, SettingPhotoActivity.class));
                break;
            //随手拍视频
            case 7:
                startActivity(new Intent(this, SettingVideoActivity.class));
                break;
            //无线电设置
            case 8:
				try{
					startActivity(new Intent("com.benshikj.ht.jf.intent.action.RADIO_SETTINGS"));
				}catch (Exception e){
            		e.printStackTrace();
					Log.d("rs","found RADIO_SETTINGS exception: "+e.toString());
        		}
                break;
            //离线地图
            case 9:
				try{
					startActivity(new Intent("com.benshikj.ht.jf.intent.action.OFFLINE_MAP_SETTINGS"));
				}catch (Exception e){
            		e.printStackTrace();
					Log.d("rs","found OFFLINE_MAP_SETTINGS exception: "+e.toString());
        		}
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


	//rs added for recovery dialog
    private void forceRecoveryDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.toast_tip));
        builder.setMessage("确定要恢复出厂设置吗？");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                sendBroadcast(intent);
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

	//end
}
