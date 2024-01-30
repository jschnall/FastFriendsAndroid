package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fastfriends.android.Settings;
import com.fastfriends.android.model.Conversation;
import com.fastfriends.android.text.style.TagSpan;
import com.fastfriends.android.view.TabPageIndicator;

import com.fastfriends.android.R;

import java.util.HashMap;
import java.util.Locale;

public class MessageCategoriesFragment extends Fragment implements ActionBar.TabListener {
    private final static String LOGTAG = MessageCategoriesFragment.class.getSimpleName();

    public static final int RECEIVED = 0;
    public static final int SENT = 1;
    public static final int DRAFTS = 2;
    public static final int PAGE_COUNT = 3;

    private TabPageIndicator mIndicator;

    private int mUnreadMessageCount;
    private int mDraftMessageCount;
    private ViewGroup mLayout;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Settings.UNREAD_MESSAGE_COUNT.equals(key)) {
                mUnreadMessageCount = sharedPreferences.getInt(key, 0);
                refreshUnreadMessageCount();
                reloadCategories(new int[]{RECEIVED, SENT});
            } else if (Settings.MESSAGE_COUNT.equals(key)) {
                // NOTE: Not necessary, and if reloadCategories removes fragment
                // a second time may prevent reloading
                //refreshUnreadMessageCount();
                //reloadCategories(new int[]{RECEIVED, SENT});
            } else if (Settings.DRAFT_MESSAGE_COUNT.equals(key)) {
                mDraftMessageCount = sharedPreferences.getInt(key, 0);
                refreshDraftMessageCount();
                reloadCategories(new int[] { DRAFTS });
            }
        }
    };

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
                case RECEIVED:
                    fragment = ConversationListFragment.newInstance(Conversation.CATEGORY_RECEIVED);
                    break;
                case SENT:
                    fragment = ConversationListFragment.newInstance(Conversation.CATEGORY_SENT);
                    break;
                case DRAFTS:
                    fragment = ConversationListFragment.newInstance(Conversation.CATEGORY_DRAFTS);
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
                case RECEIVED:
                    return activity.getString(R.string.title_received).toUpperCase(l);
                case SENT:
                    return activity.getString(R.string.title_sent).toUpperCase(l);
                case DRAFTS:
                    return activity.getString(R.string.title_drafts).toUpperCase(l);
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

    public static MessageCategoriesFragment newInstance() {
        MessageCategoriesFragment fragment = new MessageCategoriesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initActionBar();

        // Init message counts
        SharedPreferences prefs = Settings.getSharedPreferences();
        mUnreadMessageCount = prefs.getInt(Settings.UNREAD_MESSAGE_COUNT, 0);
        mDraftMessageCount = prefs.getInt(Settings.DRAFT_MESSAGE_COUNT, 0);

        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_message_categories, container, false);

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) mLayout.findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsStatePagerAdapter);

        mIndicator = (TabPageIndicator) mLayout.findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        /*
        indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }
        });
        */

        refreshDraftMessageCount();
        refreshUnreadMessageCount();

        return mLayout;
    }

    private void initActionBar() {
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Settings.getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        Settings.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);
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
        //inflater.inflate(R.menu.message_categories, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        switch (item.getItemId()) {
            case R.id.action_contacts: {
                Intent intent = new Intent(getActivity(), ContactListActivity.class);
                startActivity(intent);
                return true;
            }
        }
        */
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConversationListFragment.REQUEST_CONVERSATION_VIEW: {
                if (resultCode == Activity.RESULT_OK) {
                    mSectionsStatePagerAdapter.registeredFragments.clear();
                    mSectionsStatePagerAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }

    public BitmapDrawable formatMessageCount(Context context, int source) {
        String sourceStr = String.valueOf(source);
        return TagSpan.convertViewToDrawable(context, TagSpan.createOffScreenTextView(context, sourceStr, false, R.drawable.message_count_bg, 16));
    }

    private void refreshUnreadMessageCount() {
        TextView tab = mIndicator.getTab(RECEIVED);
        if (mUnreadMessageCount <= 0) {
            //tab.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            tab.setText(Conversation.CATEGORY_RECEIVED);
        } else {
            //tab.setCompoundDrawablesWithIntrinsicBounds(null, null, formatMessageCount(getActivity(), mUnreadMessageCount), null);
            tab.setText(Conversation.CATEGORY_RECEIVED + " (" + mUnreadMessageCount + ")");
        }
    }

    private void refreshDraftMessageCount() {
        TextView tab = mIndicator.getTab(DRAFTS);
        if (mDraftMessageCount <= 0) {
            tab.setText(Conversation.CATEGORY_DRAFTS);
        } else {
            tab.setText(Conversation.CATEGORY_DRAFTS + " (" + mDraftMessageCount + ")");
        }
    }

    private void reloadCategories(int[] categories) {
        for (int category : categories) {
            mSectionsStatePagerAdapter.registeredFragments.remove(category);
        }
        mSectionsStatePagerAdapter.notifyDataSetChanged();
    }

}
