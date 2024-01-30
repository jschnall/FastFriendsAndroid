package com.fastfriends.android.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.adapter.EventMemberListAdapter;
import com.fastfriends.android.fragment.dialog.InviteFriendsDialogFragment;
import com.fastfriends.android.helper.SharingHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.EventMember;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.MemberPage;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit.RetrofitError;


public class EventMemberListFragment extends Fragment implements InviteFriendsDialogFragment.OnSelectedListener, EventMemberListAdapter.ApproveMemberListener {
    private final static String LOGTAG = EventMemberListFragment.class.getSimpleName();

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_OWNER_ID = "event_owner_id";
    private static final String ARG_EVENT_ENDED = "event_ended";
    private static final String ARG_CATEGORY = "category";

    private static final String INVITE_FRIENDS_FRAGMENT = "invite_friends";

    private static final int PAGE_SIZE = 20;

    // Note: Indices must match with R.array.event_member_category
    public static final int CATEGORY_ALL = 0;
    public static final int CATEGORY_CONFIRMED = 1;
    public static final int CATEGORY_REQUESTED = 2;
    public static final int CATEGORY_INVITED = 3;

    private ListView mListView;
    private EventMemberListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;
    private SpinnerAdapter mSpinnerAdapter;

    private boolean mRefreshing;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    private boolean mFirstLoad = true;

    private long mEventId;
    private long mEventOwnerId;
    private boolean mEventEnded;
    private int mCategoryIndex;

    private boolean mSubmitting;

    private class InviteTask extends AsyncTask<List<Long>, Void, String> {
        List<Long> userIds;
        JsonObject response;

        @Override
        protected void onPreExecute() {
            mSubmitting = true;
        }

        @Override
        protected String doInBackground(List<Long>... params) {
            userIds = params[0];

            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    response = webService.inviteToEvent(authToken.getAuthHeader(), mEventId, WebServiceManager.buildIdListString(userIds));
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't invite friends.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't invite friends.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (response != null) {
                        reload();
                    }
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            mSubmitting = false;
        }
    }
    private InviteTask mInviteTask;

    private class ApproveMemberTask extends AsyncTask<Object, Void, String> {
        EventMember mMember;
        boolean mAcceptMember;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Object... params) {
            mMember = (EventMember) params[0];
            mAcceptMember = (Boolean) params[2];

            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    JsonObject json = webService.approveEventMember(authToken.getAuthHeader(), mMember.getId(), mAcceptMember);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't approve EventMember: " + mMember.getId(), retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't approve EventMember: " + mMember.getId(), e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    // TODO refresh all groups;
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ApproveMemberTask mApproveMemberTask;

    private class ListEventMembersTask extends AsyncTask<String, Void, String> {
        MemberPage<EventMember> eventMemberPage = null;
        boolean refresh;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            refresh = Boolean.valueOf(params[2]);

            String status = null;
            switch (mCategoryIndex) {
                case CATEGORY_CONFIRMED: {
                    status = EventMember.ACCEPTED;
                    break;
                }
                case CATEGORY_REQUESTED: {
                    status = EventMember.REQUESTED;
                    break;
                }
                case CATEGORY_INVITED: {
                    status = EventMember.INVITED;
                    break;
                }
            }
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    eventMemberPage = webService.getEventMembers(authToken.getAuthHeader(), mEventId, page, pageSize, status);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't list event members.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't list event members.", e);
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
                if (eventMemberPage != null) {
                    if (refresh) {
                        mListAdapter.reset(eventMemberPage, mEventOwnerId, mCategoryIndex);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(eventMemberPage, mEventOwnerId, mCategoryIndex);
                    }
                    mPaginatedScrollListener.setLoadingComplete(eventMemberPage.getNext() != null);
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ListEventMembersTask mListEventMembersTask;

    public static EventMemberListFragment newInstance(long eventId, long eventOwnerId, boolean eventEnded,  int category) {
        EventMemberListFragment fragment = new EventMemberListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EVENT_ID, eventId);
        args.putLong(ARG_EVENT_OWNER_ID, eventOwnerId);
        args.putBoolean(ARG_EVENT_ENDED, eventEnded);
        args.putInt(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public EventMemberListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEventId = bundle.getLong(ARG_EVENT_ID);
            mEventOwnerId = bundle.getLong(ARG_EVENT_OWNER_ID);
            mEventEnded = bundle.getBoolean(ARG_EVENT_ENDED);
            mCategoryIndex = bundle.getInt(ARG_CATEGORY);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_event_member_list, container, false);

        mListView = (ListView) layout.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < mListAdapter.getCount()) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        EventMemberListAdapter.ListItem item = (EventMemberListAdapter.ListItem) mListAdapter.getItem(position);
                        if (item.getType() == EventMemberListAdapter.ListItem.TYPE_MEMBER) {
                            EventMember member = ((EventMemberListAdapter.MemberItem) item).getEventMember();
                            Intent intent = new Intent(activity, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.EXTRA_USER_ID, member.getUserId());
                            intent.putExtra(ProfileActivity.EXTRA_TITLE, member.getDisplayName());
                            activity.startActivity(intent);
                        }
                    }
                }
            }
        });

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new EventMemberListAdapter(getActivity(), this, mEventEnded, true);
        mListView.setAdapter(mListAdapter);
        mListView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    if (mFirstLoad) {
                        mFirstLoad = false;
                        showProgress(true, null);
                    }
                    mListView.addFooterView(mLoadingFooter);
                    mListEventMembersTask = new ListEventMembersTask();
                    mListEventMembersTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), String.valueOf(false));
                }
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        mSpinnerAdapter = ArrayAdapter.createFromResource(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo),
                R.array.event_member_category, android.R.layout.simple_spinner_dropdown_item);

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
        inflater.inflate(R.menu.event_member_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        boolean shouldShowMenu;
        if (activity instanceof NavigationDrawerFragment.NavigationDrawerInterface) {
            NavigationDrawerFragment.NavigationDrawerInterface navigationDrawerInterface = (NavigationDrawerFragment.NavigationDrawerInterface) getActivity();
            shouldShowMenu = navigationDrawerInterface.shouldShowMenu();
        } else {
            shouldShowMenu = true;
        }

        // UserId is only set if displaying fragment within profile
        if (shouldShowMenu) {
            initActionBar();
            //menu.findItem(R.id.action_invite).setVisible(true);
        } else {
            //menu.findItem(R.id.action_invite).setVisible(false);
        }
   }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_invite: {
                showInviteDialog();
                return true;
            }
        }
        return false;
    }

    private void reload() {
        if (getActivity() != null && !mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mListEventMembersTask = new ListEventMembersTask();
            mListEventMembersTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), String.valueOf(true));
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (mCategoryIndex != itemPosition) {
                    mCategoryIndex = itemPosition;
                    reload();
                    return true;
                }
                return false;
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
    public void onApproveMember(EventMember member, boolean accept) {
        mApproveMemberTask = new ApproveMemberTask();
        mApproveMemberTask.execute(member, accept);
    }

    private void showInviteDialog() {
        Activity activity = getActivity();
        if (activity != null) {
            FragmentManager fm = getChildFragmentManager();
            InviteFriendsDialogFragment dialogFragment = InviteFriendsDialogFragment.newInstance(mEventId);
            dialogFragment.show(fm, INVITE_FRIENDS_FRAGMENT);
        }
    }

    @Override
    public void onSelected(List<Long> userIds) {
        mInviteTask = new InviteTask();
        mInviteTask.execute(userIds);
    }


}
