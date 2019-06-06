package com.luobin.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.service.MyLogger;
import com.luobin.log.DBMyLogHelper;
import com.luobin.log.LogCode;
import com.luobin.voice.io.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/10/20 0020.
 */

public class NetPlayer implements Consumer, Runnable {
    private String TAG = "NetPlayer";
    private final Object mutex = new Object();
    private MyLogger mLog = MyLogger.jLog();
    private volatile boolean isRunning = false;

    //private File mFile;
    private Context context;
    private AudioTrack audioTrack;
//    private SpeexDecoder decoder;
    //private DecoderAAC decoder;
    private Thread mDecoderThread;
    private MyAACDecoder mediaDecoder;
    private BlockingDeque<byte[]> decodeList = new LinkedBlockingDeque<>();

//    private Speex speex = new Speex();
    private AudioManager am;
    private SettingRW mSettings;

    private Object mLocker = new Object();
    private volatile Object threadLock = new Object();
    private volatile boolean runnableThread = true;
    private Equalizer equalizer;

    public NetPlayer(Context context) {
        super();
//        speex.init();
        setContext(context);
        mSettings = new SettingRW(context);

        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        decoder = new SpeexDecoder(this);
        mediaDecoder = new MyAACDecoder(this);

    }

    public void setContext(Context context) {
        this.context = context;
    }

    private void startAudioTrack() {
        mLog.i("player begin.");
        mSettings.load();
        am.setSpeakerphoneOn(true);
//        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        am.setSpeakerphoneOn(true);
//        if (BluetoothMonitor.isBlueToothHeadsetConnected()) {
//            mLog.i("语音播放：蓝牙");
//            am.setBluetoothScoOn(true);
//            am.startBluetoothSco();
//        } else {
//
//            if (mSettings.isHandFree()) {
//                mLog.i("免提开");
//                am.setSpeakerphoneOn(true);
//            } else {
//                mLog.i("未设置免提");
//                //am.setSpeakerphoneOn(false);
//            }
//        }

//        if (mSettings.isAutoMaxVolume()) {
//            mLog.i("设置声音最大");
//            am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
//                    , 0 /*AudioManager.FLAG_SHOW_UI, AudioManager.FLAG_PLAY_SOUND*/);
//        } else {
//            mLog.i("未设置声音最大");
//        }

        int bufferSizeInBytes = AudioTrack.getMinBufferSize(DefaultSetting.sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, DefaultSetting.sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                2 * bufferSizeInBytes, AudioTrack.MODE_STREAM);

//        if (Build.VERSION.SDK_INT >= 21) {
//            audioTrack.setVolume(1);
//        }
        try {
            setEqualizer();
        } catch (Exception e){
            e.printStackTrace();
        }

        audioTrack.play();
    }

    public boolean isRunning() {
        synchronized (mutex) {
            return isRunning;
        }
    }

    public void setRunning(boolean isRunning) {
        synchronized (mutex) {
            this.isRunning = isRunning;
            if (this.isRunning) {
                mutex.notify();
            }
        }
        Log.w(TAG, "runnableThread:" + runnableThread);
        Log.w(TAG, "setRunning:" + isRunning);
        synchronized (threadLock) {
            if (isRunning) {
                Log.w(TAG, "notifyAll");
                threadLock.notifyAll();
            }
        }
    }

    /**
     * Decoder返回来的数据
     */
    @Override
    public void putData(long ts, short[] buf, int size) {
//        decodeList.offerLast(ArrayUtils.subarray(buf, 0, size));
    }

    @Override
    public void putData(long ts, byte[] buf, int size) {
        //
        //decodeList.offerLast(ArrayUtils.subarray(buf, 0, size));
        decodeList.offerLast(ArrayUtils.subarray(buf, 0, size));
    }

    /**
     * 由网络线程发送数据到 NetPlayer,  此处不做处理，直接转发到 Decoder
     */
    public synchronized void pushRawData(byte[] buf, int nOffset, int size) {

//        //decoder.putData(0, buf, nOffset, size);
//        int getSize = speex.decode(buf, processedData, size);
//        Log.i("mychat", "decode size: {"+nOffset+"} -> {"+size+"}");
//        putData(0, processedData, getSize);
//        decoder.putData(0, ArrayUtils.subarray(buf, nOffset, nOffset + size), size);
        mediaDecoder.pushRaw(ArrayUtils.subarray(buf, nOffset, nOffset + size), size);
    }

