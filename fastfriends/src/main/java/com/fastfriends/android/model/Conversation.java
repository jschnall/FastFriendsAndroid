package com.fastfriends.android.model;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "conversation")
public class Conversation {
    public static final String CATEGORY_SENT = "SENT";
    public static final String CATEGORY_RECEIVED = "RECEIVED";
    public static final String CATEGORY_DRAFTS = "DRAFTS";

    public static final String ID = "id";
    public static final String MESSAGE = "message";
    public static final String SENDER = "sender";
    public static final String SENDER_NAME = "sender_name";
    public static final String SENDER_PORTRAIT = "sender_portrait";
    public static final String RECEIVER = "receiver";
    public static final String RECEIVER_NAME = "receiver_name";
    public static final String RECEIVER_PORTRAIT = "receiver_portrait";

    public static final String OPENED = "opened";
    public static final String REPLIED = "replied";
    public static final String SENT = "sent";
    public static final String CREATED = "created";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(MESSAGE)
    @DatabaseField(columnName = MESSAGE)
    private String mMessage = null;

    @SerializedName(SENDER)
    @DatabaseField(columnName = SENDER)
    private long mSender;

    @SerializedName(SENDER_NAME)
    @DatabaseField(columnName = SENDER_NAME)
    private String mSenderName = null;

    @SerializedName(SENDER_PORTRAIT)
    @DatabaseField(columnName = SENDER_PORTRAIT)
    private String mSenderPortrait = null;

    @SerializedName(RECEIVER)
    @DatabaseField(columnName = RECEIVER)
    private long mReceiver;

    @SerializedName(RECEIVER_NAME)
    @DatabaseField(columnName = RECEIVER_NAME)
    private String mReceiverName = null;

    @SerializedName(RECEIVER_PORTRAIT)
    @DatabaseField(columnName = SENDER_PORTRAIT)
    private String mReceiverPortrait = null;

    @SerializedName(OPENED)
    @DatabaseField(columnName = OPENED)
    private Date mOpened;

    @SerializedName(REPLIED)
    @DatabaseField(columnName = REPLIED)
    private boolean mReplied;

    @SerializedName(SENT)
    @DatabaseField(columnName = SENT)
    private Date mSent = null;

    @SerializedName(CREATED)
    @DatabaseField(columnName = CREATED)
    private Date mCreated = null;


    public Conversation() {
        // Used by ORMLite
    }

    public Conversation(String message, long receiver) {
        mMessage = message;
        mReceiver = receiver;
    }

    private Date readDate(Parcel in) {
        int exists = in.readInt();
        if (exists == 0) {
            return null;
        } else {
            return new Date(in.readLong());
        }
    }

    private void writeDate(Parcel out, Date date) {
        if (date == null) {
            out.writeInt(0);
        } else {
            out.writeInt(1);
            out.writeLong(date.getTime());
        }
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public long getSender() {
        return mSender;
    }

    public void setSender(long sender) {
        mSender = sender;
    }

    public String getSenderName() {
        return mSenderName;
    }

    public void setSenderName(String senderName) {
        mSenderName = senderName;
    }

    public long getReceiver() {
        return mReceiver;
    }

    public String getReceiverName() {
        return mReceiverName;
    }

    public void setReceiverName(String receiverName) {
        mReceiverName = receiverName;
    }

    public void setReceiver(long receiver) {
        mReceiver = receiver;
    }

    public boolean isOpened() {
        return mOpened != null;
    }

    public Date getOpened() {
        return mOpened;
    }

    public void setOpened(Date opened) {
        mOpened = opened;
    }

    public boolean isReplied() {
        return mReplied;
    }

    public void setReplied(boolean replied) {
        mReplied = replied;
    }

    public Date getSent() {
        return mSent;
    }

    public void setSent(Date sent) {
        mSent = sent;
    }

    public Date getCreated() {
        return mCreated;
    }

    public void setCreated(Date created) {
        mCreated = created;
    }

    public String getSenderPortrait() {
        return mSenderPortrait;
    }

    public void setSenderPortrait(String senderPortrait) {
        mSenderPortrait = senderPortrait;
    }

    public String getReceiverPortrait() {
        return mReceiverPortrait;
    }

    public void setReceiverPortrait(String receiverPortrait) {
        mReceiverPortrait = receiverPortrait;
    }
}
