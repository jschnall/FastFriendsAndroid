package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "comment")
public class Comment implements Parcelable {
    public static final String ID = "id";
    public static final String MESSAGE = "message";
    public static final String OWNER = "owner";

    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_PORTRAIT = "owner_portrait";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String EVENT = "event";
    public static final String EVENT_NAME = "event_name";
    public static final String PLAN = "plan";
    public static final String PLAN_OWNER_NAME = "plan_owner_name";
    public static final String MENTIONS = "mentions";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(MESSAGE)
    @DatabaseField(columnName = MESSAGE)
    private String mMessage = null;

    @SerializedName(OWNER)
    @DatabaseField(columnName = OWNER)
    private long mOwnerId;

    @SerializedName(OWNER_NAME)
    @DatabaseField(columnName = OWNER_NAME)
    private String mOwnerName = null;

    @SerializedName(OWNER_PORTRAIT)
    @DatabaseField(columnName = OWNER_PORTRAIT)
    private String mOwnerPortrait = null;

    @SerializedName(CREATED)
    @DatabaseField(columnName = CREATED)
    private Date mCreated = null;

    @SerializedName(UPDATED)
    @DatabaseField(columnName = UPDATED)
    private Date mUpdated = null;

    @SerializedName(EVENT)
    @DatabaseField(columnName = EVENT)
    private long mEventId;

    @SerializedName(EVENT_NAME)
    @DatabaseField(columnName = EVENT_NAME)
    private String mEventName;

    @SerializedName(PLAN)
    @DatabaseField(columnName = PLAN)
    private long mPlanId;

    @SerializedName(PLAN_OWNER_NAME)
    @DatabaseField(columnName = PLAN_OWNER_NAME)
    private String mPlanOwnerName;

    @SerializedName(MENTIONS)
    private List<Mention> mMentions;

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };


    public Comment() {
        // Used by ORMLite
    }

    public Comment(String message) {
        mMessage = message;
    }

    public Comment(Parcel in) {
        mId = in.readLong();
        mMessage = in.readString();
        mOwnerId = in.readLong();
        mOwnerName = in.readString();
        mOwnerPortrait = in.readString();
        mCreated = ParcelHelper.readDate(in);
        mUpdated = ParcelHelper.readDate(in);
        mEventName = in.readString();
    }

    public Comment(Comment comment) {
        copy(comment);
    }

    // TODO review  models and decide if we should provide a separate deep copy
    public void copy(Comment comment) {
        mId = comment.mId;
        mMessage = comment.mMessage;
        mOwnerId = comment.mOwnerId;
        mOwnerName = comment.mOwnerName;
        mOwnerPortrait = comment.mOwnerPortrait;
        if (comment.mCreated != null) {
            mCreated = new Date(comment.mCreated.getTime());
        }
        if (comment.mCreated != null) {
            mUpdated = new Date(comment.mUpdated.getTime());
        }
        mEventName = comment.mEventName;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mId);
        out.writeString(mMessage);
        out.writeLong(mOwnerId);
        out.writeString(mOwnerName);
        out.writeString(mOwnerPortrait);
        ParcelHelper.writeDate(out, mCreated);
        ParcelHelper.writeDate(out, mUpdated);
        out.writeString(mEventName);
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

    public String getOwnerImage() {
        return mOwnerPortrait;
    }

    public void setOwnerImage(String ownerImage) {
        mOwnerPortrait = ownerImage;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public long getOwnerId() {
        return mOwnerId;
    }

    public void setOwner(long ownerId) {
        mOwnerId = ownerId;
    }

    public String getOwnerName() {
        return mOwnerName;
    }

    public void setOwnerName(String ownerName) {
        mOwnerName = ownerName;
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

    public long getEventId() {
        return mEventId;
    }

    public void setEventId(long eventId) {
        mEventId = eventId;
    }

    public String getEventName() {
        return mEventName;
    }

    public void setEventName(String eventName) {
        mEventName = eventName;
    }

    public long getPlanId() {
        return mPlanId;
    }

    public void setPlanId(long planId) {
        mPlanId = planId;
    }

    public List<Mention> getMentions() {
        return mMentions;
    }

    public void setMentions(List<Mention> mentions) {
        mMentions = mentions;
    }

    public String getPlanOwnerName() {
        return mPlanOwnerName;
    }

    public void setPlanOwnerName(String planOwnerName) {
        mPlanOwnerName = planOwnerName;
    }
}
