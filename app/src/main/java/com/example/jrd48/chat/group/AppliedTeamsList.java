package com.example.jrd48.chat.group;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/6.
 */

public class AppliedTeamsList implements Parcelable {
    private List<AppliedTeams> appliedTeams;

    public static final Creator<AppliedTeamsList> CREATOR = new Creator<AppliedTeamsList>() {
        @Override
        public AppliedTeamsList createFromParcel(Parcel in) {
            List<AppliedTeams> appl = new ArrayList<AppliedTeams>();
            in.readTypedList(appl, AppliedTeams.CREATOR);
            AppliedTeamsList a = new AppliedTeamsList();
            a.appliedTeams = appl;
            return a;
        }

        @Override
        public AppliedTeamsList[] newArray(int size) {
            return new AppliedTeamsList[size];
        }
    };

    public List<AppliedTeams> getAppliedTeams() {
        return appliedTeams;
    }

    public void setAppliedTeams(List<AppliedTeams> appliedTeams) {
        this.appliedTeams = appliedTeams;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(appliedTeams);
    }
}
