package com.example.jrd48.service.protocol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.voice.MediaPlayerTool;

/**
 * Created by Administrator on 2016/12/30.
 */

public class ResponseErrorProcesser {
    private Context context;
    private int errCode;


    public ResponseErrorProcesser(Context context, int errCode) {
        this.context = context;
        this.errCode = errCode;
        process();
    }

    private void process() {
        try {
            Log.i("errCode", "失败：" + errCode);
            switch (errCode) {
                case ProtoMessage.ErrorCode.UNKNOWN_VALUE:
                    Log.i("jrdchat", "未知错误");
                    //ToastR.setToast(context, "未知错误");
                    break;
                case ProtoMessage.ErrorCode.NOT_LOGIN_VALUE:
                    // 强制下线
                    SharedPreferences preferences=context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    if(TextUtils.isEmpty(preferences.getString("token",""))) {
                        Intent intent = new Intent("com.example.jrd48.chat.FORCE_OFFLINE");//测试强制下线功能
                        context.sendBroadcast(intent);
                    }
                    break;
                case ProtoMessage.ErrorCode.TOO_MANY_TEAMS_VALUE:
                    ToastR.setToast(context, "群组的数量已超出限制");
                    break;
                case ProtoMessage.ErrorCode.CHANGE_NOTHING_VALUE:
                    ToastR.setToast(context, "没有任何更改");
                    break;
                case ProtoMessage.ErrorCode.TEAM_NAME_TOO_SHORT_VALUE:
                    ToastR.setToast(context, "群名输入太短");
                    break;
                case ProtoMessage.ErrorCode.NOT_IN_ANY_CHAT_ROOM_VALUE: {
                    ToastR.setToast(context, "不在任何对讲聊天室");
                    Intent i = new Intent(AutoCloseProcesser.ACTION);
                    long roomId = GlobalStatus.getRoomID();
                    GlobalStatus.equalRoomID(roomId);
                    i.putExtra("error_code", ProtoMessage.ErrorCode.OK_VALUE);
                    i.putExtra("roomID", roomId);
                    context.sendBroadcast(i);
                    MediaPlayerTool.getInstance().play(context, R.raw.failed);
                    break;
                }
                case ProtoMessage.ErrorCode.NOT_TEAM_MEMBER_VALUE:
                    ToastR.setToast(context, "不是当前群的成员");
                    break;
                case ProtoMessage.ErrorCode.OTHER_SPEAKING_NOW_VALUE:
                    ToastR.setToast(context, "其他人在讲话中，未能抢占话权", Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    break;
                case ProtoMessage.ErrorCode.NOT_FOUND_USER_NAME_OR_PHONE_VALUE:
                    ToastR.setToast(context, "未找到相关好友");
                    break;
                case ProtoMessage.ErrorCode.LOGIN_PHONE_OR_PASS_WRONG_VALUE:
                    ToastR.setToast(context, "登录手机号或者密码错误");
                    break;
                case ProtoMessage.ErrorCode.MSG_ATTACHMENT_DOWNLOAD_NO_MSG_SUM_VALUE:
                    ToastR.setToast(context, "附件尚未上传完毕");
                    break;
                case ProtoMessage.ErrorCode.NOT_MY_TEAM_VALUE:
                    ToastR.setToast(context, "不是我的群");
                    break;
                case ProtoMessage.ErrorCode.SMS_CODE_NOT_GOT_VALUE:
                    ToastR.setToast(context, "请重新获取验证码");
                    break;
                case ProtoMessage.ErrorCode.USER_PHONE_EXIST_VALUE:
                    ToastR.setToast(context, "该用户已注册");
                    break;
                case ProtoMessage.ErrorCode.NOT_TEAM_ADMIN_VALUE:
                    ToastR.setToast(context, "操作失败，需要管理权限");
                    break;
                case ProtoMessage.ErrorCode.TEAM_NAME_EXIST_VALUE:
                    ToastR.setToast(context, "该群名已存在");
                    break;
                case ProtoMessage.ErrorCode.SMS_CODE_NOT_MATCH_VALUE:
                    ToastR.setToast(context, "输入短信验证码错误");
                    break;
                case ProtoMessage.ErrorCode.NAME_TOO_SHORT_VALUE:
                    ToastR.setToast(context, "输入名称太短");
                    break;
                case ProtoMessage.ErrorCode.USER_NAME_EXIST_VALUE:
                    ToastR.setToast(context, "名称重复");
                    break;
                case ProtoMessage.ErrorCode.NO_ANY_TEAM_MEMBER_VALUE:
                    ToastR.setToast(context, "呼叫失败，群里没有其他成员");
                    break;
                case 1015:
                    ToastR.setToast(context, "添加失败，已经是群成员了");
                    break;
                default:
                    ToastR.setToast(context, "操作失败：" + errCode);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
