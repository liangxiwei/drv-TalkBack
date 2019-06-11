package com.example.jrd48.service.Socket;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.MyLogger;
import com.example.jrd48.service.SendDataParcelable;
import com.example.jrd48.service.parser.ByteBufferLE;
import com.example.jrd48.service.parser.DiagramParser;
import com.example.jrd48.service.parser.ParserListener;
import com.luobin.voice.VolatileBool;

import org.apache.commons.lang3.ArrayUtils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class SocketRunable implements Runnable {


    private Handler mHandler;

    public interface Listener {
        /**
         * 数据完全发送以后的通知事件，注意，其中的数据部分可能不完整
         * TODO: 将来可能会增加保存整个数据包，即：只要增加一个起始偏移量即可
         */
        void onPackSent(SendDataParcelable temp);

        void onConnected();

        void onDisconnected();


    }

    ;

    private Context context;
    private Listener mListener;

    private static final int SOCKET_CONN_TIMEOUT = 5000;    // 5秒钟超时
    private static final int MAX_RECV_BUFF = 4 * 1024;        // 4K
    private MyLogger mLog = MyLogger.jLog();

    private String mHost = "irobbing.com";
    //private String mHost = "120.76.42.120";
    //private String mHost = "10.0.0.66";
   // private int mPort = 18000;
    private int mPort = 18001;
    /**
     * 接收缓冲区
     */
    private ByteBuffer mBuffer = ByteBuffer.allocate(MAX_RECV_BUFF);

    /**
     * 发送的数据队列
     */
    private BlockingDeque<SendDataParcelable> mDataQueue = new LinkedBlockingDeque<SendDataParcelable>();

    Selector selector = null;
    SocketChannel client = null;

    private DiagramParser mParser = new DiagramParser();

    private VolatileBool mThreadRunning = new VolatileBool();

    public SocketRunable(Context context, Handler handler) {
        this.context = context;
        this.mHandler = handler;
    }


    public boolean isRunning() {
        return mThreadRunning.getValue();
    }

    public void setRuning(boolean b) {
        mThreadRunning.setValue(b);
        if(!b) {
            wakeup();
        }
    }

    public void putData(SendDataParcelable d) {
        mDataQueue.offer(d);
        wakeup();
    }

    public void putDataFirst(SendDataParcelable d) {
        mDataQueue.offerFirst(d);
        wakeup();
    }

    private void wakeup() {
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
    }

    @Override
    public void run() {
        mLog.i("socket thread start.");

        try {
            doWakeLock(5000);

            for (; isRunning(); Thread.sleep(2000)) {
                mDataQueue.clear();
                mParser.resetAll();
                doSocketThreadFunc();
                if (!isRunning() || !ConnUtil.isConnected(context) || Thread.interrupted()) {
                    mLog.w("stop socket thread because disconnect or manual restart.");
                    break;
                } else {
                    mLog.i("try to connect service again in 2 seconds.");
                }

                doWakeLock(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mThreadRunning.setValue(false);
        mLog.w("socket thread stop.");

    }

    /**
     * 连接服务器并进行相关的数据接收与发送。
     * 如果不熟悉 java NIO，尽量不要修改此函数
     */
    private void doSocketThreadFunc() {
        boolean bConnected = false;

        try {

            // 唤醒15秒，用来连接服务器
            doWakeLock(15000);

            InetSocketAddress ip = new InetSocketAddress(mHost, mPort);
            //CharsetEncoder encoder = Charset.forName("GB2312").newEncoder();

            long start = System.currentTimeMillis();
            //打开Socket通道
            client = SocketChannel.open();
            //设置为非阻塞模式
            client.configureBlocking(false);
            //打开选择器
            selector = Selector.open();
            //注册连接服务端socket动作
            client.register(selector, SelectionKey.OP_CONNECT);
            mLog.i("connecting...");
            //连接
            client.connect(ip);

            mBuffer.clear();
            int total = 0;
            final int connectingTime = 15*1000; //15s
            int tryTimes = 1;

            _FOR:
            for (; isRunning(); ) {
                // 50 毫秒超时
                //mLog.i("selecting...");
                final int selectInterval = 50;

                if (selector.select(selectInterval) == 0) {
                    if (bConnected) {
                        //mLog.i("begin to write data to socket.");
                        // 检查发送队列是否要数据要发送

                        SendDataParcelable d = mDataQueue.poll();
                        while (d != null) {

                            if (!isRunning()) {
                                break _FOR;
                            }

                            int n = client.write(ByteBufferLE.wrap(d.getData()));
                            if (n > 0) {
                                if (n < d.getDataLen()) {
                                    //mLog.i("write data len: " + n + "/" + );
                                    mLog.w("miss pack data: " + n + "/" + d.getData());

                                    // 放回到队列的最前面
                                    // drop the missing data for LB1728
                                    if (!Build.PRODUCT.contains("LB1728")) {
                                        SendDataParcelable r = new SendDataParcelable(ArrayUtils.subarray(d.getData(), n, d.getData().length),
                                                d.getDataType());

                                        putDataFirst(r);
                                    }
//                                  mDataQueue.offerFirst(r);
                                    break;
                                } else {
                                    // 发送完整一个包
                                    // 复制
                                    mLog.d("completed pack data sending");
                                    final SendDataParcelable temp = new SendDataParcelable(d);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mListener.onPackSent(temp);
                                        }
                                    });
                                }
                            } else {
                                putDataFirst(d);
//                                mDataQueue.offerFirst(d);
                                break;
                            }


                            d = mDataQueue.poll();
                        }

                        continue;
                    } else {
                        if ((selectInterval * tryTimes) < connectingTime){
                            tryTimes++;
                        }else{
                            mLog.w("connect time out.");
                            break _FOR;
                        }
                    }
                }
                // Get keys
                Set keys = selector.selectedKeys();
                Iterator i = keys.iterator();
                //mLog.i("got keys: " + keys.size());
                while (i.hasNext()) {
                    SelectionKey key = (SelectionKey) i.next();
                    i.remove();

                    // Get the socket channel held by the key
                    SocketChannel channel = (SocketChannel) key.channel();

                    if (key.isConnectable()) {
                        bConnected = true;
                        // Connection OK
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        // 已经连接上了，放到读写检测区域
                        channel.register(selector, SelectionKey.OP_READ);

                        // 已经连接上
                        mListener.onConnected();

                        mLog.i("connected Ok");

                    } else if (key.isReadable()) {
                        // 获取电源管理器对象
                        doWakeLock(5000);

                        // 当前状态可读
                        int count = channel.read(mBuffer);
                        if (count > 0) {
                            total += count;
                            mBuffer.flip();

                            byte[] data = new byte[mBuffer.remaining()];
                            mBuffer.get(data);
                            mBuffer.clear();

                            //mLog.v("got data bytes: [" + data.length + "]: " + HexTools.byteArrayToHex(data));
                            mParser.parse(data);

                        } else {
                            client.close();
                            mLog.v("connection break.");
                            break _FOR;
                        }
                    }/* else if (key.isWritable()) {
                        // 检查发送队列是否要数据要发送
                        final SendDataParcelable d = mDataQueue.poll();
                        if (d != null) {
                            channel.write(ByteBufferLE.wrap(d.getData()));
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onPackSent(d);
                                }
                            });


                        }
                    }*/
                }
            }

            if (client.isOpen()) {
                client.close();
            }
        } catch (Throwable e) {
            mLog.e("socket function exception: " + e.getMessage());
            e.printStackTrace();
        }

        // close socket and select as needed
        if (client != null && client.isOpen()) {
            try {
                client.close();

            } catch (Throwable e) {
                mLog.e("close socket channel exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (selector != null && selector.isOpen()) {
            try {
                selector.close();
                selector = null;
            } catch (Throwable e) {
                mLog.e("close selector exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
        mLog.d("socket function end.");

        if (bConnected) {
            mListener.onDisconnected();
        }

    }

    private void doWakeLock(int timeout) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pocdemo");
        wl.setReferenceCounted(false);
        wl.acquire(timeout);
    }

    public void setListener(Listener func) {
        this.mListener = func;
    }

    /**
     * 异步调用监听器
     */
    public void setParseListener(ParserListener func) {
        this.mParser.setListener(func);
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String mHost) {
        this.mHost = mHost;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int mPort) {
        this.mPort = mPort;
    }
}
