package com.example.jrd48.service.protocol.root;

import android.content.Context;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.MyLogger;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by qhb on 17-1-11.
 */

public class GotSpeakMsgProcesser extends CommonProcesser {
    MyLogger mLog = MyLogger.jLog();
    public final static String ACTION = "ACTION.GotSpeakMsgProcesser";
    public int count = 0;
    public GotSpeakMsgProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        if(GlobalStatus.getChatRoomMsg() == null){
            return;
        }
        try {
            MyService myService = (MyService) context;
            ProtoMessage.SpeakMsg msg = ProtoMessage.SpeakMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            byte[] audioData = msg.getAudioData().toByteArray();
            if(count > 10) {
                count=0;
                mLog.i("获得语音数据： " + (data.length - 4));
//                mLog.i("got audio data size: " + HexTools.byteArrayToHex(audioData));
            }
            count++;
            myService.getVoiceHandler().speak(audioData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {

    }
}
