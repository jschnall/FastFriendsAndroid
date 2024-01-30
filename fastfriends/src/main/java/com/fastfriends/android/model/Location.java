package com.fastfriends.android.model;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jschnall on 2/17/14.
 */
public class Location implements Parcelable {
    private final static String LOGTAG = Location.class.getSimpleName();

    public static final String NAME = "name";
    // public static final String ALTITUDE = "altitude";
    public static final String SUB_THOROUGHFARE = "sub_thoroughfare"; // building
    public static final String THOROUGHFARE = "thoroughfare"; // street
    public static final String SUB_LOCALITY = "sub_locality";
    public static final String LOCALITY = "locality"; // city
    public static final String SUB_ADMIN_AREA = "sub_admin_area";
    public static final String ADMIN_AREA = "admin_area"; // state
    public static final String POSTAL_CODE = "postal_code"; // zipcode
    public static final String LOCALE = "locale"; // country and language
    public static final String POINT = "point"; // point containing latitude and longitude

    @SerializedName(NAME)
    private String mName;

    @SerializedName(SUB_THOROUGHFARE)
    private String mSubThoroughfare;

    @SerializedName(THOROUGHFARE)
    private String mThoroughfare;

    @SerializedName(SUB_LOCALITY)
    private String mSubLocality;

    @SerializedName(LOCALITY)
    private String mLocality;

    @SerializedName(SUB_ADMIN_AREA)
    private String mSubAdminArea;

    @SerializedName(ADMIN_AREA)
    private String mAdminArea;

    @SerializedName(POSTAL_CODE)
    private String mPostalCode;

    @SerializedName(LOCALE)
    private String mLocaleStr;

    @SerializedName(POINT)
    private Point mPoint = new Point();

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };


    public Location() {
    }

    public Location(Address address) {
        mName = address.getFeatureName();
        mPoint = new Point(address.getLatitude(), address.getLongitude());
        mSubThoroughfare = address.getSubThoroughfare();
        mThoroughfare = address.getThoroughfare();
        mSubLocality = address.getSubLocality();
        mLocality = address.getLocality();
        mSubAdminArea = address.getSubAdminArea();
        mAdminArea = address.getAdminArea();
        mPostalCode = address.getPostalCode();
        mLocaleStr = address.getLocale().toString();
    }

    public Location(Parcel in) {
        mName = in.readString();
        mPoint = (Point) in.readParcelable(Location.class.getClassLoader());
        mSubThoroughfare = in.readString();
        mThoroughfare = in.readString();
        mSubLocality = in.readString();
        mLocality = in.readString();
        mSubAdminArea = in.readString();
        mAdminArea = in.readString();
        mPostalCode = in.readString();
        mLocaleStr = in.readString();
    }

    public Location(Location location) {
        copy(location);
    }

    public void copy(Location location) {
        mName = location.mName;
        mPoint = location.mPoint;
        mSubThoroughfare = location.mSubThoroughfare;
        mThoroughfare = location.mThoroughfare;
        mSubLocality = location.mSubLocality;
        mLocality = location.mLocality;
        mSubAdminArea = location.mSubAdminArea;
        mAdminArea = location.mAdminArea;
        mPostalCode= location.mPostalCode;
        mLocaleStr = location.mLocaleStr;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeParcelable(mPoint, 0);
        out.writeString(mSubThoroughfare);
        out.writeString(mThoroughfare);
        out.writeString(mSubLocality);
        out.writeString(mLocality);
        out.writeString(mSubAdminArea);
        out.writeString(mAdminArea);
        out.writeString(mPostalCode);
        out.writeString(mLocaleStr);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Point getPoint() {
        return mPoint;
    }

    public void setPoint(Point point) {
        mPoint = point;
    }

    public String getSubThoroughfare() {
        return mSubThoroughfare;
    }

    public void setSubThoroughfare(String subThoroughfare) {
        mSubThoroughfare = subThoroughfare;
    }

    public String getThoroughfare() {
        return mThoroughfare;
    }

    public void setThoroughfare(String thoroughfare) {
        mThoroughfare = thoroughfare;
    }

    public String getSubLocality() {
        return mSubLocality;
    }

    public void setSubLocality(String subLocality) {
        mSubLocality = subLocality;
    }

    public String getLocality() {
        return mLocality;
    }

    public void setLocality(String locality) {
        mLocality = locality;
    }

    public String getSubAdminArea() {
        return mSubAdminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        mSubAdminArea = subAdminArea;
    }

    public String getAdminArea() {
        return mAdminArea;
    }

    public void setAdminArea(String adminArea) {
        mAdminArea = adminArea;
    }

    public String getPostalCode() {
        return mPostalCode;
    }

    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    public String getLocaleStr() {
        return mLocaleStr;
    }

    public void setLocaleStr(String localeStr) {
        mLocaleStr = localeStr;
    }

    public String getStreetAddress() {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(mName) && !mName.equals(mSubThoroughfare)) {
            builder.append(mName);
            if (!TextUtils.isEmpty(mThoroughfare)) {
                builder.append(", ");
                builder.append(mThoroughfare);
            }
        } else if (!TextUtils.isEmpty(mSubThoroughfare)){
            builder.append(mSubThoroughfare);
            if (!TextUtils.isEmpty(mThoroughfare)) {
                builder.append(" ");
                builder.append(mThoroughfare);
            }
        }

        if (!TextUtils.isEmpty(mSubLocality)) {
            builder.append(", ");
            builder.append(mSubLocality);

            if (!TextUtils.isEmpty(mLocality)) {
                builder.append(" ");
                builder.append(mLocality);
            }
        } else if (!TextUtils.isEmpty(mLocality)) {
            builder.append(", ");
            builder.append(mLocality);
        }

        if (!TextUtils.isEmpty(mPostalCode)) {
            builder.append(" ");
            builder.append(mPostalCode);
        }

        return builder.toString();
    }
}
