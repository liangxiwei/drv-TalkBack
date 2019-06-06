package me.lake.librestreaming.client;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BottomLayoutManager;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.location.Utils;
import com.example.jrd48.chat.receiver.ToastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.google.protobuf.ByteString;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.ui.MainActivity;
import com.luobin.voice.DefaultSetting;
import com.luobin.voice.VolatileBool;
import com.video.LightSensorListener;

import java.nio.ByteBuffer;

import me.lake.librestreaming.core.Packager;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.model.RESConfig;
import me.lake.librestreaming.model.RESCoreParameters;
import me.lake.librestreaming.model.Size;
import me.lake.librestreaming.rtmp.RESFlvData;
import me.lake.librestreaming.rtmp.RESFlvDataCollecter;
import me.lake.librestreaming.rtmp.RESRtmpSender;
import me.lake.librestreaming.tools.LogTools;

/**
 * Created by lake on 16-3-16.
 */
public class RESClient {
    private static final String TAG = "RESClient";
    //    rtmp://video-center.alivecdn.com/AppName/StreamName?vhost=irobbing.com
//    rtmp://irobbing.com/AppName/StreamName
    public static final String PLAY_SERVER_URL = "rtmp://pili-live-rtmp.irobbing.com/dvrlive/";
    public static final String SERVER_URL = "rtmp://pili-publish.irobbing.com/dvrlive/";
    //    public static final String SERVER_URL_LAST = "?vhost=live.irobbing.com";
    public static final String ACTION_START_RTMP = "android.luobin.action.RTMP_START";
    public static final String ACTION_STOP_RTMP = "android.luobin.action.RTMP_STOP";
    public static final String ACTION_SWITCH_RTMP = "android.luobin.action.SWITCH_RTMP";
    public static final String ACTION_UPDATE_RTMP = "android.luobin.action.UPDATE_RTMP";
    public static final String ACTION_START_PLAY = "android.luobin.action.PLAY_START";
    public static final String ACTION_STOP_PLAY = "android.luobin.action.PLAY_STOP";
    public static final String ACTION_UPDATE_PLAY = "android.luobin.action.UPDATE_PLAY";
    public static final String ACTION_ONCLICK_LEFT_TOP = "android.luobin.action.ACTION_ONCLICK_LEFT_TOP";
    public static final String ACTION_ONCLICK_DVR = "android.luobin.action.ACTION_ONCLICK_DVR";
    public static final String ACTION_VOICE_RECORD = "android.luobin.action.ACTION_VOICE_RECORD";
    public static final String ACTION_VOICE_STOP = "android.luobin.action.ACTION_VOICE_STOP";
    public static final String ACTION_SHOW_NAVI_BAR = "com.erobbing.action.show_bar";
    public static final String ACTION_HIDE_NAVI_BAR = "com.erobbing.action.hide_bar";
    public static final String ACTION_VIDEO_VOIDE_SWITCH = "com.luobin.dvr.action.ACTION_VIDEO_VOIDE_SWITCH";
    public static final String ACTION_VIDEO_VOIDE_UPDATE = "com.luobin.dvr.action.ACTION_VIDEO_VOIDE_UPDATE";
    public static final String PATH = "path";
    public static final String SELF_VIDEO = "self_video";
    public static final String LOCATION = "loation";
    public static final String GROUP = "group";
    public static final String PHONE = "phone";
    public static final int STATUS_NULL_PREPARED = 0;
    public static final int STATUS_NULL_STARTED = 1;
    public static final int STATUS_SUCCESS = 2;
    //    private RESVideoClient videoClient;
    //    private RESAudioClient audioClient;
    private final Object SyncOp;
    private final Object usbDrawLock = new Object();
    //parameters
    RESCoreParameters coreParameters;
    private RESRtmpSender rtmpSender;
    private RESFlvDataCollecter dataCollecter;
    private boolean oldSelf = true;
    private boolean self_video = true;
    private boolean isRoad = false;
    private static RESClient resClient;
    private RESConfig resConfig;
    private MediaFormat drvFormat;
    private int reStartCount = 0;
    private boolean started;
    private boolean prepared;
    private VideoBase mCamView = null;
    private WindowManager.LayoutParams mCamHideViewParams = null;
    private WindowManager.LayoutParams mCamViewParams = null;
    private long lastToastTime = 0;
    private boolean isForceSwitchVoice = false;
    private Handler mainHander = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            if (rtmpSender != null && started) {
                Log.d(TAG,"FreePercent = " + rtmpSender.getSendBufferFreePercent());
                if (rtmpSender.getSendBufferFreePercent() < 0.8 && SystemClock.elapsedRealtime() - lastToastTime > 5 * 1000 && !isForceSwitchVoice) {
                    Log.d(TAG,"IsRandomChat = " + GlobalStatus.IsRandomChat());
                    isForceSwitchVoice = true;
                    lastToastTime = SystemClock.elapsedRealtime();
                    if(dvrService!=null){
                        Intent ii = new Intent(ACTION_VIDEO_VOIDE_SWITCH);
                        ii.putExtra("isVideo",false);
                        dvrService.sendBroadcast(ii);
                    }
                    //ToastR.setToastLong(MyApplication.getContext(), "当前网络质量不好，切换为语音对讲模式");
                }
            }
            mainHander.sendEmptyMessageDelayed(0, 3000);
            super.handleMessage(msg);
        }
    };
    private DvrService dvrService;
    private boolean isTransferVoice = false;

    byte mBufferCache[] = new byte[14336];
    int mBufferOffset = 0;
    private VolatileBool mFirstCache = new VolatileBool(true);
    private AlertDialog dialog;
    private SensorManager sensorManager;
    private LightSensorListener lightSensorListener;
    private Runnable startStreamRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v(TAG, "started:" + started + "," + prepared);
            if (!started && prepared) {
                resClient.startStreaming();
                started = !started;
            }
        }
    };

    public synchronized static RESClient getInstance() {
        if (resClient == null) {
            resClient = new RESClient();
        }
        return resClient;
    }

    public RESClient() {
        SyncOp = new Object();
        coreParameters = new RESCoreParameters();
        sensorManager = (SensorManager) MyApplication.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        CallbackDelivery.i();
    }

    /**
     * prepare to stream
     *
     * @param resConfig config
     * @return true if prepare success
     */
    public boolean prepare(RESConfig resConfig) {
        synchronized (SyncOp) {
            checkDirection(resConfig);
            coreParameters.filterMode = resConfig.getFilterMode();
            coreParameters.rtmpAddr = resConfig.getRtmpAddr();
            coreParameters.printDetailMsg = resConfig.isPrintDetailMsg();
            coreParameters.senderQueueLength = 150;

            if (rtmpSender == null) {
                rtmpSender = new RESRtmpSender();
                rtmpSender.prepare(coreParameters);
                dataCollecter = new RESFlvDataCollecter() {
                    @Override
                    public void collect(RESFlvData flvData, int type) {
                        if (rtmpSender == null) {
                            return;
                        }
                        rtmpSender.feed(flvData, type);
                    }
                };
                coreParameters.done = true;
                LogTools.d("===INFO===coreParametersReady:");
                LogTools.d(coreParameters.toString());
            }
            return true;
        }
    }


    private volatile boolean isFirstFrame = false;

    /**
     * start streaming
     */
    public void startStreaming() {
        synchronized (SyncOp) {
            if (self_video) {
                showSurfaceView();
            }else{
                if (dvrService != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                                dvrService.startPreview();
                            if (mCamViewParams == null) {
                                mCamViewParams = createParams();
                            }
                            dvrService.show(mCamViewParams.x, mCamViewParams.y, mCamViewParams.width, mCamViewParams.height);
                        }
                    }).start();
                }
            }

            if (rtmpSender != null && !started) {
                rtmpSender.stop();
                rtmpSender.start(coreParameters.rtmpAddr);
                isFirstFrame = true;
            }

            if (drvFormat != null) {
                sendAVCDecoderConfigurationRecord(0, drvFormat);
            }
