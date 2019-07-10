package com.example.jrd48.chat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * 轨迹
 */
public class Track implements Parcelable {
    long track_id;
    String title;
    String desc;
    long time; //轨迹开始时间
    int systime; //轨迹上传时间
    int visible; //是否公开
    byte[] img; //图片统一为JPG或PNG

    public long getTrack_id() {
        return track_id;
    }

    public void setTrack_id(long track_id) {
        this.track_id = track_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getSystime() {
        return systime;
    }

    public void setSystime(int systime) {
        this.systime = systime;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.track_id);
        dest.writeString(this.title);
        dest.writeString(this.desc);
        dest.writeLong(this.time);
        dest.writeInt(this.systime);
        dest.writeInt(this.visible);
        dest.writeByteArray(this.img);
    }

    public Track() {
    }

    protected Track(Parcel in) {
        this.track_id = in.readLong();
        this.title = in.readString();
        this.desc = in.readString();
        this.time = in.readLong();
        this.systime = in.readInt();
        this.visible = in.readInt();
        this.img = in.createByteArray();
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel source) {
            return new Track(source);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    @Override
    public String toString() {
        return "Track{" +
                "track_id=" + track_id +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", time=" + time +
                ", systime=" + systime +
                ", visible=" + visible +
                ", img=" + Arrays.toString(img) +
                '}';
    }
}
