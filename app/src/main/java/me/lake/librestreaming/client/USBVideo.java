package me.lake.librestreaming.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.receiver.ToastReceiver;
import com.luobin.dvr.R;
import com.luobin.dvr.grafika.gles.EglCore;
import com.luobin.dvr.grafika.gles.FullFrameRect;
import com.luobin.dvr.grafika.gles.Texture2dProgram;
import com.luobin.dvr.grafika.gles.WindowSurface;
import com.luobin.musbcam.UsbCamera;

import java.io.File;

import me.lake.librestreaming.model.RESConfig;

/**
 * Created by Administrator on 2017/9/9.
 */

public class USBVideo extends VideoBase implements SurfaceHolder.Callback, UsbCamera.UsbCameraListener {
    private static final String TAG = "USBVideo";
    private UsbCamera usbCamera;
    private static final int MSG_FRAME_AVAILABLE = 1;
    private int mCamTextureId;
    private int mUsbTextureId = -1;
    private EglCore mEglCore;
    private WindowSurface mDisplaySurface;
    private FullFrameRect mFullFrameBlit;
    private FullFrameRect mUsbFrameBlit;
    private SurfaceTexture mCameraTexture;
    private final float[] mTmpMatrix = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f};
    private GlobalMediaCodec globalMediaCodec;
    private boolean isDrawing = false;
    private boolean curNoDrawing = false;
    private long mFrameCount = 0;
    private int mCameraPreviewThousandFps;
    private int mCamPrevWidth = 0;
    private int mCamPrevHeight = 0;
    protected BitmapFactory.Options options;
    private int oldWidth = 0;
    private boolean isOpen;
    private Object mUsbLock = new Object();
    protected Bitmap mUsbBmp = null;
    private boolean usbThreadOn = true;
    private DecoderThread mDecoderThread;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_FRAME_AVAILABLE) {
                drawFrameUsb((Bitmap) msg.obj);
                synchronized (RESClient.getInstance().getUsbDrawLock()) {
                    try {
                        drawFrame();
                        if (isDrawing && globalMediaCodec != null && globalMediaCodec.isStarted()) {
                            globalMediaCodec.drawFrame(mUsbFrameBlit,mUsbTextureId,mTmpMatrix);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    public USBVideo(Context context) {
        super(context);
        init();
    }

    public USBVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public USBVideo(Context context, AttributeSet attrs, int defStyleAttr) {
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
    private void openCamera(int desiredWidth, int desiredHeight, int desiredFps) {

        if (usbCamera != null) {
            Log.v(TAG,"camera already initialized");
        }
        String dev = getResources().getStringArray(R.array.video_devs)[3];
        String product = Build.PRODUCT;
        if (product != null && product.equals("LB1728V4")) {
            dev = getResources().getStringArray(R.array.video_devs)[3];
        }
        File file = new File(dev);
        if(!file.exists()){
            ToastR.setToast(MyApplication.getContext(), dev + MyApplication.getContext().getString(R.string.camera_node_not_exist));
            Log.v(TAG,dev+" is not exist");
            return;
        }

        if(GlobalStatus.getUsbVideo1() != null){
            usbCamera = GlobalStatus.getUsbVideo1();
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
                GlobalStatus.setUsbVideo1(null);
            } else {
                GlobalStatus.setUsbVideo1(usbCamera);
                usbCamera.start(this);
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mCamPrevWidth = RESConfig.VIDEO_WIDTH;
        mCamPrevHeight = RESConfig.VIDEO_HEIGHT2;///640x480
        usbCamera = GlobalStatus.getUsbVideo1();
        Log.d(TAG, "usbCamera == null ,"+(usbCamera == null));
        try {
            openCamera(mCamPrevWidth, mCamPrevHeight, RESConfig.FPS);
        } catch (Throwable e){
            e.printStackTrace();
            usbCamera = null;
        }

        usbThreadOn = true;
        if (mDecoderThread == null) {
            mDecoderThread = new DecoderThread();
        }
        if (!mDecoderThread.isAlive()) {
            mDecoderThread.start();
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
        mUsbFrameBlit = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D));
        mCamTextureId = mFullFrameBlit.createTextureObject();
        mCameraTexture = new SurfaceTexture(mCamTextureId);
        //mCameraTexture.setOnFrameAvailableListener(this);

        globalMediaCodec = new GlobalMediaCodec();
        if (mEglCore != null) {
            globalMediaCodec.start(mEglCore);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged width="+width);
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
        Log.d(TAG, "release mDvrEncoder != null");
        if (usbCamera != null && isOpen) {
            usbCamera.stop();
        }
        usbCamera = null;
        if (mFullFrameBlit != null) {
            mFullFrameBlit.release(true);
        }

        if (mUsbFrameBlit != null) {
            mUsbFrameBlit.release(true);
        }
        if (mUsbTextureId >= 0) {
            GLES20.glDeleteTextures(1,
                    new int[]{mUsbTextureId}, 0);
            checkGlError("glDeleteTextures");
            mUsbTextureId = -1;
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
                    GlobalStatus.setUsbVideo1(null);
                    usbThreadOn = false;
                    if (mDecoderThread != null) {
                        try {
                            mDecoderThread.join(1000);
                            mDecoderThread.interrupt();
                            mDecoderThread = null;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "mDecoderThread end;");
                }
            }
        };
        RESClient.getInstance().runOnUiThread(r);
        Log.d(TAG, "release return");
    }

   /* @Override
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
    }*/

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.v(TAG,"onSizeChanged: w="+w + ",h="+h+ ",oldw="+oldw+",oldh="+oldh);
        setNoDrawing(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setNoDrawing(false);
            }
        },200);
        if(mCameraTexture != null) {
            //mCameraTexture.updateTexImage();
            //mCameraTexture.getTransformMatrix(mTmpMatrix);
        }
    }

    public void drawFrame(){
        //Log.d(TAG, "drawFrame");
        if (mEglCore == null) {
            Log.d(TAG, "Skipping drawFrame after shutdown");
            return;
        }

        mDisplaySurface.makeCurrent();
        mCameraTexture.updateTexImage();
        //mCameraTexture.getTransformMatrix(mTmpMatrix);
        //Log.d(TAG, "curNoDrawing="+curNoDrawing);
        if(!curNoDrawing) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            if(viewWidth <= 0){
                viewWidth = RESConfig.VIDEO_WIDTH;
                viewHeight = RESConfig.VIDEO_HEIGHT;
            }
            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            mFullFrameBlit.drawFrame(mCamTextureId, mTmpMatrix);
            if(oldWidth == 0 || oldWidth != viewWidth){
                Log.v(TAG,"drawFrame viewWidth=" + viewWidth+",viewHeight="+viewHeight);
                oldWidth = viewWidth;
            }
            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            /*for (int i=0; i<mTmpMatrix.length; i+=4) {
                Log.d(TAG, "mTmpMatrix = " + mTmpMatrix[i] + " " + mTmpMatrix[i+1] + " " + mTmpMatrix[i+2] + " " + mTmpMatrix[i+3]);
            }*/
            mUsbFrameBlit.drawFrame(mUsbTextureId, mTmpMatrix);
        }
        mDisplaySurface.swapBuffers();
    }

    public void setIsDrawing(boolean isDrawing){
        Log.v(TAG,"setIsDrawing:" + isDrawing);
        if(!isDrawing && mCameraTexture != null){
            //mCameraTexture.updateTexImage();
            //mCameraTexture.getTransformMatrix(mTmpMatrix);
        }
        this.isDrawing = isDrawing;
        this.curNoDrawing = false;
    }

    public void setNoDrawing(boolean isDrawing){
        Log.v(TAG,"setNoDrawing:" + isDrawing);
        this.curNoDrawing = isDrawing;
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
                if (mUsbBmp != null) {
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


    private void drawFrameUsb(Bitmap mUsbBmp) {
        try {
            if (mUsbBmp != null) {
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
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mUsbBmp, 0);
                mUsbBmp = null;
            } else {
                Log.e(TAG, "bmp == null");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        //Log.d(TAG, "onJpegFrame");
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
        }
        Bitmap tempBmp = null;
        try {
            tempBmp = BitmapFactory.decodeByteArray(framebuf, 0, framebuf.length,options);
            if(mCamPrevWidth != RESConfig.VIDEO_WIDTH || mCamPrevHeight != RESConfig.VIDEO_HEIGHT){
                tempBmp = Bitmap.createBitmap(BitmapFactory.decodeByteArray(framebuf, 0, framebuf.length,options), 0,0, RESConfig.VIDEO_WIDTH, RESConfig.VIDEO_HEIGHT);
                //Log.v(TAG,"tempBmp H = "+tempBmp.getHeight());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        synchronized (mUsbLock) {
            mUsbBmp = tempBmp;
            mUsbLock.notifyAll();
        }
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
