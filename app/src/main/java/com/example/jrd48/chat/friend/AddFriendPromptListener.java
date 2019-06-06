package com.example.jrd48.chat.friend;

public interface AddFriendPromptListener {
	void onOk(String msg, String remark);

	void onFail(String typ);
}
