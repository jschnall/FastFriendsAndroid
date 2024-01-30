package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "event_filter")
public class PlanSearchFilter implements Parcelable {
    public static final String KILOMETERS = "km";
    public static final String MILES = "mi";

    public static int DEFAULT_DISTANCE = 25;

    //public static final String ID = "id";
    public static final String DISTANCE = "distance";
    public static final String DISTANCE_UNITS = "distance_units";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static final String SEARCH = "search";

    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "page_size";

    //@SerializedName(ID)
    //@DatabaseField(id = true, columnName = ID)
    //private long mId;

    @SerializedName(LATITUDE)
    @DatabaseField(columnName = LATITUDE)
    private double mLatitude;

    @SerializedName(LONGITUDE)
    @DatabaseField(columnName = LONGITUDE)
    private double mLongitude;

    @SerializedName(DISTANCE)
    @DatabaseField(columnName = DISTANCE)
    private int mDistance = DEFAULT_DISTANCE;

    @SerializedName(DISTANCE_UNITS)
    @DatabaseField(columnName = DISTANCE_UNITS)
    private String mDistanceUnits = MILES;

    @SerializedName(SEARCH)
    @DatabaseField(columnName = SEARCH)
    private String mSearch = "";

    @SerializedName(PAGE)
    @DatabaseField(columnName = PAGE)
    private int mPage = 1;

    @SerializedName(PAGE_SIZE)
    @DatabaseField(columnName = PAGE_SIZE)
    private int mPageSize = 20;

    public static final Creator<PlanSearchFilter> CREATOR = new Creator<PlanSearchFilter>() {
        public PlanSearchFilter createFromParcel(Parcel in) {
            return new PlanSearchFilter(in);
        }

        public PlanSearchFilter[] newArray(int size) {
            return new PlanSearchFilter[size];
        }
    };


    public PlanSearchFilter() {
    }

    public PlanSearchFilter(PlanSearchFilter eventFilter) {
        copy(eventFilter);
    }

    public PlanSearchFilter(Parcel in) {
        // Editable fields
        //mId = in.readLong();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mDistance = in.readInt();
        mDistanceUnits = in.readString();
    }

    public void copy(PlanSearchFilter eventFilter) {
        //mId = eventFilter.mId;
        mLatitude = eventFilter.mLatitude;
        mLongitude = eventFilter.mLongitude;
        mDistance = eventFilter.mDistance;
        mDistanceUnits = eventFilter.mDistanceUnits;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        //out.writeLong(mId);
        out.writeDouble(mLatitude);
        out.writeDouble(mLongitude);
        out.writeInt(mDistance);
        out.writeString(mDistanceUnits);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //public long getId() {
    //    return mId;
    //}

    //public void setId(long id) {
    //    mId = id;
    //}

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public String getDistanceUnits() {
        return mDistanceUnits;
    }

    public void setDistanceUnits(String distanceUnits) {
        mDistanceUnits = distanceUnits;
    }

    public String getSearch() {
        return mSearch;
    }

    public void setSearch(String search) {
        mSearch = search;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public int getPageSize() {
        return mPageSize;
    }

    public void setPageSize(int pageSize) {
        mPageSize = pageSize;
    }
}
