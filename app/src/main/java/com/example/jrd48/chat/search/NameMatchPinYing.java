package com.example.jrd48.chat.search;

import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */

public class NameMatchPinYing {
    private List<String> name;
    private List<String> pinyinlist;
    private String pinyin;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getPinyinlist() {
        return pinyinlist;
    }

    public void setPinyinlist(List<String> pinyinlist) {
        this.pinyinlist = pinyinlist;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

}
