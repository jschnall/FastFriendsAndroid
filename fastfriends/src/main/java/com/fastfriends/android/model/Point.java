package com.fastfriends.android.model;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jschnall on 2/17/14.
 */
public class Point implements Parcelable {
    private final static String LOGTAG = Point.class.getSimpleName();

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    // public static final String ALTITUDE = "altitude";

    @SerializedName(LATITUDE)
    private double mLatitude;

    @SerializedName(LONGITUDE)
    private double mLongitude;


    public static final Creator<Point> CREATOR = new Creator<Point>() {
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        public Point[] newArray(int size) {
            return new Point[size];
        }
    };


    public Point() {
    }

    public Point(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public Point(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    public Point(Point location) {
        copy(location);
    }

    public void copy(Point location) {
        mLatitude = location.mLatitude;
        mLongitude = location.mLongitude;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(mLatitude);
        out.writeDouble(mLongitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}
