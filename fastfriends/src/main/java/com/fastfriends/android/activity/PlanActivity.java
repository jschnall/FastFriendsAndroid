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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.AlbumFragment;
import com.fastfriends.android.fragment.PlanFragment;

public class PlanActivity extends ActionBarActivity {
    private final static String LOGTAG = PlanActivity.class.getSimpleName();

    public static final String EXTRA_PLAN_ID = "plan_id";
    public static final String EXTRA_OWNER_NAME = "owner_name";

    // Fragment tags
    private final static String PLAN_FRAGMENT = "plan";

    private long mPlanId;
    private String mOwnerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initActionBar();

        handleIntent();

        Fragment fragment = PlanFragment.newInstance(mPlanId);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, PLAN_FRAGMENT)
                .commit();

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
            mPlanId = bundle.getLong(EXTRA_PLAN_ID);
            mOwnerName = bundle.getString(EXTRA_OWNER_NAME);

            if (mOwnerName != null) {
                setTitle(getResources().getString(R.string.title_plan, mOwnerName));
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
