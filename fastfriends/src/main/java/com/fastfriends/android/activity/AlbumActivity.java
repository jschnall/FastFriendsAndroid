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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.AlbumFragment;
import com.fastfriends.android.fragment.AuthenticationFragment;
import com.fastfriends.android.fragment.CommentListFragment;
import com.fastfriends.android.fragment.EventDetailsFragment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AlbumActivity extends ActionBarActivity implements AlbumFragment.ProfileUpdateListener,
        AlbumFragment.ListFormatListener {
    private final static String LOGTAG = AlbumActivity.class.getSimpleName();

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_PROFILE = "profile";
    public static final String EXTRA_SELECT_PORTRAIT = "select_portrait";

    // Fragment tags
    private final static String ALBUM_FRAGMENT = "album";

    private long mUserId;

    private ViewGroup mFragmentContainer;
    private View mStatusLayout;
    private TextView mStatusTextView;

    private boolean mPortraitSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        mStatusLayout = findViewById(R.id.status);
        mStatusTextView = (TextView) findViewById(R.id.status_message);
        mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);

        handleIntent();
        if (savedInstanceState == null) {
            showGrid();
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
           mUserId = bundle.getLong(EXTRA_USER_ID);
           mPortraitSelectionMode = bundle.getBoolean(EXTRA_SELECT_PORTRAIT, false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onProfileUpdated(Profile profile) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PROFILE, profile);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Shows the progress UI and hides the fragment.
     */
    @Override
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

    @Override
    public void showGrid() {
        Fragment fragment = AlbumFragment.newInstance(mUserId, mPortraitSelectionMode, AlbumFragment.DISPLAY_GRID);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, ALBUM_FRAGMENT)
                .commit();
    }

    @Override
    public void showList() {
        Fragment fragment = AlbumFragment.newInstance(mUserId, mPortraitSelectionMode, AlbumFragment.DISPLAY_LIST);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, ALBUM_FRAGMENT)
                .commit();
    }
}
