package com.example.jrd48.chat.group;

import android.app.ActionBar;
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
import android.widget.ImageView;
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
import com.luobin.ui.BaseDialogActivity;

/**
 * Created by Administrator on 2016/12/8.
 */

public class CreateGroupActivity extends BaseDialogActivity {
    private Button mBtnCreate;
    private EditText mEtGroupName;
    private EditText mEtGroupDescribe;
    private RadioGroup mRadioGroup;
    private ImageView imgClose;
    private static int defualtData = 0;
    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    private int teamType = ProtoMessage.TeamType.teamTempo_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
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
        mEtGroupDescribe = (EditText) findViewById(R.id.et_group_describe);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);




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
        imgClose = (ImageView) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_pDialog.cancel();
//            startActivity(new Intent(CreateGroupActivity.this, AddGroupActivity.class));
                Intent intent = new Intent();
                intent.putExtra("data", 0);
                setResult(RESULT_OK, intent);
                finish();

            }
        });

    }

    private void checkCreateGroupMsg() {
        String groupName = mEtGroupName.getText().toString();
        String groupDescribe = mEtGroupDescribe.getText().toString();
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
        uploadCreateGroupMsg(groupName, groupDescribe);
    }

    private void uploadCreateGroupMsg(String groupName, String groupDescribe) {
        int a = 0;
        checkDialog = true;
        m_pDialog.show();
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
            }
        });

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