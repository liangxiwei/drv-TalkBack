package com.luobin.dvr.fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.dvr.IDvrService;
import com.luobin.dvr.adapter.SettingsLeftAdapter;
import com.luobin.dvr.adapter.SettingsRightAdapter;
import com.luobin.dvr.model.SettingItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment2 extends Fragment {
    private final String TAG = com.luobin.dvr.DvrService.TAG;
    private List<SettingItem> mItems = new ArrayList();
    private List<String> mItemValues = new ArrayList();
    private Context mContext;
    private SettingsLeftAdapter mItemAdapter;
    private SettingsRightAdapter mValueAdapter;
    private ListView mSettingsItemList;
    private ListView mSettingsValueList;
    private TextView mValueTitleView;
    private int curIndex;
    SettingItem mCurSettingItem;
    private ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onServiceConnected");
            mDvrService = IDvrService.Stub.asInterface(service);

            if (mStopDvrWhenServiceConnected) {
                stopDvr();
            }

            if (mUpdateSettingsWhenServiceConnected) {
                updateSettings();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onServiceDisconnected");
            mDvrService = null;
        }

    };

    private IDvrService mDvrService;

    private boolean mUpdateSettingsWhenServiceConnected = false;

    private boolean mStartDvrPreviewWhenExit = false;
    private boolean mStartDvrRecordingWhenExit = false;

    private boolean mStopDvrWhenServiceConnected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SettingsFragment onCreate");
        Intent i = new Intent("com.luobin.dvr.DvrService");
        i.setComponent(new ComponentName("com.luobin.dvr", "com.luobin.dvr.DvrService"));
        getActivity().bindService(i, mServiceConn, Context.BIND_AUTO_CREATE);
        mContext = getActivity();
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "SettingsFragment onCreateView");
        View view = inflater.inflate(R.layout.settings_main, null);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "SettingsFragment onStart");
        super.onStart();
        if (mDvrService == null) {
            mStopDvrWhenServiceConnected = true;
        } else {
            stopDvr();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "SettingsFragment onResume");
        super.onResume();
        if (mDvrService == null) {
            mUpdateSettingsWhenServiceConnected = true;
        } else {
            updateSettings();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "SettingsFragment onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "SettingsFragment onStop");
        super.onStop();
        saveSettings();
        try {
            if (mStartDvrPreviewWhenExit) {
                mDvrService.startPreview();
            }
            if (mStartDvrRecordingWhenExit) {
                mDvrService.startCircleRecord();
            }
            getActivity().unbindService(mServiceConn);
            mDvrService = null;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "SettingsFragment onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SettingsFragment onStop");
        super.onDestroy();
    }

    private void stopDvr() {
        try {
            if (mDvrService.isRecording()) {
                mDvrService.stopRecord();
                mStartDvrRecordingWhenExit = true;
            }
            if (mDvrService.isPreviewing()) {
                mDvrService.hide();
                mDvrService.stopPreview();
                mStartDvrPreviewWhenExit = true;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateSettings() {
        // update video size
        /*int[] size = new int[2];
        try {
            mDvrService.getVideoSize(size);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        Spinner vs = (Spinner)getActivity().findViewById(R.id.videosize);
        String[] vsval = getResources().getStringArray(R.array.videosize_value);
        for (int i=vsval.length-1; i>=0; i--) {
            if (vsval[i].equals(String.format("%dx%d", size[0], size[1]))) {
                vs.setSelection(i);
                break;
            }
        }
        // video duration
        vs = (Spinner)getActivity().findViewById(R.id.videoduration);
        int[] durations = getResources().getIntArray(R.array.videodurationvalms);
        int duration = 0;
        try {
            duration = mDvrService.getVideoDuration();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        for (int i=durations.length-1; i>=0; i--) {
            if (durations[i] == duration) {
                vs.setSelection(i);
                break;
            }
        }
        
        // audio enabled ?
        Switch sw = (Switch)getActivity().findViewById(R.id.audio);
        boolean audioEnabled = false;
        try {
            audioEnabled = mDvrService.getAudioEnabled();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sw.setChecked(audioEnabled);
        
        sw = (Switch)getActivity().findViewById(R.id.auto_run);
        boolean autorun = false;
        try {
            autorun = mDvrService.getAutoRunWhenBoot();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sw.setChecked(autorun);*/
    }

    private void saveSettings() {
        // video size
        /*Spinner vs = (Spinner)getActivity().findViewById(R.id.videosize);
        int pos = vs.getSelectedItemPosition();
        String vsval = getResources().getStringArray(R.array.videosize_value)[pos];
        String[] sizes = vsval.split("x");
        int[] size = new int[2];
        size[0] = Integer.parseInt(sizes[0]);
        size[1] = Integer.parseInt(sizes[1]);
        try {
            mDvrService.setVideoSize(size);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // video duration
        vs = (Spinner)getActivity().findViewById(R.id.videoduration);
        pos = vs.getSelectedItemPosition();
        int duration = getResources().getIntArray(R.array.videodurationvalms)[pos];
        try {
            mDvrService.setVideoDuration(duration);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // audio enabled ?
        Switch sw = (Switch)getActivity().findViewById(R.id.audio);
        try {
            mDvrService.setAudioEnabled(sw.isChecked());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        sw = (Switch)getActivity().findViewById(R.id.auto_run);
        try {
            mDvrService.setAutoRunWhenBoot(sw.isChecked());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    }

    private void init() {
        mItems.clear();
        mItems.add(new SettingItem(getActivity(), SettingItem.SETTING_ITEM_AUTORUN_BOOT, mContext.getResources().getString(R.string.auto_run_when_boot), R.array.autorun_boot));
        mItems.add(new SettingItem(getActivity(), SettingItem.SETTING_ITEM_VIDEOSIZE, mContext.getResources().getString(R.string.video_size), R.array.videosize));
        mItems.add(new SettingItem(getActivity(), SettingItem.SETTING_ITEM_VIDEODURATION, mContext.getResources().getString(R.string.video_duration), R.array.videoduration));
        mItems.add(new SettingItem(getActivity(), SettingItem.SETTING_ITEM_SYNC_AUDIO, mContext.getResources().getString(R.string.audio_sync), R.array.audio_enabled));
        mItems.add(new SettingItem(getActivity(), SettingItem.SETTING_ITEM_COLLISION_SENSITIVITY, mContext.getResources().getString(R.string.collision_sensitivity), R.array.collision_sensitivity));
        mItemAdapter = new SettingsLeftAdapter(getActivity(), mItems);
        mValueAdapter = new SettingsRightAdapter(getActivity(), mItemValues);
    }

    private void initView(View view) {
        mSettingsItemList = ((ListView) view.findViewById(R.id.lv_left));
        mSettingsItemList.setAdapter(this.mItemAdapter);
        mSettingsItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> AdapterView, View View, int position, long id) {
                Log.i("DvrFrontSettingsFragment", " onItemClick position = " + position);
                selectItem(position);
            }
        });
        mSettingsValueList = ((ListView) view.findViewById(R.id.lv_right));
        mSettingsValueList.setAdapter(this.mValueAdapter);
        mValueTitleView = ((TextView) view.findViewById(R.id.title_right));
        mSettingsValueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> AdapterView, View View, int position, long id) {
                mCurSettingItem.setCurValue(position);
                mItemAdapter.notifyDataSetChanged();
                mValueAdapter.setSelect(position);
            }
        });
        selectItem(this.curIndex);
    }

    private void selectItem(int position) {
        curIndex = position;
        if ((position >= 0) && (position < mItems.size())) {
            mItemValues.clear();
            mCurSettingItem = ((SettingItem) mItems.get(position));
            mItemValues.addAll(Arrays.asList(mCurSettingItem.getValues()));
            mItemAdapter.setSelect(position);
            mValueAdapter.setSelect(mCurSettingItem.getCurValueIndex());
            mValueTitleView.setText(mCurSettingItem.getName());
        }
    }
}