    @Override
    public void run() {

        while (isRunnableThread()) {
//        Thread decoderThread = new Thread(decoder);
            try {
                android.os.Process
                        .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                synchronized (threadLock) {
                    if (!isRunning()) {
                        Log.w(TAG, "wait");
                        threadLock.wait();
                    }
                }
                Log.w(TAG, "notify success--decodeList.size=" + decodeList.size());
                // clear decodeList avoid playing last audio
                decodeList.clear();
                // 开始播放语音
                startAudioTrack();
                DBMyLogHelper.insertLog(context, LogCode.PLAU, "NetPlayer startAudioTrack", null);

                // 启动解码器
                mediaDecoder.setStopFlag(false);
                //decoderThread.start();
                mDecoderThread = new Thread(mediaDecoder);
                mDecoderThread.start();
                int len = 0;
                for (; this.isRunning(); Thread.yield()) {
                    byte[] b;
                    try {
                        b = decodeList.poll(50, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        Log.w("mychat", "net player thread poll interrupted.");
                        break;
                    }
                    if (b == null) {
//                    Log.w("mychat", "audioTrack data == null");
                        continue;
                    }
                    //synchronized (mLocker) {
                    if (isRunning) {
                        //Log.i("mychat", "audiotrack write: {" + b.length + "}");
                        int totalLen = b.length;
                        int offset = 0;
                        do {
                            len = audioTrack.write(b, offset, totalLen - offset);
                            if (len < 0) {
                                mLog.w("写入播放器数据时出错.");
                                break;
                            } else {
                                offset += len;
                            }

                        }while(offset<totalLen);
                    }
                    //}

                    //Log.i("mychat", "decode size: {"+b.length+"} -> {"+getSize+"}");
                    //consumer.putData(ts, processedData, getSize);

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mediaDecoder.setStopFlag(true);
                    try{
                        mDecoderThread.interrupt();
                    }catch (Exception e){

                    }
                    mDecoderThread.join(2000);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                try {
                    mDecoderThread.interrupt();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                closeAudioTrack();
                setRunning(false);
                mLog.i("restart player here.");

            }
        }
    }

    private void closeAudioTrack() {
        try {
            synchronized (mLocker) {
                mLog.i("close audio track here.");
                audioTrack.release();
                am.stopBluetoothSco();
            }

            if (equalizer != null) {

                try {
                    equalizer.setEnabled(false);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    try {
                        equalizer.release();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                equalizer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mLog.w("send restart net player cmd.");
        setRunning(false);
    }

    public boolean isRunnableThread() {
        return runnableThread;
    }

    public void setRunnableThread(boolean runnableThread) {
        this.runnableThread = runnableThread;
    }


    public void setEqualizer() {
        equalizer = new Equalizer(0, audioTrack.getAudioSessionId());
        equalizer.setEnabled(true);
        short bands = equalizer.getNumberOfBands();

        final short minEqualizer = equalizer.getBandLevelRange()[0];
        final short maxEqualizer = equalizer.getBandLevelRange()[1];
        Log.v(TAG, "minEqualizer:" + minEqualizer / 100 + "dp");
        Log.v(TAG, "maxEqualizer:" + maxEqualizer / 100 + "dp");
        for (short i = 0; i < bands; i++) {
            int hz = equalizer.getCenterFreq(i) / 1000;
            Log.v(TAG, "HZ:" + hz + "HZ");
            if(hz < 150){
                equalizer.setBandLevel(i,(short) 400);
            } else if(hz < 600){
                equalizer.setBandLevel(i,(short) 600);
            } else if(hz < 1400){
                equalizer.setBandLevel(i,(short) 0);
            } else if(hz < 8000){
                equalizer.setBandLevel(i,(short) 0);
            } else {
                equalizer.setBandLevel(i,(short) 0);
            }
        }

    }
}
