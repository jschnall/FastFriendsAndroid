package com.fastfriends.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.EditPlanFragment;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.Plan;

public class EditPlanActivity extends ActionBarActivity implements OnShowProgressListener {
    private final static String LOGTAG = EditPlanActivity.class.getSimpleName();

    public static final String EXTRA_PLAN = "plan";

    private static final String PROGRESS_FRAGMENT = "progress";
    private static final String ADD_PLAN_FRAGMENT = "add_plan";

    private Plan mPlan;

    private EditPlanFragment mEditPlanFragment;
    private ProgressFragment mProgressFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        handleIntent();

        if (savedInstanceState == null) {
            mEditPlanFragment = EditPlanFragment.newInstance(mPlan);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mEditPlanFragment, ADD_PLAN_FRAGMENT)
                    .commit();
        } else {
            mEditPlanFragment = (EditPlanFragment) getSupportFragmentManager().findFragmentByTag(ADD_PLAN_FRAGMENT);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mPlan = bundle.getParcelable(EXTRA_PLAN);
        }

        if (mPlan == null) {
            setTitle(R.string.title_activity_add_plan);
        } else {
            setTitle(R.string.title_activity_edit_plan);
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
        if (!mEditPlanFragment.onBackPressed()) {
           super.onBackPressed();
        }
    }
}
