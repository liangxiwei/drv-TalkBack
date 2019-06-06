package com.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.luobin.dvr.R;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.ConnUtil;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.common.Util;
import com.pili.pldroid.player.widget.PLVideoView;

import java.util.ArrayList;
import java.util.List;

import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.model.RESConfig;
import me.lake.librestreaming.model.Size;

/**
 * Created by wangsheng on 2017/3/14.
 */

public class VideoCallActivity extends BaseActivity implements RESConnectionListener, TextureView.SurfaceTextureListener {
    private static final String TAG = "VideoCallActivity";
    public static final String ACTION_LIVE_CALL_ANS_DENY = "com.luobin.dvr.action.ACTION_LIVE_CALL_ANS_DENY";
    private static final int MESSAGE_ID_RECONNECTING = 0x01;
    private static final int START = 0;
    private static final int PAUSE = 1;
    private static final int STOP = 2;
    private static final int SETTING = 3;
    public static final String OTHER_PHONE = "other_phone";
    public static final String VIDEO_URL = "videoUrl";
    public static final String TEAM_ID = "team_id";
    public static final String GROUP_MEMBER = "group_member";
    protected Handler mainHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MESSAGE_ID_RECONNECTING) {
                sendEmptyMessageDelayed(0, 3000);
//                if (resClient.getSendBufferFreePercent() <= 0.05 && isConnected) {
//                    Toast.makeText(context, "sendbuffer is full,netspeed is low!", Toast.LENGTH_SHORT).show();
//                }
                return;
            }
            if (mIsActivityPaused) {
                finish();
                return;
            }
            if (!ConnUtil.isConnected(context)) {
                sendReconnectMessage();
                return;
            }

            if (singleView.getVisibility() == View.VISIBLE) {
                otherView.setVideoPath(otherAddr);
                otherView.start();
            } else {
                controlVideo(START);
            }
        }
    };
    ;
    private Context context;
//    private RESClient resClient;
//    private AspectTextureView self_view;
//    private String selfAddr;
    private String otherAddr;
    private long teamId;
    private String videoUrl;
    private List<TeamMemberInfo> teamMemberInfos;
    private boolean started;

    private RESConfig resConfig;

    private boolean isConnected = false;

    private int reStartCount;

    private PLVideoView otherView;
    private PLVideoView[] otherArray;
    private View[] mLoadingArray;
    private View mLoadingView;
//    private View mConnectingView;
    private AVOptions mAVOptions;
    private boolean mIsActivityPaused = true;

    private View singleView;
//    private GridLayout groupView;
    private LayoutInflater inflater;
    private String myPhone;
    private String otherPhone;
