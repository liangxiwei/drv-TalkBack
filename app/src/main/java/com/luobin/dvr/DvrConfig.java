package com.luobin.dvr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.example.jrd48.chat.crash.MyApplication;
import com.luobin.dvr.R;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DvrConfig {
    private static final String TAG = DvrService.TAG;
    private static Context mContext = null;
    private final static String PREFERENCE_NAME = "Dvr";
    private final static String KEY_VIDEO_DURATION = "VideoDuration";
    private final static String KEY_VIDEO_SIZE = "VideoSize";
    private final static String KEY_STORAGE_PATH = "StoragePath";
    private final static String KEY_AUDIO_ENABLED = "AudioEnabled";
    private final static String KEY_COLLISION_SENSITIVITY = "CollisionSensitivity";
    private final static String KEY_AUTO_RUN_WHEN_BOOT = "AutoRunWhenBoot";
    private final static String KEY_THUMBNAIL_VIEW_RECT = "ThumbnailViewRect";
    private final static String KEY_PRE_VIDEO_TIME = "PreVideoTime";
    private final static String KEY_VIDEO_BITRATE = "VideoBitrate";

    public static void init(Context context) {
        if (mContext == null) {
            mContext = context;
        } else {
            Log.w(TAG, "DvrConfig already initialized");
        }
    }

    public static int getVideoDuration() {
        int defval = mContext.getResources().getInteger(R.integer.default_video_duration);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        int val = sp.getInt(KEY_VIDEO_DURATION, defval);
        Log.d(TAG, "DvrConfig getVideoDuration: " + val);
        return val;
    }

    public static void setVideoDuration(int duration_ms) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_VIDEO_DURATION, duration_ms).commit();
        Log.d(TAG, "DvrConfig setVideoDuration: " + duration_ms);
    }

    public static void getVideoSize(int[] size) {
        String defval = mContext.getResources().getString(R.string.default_videosize);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String s = sp.getString(KEY_VIDEO_SIZE, defval);
        String[] slist = s.split("x");
        size[0] = Integer.parseInt(slist[0]);
        size[1] = Integer.parseInt(slist[1]);
        Log.d(TAG, "DvrConfig getVideoSize: " + s);
    }

    public static void setVideoSize(int[] size) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_VIDEO_SIZE, String.format("%dx%d", size[0], size[1])).commit();
        Log.d(TAG, "DvrConfig setVideoSize: " + String.format("%dx%d", size[0], size[1]));
    }

    public static String getStoragePath() {
//        String defval = mContext.getResources().getString(R.string.default_storage_path);
//        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
//        String val = sp.getString(KEY_STORAGE_PATH, defval);
        String path1 = DvrConfig.getStoragePath(MyApplication.getContext(),true);
        if(path1 != null) {
            String val = path1 + "/dvr";
            Log.d(TAG, "DvrConfig getStoragePath: " + val);
            return val;
        } else {
            String path2 = DvrConfig.getStoragePath(MyApplication.getContext(),false);
            String val = path2 + "/dvr";
            Log.d(TAG, "DvrConfig getStoragePath: " + val);
            return val;
        }
    }

    public static String getStoragePath(Context mContext, boolean is_removale) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void setStoragePath(String path) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_STORAGE_PATH, path).commit();
        Log.d(TAG, "DvrConfig setStoragePath: " + path);
    }

    public static boolean getAudioEnabled() {
        boolean defval = mContext.getResources().getBoolean(R.bool.default_audio_enabled);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean val = sp.getBoolean(KEY_AUDIO_ENABLED, defval);
        Log.d(TAG, "DvrConfig getAudioEnabled: " + val);
        return val;
    }

    public static void setAudioEnabled(boolean enabled) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_AUDIO_ENABLED, enabled).commit();
        Log.d(TAG, "DvrConfig setAudioEnabled: " + enabled);
    }

    public static int getCollisionSensitivity() {
        int defval = mContext.getResources().getInteger(R.integer.default_collision_value);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        int val = sp.getInt(KEY_COLLISION_SENSITIVITY, defval);
        Log.d(TAG, "DvrConfig getCollisionSensitivity: " + val);
        return val;
    }

    public static void setCollisionSensitivity(int val) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_COLLISION_SENSITIVITY, val).commit();
        Log.d(TAG, "DvrConfig setCollisionSensitivity: " + val);
    }

    public static void setAutoRunWhenBoot(boolean enabled) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_AUTO_RUN_WHEN_BOOT, enabled).commit();
        Log.d(TAG, "DvrConfig setAutoRunWhenBoot: " + enabled);
    }

    public static boolean getAutoRunWhenBoot() {
        boolean defval = mContext.getResources().getBoolean(R.bool.default_auto_run_when_boot);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean val = sp.getBoolean(KEY_AUTO_RUN_WHEN_BOOT, defval);
        Log.d(TAG, "DvrConfig getAutoRunWhenBoot: " + val);
        return val;
    }

    public static void getThumbnailViewRect(int[] size) {
        String defval = mContext.getResources().getString(R.string.default_thumbnail_view_rect);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String val = sp.getString(KEY_THUMBNAIL_VIEW_RECT, defval);
        String[] rect = val.split(",");
        for (int i = 0; i < 4; i++) {
            size[i] = Integer.parseInt(rect[i]);
        }
        Log.d(TAG, "DvrConfig getThumbnailViewRect: " + val);
    }

    public static void setThumbnailViewRect(int[] size) {
        String val = String.format("%d,%d,%d,%d", size[0], size[1], size[2], size[3]);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_THUMBNAIL_VIEW_RECT, val).commit();
        Log.d(TAG, "DvrConfig getThumbnailViewRect: " + val);
    }

    public static int getPreVideoTime() {
        int defval = mContext.getResources().getInteger(R.integer.default_pre_video_time);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        int val = sp.getInt(KEY_PRE_VIDEO_TIME, defval);
        Log.d(TAG, "DvrConfig getPreVideoTime: " + val);
        return val;
    }

    public static void setPreVideoTime(int time) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_PRE_VIDEO_TIME, time).commit();
        Log.d(TAG, "DvrConfig setPreVideoTime: " + time);
    }

    public static int getVideoBitrate() {
        int defval = mContext.getResources().getInteger(R.integer.default_video_bitrate);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        int val = sp.getInt(KEY_VIDEO_BITRATE, defval);
        Log.d(TAG, "DvrConfig getVideoBitrate: " + val);
        return val;
    }

    public static void setVideoBitrate(int bitrate) {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_VIDEO_BITRATE, bitrate).commit();
        Log.d(TAG, "DvrConfig setVideoBitrate: " + bitrate);
    }
}
