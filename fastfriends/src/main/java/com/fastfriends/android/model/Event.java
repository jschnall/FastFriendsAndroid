package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastfriends.android.helper.ParcelHelper;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "event")
public class Event implements Parcelable {

    // Assumed length of events when end date is not set
    public static final int DEFAULT_LENGTH = 4; // hrs
    // Offset from event start when checkins begin
    public static final int CHECKIN_START = -30; // mins

    public static final int MEMBER_LIMIT_MIN = 2;
    public static final int MEMBER_LIMIT_MAX = 2147483647;

    // Join policies
    public static final String OPEN = "OPEN";
    public static final String OWNER_APPROVAL = "OWNER_APPROVAL";
    public static final String INVITE_ONLY = "INVITE_ONLY";
    public static final String OWNER_INVITE_ONLY = "OWNER_INVITE_ONLY";
    public static final String FRIENDS_ONLY = "FRIENDS_ONLY";

    // Filter Categories
    public static final String CATEGORY_ATTENDING = "ATTENDING";
    public static final String CATEGORY_FRIENDS = "FRIENDS";
    public static final String CATEGORY_NEARBY = "NEARBY";
    public static final String CATEGORY_RECOMMENDED = "RECOMMENDED";

    // Fields
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String TAGS = "tags";
    public static final String ALBUM = "album";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String OWNER = "owner";
    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_IMAGE = "owner_image";
    public static final String MODIFIED = "modified";
    public static final String LOCATION = "location";
    public static final String PRICE = "price";
    public static final String JOIN_POLICY = "join_policy";
    public static final String MAX_MEMBERS = "max_members";
    public static final String IMAGE = "image";
    public static final String LANGUAGE = "language";
    public static final String MENTIONS = "mentions";
    public static final String MEMBER_COUNT = "member_count";
    public static final String INVITED_COUNT = "invited_count";
    public static final String REQUESTED_COUNT = "requested_count";
    public static final String FRIEND_COUNT = "friend_count";
    public static final String CLOSE_FRIEND_COUNT = "close_friend_count";
    public static final String CURRENT_USER_MEMBER = "current_user_member";
    public static final String CANCELED = "canceled";
    public static final String SOURCE = "source";
    public static final String DISTANCE = "distance";
    public static final String UNITS = "units";
    public static final String FRIEND_OF_OWNER = "friend_of_owner";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId;

    @SerializedName(NAME)
    @DatabaseField(columnName = NAME)
    private String mName = null;

    @SerializedName(LOCATION)
    //@DatabaseField(columnName = LOCATION)
    private Location mLocation = new Location();

    @SerializedName(DESCRIPTION)
    @DatabaseField(columnName = DESCRIPTION)
    private String mDescription = null;

    @SerializedName(START_DATE)
    @DatabaseField(columnName = START_DATE)
    private Date mStartDate = null;

    @SerializedName(END_DATE)
    @DatabaseField(columnName = END_DATE)
    private Date mEndDate = null;

    @SerializedName(TAGS)
    private List<String> mTagNames = new ArrayList<String>();

    @SerializedName(ALBUM)
    //@DatabaseField(columnName = ALBUM)
    private Album mAlbum = null;

    @SerializedName(CREATED)
    @DatabaseField(columnName = CREATED)
    private Date mCreated = null;

    @SerializedName(UPDATED)
    @DatabaseField(columnName = UPDATED)
    private Date mUpdated = null;

    @SerializedName(OWNER)
    @DatabaseField(columnName = OWNER)
    private long mOwnerId;

    @SerializedName(OWNER_NAME)
    @DatabaseField(columnName = OWNER_NAME)
    private String mOwnerName = null;

    @SerializedName(OWNER_IMAGE)
    @DatabaseField(columnName = OWNER_IMAGE)
    private String mOwnerImage = null;

    @SerializedName(MODIFIED)
    @DatabaseField(columnName = MODIFIED)
    private boolean mModified = false;

    @SerializedName(PRICE)
    //@DatabaseField(columnName = PRICE)
    private Price mPrice = new Price();

    @SerializedName(JOIN_POLICY)
    @DatabaseField(columnName = JOIN_POLICY)
    private String mJoinPolicy = OPEN;

    @SerializedName(MAX_MEMBERS)
    @DatabaseField(columnName = MAX_MEMBERS)
    private int mMaxMembers = MEMBER_LIMIT_MAX;

    @SerializedName(IMAGE)
    @DatabaseField(columnName = IMAGE)
    private String mImage = null;

    @SerializedName(LANGUAGE)
    @DatabaseField(columnName = LANGUAGE)
    private String mLanguage = "en";

    @SerializedName(MENTIONS)
    //@DatabaseField(columnName = MENTIONS)
    private List<Mention> mMentions;

    @SerializedName(MEMBER_COUNT)
    @DatabaseField(columnName = MEMBER_COUNT)
    private int mMemberCount;

    @SerializedName(INVITED_COUNT)
    @DatabaseField(columnName = INVITED_COUNT)
    private int mInvitedCount;