//    private Runnable startStreamRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (!started) {
//                resClient.startStreaming();
//                started = !started;
//            }
//        }
//    };


    public static void stopVideoCall(final Context context,final String linkmanPhone) {
        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        builder.setToUserPhone(linkmanPhone);
        builder.setAccept(ProtoMessage.AcceptType.atDeny);
        MyService.start(context, ProtoMessage.Cmd.cmdLiveVideoCall.getNumber(), builder.build());
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(LiveVideoCallProcesser.ACTION);
//        final TimeoutBroadcast b = new TimeoutBroadcast(context, filter,new TimeoutBroadcastManager());
//
//        b.startReceiver(10, new ITimeoutBroadcast() {
//            @Override
//            public void onTimeout() {
//                ToastR.setToast(context, "超时");
//                Log.v(TAG,"停止失败:超时");
//            }
//
//            @Override
//            public void onGot(Intent i) {
//                if (i.getIntExtra("error_code", -1) == ProtoMessage.ErrorCode.OK.getNumber()) {
////                    VideoCallActivity.startActivity(context, linkmanPhone);
//                } else {
//                    ToastR.setToast(context, "停止失败");
//                    Log.v(TAG,"停止失败:" + i.getIntExtra("error_code", -1));
//                }
//            }
//        });
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_LIVE_CALL_ANS_DENY)){
                if(otherPhone != null && otherPhone.equals(intent.getStringExtra("phone"))){
                    ToastR.setToast(context,"对方已经关闭了路况分享");
                    finish();
                }
            }
        }
    };

    public static void startActivity(Context context, String otherPhone) {
        Intent recordIntent = new Intent(context, VideoCallActivity.class);
        recordIntent.putExtra(OTHER_PHONE, otherPhone);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(recordIntent);
    }
    public static void startActivity(Context context, String otherPhone,String VideoUrl) {
        Intent recordIntent = new Intent(context, VideoCallActivity.class);
        recordIntent.putExtra(OTHER_PHONE, otherPhone);
        recordIntent.putExtra(VIDEO_URL, VideoUrl);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(recordIntent);
    }
    public static void startActivity(Context context, long teamId, ArrayList<TeamMemberInfo> teamMemberInfos) {
        Intent recordIntent = new Intent(context, VideoCallActivity.class);
        recordIntent.putExtra(TEAM_ID, teamId);
        recordIntent.putParcelableArrayListExtra(GROUP_MEMBER, teamMemberInfos);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        recordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(recordIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        stopVideo();
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        otherPhone = intent.getStringExtra(OTHER_PHONE);
        teamId = intent.getLongExtra(TEAM_ID, 0);
        videoUrl = intent.getStringExtra(VIDEO_URL);
        teamMemberInfos = intent.getParcelableArrayListExtra(GROUP_MEMBER);

        GlobalStatus.setCurRoadPhone(otherPhone);
        if(teamId == 0){
//            selfAddr = SERVER_URL + myPhone + "/" + otherPhone;
            otherAddr = RESClient.PLAY_SERVER_URL + otherPhone + "---" + myPhone;
            if(videoUrl != null){
                otherAddr = otherAddr + "_" + videoUrl;
            }
            MyApplication.setCurVideo(otherPhone,0);
        } else {
//            selfAddr = SERVER_URL + "group/" + teamId + "/" + myPhone;
            MyApplication.setCurVideo(null,teamId);
            if (teamMemberInfos.size() <= 0) {
                Log.w("video", "没有获取到群组信息");
            }
        }
//        if (groupView != null) {
//            groupView.removeAllViews();
//        }

        mainHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                initView();
            }
        },800);
        setIntent(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        started = false;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_video_call);
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        inflater = LayoutInflater.from(context);
        registerReceiver(receiver,new IntentFilter(ACTION_LIVE_CALL_ANS_DENY));

        mAVOptions = new AVOptions();

        // the unit of timeout is ms
