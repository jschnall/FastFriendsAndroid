package com.fastfriends.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.EditEventFragment;
import com.fastfriends.android.fragment.EditProfileFragment;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Profile;

public class EditProfileActivity extends ActionBarActivity implements OnShowProgressListener {
    private final static String LOGTAG = EditProfileActivity.class.getSimpleName();

    public static final String EXTRA_PROFILE = "profile";
    public static final String EXTRA_USER = "user";

    private static final String PROGRESS_FRAGMENT = "progress";
    private static final String EDIT_PROFILE_FRAGMENT = "edit_profile";

    private Profile mProfile;

    private EditProfileFragment mEditProfileFragment;
    private ProgressFragment mProgressFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        handleIntent();

        if (savedInstanceState == null) {
            mEditProfileFragment = EditProfileFragment.newInstance(mProfile);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mEditProfileFragment, EDIT_PROFILE_FRAGMENT)
                    .commit();
        } else {
            mEditProfileFragment = (EditProfileFragment) getSupportFragmentManager().findFragmentByTag(EDIT_PROFILE_FRAGMENT);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mProfile = bundle.getParcelable(EXTRA_PROFILE);
        }
        setTitle(R.string.title_activity_edit_profile);

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        switch(item.getItemId()) {
            case R.id.action_cancel: {
                finish();
                return true;
            }
            case R.id.action_done: {
                finish();
                return true;
            }
        };
        */
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showProgress(boolean show, String message) {
        if (show) {
            mProgressFragment = ProgressFragment.newInstance(message);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mProgressFragment, PROGRESS_FRAGMENT)
                    .addToBackStack(PROGRESS_FRAGMENT)
                    .commit();
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    @Override
    public void onBackPressed() {
        if (!mEditProfileFragment.onBackPressed()) {
           super.onBackPressed();
        }
    }
}
