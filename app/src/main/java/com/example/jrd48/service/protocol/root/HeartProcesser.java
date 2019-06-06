package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.util.Log;

import com.example.jrd48.service.protocol.CommonProcesser;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class HeartProcesser extends CommonProcesser {
    public HeartProcesser(Context context){
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "got heart package");
    }

    @Override
    public void onSent() {
        Log.i("chat", "sent heart package");
    }
}
