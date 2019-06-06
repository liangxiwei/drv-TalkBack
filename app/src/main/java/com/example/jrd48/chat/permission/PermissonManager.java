package com.example.jrd48.chat.permission;

import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2017/2/22.
 */

public class PermissonManager {
    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;
    private String permisson;

    public RelativeLayout getmRelativeLayout() {
        return mRelativeLayout;
    }

    public void setmRelativeLayout(RelativeLayout mRelativeLayout) {
        this.mRelativeLayout = mRelativeLayout;
    }

    public ImageView getmImageView() {
        return mImageView;
    }

    public void setmImageView(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    public String getPermisson() {
        return permisson;
    }

    public void setPermisson(String permisson) {
        this.permisson = permisson;
    }
}
