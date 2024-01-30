package com.fastfriends.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.EventMemberListFragment;
import com.fastfriends.android.fragment.FriendListFragment;

public class EventMemberListActivity extends ActionBarActivity {
    private final static String LOGTAG = EventMemberListActivity.class.getSimpleName();

    // Fragment tags
    private final static String EVENT_MEMBER_LIST_FRAGMENT = "event_member_list";

    public final static String EXTRA_EVENT_ID = "event_id";
    public final static String EXTRA_EVENT_OWNER_ID = "event_owner_id";
    public final static String EXTRA_EVENT_ENDED = "event_ended";
    public final static String EXTRA_CATEGORY = "event_category";

    private ViewGroup mFragmentContainer;
    private View mStatusLayout;
    private TextView mStatusTextView;

    private long mEventId;
    private long mEventOwnerId;
    private boolean mEventEnded;
    private int mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mStatusLayout = findViewById(R.id.status);
        mStatusTextView = (TextView) findViewById(R.id.status_message);
        mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);

        handleIntent();

        Fragment fragment = EventMemberListFragment.newInstance(mEventId, mEventOwnerId, mEventEnded, mCategory);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, EVENT_MEMBER_LIST_FRAGMENT)
                .commit();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mEventId = bundle.getLong(EXTRA_EVENT_ID);
            mEventOwnerId = bundle.getLong(EXTRA_EVENT_OWNER_ID);
            mEventEnded = bundle.getBoolean(EXTRA_EVENT_ENDED);
            mCategory = bundle.getInt(EXTRA_CATEGORY);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Shows the progress UI and hides the fragment.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, String message) {
        mStatusTextView.setText(message);

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mStatusLayout.setVisibility(View.VISIBLE);
            mStatusLayout.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mFragmentContainer.setVisibility(View.VISIBLE);
            mFragmentContainer.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            mFragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed(){
        if (View.VISIBLE != mStatusLayout.getVisibility()) {
            super.onBackPressed();
        }
    }
}
