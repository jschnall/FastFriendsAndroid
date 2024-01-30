package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jschnall on 1/20/14.
 */
public class UserStatus {
    public static final String USER_ID = "id";
    public static final String ACTIVE = "is_active";
    public static final String PORTRAIT = "portrait";
    public static final String PORTRAIT_ID = "portrait_id";
    public static final String DISPLAY_NAME = "display_name";
    public static final String BIRTHDAY = "birthday";
    public static final String GENDER = "gender";
    public static final String INTERESTS = "interests";

    public static final String MESSAGE_COUNT = "message_count";
    public static final String UNREAD_MESSAGE_COUNT = "unread_message_count";
    public static final String MAX_MESSAGES = "max_messages";
    public static final String DRAFT_MESSAGE_COUNT = "draft_message_count";

    @SerializedName(USER_ID)
    private long mUserId;

    @SerializedName(ACTIVE)
    private boolean mActive;

    @SerializedName(PORTRAIT)
    private String mPortrait;

    @SerializedName(PORTRAIT_ID)
    private long mPortraitId;

    @SerializedName(BIRTHDAY)
    private String mBirthday;

    @SerializedName(GENDER)
    private String mGender;

    @SerializedName(INTERESTS)
    private List<String> mInterests = new ArrayList<String>();

    @SerializedName(DISPLAY_NAME)
    private String mDisplayName;

    @SerializedName(MESSAGE_COUNT)
    private int mMessageCount;

    @SerializedName(UNREAD_MESSAGE_COUNT)
    private int mUnreadMessageCount;

    @SerializedName(MAX_MESSAGES)
    private int mMaxMessages;

    @SerializedName(DRAFT_MESSAGE_COUNT)
    private int mDraftMessageCount;

    public UserStatus() {
        // Used by ORMLite
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        mUserId = userId;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public int getUnreadMessageCount() {
        return mUnreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        mUnreadMessageCount = unreadMessageCount;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    public long getPortraitId() {
        return mPortraitId;
    }

    public void setPortraitId(long portraitId) {
        mPortraitId = portraitId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getBirthday() {
        return mBirthday;
    }

    public void setBirthday(String birthday) {
        mBirthday = birthday;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public List<String> getInterests() {
        return mInterests;
    }

    public void setInterests(List<String> interests) {
        mInterests = interests;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public int getMessageCount() {
        return mMessageCount;
    }

    public void setMessageCount(int messageCount) {
        mMessageCount = messageCount;
    }

    public int getMaxMessages() {
        return mMaxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        mMaxMessages = maxMessages;
    }

    public int getDraftMessageCount() {
        return mDraftMessageCount;
    }

    public void setDraftMessageCount(int draftMessageCount) {
        mDraftMessageCount = draftMessageCount;
    }


}