//        mAVOptions.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        mAVOptions.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
        mAVOptions.setInteger(AVOptions.KEY_PROBESIZE, 10 * 1024);
        // Some optimization with buffering mechanism when be set to 1
        mAVOptions.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        mAVOptions.setInteger(AVOptions.KEY_DELAY_OPTIMIZATION, 1);

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int iCodec = AVOptions.MEDIA_CODEC_HW_DECODE;
        mAVOptions.setInteger(AVOptions.KEY_MEDIACODEC, iCodec);

        // whether start play automatically after prepared, default value is 1
        mAVOptions.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);
        handleIntent(getIntent());

    }

    public void initView() {
        singleView = findViewById(R.id.single_video_layout);
//        groupView = (GridLayout) findViewById(R.id.group_view);
//        if (self_view != null) {
//            self_view.setSurfaceTextureListener(null);
//        }
        if (teamId == 0) {
            singleView.setVisibility(View.VISIBLE);
//            groupView.setVisibility(View.GONE);
//            findViewById(R.id.self_view).setVisibility(View.GONE);
//            mConnectingView = findViewById(R.id.connect_hint);
//            mConnectingView.setVisibility(View.GONE);
        } else {
            singleView.setVisibility(View.GONE);
//            groupView.setVisibility(View.VISIBLE);
            otherArray = new PLVideoView[teamMemberInfos.size() - 1];
            mLoadingArray = new View[teamMemberInfos.size() - 1];
        }


        mLoadingView = findViewById(R.id.loading_view);
        otherView = (PLVideoView) findViewById(R.id.other_view);
        otherView.setKeepScreenOn(true);
        ImageView mCoverView = (ImageView) findViewById(R.id.cover_view);
        mCoverView.setAlpha(0.5f);
//        self_view.setKeepScreenOn(true);
//        self_view.setSurfaceTextureListener(this);
//        resClient = new RESClient();
        resConfig = RESConfig.obtain();
//        startRecord();
        if (singleView.getVisibility() == View.VISIBLE) {
            mCoverView.setImageBitmap(GlobalImg.getImage(context,otherPhone));
            otherView.setCoverView(mCoverView);
            otherView.setBufferingIndicator(mLoadingView);
            setVideoView(otherView, otherAddr);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityPaused = false;
//        if (singleView.getVisibility() == View.VISIBLE) {
//            otherView.start();
//        } else {
//            controlVideo(START);
//        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        mIsActivityPaused = true;
//        if (singleView.getVisibility() == View.VISIBLE) {
//            otherView.pause();
//        } else {
//            controlVideo(PAUSE);
//        }
        ToastR.setToast(context,"结束路况查询");
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        GlobalStatus.setCurRoadPhone(null);
        stopVideoCall(context,otherPhone);
        unregisterReceiver(receiver);
        stopVideo();
        MyApplication.setCurVideo(null,0);
        super.onDestroy();
    }

    public void stopVideo() {
        if (mainHander != null) {
            mainHander.removeCallbacksAndMessages(null);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    resClient.stopStreaming();
                    started = false;
//                    if (resClient != null) {
//                        resClient.destroy();
//                    }

                    if (otherView != null) {
                        otherView.stopPlayback();
                    }
                    if (otherArray != null) {
                        controlVideo(STOP);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                };
            }
        }).start();
    }

