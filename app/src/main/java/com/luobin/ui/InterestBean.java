package com.luobin.ui;

import java.io.Serializable;

/**
 * @author wangjunjie
 */
public class InterestBean implements Serializable {
    private String name = "";
    private boolean isChecked = false;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
