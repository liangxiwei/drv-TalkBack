package com.example.jrd48.chat.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Date;

/**
 * Created by qhb on 17-6-13.
 */

public class SysUtil {
    private static final String TAG = "SysUtil";

    public static String getSysVersion() {
        return "Android(" + Build.VERSION.RELEASE + "): " +/* Build.PRODUCT+", "+*/ Build.DISPLAY + "-" + Build.TYPE + ", "
                + MyDateUtil.formatDate(new Date(Build.TIME));
    }

    public static int getVersionCode(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
