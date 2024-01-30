package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.ConversationActivity;
import com.fastfriends.android.adapter.ConversationListAdapter;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Conversation;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.UserStatus;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.RetrofitError;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class ConversationListFragment extends Fragment {
    private final static String LOGTAG = ConversationListFragment.class.getSimpleName();

    private static final String ARG_CATEGORY = "category";

    public static final int REQUEST_CONVERSATION_VIEW = 1;

    private static final int PAGE_SIZE = 20;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    boolean mFirstLoad = true;

    private String mCategory = null;
    private boolean mRefreshing;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private ConversationListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mListHeader;
    private ViewGroup mLoadingFooter;

    private int mMessageCount;
    private int mMaxMessages;

    private ViewGroup mLayout;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Settings.MESSAGE_COUNT.equals(key)) {
                mMessageCount = sharedPreferences.getInt(key, 0);
                refreshMessageCount();
            } else if (Settings.MAX_MESSAGES.equals(key)) {
                mMaxMessages = sharedPreferences.getInt(key, 0);
                refreshMessageCount();
            }
        }
    };

    private class DeleteDraftsTask extends AsyncTask<List<Long>, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(List<Long>... params) {
            List<Long> userIds = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final JsonObject json = webService.deleteDrafts(authToken.getAuthHeader(), WebServiceManager.buildIdListString(userIds));

                    UserStatus userStatus = webService.getUserStatus(authToken.getAuthHeader());
                    Settings.saveUserStatus(userStatus);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't delete drafts.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't delete drafts.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private DeleteDraftsTask mDeleteDraftsTask;

    private class DeleteConversationsTask extends AsyncTask<List<Long>, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(List<Long>... params) {
            List<Long> userIds = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final JsonObject json = webService.setConversationDeleted(authToken.getAuthHeader(), WebServiceManager.buildIdListString(userIds));

                    UserStatus userStatus = webService.getUserStatus(authToken.getAuthHeader());
                    Settings.saveUserStatus(userStatus);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't delete conversation(s)", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't delete conversation(s)", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
    DeleteConversationsTask mDeleteConversationsTask;

    private class ListConversationsTask extends AsyncTask<String, Void, String> {
        Page<Conversation> conversationPage;
        boolean refresh;

        @Override
        protected void onPreExecute() {
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
                    conversationPage = webService.listConversations(authToken.getAuthHeader(), page, pageSize, mCategory);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get " + mCategory , e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get " + mCategory, e);
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
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (refresh) {
                        mListAdapter.reset(conversationPage);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(conversationPage);
                    }
                    mPaginatedScrollListener.setLoadingComplete(conversationPage.getNext() != null);
                } else {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ListConversationsTask mListConversationsTask;

    public static ConversationListFragment newInstance(String category) {
        ConversationListFragment fragment = new ConversationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public ConversationListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCategory = bundle.getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Init message counts
        SharedPreferences prefs = Settings.getSharedPreferences();
        mMessageCount = prefs.getInt(Settings.MESSAGE_COUNT, 0);
        mMaxMessages = prefs.getInt(Settings.MAX_MESSAGES, 0);

        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_conversation_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mLayout.findViewById(R.id.ptr_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadConversations();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_orange);

        mListView = (ListView) mLayout.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Conversation conversation;
                if (mListHeader == null) {
                    conversation = (Conversation) mListAdapter.getItem(position);
                } else {
                    conversation = (Conversation) mListAdapter.getItem(position - 1);
                }
                conversation.setOpened(new Date());

                long otherUserId;
                String otherUserName;
                String currentUserPortrait;
                String otherUserPortrait;
                if (Conversation.CATEGORY_RECEIVED.equals(mCategory)) {
                    otherUserId = conversation.getSender();
                    otherUserName = conversation.getSenderName();
                    otherUserPortrait = conversation.getSenderPortrait();
                    currentUserPortrait = conversation.getReceiverPortrait();
                } else {
                    otherUserId = conversation.getReceiver();
                    otherUserName = conversation.getReceiverName();
                    otherUserPortrait = conversation.getReceiverPortrait();
                    currentUserPortrait = conversation.getSenderPortrait();
                }

                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_ID, otherUserId);
                intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_NAME, otherUserName);
                intent.putExtra(ConversationActivity.EXTRA_CURRENT_USER_PORTRAIT, currentUserPortrait);
                intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_PORTRAIT, otherUserPortrait);
                getParentFragment().startActivityForResult(intent, REQUEST_CONVERSATION_VIEW);
            }
        });
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                if (checked) {
                    mListAdapter.addSelection(id);
                } else {
                    mListAdapter.removeSelection(id);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.conversation_list_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        deleteSelectedItems();
                        mListAdapter.clearSelection();
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                mListAdapter.clearSelection();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                mListView.setItemChecked(position, !mListAdapter.isItemSelected(id));
                return false;
            }
        });

        if (Conversation.CATEGORY_RECEIVED.equals(mCategory)) {
            // Add header
            mListHeader = (ViewGroup) inflater.inflate(R.layout.header_message_count, mListView, false);
            mListHeader.setClickable(true); // Prevent list header triggering onItemClickListener
            refreshMessageCount();
            mListView.addHeaderView(mListHeader);
        }

        // Add footer
        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new ConversationListAdapter(getActivity(), mCategory);
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
                    mListConversationsTask = new ListConversationsTask();
                    mListConversationsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false");
                }
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        return mLayout;
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

        Settings.getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        Settings.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            DBManager.delete(Message.class, null);
            NotificationHelper.cancel(getActivity(), NotificationHelper.MESSAGE_NOTIFICATION_ID);
        }
    }

    public void reloadConversations() {
        if (!mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mSwipeRefreshLayout.setRefreshing(true);
            mListConversationsTask = new ListConversationsTask();
            mListConversationsTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true");
        }
    }

    private void deleteSelectedItems() {
        if (Conversation.CATEGORY_DRAFTS.equals(mCategory)) {
            mDeleteDraftsTask = new DeleteDraftsTask();
            mDeleteDraftsTask.execute(mListAdapter.getSelectedItemIds());
        } else {
            mDeleteConversationsTask = new DeleteConversationsTask();
            mDeleteConversationsTask.execute(mListAdapter.getSelectedItemIds());
        }
    }

    private void refreshMessageCount() {
        if (mListHeader != null) {
            ProgressBar progressBar = (ProgressBar) mListHeader.findViewById(R.id.progress);
            progressBar.setProgress(mMessageCount);
            progressBar.setMax(mMaxMessages);

            TextView messageCount = (TextView) mListHeader.findViewById(R.id.message_count);
            messageCount.setText(mMessageCount + "/" + mMaxMessages);
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
}
