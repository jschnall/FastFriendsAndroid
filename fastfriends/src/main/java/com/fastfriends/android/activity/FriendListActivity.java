package com.fastfriends.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
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
import com.fastfriends.android.fragment.AlbumFragment;
import com.fastfriends.android.fragment.FriendListFragment;
import com.fastfriends.android.model.Profile;

public class FriendListActivity extends ActionBarActivity {
    private final static String LOGTAG = FriendListActivity.class.getSimpleName();

    public static final String EXTRA_SELECT_FRIENDS = "select_friends";

    // Fragment tags
    private final static String FRIEND_LIST_FRAGMENT = "friend_list";

    private ViewGroup mFragmentContainer;
    private View mStatusLayout;
    private TextView mStatusTextView;

    private boolean mFriendSelectionMode = false;

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

        Fragment fragment = FriendListFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, FRIEND_LIST_FRAGMENT)
                .commit();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mFriendSelectionMode = bundle.getBoolean(EXTRA_SELECT_FRIENDS, false);
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
