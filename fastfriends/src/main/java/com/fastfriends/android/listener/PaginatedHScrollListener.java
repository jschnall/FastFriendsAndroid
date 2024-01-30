package com.fastfriends.android.listener;

import android.widget.AbsListView;

import it.sephiroth.android.library.widget.AbsHListView;
import it.sephiroth.android.library.widget.HListView;

/**
 * Created by jschnall on 2/13/14.
 */
public abstract class PaginatedHScrollListener implements AbsHListView.OnScrollListener {
    private final static String LOGTAG = PaginatedHScrollListener.class.getSimpleName();

    private static final int DEFAULT_LOADING_THRESHOLD = 5;

    private boolean mLoading = false;
    private boolean mNextPage = true; // Whether there is another page to load
    private int mCurrentPage = 0;
    private int mLoadingThreshold;
    private boolean mLoadAtTop = false;

    public PaginatedHScrollListener() {
        this(false, DEFAULT_LOADING_THRESHOLD);
    }

    public PaginatedHScrollListener(int loadingThreshold) {
        this(false, loadingThreshold);
    }

    public PaginatedHScrollListener(boolean loadAtTop, int loadingThreshold) {
        mLoadAtTop = loadAtTop;
        mLoadingThreshold = loadingThreshold;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public boolean hasNextPage() {
        return mNextPage;
    }

    @Override
    public void onScrollStateChanged(AbsHListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsHListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        if (mLoadAtTop) {
            if (!mLoading && mNextPage && firstVisibleItem == 0) {
                mLoading = true;
                loadPage(mCurrentPage + 1);
            }
        } else if (!mLoading && mNextPage && (totalItemCount - visibleItemCount) <= (firstVisibleItem + mLoadingThreshold)) {
            mLoading = true;
            loadPage(mCurrentPage + 1);
        }
    }

    public void reset() {
        mCurrentPage = 0;
    }

    public void setLoadingComplete(boolean nextPage) {
        mNextPage = nextPage;
        mCurrentPage++;
        mLoading = false;
    }

    protected abstract void loadPage(int page);
}
