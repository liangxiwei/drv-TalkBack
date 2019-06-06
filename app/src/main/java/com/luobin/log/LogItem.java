package com.luobin.log;

import java.util.Date;

/**
 * Created by quhuabo on 2017/10/31 0031.
 */

public class LogItem {
    private int code;
    private String desc;
    private Date date;

    public LogItem() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
