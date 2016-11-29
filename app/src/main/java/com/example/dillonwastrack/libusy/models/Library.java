package com.example.dillonwastrack.libusy.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by dillonwastrack on 11/3/16.
 */

public class Library implements Parcelable, Comparable {

    public String libraryName;
    public double lat;
    public double lng;
    public String placeId;
    public String phoneNumber;
    public String address;
    public String openNow;
    public String busyness;
    public String checkIns;
    public String hours;
    public String libraryId;
    public String totalCheckIns;
    public String veryBusyVotes;
    public String busyVotes;
    public String notBusyVotes;

    public Library() // empty constructor for use in NetworkManager
    {

    }

    public Library(Parcel in) {
        libraryName = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        placeId = in.readString();
        phoneNumber = in.readString();
        address = in.readString();
        openNow = in.readString();
        busyness = in.readString();
        checkIns = in.readString();
        hours = in.readString();
        libraryId = in.readString();
        totalCheckIns = in.readString();
        veryBusyVotes = in.readString();
        busyVotes = in.readString();
        notBusyVotes = in.readString();
    }

    public static final Creator<Library> CREATOR = new Creator<Library>() {
        @Override
        public Library createFromParcel(Parcel in) {
            return new Library(in);
        }

        @Override
        public Library[] newArray(int size) {
            return new Library[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(libraryName);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(placeId);
        parcel.writeString(phoneNumber);
        parcel.writeString(address);
        parcel.writeString(openNow);
        parcel.writeString(busyness);
        parcel.writeString(checkIns);
        parcel.writeString(hours);
        parcel.writeString(libraryId);
        parcel.writeString(totalCheckIns);
        parcel.writeString(veryBusyVotes);
        parcel.writeString(busyVotes);
        parcel.writeString(notBusyVotes);
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