    @SerializedName(REQUESTED_COUNT)
    @DatabaseField(columnName = REQUESTED_COUNT)
    private int mRequestedCount;

    @SerializedName(FRIEND_COUNT)
    @DatabaseField(columnName = CLOSE_FRIEND_COUNT)
    private int mFriendCount;

    @SerializedName(CLOSE_FRIEND_COUNT)
    @DatabaseField(columnName = CLOSE_FRIEND_COUNT)
    private int mCloseFriendCount;

    @SerializedName(CURRENT_USER_MEMBER)
    //@DatabaseField(columnName = CURRENT_USER_MEMBER)
    private EventMember mCurrentUserMember;

    @SerializedName(CANCELED)
    @DatabaseField(columnName = CANCELED)
    private Date mCanceled = null;

    @SerializedName(SOURCE)
    @DatabaseField(columnName = SOURCE)
    private String mSource = null;

    @SerializedName(DISTANCE)
    @DatabaseField(columnName = DISTANCE)
    private double mDistance;

    @SerializedName(UNITS)
    @DatabaseField(columnName = UNITS)
    private String mUnits;

    @SerializedName(FRIEND_OF_OWNER)
    @DatabaseField(columnName = FRIEND_OF_OWNER)
    private boolean mFriendOfOwner;

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public Event() {
        // Used by ORMLite
    }

    public Event(Parcel in) {
        // Editable fields
        mId = in.readLong();
        mName = in.readString();
        mLocation = (Location) in.readParcelable(Event.class.getClassLoader());
        mDescription = in.readString();
        mStartDate = ParcelHelper.readDate(in);
        mEndDate = ParcelHelper.readDate(in);
        in.readStringList(mTagNames);
        mPrice = (Price) in.readParcelable((Event.class.getClassLoader()));
        mJoinPolicy = in.readString();
        mMaxMembers = in.readInt();

        //mAlbum = (Album) in.readParcelable((Event.class.getClassLoader()));
        //mComments = new ArrayList<Comment>();
        //in.readTypedList(mComments, Comment.CREATOR);
        //mCreated = ParcelHelper.readDate(in);
        //mUpdated = ParcelHelper.readDate(in);
        //mOwnerId = in.readLong();
        //mOwnerName = in.readString();
        //mOwnerImage = in.readString();
        //mMembers = new ArrayList<EventMember>();
        //in.readTypedList(mMembers, EventMember.CREATOR);
        //mModified = ParcelHelper.readBoolean(in);
        mLanguage = in.readString();
        mMemberCount = in.readInt();
        mInvitedCount = in.readInt();
        mRequestedCount = in.readInt();
        mFriendCount = in.readInt();
        mCloseFriendCount = in.readInt();
        mCanceled = ParcelHelper.readDate(in);
        mSource = in.readString();
    }

    public Event(Event event) {
        copy(event);
    }

