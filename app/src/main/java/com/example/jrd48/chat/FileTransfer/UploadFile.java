package com.example.jrd48.chat.FileTransfer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.jrd48.chat.SQLite.LinkmanRecordHelper;
import com.example.jrd48.chat.SQLite.TeamRecordHelper;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.UploadProcesser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrd48 on 2017/2/13.
 */

public class UploadFile {
    private Context context;
    private TimeoutBroadcastManager mBroadcastManger;
    private List<TansferFileUp> fileList = new ArrayList<>();

    public UploadFile(Context context, TimeoutBroadcastManager mBroadcastManger) {
        this.context = context;
        this.mBroadcastManger = mBroadcastManger;
    }

    public TimeoutBroadcastManager getBroadcastManager() {
        return mBroadcastManger;
    }

    public void startUpload(TansferFileUp tansferFileUp) {
        fileList.add(tansferFileUp);
        Log.i("jrdchat", "列表长度：" + fileList.size() + "");
        if (fileList.size() == 1) {
            UpFile(fileList.get(0));
        }
    }

    private void UpFile(TansferFileUp tansferFileUp) {
        ProtoMessage.MsgAttachment.Builder builder = ProtoMessage.MsgAttachment.newBuilder();

        builder.setMsgID(tansferFileUp.getMsgID());
        builder.setMsgSize(tansferFileUp.getMsgSize());
        builder.setPackSize(tansferFileUp.getPackSize());
        builder.setPackCnt(tansferFileUp.getPackCnt());
        builder.setPackNum(tansferFileUp.getPackNum());
        //Log.i("jrdchat", "msg_sum: " + HexTools.byteArrayToHex(tansferFileUp.getMsgSum()));
        builder.setMsgSum(com.google.protobuf.ByteString.copyFrom(tansferFileUp.getMsgSum()));
        if (tansferFileUp.getPackNum() != 0) {
            builder.setPackData(com.google.protobuf.ByteString.copyFrom(tansferFileUp.getPackData(), 0, tansferFileUp.getReadLen()));
        }
        MyService.start(context, ProtoMessage.Cmd.cmdUploadAttachment.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UploadProcesser.ACTION);
        TimeoutBroadcast b = new TimeoutBroadcast(context, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("jrdchat", "上传超时");
                SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                String myphone = preferences.getString("phone", "");
                long teamId = fileList.get(0).getTeamID();
                if (teamId == 0) {
                    LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context,
                            myphone + fileList.get(0).getPhone() + "LinkmanMsgShow.dp", null);
                    SQLiteDatabase db = linkmanRecordHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("percent", -1);
                    db.update("LinkmanRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                    db.close();
                } else {
                    TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamId + "TeamMsgShow.dp", null);
                    SQLiteDatabase db = teamRecordHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("percent", -1);
                    db.update("TeamRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                    db.close();
                }
                Intent intent = new Intent("upload.percent");
                intent.putExtra("msgid", fileList.get(0).getMsgID());
                intent.putExtra("uploading", false);
                context.sendBroadcast(intent);
                removeList();
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("jrdchat", "上传返回ok");
                    ProtoMessage.MsgAttachment resp = (ProtoMessage.MsgAttachment) i.getSerializableExtra("resp");
                    if (fileList.size() > 0) {
                        //Log.i("jrdchat", "本地msgID: " + fileList.get(0).getMsgID() + "   服务器返回的msgID: " + resp.getMsgID());
                        if (fileList.get(0).getMsgID() == resp.getMsgID()) {
                            if (fileList.get(0).setPackNum(resp.getPackNum())) {
                                int percent = ((fileList.get(0).getPackNum()) * 100) / fileList.get(0).getPackCnt();
                                SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                                String myphone = preferences.getString("phone", "");
                                long teamId = fileList.get(0).getTeamID();
                                if (teamId == 0) {
                                    LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context,
                                            myphone + fileList.get(0).getPhone() + "LinkmanMsgShow.dp", null);
                                    SQLiteDatabase db = linkmanRecordHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("percent", percent);
                                    db.update("LinkmanRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                                    db.close();
                                } else {
                                    TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamId + "TeamMsgShow.dp", null);
                                    SQLiteDatabase db = teamRecordHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("percent", percent);
                                    db.update("TeamRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                                    db.close();
                                }
                                UpFile(fileList.get(0));
                                Intent intent = new Intent("upload.percent");
                                intent.putExtra("percent", percent);
                                intent.putExtra("msgid", fileList.get(0).getMsgID());
                                intent.putExtra("uploading", true);
                                context.sendBroadcast(intent);
                            } else {
                                SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                                String myphone = preferences.getString("phone", "");
                                long teamId = fileList.get(0).getTeamID();
                                Log.i("jrdchat", "teamid:" + teamId);
                                Log.i("jrdchat", "phone:" + fileList.get(0).getPhone());
                                if (teamId == 0) {
                                    LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context,
                                            myphone + fileList.get(0).getPhone() + "LinkmanMsgShow.dp", null);
                                    SQLiteDatabase db = linkmanRecordHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("percent", 101);
                                    db.update("LinkmanRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                                    db.close();
                                } else {
                                    TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamId + "TeamMsgShow.dp", null);
                                    SQLiteDatabase db = teamRecordHelper.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("percent", 101);
                                    db.update("TeamRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                                    db.close();
                                }
                                Intent intent = new Intent("upload.percent");
                                intent.putExtra("msgid", fileList.get(0).getMsgID());
                                intent.putExtra("uploading", false);
                                context.sendBroadcast(intent);
                                removeList();
                                Log.i("chatjrd", "上传成功");
                            }
                        } else {
                            Log.i("jrdchat", "上传数据出现问题");
                        }
                    } else {
                        Log.i("jrdchat", "List列表数据丢失");
                    }
                } else if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.MSG_ATTACHMENT_UPLOADED.getNumber()) {
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    String myphone = preferences.getString("phone", "");
                    long teamId = fileList.get(0).getTeamID();
                    Log.i("jrdchat", "teamid:" + teamId);
                    Log.i("jrdchat", "phone:" + fileList.get(0).getPhone());
                    if (teamId == 0) {
                        LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context,
                                myphone + fileList.get(0).getPhone() + "LinkmanMsgShow.dp", null);
                        SQLiteDatabase db = linkmanRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("percent", 101);
                        db.update("LinkmanRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                        db.close();
                    } else {
                        TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamId + "TeamMsgShow.dp", null);
                        SQLiteDatabase db = teamRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("percent", 101);
                        db.update("TeamRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                        db.close();
                    }
                    Intent intent = new Intent("upload.percent");
                    intent.putExtra("msgid", fileList.get(0).getMsgID());
                    intent.putExtra("uploading", false);
                    context.sendBroadcast(intent);
                    removeList();
                    Log.i("chatjrd", "上传成功");
                } else {
                    Log.i("jrdchat", "上传返回有问题: " + i.getIntExtra("error_code", -1));
                    SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
                    String myphone = preferences.getString("phone", "");
                    long teamId = fileList.get(0).getTeamID();
                    if (teamId == 0) {
                        LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context,
                                myphone + fileList.get(0).getPhone() + "LinkmanMsgShow.dp", null);
                        SQLiteDatabase db = linkmanRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("percent", -1);
                        db.update("LinkmanRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                        db.close();
                    } else {
                        TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamId + "TeamMsgShow.dp", null);
                        SQLiteDatabase db = teamRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("percent", -1);
                        db.update("TeamRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                        db.close();
                    }
                    Intent intent = new Intent("upload.percent");
                    intent.putExtra("msgid", fileList.get(0).getMsgID());
                    intent.putExtra("uploading", false);
                    context.sendBroadcast(intent);
                    removeList();
                    Log.i("chatjrd", "上传失败");
                    }
            }
        });
    }

    private void removeList() {
        fileList.remove(0);
        Log.i("jrdchat", "列表长度：" + fileList.size() + "");
        if (!fileList.isEmpty()) {
            UpFile(fileList.get(0));
        }else{
            Intent i = new Intent(context, TransferService.class);
            context.stopService(i);
        }
    }

    public void stopAll() {
        try {
            SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
            String myphone = preferences.getString("phone", "");
            for (; !fileList.isEmpty(); fileList.remove(0)) {
                long teamId = fileList.get(0).getTeamID();
                if (teamId == 0) {
                    LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(context,
                            myphone + fileList.get(0).getPhone() + "LinkmanMsgShow.dp", null);
                    SQLiteDatabase db = linkmanRecordHelper.getWritableDatabase();
                    try {
                        ContentValues values = new ContentValues();
                        values.put("percent", -1);
                        db.update("LinkmanRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                    } finally {
                        db.close();
                    }

                } else {
                    TeamRecordHelper teamRecordHelper = new TeamRecordHelper(context, myphone + teamId + "TeamMsgShow.dp", null);
                    SQLiteDatabase db = teamRecordHelper.getWritableDatabase();
                    try {
                        ContentValues values = new ContentValues();
                        values.put("percent", -1);
                        db.update("TeamRecord", values, "service_id = ?", new String[]{fileList.get(0).getMsgID() + ""});
                    } finally {
                        db.close();
                    }

                }
                Intent intent = new Intent("upload.percent");
                intent.putExtra("msgid", fileList.get(0).getMsgID());
                intent.putExtra("uploading", false);
                context.sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
