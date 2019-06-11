package com.example.jrd48.chat.friend;

public class ViewFriendsMsg {
    public AppliedFriends friends;
    public String pinyin;
    private boolean bChecked = false;

    public ViewFriendsMsg() {
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public boolean isbChecked() {
        return bChecked;
    }

    public void setbChecked(boolean bChecked) {
        this.bChecked = bChecked;
    }
}
