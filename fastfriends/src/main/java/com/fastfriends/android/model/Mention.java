package com.fastfriends.android.model;

import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "mention")
public class Mention extends Binder implements Parcelable {

    // Fields
    public static final String ID = "id";
    public static final String NAME = "name"; // Name mentioned
    public static final String USER_ID = "user";
    public static final String USER_NAME = "user_name"; // User's current name

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(NAME)
    @DatabaseField(columnName = NAME)
    private String mName = null;

    @SerializedName(USER_ID)
    @DatabaseField(columnName = USER_ID)
    private long mUserId;

    @SerializedName(USER_NAME)
    @DatabaseField(columnName = USER_NAME)
    private String mUserName = null;


    public static final Creator<Mention> CREATOR = new Creator<Mention>() {
        public Mention createFromParcel(Parcel in) {
            return new Mention(in);
        }

        public Mention[] newArray(int size) {
            return new Mention[size];
        }
    };


    public Mention() {
        // Used by ORMLite
    }

    public Mention(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mUserId = in.readLong();
        mUserName = in.readString();
    }

    public Mention(Mention plan) {
        copy(plan);
    }

    public void copy(Mention mention) {
        mId = mention.mId;
        mName = mention.mName;
        mUserId = mention.mUserId;
        mUserName = mention.mUserName;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeString(mName);
        out.writeLong(mUserId);
        out.writeString(mUserName);
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

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Long getUserId() {
        return mUserId;
    }

    public void setUserId(Long userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }
}
