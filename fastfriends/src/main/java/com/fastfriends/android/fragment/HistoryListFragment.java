package com.fastfriends.android.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.EventActivity;
import com.fastfriends.android.activity.PlanActivity;
import com.fastfriends.android.adapter.HistoryListAdapter;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Set;

import retrofit.RetrofitError;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.fastfriends.android.fragment.NavigationDrawerFragment.NavigationDrawerInterface;


public class HistoryListFragment extends Fragment {
    private final static String LOGTAG = HistoryListFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "user_id";

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    private boolean mFirstLoad = true;

    public static final int REQUEST_EVENT_VIEW = 1;
    public static final int REQUEST_PLAN_VIEW = 2;

    private static final int PAGE_SIZE = 20;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StaggeredGridView mGridView;
    private HistoryListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;
    private SpinnerAdapter mSpinnerAdapter;
    private AdView mAdView;

    private int mSelectedItemPosition;
    private boolean mRefreshing;
    private long mUserId;

    private int mCategory;

    private class ListHistoryTask extends AsyncTask<String, Void, String> {
        boolean mRefresh = false;
        Page<Object> mHistoryPage = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            mRefresh = Boolean.valueOf(params[2]);

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Page<Object> historyPage;
                    JsonObject json =  webService.getUserHistory(authToken.getAuthHeader(), page, pageSize, mUserId);
                    mHistoryPage = WebServiceManager.jsonToUserHistoryPage(json);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get history.", e);
                String reason = e.getResponse().getReason();
                if (reason == null) {
                    return e.getMessage();
                }
                return reason;
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get history.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mSwipeRefreshLayout.setRefreshing(false);
            initAd();
            //mGridView.removeFooterView(mLoadingFooter);
            if (error == null) {
                if (mHistoryPage != null) {
                    if (mRefresh) {
                        mListAdapter.reset(mHistoryPage);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(mHistoryPage);
                    }
                    mPaginatedScrollListener.setLoadingComplete(mHistoryPage.getNext() != null);
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ListHistoryTask mListHistoryTask;

    public static HistoryListFragment newInstance(Long userId) {
        HistoryListFragment fragment = new HistoryListFragment();
        Bundle args = new Bundle();
        if (userId != null) {
            args.putLong(ARG_USER_ID, userId);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public HistoryListFragment() {
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
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_history_list, container, false);

        mAdView = (AdView) layout.findViewById(R.id.adView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);

        mGridView = (StaggeredGridView) layout.findViewById(R.id.grid);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < mListAdapter.getCount()) {
                    Object item = mListAdapter.getItem(position);
                    if (item instanceof Event) {
                        Event event = (Event) item;
                        Intent intent = new Intent(getActivity(), EventActivity.class);
                        intent.putExtra(EventActivity.EXTRA_EVENT_ID, event.getId());
                        intent.putExtra(EventActivity.EXTRA_OWNER_ID, event.getOwnerId());
                        intent.putExtra(EventActivity.EXTRA_TITLE, event.getName());
                        startActivityForResult(intent, REQUEST_EVENT_VIEW);
                        mSelectedItemPosition = position;
                    } else if (item instanceof Plan) {
                        Plan plan = (Plan) item;
                        Intent intent = new Intent(getActivity(), PlanActivity.class);
                        intent.putExtra(PlanActivity.EXTRA_PLAN_ID, plan.getId());
                        intent.putExtra(PlanActivity.EXTRA_OWNER_NAME, plan.getOwnerName());
                        startActivityForResult(intent, REQUEST_PLAN_VIEW);
                        mSelectedItemPosition = position;
                    }
                }
            }
        });

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mGridView, false);
        //mGridView.addFooterView(mLoadingFooter);

        mListAdapter = new HistoryListAdapter(getActivity());
        mGridView.setAdapter(mListAdapter);
        //mGridView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    if (mFirstLoad) {
                        mFirstLoad = false;
                        showProgress(true, null);
                    }
                    //mGridView.addFooterView(mLoadingFooter);
                    mListHistoryTask = new ListHistoryTask();
                    mListHistoryTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false");
                }
            }
        };
        mGridView.setOnScrollListener(mPaginatedScrollListener);


        mSpinnerAdapter = ArrayAdapter.createFromResource(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo),
                R.array.event_category, android.R.layout.simple_spinner_dropdown_item);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        inflater.inflate(R.menu.history_list, menu);

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PLAN_VIEW:
            case REQUEST_EVENT_VIEW: {
                if (resultCode == Activity.RESULT_OK) {
                    // Plan or Event was modified
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
                mFirstLoad = false;
                showProgress(true, null);
            }
            mSwipeRefreshLayout.setRefreshing(true);
            mListHistoryTask = new ListHistoryTask();
            mListHistoryTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true");
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
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
            if (mAdView.getVisibility() == View.VISIBLE) {
                // Already showing ad
                return;
            }
            // Main EventList, show ad
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
}
