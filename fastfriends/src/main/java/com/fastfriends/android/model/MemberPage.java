package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jschnall on 10/8/14.
 * Adds additional counts to display for categories
 */
public class MemberPage<T> {
    public static final String COUNT = "count";
    public static final String PREVIOUS = "previous";
    public static final String NEXT = "next";
    public static final String RESULTS = "results";
    public static final String FRIEND_COUNT = "friend_count";
    public static final String CLOSE_FRIEND_COUNT = "close_friend_count";
    public static final String OTHER_MEMBER_COUNT = "other_member_count";
    public static final String ACCEPTED_COUNT = "accepted_count";
    public static final String REQUESTED_COUNT = "requested_count";
    public static final String INVITED_COUNT = "invited_count";


    @SerializedName(COUNT)
    private int mCount;

    @SerializedName(PREVIOUS)
    private String mPrevious;

    @SerializedName(NEXT)
    private String mNext;

    @SerializedName(RESULTS)
    private List<T> mResults;

    @SerializedName(FRIEND_COUNT)
    private int mFriendCount;

    @SerializedName(CLOSE_FRIEND_COUNT)
    private int mCloseFriendCount;

    @SerializedName(OTHER_MEMBER_COUNT)
    private int mOtherMemberCount;

    @SerializedName(ACCEPTED_COUNT)
    private int mAcceptedCount;

    @SerializedName(REQUESTED_COUNT)
    private int mRequestedCount;

    @SerializedName(INVITED_COUNT)
    private int mInvitedCount;


    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public String getPrevious() {
        return mPrevious;
    }

    public void setPrevious(String previous) {
        mPrevious = previous;
    }

    public String getNext() {
        return mNext;
    }

    public void setNext(String next) {
        mNext = next;
    }

    public List<T> getResults() {
        return mResults;
    }

    public void setResults(List<T> results) {
        mResults = results;
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

    public int getOtherMemberCount() {
        return mOtherMemberCount;
    }

    public void setOtherMemberCount(int otherMemberCount) {
        mOtherMemberCount = otherMemberCount;
    }

    public int getAcceptedCount() {
        return mAcceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        mAcceptedCount = acceptedCount;
    }

    public int getRequestedCount() {
        return mRequestedCount;
    }

    public void setRequestedCount(int requestedCount) {
        mRequestedCount = requestedCount;
    }

    public int getInvitedCount() {
        return mInvitedCount;
    }

    public void setInvitedCount(int invitedCount) {
        mInvitedCount = invitedCount;
    }
}
