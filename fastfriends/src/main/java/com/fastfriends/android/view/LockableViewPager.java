package com.fastfriends.android.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jschnall on 6/5/14.
 */
public class LockableViewPager extends ViewPager {
    private boolean mPagingDisabled;

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mPagingDisabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mPagingDisabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingDisabled(boolean disabled) {
        mPagingDisabled = disabled;
    }

    public boolean isPagingDisabled() {
        return mPagingDisabled;
    }
}