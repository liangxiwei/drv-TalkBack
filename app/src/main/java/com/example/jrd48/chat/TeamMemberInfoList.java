package com.example.jrd48.chat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/8.
 */

public class TeamMemberInfoList implements Parcelable{
    public static final Creator<TeamMemberInfoList> CREATOR = new Creator<TeamMemberInfoList>() {
        @Override
        public TeamMemberInfoList createFromParcel(Parcel in) {
            List<TeamMemberInfo> appl = new ArrayList<TeamMemberInfo>();
            in.readTypedList(appl,TeamMemberInfo.CREATOR);
            TeamMemberInfoList a = new TeamMemberInfoList();
            a.mTeamMemberInfo = appl;
            return a;
        }

        @Override
        public TeamMemberInfoList[] newArray(int size) {
            return new TeamMemberInfoList[size];
        }
    };
    private List<TeamMemberInfo> mTeamMemberInfo;

    public List<TeamMemberInfo> getmTeamMemberInfo() {
        return mTeamMemberInfo;
    }

    public void setmTeamMemberInfo(List<TeamMemberInfo> mTeamMemberInfo) {
        this.mTeamMemberInfo = mTeamMemberInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(mTeamMemberInfo);
    }
}
