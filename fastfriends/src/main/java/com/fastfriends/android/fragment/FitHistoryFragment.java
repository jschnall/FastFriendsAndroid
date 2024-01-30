package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.FitActivity;
import com.fastfriends.android.adapter.EventListAdapter;
import com.fastfriends.android.adapter.FitHistoryListAdapter;
import com.fastfriends.android.helper.FitHistoryHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.FitHistory;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;


public class FitHistoryFragment extends Fragment {
    private final static String LOGTAG = FitHistoryFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "user_id";

    private static final int PAGE_SIZE = 20;

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;

    private TextView mEmptyView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private ViewGroup mLoadingFooter;
    private SpinnerAdapter mSpinnerAdapter;
    private FitHistoryListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;

    private boolean mRefreshing;
    private long mUserId;

    // Note: This must match R.arrays.fit_history_category
    private static final int CATEGORY_WEEK = 0;
    private static final int CATEGORY_MONTH = 1;
    private static final int CATEGORY_YEAR = 2;

    private int mCategory;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    boolean mFirstLoad = true;


    private class GetFitDataTask extends AsyncTask<String, Void, String> {
        Page<FitHistory> fitHistoryPage;
        boolean refresh;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            refresh = Boolean.valueOf(params[2]);
            long userId = Long.valueOf(params[3]);
            String period = params[4];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    fitHistoryPage = webService.getFitHistory(authToken.getAuthHeader(), page, pageSize, userId, period, FitHistoryHelper.getStartTime(period));
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get fitHistory", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get fitHistory", e);
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mSwipeRefreshLayout.setRefreshing(false);
            //mListView.removeFooterView(mLoadingFooter);
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (refresh) {
                        mListAdapter.reset(fitHistoryPage);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(fitHistoryPage);
                    }
                    if (fitHistoryPage.getCount() > 0) {
                        mListView.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                    } else {
                        mListView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                    mPaginatedScrollListener.setLoadingComplete(fitHistoryPage.getNext() != null);
                } else {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private GetFitDataTask mGetFitDataTask;


    private class AddHistoryTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            DataReadRequest readRequest = FitHistoryHelper.readFitnessHistory(FitHistory.WEEK);
            DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest)
                    .await(1, TimeUnit.MINUTES);
            List<FitHistory> historyList = FitHistoryHelper.buildHistoryList(dataReadResult, FitHistory.WEEK);

            readRequest = FitHistoryHelper.readFitnessHistory(FitHistory.MONTH);
            dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest)
                    .await(1, TimeUnit.MINUTES);
            historyList.addAll(FitHistoryHelper.buildHistoryList(dataReadResult, FitHistory.MONTH));

            readRequest = FitHistoryHelper.readFitnessHistory(FitHistory.YEAR);
            dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest)
                    .await(1, TimeUnit.MINUTES);
            historyList.addAll(FitHistoryHelper.buildHistoryList(dataReadResult, FitHistory.YEAR));

            //printData(dataReadResult);

            // TODO do this before adding the fragment, or refresh after and only update once a day in a bg service
            // Upload fit history
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    webService.addFitHistory(authToken.getAuthHeader(), historyList);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't add fitHistory", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't add fitHistory", e);
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                SharedPreferences prefs = Settings.getSharedPreferences();
                prefs.edit().putLong(Settings.LAST_FIT_SYNC, System.currentTimeMillis()).commit();

                reload();
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT);
                }
            }
        }
    }
    private AddHistoryTask mAddHistoryTask;

    public static FitHistoryFragment newInstance(long userId) {
        FitHistoryFragment fragment = new FitHistoryFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        fragment.setArguments(args);

        return fragment;
    }

    public FitHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getLong(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_fit_history, container, false);

        mEmptyView = (TextView) layout.findViewById(R.id.empty_item);

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);
        mListView = (ListView) layout.findViewById(R.id.list);

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        //mGridView.addFooterView(mLoadingFooter);

        mListAdapter = new FitHistoryListAdapter(getActivity());
        mListView.setAdapter(mListAdapter);
        //mListView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    if (mFirstLoad) {
                        mFirstLoad = false;
                        showProgress(true, null);
                    }
                    //mListView.addFooterView(mLoadingFooter);
                    mGetFitDataTask = new GetFitDataTask();
                    mGetFitDataTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false", String.valueOf(mUserId), getPeriodString(mCategory));
                }
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        buildFitnessClient();
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOGTAG, "Connecting...");
        // Connect to the Fitness API
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        initActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync: {
                mSwipeRefreshLayout.setRefreshing(true);
                mAddHistoryTask = new AddHistoryTask();
                mAddHistoryTask.execute();
                break;
            }
        }
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void reload() {
        if (!mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mSwipeRefreshLayout.setRefreshing(true);
            mGetFitDataTask = new GetFitDataTask();
            mGetFitDataTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true", String.valueOf(mUserId), getPeriodString(mCategory));
        }
    }

    public void showProgress(boolean show, String message) {
        if (getActivity() != null) {
            try {
                if (show) {
                    mProgressFragment = ProgressFragment.newInstance(message);
                    getChildFragmentManager().beginTransaction()
                            .add(R.id.container, mProgressFragment, PROGRESS_FRAGMENT)
                            .addToBackStack(PROGRESS_FRAGMENT)
                            .commit();
                } else {
                    getChildFragmentManager().popBackStack();
                }
            } catch (Exception e) {
                if (show) {
                    Log.e(LOGTAG, "Can't show progress", e);
                } else {
                    Log.e(LOGTAG, "Can't hide progress", e);
                }
            }
        }
    }

    private void initActionBar() {
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mSpinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(),
                R.array.fit_history_category, android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner_nav);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
                if (mCategory != itemPosition) {
                    mCategory = itemPosition;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public String getPeriodString(int period) {
        switch (period) {
            case 0: {
                return FitHistory.WEEK.toUpperCase();
            }
            case 1: {
                return FitHistory.MONTH.toUpperCase();
            }
            case 2: {
                return FitHistory.YEAR.toUpperCase();
            }
        }
        return null;
    }

    /**
     *  Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        final Activity activity = getActivity();
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(activity)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(LOGTAG, "Google API client Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Play with some sessions!!
                                if (Settings.isFitSyncNeeded(activity)) {
                                    Log.d(LOGTAG, "Updating fit history...");
                                    mAddHistoryTask = new AddHistoryTask();
                                    mAddHistoryTask.execute();
                                } else {
                                    Log.d(LOGTAG, "Fit History up to date.");
                                }
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.d(LOGTAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.d(LOGTAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.d(LOGTAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            activity, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.d(LOGTAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(activity,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(LOGTAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

}