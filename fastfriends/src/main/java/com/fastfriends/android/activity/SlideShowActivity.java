package com.fastfriends.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.adapter.SlideShowAdapter;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.fragment.SlideShowItemFragment;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.Album;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Resource;
import com.fastfriends.android.view.LockableViewPager;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import java.util.List;

import retrofit.RetrofitError;

public class SlideShowActivity extends ActionBarActivity implements SlideShowItemFragment.OnEditListener {
    private final static String LOGTAG = SlideShowActivity.class.getSimpleName();

    public static final String EXTRA_ALBUM = "album";
    public static final String EXTRA_INDEX = "index";

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_RESOURCE_ID = "resource_id";

    private static final int PAGE_SIZE = 20;

    private Album mAlbum;
    private LockableViewPager mViewPager;
    private SlideShowAdapter mAdapter;

    private long mEventId;
    private long mUserId;

    private class GetAlbumTask extends AsyncTask<String, Void, String> {
        Page<Album> albumPage;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(SlideShowActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    if (mEventId > 0) {
                        albumPage = webService.listEventAlbums(authToken.getAuthHeader(), page, pageSize, mEventId);
                    } else {
                        albumPage = webService.listUserAlbums(authToken.getAuthHeader(), page, pageSize, mUserId);
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get albums.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get albums.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            //mRefreshing = false;
            //showProgress(false, null);
            if (error == null) {
                if (albumPage.getCount() > 0) {
                    mAlbum = albumPage.getResults().get(0);
                }
                mAdapter.refresh(mAlbum);
                initViewPager(getIntent().getExtras());
            } else {
                Toast.makeText(SlideShowActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private GetAlbumTask mGetAlbumTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_slide_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initActionBar();

        mAdapter = new SlideShowAdapter(getSupportFragmentManager(), this);
        mViewPager = (LockableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        handleIntent();
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
            mAlbum = (Album) bundle.getParcelable(EXTRA_ALBUM);
            if (mAlbum == null) {
                mEventId = bundle.getLong(EXTRA_EVENT_ID, 0);
                mUserId = bundle.getLong(EXTRA_USER_ID, 0);
                mGetAlbumTask = new GetAlbumTask();
                mGetAlbumTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE));
            } else {
                mAdapter.refresh(mAlbum);
                initViewPager(bundle);
            }
        }
    }

    public void initViewPager(Bundle bundle) {
        long id = bundle.getLong(EXTRA_RESOURCE_ID, 0);
        int index = 0;
        if (id > 0) {
            List<Resource> resources = mAlbum.getResources();
            int i = 0;
            for (Resource resource : resources) {
                if (resource.getId() == id) {
                    index = i;
                }
                i++;
            }
        } else {
            index = bundle.getInt(EXTRA_INDEX, 0);
        }
        mViewPager.setCurrentItem(index);
    }

    @Override
    public void onEditStart() {
        mViewPager.setPagingDisabled(true);
    }

    @Override
    public void onEditEnd() {
        mViewPager.setPagingDisabled(false);
    }

    @Override
    public void onBackPressed() {
        int position = mViewPager.getCurrentItem();
        SlideShowItemFragment fragment = mAdapter.getFragment(position);
        if (fragment == null) {
            super.onBackPressed();
        } else {
            fragment.onBackPressed();
        }
    }
}
