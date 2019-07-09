package com.luobin.dvr.grafika;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BottomLayoutManager;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.receiver.ToastReceiver;
import com.example.jrd48.service.MyService;
import com.luobin.dvr.CollisionDetector;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.DvrImplBase;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.dvr.grafika.gles.EglCore;
import com.luobin.dvr.grafika.gles.FullFrameRect;
import com.luobin.dvr.grafika.gles.Texture2dProgram;
import com.luobin.dvr.grafika.gles.WindowSurface;
import com.luobin.musbcam.UsbCamera;
import com.luobin.musbcam.UsbVideoRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import me.lake.librestreaming.client.CameraVideo;
import me.lake.librestreaming.client.GlobalMediaCodec;
import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.model.RESConfig;

public class DvrImpl extends DvrImplBase
        implements Callback, UsbCamera.UsbCameraListener, OnFrameAvailableListener {

    private final String TAG = com.luobin.dvr.DvrService.TAG;
    private static final boolean VERBOSE = false;
    private final boolean TAKE_PHOTO_FROM_PREVIEW_DATA = true;
    private static final int SAMPLE_RATE = 44100; // must be same with DvrEncoder define

    private SurfaceView mCamView = null;
    private WindowManager.LayoutParams mCamViewParams = null;
    private boolean curNoDrawing = false;

    private Camera mCamera;
    private CameraVideo mCameraVideo;
    private UsbCamera usbCamera;
    private int mCameraPreviewThousandFps;
    private int mCamPrevWidth = 1280;
    private int mCamPrevHeight = 720;
    private int oldWidth = 0;
    private boolean isOpen;
    private boolean mAudioEnabled = false;

    private EglCore mEglCore;

    private WindowSurface mDisplaySurface;

    private FullFrameRect mFullFrameBlit;
    private FullFrameRect mWaterMarkBlit;
    private FullFrameRect mUsbFrameBlit;
    private ChatVideoScreenModeObserver mChatVideoScreenModeObserver;
    private int mCurrentVideoScreenMode = 0;
    private final float[] mTmpMatrix = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 1.0f};

    private long mFrameCount = 0;
    private final int mRecordingFlagFlashRate = 20; // frames
    private int mCamTextureId;
    private int mWaterMarkTextureId = -1;
    private int mUsbTextureId = -1;
    private int mWaterMarkWidth;
    private int mWaterMarkHeight;
    private int mWaterMarkGravity;

    public SurfaceTexture mCameraTexture;

    private DvrEncoder mDvrEncoder;
    public static final boolean USB_H264_CAM = true;//
    private UsbVideoRecorder mUsbVideoRecorder = null;

    private boolean mAudioRecording = false;
    private Thread mAudioRecordThread;
    private AudioRecord audioRecord;
    private Runnable mAudioRecordRunnable = new Runnable() {
        @Override
        public void run() {
            mAudioRecording = true;
            int bufferSizeInBytes;
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            try {
                bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSizeInBytes
                );
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            audioRecord.startRecording();

            Log.d(TAG, "audioRecord start");
            while (mAudioRecording) {
                final int bufsize = bufferSizeInBytes;
                byte[] buffer = new byte[bufsize];

                if (VERBOSE) Log.d(TAG, "Read audio data");
                int bufferReadResult = audioRecord.read(buffer, 0, bufsize);
                if (VERBOSE) Log.d(TAG, "Read audio data result = " + bufferReadResult);

                if (bufferReadResult > 0 && mDvrEncoder != null) {
                    mDvrEncoder.audioAvailable(buffer, bufferReadResult);
                }
                if (VERBOSE) Log.d(TAG, "Read audio data result");
            }
            Log.d(TAG, "audioRecord.stop");
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            mAudioRecordThread = null;
        }
    };

    private WindowSurface mEncoderSurface;

    private static final int MSG_FRAME_AVAILABLE = 1;
    private static final int MSG_FRAME_AVAILABLE_TIMEOUT = 2;

    private Object mLock = new Object();
    private Object mUsbLock = new Object();
    private boolean mWaitingForPreview = false;
    private boolean mPreviewResult = false;
    private boolean mTakingPhoto = false;
    private String mTakePhotoFile = null;

    private Rect mPreviewRect;

