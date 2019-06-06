package com.example.jrd48.service.protocol;

import android.content.Context;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public abstract class CommonProcesser {

    /** 通常为接收数据的Service对象 */
    protected Context context;

    public CommonProcesser(Context context) {
        this.context = context;
    }

    /**
     *  接收到数据包的处理方法
     * @param data 不包含协议的头、长度和尾
     */
    public abstract void onGot(byte[] data);

    /* 数据发送以后的通知事件 */
    public abstract void onSent();


}