    public void copy(Event event) {
        mId = event.mId;
        mName = event.mName;
        mLocation = event.mLocation;
        mDescription = event.mDescription;
        if (event.mStartDate != null) {
            mStartDate = new Date(event.mStartDate.getTime());
        }
        if (event.mEndDate != null) {
            mEndDate = new Date(event.mEndDate.getTime());
        }
        mTagNames.addAll(event.mTagNames);
        if (event.mAlbum != null) {
            mAlbum = new Album(event.mAlbum);
        }
        mCreated = event.mCreated;
        mUpdated = event.mUpdated;
        mOwnerId = event.mOwnerId;
        mOwnerName = event.mOwnerName;
        mOwnerImage = event.mOwnerImage;
        mModified = event.mModified;
        mPrice.copy(event.mPrice);
        mJoinPolicy = event.mJoinPolicy;
        mMaxMembers = event.mMaxMembers;
        mLanguage = event.mLanguage;
        mMemberCount = event.mMemberCount;
        mInvitedCount = event.mInvitedCount;
        mRequestedCount = event.mRequestedCount;
        mFriendCount = event.mFriendCount;
        mCloseFriendCount = event.mCloseFriendCount;
        mCanceled = event.mCanceled;
        mSource = event.mSource;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // Editable fields
        out.writeLong(mId);
        out.writeString(mName);
        out.writeParcelable(mLocation, 0);
        out.writeString(mDescription);
        ParcelHelper.writeDate(out, mStartDate);
        ParcelHelper.writeDate(out, mEndDate);
        out.writeStringList(mTagNames);
        out.writeParcelable(mPrice, 0);
        out.writeString(mJoinPolicy);
        out.writeInt(mMaxMembers);

        //out.writeParcelable(mAlbum, 0);
        //out.writeTypedList(mComments);
        //ParcelHelper.writeDate(out, mCreated);
        //ParcelHelper.writeDate(out, mUpdated);
        //out.writeLong(mOwnerId);
        //out.writeString(mOwnerName);
        //out.writeString(mOwnerImage);
        //out.writeTypedList(mMembers);
        //ParcelHelper.writeBoolean(out, mModified);
        out.writeString(mLanguage);
        out.writeInt(mMemberCount);
        out.writeInt(mInvitedCount);
        out.writeInt(mRequestedCount);
        out.writeInt(mFriendCount);
        out.writeInt(mCloseFriendCount);
        ParcelHelper.writeDate(out, mCanceled);
        out.writeString(mSource);
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

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public List<String> getTagNames() {
        return mTagNames;
    }

    public void setTagNames(List<String> tagNames) {
        mTagNames = tagNames;
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

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public Album getAlbum() {
        return mAlbum;
    }

    public void setAlbum(Album album) {
        mAlbum = album;
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

    public String getOwnerImage() {
        return mOwnerImage;
    }

    public void setOwnerImage(String ownerImage) {
        mOwnerImage = ownerImage;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public boolean isModified() {
        return mModified;
    }

    public void setModified(boolean modified) {
        mModified = modified;
    }

    public Price getPrice() {
        return mPrice;
    }

    public void setPrice(Price price) {
        mPrice = price;
    }

    public String getJoinPolicy() {
        return mJoinPolicy;
    }

    public void setJoinPolicy(String joinPolicy) {
        mJoinPolicy = joinPolicy;
    }

    public int getMaxMembers() {
        return mMaxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        mMaxMembers = maxMembers;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
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

    public int getMemberCount() {
        return mMemberCount;
    }

    public void setMemberCount(int memberCount) {
        mMemberCount = memberCount;
    }

    public int getInvitedCount() {
        return mInvitedCount;
    }

    public void setInvitedCount(int invitedCount) {
        mInvitedCount = invitedCount;
    }

    public int getRequestedCount() {
        return mRequestedCount;
    }

    public void setRequestedCount(int requestedCount) {
        mRequestedCount = requestedCount;
    }

    public int getFriendCount() {
        return mFriendCount;
    }

    public void setFriendCount(int friendCount) {
        mFriendCount = friendCount;
    }

    public int getCloseFriendCount() {
        return mCloseFriendCount;
    }

    public void setCloseFriendCount(int closeFriendCount) {
        mCloseFriendCount = closeFriendCount;
    }

    public EventMember getCurrentUserMember() {
        return mCurrentUserMember;
    }

    public void setCurrentUserMember(EventMember currentUserMember) {
        mCurrentUserMember = currentUserMember;
    }

    public Date getCanceled() {
        return mCanceled;
    }

    public boolean isCanceled() {
        return mCanceled != null;
    }

    public void setCanceled(Date canceled) {
        mCanceled = canceled;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }

    public boolean hasStarted() {
        Calendar start = Calendar.getInstance();
        start.setTime(mStartDate);

        Calendar now = Calendar.getInstance();
        return now.after(start);
    }

    public boolean hasEnded() {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Calendar end = Calendar.getInstance();
        if (mEndDate == null) {
            if (mStartDate == null) {
                // Event not yet loaded
                return true;
            }
            end.setTime(mStartDate);
            end.add(Calendar.HOUR, DEFAULT_LENGTH);
        } else {
            end.setTime(mEndDate);
        }

        return now.after(end);
    }

    public boolean isOwner(long userId) {
        return mOwnerId == userId;
    }

    public boolean canCheckIn() {
        if (mCurrentUserMember != null && EventMember.ACCEPTED.equals(mCurrentUserMember.getStatus())
                && mCurrentUserMember.getCheckedIn() == null && !isCanceled()) {
            // accepted member and not yet checked in, and event has not been canceled
            Calendar checkInStart = Calendar.getInstance();
            checkInStart.setTime(mStartDate);
            checkInStart.add(Calendar.MINUTE, CHECKIN_START);

            Calendar checkInEnd = Calendar.getInstance();
            if (mEndDate == null) {
                // End date not specified, give members 4 hours to check in
                checkInEnd.setTime(mStartDate);
                checkInEnd.add(Calendar.HOUR, DEFAULT_LENGTH);
            } else {
                checkInEnd.setTime(mEndDate);
            }

            Calendar now = Calendar.getInstance();

            return now.after(checkInStart) && now.before(checkInEnd);
        }

        return false;
    }

    public boolean canInvite() {
        return !hasStarted() && mCurrentUserMember != null && (isOwner(mCurrentUserMember.getUserId()) ||
                (EventMember.ACCEPTED.equals(mCurrentUserMember.getStatus()) &&
                (Event.INVITE_ONLY.equals(mJoinPolicy) || Event.OPEN.equals(mJoinPolicy))));
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double distance) {
        mDistance = distance;
    }

    public String getUnits() {
        return mUnits;
    }

    public void setUnits(String units) {
        mUnits = units;
    }

    public boolean isFriendOfOwner() {
        return mFriendOfOwner;
    }

    public void setFriendOfOwner(boolean friendOfOwner) {
        mFriendOfOwner = friendOfOwner;
    }
}
