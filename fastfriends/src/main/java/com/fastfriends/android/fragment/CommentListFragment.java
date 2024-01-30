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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.adapter.CommentListAdapter;
import com.fastfriends.android.fragment.dialog.CommentEditDialogFragment;
import com.fastfriends.android.fragment.dialog.CommentOptionsDialogFragment;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.User;
import com.fastfriends.android.service.GcmIntentService;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit.RetrofitError;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class CommentListFragment extends Fragment implements CommentOptionsDialogFragment.CommentOptionsListener,
        CommentEditDialogFragment.CommentEditListener {
    private final static String LOGTAG = CommentListFragment.class.getSimpleName();

    private static final String ARG_EVENT_ID = "event_id";

    private static final int PAGE_SIZE = 20;

    private static final String EDIT_COMMENT_FRAGMENT = "edit_comment";
    private static final String COMMENT_OPTIONS_FRAGMENT = "comment_options";
    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    boolean mFirstLoad = true;

    private long mEventId = 0;
    private boolean mAddingComment;
    private boolean mRefreshing;

    private EditText mMessageView;
    private ImageButton mSubmitButton;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private CommentListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;

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
                    if (mEventId > 0) {
                        comment = webService.addEventComment(authToken.getAuthHeader(), mEventId, comment);
                    }
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
            if (error == null) {
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
                    if (mEventId > 0) {
                        commentPage = webService.listEventComments(authToken.getAuthHeader(), page, pageSize, mEventId);
                    }
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
            if (error == null) {
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

    public static CommentListFragment newEventInstance(long eventId) {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    public CommentListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEventId = bundle.getLong(ARG_EVENT_ID, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_comment_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadComments();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);

        mListView = (ListView) layout.findViewById(R.id.list);

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new CommentListAdapter(getActivity(), false, mOnLongClickListener);
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
                    mListCommentsTask = new ListCommentsTask();
                    mListCommentsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false");
                }
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
    public void onResume() {
        super.onResume();

        mCommentReceiver = new CommentReceiver();
        IntentFilter intentFilter = new IntentFilter(GcmIntentService.ACTION_GCM_COMMENT);
        // Default priority is 0.  Set a higher priority so this can intercept first
        intentFilter.setPriority(1);
        getActivity().registerReceiver(mCommentReceiver,
                intentFilter);

        DBManager.deleteEventComments(mEventId);
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
        if (!mAddingComment && isAcceptedMember()) {
            String message = mMessageView.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                mAddingComment = true;
                mMessageView.setText("");
                mAddCommentTask = new AddCommentTask();
                mAddCommentTask.execute(new Comment(message));
            }
        }
    }

    public void reloadComments() {
        if (!mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mSwipeRefreshLayout.setRefreshing(true);
            mListAdapter.clearItems();
            mListCommentsTask = new ListCommentsTask();
            mListCommentsTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true");
        }
    }

    public boolean isAcceptedMember() {
        // TODO
        return true;
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

}
