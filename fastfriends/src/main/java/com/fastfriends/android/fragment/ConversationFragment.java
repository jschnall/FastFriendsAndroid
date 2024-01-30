package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import static android.view.ViewTreeObserver.OnPreDrawListener;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.adapter.MessageListAdapter;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.service.GcmIntentService;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;


public class ConversationFragment extends Fragment {
    private final static String LOGTAG = ConversationFragment.class.getSimpleName();

    private static final String ARG_OTHER_USER_ID = "user_id";

    private static final int PAGE_SIZE = 20;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    boolean mFirstLoad = true;

    private long mUserId;
    private boolean mAddingMessage;
    private boolean mRefreshing;
    private boolean mTextChanged;

    private EditText mMessageView;
    private ImageButton mSubmitButton;

    private ListView mListView;
    private MessageListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingHeader;

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(GcmIntentService.EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Message message = gson.fromJson(data, Message.class);

            // Add the new message to the conversation
            mListAdapter.addItem(message);

            // User is actively viewing conversation, so flag message as having been read
            mSetConversationOpenedTask = new SetConversationOpenedTask();
            mSetConversationOpenedTask.execute(mUserId);

            // Delete new message from unread messages in db so they won't be notified again
            DBManager.delete(Message.class, message.getId());

            setResultData(String.valueOf(true));
        }
    }
    private MessageReceiver mMessageReceiver;

    private class SaveDraftTask extends AsyncTask<Message, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Message... params) {
            Message message = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Message newMessage = webService.saveDraft(authToken.getAuthHeader(), message);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't save draft.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't save draft.", e);
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
    private SaveDraftTask mSaveDraftTask;

    private class DeleteDraftTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    List<Long> userIds = new ArrayList<Long>();
                    userIds.add(mUserId);
                    final JsonObject json = webService.deleteDrafts(authToken.getAuthHeader(), WebServiceManager.buildIdListString(userIds));
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't delete draft.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't delete draft.", e);
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
    private DeleteDraftTask mDeleteDraftTask;

    private class AddMessageTask extends AsyncTask<Message, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Message... params) {
            Message message = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Message newMessage = webService.addMessage(authToken.getAuthHeader(), message);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListAdapter.replaceItem(mListAdapter.getCount() - 1, newMessage);
                        }
                    });
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't add message.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't add message.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mAddingMessage = false;
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    activity.setResult(Activity.RESULT_OK); // refresh conversation list
                    reloadMessages();
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private AddMessageTask mAddMessageTask;

    private class ListMessagesTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            mListView.addHeaderView(mLoadingHeader);
        }

        @Override
        protected String doInBackground(String... params) {
            final String page = params[0];
            final String pageSize = params[1];
            final boolean refresh = Boolean.valueOf(params[2]);

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Page<Message> messagePage = webService.listMessages(authToken.getAuthHeader(), page, pageSize, mUserId);
                    String draftMessage = null;
                    List<Message> messages = messagePage.getResults();
                    // Messages come ordered by descending sent date
                    // if there is a draft it should always be the first one
                    for (Message message : messages) {
                        if (message.getSent() == null) {
                            draftMessage = message.getMessage();
                            messages.remove(message);
                            break;
                        }
                    }

                    final String message = draftMessage;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (message != null && "1".equals(page)) {
                                // Load existing draft
                                mMessageView.setText(message);
                                mMessageView.setSelection(mMessageView.getText().length());
                                mTextChanged = false;
                            }

                            if (refresh) {
                                mListAdapter.reset(messagePage);
                                mPaginatedScrollListener.reset();
                            } else {
                                addNewPage(messagePage);
                            }
                            mPaginatedScrollListener.setLoadingComplete(messagePage.getNext() != null);
                        }
                    });
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get messages.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get messages.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mListView.removeHeaderView(mLoadingHeader);
            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ListMessagesTask mListMessagesTask;

    private class SetConversationOpenedTask extends AsyncTask<Long, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Long... params) {
            long userId = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final JsonObject json = webService.setConversationOpened(authToken.getAuthHeader(), userId);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't set conversation opened.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't set conversation opened.", e);
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
    private SetConversationOpenedTask mSetConversationOpenedTask;

    public static ConversationFragment newInstance(long userId, String userName, boolean opened,
            String currentUserPortrait, String otherUserPortrait) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_OTHER_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getLong(ARG_OTHER_USER_ID);
        }

        mSetConversationOpenedTask = new SetConversationOpenedTask();
        mSetConversationOpenedTask.execute(mUserId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_conversation, container, false);


        mListView = (ListView) layout.findViewById(R.id.list);

        mLoadingHeader = (ViewGroup) inflater.inflate(R.layout.list_header_loading, mListView, false);
        mListView.addHeaderView(mLoadingHeader);

        mListAdapter = new MessageListAdapter(getActivity(), mUserId);
        mListView.setAdapter(mListAdapter);
        mListView.removeHeaderView(mLoadingHeader);

        mPaginatedScrollListener = new PaginatedScrollListener(true, 0) {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    if (mFirstLoad) {
                        mFirstLoad = false;
                        showProgress(true, null);
                    }
                    mListMessagesTask = new ListMessagesTask();
                    mListMessagesTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), "false");
                }
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        mMessageView = (EditText) layout.findViewById(R.id.message);
        mMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        addMessage();
                        return true;
                    }
                    return false;
                }

                addMessage();
                return true;
            }
        });
        mMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mTextChanged = true;
            }
        });

        mSubmitButton = (ImageButton) layout.findViewById(R.id.submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMessage();
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

        mMessageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter(GcmIntentService.ACTION_GCM_MESSAGE);
        // Default priority is 0.  Set a higher priority so this can intercept first
        intentFilter.setPriority(1);
        getActivity().registerReceiver(mMessageReceiver,
                intentFilter);

        DBManager.deleteMessages(mUserId);
        NotificationHelper.cancel(getActivity(), NotificationHelper.MESSAGE_NOTIFICATION_ID);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveDraft();
        if (mMessageReceiver != null) {
            getActivity().unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
        }
    }

    private void addMessage() {
        if (!mAddingMessage) {
            String messageStr = mMessageView.getText().toString();
            if (!TextUtils.isEmpty(messageStr)) {
                mAddingMessage = true;
                Message message = new Message(messageStr, mUserId);
                mListAdapter.addItem(message);
                mMessageView.setText("");
                mTextChanged = false;
                mAddMessageTask = new AddMessageTask();
                mAddMessageTask.execute(message);
            }
        }
    }

    private void saveDraft() {
        String message = mMessageView.getText().toString();
        if (mTextChanged) {
            if (TextUtils.isEmpty(message)) {
                mDeleteDraftTask = new DeleteDraftTask();
                mDeleteDraftTask.execute();
            } else {
                mSaveDraftTask = new SaveDraftTask();
                mSaveDraftTask.execute(new Message(message, mUserId));
            }
        }
    }

    public void reloadMessages() {
        if (!mRefreshing) {
            mRefreshing = true;
            if (mFirstLoad) {
                mFirstLoad = false;
                showProgress(true, null);
            }
            mListMessagesTask = new ListMessagesTask();
            mListMessagesTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true");
        }
    }

    public void addNewPage(final Page<Message> messagePage) {
        final OnPreDrawListener onPreDrawListener = new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                return false;
            }
        };
        mListView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);

        mListAdapter.addPage(messagePage);
        final int positionToSave = mListView.getFirstVisiblePosition() + messagePage.getResults().size() - 1;
        mListView.clearFocus();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(positionToSave);
                mListView.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
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

    public void onBackPressed() {
        if (mTextChanged) {
            getActivity().setResult(Activity.RESULT_OK); // refresh conversation list
        }
    }
}
