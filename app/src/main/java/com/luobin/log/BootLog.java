package com.luobin.log;

import android.content.Context;

import com.example.jrd48.service.MyLogger;

import java.io.File;
import java.util.Date;

/*//import org.apache.commons.io.FileUtils;*/

/**
 * Created by quhuabo on 2017/10/31 0031.
 */

public class BootLog {

    public static boolean isEnableTouch() {
        return mEnableTouch;
    }

    public static void setEnableTouch(boolean x) {
        BootLog.mEnableTouch = x;
        MyLogger.jLog().w("设置 touch 状态：" + x);
    }

    private static boolean mEnableTouch = true;

    private static String getFileName(Context context) {
        return context.getFilesDir() + File.separator + "check_poweroff";
    }

    public synchronized static void touch(Context context) {
        if (!mEnableTouch) {
            MyLogger.jLog().w("停止touch bootlog file");
            return;
        }
        try {
            File f = new File(getFileName(context));
            //FileUtils.touch(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeTouch(Context context) {
        try {
            File f = new File(getFileName(context));
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 停止记录事件表
        setEnableTouch(false);

    }

    public static void check_poweroff(Context context) {
        try {
            File f = new File(getFileName(context));
            if (f.exists()) {
                Date dt = new Date(f.lastModified());
                DBMyLogHelper.insertLog(context, LogCode.POWER_OFF, null, dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
