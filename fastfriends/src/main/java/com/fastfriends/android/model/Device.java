package com.fastfriends.android.model;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.Settings;
import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;


/**
 * Created by jschnall on 1/20/14.
 */
public class Device implements Parcelable {
    public static final String ID = "id";
    public static final String OWNER = "owner";
    public static final String NAME = "name";
    public static final String DEVICE_ID = "device_id";
    public static final String TELEPHONY_ID = "telephony_id"; // IMEI
    public static final String GCM_REG_ID = "gcm_reg_id";
    public static final String NOTIFICATIONS = "notifications";
    public static final String MESSAGE_NOTIFICATIONS = "message_notifications";
    public static final String COMMENT_NOTIFICATIONS = "comment_notifications";
    public static final String EVENT_NOTIFICATIONS = "event_notifications";

    @SerializedName(ID)
    private long mId;

    @SerializedName(OWNER)
    private long mOwnerId;

    @SerializedName(NAME)
    private String mName;

    @SerializedName(DEVICE_ID)
    private String mDeviceId;

    @SerializedName(TELEPHONY_ID)
    private String mTelephonyId;

    @SerializedName(GCM_REG_ID)
    private String mGcmRegId;

    // Notification Settings
    @SerializedName(NOTIFICATIONS)
    private boolean mNotifications = true;

    @SerializedName(MESSAGE_NOTIFICATIONS)
    private boolean mMessageNotifications = true;

    @SerializedName(COMMENT_NOTIFICATIONS)
    private boolean mCommentNotifications = true;

    @SerializedName(EVENT_NOTIFICATIONS)
    private boolean mEventNotifications = true;


    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        public Device[] newArray(int size) {
            return new Device[size];
        }
    };


    public Device() {
    }

    public Device(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mOwnerId = in.readLong();
        mName = in.readString();
        mDeviceId = in.readString();
        mTelephonyId = in.readString();
        mGcmRegId = in.readString();
        mNotifications = ParcelHelper.readBoolean(in);
        mMessageNotifications = ParcelHelper.readBoolean(in);
        mCommentNotifications = ParcelHelper.readBoolean(in);
        mEventNotifications = ParcelHelper.readBoolean(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeLong(mOwnerId);
        out.writeString(mName);
        out.writeString(mDeviceId);
        out.writeString(mTelephonyId);
        out.writeString(mGcmRegId);
        ParcelHelper.writeBoolean(out, mNotifications);
        ParcelHelper.writeBoolean(out, mMessageNotifications);
        ParcelHelper.writeBoolean(out, mCommentNotifications);
        ParcelHelper.writeBoolean(out, mEventNotifications);
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

    public long getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(long ownerId) {
        mOwnerId = ownerId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public String getTelephonyId() {
        return mTelephonyId;
    }

    public void setTelephonyId(String telephonyId) {
        mTelephonyId = telephonyId;
    }

    public String getGcmRegId() {
        return mGcmRegId;
    }

    public void setGcmRegId(String gcmRegId) {
        mGcmRegId = gcmRegId;
    }

    public boolean isNotifications() {
        return mNotifications;
    }

    public void setNotifications(boolean notifications) {
        mNotifications = notifications;
    }

    public boolean isMessageNotifications() {
        return mMessageNotifications;
    }

    public void setMessageNotifications(boolean messageNotifications) {
        mMessageNotifications = messageNotifications;
    }

    public boolean isCommentNotifications() {
        return mCommentNotifications;
    }

    public void setCommentNotifications(boolean commentNotifications) {
        mCommentNotifications = commentNotifications;
    }

    public boolean isEventNotifications() {
        return mEventNotifications;
    }

    public void setEventNotifications(boolean eventNotifications) {
        mEventNotifications = eventNotifications;
    }

    public void loadSettings() {
        SharedPreferences prefs = Settings.getSharedPreferences();
        mId = prefs.getLong(Settings.DEVICE_ID, 0);
        mNotifications = prefs.getBoolean(Settings.NOTIFICATIONS, true);
        mMessageNotifications = prefs.getBoolean(Settings.MESSAGE_NOTIFICATIONS, true);
        mCommentNotifications = prefs.getBoolean(Settings.COMMENT_NOTIFICATIONS, true);
        mEventNotifications = prefs.getBoolean(Settings.EVENT_NOTIFICATIONS, true);
    }

    public void saveSettings() {
        SharedPreferences prefs = Settings.getSharedPreferences();
        prefs.edit()
            .putBoolean(Settings.NOTIFICATIONS, mNotifications)
            .putBoolean(Settings.MESSAGE_NOTIFICATIONS, mMessageNotifications)
            .putBoolean(Settings.COMMENT_NOTIFICATIONS, mCommentNotifications)
            .putBoolean(Settings.EVENT_NOTIFICATIONS, mEventNotifications)
            .commit();
    }

}
