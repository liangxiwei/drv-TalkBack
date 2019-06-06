package com.example.jrd48.chat;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jrd48.GlobalStatus;

/**
 * Created by jrd48 on 2017/3/3.
 */

public class SettingRW {

    private static final int DEFAULT_LOCATION_INTERVAL = 1; // 单位：分钟
    private final static int HEART_INTERVAL = 1 * 60;

    private boolean handFree = true;
    private boolean autoMaxVolume = true;
    private boolean enableLocation = true;
    private int micWay = 0;
    private int intervalTime = DEFAULT_LOCATION_INTERVAL;  // 间隔10分钟一次定位
    private float mapZoomLevel = 11;
    private boolean notifyUser = false;
    private boolean autoStart = false;

    private boolean viewVideo = false;
    private boolean transferVideo = false;
    private int roadRequestReply = 1;
    Context mContext = null;

    private int changeHeartbeat = HEART_INTERVAL;

    public int getChangeHeartbeat() {
        return changeHeartbeat;
    }

    public void setChangeHeartbeat(int changeHeartbeat) {
        GlobalStatus.setChangeHeartbeat(changeHeartbeat);
        this.changeHeartbeat = changeHeartbeat;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isHandFree() {
        return handFree;
    }

    public void setHandFree(boolean handFree) {
        this.handFree = handFree;
    }

    public boolean isAutoMaxVolume() {
        return autoMaxVolume;
    }

    public void setAutoMaxVolume(boolean autoMaxVolume) {
        this.autoMaxVolume = autoMaxVolume;
    }

    public boolean isEnableLocation() {
        return enableLocation;
    }

    public void setEnableLocation(boolean enableLocation) {
        this.enableLocation = enableLocation;
    }

    public int getMicWay() {
        return micWay;
    }

    public void setMicWay(int micWay) {
        this.micWay = micWay;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int value) {
        intervalTime = value;
    }

    public float getMapZoomLevel() {
        return mapZoomLevel > 0 ? mapZoomLevel : 11;
    }

    public void setMapZoomLevel(float value) {
        mapZoomLevel = value;
    }

    public boolean isNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(boolean notifyUser) {
        this.notifyUser = notifyUser;
    }

    public SettingRW(Context context) {
        mContext = context;
    }

    public void loadMapZoomLevel() {
        SharedPreferences preferences = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        mapZoomLevel = preferences.getFloat("map_zoom_level", 11);
        return;
    }

    public synchronized void saveMapZoomLevel() {
        SharedPreferences preferences = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("map_zoom_level", mapZoomLevel);
        editor.commit();
    }

    public SettingRW(Context context, boolean bLoad) {
        mContext = context;
        if (bLoad) {
            load();
        }
    }

    public void load() {
        SharedPreferences preferences = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        handFree = preferences.getBoolean("handFree", true);
        autoMaxVolume = preferences.getBoolean("max_volume", true);
        enableLocation = preferences.getBoolean("enableLocation", true);
        notifyUser = preferences.getBoolean("notify_user", true);
        autoStart = preferences.getBoolean("auto_start", true);
        viewVideo = preferences.getBoolean("view_video", false);
        transferVideo = preferences.getBoolean("transfer_video", false);
        micWay = preferences.getInt("mic_way", 0);
        intervalTime = preferences.getInt("interval_time", DEFAULT_LOCATION_INTERVAL);
        mapZoomLevel = preferences.getFloat("map_zoom_level", 11);
        roadRequestReply = preferences.getInt("road_request_reply", 0);
        changeHeartbeat = preferences.getInt("heartbeat", HEART_INTERVAL);

        return;
    }

    public synchronized void save() {
        SharedPreferences preferences = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("handFree", handFree);
        editor.putBoolean("max_volume", autoMaxVolume);
        editor.putBoolean("enableLocation", enableLocation);
        editor.putBoolean("notify_user", notifyUser);
        editor.putBoolean("auto_start", autoStart);
        editor.putBoolean("view_video", viewVideo);
        editor.putBoolean("transfer_video", transferVideo);
        editor.putInt("mic_way", micWay);
        editor.putInt("interval_time", intervalTime);
        editor.putFloat("map_zoom_level", mapZoomLevel);
        editor.putInt("road_request_reply", roadRequestReply);
        editor.putInt("heartbeat", changeHeartbeat);
        editor.commit();
    }

    public void reDefault() {
        setAutoMaxVolume(true);
        setHandFree(true);
        setEnableLocation(true);
        setNotifyUser(true);
        setAutoStart(true);
        setViewVideo(false);
        setTransferVideo(false);
        setMicWay(0);
        setIntervalTime(DEFAULT_LOCATION_INTERVAL);
        setMapZoomLevel(11);
        setRoadRequestReply(1);
        setChangeHeartbeat(HEART_INTERVAL);
        save();
    }


    public boolean isViewVideo() {
        return viewVideo;
    }

    public void setViewVideo(boolean viewVideo) {
        this.viewVideo = viewVideo;
    }

    public boolean isTransferVideo() {
        return transferVideo;
    }

    public void setTransferVideo(boolean transferVideo) {
        this.transferVideo = transferVideo;
    }

    public int getRoadRequestReply() {
        return roadRequestReply;
    }

    public void setRoadRequestReply(int roadRequestReply) {
        this.roadRequestReply = roadRequestReply;
    }
}
