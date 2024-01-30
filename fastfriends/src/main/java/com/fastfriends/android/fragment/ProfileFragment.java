package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fastfriends.android.R;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Profile;
import com.viewpagerindicator.TabPageIndicator;

import java.util.HashMap;
import java.util.Locale;

public class ProfileFragment extends Fragment implements ActionBar.TabListener,
        AlbumFragment.ListFormatListener {
    private final static String LOGTAG = ProfileFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_SECTION = "section";

    public static final int DETAILS = 0;
    public static final int ALBUM = 1;
    public static final int HISTORY = 2;
    public static final int PAGE_COUNT = 3;

    private long mUserId;
    private int mLaunchSection;

    private int mAlbumDisplayType = AlbumFragment.DISPLAY_GRID;

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {
        HashMap<Integer, Fragment> registeredFragments = new HashMap<Integer, Fragment>();

        public SectionsStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case DETAILS:
                    fragment = ProfileDetailsFragment.newInstance(mUserId);
                    break;
                case ALBUM:
                    if (mAlbumDisplayType == AlbumFragment.DISPLAY_GRID) {
                        fragment = AlbumFragment.newInstance(mUserId, false, AlbumFragment.DISPLAY_GRID);
                    } else {
                        fragment = AlbumFragment.newInstance(mUserId, false, AlbumFragment.DISPLAY_LIST);
                    }
                    break;
                case HISTORY:
                    fragment = HistoryListFragment.newInstance(mUserId);
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Activity activity = getActivity();
            if (activity == null) {
                return null;
            }

            Locale l = Locale.getDefault();
            switch (position) {
                case DETAILS:
                    return activity.getString(R.string.title_details).toUpperCase(l);
                case ALBUM:
                    return activity.getString(R.string.title_album).toUpperCase(l);
                case HISTORY:
                    return activity.getString(R.string.title_history).toUpperCase(l);
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            if (registeredFragments.containsValue(object)) {
                return POSITION_UNCHANGED;
            }
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
    SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    ViewPager mViewPager;

    public static ProfileFragment newInstance(long userId, int section) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        args.putInt(ARG_SECTION, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLaunchSection = 0;

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getLong(ARG_USER_ID, 0);
            mLaunchSection = bundle.getInt(ARG_SECTION, 0);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initActionBar();

        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) layout.findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsStatePagerAdapter);

        TabPageIndicator indicator = (TabPageIndicator) layout.findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        /*
        indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }
        });
        */
        return layout;
    }

    private void initActionBar() {
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPager.setCurrentItem(mLaunchSection);
    }

    private void handleIntent() {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment currentFragment = mSectionsStatePagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void showGrid() {
        mAlbumDisplayType = AlbumFragment.DISPLAY_GRID;
        mSectionsStatePagerAdapter.registeredFragments.remove(ALBUM);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showList() {
        mAlbumDisplayType = AlbumFragment.DISPLAY_LIST;
        mSectionsStatePagerAdapter.registeredFragments.remove(ALBUM);
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }
}