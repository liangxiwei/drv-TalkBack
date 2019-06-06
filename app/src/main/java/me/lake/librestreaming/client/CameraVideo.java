package me.lake.librestreaming.client;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.grafika.CameraUtils;
import com.luobin.dvr.grafika.gles.EglCore;
import com.luobin.dvr.grafika.gles.FullFrameRect;
import com.luobin.dvr.grafika.gles.Texture2dProgram;
import com.luobin.dvr.grafika.gles.WindowSurface;

import java.io.IOException;

import me.lake.librestreaming.model.RESConfig;

/**
 * Created by zhouyuhuan on 2018/4/2.
 */


public class CameraVideo extends VideoBase implements SurfaceHolder.Callback,SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "CameraVideo";
    private Camera mCamera;
    private static final int MSG_FRAME_AVAILABLE = 1;
    private int mCamTextureId;
    private EglCore mEglCore;
    private WindowSurface mDisplaySurface;
    private FullFrameRect mFullFrameBlit;
    private SurfaceTexture mCameraTexture;
    private final float[] mTmpMatrix = new float[16];
    private GlobalMediaCodec globalMediaCodec;
    private boolean isDrawing = false;
    private boolean curNoDrawing = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_FRAME_AVAILABLE) {
                synchronized (RESClient.getInstance().getUsbDrawLock()) {
                    try {
                        drawFrame();
                        if (isDrawing && globalMediaCodec != null && globalMediaCodec.isStarted()) {
                            globalMediaCodec.drawFrame(mFullFrameBlit,mCamTextureId,mTmpMatrix);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    public CameraVideo(Context context) {
        super(context);
        init();
    }

    public CameraVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        getHolder().addCallback(this);
    }

    /**
     * Opens a camera, and attempts to establish preview mode at the specified width and height.
     * <p>
     * Sets mCameraPreviewFps to the expected frame rate (which might actually be variable).
     */
    private long lastCallBackTime;
    public void openCamera(int desiredWidth, int desiredHeight) {
        try {
            if (mCamera != null) {
                throw new RuntimeException("camera already initialized");
            }
//            File file = new File("/dev/video2");
//            if (!file.exists()) {
//                ToastR.setToastLong(MyApplication.getContext(), "camera无法打开");
//                Log.v(TAG, "/dev/video2 is not exist");
//                RESClient.getInstance().removeSurfaceView();
//                return;
//            }
            Camera.CameraInfo info = new Camera.CameraInfo();

            // Try to find a front-facing camera (e.g. for videoconferencing).
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Log.d(TAG, "openCamera opening camera :" + i);
                    mCamera = Camera.open(i);
                    break;
                }
            }
            if (mCamera == null) {
                Log.d(TAG, "No back-facing camera found; opening default");
                mCamera = Camera.open();    // opens first back-facing camera
            }
            if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
            }

            Camera.Parameters parms = mCamera.getParameters();
            mCamera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int i, Camera camera) {
//                    Intent intent = new Intent(ToastReceiver.TOAST_ACTION);
//                    intent.putExtra(ToastReceiver.TOAST_CONTENT, "Camera打开异常，正在重启");
//                    MyApplication.getContext().sendBroadcast(intent);
                    Log.e(TAG,"onError:" + i);
                    DvrService dvrService = RESClient.getInstance().getDvrService();
                    if(dvrService != null) {
                        dvrService.stopRecord();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.exit(0);
                }
            });
            CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight);
            parms.setRecordingHint(true);

            parms.setPictureFormat(ImageFormat.JPEG);
            parms.setPictureSize(desiredWidth, desiredHeight);

            mCamera.setParameters(parms);
            GlobalStatus.setCamera(mCamera);
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mCamera = GlobalStatus.getCamera();
        if(mCamera == null) {
            try {
                openCamera(RESConfig.VIDEO_WIDTH, RESConfig.VIDEO_HEIGHT);
            } catch (Throwable e){
                e.printStackTrace();
                mCamera = null;
            }
        }
        GlobalStatus.setIsUSBVideoShow(true);
        mEglCore = GlobalStatus.getEglCore();
        if(mEglCore == null) {
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            GlobalStatus.setEglCore(mEglCore);
        }
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        mFullFrameBlit = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mCamTextureId = mFullFrameBlit.createTextureObject();
        mCameraTexture = new SurfaceTexture(mCamTextureId);
        mCameraTexture.setOnFrameAvailableListener(this);

        Log.d(TAG, "starting camera preview");
        if(mCamera != null) {
            try {
                mCamera.setPreviewTexture(mCameraTexture);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            mCamera.startPreview();
        } else {
            ToastR.setToastLong(MyApplication.getContext(), "camera无法打开");
            RESClient.getInstance().removeSurfaceView();
        }
        globalMediaCodec = new GlobalMediaCodec();
        if (mEglCore != null) {
            globalMediaCodec.start(mEglCore);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    protected void onAttachedToWindow() {
        try {
            super.onAttachedToWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        final Object lock = new Object();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "release Runnable");
                synchronized (lock) {
                    if(GlobalStatus.getCamera() != null){
                        GlobalStatus.getCamera().stopPreview();
                        GlobalStatus.getCamera().release();
                        GlobalStatus.setCamera(null);
                    }
                    GlobalStatus.setIsUSBVideoShow(false);
                    if (mEglCore != null && !GlobalStatus.isDvrCamShow()) {
                        mEglCore.release();
                        mEglCore = null;
                        GlobalStatus.setEglCore(null);
                    }
                    Log.d(TAG, "globalMediaCodec.shutdown();");
                    if (globalMediaCodec != null) {
                        globalMediaCodec.shutdown();
                        globalMediaCodec = null;
                    }
                    Log.d(TAG, "globalMediaCodec.shutdown() end;");

                }
            }
        };
        RESClient.getInstance().runOnUiThread(r);
        Log.d(TAG, "release return");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        Log.d(TAG, "onFrameAvailable");
        if(getWidth() > 1) {
//            Log.d(TAG, "onFrameAvailable: getWidth() > 1");
            mHandler.sendEmptyMessage(MSG_FRAME_AVAILABLE);
        } else if(getWidth() == 1){
//            Log.d(TAG, "onFrameAvailable: getWidth() :" + getWidth());
            if (mEglCore == null) {
                Log.d(TAG, "Skipping drawFrame after shutdown");
                return;
            }
            mDisplaySurface.makeCurrent();
            mDisplaySurface.swapBuffers();
        } else {
            mCameraTexture.updateTexImage();
            mCameraTexture.getTransformMatrix(mTmpMatrix);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setNoDrawing(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setNoDrawing(false);
            }
        },200);
        if(mCameraTexture != null) {
            mCameraTexture.updateTexImage();
            mCameraTexture.getTransformMatrix(mTmpMatrix);
        }
    }

    public void drawFrame(){
        // Log.d(TAG, "drawFrame");
        if (mEglCore == null) {
            Log.d(TAG, "Skipping drawFrame after shutdown");
            return;
        }

        mDisplaySurface.makeCurrent();
        mCameraTexture.updateTexImage();
        mCameraTexture.getTransformMatrix(mTmpMatrix);

        if(!curNoDrawing) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            mFullFrameBlit.drawFrame(mCamTextureId, mTmpMatrix);
        }
        mDisplaySurface.swapBuffers();
    }

    public void setIsDrawing(boolean isDrawing){
        Log.v(TAG,"setIsDrawing:" + isDrawing);
        if(!isDrawing && mCameraTexture != null){
            mCameraTexture.updateTexImage();
            mCameraTexture.getTransformMatrix(mTmpMatrix);
        }
        this.isDrawing = isDrawing;
        this.curNoDrawing = false;
    }

    public void setNoDrawing(boolean isDrawing){
        Log.v(TAG,"setNoDrawing:" + isDrawing);
        this.curNoDrawing = isDrawing;
    }
}
