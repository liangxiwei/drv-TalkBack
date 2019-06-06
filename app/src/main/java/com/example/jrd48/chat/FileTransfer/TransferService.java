package com.example.jrd48.chat.FileTransfer;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.luobin.dvr.R;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.TimeoutBroadcastManager;

/**
 * Created by jrd48 on 2017/2/23.
 */

public class TransferService extends Service {
    public static final int DOWNLOAD_FILE = 1;
    public static final int UPLOAD_FILE = 2;
    private TimeoutBroadcastManager mBroadcastManger = new TimeoutBroadcastManager();
    private DownloadFile downloadFile;
    private UploadFile uploadFile;
    private long tempTeamID;
    private String tempPhone;
    private long tempMsgID;
    private String tempFileAddress;
    private IntentFilter filter;
    private ConnetReceiver connetReceiver;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.btn_call)//必须要先setSmallIcon，否则会显示默认的通知，不显示自定义通知
                .setTicker("Chat 正在传送文件")
                .setContentTitle("Chat 正在传送文件")
                .setContentText("")
                .build();
        startForeground(-5, notification);
        downloadFile = new DownloadFile(this,mBroadcastManger);
        uploadFile = new UploadFile(this,mBroadcastManger);
        //开启网络监听
        filter = new IntentFilter();
        connetReceiver = new ConnetReceiver();
        filter.addAction("android.intent.action.ANY_DATA_STATE");
        registerReceiver(connetReceiver, filter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean bConnected = ConnUtil.isConnected(this);
        if(bConnected){
            int type = intent.getIntExtra("type",-1);
            tempTeamID = intent.getLongExtra("teamid",-1);
            tempPhone = intent.getStringExtra("phone");
            tempMsgID = intent.getLongExtra("msgid",-1);
            tempFileAddress = intent.getStringExtra("address");
            Log.i("jrdchat","tempMsgID："+tempMsgID);
            if(type==DOWNLOAD_FILE){
                downloadFile.startDownload(new TansferFileDown(tempTeamID,tempPhone,tempMsgID,tempFileAddress));
            }else if(type == UPLOAD_FILE){
                uploadFile.startUpload(new TansferFileUp(tempTeamID,tempPhone,tempMsgID,tempFileAddress));
            }
        }else{
            Intent i = new Intent(this,TransferService.class);
            stopService(i);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        downloadFile.stopAll();
        uploadFile.stopAll();
        mBroadcastManger.stopAll();
        if (connetReceiver != null) {
            unregisterReceiver(connetReceiver);
        }
        super.onDestroy();
    }

    class ConnetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean bConnected = ConnUtil.isConnected(TransferService.this);
            if(!bConnected) {
                Intent i = new Intent(TransferService.this, TransferService.class);
                stopService(i);
            }
        }
    }
}
