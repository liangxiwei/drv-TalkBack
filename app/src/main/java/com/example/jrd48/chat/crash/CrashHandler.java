package com.example.jrd48.chat.crash;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.receiver.ToastReceiver;
import com.luobin.dvr.DvrService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import me.lake.librestreaming.client.RESClient;


public class CrashHandler implements UncaughtExceptionHandler {

    private static CrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例

    private CrashHandler() {
    }

    public synchronized static CrashHandler getInstance() {  //同步方法，以免单例多线程环境下出现异常
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public static String getStackTrace(Throwable t) {

      /* prints this throwable and its backtrace to the specified
      print writer. */
        Writer wr = new StringWriter();
        PrintWriter pWriter = new PrintWriter(wr);
        t.printStackTrace(pWriter);
        return wr.toString();
    }

    public void init(Context ctx) {  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e("pocdemo", "---------------------------- start crash exception ----------------------------");
        MyLog.e("uncaughtException: " + getStackTrace(throwable));
        DvrService dvrService = RESClient.getInstance().getDvrService();
        if(dvrService != null) {
            dvrService.stopRecord();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (GlobalStatus.getUsbVideo1() != null) {
            GlobalStatus.getUsbVideo1().close();
            GlobalStatus.setUsbVideo1(null);
        }
        if (GlobalStatus.getUsbVideo2() != null) {
            GlobalStatus.getUsbVideo2().close();
            GlobalStatus.setUsbVideo2(null);
        }
        if (GlobalStatus.getCamera() != null) {
            GlobalStatus.getCamera().stopPreview();
            GlobalStatus.getCamera().release();
            GlobalStatus.setCamera(null);
        }

        System.exit(0);
        Log.e("pocdemo", "============================= end crash exception =============================");
    }
}