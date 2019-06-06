package com.video;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SQLite.SQLiteTool;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangsheng on 2017/3/17.
 */

public class VideoInvitedActivity extends BaseActivity implements PermissionUtil.PermissionCallBack {
    private static final String TAG = "InstantCallInviteDialog";
    public static final String OTHER_PHONE = "other_phone";
    public static final String TEAM_ID = "team_id";
    private Dialog mInstantInviteDialog;
    private String otherPhone;
    private long teamId;
    private Context context;
    private GetGroupMemberReceiiver getGroupMemberReceiiver;
    private PermissionUtil mPermissionUtil;
    int MY_PERMISSIONS_REQUEST_VIDEO = 10055;
    public static void startActivity(Context context, String otherPhone, long teamId) {
        Intent recordIntent = new Intent(context, VideoInvitedActivity.class);
        recordIntent.putExtra(TEAM_ID, teamId);
        recordIntent.putExtra(OTHER_PHONE, otherPhone);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(recordIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        getWindow().addFlags(flags);
        context = this;
        mPermissionUtil = PermissionUtil.getInstance();
        handleIntent(getIntent());
    }


    public void handleIntent(Intent intent) {
        teamId = intent.getLongExtra(TEAM_ID, 0);
        otherPhone = intent.getStringExtra(OTHER_PHONE);
        showInvitationDialog();
        setIntent(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (getGroupMemberReceiiver != null) {
            unregisterReceiver(getGroupMemberReceiiver);
        }
        super.onDestroy();
    }

    private void showInvitationDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.video_invited_dialog, null);

        ImageView orgPhoto = (ImageView) view.findViewById(R.id.call_invite_photo);
        TextView content = (TextView) view.findViewById(R.id.call_invite_message);
        Button answerBtn = (Button) view.findViewById(R.id.accept);
        Button declineBtn = (Button) view.findViewById(R.id.cancel);

        if (teamId == 0) {
            Bitmap bitmap = GlobalImg.getImage(this, otherPhone);
            if (bitmap == null) {
                orgPhoto.setImageResource(R.drawable.man);
            } else {
                orgPhoto.setImageBitmap(bitmap);
            }
            content.setText(getString(R.string.video_invited_single_content, MsgTool.getFriendName(this, otherPhone)));
        } else {
            Bitmap bitmap = GlobalImg.getImage(this, "team" + teamId);
            if (bitmap == null) {
                orgPhoto.setImageResource(R.drawable.group);
            } else {
                orgPhoto.setImageBitmap(bitmap);
            }
            content.setText(getString(R.string.video_invited_team_content, MsgTool.getTeamName(this, teamId)));
        }
        answerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnUtil.checkNetworkWifi(context)) {
                    checkVideoPermission();
                } else {
                    Dialog dialog = new AlertDialog.Builder(context).setTitle("提示：")
                            .setMessage(getString(R.string.video_not_wifi)).setPositiveButton("确定",
                                    new android.content.DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            checkVideoPermission();
                                        }
                                    })
                            .setNegativeButton("取消", new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    dismissInstantInviteDialog();
                                    finish();
                                }
                            }).create();
                    dialog.show();
                }
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismissInstantInviteDialog();
                finish();
            }
        });

        if (mInstantInviteDialog != null && mInstantInviteDialog.isShowing()) {
            dismissInstantInviteDialog();
        }
        if (mInstantInviteDialog == null) {
            mInstantInviteDialog = new Dialog(this, R.style.BottomNoTitleDialogStyle);

            mInstantInviteDialog.setCanceledOnTouchOutside(false);
            mInstantInviteDialog.setCancelable(false);
        }
        mInstantInviteDialog.setContentView(view);
        Window mWindow = mInstantInviteDialog.getWindow();
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setGravity(Gravity.BOTTOM);
        mInstantInviteDialog.show();
    }

    private void startVideoActivity() {
        if (teamId == 0) {
            VideoCallActivity.startActivity(context, otherPhone);
        } else {
            startActivity(context, teamId, (ArrayList<TeamMemberInfo>) SQLiteTool.getTeamMembers(context, teamId));
//            VideoCallActivity.startActivity(context, teamId, (ArrayList<TeamMemberInfo>) SQLiteTool.getTeamMembers(context, teamId));
        }
    }


    public void startActivity(Context context, long teamId, ArrayList<TeamMemberInfo> mTeamMemberInfos) {
        ArrayList<TeamMemberInfo> teamMemberInfos = null;
        if (mTeamMemberInfos == null || mTeamMemberInfos.size() <= 0) {
            getGroupMember(context, teamId);
        } else {
            teamMemberInfos = mTeamMemberInfos;
            Intent recordIntent = new Intent(context, VideoCallActivity.class);
            recordIntent.putExtra(TEAM_ID, teamId);
            recordIntent.putParcelableArrayListExtra(VideoCallActivity.GROUP_MEMBER, teamMemberInfos);
            recordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(recordIntent);
        }
    }

    private void getGroupMember(final Context context, final long teamId) {
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(teamId);
        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TeamMemberProcesser.ACTION);
        getGroupMemberReceiiver = new GetGroupMemberReceiiver();
        registerReceiver(getGroupMemberReceiiver, filter);
    }

    class GetGroupMemberReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int code = intent.getIntExtra("error_code", -1);
            if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                Intent recordIntent = new Intent(context, VideoCallActivity.class);
                recordIntent.putExtra(TEAM_ID, teamId);
                recordIntent.putParcelableArrayListExtra(VideoCallActivity.GROUP_MEMBER, (ArrayList<TeamMemberInfo>)
                        SQLiteTool.getTeamMembers(context, teamId));
                recordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(recordIntent);
                //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
            } else if (code == ProtoMessage.ErrorCode.TEAM_NOT_EXIST_VALUE ||
                    code == ProtoMessage.ErrorCode.NOT_TEAM_MEMBER_VALUE) {
                ToastR.setToast(context, "你已经不是这个群成员（被踢出），或者群已经被解散");
            } else {
                new ResponseErrorProcesser(VideoInvitedActivity.this, code);
            }
        }
    }

    private void dismissInstantInviteDialog() {
        if (mInstantInviteDialog != null) {
            mInstantInviteDialog.dismiss();
            mInstantInviteDialog = null;
        }
    }

    private void checkVideoPermission() {
        List<String> mList = new ArrayList<String>();
        mList.add(PermissionUtil.PERMISSIONS_STORAGE);
        mList.add(PermissionUtil.PERMISSIONS_CAMERA);
        mList.add(PermissionUtil.PERMISSIONS_RECORD_AUDIO);
        List<String> mDeniedPermissionList = mPermissionUtil.findDeniedPermissions(VideoInvitedActivity.this, mList);
        if (mDeniedPermissionList != null && mDeniedPermissionList.size() > 0) {
            mPermissionUtil.requestPermission(VideoInvitedActivity.this, MY_PERMISSIONS_REQUEST_VIDEO, VideoInvitedActivity.this, mDeniedPermissionList.get(0), mDeniedPermissionList.get(0));
        } else {
            startVideoActivity();
            dismissInstantInviteDialog();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        mPermissionUtil.requestResultString(this, permissions, grantResults, this);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                String permission = permissions[i];
                if (permission.equals(PermissionUtil.PERMISSIONS_RECORD_AUDIO)) {
                    ToastR.setToastLong(context, "[ 录音 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                } else if (permission.equals(PermissionUtil.PERMISSIONS_LOCATION)) {
                    ToastR.setToastLong(context, "[ 定位 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                } else if (permission.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
                    ToastR.setToastLong(context, "[ 摄像 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                } else if (permission.equals(PermissionUtil.PERMISSIONS_STORAGE)){
                    ToastR.setToastLong(context, "[ 存储 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                }
            } else {
                checkVideoPermission();
            }
        }
    }

    @Override
    public void onPermissionSuccess(String type) {
        checkVideoPermission();
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(context, strMessage + "权限已经拒绝，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onPermissionFail(String failType) {

    }
}
