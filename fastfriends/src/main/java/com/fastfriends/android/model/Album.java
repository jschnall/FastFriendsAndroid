package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jschnall on 3/3/14.
 */
@DatabaseTable(tableName = "album")
public class Album implements Parcelable {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String OWNER = "owner";
    public static final String EVENT = "event";
    public static final String EVENT_OWNER = "event_owner";
    public static final String COVER = "cover";
    public static final String RESOURCES = "resources";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(NAME)
    @DatabaseField(columnName = NAME)
    private String mName = null;

    @SerializedName(OWNER)
    @DatabaseField(columnName = OWNER)
    private long mOwnerId;

    @SerializedName(EVENT)
    @DatabaseField(columnName = EVENT)
    private long mEventId;

    @SerializedName(EVENT_OWNER)
    @DatabaseField(columnName = EVENT_OWNER)
    private long mEventOwnerId;

    @SerializedName(COVER)
    @DatabaseField(columnName = COVER)
    private long mCover;

    @SerializedName(RESOURCES)
    @DatabaseField(columnName = RESOURCES)
    private List<Resource> mResources = null;

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public Album() {
        // Used by ORMLite
    }

    public Album(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mName = in.readString();
        mOwnerId = in.readLong();
        mEventId = in.readLong();
        mEventOwnerId = in.readLong();
        mCover = in.readLong();
        mResources = in.createTypedArrayList(Resource.CREATOR);
    }

    public Album(Album album) {
        copy(album);
    }

    public void copy(Album album) {
        mId = album.mId;
        mName = album.mName;
        mOwnerId = album.mOwnerId;
        mEventId = album.mEventId;
        mEventOwnerId = album.mEventOwnerId;
        mCover = album.mCover;
        mResources = new ArrayList<Resource>();
        for(Resource resource : album.mResources) {
            Resource newResource = new Resource();
            newResource.copy(resource);
            mResources.add(newResource);
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeString(mName);
        out.writeLong(mOwnerId);
        out.writeLong(mEventId);
        out.writeLong(mEventOwnerId);
        out.writeLong(mCover);
        out.writeTypedList(mResources);
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

    public long getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(long ownerId) {
        mOwnerId = ownerId;
    }

    public long getEventId() {
        return mEventId;
    }

    public void setEventId(long eventId) {
        mEventId = eventId;
    }

    public long getEventOwnerId() {
        return mEventOwnerId;
    }

    public void setEventOwnerId(long eventOwnerId) {
        mEventOwnerId = eventOwnerId;
    }

    public long getCover() {
        return mCover;
    }

    public void setCover(long cover) {
        mCover = cover;
    }

    public List<Resource> getResources() {
        return mResources;
    }

    public void setResources(List<Resource> resources) {
        mResources = resources;
    }
}