//            audioClient.start(dataCollecter);
            LogTools.d("RESClient,startStreaming() self_video= " + self_video);
            isForceSwitchVoice = false;
            Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); // 获取光线传感器
            if (lightSensorListener == null && lightSensor != null && !isRoad) { // 光线传感器存在时
                lightSensorListener = new LightSensorListener();
                sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL); // 注册事件监听
            }
        }
    }

    /**
     * restart streaming
     */
    public void stopStreaming() {
        LogTools.d("RESClient,stopStreaming()");
        synchronized (SyncOp) {
            Log.i("chatjrd", "stopStreaming, dstVideoEncoder.release()的上上上层");
            started = false;
            if (rtmpSender != null) {
                rtmpSender.stop();
            }
            if (mCamView != null && dvrService != null && !self_video) {
                hideSurfaceView();
            }
//            audioClient.restart();
            isForceSwitchVoice = false;
            if (lightSensorListener != null) {
                try {
                    sensorManager.unregisterListener(lightSensorListener);
                    lightSensorListener = null;
                } catch (Exception e) {
                }
            }
            LogTools.d("RESClient,stopStreaming() end");
        }
    }

    /**
     * clean up
     */
    public void destroy() {
        LogTools.d("RESClient,destroy()");
        synchronized (SyncOp) {
            started = false;
            prepared = false;
            if (rtmpSender != null) {
                rtmpSender.destroy();
            }
            if (self_video) {
                if (mCamView != null && dvrService != null) {
                    hideSurfaceView();
                }
            } else if (Settings.System.getInt(MyApplication.getContext().getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0) == 0) {
                if (dvrService != null) {
                    dvrService.startThumbnailPreview();
                }
            }
//            audioClient.destroy();
            rtmpSender = null;
            self_video = oldSelf;
            isRoad = false;
            mainHander.removeMessages(0);
//            audioClient = null;
            LogTools.d("RESClient,destroy() end");
        }
    }

    /**
     * get the real video size,call after prepare()
     *
     * @return
     */
    public Size getVideoSize() {
        return new Size(coreParameters.videoWidth, coreParameters.videoHeight);
    }

    /**
     * get the rtmp server ip addr ,call after connect success.
     *
     * @return
     */
    public String getServerIpAddr() {
        synchronized (SyncOp) {
            return rtmpSender == null ? null : rtmpSender.getServerIpAddr();
        }
    }

    /**
     * get the rate of video frame sent by rtmp
     *
     * @return
     */
    public float getSendFrameRate() {
        synchronized (SyncOp) {
            return rtmpSender == null ? 0 : rtmpSender.getSendFrameRate();
        }
    }

    /**
     * get free percent of send buffer
     * return ~0.0 if the netspeed is not enough or net is blocked.
     *
     * @return
     */
    public float getSendBufferFreePercent() {
        synchronized (SyncOp) {
            return rtmpSender == null ? 0 : rtmpSender.getSendBufferFreePercent();
        }
    }

    /**
     * get video & audio real send Speed
     *
     * @return speed in B/s
     */
    public int getAVSpeed() {
        synchronized (SyncOp) {
            return rtmpSender == null ? 0 : rtmpSender.getTotalSpeed();
        }
    }

    /**
     * call it AFTER {@link #prepare(RESConfig)}
     *
     * @param connectionListener
     */
    public void setConnectionListener(RESConnectionListener connectionListener) {
        rtmpSender.setConnectionListener(connectionListener);
    }

    /**
     * =====================PRIVATE=================
     **/
    private void checkDirection(RESConfig resConfig) {
        int frontFlag = resConfig.getFrontCameraDirectionMode();
        int backFlag = resConfig.getBackCameraDirectionMode();
        int fbit = 0;
        int bbit = 0;
        if ((frontFlag >> 4) == 0) {
            frontFlag |= RESCoreParameters.FLAG_DIRECTION_ROATATION_0;
        }
        if ((backFlag >> 4) == 0) {
            backFlag |= RESCoreParameters.FLAG_DIRECTION_ROATATION_0;
        }
        for (int i = 4; i <= 8; ++i) {
            if (((frontFlag >> i) & 0x1) == 1) {
                fbit++;
            }
            if (((backFlag >> i) & 0x1) == 1) {
                bbit++;
            }
        }
        if (fbit != 1 || bbit != 1) {
            throw new RuntimeException("invalid direction rotation flag:frontFlagNum=" + fbit + ",backFlagNum=" + bbit);
        }
        if (((frontFlag & RESCoreParameters.FLAG_DIRECTION_ROATATION_0) != 0) || ((frontFlag & RESCoreParameters.FLAG_DIRECTION_ROATATION_180) != 0)) {
            fbit = 0;
        } else {
            fbit = 1;
        }
        if (((backFlag & RESCoreParameters.FLAG_DIRECTION_ROATATION_0) != 0) || ((backFlag & RESCoreParameters.FLAG_DIRECTION_ROATATION_180) != 0)) {
            bbit = 0;
        } else {
            bbit = 1;
        }
        if (bbit != fbit) {
            if (bbit == 0) {
                throw new RuntimeException("invalid direction rotation flag:back camera is landscape but front camera is portrait");
            } else {
                throw new RuntimeException("invalid direction rotation flag:back camera is portrait but front camera is landscape");
            }
        }
        if (fbit == 1) {
            coreParameters.isPortrait = true;
        } else {
            coreParameters.isPortrait = false;
        }
        coreParameters.backCameraDirectionMode = backFlag;
        coreParameters.frontCameraDirectionMode = frontFlag;
    }

    static {
        System.loadLibrary("restream");
    }

    public void setSelf_video(boolean self_video) {
        this.self_video = self_video;
    }

    public boolean getSelf_video() {
        return self_video;
    }

    public boolean getOldSelf() {
        return oldSelf;
    }

    public void startRecording(String path, boolean mSelf_video) {
        stopStreaming();
        destroy();

//        path = path + SERVER_URL_LAST;
        if (!mSelf_video) {
            setSelf_video(mSelf_video);
            isRoad = true;
        }

//        if(self_video){
//            dvrService.stopPreview();
//        }

        resConfig = RESConfig.obtain();
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
//        resConfig.setBackCameraDirectionMode((backDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180));
//        resConfig.setFrontCameraDirectionMode((frontDirection == 90 ? RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180 : RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0) | RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);

//        String selfAddr = "rtmp://irobbing.com:1935/live/13560420669/18090321996";
        Log.v(TAG, "selfAddr path:" + path);
        resConfig.setRtmpAddr(path);
        prepared = false;
        if (!prepare(resConfig)) {
            Log.e(TAG, "prepare,failed!!");
            self_video = false;
            oldSelf = self_video;
            startRecording(path, true);
            Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
            intent.putExtra(ToastReceiver.TOAST_CONTENT, "设备不支持打开记录仪视频以外的摄像头");
            MyApplication.getContext().sendBroadcast(intent);
            return;
        }
        prepared = true;
        resClient.setConnectionListener(new RESConnectionListener() {

            @Override
            public void onOpenConnectionResult(int result) {
                if (result == 0) {
                    Log.v(TAG, "server IP = " + resClient.getServerIpAddr());
                } else {
                    reStartCount++;
                    Log.e(TAG, "startfailed " + reStartCount);
                    started = false;
                    if (resClient != null) {
                        resClient.stopStreaming();
                        mainHander.postDelayed(startStreamRunnable, 50);
                    }
                }
            }

            @Override
            public void onWriteError(int errno) {
                if (errno == 9) {
                    resClient.stopStreaming();
                    resClient.startStreaming();
//            Toast.makeText(activity, "errno==9,restarting", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCloseConnectionResult(int result) {

            }
        });
        reStartCount = 0;
        mainHander.postDelayed(startStreamRunnable, 50);
        if(!isRoad) {
            mainHander.sendEmptyMessageDelayed(0, 3000);
        }
    }

    public void pushData(MediaCodec mediaCodec, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        encodedData.position(bufferInfo.offset + 4);
        encodedData.limit(bufferInfo.offset + bufferInfo.size);
        if (bufferInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG && dataCollecter != null) {
            if (bufferInfo.flags != MediaCodec.BUFFER_FLAG_KEY_FRAME && isFirstFrame) {
                mediaCodec.flush();
                return;
            }
            isFirstFrame = false;
            int realDataLength = encodedData.remaining();
            if (realDataLength == 0) {
                return;
            }
            int packetLen = Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                    Packager.FLVPackager.NALU_HEADER_LENGTH +
                    realDataLength;
            byte[] finalBuff = new byte[packetLen];
            encodedData.get(finalBuff, Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                            Packager.FLVPackager.NALU_HEADER_LENGTH,
                    realDataLength);
            int frameType = finalBuff[Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                    Packager.FLVPackager.NALU_HEADER_LENGTH] & 0x1F;
            Packager.FLVPackager.fillFlvVideoTag(finalBuff,
                    0,
                    false,
                    frameType == 5,
                    realDataLength);
            RESFlvData resFlvData = new RESFlvData();
            resFlvData.byteBuffer = finalBuff;
            resFlvData.size = finalBuff.length;
            resFlvData.dts = (int) (bufferInfo.presentationTimeUs / 1000);
            resFlvData.flvTagType = RESFlvData.FLV_RTMP_PACKET_TYPE_VIDEO;
            resFlvData.videoFrameType = frameType;
            dataCollecter.collect(resFlvData, RESRtmpSender.FROM_VIDEO);
        }
    }


    public void pushVoiceData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        encodedData.position(bufferInfo.offset);
        encodedData.limit(bufferInfo.offset + bufferInfo.size);
        if (bufferInfo.size != 2) {
            encodedData.get(mBufferCache, mBufferOffset, bufferInfo.size);
            mBufferOffset += bufferInfo.size;

            if (mFirstCache.getValue()) {
                if (mBufferOffset > DefaultSetting.CACHE_BUFFER_SIZE) {
                    send_cached_buffer_to_socket();
                    mFirstCache.setValue(false);
                }
            } else if (mBufferOffset > DefaultSetting.CACHE_BUFFER_SIZE) {
                send_cached_buffer_to_socket();
            }
        }
    }

    public int count = 0;

    private void send_cached_buffer_to_socket() {
        try {
            //byte[] bb = Arrays.copyOf(mBufferCache.array(), mBufferCache.position());
            if (mBufferOffset > 0) {
                ProtoMessage.SpeakMsg.Builder builder = ProtoMessage.SpeakMsg.newBuilder();
                builder.setAudioData(ByteString.copyFrom(mBufferCache, 0, mBufferOffset));
                ProtoMessage.SpeakMsg msg = builder.build();
                MyService.start(MyApplication.getContext(), ProtoMessage.Cmd.cmdSpeakMsg_VALUE, msg);
                if (count > 100) {
                    count = 0;
                    Log.i(TAG, "发送语音数据字节数: " + mBufferOffset);
//                    Log.i(TAG,"got audio data size: " + HexTools.byteArrayToHex(mBufferCache));
                }
                count++;
                mBufferOffset = 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAVCDecoderConfigurationRecord(long tms, MediaFormat format) {
        if (dataCollecter != null) {
            byte[] AVCDecoderConfigurationRecord = Packager.H264Packager.generateAVCDecoderConfigurationRecord(format);
            int packetLen = Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH +
                    AVCDecoderConfigurationRecord.length;
            byte[] finalBuff = new byte[packetLen];
            Packager.FLVPackager.fillFlvVideoTag(finalBuff,
                    0,
                    true,
                    true,
                    AVCDecoderConfigurationRecord.length);
            System.arraycopy(AVCDecoderConfigurationRecord, 0,
                    finalBuff, Packager.FLVPackager.FLV_VIDEO_TAG_LENGTH, AVCDecoderConfigurationRecord.length);
            RESFlvData resFlvData = new RESFlvData();
            resFlvData.byteBuffer = finalBuff;
            resFlvData.size = finalBuff.length;
            resFlvData.dts = (int) tms;
            resFlvData.flvTagType = RESFlvData.FLV_RTMP_PACKET_TYPE_VIDEO;
            resFlvData.videoFrameType = RESFlvData.NALU_TYPE_IDR;
            dataCollecter.collect(resFlvData, RESRtmpSender.FROM_VIDEO);
        }
    }

    public int getStatus() {
        if (!prepared) {
            return STATUS_NULL_PREPARED;
        } else if (!started) {
            return STATUS_NULL_STARTED;
        } else {
            return STATUS_SUCCESS;
        }
    }

    public boolean isShow() {
        return mCamView != null;
    }

    public void onCallClick() {
        if (mCamView != null) {
            mCamView.performClick();
        }
    }

    public void switchCamera() {
        synchronized (SyncOp) {
            if (oldSelf) {
                oldSelf = false;
                if (isRoad) {
                    self_video = false;
                } else {
                    self_video = false;
                }

                if (mCamHideViewParams != null) {
                    mCamHideViewParams.width = 1;
                    mCamHideViewParams.height = 1;
                }

                if (mCamView != null && getStatus() == STATUS_SUCCESS) {
                    if (mCamView != null && dvrService != null) {
                        hideSurfaceView();
                    }

                    if (dvrService != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                dvrService.startPreview();
                                dvrService.show(mCamViewParams.x, mCamViewParams.y, mCamViewParams.width, mCamViewParams.height);
                            }
                        }).start();
                    }
                } else {
                    if (!Utils.getDvrTopActivity(MyApplication.getContext())) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                dvrService.startThumbnailPreview();
                            }
                        }).start();
                    }
                }
                Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
                intent.putExtra(ToastReceiver.TOAST_CONTENT, "当前对讲使用车外摄像头");
                MyApplication.getContext().sendBroadcast(intent);
            } else {
                oldSelf = true;
                int[] rect = new int[4];
                DvrConfig.getThumbnailViewRect(rect);
                if (mCamHideViewParams != null) {
                    mCamHideViewParams.width = rect[2];
                    mCamHideViewParams.height = rect[3];
                }
                if (isRoad) {
                    self_video = false;
                } else {
                    self_video = true;
                }
                if (!Utils.getDvrTopActivity(MyApplication.getContext())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dvrService.startThumbnailPreview();
                        }
                    }).start();
                }
                Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
                intent.putExtra(ToastReceiver.TOAST_CONTENT, "当前对讲使用车内摄像头");
                MyApplication.getContext().sendBroadcast(intent);
            }
            Log.e(TAG, "switchCamera Now self:" + self_video);
            if (getStatus() == STATUS_SUCCESS && !isRoad) {
                if (!prepare(resConfig)) {
                    Log.e(TAG, "prepare,failed!!");
                    self_video = false;
                    oldSelf = self_video;
                    startRecording(resConfig.getRtmpAddr(), true);
                    Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
                    intent.putExtra(ToastReceiver.TOAST_CONTENT, "设备不支持打开记录仪视频以外的摄像头");
                    MyApplication.getContext().sendBroadcast(intent);
                    return;
                }
                startStreaming();
            } else {
                hideSurfaceView();
            }
        }
    }

    public void setDvrService(DvrService dvrService) {
        this.dvrService = dvrService;
    }

    public void set(DvrService dvrService) {
        this.dvrService = dvrService;
    }

    public synchronized void removeSurfaceView() {
        Log.e(TAG, "removeSurfaceView");
        if (dvrService != null && mCamView != null) {
            dvrService.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WindowManager wm = (WindowManager) dvrService.getSystemService(Context.WINDOW_SERVICE);
                    try {
                        wm.removeView(mCamView);
                        mCamView = null;
                    } catch (Throwable e) {
                        e.printStackTrace();
                        mCamView = null;
                    }

                    Log.e(TAG, "removeView");
                }
            });
        }
    }

    public synchronized void createSurfaceView() {
        if (dvrService != null && mCamView == null) {
            Log.e(TAG, "createSurfaceView");
            dvrService.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCamView = new VideoFactory().createVideo(dvrService);
                    mCamView.setBackgroundResource(android.R.color.transparent);
                    mCamView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int[] rect = new int[4];
                            DvrConfig.getThumbnailViewRect(rect);
                            dvrService.sendBroadcast(new Intent(BottomLayoutManager.ACTION_VIDEO_CONTROL_SHOW));
                            if (oldSelf) {
                                dvrService.sendBroadcast(new Intent(RESClient.ACTION_ONCLICK_LEFT_TOP));
                            } else {
                                dvrService.sendBroadcast(new Intent(RESClient.ACTION_ONCLICK_DVR));
                            }
                        }
                    });
                    WindowManager wm = (WindowManager) dvrService.getSystemService(Context.WINDOW_SERVICE);
                    if (mCamHideViewParams == null) {
                        mCamHideViewParams = createHideParams();
                    }
                    wm.addView(mCamView, mCamHideViewParams);
                    Log.e(TAG, "addView");
                }
            });
        }
    }

    private void showSurfaceView() {
        if (mCamView != null) {
            Log.v(TAG, "showSurfaceView:");
            mCamView.setIsDrawing(true);
            if (dvrService != null) {
                dvrService.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager wm = (WindowManager) dvrService.getSystemService(Context.WINDOW_SERVICE);
                        if (mCamViewParams == null) {
                            mCamViewParams = createParams();
                        }
                        try {
                            wm.updateViewLayout(mCamView, mCamViewParams);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            mCamView = null;
                        }

                    }
                });
            }
        } else {
            createSurfaceView();
            showSurfaceView();
        }
    }

    private void hideSurfaceView() {
        if (mCamView != null) {
            Log.v(TAG, "hideSurfaceView:");
            mCamView.setIsDrawing(false);
            if (dvrService != null) {
                dvrService.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager wm = (WindowManager) dvrService.getSystemService(Context.WINDOW_SERVICE);
                        if (mCamHideViewParams == null) {
                            mCamHideViewParams = createHideParams();
                        }

                        Log.v(TAG, "hideSurfaceView:" + mCamHideViewParams.toString());
                        try {
                            wm.updateViewLayout(mCamView, mCamHideViewParams);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            mCamView = null;
                        }
                    }
                });
            }
        }
    }

    private WindowManager.LayoutParams createHideParams() {
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

        if (getOldSelf()) {
            params.x = rect[0];
            params.y = rect[1];
            params.width = rect[2];
            params.height = rect[3];
        } else {
            params.x = rect[0];
            params.y = rect[1];
            params.width = 1;
            params.height = 1;
        }
        Log.e(TAG, "createHideParams:" + params.toString());
        return params;
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

    public void updateSurfaceView(final int left, final int top, final int width, final int height) {
        if (dvrService != null) {
            dvrService.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WindowManager wm = (WindowManager) dvrService
                            .getSystemService(Context.WINDOW_SERVICE);
                    if (mCamViewParams == null) {
                        mCamViewParams = createParams();
                    }
                    boolean isNo = mCamViewParams.width != width;
                    mCamViewParams.x = left;
                    mCamViewParams.y = top;
                    mCamViewParams.width = width;
                    mCamViewParams.height = height;
                    Log.v(TAG, "updateSurfaceView:" + mCamViewParams.toString());
                    if (self_video && started && mCamView != null) {
                        try {
                            if (isNo) {
                                mCamView.setNoDrawing(true);
                                mainHander.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCamView.setNoDrawing(false);
                                    }
                                }, 200);
                            }
                            Log.v(TAG, "mCamView updateViewLayout");
                            wm.updateViewLayout(mCamView, mCamViewParams);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            mCamView = null;
                        }
                    }
                }
            });
        }
    }


    public boolean isTransferVoice() {
        return isTransferVoice;
    }

    public void setTransferVoice(boolean transferVoice) {
        mFirstCache.setValue(true);
        mBufferCache = new byte[14336];
        mBufferOffset = 0;
        isTransferVoice = transferVoice;
    }

    public void runOnUiThread(Runnable runnable) {
        if (dvrService != null) {
            dvrService.runOnUiThread(runnable);
        }
    }

    public Object getUsbDrawLock() {
        return usbDrawLock;
    }

    public RESFlvDataCollecter getDataCollecter() {
        return dataCollecter;
    }

    public RESCoreParameters getCoreParameters() {
        return coreParameters;
    }

    public void setDrvFormat(MediaFormat drvFormat) {
        this.drvFormat = drvFormat;
    }

    public DvrService getDvrService() {
        return dvrService;
    }
}
