package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.adapter.FriendListAdapter;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by jschnall on 2/20/14.
 */
public class InviteFriendsDialogFragment extends DialogFragment {
    private final static String LOGTAG = InviteFriendsDialogFragment.class.getSimpleName();
    private final static String EMAIL_FILE_NAME = "emails";

    // Fragment launch args
    public static final String ARG_EVENT_ID = "event_id";

    private static final int PAGE_SIZE = 20;

    private long mEventId;

    // Progress
    private View mStatusLayout;
    private TextView mStatusTextView;

    private AlertDialog mDialog;
    private ListView mListView;
    private FriendListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;


    public interface OnSelectedListener {
        public void onSelected(List<Long> userIds);
    }
    private OnSelectedListener mOnSelectedListener;


    private class ListFriendsTask extends AsyncTask<String, Void, String> {
        Page<Friend> friendPage = null;
        boolean refresh = false;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            refresh = Boolean.valueOf(params[2]);

            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    friendPage = webService.listNonMemberFriends(authToken.getAuthHeader(), page,
                            pageSize, Friend.CATEGORY_NAME, mEventId);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't list friends.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't list friends.", e);
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mListView.removeFooterView(mLoadingFooter);

            if (error == null) {
                if (friendPage != null) {
                    if (refresh) {
                        mListAdapter.reset(friendPage);
                    } else {
                        mListAdapter.addPage(friendPage);
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
    ListFriendsTask mListFriendsTask;

    public InviteFriendsDialogFragment() {
        // Required empty constructor
    }

    public static InviteFriendsDialogFragment newInstance(long eventId) {
        InviteFriendsDialogFragment fragment = new InviteFriendsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mEventId = bundle.getLong(ARG_EVENT_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();

        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_event_invite, null);

        mStatusLayout = layout.findViewById(R.id.status);
        mStatusTextView = (TextView) layout.findViewById(R.id.status_message);

        mListView = (ListView) layout.findViewById(R.id.list);

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new FriendListAdapter(activity, true);
        mListView.setAdapter(mListAdapter);
        mListView.removeFooterView(mLoadingFooter);

        mPaginatedScrollListener = new PaginatedScrollListener() {
            @Override
            public void loadPage(int page) {
                    mListView.addFooterView(mLoadingFooter);
                    mListFriendsTask = new ListFriendsTask();
                    mListFriendsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), String.valueOf(false));
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        mDialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setTitle(R.string.dialog_invite_friends_title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.invite_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mOnSelectedListener != null) {
                            mOnSelectedListener.onSelected(mListAdapter.getSelectedUserIds());
                        }
                    }
                }).create();

        // If listener used in setAdapter above, the dialog is dismissed on click
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListAdapter.toggleSelection(position);
                if (mListAdapter.getSelectedUserIds().size() > 0) {
                    mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                }

            }
        });

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return mDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnSelectedListener) {
            mOnSelectedListener = (OnSelectedListener) getParentFragment();
        }
        else if (activity instanceof OnSelectedListener) {
            mOnSelectedListener = (OnSelectedListener) activity;
        }
    }

    public void showProgress(final boolean show, String message) {
        mStatusTextView.setText(message);
        mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}