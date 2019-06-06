package com.luobin.voice;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.luobin.dvr.R;
import com.example.jrd48.service.BluetoothMonitor;

import java.io.IOException;

/**
 * Created by Administrator on 2017/3/13.
 */

public class SoundPoolTool {
    public interface PlayCompleteListener {
        void onComplete();
    }

    private static SoundPoolTool mInstance = null;
    private Context context;
    private MediaPlayer mediaPlayer;
    public synchronized static SoundPoolTool getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SoundPoolTool(context);
        }
        return mInstance;
    }

    private SoundPoolTool(Context context) {
        this.context = context;
        initVoice(null);
    }

    public synchronized void initVoice(final Handler handler) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            Uri notificationUri = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION);
            try {
                Log.e("SoundPooTool", "setDataSource");
                mediaPlayer.setDataSource(context, notificationUri);
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer = null;
                if(handler != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initVoice(handler);
                        }
                    }, 2000);
                }
            }
        }
    }


    public synchronized void play_voice(final PlayCompleteListener func) {
        if (mediaPlayer == null) {
            Log.e("SoundPooTool", "media player 没有初始化");
            new Throwable().printStackTrace();
            if(func != null) {
                func.onComplete();
            }
            return;
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (BluetoothMonitor.isBlueToothHeadsetConnected()) {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    am.stopBluetoothSco();
                }
                if (func != null) {
                    func.onComplete();
                }
            }
        });

        if (BluetoothMonitor.isBlueToothHeadsetConnected()) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Log.i("SoundPoolToo", "语音播放：蓝牙");
            am.setBluetoothScoOn(true);
            am.startBluetoothSco();
        }
        mediaPlayer.start();

    }

    private boolean isPlaying = false;
    public synchronized void play_voice() {
        if(isPlaying){
            Log.e("SoundPooTool", "media player 正在播放");
            return;
        }
        if (mediaPlayer == null) {
            Log.e("SoundPooTool", "media player 没有初始化");
            initVoice(null);
            if(mediaPlayer == null){
                new Throwable().printStackTrace();
                return;
            }
        }
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /*if (BluetoothMonitor.isBlueToothHeadsetConnected()) {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    am.stopBluetoothSco();
                }*/
                isPlaying = false;
            }
        });

        /*if (BluetoothMonitor.isBlueToothHeadsetConnected()) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Log.i("SoundPoolToo", "语音播放：蓝牙");
            am.setBluetoothScoOn(true);
            am.startBluetoothSco();
        }*/
        mediaPlayer.start();

    }
}
