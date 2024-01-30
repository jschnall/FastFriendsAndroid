package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "friend")
public class Friend {
    // Filter Categories
    public static final String CATEGORY_NAME = "NAME";
    public static final String CATEGORY_FRIEND = "FRIEND";
    public static final String CATEGORY_RECENT = "RECENT";
    public static final String CATEGORY_FREQUENT = "FREQUENT";

    public static final String ID = "id";
    public static final String OWNER_ID = "owner";
    public static final String USER_ID = "user";
    public static final String USER_NAME = "user_name";
    public static final String PORTRAIT = "portrait";
    public static final String CLOSE = "close"; // Close friend
    public static final String IMPORTED = "imported"; // Originally imported from contacts
    public static final String MUTUAL_FRIEND_COUNT = "mutual_friend_count";


    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(OWNER_ID)
    @DatabaseField(columnName = OWNER_ID)
    private long mOwnerId;

    @SerializedName(USER_ID)
    @DatabaseField(columnName = USER_ID)
    private long mUserId;

    @SerializedName(USER_NAME)
    @DatabaseField(columnName = USER_NAME)
    private String mUserName = null;

    @SerializedName(PORTRAIT)
    @DatabaseField(columnName = PORTRAIT)
    private String mPortrait = null;

    @SerializedName(CLOSE)
    @DatabaseField(columnName = CLOSE)
    private boolean mClose = false;

    @SerializedName(IMPORTED)
    @DatabaseField(columnName = IMPORTED)
    private boolean mImported = false;

    @SerializedName(MUTUAL_FRIEND_COUNT)
    @DatabaseField(columnName = MUTUAL_FRIEND_COUNT)
    private int mMutualFriendCount;

    public Friend() {
        // Used by ORMLite
    }

    public Friend(long ownerId, long userId, boolean close) {
        mOwnerId = ownerId;
        mUserId = userId;
        mClose = close;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getmOwnerId() {
        return mOwnerId;
    }

    public void setmOwnerId(long mOwnerId) {
        this.mOwnerId = mOwnerId;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long id) {
        mUserId = id;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    public boolean isImported() {
        return mImported;
    }

    public void setImported(boolean imported) {
        mImported = imported;
    }

    public boolean isClose() {
        return mClose;
    }

    public void setClose(boolean close) {
        mClose = close;
    }

    public int getMutualFriendCount() {
        return mMutualFriendCount;
    }

    public void setMutualFriendCount(int mutualFriendCount) {
        mMutualFriendCount = mutualFriendCount;
    }
}
