package com.luobin.dvr.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.R;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.fragment.SettingsFragment2;
import com.luobin.dvr.view.ImageViewWithText;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = com.luobin.dvr.DvrService.TAG;
    private ImageViewWithText mBtnPreview = null;
    private ImageViewWithText mBtnSettings = null;
    private ImageViewWithText mBtnFiles = null;

    private Fragment mPreviewFragment = null;
    private Fragment mSettingsFragment = null;
    public static final String DVR_FULLSCREEN_SHOW = "dvr_fullscreen_show";
    private boolean isSmartMirror = true;
    private SmartMirrorsObserver mSmartMirrorsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        startService(new Intent(this, DvrService.class));
        setContentView(R.layout.main_activity);
        mBtnPreview = (ImageViewWithText) findViewById(R.id.btn_preview);
        mBtnPreview.setOnClickListener(this);
        mBtnSettings = (ImageViewWithText) findViewById(R.id.btn_settings);
        mBtnSettings.setOnClickListener(this);
        mBtnFiles = (ImageViewWithText) findViewById(R.id.btn_files);
        mBtnFiles.setOnClickListener(this);

        onClick(mBtnPreview);
        if (isSmartMirror) {
            mSmartMirrorsObserver = new SmartMirrorsObserver(new Handler());
            mSmartMirrorsObserver.startObserving();
        }
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (v == mBtnPreview) {
            if (mPreviewFragment == null) {
                mPreviewFragment = new PreviewFragment();
            }
            ft.replace(R.id.id_content, mPreviewFragment);
        } else if (v == mBtnSettings) {
            if (mSettingsFragment == null) {
                mSettingsFragment = new SettingsFragment2();
            }
            ft.replace(R.id.id_content, mSettingsFragment);
        } else if (v == mBtnFiles) {

        }
        ft.commit();

    }
	
	/* zhangzhaolei add for switch dvr fullscreen preview 20170328 start */
    @Override
    protected void onResume() {
        super.onResume();
        if (isSmartMirror) {
            Settings.System.putInt(getContentResolver(), DVR_FULLSCREEN_SHOW, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSmartMirror) {
            Settings.System.putInt(getContentResolver(), DVR_FULLSCREEN_SHOW, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isSmartMirror) {
            mSmartMirrorsObserver.stopObserving();
        }
    }

    /**
     * ContentObserver to watch dvr fullscreen
     **/
    private class SmartMirrorsObserver extends ContentObserver {

        private final Uri DVR_FULLSCREEN_SHOW_URI =
                Settings.System.getUriFor(DVR_FULLSCREEN_SHOW);
        public SmartMirrorsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;
            if (DVR_FULLSCREEN_SHOW_URI.equals(uri)) {
                if (1 == Settings.System.getInt(getContentResolver(), DVR_FULLSCREEN_SHOW, 0)) {
                    Log.e("====", "====SmartMirrorsObserver.onChange=DB_NAVI_FORWARD");
                } else if (0 == Settings.System.getInt(getContentResolver(), DVR_FULLSCREEN_SHOW, 0)) {
                    Log.e("====", "====SmartMirrorsObserver.onChange=DB_NAVI_BACK");
                    finish();
                }
            }
        }

        public void startObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    DVR_FULLSCREEN_SHOW_URI,
                    false, this);
        }

        public void stopObserving() {
            final ContentResolver cr = getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
    /* zhangzhaolei add for switch dvr fullscreen preview 20170328 end */
}
