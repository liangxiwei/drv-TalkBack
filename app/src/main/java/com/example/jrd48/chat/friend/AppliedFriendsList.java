package com.example.jrd48.chat.friend;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/6.
 */

public class AppliedFriendsList implements Parcelable {
    private List<AppliedFriends> appliedFriends;

    public List<AppliedFriends> getAppliedFriends() {
        return appliedFriends;
    }

    public void setAppliedFriends(List<AppliedFriends> appliedFriends) {
        this.appliedFriends = appliedFriends;
    }

    public static final Creator<AppliedFriendsList> CREATOR = new Creator<AppliedFriendsList>() {
        @Override
        public AppliedFriendsList createFromParcel(Parcel in) {
            List<AppliedFriends> appl = new ArrayList<AppliedFriends>();
            in.readTypedList(appl, AppliedFriends.CREATOR);
            AppliedFriendsList a = new AppliedFriendsList();
            a.appliedFriends = appl;
            return a;
        }

        @Override
        public AppliedFriendsList[] newArray(int size) {
            return new AppliedFriendsList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(appliedFriends);
    }
}
