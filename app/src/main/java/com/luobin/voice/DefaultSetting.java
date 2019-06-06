package com.luobin.voice;

/**
 * Created by quhuabo on 2016/10/21 0021.
 */
public class DefaultSetting {

    /**
     * 缓冲时间
     */
    public static final int CACHE_BUFFER_SIZE_FIRST_TIME = 27 * 10;       //

    public static String mime = "audio/3gpp";//MediaFormat.MIMETYPE_AUDIO_AMR_NB;
    //    public static String mime = "audio/mp4a-latm";
    public static int channelCnt = 1;

    public static final int CACHE_BUFFER_SIZE = 60 * 10;                  // 暂时 10 最优值

    public static int sampleRate = 8000;
    public static int bitrate = 8000;
    //public static final int AUDIO_TRACK_PLAY_BUFF = PcmRecorder.frequency;
}
