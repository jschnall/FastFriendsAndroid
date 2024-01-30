package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;

import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
public class EventMember implements Parcelable {
    // Member status
    public static final String REQUESTED = "REQUESTED";
    public static final String INVITED = "INVITED";
    public static final String ACCEPTED = "ACCEPTED";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String DISPLAY_NAME = "display_name";
    public static final String PORTRAIT = "portrait";
    public static final String STATUS = "status";
    public static final String VIEWED_EVENT = "viewed_event";
    public static final String CHECKED_IN = "checked_in";
    public static final String MUTUAL_FRIEND_COUNT = "mutual_friend_count";
    public static final String INVITER = "inviter";
    public static final String INVITER_NAME = "inviter_name";
    public static final String INVITER_PORTRAIT = "inviter_portrait";
    public static final String FRIEND = "friend";
    public static final String CLOSE = "close";

    @SerializedName(ID)
    private long mId;

    @SerializedName(USER_ID)
    private long mUserId;

    @SerializedName(DISPLAY_NAME)
    private String mDisplayName = null;

    @SerializedName(PORTRAIT)
    private String mPortrait = null;

    @SerializedName(STATUS)
    private String mStatus = null;

    @SerializedName(VIEWED_EVENT)
    private Date mViewedEvent = null;

    @SerializedName(CHECKED_IN)
    private Date mCheckedIn = null;

    @SerializedName(MUTUAL_FRIEND_COUNT)
    private int mMutualFriendCount;

    @SerializedName(INVITER)
    private String mInviter;

    @SerializedName(INVITER_NAME)
    private String mInviterName;

    @SerializedName(INVITER_PORTRAIT)
    private String mInviterPortrait;

    @SerializedName(FRIEND)
    private boolean mFriend;

    @SerializedName(CLOSE)
    private boolean mClose;

    public static final Parcelable.Creator<EventMember> CREATOR = new Parcelable.Creator<EventMember>() {
        public EventMember createFromParcel(Parcel in) {
            return new EventMember(in);
        }

        public EventMember[] newArray(int size) {
            return new EventMember[size];
        }
    };


    public EventMember() {
        // Used by ORMLite
    }

    public EventMember(Parcel in) {
        mId = in.readLong();
        mUserId = in.readLong();
        mDisplayName = in.readString();
        mPortrait = in.readString();
        mStatus = in.readString();
        mViewedEvent = ParcelHelper.readDate(in);
        mCheckedIn = ParcelHelper.readDate(in);
    }

    public EventMember(EventMember eventMember) {
        copy(eventMember);
    }

    // Copy editable fields
    public void copy(EventMember eventMember) {
        mId = eventMember.mId;
        mUserId = eventMember.mUserId;
        mDisplayName = eventMember.mDisplayName;
        mPortrait = eventMember.mPortrait;
        mStatus = eventMember.mStatus;
        mViewedEvent = eventMember.mViewedEvent;
        mCheckedIn = eventMember.mCheckedIn;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mId);
        out.writeLong(mUserId);
        out.writeString(mDisplayName);
        out.writeString(mPortrait);
        out.writeString(mStatus);
        ParcelHelper.writeDate(out, mViewedEvent);
        ParcelHelper.writeDate(out, mCheckedIn);
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

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        mUserId = userId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public Date getViewedEvent() {
        return mViewedEvent;
    }

    public void setViewedEvent(Date viewedEvent) {
        mViewedEvent = viewedEvent;
    }

    public Date getCheckedIn() {
        return mCheckedIn;
    }

    public void setCheckedIn(Date checkedIn) {
        mCheckedIn = checkedIn;
    }

    public int getMutualFriendCount() {
        return mMutualFriendCount;
    }

    public void setMutualFriendCount(int mutualFriendCount) {
        mMutualFriendCount = mutualFriendCount;
    }

    public String getInviter() {
        return mInviter;
    }

    public void setInviter(String inviter) {
        mInviter = inviter;
    }

    public String getInviterName() {
        return mInviterName;
    }

    public void setInviterName(String inviterName) {
        mInviterName = inviterName;
    }

    public String getInviterPortrait() {
        return mInviterPortrait;
    }

    public void setInviterPortrait(String inviterPortrait) {
        mInviterPortrait = inviterPortrait;
    }

    public boolean isFriend() {
        return mFriend;
    }

    public void setFriend(boolean friend) {
        mFriend = friend;
    }

    public boolean isClose() {
        return mClose;
    }

    public void setClose(boolean close) {
        mClose = close;
    }
}
