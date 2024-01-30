package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.EditPlanActivity;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.adapter.CommentListAdapter;
import com.fastfriends.android.fragment.dialog.CommentEditDialogFragment;
import com.fastfriends.android.fragment.dialog.CommentOptionsDialogFragment;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.User;
import com.fastfriends.android.service.GcmIntentService;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Date;

import retrofit.RetrofitError;


public class PlanFragment extends Fragment implements CommentOptionsDialogFragment.CommentOptionsListener,
        CommentEditDialogFragment.CommentEditListener {
    private final static String LOGTAG = PlanFragment.class.getSimpleName();

    private static final int HEADER = 0;
    private static final int HEADER_COUNT = 1;

    private static final String ARG_PLAN_ID = "plan_id";

    public static final int REQUEST_PLAN_EDIT = 1;

    private static final int PAGE_SIZE = 20;

    private static final String EDIT_COMMENT_FRAGMENT = "edit_comment";
    private static final String COMMENT_OPTIONS_FRAGMENT = "comment_options";
    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    boolean mFirstLoad = true;

    private long mPlanId = 0;
    private Plan mPlan;
    private boolean mAddingComment;
    private boolean mRefreshing;

    private EditText mMessageView;
    private ImageButton mSubmitButton;

    private ViewGroup mListHeader;
    private TextView mLocationView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private CommentListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;

    //private ViewGroup mOverlayLayout;
    //private boolean mOverlayShown = true;

    private TagHelper mTagHelper;

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Comment comment = (Comment) view.getTag();
            if (User.isCurrentUser(comment.getOwnerId())) {

                FragmentManager fm = getChildFragmentManager();
                CommentOptionsDialogFragment dialog = CommentOptionsDialogFragment.newInstance(comment.getId());
                dialog.show(fm, COMMENT_OPTIONS_FRAGMENT);

            }
            return false;
        }
    };

    public class CommentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(GcmIntentService.EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Comment comment = gson.fromJson(data, Comment.class);
            mListAdapter.addItem(comment);

            setResultData(String.valueOf(true));
        }
    }

    private CommentReceiver mCommentReceiver;

    private class AddCommentTask extends AsyncTask<Comment, Void, String> {
        Comment comment;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Comment... params) {
            comment = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    comment = webService.addPlanComment(authToken.getAuthHeader(), mPlanId, comment);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't add comment.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't add comment.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mAddingComment = false;
            if (error == null && comment != null) {
                mListAdapter.addItem(comment);
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private AddCommentTask mAddCommentTask;

    private class ListCommentsTask extends AsyncTask<String, Void, String> {
        Page<Comment> commentPage;
        boolean refresh;

        @Override
        protected void onPreExecute() {
            mListView.addFooterView(mLoadingFooter);
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            refresh = Boolean.valueOf(params[2]);

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    commentPage = webService.listPlanComments(authToken.getAuthHeader(), page, pageSize, mPlanId);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get comments.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get comments.", e);
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
            if (error == null && commentPage != null) {
                if (refresh) {
                    mListAdapter.reset(commentPage);
                    mPaginatedScrollListener.reset();
                } else {
                    mListAdapter.addPage(commentPage);
                }
                mPaginatedScrollListener.setLoadingComplete(commentPage.getNext() != null);
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private ListCommentsTask mListCommentsTask;

    private class DeleteCommentTask extends AsyncTask<Long, Void, String> {
        long commentId;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Long... params) {
            commentId = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final JsonObject json = webService.deleteComment(commentId, authToken.getAuthHeader());
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't delete comment.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't delete comment.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mAddingComment = false;
            if (error == null) {
                mListAdapter.removeItem(commentId);
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private DeleteCommentTask mDeleteCommentTask;

    private class GetPlanTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            Activity activity = getActivity();
            if (activity != null) {
                showProgress(true, activity.getString(R.string.progress_please_wait));
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mPlan = webService.getPlan(authToken.getAuthHeader(), mPlanId);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get plan.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get plan.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            showProgress(false, null);
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (mPlan != null) {
                        activity.invalidateOptionsMenu();
                        populateHeader(mListHeader);
                        //populateHeader(mOverlayLayout);
                    }
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private GetPlanTask mGetPlanTask;


    public static PlanFragment newInstance(long planId) {
        PlanFragment fragment = new PlanFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAN_ID, planId);
        fragment.setArguments(args);
        return fragment;
    }

    public PlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPlanId = bundle.getLong(ARG_PLAN_ID, 0);
        }

        mTagHelper = TagHelper.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_plan, container, false);

        //mOverlayLayout = (ViewGroup) layout.findViewById(R.id.overlay_layout);

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPlan();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);

        mListView = (ListView) layout.findViewById(R.id.list);

        mListHeader = (ViewGroup) inflater.inflate(R.layout.header_plan, mListView, false);
        mListHeader.setClickable(true); // Prevent list header triggering onItemClickListener
        mListView.addHeaderView(mListHeader);

        mLocationView = (TextView) mListHeader.findViewById(R.id.location);

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new CommentListAdapter(getActivity(), true, mOnLongClickListener);
        mListView.setAdapter(mListAdapter);
        mListView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            int mPreviousFirstItem = 0;

            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    if (mFirstLoad) {
                        mFirstLoad = false;
                        showProgress(true, null);
                    }
                    mListCommentsTask = new ListCommentsTask();
                    mListCommentsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false");
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

                if (firstVisibleItem < mPreviousFirstItem) {
                    // scrolling in from top
                    //showOverlay();
                } else if (firstVisibleItem > mPreviousFirstItem) {
                    // scrolling in from bottom
                    //hideOverlay();
                }
                mPreviousFirstItem = firstVisibleItem;
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        mMessageView = (EditText) layout.findViewById(R.id.message);
        mMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        addComment();
                        return true;
                    }
                    return false;
                }

                addComment();
                return true;
            }
        });

        mSubmitButton = (ImageButton) layout.findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();
            }
        });

        return layout;
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
    public void onStart() {
        super.onStart();

        mGetPlanTask = new GetPlanTask();
        mGetPlanTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();

        mCommentReceiver = new CommentReceiver();
        IntentFilter intentFilter = new IntentFilter(GcmIntentService.ACTION_GCM_COMMENT);
        // Default priority is 0.  Set a higher priority so this can intercept first
        intentFilter.setPriority(1);
        getActivity().registerReceiver(mCommentReceiver,
                intentFilter);

        DBManager.deletePlanComments(mPlanId);
        NotificationHelper.cancel(getActivity(), NotificationHelper.COMMENT_NOTIFICATION_ID);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCommentReceiver != null) {
            getActivity().unregisterReceiver(mCommentReceiver);
            mCommentReceiver = null;
        }
    }

    private void addComment() {
        if (!mAddingComment) {
            String message = mMessageView.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                mAddingComment = true;
                mMessageView.setText("");
                mAddCommentTask = new AddCommentTask();
                mAddCommentTask.execute(new Comment(message));
            }
        }
    }

    public void reloadPlan() {
        if (!mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mSwipeRefreshLayout.setRefreshing(true);
            mListAdapter.clearItems();

            mGetPlanTask = new GetPlanTask();
            mGetPlanTask.execute();

            mListCommentsTask = new ListCommentsTask();
            mListCommentsTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true");
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

    @Override
    public void onEditComment(long commentId) {
        Comment comment = mListAdapter.getItemById(commentId);
        FragmentManager fm = getChildFragmentManager();
        CommentEditDialogFragment dialog = CommentEditDialogFragment.newInstance(comment);
        dialog.show(fm, EDIT_COMMENT_FRAGMENT);

    }

    @Override
    public void onDeleteComment(long commentId) {
        mDeleteCommentTask = new DeleteCommentTask();
        mDeleteCommentTask.execute(commentId);
    }

    @Override
    public void onEdited(Comment comment) {
        mListAdapter.replaceItem(comment);
    }


    private void populateHeader(ViewGroup header) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        String ownerName = mPlan.getOwnerName();

        ImageView portraitView = (ImageView) header.findViewById(R.id.portrait);
        String portrait = mPlan.getOwnerPortrait();
        if (portrait == null) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            ImageLoader.getInstance().displayImage(portrait, portraitView);
        }
        portraitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, mPlan.getOwnerId());
                intent.putExtra(ProfileActivity.EXTRA_TITLE, mPlan.getOwnerName());
                activity.startActivity(intent);
            }
        });

        TextView ownerNameView = (TextView) header.findViewById(R.id.owner);
        ownerNameView.setText(ownerName);

        TextView dateView = (TextView) header.findViewById(R.id.date);
        Date updated = mPlan.getUpdated();
        String timeStamp = DateHelper.buildShortTimeStamp(activity, updated, false);
        dateView.setText(timeStamp);

        TextView editedView = (TextView) header.findViewById(R.id.edited);
        if (updated.getTime() - mPlan.getCreated().getTime() > CommentListAdapter.FIVE_SECONDS) {
            editedView.setText(activity.getString(R.string.edited));
        } else {
            editedView.setText(null);
        }

        CharSequence body = mTagHelper.markup(activity, mPlan.getText(), mPlan.getMentions(), TagHelper.SEARCH_PLANS);
        TextView planTextView = (TextView) header.findViewById(R.id.text);
        planTextView.setMovementMethod(new LinkTouchMovementMethod());
        planTextView.setText(body);

        Location location = mPlan.getLocation();
        mLocationView.setText(formatAddress(location));
    }

    private String formatAddress(Location location) {
        return location.getLocality();
    }

    /*
    private void showOverlay() {
        if (mOverlayShown) {
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mOverlayShown = true;
        mOverlayLayout.setVisibility(View.VISIBLE);

        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.slide_down_in);
        mOverlayLayout.startAnimation(anim);
    }

    private void hideOverlay() {
        if (!mOverlayShown) {
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mOverlayShown = false;

        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.slide_up_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOverlayLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mOverlayLayout.startAnimation(anim);
    }
    */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        long currentUserId = Settings.getSharedPreferences().getLong(Settings.USER_ID, 0);
        if (mPlan != null && mPlan.isOwner(currentUserId)) {
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_make_event).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_make_event).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = getActivity();
        if (activity != null) {
            switch (item.getItemId()) {
                case R.id.action_edit: {
                    Intent intent = new Intent(activity, EditPlanActivity.class);
                    intent.putExtra(EditPlanActivity.EXTRA_PLAN, mPlan);
                    activity.startActivityForResult(intent, REQUEST_PLAN_EDIT);
                    return true;
                }
                case R.id.action_make_event: {
                    // TODO
                    Toast.makeText(activity, "Comming soon...", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PLAN_EDIT: {
                break;
            }
        }
    }
}
