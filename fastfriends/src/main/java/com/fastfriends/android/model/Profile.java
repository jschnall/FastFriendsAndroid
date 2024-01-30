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
@DatabaseTable(tableName = "profile")
public class Profile implements Parcelable {
    public static final String GENDER_MALE = "M";
    public static final String GENDER_FEMALE = "F";

    public static final String ID = "id";
    public static final String GENDER = "gender";
    public static final String BIRTHDAY = "birthday";
    public static final String DISPLAY_NAME = "display_name";
    public static final String ABOUT = "about";
    public static final String LOCATION = "location";
    public static final String PORTRAIT = "portrait";
    public static final String PORTRAIT_ID = "portrait_id";
    public static final String MENTIONS = "mentions";
    public static final String FRIEND = "friend";
    public static final String MUTUAL_FRIEND_COUNT = "mutual_friend_count";
    public static final String RELIABILITY = "reliability";

    // TODO: How you met them

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(GENDER)
    @DatabaseField(columnName = GENDER)
    private String mGender = null;

    @SerializedName(BIRTHDAY)
    @DatabaseField(columnName = BIRTHDAY)
    private Date mBirthday = null;

    @SerializedName(DISPLAY_NAME)
    @DatabaseField(columnName = DISPLAY_NAME)
    private String mDisplayName = null;

    @SerializedName(ABOUT)
    @DatabaseField(columnName = ABOUT)
    private String mAbout = null;

    @SerializedName(LOCATION)
    @DatabaseField(columnName = LOCATION)
    private String mLocation = null;

    @SerializedName(PORTRAIT)
    @DatabaseField(columnName = PORTRAIT)
    private String mPortrait = null;

    @SerializedName(PORTRAIT_ID)
    @DatabaseField(columnName = PORTRAIT_ID)
    private Long mPortraitId = null;

    @SerializedName(MENTIONS)
    private List<Mention> mMentions;

    @SerializedName(FRIEND)
    private boolean mFriend = false;

    @SerializedName(MUTUAL_FRIEND_COUNT)
    private int mMutualFriendCount = 0;

    @SerializedName(RELIABILITY)
    private int mReliability = 50;

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public Profile() {
        // Used by ORMLite
    }

    public Profile(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mGender = in.readString();
        mBirthday = ParcelHelper.readDate(in);
        mDisplayName = in.readString();
        mAbout = in.readString();
        mLocation = in.readString();
    }

    // Copy editable fields
    public void copy(Profile profile) {
        mId = profile.mId;
        mGender = profile.mGender;
        if (profile.mBirthday != null) {
            mBirthday = new Date(profile.mBirthday.getTime());
        }
        mDisplayName = profile.mDisplayName;
        mAbout = profile.mAbout;
        mLocation = profile.mLocation;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeString(mGender);
        ParcelHelper.writeDate(out, mBirthday);
        out.writeString(mDisplayName);
        out.writeString(mAbout);
        out.writeString(mLocation);
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

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public Date getBirthday() {
        return mBirthday;
    }

    public void setBirthday(Date birthday) {
        mBirthday = birthday;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String about) {
        mAbout = about;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    public Long getPortraitId() {
        return mPortraitId;
    }

    public void setPortraitId(Long portraitId) {
        mPortraitId = portraitId;
    }

    public List<Mention> getMentions() {
        return mMentions;
    }

    public void setMentions(List<Mention> mentions) {
        mMentions = mentions;
    }

    public boolean isFriend() {
        return mFriend;
    }

    public void setFriend(boolean friend) {
        mFriend = friend;
    }

    public int getMutualFriendCount() {
        return mMutualFriendCount;
    }

    public void setMutualFriendCount(int mutualFriendCount) {
        mMutualFriendCount = mutualFriendCount;
    }

    public int getReliability() {
        return mReliability;
    }

    public void setReliability(int reliability) {
        mReliability = reliability;
    }
}
