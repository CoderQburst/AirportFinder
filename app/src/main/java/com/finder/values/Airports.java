package com.finder.values;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Airports implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Airports createFromParcel(Parcel parcel) {
            return new Airports(parcel);
        }

        @Override
        public Airports[] newArray(int size) {
            return new Airports[size];
        }
    };
    private String id, airport, phoneNo, latitude, longitude;

    public Airports() {
    }

    private Airports(Parcel parcel) {
        readFromParcel(parcel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void putData(ArrayList<String> data) {
        this.id = data.get(0);
        this.airport = data.get(1);
        this.phoneNo = data.get(2);
        this.latitude = data.get(3);
        this.longitude = data.get(4);
    }

    public String getId() {
        return this.id;
    }

    public String getAirport() {
        return this.airport;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(airport);
        dest.writeString(phoneNo);
        dest.writeString(latitude);
        dest.writeString(longitude);
    }

    private void readFromParcel(Parcel parcel) {
        id = parcel.readString();
        airport = parcel.readString();
        phoneNo = parcel.readString();
        latitude = parcel.readString();
        longitude = parcel.readString();
    }
}