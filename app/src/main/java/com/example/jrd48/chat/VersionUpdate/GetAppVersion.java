package com.example.jrd48.chat.VersionUpdate;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Administrator on 2017/1/13.
 */

public class GetAppVersion {

    @SuppressWarnings("unused")
    private Context context;
    private String versionName;

    private int versionCode;

    public GetAppVersion(Context context) {
        this.context = context;
        getAppVersionName(context);
    }

    /**
     * 返回当前程序版本名
     */
    public void getAppVersionName(Context context) {
        setVersionCode(-1);
        versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            setVersionCode(pi.versionCode);
            if (versionName == null || versionName.length() <= 0) {
                versionName = "";
                setVersionCode(-1);
                return;
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }

    }

    public int getVersionCode() {
        return versionCode;
    }

    private void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    @SuppressWarnings("unused")
    private void setVersionName(String versionName) {
        this.versionName = versionName;
    }

}
