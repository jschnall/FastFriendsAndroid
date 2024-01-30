package com.fastfriends.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.ConversationFragment;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.listener.OnShowProgressListener;

public class ConversationActivity extends ActionBarActivity implements OnShowProgressListener {
    private final static String LOGTAG = ConversationActivity.class.getSimpleName();

    public static final String EXTRA_OTHER_USER_ID = "user_id";
    public static final String EXTRA_OTHER_USER_NAME = "user_name";
    public static final String EXTRA_CURRENT_USER_PORTRAIT = "current_user_portrait";
    public static final String EXTRA_OTHER_USER_PORTRAIT = "other_user_portrait";

    private static final String PROGRESS_FRAGMENT = "progress";
    private static final String CONVERSATION_FRAGMENT = "conversation";

    private long mUserId;
    private String mUserName;
    private String mCurrentUserPortrait;
    private String mOtherUserPortrait;
    private boolean mOpened;

    private ConversationFragment mConversationFragment;
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
            mConversationFragment = ConversationFragment.newInstance(mUserId, mUserName, mOpened,
                    mCurrentUserPortrait, mOtherUserPortrait);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mConversationFragment, CONVERSATION_FRAGMENT)
                    .commit();
        } else {
            mConversationFragment = (ConversationFragment) getSupportFragmentManager().findFragmentByTag(CONVERSATION_FRAGMENT);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mUserId = bundle.getLong(EXTRA_OTHER_USER_ID);
            mUserName = bundle.getString(EXTRA_OTHER_USER_NAME);
            mCurrentUserPortrait = bundle.getString(EXTRA_CURRENT_USER_PORTRAIT);
            mOtherUserPortrait = bundle.getString(EXTRA_OTHER_USER_PORTRAIT);

            setTitle(getString(R.string.conversation_title, mUserName));
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
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
        switch(item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        };
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
        mConversationFragment.onBackPressed();
        super.onBackPressed();
    }
}
