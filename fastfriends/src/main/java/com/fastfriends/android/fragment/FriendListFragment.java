package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.activity.SearchActivity;
import com.fastfriends.android.adapter.FriendListAdapter;
import com.fastfriends.android.fragment.dialog.ImportContactsDialogFragment;
import com.fastfriends.android.helper.SharingHelper;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit.RetrofitError;


public class FriendListFragment extends Fragment implements ImportContactsDialogFragment.OnSelectedListener {
    private final static String LOGTAG = FriendListFragment.class.getSimpleName();

    private final static String IMPORT_CONTACTS_FRAGMENT = "import_contacts";

    private static final int PAGE_SIZE = 20;
    // Note: Indices must match with R.array.friend_category
    private static final int CATEGORY_NAME = 0;
    private static final int CATEGORY_FRIEND = 1;
    private static final int CATEGORY_RECENT = 2;
    private static final int CATEGORY_FREQUENT = 3;

    private ListView mListView;
    private FriendListAdapter mListAdapter;
    private PaginatedScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;
    private SpinnerAdapter mSpinnerAdapter;

    private View mHelpLayout;
    private TextView mHelpTextView;
    private Button mImportContactsButton;

    private boolean mRefreshing;
    private int mCategory;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;
    private boolean mFirstLoad = true;

    private class ImportContactsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            mRefreshing = false;
        }

        @Override
        protected String doInBackground(String... params) {
            String userIds = params[0];
            try {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                    if (authToken != null) {
                        FastFriendsWebService webService = WebServiceManager.getWebService();
                        JsonObject json = webService.importContacts(authToken.getAuthHeader(), userIds);
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't import contacts.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't import contacts.", e);
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
                reload();
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            if (mListAdapter.getCount() > 0) {
                mListView.setVisibility(View.VISIBLE);
                mHelpLayout.setVisibility(View.GONE);
            } else {
                mListView.setVisibility(View.GONE);
                mHelpLayout.setVisibility(View.VISIBLE);
            }
        }
    }
    ImportContactsTask mImportFriendsTask;

    private class ListFriendsTask extends AsyncTask<String, Void, String> {
        Page<Friend> mFriendPage = null;
        boolean mRefresh;

        @Override
        protected void onPreExecute() {
            mRefreshing = false;
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];
            mRefresh = Boolean.valueOf(params[2]);

            try {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                    if (authToken != null) {
                        FastFriendsWebService webService = WebServiceManager.getWebService();
                        switch (mCategory) {
                            case CATEGORY_NAME: {
                                mFriendPage = webService.listFriends(authToken.getAuthHeader(), page, pageSize, Friend.CATEGORY_NAME);
                                break;
                            }
                            case CATEGORY_FRIEND: {
                                mFriendPage = webService.listFriends(authToken.getAuthHeader(), page, pageSize, Friend.CATEGORY_FRIEND);
                                break;
                            }
                            case CATEGORY_RECENT: {
                                mFriendPage = webService.listFriends(authToken.getAuthHeader(), page, pageSize, Friend.CATEGORY_RECENT);
                                break;
                            }
                        }
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get friends.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get friends.", e);
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
                if (mFriendPage != null) {
                    if (mRefresh) {
                        mListAdapter.reset(mFriendPage);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(mFriendPage);
                    }
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            if (mListAdapter.getCount() > 0) {
                mListView.setVisibility(View.VISIBLE);
                mHelpLayout.setVisibility(View.GONE);
            } else {
                mListView.setVisibility(View.GONE);
                mHelpLayout.setVisibility(View.VISIBLE);
            }
        }
    }
    ListFriendsTask mListFriendsTask;

    public static FriendListFragment newInstance() {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_friend_list, container, false);

        mListView = (ListView) layout.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < mListAdapter.getCount()) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        Friend friend = (Friend) mListAdapter.getItem(position);
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        intent.putExtra(ProfileActivity.EXTRA_USER_ID, friend.getUserId());
                        intent.putExtra(ProfileActivity.EXTRA_TITLE, friend.getUserName());
                        activity.startActivity(intent);
                    }
                }
            }
        });

        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.list_footer_loading, mListView, false);
        mListView.addFooterView(mLoadingFooter);

        mListAdapter = new FriendListAdapter(getActivity(), false);
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
                    mListFriendsTask = new ListFriendsTask();
                    mListFriendsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), String.valueOf(false));
                }
            }
        };
        mListView.setOnScrollListener(mPaginatedScrollListener);

        mHelpLayout = layout.findViewById(R.id.help_layout);
        mHelpTextView = (TextView) layout.findViewById(R.id.friends_help);
        String str = getString(R.string.friends_help);
        SpannableString ss = new SpannableString(str);
        Drawable d = getResources().getDrawable(R.drawable.ic_rating_important);
        int starSize = (int) mHelpTextView.getTextSize();
        d.setBounds(0, 0, starSize, starSize);
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        int index = str.indexOf("*");
        ss.setSpan(span, index, index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mHelpTextView.setText(ss);

        mImportContactsButton = (Button) layout.findViewById(R.id.import_contacts);
        mImportContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportContactsDialog();
            }
        });

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
        inflater.inflate(R.menu.friend_list, menu);

        MenuItem item = menu.findItem(R.id.action_invite);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        SharingHelper helper = new SharingHelper(getActivity(),
                getString(R.string.invite_contacts),
                getString(R.string.invite_subject),
                getString(R.string.invite_message));
        shareActionProvider.setShareIntent(helper.createShareIntent());

        // Set history different from the default before getting the action
        // view since a call to MenuItemCompat.getActionView() calls
        // onCreateActionView() which uses the backing file name. Omit this
        // line if using the default share history file is desired.
        //mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");

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
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_invite).setVisible(true);
            menu.findItem(R.id.action_import).setVisible(true);
        } else {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_invite).setVisible(false);
            menu.findItem(R.id.action_import).setVisible(false);
        }
   }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_CATEGORY, SearchActivity.CATEGORY_PROFILES);
                startActivity(intent);
                return true;
            }
            case R.id.action_invite: {
                return true;
            }
            case R.id.action_import: {
                showImportContactsDialog();
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
            mListFriendsTask = new ListFriendsTask();
            mListFriendsTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), String.valueOf(true));
        }
    }

    private void initActionBar() {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mSpinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(),
                R.array.friend_category , android.R.layout.simple_spinner_dropdown_item);

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

    private void showImportContactsDialog() {
        FragmentManager fm = getChildFragmentManager();
        // TODO pass in list of already imported emails
        ImportContactsDialogFragment dialogFragment = ImportContactsDialogFragment.newInstance(new String[0]);
        dialogFragment.show(fm, IMPORT_CONTACTS_FRAGMENT);
    }

    @Override
    public void onSelected(List<Long> userIds) {
        String str = WebServiceManager.buildIdListString(userIds);
        mImportFriendsTask = new ImportContactsTask();
        mImportFriendsTask.execute(str);
    }
}
