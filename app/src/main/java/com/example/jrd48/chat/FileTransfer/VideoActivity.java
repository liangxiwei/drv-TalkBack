package com.example.jrd48.chat.FileTransfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;

import java.io.File;

public class VideoActivity extends BaseActivity {
    private VideoView videoView;
    private Button downloadBtn;
    private Button playerBtn;
    private String myPhone;
    private long msgID;
    private long teamID;
    private long sn;
    private String phone;
    private String address;
    private IntentFilter filterDown;
    private DownloadReceiver downloadReceiver;
    private byte[] byteBitmap;
    private ImageView imageView;
    private boolean player = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show);
        imageView = (ImageView) findViewById(R.id.iv_temp);
        videoView = (VideoView) findViewById(R.id.video_view_temp);
        downloadBtn = (Button) findViewById(R.id.buttonDownload);
        playerBtn = (Button) findViewById(R.id.player);
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        filterDown = new IntentFilter();
        downloadReceiver = new DownloadReceiver();
        filterDown.addAction("download.percent");
        playerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player) {
                    videoView.pause();
                    playerBtn.setBackgroundResource(R.drawable.player);
                    player = false;
                } else {
                    videoView.start();
                    playerBtn.setBackgroundResource(R.drawable.pause);
                    player = true;
                }
            }
        });

        //设置播放完成以后监听
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playerBtn.setBackgroundResource(R.drawable.player);
                player = false;
            }
        });

        initSomething();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(downloadReceiver, filterDown);
    }

    @Override
    protected void onPause() {
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initSomething() {

        Intent i = getIntent();
        if (i != null) {
            msgID = i.getLongExtra("msgid", -1);
            teamID = i.getLongExtra("teamid", 0);
            phone = i.getStringExtra("otherphone");
            sn = i.getLongExtra("sn", -1);
            address = i.getStringExtra("address");
            byteBitmap = i.getByteArrayExtra("bitmap");
            //addressTemp = getMyDataRoot(this) + "/" + myPhone + "/" + teamID + "/" + phone + "/" + sn + ".mp4";
            File file = new File(address);
            if (file.exists()) {
                videoView.setVideoPath(file.getPath());
                downloadBtn.setVisibility(View.GONE);
                videoView.start();
                playerBtn.setBackgroundResource(R.drawable.pause);
                player = true;
                //videoView.start();
            } else {
                downloadBtn.setVisibility(View.VISIBLE);
                downloadBtn.setText("下载视频");
                if (byteBitmap != null && byteBitmap.length > 0) {
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(byteBitmap, 0, byteBitmap.length));
                }
                playerBtn.setVisibility(View.GONE);
                downloadBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        doDowndloadImage();
                    }
                });
            }
        }
    }

    private void doDowndloadImage() {
        Intent i = new Intent(this, TransferService.class);
        i.putExtra("type",TransferService.DOWNLOAD_FILE);
        i.putExtra("teamid",teamID);
        i.putExtra("phone",phone);
        i.putExtra("msgid",msgID);
        i.putExtra("address",address);
        startService(i);
        //mDownloadFile.startDownload(new TansferFileDown(teamID, phone, msgID, address));
        downloadBtn.setClickable(false);
        downloadBtn.setText("正在下载 " + 0 + "%");
    }

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long msgid = intent.getLongExtra("msgid", -1);
            if (msgID == msgid) {
                if (intent.getBooleanExtra("success", false)) {
                    Log.i("jrdchat", "下载视频成功了~");
                    File file = new File(address);
                    if (file.exists()) {
                        videoView.setVideoPath(file.getPath());
                        downloadBtn.setVisibility(View.GONE);
                        playerBtn.setVisibility(View.VISIBLE);
                        videoView.start();
                        imageView.setVisibility(View.GONE);
                        playerBtn.setBackgroundResource(R.drawable.pause);
                        player = true;
                    } else {
                        ToastR.setToast(VideoActivity.this, "获取视频出现问题~");
                        downloadBtn.setVisibility(View.VISIBLE);
                        playerBtn.setVisibility(View.GONE);
                        downloadBtn.setText("下载视频");
                    }
                } else {
                    //还未成功
                    int percent = intent.getIntExtra("percent", -1);
                    if (percent != -1) {
                        downloadBtn.setText("正在下载 " + percent + "%");
                    } else {
                        Log.i("jrdchat", "下载视频失败了~");
                        ToastR.setToast(VideoActivity.this, "视频下载失败！");
                        downloadBtn.setClickable(true);
                        downloadBtn.setText("下载视频");
                    }
                }
            }
        }
    }
}

