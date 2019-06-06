package com.luobin.voice;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.example.jrd48.service.HexTools;
import com.luobin.voice.io.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by qhb on 17-9-18.
 */

public class MyAACDecoder implements Runnable {

    private static final String TAG = "MyAACDecoder";
    private MediaCodec mediaDecoder;
    private BlockingDeque<byte[]> rawdataList = new LinkedBlockingDeque<>();
    private MediaFormat mediaFormat;
    protected volatile boolean threadRuning = false;


    private volatile boolean mStopFlag = false;

    private Consumer mConsumer;

    public MyAACDecoder(NetPlayer netPlayer) {
        mConsumer = netPlayer;
    }

    public void setConsumer(Consumer aConsumer) {
        this.mConsumer = aConsumer;
    }

    public void setStopFlag(boolean AStopFlag) {
        this.mStopFlag = AStopFlag;
    }


    @Override
    public void run() {
        try {
            threadRuning = true;
            mediaDecoder = MediaCodec.createDecoderByType(DefaultSetting.mime);
            mediaFormat = MediaFormat.createAudioFormat(DefaultSetting.mime, DefaultSetting.sampleRate, DefaultSetting.channelCnt);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DefaultSetting.bitrate);
//            mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 0);
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC);

//            ByteBuffer csd = ByteBuffer.allocate(2);
//            csd.put(0, (byte) 20); //16k
//            csd.put(1, (byte) 8); //16k
//            csd.put(0, (byte) 21); //8k
//            csd.put(1, (byte) 136); //8k
//            csd.put(0, (byte) 19); //22.05k
//            csd.put(1, (byte) 136); //22.05k
//            csd.put(0, (byte) 18); //44.1k
//            csd.put(1, (byte) 8); //44.1k
//            Log.v(TAG, "csd-0 value: " + HexTools.byteArrayToHex(csd.array()));
//            mediaFormat.setByteBuffer("csd-0", csd);
            mediaDecoder.configure(mediaFormat, null, null, 0);
            mediaDecoder.start();//启动MediaCodec ，等待传入数据
            rawdataList.clear();

            ByteBuffer[] decInputBuffers = mediaDecoder.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaDecoder.getOutputBuffers();
            for (; !mStopFlag; Thread.yield()) {
                byte[] buffer;
                try {
                    buffer = rawdataList.poll(20, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    Log.w(TAG, "poll interrupt here.");
                    break;
                }

                if (buffer != null) {

                    int bufferIndex = mediaDecoder.dequeueInputBuffer(10 * 1000);
                    if (bufferIndex >= 0) {
                        decInputBuffers[bufferIndex].clear();
                        decInputBuffers[bufferIndex].put(buffer);
                        long naotime = System.nanoTime() / 1000;
                        mediaDecoder.queueInputBuffer(bufferIndex, 0, buffer.length, naotime, 0);
                    }
                }


                MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
                int outIndex;
//每次取出的时候，把所有加工好的都循环取出来
                do {
                    outIndex = mediaDecoder.dequeueOutputBuffer(mInfo, 10 * 1000);
                    if (outIndex >= 0) {
                        ByteBuffer buffOut;

                        if (Build.VERSION.SDK_INT >= 21) {
                            buffOut = mediaDecoder.getOutputBuffer(outIndex);
                        } else {
                            buffOut = outputBuffers[outIndex];
                        }

                        buffOut.position(mInfo.offset);
                        buffOut.limit(mInfo.offset + mInfo.size);

//                    //AAC编码，需要加数据头，AAC编码数据头固定为7个字节
//                    byte[] temp = new byte[mInfo.size + 7];
//                    buffer.get(temp, 7, mInfo.size);
//                    addADTStoPacket(temp, temp.length);
//                    fos.write(temp);

                        if (mInfo.size > 0) {
                            byte temp[] = new byte[mInfo.size];
                            buffOut.get(temp);
                            mConsumer.putData(0, temp, temp.length);
                        }

                        buffOut.clear();
                        mediaDecoder.releaseOutputBuffer(outIndex, false);

                    } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        //TODO something
                        //Log.i(TAG, "[decoder]try again");
                        outputBuffers = mediaDecoder.getOutputBuffers();
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

        try {
            mediaDecoder.stop();
        } catch (Exception e){
            e.printStackTrace();
        }


        threadRuning = false;

    }


    public void pushRaw(final byte[] buffer, final int size) {
        rawdataList.offerLast(ArrayUtils.subarray(buffer, 0, size));
//        if (mediaDecoder == null) {
//            start(buffer);
//        } else if(buffer.length > 2){
//
//        } else if (buffer.length == 2){
//            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(buffer));
//            mediaDecoder.configure(mediaFormat,null,null,0);
//        }
    }
}
