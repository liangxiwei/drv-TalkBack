package com.example.jrd48.chat.friend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.example.jrd48.GlobalStatus;
import com.luobin.dvr.R;
import com.luobin.utils.ButtonUtils;

public class AddFriendPrompt {
    public static final String TYP = "short";
    public static void dialogFriendsRequest(Context context, String msg, String userRemark, String userNameMe, final AddFriendPromptListener func) {
        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.friend_request_editbox_layout, null);// 这里必须是final的
        final EditText edit = (EditText) view.findViewById(R.id.msg_editText);// 获得输入框对象
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        editRemark.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editRemark.setText(userRemark);
        editRemark.setSelection(userRemark.length());// 将光标追踪到内容的最后
        edit.setText("我是" + userNameMe + msg);
        edit.setSelection(edit.length());
        new AlertDialog.Builder(context).setTitle("信息设置")// 提示框标题
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                ButtonUtils.changeLeftOrRight(true);
                                return true;
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                ButtonUtils.changeLeftOrRight(false);
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String msg = edit.getText().toString().trim();
                        String remark = editRemark.getText().toString().trim();
                        if (msg.length() <= 0 || remark.length() <= 0) {
                            func.onFail(TYP);
                        }else if (remark.length() > GlobalStatus.MAX_TEXT_COUNT){
                            func.onFail("");
                        } else {
                            func.onOk(remark, msg);
                        }

                        dialog.dismiss();
                    }

                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                    }
//                })
                .create().show();

    }
}
