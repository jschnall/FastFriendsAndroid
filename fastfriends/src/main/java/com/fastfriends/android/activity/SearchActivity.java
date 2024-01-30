package com.fastfriends.android.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.adapter.EventListAdapter;
import com.fastfriends.android.adapter.ProfileListAdapter;
import com.fastfriends.android.adapter.PageAdapterInterface;
import com.fastfriends.android.adapter.PlanListAdapter;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.fragment.dialog.EventFilterDialogFragment;
import com.fastfriends.android.fragment.dialog.PlanFilterDialogFragment;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.EventSearchFilter;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.PlanSearchFilter;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;

import retrofit.RetrofitError;

public class SearchActivity extends ActionBarActivity implements EventFilterDialogFragment.OnFilterListener,
        PlanFilterDialogFragment.OnFilterListener {
    private final static String LOGTAG = SearchActivity.class.getSimpleName();

    private static final int PAGE_SIZE = 20;
    private final static String EVENT_FILTER_FRAGMENT = "event_filter";
    private final static String PLAN_FILTER_FRAGMENT = "plan_filter";

    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_SEARCH_TEXT = "search_text";

    private ListView mListView;
    private ListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;
    private SearchView mSearchView;
    private AdView mAdView;
    private SpinnerAdapter mSpinnerAdapter;

    // Order must match R.array.search_category
    public static final int CATEGORY_EVENTS = 0;
    public static final int CATEGORY_PLANS = 1;
    public static final int CATEGORY_PROFILES = 2;
    private int mCategory = CATEGORY_EVENTS;

    private boolean mRefreshing;
    private String mSearchText;

    private EventSearchFilter mEventFilter = new EventSearchFilter();
    private PlanSearchFilter mPlanFilter = new PlanSearchFilter();

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    private boolean mFirstLoad = true;

    private LocationManager mLocationManager;
    private android.location.Location mMyLocation;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            mMyLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private class SearchProfilesTask extends AsyncTask<String, Void, String> {
        Page<Profile> mProfilePage= null;

        @Override
        protected void onPreExecute() {
            mRefreshing = true;
            if (mFirstLoad) {
                showProgress(true, null);
            }
            hideKeyboard();
            //mAdView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            String searchText = params[2];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(SearchActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mProfilePage = webService.searchProfiles(authToken.getAuthHeader(), page, pageSize, searchText);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't search friends.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't search friends.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mListView.removeFooterView(mLoadingFooter);
            if (error == null) {
                ((PageAdapterInterface) mListAdapter).reset(mProfilePage);
            } else {
                Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    SearchProfilesTask mSearchProfilesTask;

    private class SearchEventsTask extends AsyncTask<String, Void, String> {
        Page<Event> mEventPage = null;

        @Override
        protected void onPreExecute() {
            mRefreshing = true;
            if (mFirstLoad) {
                showProgress(true, null);
            }
            hideKeyboard();
            //mAdView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            int page = Integer.valueOf(params[0]);
            int pageSize = Integer.valueOf(params[1]);
            String searchText = params[2];

            mEventFilter.setPage(page);
            mEventFilter.setPageSize(pageSize);
            mEventFilter.setSearch(searchText);

            if (LocationHelper.isEmpty(mEventFilter.getLatitude(), mEventFilter.getLongitude())) {
                mEventFilter.setLatitude(mMyLocation.getLatitude());
                mEventFilter.setLongitude(mMyLocation.getLongitude());
            }

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(SearchActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mEventPage = webService.searchEvents(authToken.getAuthHeader(), mEventFilter);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't search events.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't search events.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mListView.removeFooterView(mLoadingFooter);
            if (error == null && mEventPage != null) {
                ((PageAdapterInterface) mListAdapter).reset(mEventPage);
                initAd();
            } else {
                Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    SearchEventsTask mSearchEventsTask;

    private class SearchPlansTask extends AsyncTask<String, Void, String> {
        Page<Plan> mPlanPage = null;

        @Override
        protected void onPreExecute() {
            mRefreshing = true;
            if (mFirstLoad) {
                showProgress(true, null);
            }
            hideKeyboard();
            //mAdView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            int page = Integer.valueOf(params[0]);
            int pageSize = Integer.valueOf(params[1]);
            String searchText = params[2];

            mPlanFilter.setPage(page);
            mPlanFilter.setPageSize(pageSize);
            mPlanFilter.setSearch(searchText);

            if (LocationHelper.isEmpty(mPlanFilter.getLatitude(), mPlanFilter.getLongitude())) {
                mPlanFilter.setLatitude(mMyLocation.getLatitude());
                mPlanFilter.setLongitude(mMyLocation.getLongitude());
            }

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(SearchActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mPlanPage = webService.searchPlans(authToken.getAuthHeader(), mPlanFilter);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't search plans.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't search plans.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mListView.removeFooterView(mLoadingFooter);
            if (error == null && mPlanPage != null) {
                ((PageAdapterInterface) mListAdapter).reset(mPlanPage);
                initAd();
            } else {
                Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    SearchPlansTask mSearchPlansTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initActionBar();

        mAdView = (AdView) findViewById(R.id.adView);

        mEventFilter.initDates();

        mPaginatedScrollListener = new PaginatedScrollListener() {
            int mFirstItemIndex = 0;

            @Override
            public void loadPage(int page) {
                if (!mRefreshing && !TextUtils.isEmpty(mSearchText)) {
                    mRefreshing = true;
                    mListView.addFooterView(mLoadingFooter);
                    switch (mCategory) {
                        case CATEGORY_EVENTS: {
                            mSearchEventsTask = new SearchEventsTask();
                            mSearchEventsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false", mSearchText);
                            break;
                        }
                        case CATEGORY_PLANS: {
                            mSearchPlansTask = new SearchPlansTask();
                            mSearchPlansTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false", mSearchText);
                            break;
                        }
                        case CATEGORY_PROFILES: {
                            mSearchProfilesTask = new SearchProfilesTask();
                            mSearchProfilesTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false", mSearchText);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

                if (firstVisibleItem > mFirstItemIndex) {
                    // Scrolled down
                    if (mSearchView != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                    }
                } else if (firstVisibleItem < mFirstItemIndex) {
                    // Scrolled up
                    if (mSearchView != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                    }
                }
                mFirstItemIndex = firstVisibleItem;
            }
        };

        mSearchView = (SearchView) findViewById(R.id.search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false); // Without this it doesn't get focus
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)) {
                    mSearchText = s;
                    refresh();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        mLocationManager = LocationHelper.startLocationUpdates(this, mLocationListener);
        mMyLocation = LocationHelper.getLastKnownLocation(mLocationManager);

        handleIntent(getIntent());
        initList(mCategory);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLocationManager = LocationHelper.startLocationUpdates(this, mLocationListener);
        mMyLocation = LocationHelper.getLastKnownLocation(mLocationManager);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mSearchView.setQuery(query, true);
        } else if (bundle != null) {
            int category = bundle.getInt(EXTRA_CATEGORY, 0);
            Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
            spinner.setSelection(category);

            String searchText = bundle.getString(EXTRA_SEARCH_TEXT);
            if (!TextUtils.isEmpty(searchText)) {
                mSearchView.setQuery(searchText, true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem filterMenuItem = menu.findItem(R.id.action_filter);
        if (mCategory == CATEGORY_PROFILES) {
            filterMenuItem.setVisible(false);
        } else {
            filterMenuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {
                switch(mCategory) {
                    case CATEGORY_EVENTS: {
                        FragmentManager fm = getSupportFragmentManager();
                        EventFilterDialogFragment eventFilterDialog = EventFilterDialogFragment.newInstance(mEventFilter);
                        eventFilterDialog.show(fm, EVENT_FILTER_FRAGMENT);
                        return true;
                    }
                    case CATEGORY_PLANS: {
                        FragmentManager fm = getSupportFragmentManager();
                        PlanFilterDialogFragment planFilterDialog = PlanFilterDialogFragment.newInstance(mPlanFilter);
                        planFilterDialog.show(fm, PLAN_FILTER_FRAGMENT);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onFilter(EventSearchFilter eventFilter) {
        mEventFilter = eventFilter;
        refresh();
    }

    @Override
    public void onFilter(PlanSearchFilter planFilter) {
        mPlanFilter = planFilter;
        refresh();
    }

    private void refresh() {
        switch (mCategory) {
            case CATEGORY_EVENTS: {
                mSearchEventsTask = new SearchEventsTask();
                mSearchEventsTask.execute(String.valueOf(String.valueOf(1)), String.valueOf(PAGE_SIZE), mSearchText);
                break;
            }
            case CATEGORY_PLANS: {
                mSearchPlansTask = new SearchPlansTask();
                mSearchPlansTask.execute(String.valueOf(String.valueOf(1)), String.valueOf(PAGE_SIZE), mSearchText);
                break;
            }
            case CATEGORY_PROFILES: {
                mSearchProfilesTask = new SearchProfilesTask();
                mSearchProfilesTask.execute(String.valueOf(String.valueOf(1)), String.valueOf(PAGE_SIZE), mSearchText);
                break;
            }
        }
    }

    private void initAd() {
        SharedPreferences prefs = Settings.getSharedPreferences();
        String birthday = prefs.getString(Settings.BIRTHDAY, null);
        String gender = prefs.getString(Settings.GENDER, null);

        AdRequest.Builder builder = new AdRequest.Builder();
        if (Profile.GENDER_FEMALE.equals(gender)) {
            builder.setGender(AdRequest.GENDER_FEMALE);
        } else if (Profile.GENDER_MALE.equals(gender)){
            builder.setGender(AdRequest.GENDER_MALE);
        }

        if (birthday != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.GOOGLE_PLUS_DATE_FORMAT);
            try {
                builder.setBirthday(sdf.parse(birthday));
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't parse birthday: " + birthday, e);
            }
        }

        builder.addKeyword(mSearchText);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (View.VISIBLE != mAdView.getVisibility()) {
                    Animation anim = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.slide_up_in);
                    mAdView.setVisibility(View.VISIBLE);
                    mAdView.startAnimation(anim);
                }
            }
        });
        mAdView.loadAd(builder.build());
    }

    private void initActionBar() {
        /*
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        mSearchView = (SearchView) getLayoutInflater().inflate(R.layout.actionbar_search, null);
        mSearchView.setIconified(false); // Without this it doesn't get focus
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s) && !s.equals(mSearchText)) {
                    mSearchText = s;
                    refresh();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        actionBar.setCustomView(mSearchView);
        */

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSpinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(),
                R.array.search_category, android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
                mCategory = itemPosition;
                mFirstLoad = true;
                initList(mCategory);
                invalidateOptionsMenu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initList(int category) {
        mListView = (ListView) findViewById(R.id.list);
        LayoutInflater inflater = getLayoutInflater();
        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        switch(category) {
            case CATEGORY_EVENTS: {
                mListAdapter = new EventListAdapter(this);
                mListView.setAdapter(mListAdapter);
                mListView.removeFooterView(mLoadingFooter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        if (position >= 0 && position < mListAdapter.getCount()) {
                            Event event = (Event) mListAdapter.getItem(position);
                            Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                            intent.putExtra(EventActivity.EXTRA_EVENT_ID, event.getId());
                            intent.putExtra(EventActivity.EXTRA_OWNER_ID, event.getOwnerId());
                            intent.putExtra(EventActivity.EXTRA_TITLE, event.getName());
                            startActivity(intent);
                        }
                    }
                });
                break;
            }
            case CATEGORY_PROFILES: {
                mListAdapter = new ProfileListAdapter(this);
                mListView.setAdapter(mListAdapter);
                mListView.removeFooterView(mLoadingFooter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        if (position >= 0 && position < mListAdapter.getCount()) {
                            Profile profile = (Profile) mListAdapter.getItem(position);
                            Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.EXTRA_USER_ID, profile.getId());
                            intent.putExtra(ProfileActivity.EXTRA_TITLE, profile.getDisplayName());
                            startActivity(intent);
                        }
                    }
                });
                break;
            }
            case CATEGORY_PLANS: {
                mListAdapter = new PlanListAdapter(this);
                mListView.setAdapter(mListAdapter);
                mListView.removeFooterView(mLoadingFooter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        if (position >= 0 && position < mListAdapter.getCount()) {
                            Plan plan = (Plan) mListAdapter.getItem(position);
                            Intent intent = new Intent(SearchActivity.this, PlanActivity.class);
                            intent.putExtra(PlanActivity.EXTRA_PLAN_ID, plan.getId());
                            intent.putExtra(PlanActivity.EXTRA_OWNER_NAME, plan.getOwnerName());
                            startActivity(intent);
                        }
                    }
                });
                break;
            }
        }
        mListView.setOnScrollListener(mPaginatedScrollListener);
    }

    public void showProgress(boolean show, String message) {
        try {
            if (show) {
                mProgressFragment = ProgressFragment.newInstance(message);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, mProgressFragment, PROGRESS_FRAGMENT)
                        .addToBackStack(PROGRESS_FRAGMENT)
                        .commit();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } catch (Exception e) {
            if (show) {
                Log.e(LOGTAG, "Can't show progress", e);
            } else {
                Log.e(LOGTAG, "Can't hide progress", e);
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }
}
