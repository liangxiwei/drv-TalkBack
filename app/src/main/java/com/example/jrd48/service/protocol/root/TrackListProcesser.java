package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.bean.Track;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class TrackListProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.TrackListProcesser";
    private Context mContext;

    public TrackListProcesser(Context context) {
        super(context);
        mContext = context;
    }
    @Override
    public void onGot(byte[] data) {
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.MsgTrackQuery re = ProtoMessage.MsgTrackQuery.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.MsgTrack> tracks = re.getTracksList();

                    ArrayList<Track> myTracks = new ArrayList<>();
                    for (ProtoMessage.MsgTrack msgTrack : tracks){
                        Track track = new Track();
                        track.setTrack_id(msgTrack.getTrackId());
                        track.setDesc(msgTrack.getDesc());
                        track.setTitle(msgTrack.getTitle());
                        track.setTime(msgTrack.getTime());
                        track.setImg(msgTrack.getImg().toByteArray());
                        track.setVisible(msgTrack.getVisible());
                        myTracks.add(track);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("track_list",myTracks);
                    i.putExtras(bundle);
                } else {
                    Log.i("chat", "获得群列表错误码: " + re.getErrorCode());
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
        }
        context.sendBroadcast(i);
    }
    @Override
    public void onSent() {

    }

}
