package com.luobin.dvr.model;

import android.content.Context;

import com.luobin.dvr.R;
import com.luobin.dvr.DvrConfig;

public class SettingItem {

    public static final int SETTING_ITEM_AUTORUN_BOOT = 0;
    public static final int SETTING_ITEM_VIDEOSIZE = 1;
    public static final int SETTING_ITEM_VIDEODURATION = 2;
    public static final int SETTING_ITEM_SYNC_AUDIO = 3;
    public static final int SETTING_ITEM_COLLISION_SENSITIVITY = 4;

    private int selectId;
    private String name;
    private String[] values;

    private Context mContext;

    public SettingItem(Context context, int selectId, String name, int valuesId) {
        super();
        DvrConfig.init(context);
        mContext = context;
        this.selectId = selectId;
        this.name = name;
        values = this.mContext.getResources().getStringArray(valuesId);
    }

    public String getName() {
        return name;
    }

    public String[] getValues() {
        return values;
    }

    public void setCurValue(int position) {
        switch (selectId) {
            case SETTING_ITEM_AUTORUN_BOOT:
                String abval = mContext.getResources().getStringArray(R.array.autorun_boot_value)[position];
                boolean enable = Boolean.parseBoolean(abval);
                DvrConfig.setAutoRunWhenBoot(enable);
                break;
            case SETTING_ITEM_VIDEOSIZE:
                String vsval = mContext.getResources().getStringArray(R.array.videosize_value)[position];
                String[] sizes = vsval.split("x");
                int[] size = new int[2];
                size[0] = Integer.parseInt(sizes[0]);
                size[1] = Integer.parseInt(sizes[1]);
                DvrConfig.setVideoSize(size);
                break;
            case SETTING_ITEM_VIDEODURATION:
                int duration = mContext.getResources().getIntArray(R.array.videoduration_value)[position];
                DvrConfig.setVideoDuration(duration);
                break;
            case SETTING_ITEM_SYNC_AUDIO:
                String audioval = mContext.getResources().getStringArray(R.array.audio_enabled_value)[position];
                boolean en = Boolean.parseBoolean(audioval);
                DvrConfig.setAudioEnabled(en);
                break;
            case SETTING_ITEM_COLLISION_SENSITIVITY:
                int collision = mContext.getResources().getIntArray(R.array.collision_value)[position];
                DvrConfig.setCollisionSensitivity(collision);
                break;
        }
    }

    public String getCurValue() {
        switch (selectId) {
            case SETTING_ITEM_AUTORUN_BOOT:
                return values[getAutorunBootIndex()];
            case SETTING_ITEM_VIDEOSIZE:
                return values[getVideoSizeIndex()];
            case SETTING_ITEM_VIDEODURATION:
                return values[getVideoDurationIndex()];
            case SETTING_ITEM_SYNC_AUDIO:
                return values[getAudioEnableIndex()];
            case SETTING_ITEM_COLLISION_SENSITIVITY:
                return values[getCollisionSensitivityIndex()];
        }
        return null;
    }

    public int getCurValueIndex() {
        switch (selectId) {
            case SETTING_ITEM_AUTORUN_BOOT:
                return getAutorunBootIndex();
            case SETTING_ITEM_VIDEOSIZE:
                return getVideoSizeIndex();
            case SETTING_ITEM_VIDEODURATION:
                return getVideoDurationIndex();
            case SETTING_ITEM_SYNC_AUDIO:
                return getAudioEnableIndex();
            case SETTING_ITEM_COLLISION_SENSITIVITY:
                return getCollisionSensitivityIndex();

        }
        return 0;
    }

    private int getAutorunBootIndex() {
        boolean autoRun = false;
        autoRun = DvrConfig.getAutoRunWhenBoot();
        String bool = Boolean.toString(autoRun);
        String[] autoRunBoot = mContext.getResources().getStringArray(R.array.autorun_boot_value);
        for (int i = autoRunBoot.length - 1; i >= 0; i--) {
            if (autoRunBoot[i].equals(bool)) {
                return i;
            }
        }
        return 0;
    }

    private int getVideoSizeIndex() {
        int[] size = new int[2];
        DvrConfig.getVideoSize(size);
        String[] vsval = mContext.getResources().getStringArray(R.array.videosize_value);
        for (int i = vsval.length - 1; i >= 0; i--) {
            if (vsval[i].equals(String.format("%dx%d", size[0], size[1]))) {
                return i;
            }
        }
        return 0;
    }

    private int getVideoDurationIndex() {
        int duration = 0;
        duration = DvrConfig.getVideoDuration();
        int[] durations = mContext.getResources().getIntArray(R.array.videoduration_value);
        for (int i = durations.length - 1; i >= 0; i--) {
            if (durations[i] == duration) {
                return i;
            }
        }
        return 0;
    }

    private int getAudioEnableIndex() {
        boolean audioEnabled = false;
        audioEnabled = DvrConfig.getAudioEnabled();
        String audioEN = Boolean.toString(audioEnabled);
        String[] audio = mContext.getResources().getStringArray(R.array.audio_enabled_value);
        for (int i = audio.length - 1; i >= 0; i--) {
            if (audio[i].equals(audioEN)) {
                return i;
            }
        }
        return 0;
    }

    private int getCollisionSensitivityIndex() {
        int collision = 0;
        collision = DvrConfig.getCollisionSensitivity();
        int[] collision_value = mContext.getResources().getIntArray(R.array.collision_value);
        for (int i = collision_value.length - 1; i >= 0; i--) {
            if (collision_value[i] == collision) {
                return i;
            }
        }
        return 0;
    }

}
