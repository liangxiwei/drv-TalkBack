package com.example.jrd48.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.ToastR;
import com.luobin.dvr.R;
import com.luobin.voice.VoiceHandler;

/**
 * Created by quhuabo on 2017/3/31.
 */

public class BluetoothMonitor {

    public static final String TAG = "pocdemo";
    public static final String VENDOR_CATEGORY = "android.bluetooth.headset.intent.category.companyid.85";
    public static final String UPDATE_CHAT_VIEW = "com.example.jrd48.service.update.chat.view";
    private static final String[] TALK_STRING = {"PTT1", "PTT2"};
    // The BroadcastReceiver that listens for discovered ACTION_VENDOR_SPECIFIC_HEADSET_EVENT
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "get action: " + action);
            // When discovery finds a ACTION_VENDOR_SPECIFIC_HEADSET_EVENT
            if (BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT.equals(action)) {
                Bundle eventExtra = intent.getExtras();

                // get the arguments that the headset passed out
                Object[] args = (Object[]) eventExtra.get(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS);

                String eventName = (String) args[0];
                if (eventName.equals("PTT1") || eventName.equals("PTT2")) {
                    Integer eventValue = (Integer) args[1];
                    boolean bStart = false;
                    if (eventValue == 1) {
                        if (GlobalStatus.isPttKeyDown()) {
                            Log.i(TAG, "cur is ptt down");
                            return;
                        }
                        bStart = true;
//                        ToastR.setToast(context,"ptt down");
                        Log.i(TAG, "ptt down");
                        GlobalStatus.setPttKeyDown(true);
                        VoiceHandler.speakBeginAndRecording(context);
                    } else if (eventValue == 0) {
                        bStart = false;
                        Log.i(TAG, "ptt up");
                        GlobalStatus.setPttKeyDown(false);
                        VoiceHandler.speakEndAndRecroding(context);

//                        ToastR.setToast(context,"ptt up");
                    }


                    Intent in = new Intent(UPDATE_CHAT_VIEW);
                    in.putExtra("keyDown", bStart);
                    context.sendBroadcast(in);
                }
            }
        }
    };

    private final BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "get action: " + action);
            // When discovery finds a ACTION_VENDOR_SPECIFIC_HEADSET_EVENT
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                switch (state) {
                    case BluetoothDevice.BOND_NONE:
                        Log.d("aaa", name + "disconnect");
                        if(GlobalStatus.isPttKeyDown()) {
                            ToastR.setToast(context, context.getString(R.string.bluetooth_disconnect_close_speak));
                            GlobalStatus.setPttKeyDown(false);
                            VoiceHandler.speakEndAndRecroding(context);
                            Intent in = new Intent(UPDATE_CHAT_VIEW);
                            in.putExtra("keyDown", false);
                            context.sendBroadcast(in);
                        }
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("aaa", name + "connecting");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("aaa", name + "connected");
                        break;
                }
            }
        }
    };

    Context context;
    private Handler mHandler = new Handler();

    public BluetoothMonitor(Context context) {
        this.context = context;
        InitBroadcast();
    }

    public static boolean isBlueToothHeadsetConnected() {
        boolean retval = true;
        try {
            retval = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(android.bluetooth.BluetoothProfile.HEADSET)
                    != android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

        } catch (Exception exc) {
            // nothing to do
        }
        return retval;
    }

    //初始化监听蓝牙PTT按键广播
    private void InitBroadcast() {
        IntentFilter filter = new IntentFilter(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        filter.addCategory(VENDOR_CATEGORY);
        context.registerReceiver(mReceiver, filter);

        IntentFilter connectFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mConnectReceiver, connectFilter);
    }

    public void onDestroy() {
        try {
            if (mReceiver != null) {
                context.unregisterReceiver(mReceiver);
            }

            if (mConnectReceiver != null) {
                context.unregisterReceiver(mConnectReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
