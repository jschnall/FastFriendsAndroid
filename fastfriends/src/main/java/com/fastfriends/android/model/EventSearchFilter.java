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
public class EventSearchFilter implements Parcelable {
    public static final String KILOMETERS = "km";
    public static final String MILES = "mi";

    public static int DEFAULT_DISTANCE = 25;

    //public static final String ID = "id";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";

    public static final String DISTANCE = "distance";
    public static final String DISTANCE_UNITS = "distance_units";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static final String MIN_PRICE = "min_price";
    public static final String MAX_PRICE = "max_price";
    public static final String CURRENCY_CODE = "currency_code";

    public static final String MIN_SIZE = "min_size";
    public static final String MAX_SIZE = "max_size";

    public static final String SEARCH = "search";

    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "page_size";

    //@SerializedName(ID)
    //@DatabaseField(id = true, columnName = ID)
    //private long mId;

    @SerializedName(START_DATE)
    @DatabaseField(columnName = START_DATE)
    private Date mStartDate = new Date();

    @SerializedName(END_DATE)
    @DatabaseField(columnName = END_DATE)
    private Date mEndDate = new Date();

    @SerializedName(MIN_PRICE)
    @DatabaseField(columnName = MIN_PRICE)
    private double mMinPrice = 0;

    @SerializedName(MAX_PRICE)
    @DatabaseField(columnName = MAX_PRICE)
    private double mMaxPrice = Price.MAX_AMOUNT;

    @SerializedName(CURRENCY_CODE)
    @DatabaseField(columnName = CURRENCY_CODE)
    private String mCurrencyCode = "usd";

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

    @SerializedName(MIN_SIZE)
    @DatabaseField(columnName = MIN_SIZE)
    private int mMinSize = 2;

    @SerializedName(MAX_SIZE)
    @DatabaseField(columnName = MAX_SIZE)
    private int mMaxSize = Event.MEMBER_LIMIT_MAX;

    @SerializedName(SEARCH)
    @DatabaseField(columnName = SEARCH)
    private String mSearch = "";

    @SerializedName(PAGE)
    @DatabaseField(columnName = PAGE)
    private int mPage = 1;

    @SerializedName(PAGE_SIZE)
    @DatabaseField(columnName = PAGE_SIZE)
    private int mPageSize = 20;

    public static final Creator<EventSearchFilter> CREATOR = new Creator<EventSearchFilter>() {
        public EventSearchFilter createFromParcel(Parcel in) {
            return new EventSearchFilter(in);
        }

        public EventSearchFilter[] newArray(int size) {
            return new EventSearchFilter[size];
        }
    };


    public EventSearchFilter() {
        initDates();
    }

    public EventSearchFilter(EventSearchFilter eventFilter) {
        copy(eventFilter);
    }

    public EventSearchFilter(Parcel in) {
        // Editable fields
        //mId = in.readLong();
        mStartDate = ParcelHelper.readDate(in);
        mEndDate = ParcelHelper.readDate(in);
        mMinPrice = in.readDouble();
        mMaxPrice = in.readDouble();
        mCurrencyCode = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mDistance = in.readInt();
        mDistanceUnits = in.readString();
        mMinSize = in.readInt();
        mMaxSize = in.readInt();
    }

    public void copy(EventSearchFilter eventFilter) {
        //mId = eventFilter.mId;
        if (eventFilter.mStartDate != null) {
            mStartDate = new Date(eventFilter.mStartDate.getTime());
        }
        if (eventFilter.mEndDate != null) {
            mEndDate = new Date(eventFilter.mEndDate.getTime());
        }
        mMinPrice = eventFilter.mMinPrice;
        mMaxPrice = eventFilter.mMaxPrice;
        mCurrencyCode = eventFilter.mCurrencyCode;
        mLatitude = eventFilter.mLatitude;
        mLongitude = eventFilter.mLongitude;
        mDistance = eventFilter.mDistance;
        mDistanceUnits = eventFilter.mDistanceUnits;
        mMinSize = eventFilter.mMinSize;
        mMaxSize = eventFilter.mMaxSize;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        //out.writeLong(mId);
        ParcelHelper.writeDate(out, mStartDate);
        ParcelHelper.writeDate(out, mEndDate);
        out.writeDouble(mMinPrice);
        out.writeDouble(mMaxPrice);
        out.writeString(mCurrencyCode);
        out.writeDouble(mLatitude);
        out.writeDouble(mLongitude);
        out.writeInt(mDistance);
        out.writeString(mDistanceUnits);
        out.writeInt(mMinSize);
        out.writeInt(mMaxSize);
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

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public double getMinPrice() {
        return mMinPrice;
    }

    public void setMinPrice(double minPrice) {
        mMinPrice = minPrice;
    }

    public double getMaxPrice() {
        return mMaxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        mMaxPrice = maxPrice;
    }

    public String getCurrencyCode() {
        return mCurrencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        mCurrencyCode = currencyCode;
    }

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

    public int getMinSize() {
        return mMinSize;
    }

    public void setMinSize(int minSize) {
        mMinSize = minSize;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
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

    public void initDates() {
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(mStartDate);
        endCalendar.add(Calendar.DATE, 7);
        mEndDate = endCalendar.getTime();
    }
}
