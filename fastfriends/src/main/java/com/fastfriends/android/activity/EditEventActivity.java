package com.fastfriends.android.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.EditEventFragment;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.Event;

public class EditEventActivity extends ActionBarActivity implements OnShowProgressListener {
    private final static String LOGTAG = EditEventActivity.class.getSimpleName();

    public static final String EXTRA_EVENT = "event";

    private static final String PROGRESS_FRAGMENT = "progress";
    private static final String ADD_EVENT_FRAGMENT = "add_event";

    private Event mEvent;

    private EditEventFragment mEditEventFragment;
    private ProgressFragment mProgressFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        handleIntent();

        if (savedInstanceState == null) {
            mEditEventFragment = EditEventFragment.newInstance(mEvent);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mEditEventFragment, ADD_EVENT_FRAGMENT)
                    .commit();
        } else {
            mEditEventFragment = (EditEventFragment) getSupportFragmentManager().findFragmentByTag(ADD_EVENT_FRAGMENT);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mEvent = bundle.getParcelable(EXTRA_EVENT);
        }

        if (mEvent == null) {
            setTitle(R.string.title_activity_add_event);
        } else {
            setTitle(R.string.title_activity_edit_event);
        }
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
        if (!mEditEventFragment.onBackPressed()) {
           super.onBackPressed();
        }
    }
}
