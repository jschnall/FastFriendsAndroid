package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by jschnall on 1/20/14.
 */
public class UserAttributeSet {
    public static final String USER_ID = "user_id";
    public static final String PREMIUM = "premium";
    public static final String NOTIFICATIONS = "notifications";

    @SerializedName(USER_ID)
    private long mUserId;

    @SerializedName(PREMIUM)
    private boolean mPremium;

    @SerializedName(NOTIFICATIONS)
    private boolean mNotifications;


    public UserAttributeSet() {
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        mUserId = userId;
    }

    public boolean isPremium() {
        return mPremium;
    }

    public void setPremium(boolean premium) {
        mPremium = premium;
    }

    public boolean isNotifications() {
        return mNotifications;
    }

    public void setNotifications(boolean notifications) {
        mNotifications = notifications;
    }
}

