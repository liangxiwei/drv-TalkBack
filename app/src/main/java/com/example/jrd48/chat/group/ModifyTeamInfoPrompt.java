package com.example.jrd48.chat.group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.util.List;

/**
 * Created by Administrator on 2016/12/27.
 */

public class ModifyTeamInfoPrompt {
    public static String typePriority = "priority";
    public static String emptyEmpty = "hasempty";
    public static String typeOK = "ok";

    public void dialogModifyTeamInfoRequest(Context context, final List<TeamInfo> allTeamInfos, final int id, final ModifyTeamInfoPromptListener func) {
        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.change_group_msg, null);// 这里必须是final的
        final EditText name = (EditText) view.findViewById(R.id.change_et_group_name);
        final EditText priority = (EditText) view.findViewById(R.id.change_et_priority);
        final EditText describe = (EditText) view.findViewById(R.id.change_et_group_describe);//
//        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.change_radioGroup);// 获得输入框对象
//        final RadioButton radioTemporary = (RadioButton)view.findViewById(R.id.change_radio_temporary);
//        final RadioButton radioPublic = (RadioButton)view.findViewById(R.id.change_radio_public);
//        final RadioButton radioPrivate = (RadioButton)view.findViewById(R.id.change_radio_private);
        name.setText(allTeamInfos.get(id).getTeamName());
        name.setSelection(name.length());// 将光标追踪到内容的最后
        priority.setText(allTeamInfos.get(id).getTeamPriority() + "");
        describe.setText(allTeamInfos.get(id).getTeamDesc());
        int teamType = allTeamInfos.get(id).getTeamType();
//        if (teamType == ProtoMessage.TeamType.teamPublic_VALUE){
//            radioPublic.setChecked(true);
//        }else if(teamType == ProtoMessage.TeamType.teamPrivate_VALUE){
//            radioPrivate.setChecked(true);
//        }else{
//            radioTemporary.setChecked(true);
//        }
        new AlertDialog.Builder(context).setTitle("修改群组信息")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = typeOK;
                        String teamName = name.getText().toString().trim();
                        String teamPriority = priority.getText().toString().trim();
                        String teamDescribe = describe.getText().toString().trim();

                        if (teamName.length() <= 0 || teamPriority.length() <= 0 || teamDescribe.length() <= 0) {
                            str = emptyEmpty;
                        } else {
                            int b = Integer.parseInt(teamPriority);
                            if (b > 0 && b <= 15) {
                                str = typeOK;
                            } else {
                                str = typePriority;
                            }
                        }
                        if (!str.equals(typeOK)) {
                            func.onFail(true, str);
                        } else {
                            TeamInfo tm = new TeamInfo();
                            tm.setTeamName(teamName);
                            tm.setTeamPriority(Integer.parseInt(teamPriority));
                            tm.setTeamDesc(teamDescribe);
//                            if (radioTemporary.isChecked()) {
//                                tm.setTeamType(ProtoMessage.TeamType.teamTempo_VALUE);
//                            } else if (radioPublic.isChecked()) {
//                                tm.setTeamType(ProtoMessage.TeamType.teamPublic_VALUE);
//                            } else {
//                                tm.setTeamType(ProtoMessage.TeamType.teamPrivate_VALUE);
//                            }
                            tm.setTeamID(allTeamInfos.get(id).getTeamID());
                            func.onOk(tm);
                        }
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create().show();

    }
}
