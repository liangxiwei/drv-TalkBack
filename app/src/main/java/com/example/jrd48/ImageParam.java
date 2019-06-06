package com.example.jrd48;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageParam implements Parcelable {
    public static final Parcelable.Creator<ImageParam> CREATOR = new Creator<ImageParam>() {
        @Override
        public ImageParam[] newArray(int size) {
            return new ImageParam[size];
        }

        @Override
        public ImageParam createFromParcel(Parcel in) {
            return new ImageParam(in);
        }
    };

    public long msgID;
    public long msgSN;
    public boolean mySend;

    public ImageParam() {

    }

    public ImageParam(Parcel in) {
        msgID = in.readLong();
        msgSN = in.readLong();
        mySend = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(msgID);
        parcel.writeLong(msgSN);
        parcel.writeInt(mySend ? 1 : 0);
    }

    public long getMsgID() {
        return msgID;
    }

    public void setMsgID(long msgID) {
        this.msgID = msgID;
    }

    public long getMsgSN() {
        return msgSN;
    }

    public void setMsgSN(long msgSN) {
        this.msgSN = msgSN;
    }

    public boolean isMySend() {
        return mySend;
    }

    public void setMySend(boolean mySend) {
        this.mySend = mySend;
    }
}