package com.example.jrd48.chat.group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.luobin.dvr.R;

import java.util.List;

/**
 * Created by Administrator on 2016/12/27.
 */

public class ShowTeamInfoPrompt {
    public void dialogSTeamInfo(Context context, final TeamInfo teamInfo) {
        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.show_group_msg, null);// 这里必须是final的
        final TextView name = (TextView) view.findViewById(R.id.change_et_group_name);
        final TextView priority = (TextView) view.findViewById(R.id.change_et_priority);
        final TextView describe = (TextView) view.findViewById(R.id.change_et_group_describe);//
        name.setText(teamInfo.getTeamName());
        priority.setText(teamInfo.getTeamPriority() + "");
        describe.setText(teamInfo.getTeamDesc());
        int teamType = teamInfo.getTeamType();
        new AlertDialog.Builder(context).setTitle("查看群组信息")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create().show();

    }
}
