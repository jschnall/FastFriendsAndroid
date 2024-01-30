package com.fastfriends.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.EditEventActivity;
import com.fastfriends.android.activity.EventActivity;
import com.fastfriends.android.activity.EventMemberListActivity;
import com.fastfriends.android.activity.SearchActivity;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.adapter.EventMemberListAdapter;
import com.fastfriends.android.fragment.dialog.InviteFriendsDialogFragment;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.helper.PriceHelper;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.listener.ConfirmListener;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.EventMember;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.MemberPage;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Point;
import com.fastfriends.android.model.Price;
import com.fastfriends.android.text.style.ClickableMovementMethod;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.fastfriends.android.text.style.TagSpan;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import retrofit.RetrofitError;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class EventDetailsFragment extends Fragment implements InviteFriendsDialogFragment.OnSelectedListener, EventMemberListAdapter.ApproveMemberListener, ConfirmListener {
    private final static String LOGTAG = EventDetailsFragment.class.getSimpleName();

    // Child Fragments
    private static final String CONFIRM_CANCEL_FRAGMENT = "confirm_cancel";

    private static final int DETAILS_HEADER = 0;
    private static final int HEADER_COUNT = 1;

    public static final int REQUEST_SELECT_PHOTO = 1;

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_CHECKIN = "checkin";
    private static final String TAG_SEPARATOR = "\t";

    private static final String INVITE_FRIENDS_FRAGMENT = "invite_friends";

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    private boolean mFirstLoad = true;

    private boolean mCheckin = false;
    private Event mEvent;
    private boolean mRefreshing;
    private long mCurrentUserId;

    private ViewGroup mListHeader;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Member list
    private static final int PAGE_SIZE = 20;
    private ListView mListView;
    private EventMemberListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;

    private ViewGroup mButtonLayout;
    private Button mJoinButton;
    private Button mLeaveButton;
    private ViewGroup mInviteLayout;
    private ImageView mInviterPortraitView;
    private TextView mInviterView;
    private Button mDeclineInviteButton;
    private Button mAcceptInviteButton;
    private ImageButton mActionButton;
    private TextView mCanceledText;

    private WebView mWebView;
    private View mDescriptionLayout;
    private TextView mDescriptionView;
    private ImageView mMoreView;
    private boolean mDescriptionExpanded = false;
    private boolean mSubmitting;
    private int mLineCount;

    private boolean mButtonsShown = true;

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

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.location: {
                    showMap();
                    break;
                }
                case R.id.date: {
                    addEventToCalendar();
                    break;
                }
                case R.id.description:
                case R.id.more: {
                    expandDescription(!mDescriptionExpanded);
                    break;
                }
                case R.id.action_button: {
                    checkIn();
                    break;
                }
            }
        }
    };


    public static class ConfirmCancelDialogFragment extends DialogFragment {
        ConfirmListener confirmListener;

        public ConfirmCancelDialogFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_confirm_cancel_event_title)
                    .setMessage(R.string.dialog_confirm_cancel_event_message)
                    .setNegativeButton(R.string.dialog_confirm_cancel_event_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setPositiveButton(R.string.dialog_confirm_cancel_event_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            confirmListener.onConfirm();
                        }
                    })
                    .create();
            return dialog;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            confirmListener = (ConfirmListener) getParentFragment();
        }

    };
    private ConfirmCancelDialogFragment mConfirmCancelDialogFragment;

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

    private class CancelEventTask extends AsyncTask<Void, Void, String> {
        JsonObject response;

        @Override
        protected void onPreExecute() {
            mSubmitting = true;
        }

        @Override
        protected String doInBackground(Void... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    response = webService.cancelEvent(mEvent.getId(), authToken.getAuthHeader());
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't cancel event.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't cancel event.", e);
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
                        activity.setResult(Activity.RESULT_OK);
                        activity.finish();
                    }
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            mSubmitting = false;
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(true);
        }
    }
    private CancelEventTask mCancelEventTask;

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
                    response = webService.inviteToEvent(authToken.getAuthHeader(), mEvent.getId(), WebServiceManager.buildIdListString(userIds));
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
                        reloadEvent();
                        reloadMembers();
                    }
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            mSubmitting = false;
        }
    }
    private InviteTask mInviteTask;

    private class CheckInTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
            mSubmitting = true;
            mActionButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    double latitude = 0.0;
                    double longitude = 0.0;
                    if (mMyLocation != null) {
                        latitude = mMyLocation.getLatitude();
                        longitude = mMyLocation.getLongitude();
                    }
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    JsonObject json = webService.checkInEvent(authToken.getAuthHeader(), mEvent.getId(), latitude, longitude);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't check in event.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't check in event.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mSwipeRefreshLayout.setRefreshing(true);
            mActionButton.setEnabled(true);

            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    //Date now = new Date();
                    //mEvent.getCurrentUserMember().setCheckedIn(now);
                    //populateHeader();
                    //activity.invalidateOptionsMenu();

                    //EventMember member = mListAdapter.getMember(mCurrentUserId);
                    //if (member != null) {
                    //    member.setCheckedIn(now);
                    //    mListAdapter.initMembers();
                    //}
                    reloadEvent();
                    reloadMembers();
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            mSubmitting = false;
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(true);
        }
    }
    private CheckInTask mCheckInTask;

    private class JoinTask extends AsyncTask<Boolean, Void, String> {
        boolean mJoining;

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
            mSubmitting = true;
            mJoinButton.setEnabled(false);
            mLeaveButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(Boolean... params) {
            mJoining = params[0];
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    if (mJoining) {
                        EventMember member = webService.joinEvent(authToken.getAuthHeader(), mEvent.getId());
                    } else {
                        JsonObject json = webService.leaveEvent(authToken.getAuthHeader(), mEvent.getId());
                    }
                }
            } catch (RetrofitError retrofitError) {
                if (mJoining) {
                    Log.e(LOGTAG, "Can't join event.", retrofitError);
                } else {
                    Log.e(LOGTAG, "Can't leave event.", retrofitError);
                }
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                if (mJoining) {
                    Log.e(LOGTAG, "Can't join event.", e);
                } else {
                    Log.e(LOGTAG, "Can't leave event.", e);
                }
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mSwipeRefreshLayout.setRefreshing(true);
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    reloadEvent();
                    reloadMembers();
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            mSubmitting = false;
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(true);
        }
    }
    private JoinTask mJoinTask;

    private class AcceptInviteTask extends AsyncTask<Boolean, Void, String> {
        boolean mAccepting;

        @Override
        protected void onPreExecute() {
            mSubmitting = true;
            mJoinButton.setEnabled(false);
            mLeaveButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(Boolean... params) {
            mAccepting = params[0];
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    JsonObject json = webService.acceptInvite(authToken.getAuthHeader(),
                            mEvent.getCurrentUserMember().getId(), mAccepting);
                }
            } catch (RetrofitError retrofitError) {
                if (mAccepting) {
                    Log.e(LOGTAG, "Can't accept invite.", retrofitError);
                } else {
                    Log.e(LOGTAG, "Can't decline invite.", retrofitError);
                }
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                if (mAccepting) {
                    Log.e(LOGTAG, "Can't accept invite.", e);
                } else {
                    Log.e(LOGTAG, "Can't decline invite.", e);
                }
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    reloadEvent();
                    reloadMembers();
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            mSubmitting = false;
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(true);
        }
    }
    private AcceptInviteTask mAcceptInviteTask;

    private class GetEventTask extends AsyncTask<String, Void, String> {
        Event event;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    event = webService.getEvent(authToken.getAuthHeader(), mEvent.getId());
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get event.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get event.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            mSwipeRefreshLayout.setRefreshing(false);
            showProgress(false, null);

            if (error == null) {
                if (event != null) {
                    mEvent = event;
                    populateHeader();
                    mListView.setOnScrollListener(mPaginatedScrollListener);

                    if (mCheckin) {
                        mCheckin = false;
                        checkIn();
                    }
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private GetEventTask mGetEventTask;

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
            String status = params[3];

            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    eventMemberPage = webService.getEventMembers(authToken.getAuthHeader(), mEvent.getId(), page, pageSize, status);
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
                        mListAdapter.reset(eventMemberPage, mEvent.getOwnerId(), EventMemberListFragment.CATEGORY_ALL);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(eventMemberPage, mEvent.getOwnerId(),  EventMemberListFragment.CATEGORY_ALL);
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

    public static EventDetailsFragment newInstance(long eventId, boolean checkIn) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EVENT_ID, eventId);
        if (checkIn) {
            args.putBoolean(ARG_CHECKIN, true);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mEvent = new Event();
            mEvent.setId(bundle.getLong(ARG_EVENT_ID));
            mCheckin = bundle.getBoolean(ARG_CHECKIN, false);
        }

        SharedPreferences prefs = Settings.getSharedPreferences();
        mCurrentUserId = prefs.getLong(Settings.USER_ID, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();

        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_event_details, container, false);

        mButtonLayout = (ViewGroup) layout.findViewById(R.id.status_layout);
        mJoinButton = (Button) mButtonLayout.findViewById(R.id.join);
        mLeaveButton = (Button) mButtonLayout.findViewById(R.id.leave);
        mInviteLayout = (ViewGroup) layout.findViewById(R.id.invite_layout);
        mInviterPortraitView = (RoundedImageView) layout.findViewById(R.id.inviter_portrait);
        mInviterView = (TextView) layout.findViewById(R.id.inviter);
        mDeclineInviteButton = (Button) mButtonLayout.findViewById(R.id.decline);
        mAcceptInviteButton = (Button) mButtonLayout.findViewById(R.id.accept);
        mCanceledText = (TextView) layout.findViewById(R.id.canceled_text);

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadEvent();
                reloadMembers();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);

        mListView = (ListView) layout.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                if (position >= HEADER_COUNT) {
                    // position - 1 for header
                    EventMemberListAdapter.ListItem item = (EventMemberListAdapter.ListItem) mListAdapter.getItem(position - HEADER_COUNT);
                    if (item.getType() == EventMemberListAdapter.ListItem.TYPE_MEMBER) {
                        EventMember member = ((EventMemberListAdapter.MemberItem) item).getEventMember();
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        intent.putExtra(ProfileActivity.EXTRA_USER_ID, member.getUserId());
                        intent.putExtra(ProfileActivity.EXTRA_TITLE, member.getDisplayName());
                        activity.startActivity(intent);
                    }
                }
            }
        });

        mListHeader = (ViewGroup) inflater.inflate(R.layout.header_event_details, mListView, false);
        mListHeader.setClickable(true); // Prevent list header triggering onItemClickListener
        mListView.addHeaderView(mListHeader);

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new EventMemberListAdapter(activity, this, mEvent.hasEnded(), false);
        mListView.setAdapter(mListAdapter);
        mListView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    mListView.addFooterView(mLoadingFooter);
                    mListEventMembersTask = new ListEventMembersTask();
                    mListEventMembersTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), String.valueOf(false), EventMember.ACCEPTED);
                }
            }
        };

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            private static final int TOLERANCE = 5;
            private float mLastY = 0;
            private float mOffset = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch(action) {
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        mLastY = 0;
                        mOffset = 0;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float y = motionEvent.getY();
                        if (mLastY > 0) {
                            mOffset += y - mLastY;
                            if (mOffset > TOLERANCE) {
                                showButtonLayout();
                                mOffset = 0;
                                mLastY = 0;
                            } else if (mOffset < -TOLERANCE) {
                                hideButtonLayout();
                                mOffset = 0;
                                mLastY = 0;
                            }
                        }
                        mLastY = y;
                        break;
                    }
                }
                return false;
            }
        };
        mListView.setOnTouchListener(onTouchListener);
        mSwipeRefreshLayout.setOnTouchListener(onTouchListener);

        mActionButton = (ImageButton) layout.findViewById(R.id.action_button);
        mActionButton.setOnClickListener(mOnClickListener);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadEvent();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.event_details, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        if (mEvent == null || mEvent.getStartDate() == null || mEvent.isCanceled()) {
            // Event not loaded yet
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_invite).setVisible(false);
            menu.findItem(R.id.action_cancel).setVisible(false);
            return;
        }

        if (!mEvent.hasEnded() && mEvent.isOwner(mCurrentUserId)) {
            menu.findItem(R.id.action_cancel).setVisible(true);
        } else {
            menu.findItem(R.id.action_cancel).setVisible(false);
        }

        boolean eventStarted = mEvent.hasStarted();
        if (!eventStarted && mEvent.isOwner(mCurrentUserId)) {
            menu.findItem(R.id.action_edit).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
        }

        String joinPolicy = mEvent.getJoinPolicy();
        if (mEvent.canInvite()) {
            menu.findItem(R.id.action_invite).setVisible(true);
        } else {
            menu.findItem(R.id.action_invite).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_cancel: {
                showConfirmCancelDialog();
                return true;
            }
            case R.id.action_edit: {
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, EditEventActivity.class);
                    intent.putExtra(EditEventActivity.EXTRA_EVENT, mEvent);
                    startActivityForResult(intent, EventActivity.REQUEST_EVENT_EDIT);
                    return true;
                }
            }
            //case R.id.action_check_in: {
            //    checkIn();
            //    return true;
            //}
            case R.id.action_invite: {
                showInviteDialog();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public SpannableString formatTag(Context context, final String source) {
        SpannableString ss = new SpannableString(source);
        ss.setSpan(new TagSpan(context, source, false) {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_TEXT, source);
                startActivity(intent);
            }
        }, 0, source.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    private void populateHeader() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.setTitle(mEvent.getName());

        if (mEvent.isCanceled()) {
            mCanceledText.setVisibility(View.VISIBLE);
        } else {
            mCanceledText.setVisibility(View.GONE);
        }

        if (mEvent.canCheckIn()) {
            mActionButton.setVisibility(View.VISIBLE);
        } else {
            mActionButton.setVisibility(View.GONE);
        }

        String imageUrl = mEvent.getImage();
        final ImageView imageView = (ImageView) mListHeader.findViewById(R.id.image);
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(R.drawable.event_default_bg);
        } else {
            ImageLoader.getInstance().displayImage(imageUrl, imageView);
        }

        TextView eventNameView = (TextView) mListHeader.findViewById(R.id.title);
        eventNameView.setText(mEvent.getName());

        Price price = mEvent.getPrice();
        TextView subTitleView = (TextView) mListHeader.findViewById(R.id.subtitle);
        String owner = mEvent.getOwnerName();
        String source = mEvent.getSource();
        mWebView = (WebView) mListHeader.findViewById(R.id.webview);
        if (owner == null) {
            if (source != null) {
                // Event was imported
                //subTitleView.setText(activity.getString(R.string.event_import_subtitle, source));
                subTitleView.setVisibility(View.INVISIBLE);
                subTitleView.setHeight(0);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadUrl("file:///android_asset/eventful.html");
                mWebView.setBackgroundColor(0x00000000);
            }
        } else {
            mWebView.setVisibility(View.GONE);
            subTitleView.setVisibility(View.VISIBLE);
            subTitleView.setText(activity.getString(R.string.event_subtitle, owner,
                    PriceHelper.formatPrice(activity, price.getCurrencyCode(), price.getAmount())));
        }

        TextView locationView = (TextView) mListHeader.findViewById(R.id.location);
        locationView.setText(mEvent.getLocation().getStreetAddress());
        locationView.setOnClickListener(mOnClickListener);

        TextView dateView = (TextView) mListHeader.findViewById(R.id.date);
        String dateStr = DateFormat.getLongDateFormat(activity).format(mEvent.getStartDate());
        String timeStr = DateFormat.getTimeFormat(activity).format(mEvent.getStartDate());
        String dateTimeStr = getString(R.string.event_date_time, dateStr, timeStr);
        dateView.setText(dateTimeStr);
        dateView.setOnClickListener(mOnClickListener);

        TextView tagsView = (TextView) mListHeader.findViewById(R.id.tags);
        tagsView.setMovementMethod(ClickableMovementMethod.getInstance());
        List<String> tagNames = mEvent.getTagNames();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (String tagName : tagNames) {
            builder.append(formatTag(getActivity(), tagName));
            builder.append(TAG_SEPARATOR);
        }
        if (TextUtils.isEmpty(builder)) {
            tagsView.setVisibility(View.GONE);
        } else {
            tagsView.setVisibility(View.VISIBLE);
            tagsView.setText(builder);
        }

        String description = mEvent.getDescription();
        mDescriptionLayout = mListHeader.findViewById(R.id.description_layout);
        mMoreView = (ImageView) mListHeader.findViewById(R.id.more);
        mMoreView.setOnClickListener(mOnClickListener);
        mDescriptionView = (TextView) mListHeader.findViewById(R.id.description);
        if (TextUtils.isEmpty(description)) {
            mDescriptionView.setVisibility(View.GONE);
            mMoreView.setVisibility(View.GONE);
        } else {
            mDescriptionView.setVisibility(View.VISIBLE);
            mMoreView.setVisibility(View.VISIBLE);
            CharSequence body = TagHelper.getInstance().markup(activity, Html.fromHtml(description), mEvent.getMentions(), TagHelper.SEARCH_EVENTS);
            mDescriptionView.setMovementMethod(new LinkTouchMovementMethod());
            mDescriptionView.setText(body);
            mLineCount = mDescriptionView.getLineCount();
            expandDescription(mDescriptionExpanded);
            mDescriptionView.setOnClickListener(mOnClickListener);
            mDescriptionView.setScrollContainer(false);
        }

        if (mEvent == null || mEvent.getOwnerId() == mCurrentUserId || mEvent.hasEnded()) {
            // Event owner, event has not yet loaded, event has ended
            mJoinButton.setVisibility(View.GONE);
            mLeaveButton.setVisibility(View.GONE);
            mInviteLayout.setVisibility(View.GONE);
        } else {
            final EventMember member = mEvent.getCurrentUserMember();
            if (member == null) {
                // Current user is not an event member
                mLeaveButton.setVisibility(View.GONE);
                mInviteLayout.setVisibility(View.GONE);

                // If join policy permits, let them join
                String joinPolicy = mEvent.getJoinPolicy();
                if (Event.INVITE_ONLY.equals(joinPolicy) ||
                        (Event.FRIENDS_ONLY.equals(joinPolicy) && !mEvent.isFriendOfOwner())) {
                    mJoinButton.setVisibility(View.GONE);
                } else {
                    mJoinButton.setVisibility(View.VISIBLE);
                    mJoinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!mSubmitting) {
                                joinEvent();
                            }
                        }
                    });
                }
            } else {
                // Current user is an event member
                String status = member.getStatus();
                if (EventMember.ACCEPTED.equals(status)) {
                    mJoinButton.setVisibility(View.GONE);
                    mLeaveButton.setVisibility(View.VISIBLE);
                    mLeaveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            leaveEvent();
                        }
                    });
                    mInviteLayout.setVisibility(View.GONE);
                } else if (EventMember.REQUESTED.equals(status)) {
                    mJoinButton.setVisibility(View.GONE);
                    mLeaveButton.setVisibility(View.GONE);
                    mInviteLayout.setVisibility(View.GONE);
                    // TODO ??? Let user cancel request
                } else if (EventMember.INVITED.equals(status)) {
                    mJoinButton.setVisibility(View.GONE);
                    mLeaveButton.setVisibility(View.GONE);
                    mInviteLayout.setVisibility(View.VISIBLE);

                    String portrait = member.getInviterPortrait();
                    if (portrait == null) {
                        mInviterPortraitView.setImageResource(R.drawable.ic_person);
                    } else {
                        ImageLoader.getInstance().displayImage(portrait, mInviterPortraitView);
                    }
                    mInviterView.setText(member.getInviterName());
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (activity != null) {
                                Intent intent = new Intent(activity, ProfileActivity.class);
                                intent.putExtra(ProfileActivity.EXTRA_USER_ID, member.getInviter());
                                intent.putExtra(ProfileActivity.EXTRA_TITLE, member.getInviterName());
                                startActivity(intent);
                            }
                        }
                    };
                    mInviterPortraitView.setOnClickListener(onClickListener);
                    mInviterView.setOnClickListener(onClickListener);

                    mAcceptInviteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            acceptInvite();
                        }
                    });
                    mDeclineInviteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            declineInvite();
                        }
                    });
                }
            }
        }

        View membersTitleLayout = mListHeader.findViewById(R.id.members_title_layout);
        membersTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEventMembers();
            }
        });

        TextView membersTitle = (TextView) mListHeader.findViewById(R.id.members_title);
        if (mEvent.hasEnded()) {
            membersTitle.setText(activity.getString(R.string.members_title_past, mEvent.getMemberCount()));
        } else {
            membersTitle.setText(activity.getString(R.string.members_title, mEvent.getMemberCount()));
        }

        int acquaintanceCount = mEvent.getFriendCount();
        TextView acquaintanceCountView = (TextView) mListHeader.findViewById(R.id.acquaintance_count);
        acquaintanceCountView.setText(activity.getString(R.string.friend_count, acquaintanceCount));

        int friendCount = mEvent.getCloseFriendCount();
        TextView friendCountView = (TextView) mListHeader.findViewById(R.id.friend_count);
        friendCountView.setText(activity.getString(R.string.friend_count, friendCount));
    }

    public void reloadEvent() {
        if (getActivity() != null && !mRefreshing) {
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mRefreshing = true;
            mSwipeRefreshLayout.setRefreshing(true);
            mGetEventTask = new GetEventTask();
            mGetEventTask.execute();
        }
    }

    public void reloadMembers() {
        mListEventMembersTask = new ListEventMembersTask();
        mListEventMembersTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), String.valueOf(true), EventMember.ACCEPTED);
    }

    private void expandDescription(boolean expand) {
        mDescriptionExpanded = expand;

        final int descriptionLines = getActivity().getResources().getInteger(R.integer.collapsed_description_lines);
        if (expand && mLineCount > 4) {
            mDescriptionView.setMaxLines(Integer.MAX_VALUE);
            mDescriptionView.setEllipsize(null);
            //Linkify.addLinks(mDescriptionView, Linkify.ALL);
            mMoreView.setVisibility(View.VISIBLE);
            mMoreView.setImageResource(R.drawable.ic_collapse);
        } else {
            mDescriptionView.setMaxLines(descriptionLines);
            mDescriptionView.setEllipsize(TextUtils.TruncateAt.END);
            //Linkify.addLinks(mDescriptionView, Linkify.ALL);
            mDescriptionView.post(new Runnable() {
                @Override
                public void run() {
                    int lines = mDescriptionView.getLineCount();
                    if (lines > 0 && mDescriptionView.getLayout().getEllipsisCount(lines - 1) > 0 ||
                            lines > descriptionLines) {
                        mMoreView.setImageResource(R.drawable.ic_expand);
                        mMoreView.setVisibility(View.VISIBLE);
                    } else {
                        mMoreView.setVisibility(View.GONE);
                    }
                }
            });
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

    public void showMap() {
        Activity activity = getActivity();
        if (activity != null) {
            Location location = mEvent.getLocation();
            Point point = location.getPoint();
            double latitude = point.getLatitude();
            double longitude = point.getLongitude();
            String uriBegin = "geo:" + latitude + "," + longitude;
            String address = location.getStreetAddress();
            String query;
            if (TextUtils.isEmpty(address)) {
                String label = mEvent.getName();
                query = latitude + "," + longitude + "(" + label + ")";
            } else {
                query = address;
            }

            String encodedQuery = Uri.encode(query);
            String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
            Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    public void showEventMembers() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, EventMemberListActivity.class);
            intent.putExtra(EventMemberListActivity.EXTRA_EVENT_ID, mEvent.getId());
            intent.putExtra(EventMemberListActivity.EXTRA_EVENT_OWNER_ID, mEvent.getOwnerId());
            intent.putExtra(EventMemberListActivity.EXTRA_EVENT_ENDED, mEvent.hasEnded());
            intent.putExtra(EventMemberListActivity.EXTRA_CATEGORY, EventMemberListFragment.CATEGORY_ALL);
            activity.startActivity(intent);
        }
    }

    public void addEventToCalendar() {
        Calendar startCal = new GregorianCalendar();
        startCal.setTime(mEvent.getStartDate());

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, mEvent.getName());
        intent.putExtra(CalendarContract.Events.ALL_DAY, false);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTime().getTime());
        Date endDate = mEvent.getEndDate();
        if (endDate != null) {
            Calendar endCal = new GregorianCalendar();
            endCal.setTime(endDate);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTime().getTime());
        }
        //intent.putExtra(Intent.EXTRA_EMAIL, "attendee1@yourtest.com, attendee2@yourtest.com");
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showButtonLayout() {
        if (mButtonsShown) {
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mButtonsShown = true;
        mButtonLayout.setVisibility(View.VISIBLE);

        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.slide_up_in);
        mButtonLayout.startAnimation(anim);
    }

    private void hideButtonLayout() {
        if (!mButtonsShown) {
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mButtonsShown = false;

        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.slide_down_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mButtonLayout.startAnimation(anim);
    }

    public void joinEvent() {
        mJoinTask = new JoinTask();
        mJoinTask.execute(true);

    }

    public void leaveEvent() {
        mJoinTask = new JoinTask();
        mJoinTask.execute(false);
    }

    public void checkIn() {
        mCheckInTask = new CheckInTask();
        mCheckInTask.execute();
    }

    public void selectPromoImage() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        // TODO Open album tab in selection mode
        //Intent intent = new Intent(activity, AlbumActivity.class);
        //intent.putExtra(AlbumActivity.EXTRA_EVENT_ID, mEvent.getId());
        //intent.putExtra(AlbumActivity.EXTRA_SELECT_PORTRAIT, true);
        //getParentFragment().startActivityForResult(intent, REQUEST_SELECT_PHOTO);
    }

    private void showInviteDialog() {
        Activity activity = getActivity();
        if (activity != null) {
            FragmentManager fm = getChildFragmentManager();
            InviteFriendsDialogFragment dialogFragment = InviteFriendsDialogFragment.newInstance(mEvent.getId());
            dialogFragment.show(fm, INVITE_FRIENDS_FRAGMENT);
        }
    }

    @Override
    public void onSelected(List<Long> userIds) {
        mInviteTask = new InviteTask();
        mInviteTask.execute(userIds);
    }

    private void acceptInvite() {
        mInviteLayout.setVisibility(View.GONE);
        mAcceptInviteTask = new AcceptInviteTask();
        mAcceptInviteTask.execute(true);
    }

    private void declineInvite() {
        mInviteLayout.setVisibility(View.GONE);
        mAcceptInviteTask = new AcceptInviteTask();
        mAcceptInviteTask.execute(false);
    }

    @Override
    public void onConfirm() {
        cancelEvent();
    }

    private void cancelEvent() {
        mCancelEventTask = new CancelEventTask();
        mCancelEventTask.execute();
    }

    private void showConfirmCancelDialog() {
        FragmentManager fm = getChildFragmentManager();
        mConfirmCancelDialogFragment = new ConfirmCancelDialogFragment();
        mConfirmCancelDialogFragment.show(fm, CONFIRM_CANCEL_FRAGMENT);
    }

    @Override
    public void onApproveMember(EventMember member, boolean accept) {
        mApproveMemberTask = new ApproveMemberTask();
        mApproveMemberTask.execute(member, accept);
    }
}
