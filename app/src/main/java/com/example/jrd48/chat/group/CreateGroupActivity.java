package com.example.jrd48.chat.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.MainActivity;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.CreateGroupProcesser;

/**
 * Created by Administrator on 2016/12/8.
 */

public class CreateGroupActivity extends BaseActivity {
    private Button mBtnCreate;
    private EditText mEtGroupName;
    private EditText mEtGroupDescribe;
    private EditText mEtPriority;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioTempo;
    private RadioButton mRadioPublic;
    private RadioButton mRadioPrivate;
    private static int defualtData = 0;
    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    private int teamType = ProtoMessage.TeamType.teamTempo_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group);
        initView();
    }

    private void initView() {
        mBtnCreate = (Button) findViewById(R.id.btn_create_group);
        mBtnCreate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCreateGroupMsg();
            }
        });
        mEtGroupName = (EditText) findViewById(R.id.et_group_name);
        mEtPriority = (EditText) findViewById(R.id.et_priority);
        mEtPriority.setText("0");
        mEtPriority.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                show();
            }
        });
        mEtGroupDescribe = (EditText) findViewById(R.id.et_group_describe);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioPublic = (RadioButton) findViewById(R.id.radio_public);
        mRadioPrivate = (RadioButton) findViewById(R.id.radio_private);
        mRadioTempo = (RadioButton) findViewById(R.id.radio_temporary);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == mRadioPublic.getId()) {
                    teamType = ProtoMessage.TeamType.teamPublic_VALUE;
                } else if (i == mRadioPrivate.getId()) {
                    teamType = ProtoMessage.TeamType.teamPrivate_VALUE;
                } else {
                    teamType = ProtoMessage.TeamType.teamTempo_VALUE;
                }
            }
        });


        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(true);
        m_pDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (checkDialog) {
                    getBroadcastManager().stopAll();
                    ToastR.setToast(CreateGroupActivity.this, "取消创建群组");
                }
            }
        });
        //********************************************弹窗设置****************************************************


    }

    private void checkCreateGroupMsg() {
        String groupName = mEtGroupName.getText().toString();
        String groupDescribe = mEtGroupDescribe.getText().toString();
        String groupPriority = mEtPriority.getText().toString();
        if (groupName.length() <= 0) {
            ToastR.setToastLong(this, "请输入群名");
            return;
        } else if (groupName.length() > GlobalStatus.MAX_TEXT_COUNT){
            ToastR.setToastLong(this, "群名输入过长（最大只能设置16个字符）");
            return;
        }
        if (groupDescribe.length() > GlobalStatus.MAX_TEXT_COUNT) {
            ToastR.setToastLong(this, "群描述信息输入过长（最大只能设置16个字符）");
            return;
        }
//        if (groupDescribe.length() <= 0) {
//            ToastR.setToast(this, "请输入群描述信息");
//            return;
//        }
        uploadCreateGroupMsg(groupName, groupDescribe, groupPriority);
    }

    private void uploadCreateGroupMsg(String groupName, String groupDescribe, String groupPriority) {
        int a = 0;
        checkDialog = true;
        m_pDialog.show();
        try {
            a = Integer.parseInt(groupPriority);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setTeamName(groupName);
        builder.setTeamDesc(groupDescribe);
        builder.setTeamType(teamType);
        builder.setTeamPriority(a);
//        builder.setMyTeamName();
        MyService.start(CreateGroupActivity.this, ProtoMessage.Cmd.cmdCreateTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(CreateGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(CreateGroupActivity.this, filter, getBroadcastManager());

        b.startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                checkDialog = false;
                m_pDialog.cancel();
                ToastR.setToast(CreateGroupActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                checkDialog = false;
                m_pDialog.cancel();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    successBack();
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void successBack() {
        ToastR.setToast(CreateGroupActivity.this, "建群成功");
        CreateGroupActivity.this.sendBroadcast(new Intent(MainActivity.TEAM_ACTION));
        Intent intent = new Intent();
        intent.putExtra("data", 1);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void fail(int i) {
        new ResponseErrorProcesser(CreateGroupActivity.this, i);
    }

    /**
     * 按钮onClick事件重写
     *
     * @param view
     */
    public void onOkBack(View view) {
        m_pDialog.cancel();
//        startActivity(new Intent(CreateGroupActivity.this, AddGroupActivity.class));
        Intent intent = new Intent();
        intent.putExtra("data", 0);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 这里重写返回键
            m_pDialog.cancel();
//            startActivity(new Intent(CreateGroupActivity.this, AddGroupActivity.class));
            Intent intent = new Intent();
            intent.putExtra("data", 0);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }


    public void show() {

        new ModifyPriorityPrompt().dialogModifyPriorityRequest(this, "", defualtData, new ModifyPrioritytListener() {
            @Override
            public void onOk(int data) {
                defualtData = data;
                mEtPriority.setText(String.valueOf(data)); //set the value to textview
                mEtPriority.setSelection(mEtPriority.length());
            }
        });
//        final AlertDialog dlg = new AlertDialog.Builder(this).create();
//        dlg.setCancelable(true);
//        dlg.show();
//        Window window = dlg.getWindow();
//        window.setContentView(R.layout.dialog);
//        Button b1 = (Button) window.findViewById(R.id.button1);
//        Button b2 = (Button) window.findViewById(R.id.button2);
//        final NumberPicker np = (NumberPicker) window.findViewById(R.id.numberPicker1);
//        np.setMaxValue(15); // max value 100
//        np.setMinValue(1);   // min value 0
//        np.setValue(defualtData);
//        np.setWrapSelectorWheel(false);
//        b1.setOnClickListener(new OnClickListener()
//        {
//            @Override
//            public void onClick(View v) {
//                defualtData = np.getValue();
//                mEtPriority.setText(String.valueOf(np.getValue())); //set the value to textview
//                mEtPriority.setSelection(mEtPriority.length());
//                dlg.cancel();
//            }
//        });
//        b2.setOnClickListener(new OnClickListener()
//        {
//            @Override
//            public void onClick(View v) {
//                dlg.cancel();
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        defualtData = 0;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}