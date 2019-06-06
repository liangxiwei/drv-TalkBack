package com.example.jrd48.chat.search;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.example.jrd48.chat.MainActivity;
import com.luobin.dvr.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationUtil {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    MainActivity context;
    public static final String CANCEL = "cancel";
    public static final String LOAD = "load";
    public static final String INSTALL = "install";
    public static final String COMPLETE = "Complete";
    public static final String FAIL = "fail";
    public static final String SHOW_NOTIFICATION = "show";
    private boolean refreshTime = false;
    int progress;
    public static final int NOTIFYCATIONID = 1001;

    public NotificationUtil(MainActivity context) {
        this.context = context;
        initNotifycation();
    }

    private void initNotifycation() {
        mNotificationManager = (NotificationManager) context
                .getSystemService(context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(
                context);
        mBuilder.setWhen(System.currentTimeMillis()).setSmallIcon(
                R.mipmap.ic_launcher);
    }

    public void showProgressNotify(int progress, String type) {
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_NO_CLEAR;
        // 设置通知的显示视图
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_view);
        remoteViews.setViewVisibility(R.id.btn_install, View.GONE);
        remoteViews.setViewVisibility(R.id.repeat_download, View.GONE);
        if (type.equals(FAIL)) {
            Intent load = new Intent(MainActivity.DOWNLOAD_APK);
            load.putExtra(MainActivity.click, LOAD);
            load.putExtra(MainActivity.TYPE, NOTIFYCATIONID);
            PendingIntent pauseLoad = PendingIntent.getBroadcast(context, 1, load, PendingIntent.FLAG_ONE_SHOT);
            remoteViews.setViewVisibility(R.id.btn_install, View.GONE);
            remoteViews.setViewVisibility(R.id.repeat_download, View.VISIBLE);
            remoteViews.setOnClickPendingIntent(R.id.repeat_download, pauseLoad);
        }

        if (type.equals(COMPLETE)) {
            Intent install = new Intent(MainActivity.DOWNLOAD_APK);
            install.putExtra(MainActivity.click, INSTALL);
            install.putExtra(MainActivity.TYPE, NOTIFYCATIONID);
            PendingIntent pauseInstall = PendingIntent.getBroadcast(context, 2, install, PendingIntent.FLAG_ONE_SHOT);
            remoteViews.setViewVisibility(R.id.repeat_download, View.GONE);
            remoteViews.setViewVisibility(R.id.btn_install, View.VISIBLE);
            remoteViews.setOnClickPendingIntent(R.id.btn_install, pauseInstall);
        }

        Intent cancel = new Intent(MainActivity.DOWNLOAD_APK);
        cancel.putExtra(MainActivity.click, CANCEL);
        cancel.putExtra(MainActivity.TYPE, NOTIFYCATIONID);
        PendingIntent pauseCancel = PendingIntent.getBroadcast(context, 0, cancel, PendingIntent.FLAG_ONE_SHOT);

        remoteViews.setViewVisibility(R.id.cancel, View.VISIBLE);
        remoteViews.setOnClickPendingIntent(R.id.cancel, pauseCancel);

        //时间更新显示
        if (type.length() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = formatter.format(curDate);
            remoteViews.setTextViewText(R.id.tv_time, str);
        }

        remoteViews.setImageViewResource(R.id.iv_icon, R.mipmap.ic_launcher);
        remoteViews.setTextViewText(R.id.tv_progress, progress + "%");
        //设置进度条，最大值 为100,当前值为progress，最后一个参数为true时显示条纹
        remoteViews.setProgressBar(R.id.pBar, 100, progress, false);
        if (type.equals(COMPLETE)) {
            remoteViews.setTextViewText(R.id.tv_content, "下载完成");
        } else if (type.equals(FAIL)) {
            remoteViews.setTextViewText(R.id.tv_content, "下载失败");
        } else {
            remoteViews.setTextViewText(R.id.tv_content, "下载中...");
        }

        remoteViews.setTextViewText(R.id.tv_title, "珞宾对讲");

        mNotification.contentView = remoteViews;
        mNotificationManager.notify(NOTIFYCATIONID, mNotification);
    }

    /**
     * 设置下载进度
     */
    public void updateNotification(int progress) {
        Intent intentClick = new Intent(MainActivity.DOWNLOAD_APK);
        intentClick.putExtra("notification_clicked", "notification_clicked");
        intentClick.putExtra(MainActivity.TYPE, NOTIFYCATIONID);
        PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_ONE_SHOT);

        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_NO_CLEAR;//
        mBuilder.setProgress(100, progress, false); // 这个方法是显示进度条
        mBuilder.setContentText("下载中... " + progress + "%").setContentTitle("珞宾对讲");
        mBuilder.setContentIntent(pendingIntentClick);
        mBuilder.setDeleteIntent(pendingIntentClick);

        if (progress >= 100) {
            mBuilder.setContentText("下载完成").setContentTitle("珞宾对讲");
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context
                    .getSystemService(context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.notify(NOTIFYCATIONID, mNotification);
    }

}