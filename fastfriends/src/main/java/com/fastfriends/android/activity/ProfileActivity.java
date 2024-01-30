package com.fastfriends.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.AlbumFragment;
import com.fastfriends.android.fragment.ProfileFragment;
import com.fastfriends.android.model.Profile;

public class ProfileActivity extends ActionBarActivity {
    private final static String LOGTAG = ProfileActivity.class.getSimpleName();

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_TITLE = "title";

    // Fragment tags
    private final static String PROFILE = "profile";

    private long mUserId;

    private ViewGroup mFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initActionBar();

        mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);

        handleIntent();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, ProfileFragment.newInstance(mUserId, 0), PROFILE)
                    .commit();

        }
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
           mUserId = bundle.getLong(EXTRA_USER_ID, 0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
