package com.luobin.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
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

import java.lang.reflect.Method;

public class FirstStatusBar extends FrameLayout {
    private static final String TAG = "FirstStatusBar";
    ImageView ivGps;
    ImageView ivWifi;
    ImageView ivMobile;
    TextView tvMobile;
    SignalStrength mSignalStrength;
    ServiceState mServiceState;
    NetworkInfo mWifiNetworkInfo;
    int mDataState = TelephonyManager.DATA_DISCONNECTED;
    int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    int mWifiSignalLevel = 0;
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

    public FirstStatusBar(Context context) {
        super(context);
        initView(context);
    }

    public FirstStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FirstStatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public FirstStatusBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.status_bar, null);
        ivGps = (ImageView) view.findViewById(R.id.iv_gps);
        ivWifi = (ImageView) view.findViewById(R.id.iv_wifi);
        tvMobile = (TextView) view.findViewById(R.id.tv_mobile);
        ivMobile = (ImageView) view.findViewById(R.id.iv_mobile);
        if (isGpsOpened()) {
            ivGps.setImageResource(R.drawable.icon_gps);
        } else {
            ivGps.setImageResource(R.drawable.ic_ban_gps);
        }
        if (isMobileDataEnabled()) {
            ivMobile.setImageResource(R.drawable.icon_4g);
        } else {
            ivMobile.setImageResource(R.drawable.ic_ban_4g);
        }
        if (isWifiEnabled()) {
            ivWifi.setImageResource(R.drawable.icon_wifi);
        } else {
            ivWifi.setImageResource(R.drawable.ic_ban_wifi);
        }
        this.addView(view);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.erobbing.gallery", "com.erobbing.gallery.activity.WifiDialogActivity");
                context.startActivity(intent);
            }
        });
        mMobileNetworkState = new MobileNetworkState(false, false, 0, 0);
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
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    }

    public void listenMobileNetworkState() {
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
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
        if (!isMobileDataEnabled()) {
            tvMobile.setText("4G");
            ivMobile.setImageResource(R.drawable.ic_ban_4g);
        } else if (mMobileNetworkState.dataInService) {
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
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mWifiStateBroadcastReceiver, filter);
    }

    private BroadcastReceiver mWifiStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == WifiManager.RSSI_CHANGED_ACTION) {
                WifiManager wifiManager = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                mWifiSignalLevel = WifiManager.calculateSignalLevel(info.getRssi(), 4);
                updateWifiState();
            } else if (intent.getAction() == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                updateWifiState();
            } else if (intent.getAction() == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                mWifiNetworkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (mWifiNetworkInfo != null) {
                    Log.d(TAG, "mWifiStateBroadcastReceiver mWifiNetworkInfo.isConnected():" + mWifiNetworkInfo.isConnected());
                }
                updateWifiState();
            } else if (intent.getAction() == LocationManager.PROVIDERS_CHANGED_ACTION) {
                if (isGpsOpened()) {
                    ivGps.setImageResource(R.drawable.icon_gps);
                } else {
                    ivGps.setImageResource(R.drawable.ic_ban_gps);
                }
            } else if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
                updateTelephony();
            }
        }
    };

    public  boolean isGpsOpened() {
        if (Build.VERSION.SDK_INT <19) {
            LocationManager myLocationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
            return myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else {
            int state = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            if (state== Settings.Secure.LOCATION_MODE_OFF) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean isMobileDataEnabled() {
        TelephonyManager telephonyService = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getDataEnabled = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getDataEnabled) {
                return (Boolean) getDataEnabled.invoke(telephonyService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isWifiEnabled() {
        WifiManager mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mWifiManager.isWifiEnabled();
    }

    public void updateWifiState() {
        Log.d(TAG, "updateWifiState: mWifiSignalLevel:" + mWifiSignalLevel);
        if (!isWifiEnabled()) {
            ivWifi.setImageResource(R.drawable.ic_ban_wifi);
        } else if ((mWifiNetworkInfo == null) || !mWifiNetworkInfo.isConnected()) {
            ivWifi.setImageResource(R.drawable.ic_wifi_signal_0_white);
        } else if (mWifiSignalLevel <= 0) {
            ivWifi.setImageResource(R.drawable.ic_wifi_signal_0_white);
        } else if (mWifiSignalLevel == 1) {
            ivWifi.setImageResource(R.drawable.ic_wifi_signal_one);
        } else if (mWifiSignalLevel == 2) {
            ivWifi.setImageResource(R.drawable.ic_wifi_signal_two);
        } else if (mWifiSignalLevel >= 3) {
            ivWifi.setImageResource(R.drawable.ic_wifi_signal_three);
        }
    }
}
