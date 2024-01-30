package com.fastfriends.android.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.Settings;
import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "user")
public class User implements Parcelable {
    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String DATE_JOINED = "date_joined";


    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(EMAIL)
    @DatabaseField(columnName = EMAIL)
    private String mEmail = null;

    @SerializedName(FIRST_NAME)
    @DatabaseField(columnName = FIRST_NAME)
    private String mFirstName = null;

    @SerializedName(LAST_NAME)
    @DatabaseField(columnName = LAST_NAME)
    private String mLastName = null;

    @SerializedName(DATE_JOINED)
    @DatabaseField(columnName = DATE_JOINED)
    private Date mDateJoined = null;

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public User() {
        // Used by ORMLite
    }

    public User(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mEmail = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mDateJoined = ParcelHelper.readDate(in);
    }

    public void copy(User user) {
        mId = user.mId;
        mEmail = user.mEmail;
        mFirstName = user.mFirstName;
        mLastName = user.mLastName;
        mDateJoined = user.mDateJoined;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeString(mEmail);
        out.writeString(mFirstName);
        out.writeString(mLastName);
        ParcelHelper.writeDate(out, mDateJoined);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
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

    public Date getDateJoined() {
        return mDateJoined;
    }

    public void setDateJoined(Date created) {
        mDateJoined = created;
    }


    public static boolean isCurrentUser(long userId) {
        long currentUserId = Settings.getSharedPreferences().getLong(Settings.USER_ID, 0);

        if (userId == currentUserId) {
            return true;
        }
        return false;
    }


}
