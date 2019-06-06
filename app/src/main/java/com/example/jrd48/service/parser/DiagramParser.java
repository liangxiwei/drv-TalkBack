package com.example.jrd48.service.parser;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.MyLogger;
import com.example.jrd48.service.protocol.IByteable;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Created by quhuabo on 2016/8/29 0029.
 * 解析网络数据包，允许少收或多收。少收时会缓存，多收时会触发多次 onGotPackage 事件。
 * 当出现 parseException 时，将会清除所有缓存的数据。即：相当于刚刚开始连接
 */

public class DiagramParser {

    public static int ITEM_HEAD = 0;    // 2        bytes
    public static int ITEM_LEN = 1;     // 2+4      bytes;
    public static int ITEM_DATA = 2;    // 2+4+n    bytes;
    public static int ITEM_CRC = 3;     // 2+4+n+4  bytes;

    private MyLogger mLog = MyLogger.jLog();

    ArrayList<Item> mItems = new ArrayList<Item>();

    Result mResult = null;
    Item mData = new Item(0);   // 变长 的数据
    private byte[] mBuffer = null;
    int mStep = 0;
    //private byte[] mPackData;
    private ParserListener mListener;       // 异步 listener(post 到主线程来执行)
    private ParserListener mSyncListener;   // 同步 listener
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public DiagramParser() {
        initial();
    }

    public byte[] getRemain() {
        return mBuffer;
    }

    private void initial() {
        mItems.add(new Item(new byte[]{(byte) 0x55, (byte) 0xaa}));  //0
        mItems.add(new Item(4)); //1.   // 长度为4个字节
        mItems.add(new Item(0)); //2.
        mItems.add(new Item(4)); //3.
    }

    private boolean isStageOK(List<Item> items) {
        for (Item i : items) {
            if (i.getStage() != ResultStage.Ok) {
                return false;
            }
        }
        return true;
    }

    public int getStep() {
        return mStep;
    }

    public int getStepCount() {
        return mItems.size();
    }


    /**
     * 组合报文解析
     * 会接收多个或不足一个包的数据
     *
     * @param ABuffer
     * @throws ParseException
     */
    public void parse(byte[] ABuffer) throws ParseException {

        assert ABuffer != this.mBuffer;

        this.mBuffer = ArrayUtils.addAll(this.mBuffer, ABuffer);

        LOOP_PACKAGE:
        /* 解析每一包 */
        do {
            try {
                // 解析每包中的每一个部分
                int j = mStep;
                for (; j < mItems.size(); ++j, ++mStep) {
                    Item i = mItems.get(j);

                    if (mBuffer == null) {
                        //mLog.v("no data to parsed, so break LOOP_PACKAGE, current step: " + j + ", got count: " + i.get_recogized_len() + "/" + i.getNeedLen());
                        break LOOP_PACKAGE;
                    }


                    mBuffer = i.parse(mBuffer);
                    if (i.isParseOk()) {
                        // mLog.v("got item: ("+i.getNeedLen()+")"+HexTools.byteArrayToHex(i.get_recognized()));
                        // 解析成功，继续下一个解析
                        if (j == ITEM_LEN) {
                            int dataLen = (int)ByteUtils.bytesToUInt(i.get_recognized());
                            //mLog.v("data block len: " + dataLen);
                            if (dataLen<0) {
                                throw new ParseException("parse error: len too large, should not great than 2G ");
                            }
                            mItems.get(ITEM_DATA).setNeedLen(dataLen);
                        }


                    } else if (i.getStage() == ResultStage.Fail) {
                        throw new ParseException("parse error, step: " + mStep);

                    } else {
                        // cached 状态下直接返回
                        //mLog.v("no data to parsed, so break LOOP_PACKAGE");
                        break LOOP_PACKAGE;
                    }

                }


                //final byte[] byteTemp = makePackData();
                //mLog.w("got [ crc error ] package: " + HexTools.byteArrayToHex(byteTemp,0, byteTemp.length, 20));

                if (!checkCrc()) {
                    throw new ParseException("parse error, crc error.");
                }


                if (getListener() != null) {

                    /* 需要深度复制，并发送到主线程去执行解析接收包的任务 */
                    final ArrayList<Item> itemsCopy = new ArrayList<Item>();
                    for (Item x : mItems) {
                        itemsCopy.add(new Item(x));
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            getListener().onGotPackage(itemsCopy);

                        }
                    });
                }

                if (mSyncListener != null) {
                    // 此处不用Copy，因为是同步线程中使用
                    mSyncListener.onGotPackage(mItems);
                }

            } catch (ParseException e) {
                e.printStackTrace();
                mLog.e("ParseException occurred: " + e.getMessage() + ", will reset all cached buffer.");
                mBuffer = null;
            }

