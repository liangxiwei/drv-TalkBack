package com.example.jrd48.chat.group;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamInfoProcesser;

/**
 * Created by Administrator on 2017/2/6.
 */

public class ModifyTeamMsgActivity extends BaseActivity {
    private ImageView mImageView;
    private EditText name;
    private TextView etPriority;
    private EditText describe;
    private Button btnSureModify;
    private static int priority = 0;
    private Context mContext;
    private long teamId;
    private int mPriority;
    private TeamInfo mTeamInfo;
    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_team_msg);
        mContext = this;
        getIntentMsg();
        initView();
        InitBroadcast();
    }

    private void InitBroadcast() {
        IntentFilter filterDelete = new IntentFilter();
        filterDelete.addAction("ACTION.refreshTeamList");
        registerReceiver(refreshTeamReceiver, filterDelete);
    }

    private void getIntentMsg() {
        Intent intent = getIntent();
        mTeamInfo = intent.getParcelableExtra("modify_team_msg");
        priority = mTeamInfo.getTeamPriority();
        setIntent(null);
    }

    private void initView() {
        mImageView = (ImageView) findViewById(R.id.iv_back);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        name = (EditText) findViewById(R.id.change_et_group_name);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > GlobalStatus.MAX_TEXT_COUNT){
                    name.setText(s.subSequence(0,16));
                    name.setSelection(name.length());
                    ToastR.setToast(ModifyTeamMsgActivity.this,"群名称最大只能设置16个字符");
                }
            }
        });
        etPriority = (TextView) findViewById(R.id.change_et_priority);
        describe = (EditText) findViewById(R.id.change_et_group_describe);
        name.setText(mTeamInfo.getTeamName());
//        name.setSelection(name.length());
        etPriority.setText(mTeamInfo.getTeamPriority() + "");
        describe.setText(mTeamInfo.getTeamDesc());
        etPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ModifyPriorityPrompt().dialogModifyPriorityRequest(ModifyTeamMsgActivity.this, "", priority, new ModifyPrioritytListener() {
                    @Override
                    public void onOk(int data) {
                        priority = data;
                        etPriority.setText(data + "");
                    }
                });
            }
        });
        btnSureModify = (Button) findViewById(R.id.btn_sure_modify);
        btnSureModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkModifyTeamMsg();
            }
        });

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
                    ToastR.setToast(mContext, "取消修改群组信息");
                }
            }
        });


    }

    private void checkModifyTeamMsg() {
        String teamName = name.getText().toString().trim();
        String teamPriority = etPriority.getText().toString().trim();
        String teamDescribe = describe.getText().toString().trim();
        if (teamName.length() <= 0 ) {
            ToastR.setToastLong(mContext, "群名输入不能为空");
            return;
        } else if (teamName.length() > GlobalStatus.MAX_TEXT_COUNT){
            ToastR.setToastLong(this, "群名输入过长（最大只能设置16个字符）");
            return;
        }
        if (teamDescribe.length() > GlobalStatus.MAX_TEXT_COUNT) {
            ToastR.setToastLong(this, "群描述信息输入过长（最大只能设置16个字符）");
            return;
        }
        TeamInfo tm = new TeamInfo();
        tm.setTeamName(teamName);
        tm.setTeamPriority(Integer.parseInt(teamPriority));
        tm.setTeamDesc(teamDescribe);
        tm.setTeamID(mTeamInfo.getTeamID());
        toModifyTeamInfo(tm);
    }

    public void dismissDialogShow() {
        checkDialog = false;
        m_pDialog.dismiss();
    }

    public void showMyDialog() {
        checkDialog = true;
        m_pDialog.show();
    }

    private BroadcastReceiver refreshTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long teamid = intent.getLongExtra("teamid", 0);
            if (intent.hasExtra("singout") && (teamid == mTeamInfo.getTeamID())) {
                finish();
            }
        }
    };
    @Override
    protected void onDestroy() {
        try {
            if (refreshTeamReceiver != null) {
                unregisterReceiver(refreshTeamReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /*
   修改群组信息
    */
    private void toModifyTeamInfo(final TeamInfo tm) {
        showMyDialog();
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setTeamName(tm.getTeamName());
        //  builder.setTeamType(tm.getTeamType());
        builder.setTeamDesc(tm.getTeamDesc());
        builder.setTeamPriority(tm.getTeamPriority());
        builder.setTeamID(tm.getTeamID());
        MyService.start(mContext, ProtoMessage.Cmd.cmdModifyTeamInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModifyTeamInfoProcesser.ACTION);
        new TimeoutBroadcast(ModifyTeamMsgActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {

                    modifyDBitem(tm);
                    ToastR.setToast(mContext, "修改群组信息成功");

                    Intent intent = new Intent();
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable("modify_team", tm);
                    intent.putExtra("data", 1);
                    intent.putExtra("teamName", tm.getTeamName());
                    intent.putExtra("teamDesc", tm.getTeamDesc());
                    intent.putExtra("priority", tm.getTeamPriority());
//                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(mContext, i);
    }

    private void modifyDBitem(TeamInfo tm) {
        try {
            DBManagerTeamList db = new DBManagerTeamList(mContext, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
            db.updateData(tm);
            db.closeDB();
            // 遍历删除内存数据
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