//    private byte mFrameBuf[];
//    private int mIn = -1, mOut = -1;
//    private int mBufCount = 3;
//    private int mFrameLen;

    private boolean usbThreadOn = true;
    private Thread mPreviewThread;
    private DecoderThread mDecoderThread;
    private GlobalMediaCodec globalMediaCodec = null;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_FRAME_AVAILABLE) {
                synchronized (mLock) {
                    if (mWaitingForPreview) {
                        mHandler.removeMessages(MSG_FRAME_AVAILABLE_TIMEOUT);
                        mWaitingForPreview = false;
                        Log.d(TAG, "mWaitingForPreview notify ");
                        mLock.notifyAll();
                    }
                }
                drawFrameUsb((Bitmap) msg.obj);
                updateTimeStampBmpIfNeeded();
                synchronized (RESClient.getInstance().getUsbDrawLock()) {
                    drawFrame();
                }
            } else if (msg.what == MSG_FRAME_AVAILABLE_TIMEOUT) {
                Log.e(TAG, "MSG_FRAME_AVAILABLE_TIMEOUT");
                if (mCamView != null) {
                    WindowManager wm = (WindowManager) mContext
                            .getSystemService(Context.WINDOW_SERVICE);
                    wm.removeView(mCamView);
                    mCamView = null;
                }
                mPreviewResult = false;
                synchronized (mLock) {
                    mWaitingForPreview = false;
                    mLock.notifyAll();
                }
                release();
            }
        }
    };

    public DvrImpl(Context context) {
        super(context);
        mChatVideoScreenModeObserver = new ChatVideoScreenModeObserver(new Handler());
        mChatVideoScreenModeObserver.startObserving();
        try {
            mCurrentVideoScreenMode = Settings.System.getInt(MyApplication.getContext().getContentResolver(), Settings.System.CHAT_VIDEO_SCREEN_MODE);
            if (mCurrentVideoScreenMode > 0) {
                DvrService.start(MyApplication.getContext(), RESClient.ACTION_SWITCH_RTMP, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isHiden = false;

    @Override
    public boolean show(final int x, final int y, final int w, final int h) {
        Log.d(TAG, "show enter ,x=" + x + ",y=" + y + ",w=" + w + ",h=" + h);
        int[] rect = new int[4];
        DvrConfig.getThumbnailViewRect(rect);
        if (w == rect[2] && RESClient.getInstance().getOldSelf()) {
            show(rect[0], rect[1], 1, 1);
            return true;
        }
        if (mCamViewParams != null && w != mCamViewParams.width) {
            mPreviewRect = null;
            isHiden = true;
            if (mCamView != null) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        WindowManager wm = (WindowManager) mContext
                                .getSystemService(Context.WINDOW_SERVICE);
                        mCamViewParams.x = x;
                        mCamViewParams.y = y;
                        mCamViewParams.width = w;
                        mCamViewParams.height = h;
                        Log.d(TAG, "mCamViewParams：234," + mCamViewParams.toString());
                        wm.updateViewLayout(mCamView, mCamViewParams);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isHiden = false;
                                mPreviewRect = new Rect(x, y, x + w, y + h);
                            }
                        }, 200);
                    }

                };
                RESClient.getInstance().runOnUiThread(r);
            }
        } else if (!isHiden) {
            mPreviewRect = new Rect(x, y, x + w, y + h);
            if (mCamViewParams != null) {
                mCamViewParams.x = x;
                mCamViewParams.y = mPreviewRect.top;
                mCamViewParams.width = mPreviewRect.width();
                mCamViewParams.height = mPreviewRect.height();
            }

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (mCamView != null) {
                        try {
                            WindowManager wm = (WindowManager) mContext
                                    .getSystemService(Context.WINDOW_SERVICE);
                            Log.d(TAG, "mCamViewParams：264" + mCamViewParams.toString());
                            wm.updateViewLayout(mCamView, mCamViewParams);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
            RESClient.getInstance().runOnUiThread(r);
            Log.d(TAG, "show return");
        }
        return true;
    }

    @Override
    public boolean hide() {
        Log.d(TAG, "hide");
        mPreviewRect = null;
        if (mCamView != null) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
//                    WindowManager wm = (WindowManager) mContext
//                            .getSystemService(Context.WINDOW_SERVICE);
                    int[] rect = new int[4];
                    DvrConfig.getThumbnailViewRect(rect);
                    show(rect[0], rect[1], rect[2], rect[3]);
//                    mPreviewRect = new Rect(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]);
//                    mCamViewParams.x = rect[0];
//                    mCamViewParams.y = rect[1];
//                    mCamViewParams.width = rect[2];
//                    mCamViewParams.height = rect[3];
//                    wm.updateViewLayout(mCamView, mCamViewParams);
                }

            };
            RESClient.getInstance().runOnUiThread(r);
        }

        return true;
    }

    @Override
    public boolean startPreview(int w, int h, boolean audioEnabled) {
        mPreviewResult = true;
        mAudioEnabled = audioEnabled;
        Log.d(TAG, "startPreview enter mCamView is " + (mCamView == null ? "" : "NOT") + " null");
        if (mCamView == null) {
//            release();
            mCamPrevWidth = w;
            mCamPrevHeight = h;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {

//
//                        if (!mPreviewThread.isAlive()) {
//                            mPreviewThread.start();
//                        }
                        mHandler.removeMessages(MSG_FRAME_AVAILABLE_TIMEOUT);
                        openCamera(mCamPrevWidth, mCamPrevHeight, RESConfig.FPS);
                        if (!isOpen) {
                            return;
                        }
                        usbThreadOn = true;
                        if (mDecoderThread == null) {
                            mDecoderThread = new DecoderThread();
                        }


                        if (!mDecoderThread.isAlive()) {
                            mDecoderThread.start();
                        }
                        mCamView = new SurfaceView(mContext);
                        mCamView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.v(TAG, "mCamView onClick");
//                                int[] rect = new int[4];
                                MyApplication.getContext().sendBroadcast(new Intent(BottomLayoutManager.ACTION_VIDEO_CONTROL_SHOW));
                                MyApplication.getContext().sendBroadcast(new Intent(RESClient.ACTION_ONCLICK_DVR));

//                                DvrConfig.getThumbnailViewRect(rect);
//                                if (RESClient.getInstance().getStatus() != RESClient.STATUS_NULL_PREPARED) {
//                                    if (mCamViewParams.x == rect[0]) {
//                                        MyApplication.getContext().sendBroadcast(new Intent(RESClient.ACTION_ONCLICK_LEFT_TOP));
//                                    }
//                                } else if (0 == Settings.System.getInt(mContext.getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0)) {
//                                    Intent intent = new Intent();
//                                    intent.setClassName("com.luobin.dvr", "com.luobin.dvr.ui.MainActivity");
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    mContext.startActivity(intent);
////                                    Intent intent = new Intent(mContext, WelcomeActivity.class);
////                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                                    mContext.startActivity(intent);
//                                } else if (1 == Settings.System.getInt(mContext.getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0)) {
//                                    Settings.System.putInt(mContext.getContentResolver(), MainActivity.DVR_FULLSCREEN_SHOW, 0);
//                                }
                            }
                        });
                        mCamView.getHolder().addCallback(DvrImpl.this);
                        WindowManager wm = (WindowManager) mContext
                                .getSystemService(Context.WINDOW_SERVICE);
                        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                        // params.type = WindowManager.LayoutParams.TYPE_TOAST;
                        params.format = PixelFormat.TRANSPARENT;
                        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                        params.gravity = Gravity.LEFT | Gravity.TOP;
                        params.x = -220;
                        params.y = 0;
                        params.width = 1;
                        params.height = 1;

                        MyService.start(mContext, false);
                        wm.addView(mCamView, params);
                        mCamViewParams = params;
                        Log.d(TAG, "mCamView added");
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        mHandler.removeMessages(MSG_FRAME_AVAILABLE_TIMEOUT);
                        mPreviewResult = false;
                        synchronized (mLock) {
                            mWaitingForPreview = false;
                            mLock.notifyAll();
                        }
                    }
                }

            };
            synchronized (mLock) {
                mHandler.post(r);
                mHandler.removeMessages(MSG_FRAME_AVAILABLE_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(MSG_FRAME_AVAILABLE_TIMEOUT, 10000);//5S
                mWaitingForPreview = true;
                while (mWaitingForPreview) {
                    try {
                        Log.d(TAG, "mWaittingForPreview waiting...");
                        mLock.wait();
                        Log.d(TAG, "mWaittingForPreview waiting exit");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (mCamViewParams != null) {
            show(mCamViewParams.x, mCamViewParams.y, mCamViewParams.width, mCamViewParams.height);
        }
        Log.d(TAG, "startPreview return");
        return mPreviewResult;
    }

    @Override
    public boolean stopPreview() {
        Log.d(TAG, "stopPreview");
        release();
        return false;
    }

    private String nowName = "";
    private String lastName = "";

    @Override
    public boolean startRecord(String file, boolean withBufferedVideo) {
        if (mCamView == null) {
            Log.e(TAG, "startRecord null mCamView");
            return false;
        }
        if (!USB_H264_CAM) {
            if (mDvrEncoder == null) {
                Log.e(TAG, "startRecord null mDvrEncoder");
                return false;
            }
        }

        lastName = nowName;
        nowName = file;
        mLastStartRecordingTime = SystemClock.elapsedRealtime();
        if (mLastStartRecordingTime - mLastCollisionTime < COLLISION_DURATION) {
            file = file.replace(".mp4", DvrService.LOCK + ".mp4");
        }
        Log.d(TAG, "DvrImpl startRecord " + file + " withBufferedVideo=" + withBufferedVideo);
        Log.d(TAG, "DvrImpl USB_H264_CAM =" + USB_H264_CAM);
        if (USB_H264_CAM) {
            if (mUsbVideoRecorder == null) {
                String  dev = mContext.getResources().getStringArray(R.array.video_devs)[2];//20190509
                String product = Build.PRODUCT;
                if (product != null && product.equals("LB1728V4")) {
                    dev = mContext.getResources().getStringArray(R.array.video_devs)[2];//h264 recorder
                }
                mUsbVideoRecorder = new UsbVideoRecorder();
                boolean res = mUsbVideoRecorder.start(dev, RESConfig.VIDEO_WIDTH_BIG, RESConfig.VIDEO_HEIGHT_BIG, file);
                if (!res) {
                    Log.e(TAG, "mUsbVideoRecorder.start return false");
                    mUsbVideoRecorder = null;
                    return false;
                }
            } else {
                mUsbVideoRecorder.switchNewFile(file);
            }
        } else {
            mDvrEncoder.saveFile(file, withBufferedVideo);

        }
        if (!mSavingFile) {
            mSavingFile = true;
            startCollisionDetector();
        }
        return true;
    }

    @Override
    public boolean stopRecord() {
        if (mUsbVideoRecorder != null) {
            mUsbVideoRecorder.stop();
            mUsbVideoRecorder = null;
        }
        if (!USB_H264_CAM) {
            if (mDvrEncoder == null) {
                Log.e(TAG, "stopRecord null mDvrEncoder");
                return false;
            }
            mDvrEncoder.stopRecord();
        }
        stopCollisionDetector();
        mSavingFile = false;
        return true;
    }

    /**
     * Opens a camera, and attempts to establish preview mode at the specified width and height.
     * <p>
     * Sets mCameraPreviewFps to the expected frame rate (which might actually be variable).
     */
    private void openCamera(int desiredWidth, int desiredHeight, int desiredFps) {

        if (usbCamera != null) {
            throw new RuntimeException("camera already initialized");
        }
        String dev = mContext.getResources().getStringArray(R.array.video_devs)[1];
        String product = Build.PRODUCT;
        if (product != null && (product.equals("LB1728V4") || product.equals("LB1822"))) {
            dev = mContext.getResources().getStringArray(R.array.video_devs)[1];
        }
        File file = new File(dev);
        if (!file.exists()) {
            ToastR.setToast(mContext, dev + MyApplication.getContext().getString(R.string.camera_node_not_exist));
            Log.v(TAG, dev + " is not exist");
            return;
        }

        if (GlobalStatus.getUsbVideo2() != null) {
            usbCamera = GlobalStatus.getUsbVideo2();
            usbCamera.start(this);
            isOpen = true;
        } else {
            usbCamera = new UsbCamera();
            int size2[] = new int[2];
            size2[0] = desiredWidth;
            size2[1] = desiredHeight;
            isOpen = usbCamera.open(dev, size2);
            if (!isOpen) {
                Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
                intent.putExtra(ToastReceiver.TOAST_CONTENT, dev + MyApplication.getContext().getString(R.string.usb_open_failed));
                MyApplication.getContext().sendBroadcast(intent);
                usbCamera = null;
                GlobalStatus.setUsbVideo2(null);
            } else {
                GlobalStatus.setUsbVideo2(usbCamera);
                usbCamera.start(this);
            }
        }


        mCameraPreviewThousandFps = desiredFps * 1000;
        mCamPrevWidth = desiredWidth;
        mCamPrevHeight = desiredHeight;
        /*if(GlobalStatus.getUsbVideo1() != null){
            GlobalStatus.getUsbVideo1().close();
            GlobalStatus.setUsbVideo1(null);
        }
        UsbCamera cam1 = new UsbCamera();
        String dev1 = mContext.getResources().getStringArray(R.array.video_devs)[1];
        int size1[] = new int[2];
        size1[0] = RESConfig.VIDEO_WIDTH;
        size1[1] = RESConfig.VIDEO_HEIGHT;
        if(!cam1.open(dev1, size1)){
            GlobalStatus.setUsbVideo1(null);
        } else {
            GlobalStatus.setUsbVideo1(cam1);
        }*/
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    public void setNoDrawing(boolean isDrawing) {
        Log.v(TAG, "setNoDrawing:" + isDrawing);
        this.curNoDrawing = isDrawing;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated holder=" + holder);

        // Set up everything that requires an EGL context.
        //
        // We had to wait until we had a surface because you can't make an EGL context current
        // without one, and creating a temporary 1x1 pbuffer is a waste of time.
        //
        // The display surface that we use for the SurfaceView, and the encoder surface we
        // use for video, use the same EGL context.
        GlobalStatus.setIsDvrCamShow(true);
        mEglCore = GlobalStatus.getEglCore();
        if (mEglCore == null) {
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            GlobalStatus.setEglCore(mEglCore);
        }
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        mFullFrameBlit = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mWaterMarkBlit = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D));
        mUsbFrameBlit = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D));
        mCamTextureId = mFullFrameBlit.createTextureObject();
        mCameraTexture = new SurfaceTexture(mCamTextureId);
//        mCameraTexture.setOnFrameAvailableListener(this);

//        if(GlobalStatus.getCamera() == null){
//            USBVideo.openNewCamera(RESConfig.VIDEO_WIDTH, RESConfig.VIDEO_HEIGHT);
//        }
//        Log.d(TAG, "starting camera preview");
//        try {
//            mCamera.setPreviewTexture(mCameraTexture);
//        } catch (IOException ioe) {
//            throw new RuntimeException(ioe);
//        }
//        mCamera.startPreview();

        // TODO: adjust bit rate based on frame rate?
        // TODO: adjust video width/height based on what we're getting from the camera preview?
        //       (can we guarantee that camera preview size is compatible with AVC video encoder?)
        // zhouyuhuan modify : allow user to set value 20170410
        int bitrate = mCamPrevHeight < 500 ? 2000000 :
                mCamPrevHeight < 800 ? 5000000 :
                        mCamPrevHeight < 1200 ? 10000000 : 10000000;
        /* int bitrate = DvrConfig.getVideoBitrate();*/
//        if (mAudioEnabled) {
//            bitrate += SAMPLE_RATE*16*1; // audio sample rate * bit depth * channel
//        }
        if (!USB_H264_CAM) {
            //get pre video time
            int time = DvrConfig.getPreVideoTime();
            try {
                if (mDvrEncoder == null) {
                    mDvrEncoder = new DvrEncoder(mCamPrevWidth, mCamPrevHeight, bitrate,
                            mCameraPreviewThousandFps / 1000, time, mAudioEnabled, isOpen);//10
                }
//            mDvrEncoder = new DvrEncoder(mCamPrevWidth, mCamPrevHeight, RESConfig.BITRATE,
//                    mCameraPreviewThousandFps / 1000, time, mAudioEnabled);//10
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }

            if (mDvrEncoder == null) {
                return;
            }
            mEncoderSurface = new WindowSurface(mEglCore, mDvrEncoder.getInputSurface(), true);

        }

        if (globalMediaCodec != null) {
            globalMediaCodec.shutdown();
            globalMediaCodec = null;
        }

        globalMediaCodec = new GlobalMediaCodec();
        globalMediaCodec.start(mEglCore);
        if (mAudioEnabled && (mAudioRecordThread == null || !mAudioRecordThread.isAlive())) {
            mAudioRecordThread = new Thread(mAudioRecordRunnable);
            mAudioRecordThread.start();
        }

        if (GlobalStatus.getUsbVideo2() != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RESClient.getInstance().createSurfaceView();
                }
            }, 3000);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.v(TAG, "surfaceChanged: w=" + width + ",h=" + height);
        setNoDrawing(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setNoDrawing(false);
            }
        }, 200);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
	
	@Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (mLock) {
            if (mWaitingForPreview) {
                mHandler.removeMessages(MSG_FRAME_AVAILABLE_TIMEOUT);
                mWaitingForPreview = false;
                Log.d(TAG, "mWaitingForPreview notify ");
                mLock.notifyAll();
            }
        }
        updateTimeStampBmpIfNeeded();
        mHandler.sendEmptyMessage(MSG_FRAME_AVAILABLE);
    }

    private void updateTimeStampBmpIfNeeded() {
        synchronized (this) {
            synchronized (mBmpLock) {
                if (mWaterMarkBmp != null) {
                    mWaterMarkWidth = mWaterMarkBmp.getWidth();
                    mWaterMarkHeight = mWaterMarkBmp.getHeight();
                    mWaterMarkGravity = super.mWaterMarkGravity;
                    //                Log.d(TAG, "createTimeStampTexture");
                    if (mWaterMarkTextureId >= 0) {
                        GLES20.glDeleteTextures(1,
                                new int[]{mWaterMarkTextureId}, 0);
                        checkGlError("glDeleteTextures");
                    }
                    int[] textures = new int[1];
                    GLES20.glGenTextures(1, textures, 0);
                    checkGlError("glGenTextures");
                    mWaterMarkTextureId = textures[0];
                    //                Log.d(TAG, "glGenTextures Y = " + mWaterMarkTextureId);

                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterMarkTextureId);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mWaterMarkBmp, 0);
                    mWaterMarkBmp = null;
                }
            }
        }
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private void setWaterMarkViewport(int viewWidth, int viewHeight) {
        int width, height;
        int x = (mWaterMarkGravity & Gravity.RIGHT) == Gravity.RIGHT ? (viewWidth - mWaterMarkWidth) :
                (mWaterMarkGravity & Gravity.LEFT) == Gravity.LEFT ? 0 : (viewWidth - mWaterMarkWidth) / 2;
        int y = (mWaterMarkGravity & Gravity.BOTTOM) == Gravity.BOTTOM ? 0 :
                (mWaterMarkGravity & Gravity.TOP) == Gravity.TOP ? (viewHeight - mWaterMarkHeight) :
                        (viewHeight - mWaterMarkHeight) / 2;

        width = x < 0 ? viewWidth : mWaterMarkWidth;
        x = x < 0 ? 0 : x;
        height = y < 0 ? viewHeight : mWaterMarkHeight;
        y = y < 0 ? 0 : y;
        //Log.d(TAG, "viewWidth="+viewWidth+",viewHeight="+viewHeight+",width="+width+", height="+height+",x="+x+",y="+y);
        GLES20.glViewport(x, y, width, height);
    }

    /**
     * Draws a frame onto the SurfaceView and the encoder surface.
     * <p>
     * This will be called whenever we get a new preview frame from the camera.  This runs
     * on the UI thread, which ordinarily isn't a great idea -- you really want heavy work
     * to be on a different thread -- but we're really just throwing a few things at the GPU.
     * The upside is that we don't have to worry about managing state changes between threads.
     * <p>
     * If there was a pending frame available notification when we shut down, we might get
     * here after onPause().
     */
    private void drawFrame() {
        //Log.d(TAG, "drawFrame ,"+Thread.currentThread().getName());
        int dvrShow = Settings.System.getInt(mContext.getContentResolver(), "dvr_fullscreen_show", 0);
        if (mEglCore == null) {
            Log.d(TAG, "Skipping drawFrame after shutdown");
            return;
        }

        if (mCamView == null) {
            Log.d(TAG, "Skipping drawFrame for null camview");
            return;
        }

        // Latch the next frame from the camera.
        mDisplaySurface.makeCurrent();
        mCameraTexture.updateTexImage();
//        mCameraTexture.getTransformMatrix(mTmpMatrix);
        /*
        for (int i=0; i<mTmpMatrix.length; i+=4) {
            Log.d(TAG, "mTmpMatrix = " + mTmpMatrix[i] + " " + mTmpMatrix[i+1] + " " + mTmpMatrix[i+2] + " " + mTmpMatrix[i+3]);
        }
        */

        if (mPreviewRect != null && mPreviewRect.width() > 1 && !curNoDrawing) {
            //Log.d(TAG,"mPreviewRect != null && mPreviewRect.width() > 1");
            // Fill the SurfaceView with it.
            SurfaceView sv = mCamView;
            int viewWidth = sv.getWidth();
            int viewHeight = sv.getHeight();
            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            mFullFrameBlit.drawFrame(mCamTextureId, mTmpMatrix);
            //Log.v(TAG,"drawFrame viewWidth=" + viewWidth+",viewHeight="+viewHeight);

            /*if (mUsbTextureId >= 0) {
                GLES20.glViewport(0, 0, viewWidth, viewHeight);
                mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
            }*/
            if (dvrShow == 1) {
                if (mUsbTextureId >= 0) {
                    GLES20.glViewport(0, 0, viewWidth, viewHeight);
                    mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                }
            } else {
                if (mUsbTextureId >= 0) {
                    switch (mCurrentVideoScreenMode) {
                        case 0:
                        case 1:
                            GLES20.glViewport(0, 0, viewWidth, viewHeight);
                            mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                            break;
                        case 2:
                            switch (GlobalStatus.getPipMode()) {
                                case 0:
                                    GLES20.glViewport(0, 0, viewWidth, viewWidth);
                                    mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                    GLES20.glViewport(viewWidth / 2, 0, viewWidth / 2, viewHeight / 2);
                                    mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                    break;
                                case 1:
                                    GLES20.glViewport(0, 0, viewWidth, viewWidth);
                                    mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                    GLES20.glViewport(0, 0, viewWidth / 2, viewHeight / 2);
                                    mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                    break;
                                case 2:
                                    GLES20.glViewport(0, 0, viewWidth, viewWidth);
                                    mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                    GLES20.glViewport(viewWidth / 2, 0, viewWidth / 2, viewHeight / 2);
                                    mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                    break;
                                case 3:
                                    GLES20.glViewport(0, 0, viewWidth, viewWidth);
                                    mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                    GLES20.glViewport(0, 0, viewWidth / 2, viewHeight / 2);
                                    mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                    break;
                            }
                            break;
                    }
                }
            }
            /*
            Log.d(TAG, "mTmpMatrix="+mTmpMatrix);
            for (int i=0; i<mTmpMatrix.length; i+=4) {
                Log.d(TAG, "mTmpMatrix = " + mTmpMatrix[i] + " " + mTmpMatrix[i+1] + " " + mTmpMatrix[i+2] + " " + mTmpMatrix[i+3]);
            }
            */
            if (mWaterMarkTextureId >= 0) {
                setWaterMarkViewport(mCamView.getWidth(), mCamView.getHeight());
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                mWaterMarkBlit.drawFrame(mWaterMarkTextureId, mTmpMatrix);
            }

            if (isRecording() && (mFrameCount % (2 * mRecordingFlagFlashRate) < mRecordingFlagFlashRate)) {
                //drawBox(viewWidth - 40, viewHeight - 40, 20, 20, isCollided());
                if (dvrShow == 1) {
                    drawRound1(isCollided());
                }
            }
        }

        mDisplaySurface.swapBuffers();

        if (!USB_H264_CAM) {
            // Send it to the video encoder.
            mEncoderSurface.makeCurrent();
            GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
            mFullFrameBlit.drawFrame(mCamTextureId, mTmpMatrix);
            if (mUsbTextureId >= 0) {
                GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
            }
            if (mWaterMarkTextureId >= 0) {
                setWaterMarkViewport(mCamPrevWidth, mCamPrevHeight);
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                mWaterMarkBlit.drawFrame(mWaterMarkTextureId, mTmpMatrix);
            }
//        drawExtra(mFrameNum, VIDEO_WIDTH, VIDEO_HEIGHT);

            mDvrEncoder.frameAvailableSoon();
            long presentationTimeUs = System.nanoTime();
            mEncoderSurface.setPresentationTime(presentationTimeUs);
            mEncoderSurface.swapBuffers();
        }

        if (!RESClient.getInstance().getSelf_video() && RESClient.getInstance().getStatus() == RESClient.STATUS_SUCCESS) {
            if (globalMediaCodec != null && globalMediaCodec.isStarted()) {
                WindowSurface windowSurface = globalMediaCodec.getmEncoderSurface();
                try {
                    windowSurface.makeCurrent();
                    int mCamPrevWidth = RESConfig.VIDEO_WIDTH;
                    int mCamPrevHeight = RESConfig.VIDEO_HEIGHT;
                    GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                    mFullFrameBlit.drawFrame(mCamTextureId, mTmpMatrix);
                    if (mUsbTextureId >= 0) {
                        //GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                        //mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                        switch (mCurrentVideoScreenMode) {
                            case 0:
                            case 1:
                                GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                                mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                break;
                            case 2:
                                switch (GlobalStatus.getPipMode()) {
                                    case 0:
                                        GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                                        mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                        GLES20.glViewport(mCamPrevWidth / 2, 0, mCamPrevWidth / 2, mCamPrevHeight / 2);
                                        mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                        break;
                                    case 1:
                                        GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                                        mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                        GLES20.glViewport(0, 0, mCamPrevWidth / 2, mCamPrevHeight / 2);
                                        mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                        break;
                                    case 2:
                                        GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                                        mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                        GLES20.glViewport(mCamPrevWidth / 2, 0, mCamPrevWidth / 2, mCamPrevHeight / 2);
                                        mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                        break;
                                    case 3:
                                        GLES20.glViewport(0, 0, mCamPrevWidth, mCamPrevHeight);
                                        mUsbFrameBlit.drawFrame(GlobalStatus.getUsbTextureId(), mTmpMatrix);
                                        GLES20.glViewport(0, 0, mCamPrevWidth / 2, mCamPrevHeight / 2);
                                        mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
                                        break;
                                }
                                break;
                        }
                    }
                    /*if (mWaterMarkTextureId >= 0) {
                        setWaterMarkViewport(mCamPrevWidth, mCamPrevHeight);
                        GLES20.glEnable(GLES20.GL_BLEND);
                        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                        mWaterMarkBlit.drawFrame(mWaterMarkTextureId, mTmpMatrix);
                    }*/
                    globalMediaCodec.frameAvailableSoon();
                    long presentationTimeUs = System.nanoTime();
                    windowSurface.setPresentationTime(presentationTimeUs);
                    windowSurface.swapBuffers();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        if (TAKE_PHOTO_FROM_PREVIEW_DATA) {
            synchronized (mLock) {
                if (mTakingPhoto) {
                    takePhotoLocked(mTakePhotoFile);
                    mTakingPhoto = false;
                    mLock.notifyAll();
                }
            }
        }

        //mFrameNum++;
    }

    private void takePhotoLocked(String file) {
        Log.d(TAG, "begin save photo...");
        IntBuffer pixelBuf = IntBuffer.allocate(mCamPrevWidth * mCamPrevHeight);
        pixelBuf.order();
        GLES20.glReadPixels(0, 0, mCamPrevWidth, mCamPrevHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuf);
        int[] rgbabuf = pixelBuf.array();
        int[] argbbuf = new int[rgbabuf.length];
        int offset1, offset2;
        for (int i = 0; i < mCamPrevHeight; i++) {
            offset1 = i * mCamPrevWidth;
            offset2 = (mCamPrevHeight - i - 1) * mCamPrevWidth;
            for (int j = 0; j < mCamPrevWidth; j++) {
                int texturePixel = rgbabuf[offset1 + j];
                int blue = (texturePixel >> 16) & 0xff;
                int red = (texturePixel << 16) & 0x00ff0000;
                int pixel = (texturePixel & 0xff00ff00) | red | blue;
                argbbuf[offset2 + j] = pixel;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(argbbuf, mCamPrevWidth, mCamPrevHeight, Bitmap.Config.ARGB_8888);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 80, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "save photo end");
    }

    @Override
    public boolean release() {
        Log.d(TAG, "release enter");
        final Object lock = new Object();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "release Runnable");
                mHandler.removeCallbacksAndMessages(null);
                synchronized (lock) {
                    if (mAudioRecordThread != null) {
                        mAudioRecording = false;
                        try {
                            mAudioRecordThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "release mDvrEncoder != null");
                    if (mDvrEncoder != null) {
                        mDvrEncoder.shutdown();
                        mDvrEncoder = null;
                    }
//                    if (mCamera != null) {
//                        releaseCamera();
//                    }


                    if (globalMediaCodec != null) {
                        globalMediaCodec.shutdown();
                        globalMediaCodec = null;
                    }

                    if (usbCamera != null && isOpen) {
                        usbCamera.close();
                    }
//                    if (GlobalStatus.getUsbVideo1() != null) {
//                        GlobalStatus.getUsbVideo1().close();
//                        GlobalStatus.setUsbVideo1(null);
//                    }
                    usbCamera = null;
                    GlobalStatus.setUsbVideo2(null);
                    usbThreadOn = false;
                    if (mPreviewThread != null) {
                        try {
                            mPreviewThread.join(1000);
                            mPreviewThread.interrupt();
                            mPreviewThread = null;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (mDecoderThread != null) {
                        try {
                            mDecoderThread.join(1000);
                            mDecoderThread.interrupt();
                            mDecoderThread = null;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                    if (mDisplaySurface != null) {
                        mDisplaySurface.release();
                        mDisplaySurface = null;
                    }

                    if (mDisplaySurface != null) {
                        mEncoderSurface.release();
                        mEncoderSurface = null;
                    }

                    if (mFullFrameBlit != null) {
                        mFullFrameBlit.release(true);
                    }

                    if (mUsbFrameBlit != null) {
                        mUsbFrameBlit.release(true);
                    }

                    if (mWaterMarkBlit != null) {
                        mWaterMarkBlit.release(true);
                    }

                    GlobalStatus.setIsDvrCamShow(false);
                    if (mEglCore != null && !GlobalStatus.isUSBVideoShow()) {
                        mEglCore.release();
                        mEglCore = null;
                        GlobalStatus.setEglCore(null);
                        if (GlobalStatus.getUsbVideo1() != null) {
                            GlobalStatus.getUsbVideo1().close();
                            GlobalStatus.setUsbVideo1(null);
                        }
                        if (GlobalStatus.getCamera() != null) {
                            GlobalStatus.getCamera().stopPreview();
                            GlobalStatus.getCamera().release();
                            GlobalStatus.setCamera(null);
                        }
                    }

                    if (mCamView != null) {
                        WindowManager wm = (WindowManager) mContext
                                .getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(mCamView);
                        mCamView = null;
                    }
                    MyService.start(mContext, true);
                }
            }
        };
        RESClient.getInstance().runOnUiThread(r);
        synchronized (mBmpLock) {
            mWaterMarkBmp = null;
        }
        if (mWaterMarkTextureId >= 0) {
            GLES20.glDeleteTextures(1,
                    new int[]{mWaterMarkTextureId}, 0);
            checkGlError("glDeleteTextures");
            mWaterMarkTextureId = -1;
        }

        if (mUsbTextureId >= 0) {
            GLES20.glDeleteTextures(1,
                    new int[]{mUsbTextureId}, 0);
            checkGlError("glDeleteTextures");
            mUsbTextureId = -1;
        }
        Log.d(TAG, "release return");
        //mChatVideoScreenModeObserver.stopObserving();
        return true;
    }

    @Override
    public boolean takePhoto(String file) {
        if (TAKE_PHOTO_FROM_PREVIEW_DATA) {
            if (mEncoderSurface == null) return false;
            synchronized (mLock) {
                mTakePhotoFile = file;
                mTakingPhoto = true;
                while (mTakingPhoto) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (mCamView == null) return false;
            final String photofile = file;
            mTakingPhoto = true;
            if (usbCamera != null) {
                usbCamera.setPictureCallback(new UsbCamera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data) {
                        usbCamera.setPictureCallback(null);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(photofile);
                            fos.write(data);
                            fos.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
//            mCamera.takePicture(null, null, new Camera.PictureCallback() {
//
//                @Override
//                public void onPictureTaken(byte[] data, Camera camera) {
//                    Log.d(TAG, "onPictureTaken");
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(photofile);
//                        fos.write(data);
//                        fos.flush();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            fos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    mCamera.startPreview();
//                    synchronized (mLock) {
//                        mTakingPhoto = false;
//                        mLock.notifyAll();
//                    }
//
//                }
//            });
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    @Override
    public boolean isPreviewing() {
        Log.d(TAG, "isPreviewing=" + (mCamView != null));
        return mCamView != null;
    }

    @Override
    public void onClick() {
        if (mCamView != null) {
            Log.i(TAG, "performClick");
            mCamView.performClick();
        }
    }

    @Override
    public void onJpegFrame(byte[] framebuf) {
//        if (mFrameBuf == null) {
//            mFrameLen = framebuf.length;
//            mFrameBuf = new byte[mFrameLen * mBufCount];
//            mIn = 0;
//            mOut = 0;
//        }
        mFrameCount++;
        if (mFrameCount == Long.MAX_VALUE) {
            mFrameCount = 0;
        }
        if (options == null) {
            options = new BitmapFactory.Options();
            options.inBitmap = Bitmap.createBitmap(mCamPrevWidth, mCamPrevHeight, Bitmap.Config.ARGB_8888);
            options.inMutable = true;
            options.outWidth = mCamPrevWidth;
            options.outHeight = mCamPrevHeight;
            //options.inSampleSize = 1;//fix Problem decoding into existing bitmap
        }
        Bitmap tempBmp = null;
        try {
            tempBmp = BitmapFactory.decodeByteArray(framebuf, 0, framebuf.length, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        synchronized (mUsbLock) {
            mUsbBmp = tempBmp;
            mUsbLock.notifyAll();
        }
    }


    private int count = 0;
    private long lastTime = 0;

    private void drawFrameUsb(Bitmap usbBmp) {
        try {
            if (usbBmp != null) {
                if (mUsbTextureId >= 0) {
                    GLES20.glDeleteTextures(1,
                            new int[]{mUsbTextureId}, 0);
                    checkGlError("glDeleteTextures");
                }

                int[] textures2 = new int[1];
                GLES20.glGenTextures(1, textures2, 0);
                checkGlError("glGenTextures");
                mUsbTextureId = textures2[0];
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUsbTextureId);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, usbBmp, 0);
                GlobalStatus.setDvrTextureId(mUsbTextureId);
                GlobalStatus.setBitmapDvr(usbBmp);
                usbBmp = null;
            } else {
                Log.e(TAG, "bmp == null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class DecoderThread extends Thread {
        @Override
        public void run() {
            while (usbThreadOn) {
                synchronized (mUsbLock) {
                    //Log.e(TAG, "=====DecoderThread,mUsbLock wait"+Thread.currentThread().getName());
                    try {
                        mUsbLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Log.e(TAG, "=====DecoderThread,"+Thread.currentThread().getName());
                if (mUsbBmp != null) {
                    //Log.e(TAG, "=====DecoderThread,mUsbBmp != null,"+Thread.currentThread().getName());
                    Message message = new Message();
                    message.what = MSG_FRAME_AVAILABLE;
                    message.obj = mUsbBmp;
                    mHandler.sendMessage(message);
                    mUsbBmp = null;
                }
            }
            super.run();
        }
    }

    private int mRound1Table[][] = null;
    private int mRound1TableLen = 0;
    private int mRound1Table2[][] = null;
    private int mRound1TableLen2 = 0;

    private void drawRound1(boolean collided) {
        if (mCamView != null) {
            int viewWidth = mCamView.getWidth();
            int viewHeight = mCamView.getHeight();
            if (viewWidth < 500) {

                if (mRound1Table == null) {
                    int r = 10;
                    int[] rect = new int[4];
                    DvrConfig.getThumbnailViewRect(rect);
                    int x = rect[2] - r - 10;
                    int y = rect[3] - r - 10;
                    mRound1Table = new int[r * 2][3];
                    for (int i = 0; i < r; i++) {
                        mRound1Table[2 * i][0] = x - (int) (Math.sqrt(r * r - i * i) + 0.5); // line left
                        mRound1Table[2 * i][1] = y + i;                                 // line top
                        mRound1Table[2 * i][2] = 2 * (x - mRound1Table[2 * i][0]) - 1;          // line len

                        mRound1Table[2 * i + 1][0] = x - (int) (Math.sqrt(r * r - i * i) + 0.5); // line left
                        mRound1Table[2 * i + 1][1] = y - i;                                 // line top
                        mRound1Table[2 * i + 1][2] = 2 * (x - mRound1Table[2 * i + 1][0]) - 1;          // line len
                    }
                    mRound1TableLen = 2 * r;
                }
                GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
                for (int i = 0; i < mRound1TableLen; i++) {
                    int left = mRound1Table[i][0];
                    int top = mRound1Table[i][1];
                    int width = mRound1Table[i][2];
                    int height = 1;
                    GLES20.glScissor(left, top, width, height);
                    if (collided) {
                        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
                    } else {
                        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
                    }
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                }
                GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
            } else {
                if (oldWidth == 0 || oldWidth != viewWidth) {
                    Log.v(TAG, "drawFrame viewWidth=" + viewWidth + ",viewHeight=" + viewHeight);
                    oldWidth = viewWidth;
                    mRound1Table2 = null;
                }

                if (mRound1Table2 == null) {
                    int r = 10;
                    int x = mCamView.getWidth() - r - 20;
                    int y = mCamView.getHeight() - r - 20;
                    mRound1Table2 = new int[r * 2][3];
                    for (int i = 0; i < r; i++) {
                        mRound1Table2[2 * i][0] = x - (int) (Math.sqrt(r * r - i * i) + 0.5); // line left
                        mRound1Table2[2 * i][1] = y + i;                                 // line top
                        mRound1Table2[2 * i][2] = 2 * (x - mRound1Table2[2 * i][0]) - 1;          // line len

                        mRound1Table2[2 * i + 1][0] = x - (int) (Math.sqrt(r * r - i * i) + 0.5); // line left
                        mRound1Table2[2 * i + 1][1] = y - i;                                 // line top
                        mRound1Table2[2 * i + 1][2] = 2 * (x - mRound1Table2[2 * i + 1][0]) - 1;          // line len
                    }
                    mRound1TableLen2 = 2 * r;
                }
                GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
                for (int i = 0; i < mRound1TableLen2; i++) {
                    int left = mRound1Table2[i][0];
                    int top = mRound1Table2[i][1];
                    int width = mRound1Table2[i][2];
                    int height = 1;
                    GLES20.glScissor(left, top, width, height);
                    if (collided) {
                        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
                    } else {
                        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
                    }
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                }
                GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
            }
        }
    }

    private long mLastStartRecordingTime = -1;
    private long mLastCollisionTime = -1;
    private static final long COLLISION_DURATION = 15 * 1000;
    private CollisionDetector mCollisionDetector = null;
    private CollisionDetector.CollisionListener mCollisionListener = new CollisionDetector.CollisionListener() {
        @Override
        public void onCollide() {
            mLastCollisionTime = SystemClock.elapsedRealtime();
            Log.d(TAG, "==== onCollide ==== at " + mLastCollisionTime);
            if (mLastCollisionTime - mLastStartRecordingTime < COLLISION_DURATION) {
                //TODO lock last
                if (!TextUtils.isEmpty(lastName)) {
                    File file = new File(lastName);
                    if (file.exists()) {
                        boolean isSuccess = file.renameTo(new File(lastName.replace(".mp4", DvrService.LOCK + ".mp4")));
                        Log.d(TAG, "==== rename ==== " + isSuccess);
                    }
                }
            }
            if (mDvrEncoder != null) {
                mDvrEncoder.setLockNow(true);
            }
            if (mUsbVideoRecorder != null) {
                mUsbVideoRecorder.setLock(true);
            }
            //TODO lock now
//            if (mRecorderFiles != null) {
//                mRecorderFiles.onCollide(cur-COLLISION_DURATION, cur+COLLISION_DURATION);
//            }
        }
    };

    private boolean isCollided() {
        return mLastCollisionTime > 0 && (SystemClock.elapsedRealtime() - mLastCollisionTime < COLLISION_DURATION);
    }

    private void startCollisionDetector() {
        if (mCollisionDetector == null) {
            mCollisionDetector = new CollisionDetector(mContext, mCollisionListener);
        }
    }

    private void stopCollisionDetector() {
        if (mCollisionDetector != null) {
            mCollisionDetector.release();
            mCollisionDetector = null;
        }
    }

    /**
     * ContentObserver to watch video chat behavior
     **/
    private class ChatVideoScreenModeObserver extends ContentObserver {

        private final Uri CHAT_VIDEO_SCREEN_MODE_URI =
                Settings.System.getUriFor(Settings.System.CHAT_VIDEO_SCREEN_MODE);

        public ChatVideoScreenModeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;
            try {
                int lastVideoScreenMode = mCurrentVideoScreenMode;
                mCurrentVideoScreenMode = Settings.System.getInt(MyApplication.getContext().getContentResolver(), Settings.System.CHAT_VIDEO_SCREEN_MODE);
                Log.d("ChatVideoScreenMode", "=mCurrentVideoScreenMode=" + mCurrentVideoScreenMode);
                Log.e("====", "=mCurrentVideoScreenMode=" + mCurrentVideoScreenMode);
                if (lastVideoScreenMode == 0 && mCurrentVideoScreenMode != 0 || lastVideoScreenMode != 0 && mCurrentVideoScreenMode == 0) {
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_SWITCH_RTMP, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startObserving() {
            final ContentResolver cr = MyApplication.getContext().getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    CHAT_VIDEO_SCREEN_MODE_URI,
                    false, this);
        }

        public void stopObserving() {
            final ContentResolver cr = MyApplication.getContext().getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
}
