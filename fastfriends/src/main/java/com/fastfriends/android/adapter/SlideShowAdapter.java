package com.fastfriends.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fastfriends.android.fragment.SlideShowItemFragment;
import com.fastfriends.android.model.Album;
import com.fastfriends.android.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jschnall on 4/10/14.
 */
public class SlideShowAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private List<Resource> mResources;
    private long mOwnerId;
    private long mEventId;
    private long mEventOwnerId;

    private Map<Integer, SlideShowItemFragment> mPageReferenceMap;

    public SlideShowAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        mResources = new ArrayList<Resource>();
        mPageReferenceMap = new HashMap<Integer, SlideShowItemFragment>();
    }

    @Override
    public Fragment getItem(int position) {
        Resource resource = mResources.get(position);
        SlideShowItemFragment fragment = SlideShowItemFragment.newInstance(resource, mOwnerId, mEventId, mEventOwnerId);
        mPageReferenceMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void refresh(Album album) {
        mResources = album.getResources();
        mOwnerId = album.getOwnerId();
        mEventId = album.getEventId();
        mEventOwnerId = album.getEventOwnerId();
        notifyDataSetChanged();
    }

    public void destroyItem (ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }

    public SlideShowItemFragment getFragment(int position) {
        return mPageReferenceMap.get(position);
    }}
