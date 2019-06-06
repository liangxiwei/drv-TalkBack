package com.example.jrd48.chat.group;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/8.
 */

public class TeamInfoList implements Parcelable {
    private List<TeamInfo> mTeamInfo;

    public static final Creator<TeamInfoList> CREATOR = new Creator<TeamInfoList>() {
        @Override
        public TeamInfoList createFromParcel(Parcel in) {
            List<TeamInfo> appl = new ArrayList<TeamInfo>();
            in.readTypedList(appl, TeamInfo.CREATOR);
            TeamInfoList a = new TeamInfoList();
            a.mTeamInfo = appl;
            return a;
        }

        @Override
        public TeamInfoList[] newArray(int size) {
            return new TeamInfoList[size];
        }
    };

    public List<TeamInfo> getmTeamInfo() {
        return mTeamInfo;
    }

    public void setmTeamInfo(List<TeamInfo> mTeamInfo) {
        this.mTeamInfo = mTeamInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(mTeamInfo);
    }
}
