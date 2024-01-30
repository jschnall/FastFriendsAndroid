package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "event_invite")
public class EventInvite implements Parcelable {
    public static final String ID = "id";
    public static final String SENDER_ID = "sender";
    public static final String SENDER_NAME = "sender_name";
    public static final String SENDER_PORTRAIT = "sender_portrait";
    public static final String SENT = "sent";
    public static final String EVENT_ID = "event";
    public static final String EVENT_NAME = "event_name";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(SENDER_ID)
    @DatabaseField(columnName = SENDER_ID)
    private long mSenderId;

    @SerializedName(SENDER_NAME)
    @DatabaseField(columnName = SENDER_NAME)
    private String mSenderName = null;

    @SerializedName(SENDER_PORTRAIT)
    @DatabaseField(columnName = SENDER_PORTRAIT)
    private String mSenderPortrait = null;

    @SerializedName(SENT)
    @DatabaseField(columnName = SENT)
    private Date mSent = null;

    @SerializedName(EVENT_ID)
    @DatabaseField(columnName = EVENT_ID)
    private long mEventId;

    @SerializedName(EVENT_NAME)
    @DatabaseField(columnName = EVENT_NAME)
    private String mEventName = null;

    public static final Creator<EventInvite> CREATOR = new Creator<EventInvite>() {
        public EventInvite createFromParcel(Parcel in) {
            return new EventInvite(in);
        }

        public EventInvite[] newArray(int size) {
            return new EventInvite[size];
        }
    };


    public EventInvite() {
        // Used by ORMLite
    }

    public EventInvite(Parcel in) {
        mId = in.readLong();
        mSenderId = in.readLong();
        mSenderName = in.readString();
        mSenderPortrait = in.readString();
        mSent = ParcelHelper.readDate(in);
        mEventId = in.readLong();
        mEventName = in.readString();
    }

    public EventInvite(EventInvite eventMember) {
        copy(eventMember);
    }

    // Copy editable fields
    public void copy(EventInvite eventInvite) {
        mId = eventInvite.mId;
        mSenderId = eventInvite.mSenderId;
        mSenderName = eventInvite.mSenderName;
        mSenderPortrait = eventInvite.mSenderPortrait;
        mSent = eventInvite.mSent;
        mEventId = eventInvite.mEventId;
        mEventName = eventInvite.mEventName;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mId);
        out.writeLong(mSenderId);
        out.writeString(mSenderName);
        out.writeString(mSenderPortrait);
        ParcelHelper.writeDate(out, mSent);
        out.writeLong(mEventId);
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

    public long getSenderId() {
        return mSenderId;
    }

    public void setSenderId(long senderId) {
        mSenderId = senderId;
    }

    public String getSenderName() {
        return mSenderName;
    }

    public void setSenderName(String senderName) {
        mSenderName = senderName;
    }

    public String getSenderPortrait() {
        return mSenderPortrait;
    }

    public void setSenderPortrait(String senderPortrait) {
        mSenderPortrait = senderPortrait;
    }

    public Date getSent() {
        return mSent;
    }

    public void setSent(Date sent) {
        mSent = sent;
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
}
