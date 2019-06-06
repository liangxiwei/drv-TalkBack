package com.example.jrd48.chat.FileTransfer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.TimeoutBroadcastManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.DownloadProcesser;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jrd48 on 2017/2/13.
 */

public class DownloadFile {
    private final Context context;
    private TimeoutBroadcastManager mBroadcastManger;
    private List<TansferFileDown> fileList = new ArrayList<>();

    public DownloadFile(Context context, TimeoutBroadcastManager mBroadcastManger) {
        this.mBroadcastManger = mBroadcastManger;
        this.context = context;
    }

    public TimeoutBroadcastManager getBroadcastManager() {
        return mBroadcastManger;
    }

    public void startDownload(TansferFileDown tansferFileDown) {
        fileList.add(tansferFileDown);
        Log.i("jrdchat", "列表长度：" + fileList.size() + "");
        if (fileList.size() == 1) {
            DownFile(fileList.get(0));
        }
    }

    private void DownFile(TansferFileDown tansferFileDown) {
        ProtoMessage.MsgAttachment.Builder builder = ProtoMessage.MsgAttachment.newBuilder();
        builder.setMsgID(tansferFileDown.getMsgID());
        builder.setPackNum(tansferFileDown.getPackNum());

        File tempFile = new File(tansferFileDown.getFileAddress() + ".tmp");
        Log.i("jrdchat", "download tmp file: " + tempFile.getAbsolutePath());
        builder.setMsgSize(tempFile.length());

        MyService.start(context, ProtoMessage.Cmd.cmdDownloadAttachment_VALUE, builder.build());

        final IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadProcesser.ACTION);
        TimeoutBroadcast b = new TimeoutBroadcast(context, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("jrdchat", "下载出错");
                Intent intent = new Intent("download.percent");
                intent.putExtra("msgid", fileList.get(0).getMsgID());
                intent.putExtra("success", false);
                context.sendBroadcast(intent);
                removeList();
            }

            @Override
            public void onGot(Intent i) {
                try {
                    if (i.getIntExtra("error_code", -1) ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {
                        Log.i("jrdchat", "下载返回ok");
                        ProtoMessage.MsgAttachment resp = (ProtoMessage.MsgAttachment) i.getSerializableExtra("resp");
                        int packNum = resp.getPackNum();
                        if (fileList.get(0).getPackCnt() == 0) {
                            fileList.get(0).setPackCnt(resp.getPackCnt());
                            Log.i("chatjrd", "总包数：" + resp.getPackCnt() + "");
                            fileList.get(0).setPackSize(resp.getPackSize());
                            fileList.get(0).setMsgSum(resp.getMsgSum().toByteArray());
                            //fileList.get(0).setPackData(resp.getPackData().toByteArray());
                            fileList.get(0).setMsgSize(resp.getMsgSize());
                        }


                        File tempFile = new File(fileList.get(0).getFileAddress() + ".tmp");
                        if (packNum == 0) {

                            if (tempFile.exists()) {
                                tempFile.delete();
                            }
                        } else {
                            try {
                                // ***********************************写文件开始***************************************
                                Log.i("jrdchat", "file name: " + tempFile.getAbsolutePath());
                                if (!tempFile.exists()) {
                                    tempFile.createNewFile();
                                }
                                RandomAccessFile randomFile = new RandomAccessFile(tempFile, "rw");
                                // 文件长度，字节数
                                long fileLength = (packNum - 1) * resp.getPackSize();
                                //将写文件指针移到文件尾。
                                randomFile.seek(fileLength);
                                randomFile.write(resp.getPackData().toByteArray());
                                randomFile.close();
                                // ************************************写文件结束*************************************

                                if (resp.getPackNum() >= resp.getPackCnt()) {
                                    //Log.i("chatjrd", "校验字：" + SHA256Tool.bytes2Hex(fileList.get(0).getMsgSum()) + ", 文件大小：" + tempFile.length());
                                    if (Arrays.equals(fileList.get(0).getMsgSum(),
                                            SHA256Tool.getFileSHA256(tempFile))) {
                                        Log.i("jrdchat", "下载完成, 校验字成功");
                                        tempFile.renameTo(new File(fileList.get(0).getFileAddress()));
                                        Intent intent = new Intent("download.percent");
                                        intent.putExtra("msgid", fileList.get(0).getMsgID());
                                        intent.putExtra("success", true);
                                        context.sendBroadcast(intent);
                                        removeList();
                                    } else {
                                        Intent intent = new Intent("download.percent");
                                        intent.putExtra("msgid", fileList.get(0).getMsgID());
                                        intent.putExtra("success", false);
                                        context.sendBroadcast(intent);
                                        Log.i("jrdchat", "下载失败, 校验字失败，删除文件. 文件大小：" + tempFile.length());
                                        tempFile.delete();
                                        removeList();
                                    }
                                    return;
                                }
                                Log.i("jrdchat", "文件第" + packNum + "包数据");
                                Intent intent = new Intent("download.percent");
                                intent.putExtra("msgid", fileList.get(0).getMsgID());
                                intent.putExtra("percent", (int) (100.0 * packNum / fileList.get(0).getPackCnt()));
                                intent.putExtra("success", false);
                                context.sendBroadcast(intent);
                            } catch (Exception e) {
                                Log.i("jrdchat", "抓住异常文件被删除");
                                tempFile.delete();
                                removeList();
                                e.printStackTrace();
                                //Log.i("jrdchat", "文件可能被删除");
                            }
                        }
                        fileList.get(0).setPackNum(packNum + 1);
                        DownFile(fileList.get(0));
                    } else {
                        Log.i("jrdchat", "下载出错");
                        Intent intent = new Intent("download.percent");
                        intent.putExtra("msgid", fileList.get(0).getMsgID());
                        intent.putExtra("success", false);
                        context.sendBroadcast(intent);
                        removeList();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void removeList() {
        fileList.remove(0);
        Log.i("jrdchat", "列表长度：" + fileList.size() + "");
        if (!fileList.isEmpty()) {
            DownFile(fileList.get(0));
        }else{
            Intent i = new Intent(context, TransferService.class);
            context.stopService(i);
        }
    }

    public void stopAll() {
        if(!fileList.isEmpty()){
            Intent intent = new Intent("download.percent");
            intent.putExtra("msgid", fileList.get(0).getMsgID());
            intent.putExtra("success", false);
            context.sendBroadcast(intent);
        }
        fileList = null;
    }


}
