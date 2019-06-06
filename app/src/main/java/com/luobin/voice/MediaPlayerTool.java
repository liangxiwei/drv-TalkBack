package com.luobin.voice;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * Created by qhb on 17-4-14.
 */

public class MediaPlayerTool {
    private static MediaPlayerTool _instance = null;

    private MediaPlayer mediaPlayer;

    private MediaPlayerTool() {
        mediaPlayer = new MediaPlayer();
    }

    public static synchronized MediaPlayerTool getInstance() {
        if (_instance == null) {
            _instance = new MediaPlayerTool();
        }

        return _instance;
    }

    public void play(Context context, int resID) {
        try {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer = null;
            }

            mediaPlayer = MediaPlayer.create(context, resID);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
