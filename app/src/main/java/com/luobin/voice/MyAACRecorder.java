package com.luobin.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.google.protobuf.ByteString;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.log.DBMyLogHelper;
import com.luobin.log.LogCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import me.lake.librestreaming.client.RESClient;

import static com.example.jrd48.chat.crash.MyApplication.getContext;
import static com.example.jrd48.service.protocol.root.ReceiverProcesser.UPDATE_KEY;

/**
 * Created by qhb on 17-9-18.
 */

public class MyAACRecorder implements Runnable {
    private int sampleRate = DefaultSetting.sampleRate;   //采样率，默认44.1k
    private int bitRate = DefaultSetting.bitrate; // 码流
    private int channelCount = DefaultSetting.channelCnt;     //音频采样通道，默认2通道
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;        //通道设置，默认立体声
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;     //设置采样数据格式，默认16比特PCM

    private MediaCodec mEnc;
    private final String mime = DefaultSetting.mime;    //录音编码的mime
    private AudioRecord mRecorder;
    private int minBufferSize = 0;
    private String TAG = "MyAACRecorder";
    private volatile boolean isRecording = false;


    byte mBufferCache[] = new byte[14336];

    int mBufferOffset = 0;

    private boolean mFirstSend = true;
    private Context mContext;

//    private AcousticEchoCanceler canceler;
//    private NoiseSuppressor suppressor;
    private AudioManager mAudioManager;


    private volatile boolean mStopFlag = false;

    private volatile boolean runnableThread = true;
    private volatile Object lock = new Object();
    public MyAACRecorder() {

    }

    public void setStopFlag(boolean bStopFlag) {
        this.mStopFlag = bStopFlag;
        synchronized (lock) {
            if (!bStopFlag) {
                lock.notifyAll();
            }
        }
    }


    @Override
    public void run() {
        runnableThread = true;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        while (runnableThread) {
            try {
                synchronized (lock) {
                    if (mStopFlag) {
                        Log.w(TAG, "wait");
                        lock.wait();
                    }
                }
                mAudioManager.setSpeakerphoneOn(true);
                Log.w(TAG, "notify success");
                mFirstSend = true;
                mBufferOffset = 0;
                isRecording = true;
                Log.w(TAG, "录音开始");
                DBMyLogHelper.insertLog(mContext, LogCode.RECORD, "录音开始", null);
                if (true) {
//音频录制实例化和录制过程中需要用到的数据
                    minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2;
                    Log.i(TAG, "min buffer size: " + minBufferSize);
                    //byte[] buffer = new byte[minBufferSize];

//实例化AudioRecord
                    SettingRW config = new SettingRW(mContext);
                    config.load();

                    mRecorder = new AudioRecord(/*config.getMicWay() == 0 ?
                            MediaRecorder.AudioSource.VOICE_COMMUNICATION :*/ MediaRecorder.AudioSource.MIC
                            , sampleRate, channelConfig,
                            audioFormat, minBufferSize);
                    int audioRecordState = mRecorder.getState();
                    if (audioRecordState != AudioRecord.STATE_INITIALIZED) {
                        Log.e("mychat", "可能没有录音权限");
                        DvrService.start(mContext, RESClient.ACTION_VOICE_RECORD, null);
                        AudioInitailFailedBroadcast mb = new AudioInitailFailedBroadcast(mContext);
                        mb.sendBroadcast("");
//                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        return;
                    }
//
//                // 启用回声抑制
//                if (Build.VERSION.SDK_INT >= 16 && AcousticEchoCanceler.isAvailable()) {
//                    Log.i("mychat", "AcousticEchoCanceler enable");
//                    canceler = AcousticEchoCanceler.create(mRecorder.getAudioSessionId());
//                    if (canceler != null) {
//                        canceler.setEnabled(true);
//                    } else {
//                        Log.w("mychat", "可能没有录音权限...");
//                    }
//                } else {
//                    Log.w("mychat", "AcousticEchoCanceler DISABLE");
//                }
//
//                // 启用噪音压制功能
//                if (NoiseSuppressor.isAvailable()) {
//                    suppressor = NoiseSuppressor.create(mRecorder.getAudioSessionId());
//                    Log.i("mychat", "NoiseSuppressor enable");
//                    if (suppressor != null) {
//                        suppressor.setEnabled(true);
//                    } else {
//                        Log.w("mychat", "可能没有录音权限...");
//                    }
//                } else {
//                    Log.w("mychat", "NoiseSuppressor DISABLE");
//                }

                    //SoundPoolTool.getInstance(context).play_voice(null);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MediaPlayerTool.getInstance().play(mContext, R.raw.notify_ptt2);
                        }
                    });

                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//开始录制
                    mRecorder.startRecording();
                    new AudioRecordStatusBroadcast(mContext).sendBroadcast("1");

                }

//相对于上面的音频录制，我们需要一个编码器的实例

