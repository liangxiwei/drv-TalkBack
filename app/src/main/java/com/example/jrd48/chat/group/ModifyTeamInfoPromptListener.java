package com.example.jrd48.chat.group;

/**
 * Created by Administrator on 2016/12/27.
 */

public interface ModifyTeamInfoPromptListener {
    void onOk(TeamInfo team);

    void onFail(boolean check, String string);
}
