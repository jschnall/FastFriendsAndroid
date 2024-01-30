package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
public class Contact {
    public static final String USER_ID = "user_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String PORTRAIT = "portrait";

    @SerializedName(USER_ID)
    private long mUserId;

    @SerializedName(FIRST_NAME)
    private String mFirstName = null;

    @SerializedName(LAST_NAME)
    private String mLastName = null;

    @SerializedName(PORTRAIT)
    private String mPortrait = null;

    public Contact() {
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long id) {
        mUserId = id;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }
}
