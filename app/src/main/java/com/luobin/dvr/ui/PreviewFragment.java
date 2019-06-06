package com.luobin.dvr.ui;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luobin.dvr.IDvrService;
import com.luobin.dvr.R;

import me.lake.librestreaming.client.RESClient;

public class PreviewFragment extends Fragment implements View.OnClickListener {
    
    private final String TAG = com.luobin.dvr.DvrService.TAG;
    
    private boolean mStartPreviewWhenDvrConnected = false;
    
    private ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onServiceConnected");
            mDvrService = IDvrService.Stub.asInterface(service);
            mBtnRecord.setEnabled(true);
            transfer.setEnabled(true);
            if (mStartPreviewWhenDvrConnected) {
                showCamPreview();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onServiceDisconnected");
            mDvrService = null;
            mBtnRecord.setEnabled(false);
        }
        
    };
    
    private final int MSG_SHOW_CAM_PREVIEW = 1;
    private final int MSG_UPDATE_REC_BTN_TEXT = 2;
    private final int MSG_UPDATE_RTMP_BTN_TEXT = 3;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SHOW_CAM_PREVIEW) {
                if (mDvrService != null) {
                    showCamPreview();
                } else {
                    mStartPreviewWhenDvrConnected = true;
                }
            } else if (msg.what == MSG_UPDATE_REC_BTN_TEXT) {
                if (mDvrService != null) {
                    try {
                        Log.d(TAG, "updateRecBtnText isRecording()="+mDvrService.isRecording());
                        mBtnRecord.setText(mDvrService.isRecording() ? "Stop Record" : "Start Record");
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == MSG_UPDATE_RTMP_BTN_TEXT){
                if (mDvrService != null) {
                    try {
                        Log.d(TAG, "updateRecBtnText rtmpStatus()="+mDvrService.rtmpStatus());
                        switch (mDvrService.rtmpStatus()){
                            case RESClient.STATUS_NULL_PREPARED:
                                transfer.setEnabled(true);
                                switchBtn.setEnabled(false);
                                transfer.setText("start Transfer");
                                break;
                            case RESClient.STATUS_NULL_STARTED:
                                transfer.setEnabled(false);
                                switchBtn.setEnabled(false);
                                mHandler.removeMessages(MSG_UPDATE_RTMP_BTN_TEXT);
                                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RTMP_BTN_TEXT, 500);
                                break;
                            case RESClient.STATUS_SUCCESS:
                                transfer.setEnabled(true);
                                transfer.setText("Stop Transfer");
                                switchBtn.setEnabled(true);
                                break;
                            default:
                                break;
                        }

                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            super.handleMessage(msg);
        }
        
    };
    
    private IDvrService mDvrService;
    private Button mBtnRecord = null;
    private Button transfer;
    private Button switchBtn;
    private View camView;
    private View.OnLayoutChangeListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG, "PreviewFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "PreviewFragment onCreateView");
        View view = inflater.inflate(R.layout.preview_fragment, null);
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "PreviewFragment onStart");
        super.onStart();
        mBtnRecord = (Button)getActivity().findViewById(R.id.btn_record);
        mBtnRecord.setEnabled(mDvrService != null);
        mBtnRecord.setOnClickListener(this);

        transfer = (Button)getActivity().findViewById(R.id.btn_transfer);
        transfer.setEnabled(mDvrService != null);
        transfer.setOnClickListener(this);

        switchBtn = (Button)getActivity().findViewById(R.id.btn_switch);
        switchBtn.setEnabled(false);
        switchBtn.setOnClickListener(this);

        listener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                mHandler.removeMessages(MSG_SHOW_CAM_PREVIEW);
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_CAM_PREVIEW, 200);
            }
        };
        camView = getActivity().findViewById(R.id.cam_preview);
        camView.addOnLayoutChangeListener(listener);
        Intent i = new Intent("com.luobin.dvr.DvrService");
        i.setComponent(new ComponentName("com.luobin.dvr", "com.luobin.dvr.DvrService"));
        getActivity().bindService(i, mServiceConn, Context.BIND_AUTO_CREATE);
        
    }

    @Override
    public void onResume() {
        Log.d(TAG, "PreviewFragment onResume");
        super.onResume();
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_CAM_PREVIEW, 600);

    }

    @Override
    public void onPause() {
        Log.d(TAG, "PreviewFragment onPause");
        super.onPause();
        mHandler.removeMessages(MSG_SHOW_CAM_PREVIEW);
        if (mDvrService != null) {
            try {
                mDvrService.hide();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "PreviewFragment onStop");
        super.onStop();
        getActivity().unbindService(mServiceConn);

        if(camView != null) {
            camView.removeOnLayoutChangeListener(listener);
        }
        mDvrService = null;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "PreviewFragment onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "PreviewFragment onDestroy");
        super.onDestroy();
    }

    private void showCamPreview() {
        if (mDvrService != null) {
            try {
                mDvrService.startPreview();
                View camView = getActivity().findViewById(R.id.cam_preview);
                camView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                    }
                });
                Rect rect = new Rect();
                camView.getLocalVisibleRect(rect);
                Log.d(TAG, "PreviewFragment location="+rect.toString());
                mDvrService.show(rect.left,
                        rect.top,
                        rect.width(),
                        rect.height());
                mHandler.removeMessages(MSG_UPDATE_REC_BTN_TEXT);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_REC_BTN_TEXT, 500);
                mHandler.removeMessages(MSG_UPDATE_RTMP_BTN_TEXT);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RTMP_BTN_TEXT, 500);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_transfer){
            if (mDvrService != null) {
                try {
                    if (mDvrService.rtmpStatus() == RESClient.STATUS_NULL_PREPARED) {
                        mDvrService.startRtmp();
                    } else if(mDvrService.rtmpStatus() == RESClient.STATUS_SUCCESS){
                        mDvrService.stopRtmp();
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                mHandler.removeMessages(MSG_UPDATE_RTMP_BTN_TEXT);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RTMP_BTN_TEXT, 500);
            }
        } else if(v.getId() == R.id.btn_switch){
            if (mDvrService != null) {
                try {
                    if(mDvrService.rtmpStatus() == RESClient.STATUS_SUCCESS){
                        mDvrService.switchCamera();
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                mHandler.removeMessages(MSG_UPDATE_RTMP_BTN_TEXT);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RTMP_BTN_TEXT, 500);
            }
        } else if(v.getId() == R.id.btn_record){
            if (mDvrService != null) {
                try {
                    if (mDvrService.isRecording()) {
                        mDvrService.stopRecord();
                    } else {
                        mDvrService.startCircleRecord();
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //updateRecBtnText();
                mHandler.removeMessages(MSG_UPDATE_REC_BTN_TEXT);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_REC_BTN_TEXT, 500);
            }
        }
    }
    
    private void updateRecBtnText() {
        new Thread() {

            @Override
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mDvrService != null) {
                            try {
                                Log.d(TAG, "updateRecBtnText isRecording()="+mDvrService.isRecording());
                                mBtnRecord.setText(mDvrService.isRecording() ? "Stop Record" : "Start Record");
                            } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    
                });
            }
            
        }.start();
        
    }
}
