package com.video;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.jrd48.chat.BottomLayoutManager;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.ConnUtil;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.R;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by Administrator on 2017/8/4.
 */

public class GlobalVideo {
    private static final int MESSAGE_ID_RECONNECTING = 0x01;
    private static final String TAG = "GlobalVideo";
    private boolean isShow = false;
    private Context context;
    private AVOptions mAVOptions;

    private View contentView;
    private FrameLayout root;
    private PLVideoView otherView;
    private ImageView head_image;
    private View mLoadingView;
    private View mConnectingView;
    String otherAddr = null;
    private final Object mutex = new Object();
    private WindowManager.LayoutParams params = null;
    private WindowManager wm;
    protected Handler mainHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage");
            if (!ConnUtil.isConnected(context)) {
                sendReconnectMessage();
                return;
            }

            if (otherAddr != null && otherView != null) {
                otherView.setVideoPath(otherAddr);
                otherView.start();
            }
        }
    };
    private static GlobalVideo globalVideo;

    public synchronized static GlobalVideo getInstance() {
        if (globalVideo == null) {
            globalVideo = new GlobalVideo();
        }
        return globalVideo;
    }

    public GlobalVideo() {
        context = MyApplication.getContext();

        mAVOptions = new AVOptions();

        // the unit of timeout is ms
//        mAVOptions.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        mAVOptions.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, 10);
        mAVOptions.setInteger(AVOptions.KEY_MAX_CACHE_BUFFER_DURATION, 10);
        mAVOptions.setInteger(AVOptions.KEY_PROBESIZE, 1 * 1024);
        // Some optimization with buffering mechanism when be set to 1
        mAVOptions.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        mAVOptions.setInteger(AVOptions.KEY_DELAY_OPTIMIZATION, 1);

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int iCodec = AVOptions.MEDIA_CODEC_HW_DECODE;
        mAVOptions.setInteger(AVOptions.KEY_MEDIACODEC, iCodec);

        // whether start play automatically after prepared, default value is 1
        mAVOptions.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        params = createParams();
    }

    public void updateParam(final int left, final int top, final int width, final int height) {
        Log.i("wsDvr", "updateParam x="+left+",y="+top+",width="+width+",height="+height);
        params.x = left;
        params.y = top;
        params.width = width;
        params.height = height;

        RESClient.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isShow) {
                        wm.updateViewLayout(contentView, params);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startVideo(String path) {
        Log.i("wsDvr", "Global Video path:" + path);
        String newString;
        if (path.contains("_")) {
            newString = path.substring(0, path.lastIndexOf("---")) + path.substring(path.lastIndexOf("_"));
        } else {
            newString = path.substring(0, path.lastIndexOf("---"));
        }
        Log.i("wsDvr", "Global Video path:" + newString);
        if (isShow && newString.equals(otherAddr)) {
            return;
        }
        otherAddr = newString;
        stop();

        contentView = LayoutInflater.from(context).inflate(R.layout.other_video_item, null);
        root = (FrameLayout) contentView.findViewById(R.id.root);
        contentView.setClickable(true);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] rect = new int[4];
                DvrConfig.getThumbnailViewRect(rect);
                context.sendBroadcast(new Intent(BottomLayoutManager.ACTION_VIDEO_CONTROL_SHOW));
                if (params.x == rect[0]) {
                    context.sendBroadcast(new Intent(RESClient.ACTION_ONCLICK_LEFT_TOP));
                } else {
                    Log.e(TAG, "contentView.onClick");
                }
            }
        });
        synchronized (mutex) {
            mConnectingView = contentView.findViewById(R.id.connect_hint);
            mLoadingView = contentView.findViewById(R.id.loading_view);
            otherView = (PLVideoView) contentView.findViewById(R.id.other_view);
            head_image = (ImageView) contentView.findViewById(R.id.head_image);
            ImageView mCoverView = (ImageView) contentView.findViewById(R.id.cover_view);
            if (path.startsWith(RESClient.PLAY_SERVER_URL + "group---")) {
                String phone;
                if (path.contains("_")) {
                    phone = path.substring(path.lastIndexOf("---") + 3, path.lastIndexOf("_"));
                } else {
                    phone = path.substring(path.lastIndexOf("---") + 3);
                }
                Log.i("wsDvr", "Global Video phone:" + phone);
                Bitmap bitmap = GlobalImg.getImage(MyApplication.getContext(), phone);
                if (bitmap != null) {
                    bitmap.setHasAlpha(true);
                    head_image.setImageBitmap(bitmap);
                    head_image.setAlpha(0.5f);
                    mCoverView.setImageBitmap(bitmap);
                } else {
                    head_image.setImageResource(R.drawable.man);
                    head_image.setAlpha(0.5f);
                    mCoverView.setImageResource(R.drawable.man);
                }
            } else {
                int start = path.lastIndexOf("---");
//                int start = RESClient.PLAY_SERVER_URL.length();
                String phone = null;
                if (path.contains("_")) {
                    phone = path.substring(start + 3, path.lastIndexOf("_"));
                } else {
                    phone = path.substring(start + 3);
                }
                Log.i("wsDvr", "Global Video phone:" + phone);
                Bitmap bitmap = GlobalImg.getImage(MyApplication.getContext(), phone);
                if (bitmap != null) {
                    bitmap.setHasAlpha(true);
                    head_image.setImageBitmap(bitmap);
                    head_image.setAlpha(0.5f);
                    mCoverView.setImageBitmap(bitmap);
                } else {
                    head_image.setImageResource(R.drawable.man);
                    head_image.setAlpha(0.5f);
                    mCoverView.setImageResource(R.drawable.man);
                }
            }
            head_image.setVisibility(View.GONE);
            if (otherAddr != null) {
                otherView.setDebugLoggingEnabled(false);
                otherView.setCoverView(mCoverView);
                otherView.setTopRightImage(head_image);
                otherView.setBufferingIndicator(mLoadingView);
                setVideoView(otherView, otherAddr);
            }

            RESClient.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        wm.addView(contentView, params);
                        Log.v(TAG, "addView");
                        isShow = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public void stop() {
        synchronized (mutex) {
            if (mainHander != null) {
                mainHander.removeCallbacksAndMessages(null);
            }
            try {
                if (otherView != null) {
                    otherView.stopPlayback();
                    otherView = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            RESClient.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isShow) {
                        try {
                            wm.removeView(contentView);
                            Log.v(TAG, "removeView");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isShow = false;
                }
            });
        }
    }

    private void setVideoView(final PLVideoView videoView, final String addr) {
        videoView.setAVOptions(mAVOptions);
        // Set some listeners
        videoView.setOnInfoListener(mOnInfoListener);
        videoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        videoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        videoView.setOnCompletionListener(mOnCompletionListener);
//        otherView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        videoView.setOnErrorListener(mOnErrorListener);
        videoView.setVideoPath(addr);
        videoView.start();
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
                    otherView.setAVOptions(mAVOptions);
                    isNeedReconnect = true;
                    break;
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    showToastTips("connect video failed !");
                    isNeedReconnect = true;
                    break;
                default:
                    showToastTips("unknown error !");
                    break;
            }
            if (isNeedReconnect) {
                sendReconnectMessage();
            } else {
                stop();
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
            stop();
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
        mainHander.removeCallbacksAndMessages(null);
        mainHander.sendMessageDelayed(mainHander.obtainMessage(MESSAGE_ID_RECONNECTING), 100);
    }

    private void showToastTips(final String tips) {
        Log.v(TAG, "showTips:" + tips);
    }

    private WindowManager.LayoutParams createParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.format = PixelFormat.TRANSPARENT;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        int[] rect = new int[4];
        DvrConfig.getThumbnailViewRect(rect);
        params.x = rect[0];
        params.y = rect[1];
        params.width = rect[2];
        params.height = rect[3];
        return params;
    }

    public boolean isShow() {
        return isShow;
    }

    public void onCallClick() {
        if (contentView != null) {
            contentView.performClick();
        }
    }
}