                MediaFormat format = MediaFormat.createAudioFormat(mime, sampleRate, channelCount);

                format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
                format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                        MediaCodecInfo.CodecProfileLevel.AACObjectLC);

                mEnc = MediaCodec.createEncoderByType(mime);
                mEnc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);  //设置为编码器

//同样，在设置录音开始的时候，也要设置编码开始
                mEnc.start();
                ByteBuffer inputBuffers[] = mEnc.getInputBuffers();
                ByteBuffer outputBuffers[] = mEnc.getOutputBuffers();

                for (; !mStopFlag; Thread.yield()) {
//                    Log.i(TAG, "thread running ...");
//之前的音频录制是直接循环读取，然后写入文件，这里需要做编码处理再写入文件
//这里的处理就是和之前传送带取盒子放原料的流程一样了，注意一般在子线程中循环处理
                    int index = mEnc.dequeueInputBuffer(10 * 1000);
                    if (index >= 0) {
                        ByteBuffer buffer = inputBuffers[index];
                        buffer.clear();
                        int length = mRecorder.read(buffer, minBufferSize);
//                    if (length > 0) {
//                        mEnc.queueInputBuffer(index, 0, length, System.nanoTime() / 1000, 0);
//                    }
//                        Log.i(TAG, "**************** 1:" + length);
                        mEnc.queueInputBuffer(index, 0, length, System.nanoTime() / 1000, 0);
//                        Log.i(TAG, "**************** 2");
                    }

                    // 取编码后的数据
                    MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
                    int outIndex;
//每次取出的时候，把所有加工好的都循环取出来
                    do {
                        outIndex = mEnc.dequeueOutputBuffer(mInfo, 10 * 1000);
                        if (outIndex >= 0) {
                            ByteBuffer buffer;

                            if (Build.VERSION.SDK_INT >= 21) {
                                buffer = mEnc.getOutputBuffer(outIndex);
                            } else {
                                buffer = outputBuffers[outIndex];
                            }

                            buffer.position(mInfo.offset);
                            buffer.limit(mInfo.offset + mInfo.size);

//                    //AAC编码，需要加数据头，AAC编码数据头固定为7个字节
//                    byte[] temp = new byte[mInfo.size + 7];
//                    buffer.get(temp, 7, mInfo.size);
//                    addADTStoPacket(temp, temp.length);
//                    fos.write(temp);

                            Log.d(TAG,"mInfo.size =" + mInfo.size);
                            if (mInfo.size != 2) {
                                buffer.get(mBufferCache, mBufferOffset, mInfo.size);
                                mBufferOffset += mInfo.size;

                                if(mBufferOffset >= DefaultSetting.CACHE_BUFFER_SIZE_FIRST_TIME){
                                    if ((Boolean) SharedPreferencesUtils.get(mContext, "pttKeyDown", false)) {
                                       Log.d(TAG,"send_cached_buffer_to_socket");
                                        send_cached_buffer_to_socket();
                                    }else{
                                        Log.d(TAG,"not send_cached_buffer_to_socket");
                                    }
                                }else{
                                    Log.d(TAG,"mBufferOffset < CACHE_BUFFER_SIZE_FIRST_TIME");
                                }
//                                if (mFirstSend) {
//                                    if (mBufferOffset > DefaultSetting.CACHE_BUFFER_SIZE_FIRST_TIME) {
//                                        send_cached_buffer_to_socket();
//                                        mFirstSend = false;
//                                    }
//                                } else {
//                                    send_cached_buffer_to_socket();
//                                }
                            }

                            buffer.clear();
                            mEnc.releaseOutputBuffer(outIndex, false);

                        } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            //TODO something
//                            Log.i(TAG, "try again");
                            outputBuffers = mEnc.getOutputBuffers();
                            break;
                        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            //TODO something
                            Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED???");
                            //MediaFormat format = codec.getOutputFormat();
                        }

                    } while (outIndex >= 0 && !mStopFlag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.w(TAG, "---------------- thread loop end.");

//        try {
//            if (canceler != null) {
//                canceler.setEnabled(false);
//                canceler.release();
//            }
//
//            if (suppressor != null) {
//                suppressor.setEnabled(false);
//                suppressor.release();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

            if (mAudioManager.isBluetoothScoOn()) {
                mAudioManager.stopBluetoothSco();
            }


//编码停止，发送编码结束的标志，循环结束后，停止并释放编码器
            try {
                if (mEnc != null) {
                    mEnc.stop();
                    mEnc.release();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (mRecorder != null) {
                    mRecorder.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mBufferOffset = 0;
            //send_cached_buffer_to_socket();
            //中止循环并结束录制
            isRecording = false;
            Log.w(TAG, "录音结束");
//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            MediaPlayerTool.getInstance().play(mContext, R.raw.notify_ptt2);
            new AudioRecordStatusBroadcast(mContext).sendBroadcast("0");
        }
    }


    private File createFile(String name) {

        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/AudioRecord/";
        File file = new File(dirPath);

        if (!file.exists()) {
            file.mkdirs();
        }

        String filePath = dirPath + name;
        File objFile = new File(filePath);
        if (!objFile.exists()) {
            try {
                objFile.createNewFile();
                return objFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;


    }


    private int count = 0;
    private long lastClickTime = 0;
    private void send_cached_buffer_to_socket() {
        count++;
        long time = SystemClock.elapsedRealtime() - lastClickTime;
        if(time > 1000){
            lastClickTime = SystemClock.elapsedRealtime();
//            Log.i(TAG, "发送语音数据包: " + count);
            count = 0;
        }

        String tmpName = System.currentTimeMillis() + "_"  + "aaa";
        final File tmpFile = createFile(tmpName + ".aac");

        try {
            //byte[] bb = Arrays.copyOf(mBufferCache.array(), mBufferCache.position());
            FileOutputStream outputStream = new FileOutputStream(tmpFile.getAbsoluteFile());
            if (mBufferOffset > 0) {
                outputStream.write(mBufferOffset);
                ProtoMessage.SpeakMsg.Builder builder = ProtoMessage.SpeakMsg.newBuilder();
                builder.setAudioData(ByteString.copyFrom(mBufferCache, 0, mBufferOffset));
                ProtoMessage.SpeakMsg msg = builder.build();
                MyService.start(mContext, ProtoMessage.Cmd.cmdSpeakMsg_VALUE, msg);
//                Log.i(TAG, "发送语音数据字节数: " + mBufferOffset);
                mBufferOffset = 0;
            }else{
                Log.d(TAG,"mBufferOffset = 0");
            }

        } catch (Exception e) {
            Log.e(TAG,"Exception = " + e.toString());
            e.printStackTrace();
        }
    }

    public synchronized void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    public synchronized void release() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public boolean isRunning() {
        return isRecording;
    }


    public boolean isRunnableThread() {
        return runnableThread;
    }

    public void setRunnableThread(boolean runnableThread) {
        this.runnableThread = runnableThread;
    }
}