//    public void startRecord() {
//        int frontDirection, backDirection;
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        try {
//            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
//            frontDirection = cameraInfo.orientation;
//        } catch (Exception e) {
//            e.printStackTrace();
//            frontDirection = 0;
//        }
//        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
//        backDirection = cameraInfo.orientation;
//        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            resConfig.setFrontCameraDirectionMode((frontDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90) | RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
//            resConfig.setBackCameraDirectionMode((backDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270));
//        } else {
//            resConfig.setBackCameraDirectionMode((backDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180));
//            resConfig.setFrontCameraDirectionMode((frontDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0) | RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
//        }
//        Log.v(TAG, "start self addr : " + selfAddr);
//        resConfig.setRtmpAddr(selfAddr);
//        if (!resClient.prepare(resConfig)) {
//            resClient = null;
//            Log.e(TAG, "prepare,failed!!");
//            Toast.makeText(this, "RESClient prepare failed", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
//        Size s = resClient.getVideoSize();
//        self_view.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) s.getWidth()) / s.getHeight());
//        Log.d(TAG, "version=" + resClient.getVertion());
//        mConnectingView.setVisibility(View.VISIBLE);
//        resClient.setConnectionListener(this);
//        mainHander.sendEmptyMessageDelayed(0, 3000);
//
//        resClient.setSoftAudioFilter(null);
//        resClient.setHardVideoFilter(null);
//        reStartCount = 0;
//        mainHander.postDelayed(startStreamRunnable, 1000);
//    }
//
    protected SurfaceTexture texture;
    protected int sw, sh;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        if (resClient != null) {
//            resClient.startPreview(surface, width, height);
//        }
        texture = surface;
        sw = width;
        sh = height;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        if (resClient != null) {
//            resClient.updatePreview(width, height);
//        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        if (resClient != null) {
//            resClient.stopPreview(true);
//        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onOpenConnectionResult(int result) {
        if (isFinishing()) {
            return;
        }
        if (result == 0) {
            reStartCount = 0;
//            mConnectingView.setVisibility(View.GONE);
//            Log.v(TAG, "server IP = " + resClient.getServerIpAddr());
            isConnected = true;
        } else {
            reStartCount++;
            Log.e(TAG, "startfailed " + reStartCount);
//            Toast.makeText(this, "start failed,reconnection count " + reStartCount, Toast.LENGTH_SHORT).show();
//            mConnectingView.setVisibility(View.VISIBLE);
            isConnected = false;

            started = false;
//            resClient.stopStreaming();
//            mainHander.postDelayed(startStreamRunnable, 1000);
        }
    }

    @Override
    public void onWriteError(int errno) {
        if (errno == 9) {
//            resClient.stopStreaming();
//            resClient.startStreaming();
            Toast.makeText(this, "errno==9,restarting", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCloseConnectionResult(int result) {

    }

    private void setVideoView(PLVideoView videoView, String addr) {
        otherView.setDebugLoggingEnabled(false);
        videoView.setAVOptions(mAVOptions);
        // Set some listeners
        videoView.setOnInfoListener(mOnInfoListener);
        videoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        videoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        videoView.setOnCompletionListener(mOnCompletionListener);
//        otherView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        videoView.setOnErrorListener(mOnErrorListener);

        videoView.setVideoPath(addr);
        Log.v(TAG,"addr:" + addr);
        videoView.start();
    }

    private void controlVideo(int type) {
        for (int i = 0; i < otherArray.length; i++) {
            switch (type) {
                case START:
                    otherArray[i].start();
                    break;
                case PAUSE:
                    otherArray[i].pause();
                    break;
                case STOP:
                    otherArray[i].stopPlayback();
                    break;
                case SETTING:
                    otherArray[i].setAVOptions(mAVOptions);
                    break;
            }
        }
    }

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            Log.d(TAG, "onInfo: " + what + ", " + extra);
            return false;
        }
    };

    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
            boolean isNeedReconnect = false;
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_INVALID_URI:
                    showToastTips("Invalid URL !");
                    break;
                case PLMediaPlayer.ERROR_CODE_404_NOT_FOUND:
                    showToastTips("404 resource not found !");
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_REFUSED:
                    showToastTips("Connection refused !");
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT:
                    showToastTips("Connection timeout !");
                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.ERROR_CODE_EMPTY_PLAYLIST:
                    showToastTips("Empty playlist !");
                    break;
                case PLMediaPlayer.ERROR_CODE_STREAM_DISCONNECTED:
                    showToastTips("Stream disconnected !");
                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                    showToastTips("Network IO Error !");
                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.ERROR_CODE_UNAUTHORIZED:
                    showToastTips("Unauthorized Error !");
                    break;
                case PLMediaPlayer.ERROR_CODE_PREPARE_TIMEOUT:
                    showToastTips("Prepare timeout !");
                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.ERROR_CODE_READ_FRAME_TIMEOUT:
                    showToastTips("Read frame timeout !");
                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.ERROR_CODE_HW_DECODE_FAILURE:
                    mAVOptions.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
                    if (singleView.getVisibility() == View.VISIBLE) {
                        otherView.setAVOptions(mAVOptions);
                    } else {
                        controlVideo(SETTING);
                    }

                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    showToastTips("connect video failed !");
                    break;
                default:
                    showToastTips("unknown error !");
                    break;
            }
            // Todo pls handle the error status here, reconnect or call finish()
            if (isNeedReconnect) {
                sendReconnectMessage();
            } else {
                finish();
            }
            // Return true means the error has been handled
            // If return false, then `onCompletion` will be called
            return true;
        }
    };

    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            Log.d(TAG, "Play Completed !");
            showToastTips("Play Completed !");
            finish();
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
//            Log.d(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height, int videoSar, int videoDen) {
            Log.d(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height + ", sar = " + videoSar + ", den = " + videoDen);
        }
    };

    private void sendReconnectMessage() {
        showToastTips("正在重连...");
        mLoadingView.setVisibility(View.VISIBLE);
        for (int i = 0; i < mLoadingArray.length; i++) {
            mLoadingArray[i].setVisibility(View.VISIBLE);
        }
        mainHander.removeCallbacksAndMessages(null);
        mainHander.sendMessageDelayed(mainHander.obtainMessage(MESSAGE_ID_RECONNECTING), 500);
    }

    private void showToastTips(final String tips) {
        if (mIsActivityPaused) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastR.setToast(context, tips);
            }
        });
    }
}
