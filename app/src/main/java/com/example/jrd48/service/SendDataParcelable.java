package com.example.jrd48.service;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.jrd48.service.parser.DiagramParser;
import com.example.jrd48.service.protocol.Data;

import org.apache.commons.lang3.ArrayUtils;


public class SendDataParcelable implements Parcelable {
    public int getDataLen() {
        return dataLen;
    }

    private int dataLen;
    private byte[] data;
    private int dataType = 0;// 参见 Cmd 的数据类型


    public SendDataParcelable(SendDataParcelable src) {
        this.dataLen = src.dataLen;
        this.dataType = src.dataType;
        this.data = ArrayUtils.clone(src.data);
    }

    public SendDataParcelable(Data data) {
        setData(DiagramParser.packData(data));
        setDataType(data.getCmd());
    }

    public SendDataParcelable(byte[] AData, int dataType) {
        setData(AData);
        this.dataType = dataType;
    }

    public SendDataParcelable(Parcel source) {
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        dataLen = source.readInt();
        data = new byte[dataLen];
        source.readByteArray(data);
        dataType = source.readInt();
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dataLen);
        dest.writeByteArray(data);
        dest.writeInt(dataType);
    }

    // 必须提供一个名为CREATOR的static final属性 该属性需要实现android.os.Parcelable.Creator<T>接口
    public static final Creator<SendDataParcelable> CREATOR = new Creator<SendDataParcelable>() {

        @Override
        public SendDataParcelable createFromParcel(Parcel source) {
            return new SendDataParcelable(source);
        }

        @Override
        public SendDataParcelable[] newArray(int size) {
            return new SendDataParcelable[size];
        }
    };

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        dataLen = data.length;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }


}
