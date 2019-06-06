package com.example.jrd48.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by quhuabo on 2017/1/19 0019.
 */

public class TimeoutBroadcastManager {

    private Set<TimeoutBroadcast> mList = new HashSet<TimeoutBroadcast>();

    public void add(TimeoutBroadcast b) {
        synchronized (mList) {
            try {
                mList.add(b);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void remove(TimeoutBroadcast b) {
        try {
            synchronized (mList) {
                try {
                    mList.remove(b);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止所有超时广播，并清除数组
     */

    public void stopAll() {
        synchronized (mList) {
            try {
                while (!mList.isEmpty()) {
                    TimeoutBroadcast t = mList.iterator().next();
                    if (t != null) {
                        t.stop();
                        mList.remove(t);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            mList.clear();
        }
    }



}
