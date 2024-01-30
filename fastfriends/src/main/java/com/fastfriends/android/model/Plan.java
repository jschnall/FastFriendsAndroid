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
@DatabaseTable(tableName = "event")
public class Plan extends Binder implements Parcelable {

    // Filter Categories
    public static final String CATEGORY_FRIENDS = "FRIENDS";
    public static final String CATEGORY_NEWEST = "NEWEST";
    public static final String CATEGORY_RECOMMENDED = "RECOMMENDED";

    // Fields
    public static final String ID = "id";
    public static final String OWNER = "owner";
    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_PORTRAIT = "owner_portrait";
    public static final String UPDATED = "updated";
    public static final String CREATED = "created";
    public static final String TEXT = "text";
    public static final String LOCATION = "location";
    public static final String COMMENTS = "comments";
    public static final String LANGUAGE = "language";
    public static final String MENTIONS = "mentions";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(OWNER)
    @DatabaseField(columnName = OWNER)
    private long mOwnerId;

    @SerializedName(OWNER_NAME)
    @DatabaseField(columnName = OWNER_NAME)
    private String mOwnerName = null;

    @SerializedName(OWNER_PORTRAIT)
    @DatabaseField(columnName = OWNER_PORTRAIT)
    private String mOwnerPortrait = null;

    @SerializedName(UPDATED)
    @DatabaseField(columnName = UPDATED)
    private Date mUpdated = null;

    @SerializedName(CREATED)
    @DatabaseField(columnName = CREATED)
    private Date mCreated = null;

    @SerializedName(TEXT)
    @DatabaseField(columnName = TEXT)
    private String mText = null;

    @SerializedName(LOCATION)
    @DatabaseField(columnName = LOCATION)
    private Location mLocation = new Location();

    @SerializedName(COMMENTS)
    @DatabaseField(columnName = COMMENTS)
    private List<Comment> mComments = new ArrayList<Comment>();

    @SerializedName(LANGUAGE)
    @DatabaseField(columnName = LANGUAGE)
    private String mLanguage = null;

    @DatabaseField(columnName = MENTIONS)
    private List<Mention> mMentions;

    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };


    public Plan() {
        // Used by ORMLite
    }

    public Plan(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mOwnerId = in.readLong();
        mOwnerName = in.readString();
        mOwnerPortrait = in.readString();
        mUpdated = ParcelHelper.readDate(in);
        mCreated = ParcelHelper.readDate(in);
        mText = in.readString();
        mLocation = (Location) in.readParcelable(Event.class.getClassLoader());
        //mComments = new ArrayList<Comment>();
        mLanguage = in.readString();
    }

    public Plan(Plan plan) {
        copy(plan);
    }

    public void copy(Plan plan) {
        mId = plan.mId;
        mOwnerId = plan.mOwnerId;
        mOwnerName = plan.mOwnerName;
        mOwnerPortrait = plan.mOwnerPortrait;
        mUpdated = plan.mUpdated;
        mCreated = plan.mCreated;
        mText = plan.mText;
        mLocation = plan.mLocation;
        //if (event.mComments != null) {
        //    mComments = new ArrayList<Comment>(event.mComments);
        //}
        mLanguage = plan.getLanguage();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeLong(mOwnerId);
        out.writeString(mOwnerName);
        out.writeString(mOwnerPortrait);
        ParcelHelper.writeDate(out, mUpdated);
        ParcelHelper.writeDate(out, mCreated);
        out.writeString(mText);
        out.writeParcelable(mLocation, 0);
        //out.writeTypedList(mComments);
        out.writeString(mLanguage);
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

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public Date getCreated() {
        return mCreated;
    }

    public void setCreated(Date created) {
        mCreated = created;
    }

    public Date getUpdated() {
        return mUpdated;
    }

    public void setUpdated(Date updated) {
        mUpdated = updated;
    }

    public List<Comment> getComments() {
        return mComments;
    }

    public void setComments(List<Comment> comments) {
        mComments = comments;
    }

    public Long getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(Long ownerId) {
        mOwnerId = ownerId;
    }

    public String getOwnerName() {
        return mOwnerName;
    }

    public void setOwnerName(String ownerName) {
        mOwnerName = ownerName;
    }

    public String getOwnerPortrait() {
        return mOwnerPortrait;
    }

    public void setOwnerPortrait(String ownerPortrait) {
        mOwnerPortrait = ownerPortrait;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public List<Mention> getMentions() {
        return mMentions;
    }

    public void setMentions(List<Mention> mentions) {
        mMentions = mentions;
    }

    public boolean isOwner(long userId) {
        return mOwnerId == userId;
    }

}
