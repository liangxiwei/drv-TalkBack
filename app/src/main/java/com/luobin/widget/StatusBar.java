package com.luobin.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.luobin.dvr.R;

public class StatusBar extends FrameLayout {
    private static final String TAG = "DvrStatusBar";
    ImageView ivWifi;
    ImageView ivMobile;
    TextView tvMobile;
    SignalStrength mSignalStrength;
    ServiceState mServiceState;
    int mDataState = TelephonyManager.DATA_DISCONNECTED;
    int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    MobileNetworkState mMobileNetworkState;

    private class MobileNetworkState {
        public boolean dataInService;
        public boolean dataConnected;
        public int networkType;
        public int signalLevel;
        public MobileNetworkState(boolean inService, boolean connected, int type, int level) {
            dataInService = inService;
            dataConnected = connected;
            networkType = type;
            signalLevel = level;
        }
    }

    public StatusBar(Context context) {
        super(context);
        initView(context);
    }

    public StatusBar( Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public StatusBar(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public StatusBar(Context context,AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.status_bar,null);
        ivWifi = (ImageView) view.findViewById(R.id.iv_wifi);
        tvMobile = (TextView) view.findViewById(R.id.tv_mobile);
        ivMobile = (ImageView) view.findViewById(R.id.iv_mobile);
        this.addView(view);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.erobbing.gallery","com.erobbing.gallery.activity.WifiDialogActivity");
                context.startActivity(intent);
            }
        });
        mMobileNetworkState = new MobileNetworkState(false, false,0, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerBroadcast(getContext());
        listenMobileNetworkState();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mWifiStateBroadcastReceiver);
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener,PhoneStateListener.LISTEN_NONE);
    }

    public void listenMobileNetworkState() {
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                | PhoneStateListener.LISTEN_SERVICE_STATE
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    PhoneStateListener mPhoneListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            mServiceState = serviceState;
            Log.d(TAG, "mServiceState = " + mServiceState);
            updateTelephony();
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrength = signalStrength;
            Log.d(TAG, "mSignalStrength = " + mSignalStrength);
            updateTelephony();
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            mDataState = state;
            mDataNetType = networkType;
            Log.d(TAG, "mDataState = " + mDataState + "; mDataNetType = " + mDataNetType);
            updateTelephony();
        }
    };

    private void updateTelephony() {
        final TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        mMobileNetworkState.dataInService = (mSignalStrength != null) && hasService();
        mMobileNetworkState.dataConnected = (mDataState == TelephonyManager.DATA_CONNECTED);
        mMobileNetworkState.networkType = getNetworkType();
        mMobileNetworkState.signalLevel = getSingalLevel();
        Log.d(TAG, "networkType = " + mMobileNetworkState.networkType
                + "; mMobileNetworkState.signalLevel = " + mMobileNetworkState.signalLevel);
        if (mMobileNetworkState.dataInService) {
            if (mMobileNetworkState.networkType == 2) {
                tvMobile.setText("2G");
            } else if (mMobileNetworkState.networkType == 3) {
                tvMobile.setText("3G");
            } else if (mMobileNetworkState.networkType == 4) {
                tvMobile.setText("4G");
            }
            if (mMobileNetworkState.signalLevel == 4) {
                ivMobile.setImageResource(R.drawable.ic_4g_signal_four);
            } else if (mMobileNetworkState.signalLevel == 3) {
                ivMobile.setImageResource(R.drawable.ic_4g_signal_three);
            } else if (mMobileNetworkState.signalLevel == 2) {
                ivMobile.setImageResource(R.drawable.ic_4g_signal_two);
            } else if (mMobileNetworkState.signalLevel == 1) {
                ivMobile.setImageResource(R.drawable.ic_4g_signal_one);
            } else {
                ivMobile.setImageResource(R.drawable.ic_qs_signal_0);
            }
        } else if (tm.hasIccCard()) {
            tvMobile.setText("2G");
            ivMobile.setImageResource(R.drawable.ic_qs_signal_0);
        } else {
            tvMobile.setVisibility(View.GONE);
            ivMobile.setImageResource(R.drawable.ic_qs_no_sim);
        }
    }

    private int getSingalLevel() {
        int level = -1;
        if (mSignalStrength != null) {
            if (!mSignalStrength.isGsm()) {
                level = mSignalStrength.getCdmaLevel();
            } else {
                level = mSignalStrength.getLevel();
            }
        }
        return level;
    }

    private int getNetworkType() {
        int networkType = -1;
        if (mDataNetType == TelephonyManager.NETWORK_TYPE_UNKNOWN
                || mDataNetType == TelephonyManager.NETWORK_TYPE_EDGE
                || mDataNetType == TelephonyManager.NETWORK_TYPE_CDMA
                || mDataNetType == TelephonyManager.NETWORK_TYPE_1xRTT) {
            networkType = 2;
        } else if (mDataNetType == TelephonyManager.NETWORK_TYPE_EVDO_0
                || mDataNetType == TelephonyManager.NETWORK_TYPE_EVDO_A
                || mDataNetType == TelephonyManager.NETWORK_TYPE_EVDO_B
                || mDataNetType == TelephonyManager.NETWORK_TYPE_EHRPD
                || mDataNetType == TelephonyManager.NETWORK_TYPE_UMTS
                || mDataNetType == TelephonyManager.NETWORK_TYPE_HSDPA
                || mDataNetType == TelephonyManager.NETWORK_TYPE_HSUPA
                || mDataNetType == TelephonyManager.NETWORK_TYPE_HSPA
                || mDataNetType == TelephonyManager.NETWORK_TYPE_HSPAP) {
            networkType = 3;
        } else if (mDataNetType == TelephonyManager.NETWORK_TYPE_LTE
                || mDataNetType == TelephonyManager.NETWORK_TYPE_LTE_CA) {
            networkType = 4;
        }
        return networkType;
    }

    private boolean hasService() {
        if (mServiceState != null) {
            switch (mServiceState.getVoiceRegState()) {
                case ServiceState.STATE_POWER_OFF:
                    return false;
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_EMERGENCY_ONLY:
                    return mServiceState.getDataRegState() == ServiceState.STATE_IN_SERVICE;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    private void registerBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        context.registerReceiver(mWifiStateBroadcastReceiver, filter);
    }

    private BroadcastReceiver mWifiStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == WifiManager.RSSI_CHANGED_ACTION) {
                    WifiManager wifiManager = (WifiManager) context
                            .getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = wifiManager.getConnectionInfo();
                    int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                    switch (strength) {
                        case 1:
                            ivWifi.setBackgroundResource(R.drawable.ic_wifi_signal_one);
                            break;
                        case 2:
                            ivWifi.setBackgroundResource(R.drawable.ic_wifi_signal_two);
                            break;
                        case 3:
                            ivWifi.setBackgroundResource(R.drawable.ic_wifi_signal_three);
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                        default:
                            break;
                    }
            }
        }
    };
}