            // 重置分段解析状态，并解析下一包。
            resetParseStatus();


        } while (mBuffer != null);

        //mLog.v("parse all finished, current step: " + mStep + ", max step: " + mItems.size());
    }

    /**
     * 重置解析状态，但Buffer不能清空，此函数不可为 public，仅仅由解析函数调用
     * 仅当解析完一包，或解析出错时调用
     */
    private void resetParseStatus() {
        mStep = 0;
        for (Item i : mItems) {
            i.reset();
        }
    }

    /**
     * 清除所有信息，包括缓存的数据
     * 用于初始化时调用。
     */
    public void resetAll() {
        resetParseStatus();
        mBuffer = null;
    }


    /**
     * 生成字节数据包
     *
     * @return
     */
    private byte[] makePackData() {

        if (mStep != mItems.size()) {
            mLog.w("pack not parsed.");
            return null;
        }

        byte[] result = null;
        int nLen = 0;
        for (int i = 0; i < mItems.size(); ++i) {
            nLen += mItems.get(i).get_recogized_len();
        }

        result = new byte[nLen];
        int k = 0;
        for (int i = 0; i < mItems.size(); ++i) {
            int copyLen = mItems.get(i).get_recogized_len();
            System.arraycopy(mItems.get(i).get_recognized(), 0, result, k, copyLen);
            k += copyLen;
        }
        return result;
    }

    /**
     * 检验检验字是否正确
     */
    private boolean checkCrc() throws ParseException {
        long got = ByteUtils.bytesToUInt(mItems.get(ITEM_CRC).get_recognized());
        if (got == 0xfefefefeL){
            return true;
        }
        CRC32 crc32 = new CRC32();
        int k = 0;
        for (int i = 0; i < mItems.size(); ++i) {
            if (i == ITEM_CRC) {
                continue;
            }
            k+=mItems.get(i).get_recogized_len();
            crc32.update(mItems.get(i).get_recognized());
        }
        long want = crc32.getValue();

        //mLog.v("got: " + Long.toHexString(got) + ", want: " + Long.toHexString(want)+", pack length: "+k);
        return want == got;

    }

    public ParserListener getListener() {
        return mListener;
    }

    /**
     * 设置异步回调，post 到主线程中去执行
     */
    public void setListener(ParserListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置同步回调，相同线程
     */
    public void setSyncListener(ParserListener ASyncListener) {
        this.mSyncListener = ASyncListener;
    }

    public static byte[] packData(IByteable object) {
        ByteBuffer bb = ByteBufferLE.allocate(2 + 4 + object.getSize() + 4);
        bb.put((byte) 0x55);
        bb.put((byte) 0xaa);
        bb.putInt(object.getSize());
        object.toBuffer(bb);
       /* CRC32 crc = new CRC32();
        crc.update(bb.array(), 0, bb.array().length - 4);
        long x = crc.getValue();
        bb.putInt((int) (x & 0xffffffffL));*/
        bb.putInt((int) (0xfefefefeL));
        return bb.array();
    }

    public static byte[] packData(int cmd, com.google.protobuf.GeneratedMessageV3 data) {
        byte[] temp = data.toByteArray();
        ByteBuffer bb = ByteBufferLE.allocate(2 + 4 + 4+ temp.length + 4);
        bb.put((byte) 0x55);
        bb.put((byte) 0xaa);
        bb.putInt(4+temp.length);
        bb.putInt(cmd);
        bb.put(temp);


        /*CRC32 crc = new CRC32();
        crc.update(bb.array(), 0, bb.array().length - 4);
        long x = crc.getValue();
        bb.putInt((int) (x & 0xffffffffL));*/
        bb.putInt((int) (0xfefefefeL));
        //Log.v("chat", "crc32: "+ Integer.toHexString((int)x));
        return bb.array();
    }

}

