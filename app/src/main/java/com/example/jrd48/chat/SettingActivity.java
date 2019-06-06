package com.example.jrd48.chat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.MyService;
import com.luobin.dvr.R;

/**
 * Created by jrd48 on 2017/3/3.
 */

public class SettingActivity extends BaseActivity {
    private LinearLayout input_way;
    private LinearLayout intervalTime;
    private LinearLayout openPermission;
    private LinearLayout changeLevel;
    private TextView changeLevelShow;
    private TextView intervalTimeShow;
    private TextView inputShow;
    private Switch handFree;
    private Switch volume;
    private Switch location;
    private Switch autoStart;
    private Switch notifyUser;
    private Switch viewVideo;
    private Switch transferVideo;

    private Button button;
    private boolean show = true;
    private SettingRW mSettings = null;
    private NumberPicker mPicker;
    private View view;
    private String[] voice_way = new String[]{"语音通话(推荐)", "麦克风直接输入"};

    private LinearLayout changeHeartbeat;
    private TextView changeHeartbeatShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        handFree = (Switch) findViewById(R.id.switch1);
        volume = (Switch) findViewById(R.id.switch2);
        location = (Switch) findViewById(R.id.switch3);
        autoStart = (Switch) findViewById(R.id.switch4);
        notifyUser = (Switch) findViewById(R.id.switch5);
        viewVideo = (Switch) findViewById(R.id.switch6);
        transferVideo = (Switch) findViewById(R.id.switch7);

        button = (Button) findViewById(R.id.button);
        input_way = (LinearLayout) findViewById(R.id.input);
        intervalTime = (LinearLayout) findViewById(R.id.interval_time);
        changeLevel = (LinearLayout) findViewById(R.id.change_level);
        openPermission = (LinearLayout) findViewById(R.id.open_permission);
        intervalTimeShow = (TextView) findViewById(R.id.interval_time_show);
        changeLevelShow = (TextView) findViewById(R.id.change_level_show);
        inputShow = (TextView) findViewById(R.id.input_show);

        changeHeartbeat = (LinearLayout) findViewById(R.id.change_heartbeat);
        changeHeartbeatShow = (TextView) findViewById(R.id.change_heartbeat_show);

        Toolbarset();

        mSettings = new SettingRW(this);
        mSettings.load();

        handFree.setChecked(mSettings.isHandFree());
        volume.setChecked(mSettings.isAutoMaxVolume());
        location.setChecked(mSettings.isEnableLocation());
        autoStart.setChecked(mSettings.isAutoStart());
        notifyUser.setChecked(mSettings.isNotifyUser());
        viewVideo.setChecked(mSettings.isViewVideo());
        transferVideo.setChecked(mSettings.isTransferVideo());
        inputShow.setText(voice_way[mSettings.getMicWay()]);

        changeHeartbeatShow.setText(mSettings.getChangeHeartbeat() + "秒");

        changeHeartbeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeHeartbeatDialog(mSettings.getChangeHeartbeat());
            }
        });

        handFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(show) {
                    if (isChecked) {
                        ToastR.setToastLong(SettingActivity.this, "打开后，即使正在使用耳机，也会强制使用扬声器播放语音。");
                    } else {
                        ToastR.setToastLong(SettingActivity.this, "关闭后，正在使用耳机时不会打开扬声器，并使用耳机播放语音");
                    }
                }
                mSettings.setHandFree(isChecked);
                mSettings.save();

            }
        });

        volume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(show) {
                    if (isChecked) {
                        ToastR.setToastLong(SettingActivity.this, "打开后，在对讲状态音量会强制调到最大");
                    } else {
                        ToastR.setToastLong(SettingActivity.this, "关闭后，会使用系统设置音量");
                    }
                }
                mSettings.setAutoMaxVolume(isChecked);
                mSettings.save();
            }
        });

        location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettings.setEnableLocation(isChecked);
                mSettings.save();

                if(isChecked){
                    ServiceCheckUserEvent.restart(SettingActivity.this);
                }else{
                    ServiceCheckUserEvent.stop(SettingActivity.this);
                }

            }
        });
        autoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mSettings.setAutoStart(isChecked);
                mSettings.save();
                if (show) {
                    if (isChecked) {
                        ToastR.setToastLong(SettingActivity.this, "打开后，开机时系统自动启动");
                    } else {
//                        ToastR.setToastLong(SettingActivity.this, "关闭后，开机时系统不会自动启动");
                    }
                }
            }
        });

        notifyUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(show) {
                    if (isChecked) {
                        ToastR.setToastLong(SettingActivity.this, "打开后，软件会提示您一些基本操作");
                    }
                }
                mSettings.setNotifyUser(isChecked);
                mSettings.save();
            }
        });

        viewVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(show) {
                    if (isChecked) {
                        ToastR.setToastLong(SettingActivity.this, "打开后，语音对讲时会显示对方视频");
                    }
                }
                mSettings.setViewVideo(isChecked);
                mSettings.save();
            }
        });

        transferVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(show) {
                    if (isChecked) {
                        ToastR.setToastLong(SettingActivity.this, "打开后，语音对讲时上传自己的视频");
                    }
                }
                mSettings.setTransferVideo(isChecked);
                mSettings.save();
            }
        });

        input_way.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputWayDialog();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettings.reDefault();
                show = false;
                handFree.setChecked(mSettings.isHandFree());
                volume.setChecked(mSettings.isAutoMaxVolume());
                location.setChecked(mSettings.isEnableLocation());
                notifyUser.setChecked(mSettings.isNotifyUser());
                autoStart.setClickable(mSettings.isAutoStart());
                viewVideo.setChecked(mSettings.isViewVideo());
                transferVideo.setClickable(mSettings.isTransferVideo());
                inputShow.setText(voice_way[mSettings.getMicWay()]);
                intervalTimeShow.setText("间隔"+mSettings.getIntervalTime()+"分钟");
                changeLevelShow.setText("等级" + (int) mSettings.getMapZoomLevel());
                show = true;
            }
        });

        intervalTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intervalTimeDialog();
            }
        });

        changeLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeLevelDialog();
            }
        });
        changeLevelShow.setText("等级" + (int) mSettings.getMapZoomLevel());
        intervalTimeShow.setText("间隔"+mSettings.getIntervalTime()+"分钟");

        openPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(SettingActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},0);
                ActivityCompat.requestPermissions(SettingActivity.this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION},0);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Intent intent = new Intent(ServiceCheckUserEvent.ACTION);
        intent.putExtra("type", ServiceCheckUserEvent.START_AMAP);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    private void checkPermission(){
        if(PermissionUtil.isOverMarshmallow()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                openPermission.setVisibility(View.VISIBLE);
                location.setChecked(false);
                location.setClickable(false);
            } else {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    openPermission.setVisibility(View.VISIBLE);
                    location.setChecked(false);
                    location.setClickable(false);
                } else {
                    openPermission.setVisibility(View.GONE);
                    location.setClickable(true);
                }
            }
        }else{
            openPermission.setVisibility(View.GONE);
            location.setClickable(true);
        }
    }

    private void ChangeLevelDialog() {
        LayoutInflater inflater=getLayoutInflater();
        view = inflater.inflate(R.layout.picker, null);
        mPicker = (NumberPicker) view.findViewById(R.id.picker);
        mPicker.setMinValue(3);
        mPicker.setMaxValue(19);
        mPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mPicker.setValue((int) mSettings.getMapZoomLevel());

        android.app.AlertDialog mAlertDialog = new android.app.AlertDialog.Builder(SettingActivity.this)
                .setTitle("选择地图缩放等级").setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mSettings.setMapZoomLevel(mPicker.getValue());
                        mSettings.save();
                        changeLevelShow.setText("等级" + (int) mSettings.getMapZoomLevel());
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).create();
        mAlertDialog.show();
    }

    private void intervalTimeDialog() {
        LayoutInflater inflater=getLayoutInflater();
        view = inflater.inflate(R.layout.picker, null);
        mPicker = (NumberPicker) view.findViewById(R.id.picker);
        mPicker.setMinValue(1);
        mPicker.setMaxValue(60);
        mPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mPicker.setValue(mSettings.getIntervalTime());

        android.app.AlertDialog mAlertDialog = new android.app.AlertDialog.Builder(SettingActivity.this)
                .setTitle("选择定位间隔时间(1~60分钟)").setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mSettings.setIntervalTime(mPicker.getValue());
                        mSettings.save();
                        intervalTimeShow.setText("间隔"+mSettings.getIntervalTime()+"分钟");
//                        new SendLocationBroadcast(SettingActivity.this).sendBroadcast("IntervalTime");
                        //用序列化发送广播
                        Intent intent = new Intent(ServiceCheckUserEvent.ACTION);
                        intent.putExtra("time", mPicker.getValue());
                        intent.putExtra("type", ServiceCheckUserEvent.CHANGE_TIME);
                        sendBroadcast(intent);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).create();
        mAlertDialog.show();
    }

    public void inputWayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择语音输入方式");
        builder.setItems(voice_way, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSettings.setMicWay(which);
                mSettings.save();
                inputShow.setText(voice_way[which]);
            }
        });
        AlertDialog simplelistdialog = builder.create();
        simplelistdialog.show();
    }
    private void Toolbarset() {
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText("设置");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    private void showChangeHeartbeatDialog(int changeHeartbeat) {
        LayoutInflater factory = LayoutInflater.from(this);// 提示框
        final View view = factory.inflate(R.layout.change_heartbeat_dialog, null);
        final EditText edHeartbeat = (EditText) view.findViewById(R.id.et_heartbeat);
        Button btnCancel = (Button) view.findViewById(R.id.btn_dialog_cancel);
        Button btnOk = (Button) view.findViewById(R.id.btn_dialog_ok);

        final AlertDialog mDialog = new AlertDialog.Builder(this)// 提示框标题
                .setView(view).create();
        mDialog.show();

        edHeartbeat.setText(changeHeartbeat + "");
        edHeartbeat.setSelection((changeHeartbeat + "").length());

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int heartNum = Integer.parseInt(edHeartbeat.getText().toString());
                if (0 < heartNum && heartNum <= 600) {
                    mSettings.setChangeHeartbeat(heartNum);
                    mSettings.save();
                    changeHeartbeatShow.setText(heartNum + "秒");
                    startService(new Intent(SettingActivity.this, MyService.class).putExtra("heart", true));
                } else {
                    ToastR.setToast(getBaseContext(), getResources().getString(R.string.heart_set_error));
                }
                mDialog.dismiss();
            }
        });
    }
}
