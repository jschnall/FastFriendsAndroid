package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jschnall on 2/17/14.
 */
public class Page<T> {
    public static final String COUNT = "count";
    public static final String PREVIOUS = "previous";
    public static final String NEXT = "next";
    public static final String RESULTS = "results";

    @SerializedName(COUNT)
    private int mCount;

    @SerializedName(PREVIOUS)
    private String mPrevious;

    @SerializedName(NEXT)
    private String mNext;

    @SerializedName(RESULTS)
    private List<T> mResults;

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
}
