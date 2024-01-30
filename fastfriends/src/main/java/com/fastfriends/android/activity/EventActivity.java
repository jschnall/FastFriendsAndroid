package com.fastfriends.android.activity;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.fragment.AlbumFragment;
import com.fastfriends.android.fragment.CommentListFragment;
import com.fastfriends.android.fragment.EventDetailsFragment;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.User;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.viewpagerindicator.TabPageIndicator;

import retrofit.RetrofitError;

public class EventActivity extends ActionBarActivity implements ActionBar.TabListener, AlbumFragment.ListFormatListener {
    private final static String LOGTAG = EventActivity.class.getSimpleName();

    public static final int REQUEST_EVENT_EDIT = 1;
    public static final int REQUEST_INVITE = 2;

    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_OWNER_ID = "owner_id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_SECTION = "section";
    public static final String EXTRA_CHECKIN = "checkin";

    public static final int DETAILS = 0;
    public static final int COMMENTS = 1;
    public static final int ALBUM = 2;
    public static final int PAGE_COUNT = 3;

    long mEventId;
    long mOwnerId;
    boolean mCheckin = false;

    private int mAlbumDisplayType = AlbumFragment.DISPLAY_GRID;

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
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
                    fragment = EventDetailsFragment.newInstance(mEventId, mCheckin);
                    break;
                case COMMENTS:
                    fragment = CommentListFragment.newEventInstance(mEventId);
                    break;
                case ALBUM:
                    if (mAlbumDisplayType == AlbumFragment.DISPLAY_GRID) {
                        fragment = AlbumFragment.newInstance(mEventId, mOwnerId, AlbumFragment.DISPLAY_GRID);
                    } else {
                        fragment = AlbumFragment.newInstance(mEventId, mOwnerId, AlbumFragment.DISPLAY_LIST);
                    }
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
            Locale l = Locale.getDefault();
            switch (position) {
                case DETAILS:
                    return getString(R.string.title_details).toUpperCase(l);
                case COMMENTS:
                    return getString(R.string.title_comments).toUpperCase(l);
                case ALBUM:
                    return getString(R.string.title_album).toUpperCase(l);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initActionBar();

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsStatePagerAdapter);

        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);

        handleIntent();
    }

    public void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
           mEventId = bundle.getLong(EXTRA_EVENT_ID, 0);
           mOwnerId = bundle.getLong(EXTRA_OWNER_ID, 0);

           String title = bundle.getString(EXTRA_TITLE);
           setTitle(title);

           mCheckin = bundle.getBoolean(EXTRA_CHECKIN, false);

           int section = bundle.getInt(EXTRA_SECTION, 0);
           mViewPager.setCurrentItem(section);
        }
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
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_EVENT_EDIT: {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        mSectionsStatePagerAdapter.notifyDataSetChanged();
                        setResult(Activity.RESULT_OK, data);
                    }
                }
                break;
            }
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

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
