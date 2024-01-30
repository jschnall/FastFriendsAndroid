package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.EditPlanActivity;
import com.fastfriends.android.activity.PlanActivity;
import com.fastfriends.android.activity.SearchActivity;
import com.fastfriends.android.adapter.PlanListAdapter;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Set;

import retrofit.RetrofitError;

import static com.fastfriends.android.fragment.NavigationDrawerFragment.NavigationDrawerInterface;


public class PlanListFragment extends Fragment {
    private final static String LOGTAG = PlanListFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "user_id";

    // Note: This must match R.arrays.plan_category
    private static final int CATEGORY_RECOMMENDED = 0;
    private static final int CATEGORY_FRIENDS = 1;
    private static final int CATEGORY_NEWEST = 2;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    private boolean mFirstLoad = true;

    public static final int REQUEST_PLAN_VIEW = 1;
    public static final int REQUEST_PLAN_EDIT = 2;
    public static final int REQUEST_PLAN_SEARCH = 3;

    private static final int PAGE_SIZE = 20;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private PlanListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;
    private SpinnerAdapter mSpinnerAdapter;
    private AdView mAdView;

    private int mSelectedItemPosition;
    private boolean mRefreshing;
    private long mUserId;
    private String mOrdering;

    private LocationManager mLocationManager;
    private Location mMyLocation;

    private int mCategory;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
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

    private class ListPlansTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            final boolean refresh = Boolean.valueOf(params[2]);

            if (mMyLocation == null) {
                return null;
            }

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Page<Plan> planPage;
                    if (mUserId > 0) {
                        planPage = webService.listUserPlans(authToken.getAuthHeader(), page, pageSize, mUserId);
                    } else {
                        switch (mCategory) {
                            case CATEGORY_FRIENDS: {
                                planPage = webService.listPlans(authToken.getAuthHeader(), page, pageSize, Plan.CATEGORY_FRIENDS, mMyLocation.getLatitude(), mMyLocation.getLongitude());
                                break;
                            }
                            case CATEGORY_RECOMMENDED: {
                                planPage = webService.listPlans(authToken.getAuthHeader(), page, pageSize, Plan.CATEGORY_RECOMMENDED, mMyLocation.getLatitude(), mMyLocation.getLongitude());
                                break;
                            }
                            default: {
                                planPage = webService.listPlans(authToken.getAuthHeader(), page, pageSize, Plan.CATEGORY_NEWEST, mMyLocation.getLatitude(), mMyLocation.getLongitude());
                            }
                        }
                    }
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (refresh) {
                                    mListAdapter.reset(planPage);
                                    mPaginatedScrollListener.reset();
                                } else {
                                    mListAdapter.addPage(planPage);
                                }
                                mPaginatedScrollListener.setLoadingComplete(planPage.getNext() != null);
                            }
                        });
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get plans.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get plans.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mSwipeRefreshLayout.setRefreshing(false);
            mListView.removeFooterView(mLoadingFooter);
            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ListPlansTask mListPlansTask;

    public static PlanListFragment newInstance(Long userId) {
        PlanListFragment fragment = new PlanListFragment();
        Bundle args = new Bundle();
        if (userId != null) {
            args.putLong(ARG_USER_ID, userId);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public PlanListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getLong(ARG_USER_ID, 0);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLocationManager = LocationHelper.startLocationUpdates(getActivity(), mLocationListener);
        mMyLocation = LocationHelper.getLastKnownLocation(mLocationManager);

        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_plan_list, container, false);

        mAdView = (AdView) layout.findViewById(R.id.adView);
        initAd();

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);

        mListView = (ListView) layout.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < mListAdapter.getCount()) {
                    Plan plan = (Plan) mListAdapter.getItem(position);
                    Intent intent = new Intent(getActivity(), PlanActivity.class);
                    intent.putExtra(PlanActivity.EXTRA_PLAN_ID, plan.getId());
                    intent.putExtra(PlanActivity.EXTRA_OWNER_NAME, plan.getOwnerName());
                    startActivityForResult(intent, REQUEST_PLAN_VIEW);
                    mSelectedItemPosition = position;
                }
            }
        });

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new PlanListAdapter(getActivity());
        mListView.setAdapter(mListAdapter);
        mListView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    if (mFirstLoad) {
                        showProgress(true, null);
                    }
                    mListView.addFooterView(mLoadingFooter);
                    listPlans(page, false);
                }
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        mLocationManager = LocationHelper.startLocationUpdates(getActivity(), mLocationListener);
        mMyLocation = LocationHelper.getLastKnownLocation(mLocationManager);
    }

    @Override
    public void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.plan_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        boolean shouldShowMenu;
        if (activity instanceof NavigationDrawerInterface) {
            NavigationDrawerInterface navigationDrawerInterface = (NavigationDrawerInterface) getActivity();
            shouldShowMenu = navigationDrawerInterface.shouldShowMenu();
        } else {
            shouldShowMenu = true;
        }

        // UserId is only set if displaying fragment within profile
        if (shouldShowMenu && mUserId <= 0) {
            initActionBar();
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_create).setVisible(true);
        } else {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_create).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_CATEGORY, SearchActivity.CATEGORY_PLANS);
                startActivityForResult(intent, REQUEST_PLAN_SEARCH);
                return true;
            }
            case R.id.action_create: {
                Intent intent = new Intent(getActivity(), EditPlanActivity.class);
                startActivityForResult(intent, REQUEST_PLAN_EDIT);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PLAN_VIEW: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Plan plan = bundle.getParcelable(EditPlanActivity.EXTRA_PLAN);
                            mListAdapter.updateItem(mSelectedItemPosition, plan);
                            reload();
                        }
                    }
                }
                break;
            }
            case REQUEST_PLAN_SEARCH:
            case REQUEST_PLAN_EDIT: {
                if (resultCode == Activity.RESULT_OK) {
                    reload();
                }
                break;
            }
        }
    }

    private void reload() {
        if (getActivity() != null && !mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                showProgress(true, null);
            }
            mSwipeRefreshLayout.setRefreshing(true);
            listPlans(1, true);
        }
    }

    private void initActionBar() {

        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        mSpinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(),
                R.array.plan_category, android.R.layout.simple_spinner_dropdown_item);

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && User.isCurrentUser(mUserId)) {
            DBManager.delete(Comment.class, null);
            NotificationHelper.cancel(getActivity(), NotificationHelper.COMMENT_NOTIFICATION_ID);
        }
    }

    private void initAd() {
        if (mUserId <= 0) {
            SharedPreferences prefs = Settings.getSharedPreferences();
            String birthday = prefs.getString(Settings.BIRTHDAY, null);
            String gender = prefs.getString(Settings.GENDER, null);
            Set<String> interests = prefs.getStringSet(Settings.INTERESTS, null);

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

            if (interests != null) {
                for (String interest : interests) {
                    builder.addKeyword(interest);
                }
            }

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Activity activity = getActivity();
                    if (activity != null && View.VISIBLE != mAdView.getVisibility()) {
                        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.slide_up_in);
                        mAdView.setVisibility(View.VISIBLE);
                        mAdView.startAnimation(anim);
                    }
                }
            });
            mAdView.loadAd(builder.build());
        } else {
            // Don't show ad when displaying fragment within profile
            mAdView.setVisibility(View.GONE);
        }
    }

    private void listPlans(int page, boolean refresh) {
        mFirstLoad = false;

        mListPlansTask = new ListPlansTask();
        mListPlansTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), String.valueOf(refresh));

    }
}
