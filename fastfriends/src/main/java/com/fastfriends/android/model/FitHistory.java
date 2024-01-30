package com.fastfriends.android.model;

import com.fastfriends.android.R;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "tag")
public class FitHistory {
    public static final String ID = "id";
    public static final String OWNER_ID = "owner";
    public static final String PERIOD = "period";
    public static final String ACTIVITY = "activity";
    public static final String ACTIVITY_ID = "activityId";
    public static final String UPDATED = "updated";


    public static final String FIELD = "field";
    public static final String VALUE = "value";
    //public static final String UNITS = "units";

    // Tracked activities
    public static final String WALKING = "walking";
    public static final String RUNNING = "running";
    public static final String BIKING = "biking";
    public static final String SWIMMING = "swimming";
    public static final String ROCK_CLIMBING = "rock_climbing";
    public static final String AEROBICS = "aerobics";
    public static final String YOGA = "yoga";

    // Period
    public static final String WEEK = "week";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    // Fields stored
    public static final String DURATION = "duration";
    public static final String STEPS = "steps";

    // Physical distance moved.  Won't be accurate for treadmill, stationary bike, etc.
    // Instead, calculate from steps
    // Assuming an average 32” stride length, 2000 steps is 1mi, or 1.61Km
    public static final String DISTANCE = "distance";


    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(OWNER_ID)
    @DatabaseField(columnName = OWNER_ID)
    private long mOwnerId;

    @SerializedName(PERIOD)
    @DatabaseField(columnName = PERIOD)
    private String mPeriod;

    @SerializedName(ACTIVITY)
    @DatabaseField(columnName = ACTIVITY)
    private String mActivity;

    @SerializedName(ACTIVITY_ID)
    @DatabaseField(columnName = ACTIVITY_ID)
    private int mActivityId;

    @SerializedName(FIELD)
    @DatabaseField(columnName = FIELD)
    private String mField;

    @SerializedName(VALUE)
    @DatabaseField(columnName = VALUE)
    private String mValue;

    @SerializedName(UPDATED)
    @DatabaseField(columnName = UPDATED)
    private Date mUpdated = null;

    //@SerializedName(UNITS)
    //@DatabaseField(columnName = UNITS)
    //private String mUnits;

    public FitHistory() {
        // Used by ORMLite
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getOwner() {
        return mOwnerId;
    }

    public void setOwnerId(long ownerId) {
        mOwnerId = ownerId;
    }

    public String getPeriod() {
        return mPeriod;
    }

    public void setPeriod(String period) {
        mPeriod = period;
    }

    public String getActivity() {
        return mActivity;
    }

    public void setActivity(String activity) {
        mActivity = activity;
    }

    public int getActivityId() {
        return mActivityId;
    }

    public void setActivityId(int activityId) {
        mActivityId = activityId;
    }

    public String getField() {
        return mField;
    }

    public void setField(String field) {
        mField = field;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public Date getUpdated() {
        return mUpdated;
    }

    public void setUpdated(Date updated) {
        mUpdated = updated;
    }

    //public String getUnits() {
    //    return mUnits;
    //}

    //public void setUnits(String units) {
    //    mUnits = units;
    //}

    public static boolean isTrackedActivity(String activityType) {
        return WALKING.equals(activityType) || RUNNING.equals(activityType) ||
                BIKING.equals(activityType) || SWIMMING.equals(activityType) ||
                ROCK_CLIMBING.equals(activityType) || AEROBICS.equals(activityType) ||
                YOGA.equals(activityType);
    }

    public static int getIconResource(String activityType) {
        if (WALKING.equalsIgnoreCase(activityType)) {
            return R.drawable.ic_walk;
        } else if (RUNNING.equalsIgnoreCase(activityType)) {
            return R.drawable.ic_run;
        } else if (BIKING.equalsIgnoreCase(activityType)) {
            return R.drawable.ic_bike;
        } else if (SWIMMING.equalsIgnoreCase(activityType)) {
            return R.drawable.ic_swim;
        } else if (ROCK_CLIMBING.equalsIgnoreCase(activityType)) {
            return R.drawable.ic_climb;
        } else if (AEROBICS.equalsIgnoreCase(activityType)) {
            //return R.drawable.ic_aerobics;
        } else if (YOGA.equalsIgnoreCase(activityType)) {
            return R.drawable.ic_yoga;
        }
        return R.drawable.ic_fit_default;
    }
}
