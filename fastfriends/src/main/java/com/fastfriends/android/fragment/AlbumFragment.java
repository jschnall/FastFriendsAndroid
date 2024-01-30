package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.Settings;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.service.FastFriendsService;
import com.fastfriends.android.R;
import com.fastfriends.android.activity.SlideShowActivity;
import com.fastfriends.android.adapter.ResourceListAdapter;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.Album;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.Resource;
import com.fastfriends.android.model.User;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import retrofit.RetrofitError;


public class AlbumFragment extends Fragment {
    private final static String LOGTAG = AlbumFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_SELECT_IMAGE = "select_image";
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_OWNER_ID = "event_owner_id";
    private static final String ARG_DISPLAY_TYPE = "display_type";

    public static final int REQUEST_SELECT_PHOTO = 5;

    public static final int DISPLAY_GRID = 0;
    public static final int DISPLAY_LIST = 1;

    private static final int PAGE_SIZE = 20;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;

    private ResourceListAdapter mResourceListAdapter;
    private long mUserId;
    private long mEventId;
    private long mEventOwnerId;
    private Album mAlbum;
    private boolean mRefreshing;
    private ViewGroup mLayout;
    private AbsListView mListView;
    private ActionMode mActionMode;
    private int mSelectedItemCount;
    private int mDisplayType;

    private boolean mSelectionMode = false;

    public interface ListFormatListener {
        public void showGrid();
        public void showList();
    }
    ListFormatListener mListFormatListener;

    public interface ProfileUpdateListener extends OnShowProgressListener {
        public void onProfileUpdated(Profile profile);
    }
    ProfileUpdateListener mProfileUpdateListener;

    public interface EventUpdateListener extends OnShowProgressListener {
        public void onEventUpdated(Event event);
    }
    EventUpdateListener mEventUpdateListener;

    private class UploadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Resource resource = extras.getParcelable(FastFriendsService.EXTRA_RESOURCE);
                if (resource != null) {
                    if (mSelectionMode) {
                        setProfilePortrait(resource.getId());
                    } else {
                        reload();
                    }
                }
            }
        }
    }
    UploadBroadcastReceiver mUploadBroadcastReceiver;

    private class SetPortraitTask extends AsyncTask<Long, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Long... params) {
            final long resourceId = params[0];
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Profile profile = webService.setProfilePortrait(mUserId, authToken.getAuthHeader(), resourceId);
                    // Update profile portrait
                    SharedPreferences prefs = Settings.getSharedPreferences();
                    prefs.edit()
                        .putString(Settings.USER_PORTRAIT, profile.getPortrait())
                        .putLong(Settings.USER_PORTRAIT_ID, profile.getPortraitId())
                        .commit();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (activity != null) {
                                if (mProfileUpdateListener != null) {
                                    mProfileUpdateListener.onProfileUpdated(profile);
                                }
                                activity.invalidateOptionsMenu();
                            }
                        }
                    });
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get profile_details.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get profile_details.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            //mPullToRefreshLayout.setRefreshComplete();

            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private SetPortraitTask mSetPortraitTask;

    private class SetPromoTask extends AsyncTask<Long, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Long... params) {
            final long resourceId = params[0];
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Event event = webService.setEventPromo(mEventId, authToken.getAuthHeader(), resourceId);
                    // Update event promotional image
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (activity != null) {
                                if (mEventUpdateListener != null) {
                                    mEventUpdateListener.onEventUpdated(event);
                                }
                                activity.invalidateOptionsMenu();
                            }
                        }
                    });
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get profile_details.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get profile_details.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            //mPullToRefreshLayout.setRefreshComplete();

            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private SetPromoTask mSetPromoTask;

    private class DeleteResourcesTask extends AsyncTask<List<Long>, Void, String> {
        List<Long> mResourceIds;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(List<Long>... params) {
            mResourceIds = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    String str = TextUtils.join(",", mResourceIds);
                    JsonObject json = webService.deleteResources(authToken.getAuthHeader(), str);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't delete resource(s)", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't delete resource(s)", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            Activity activity = getActivity();
            if (activity != null) {
                if (error != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences prefs = Settings.getSharedPreferences();
                    long resourceId = prefs.getLong(Settings.USER_PORTRAIT_ID, 0);
                    if (mResourceIds.contains(resourceId)) {
                        // Portrait resource was deleted
                        prefs.edit()
                                .putString(Settings.USER_PORTRAIT, null)
                                .putLong(Settings.USER_PORTRAIT_ID, 0)
                                .commit();
                    }
                    reload();
                }
            }
        }
    }
    DeleteResourcesTask mDeleteResourcesTask;

    private class UpdateAlbumTask extends AsyncTask<Album, Void, String> {
        @Override
        protected String doInBackground(Album... params) {
            Album album = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mAlbum = webService.editAlbum(album.getId(), authToken.getAuthHeader(), album);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.invalidateOptionsMenu();
                            }
                            mResourceListAdapter.reset(mAlbum.getResources());
                        }
                    });
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't update album.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't update albums", e);
                return e.getMessage();
            }

            return null;
        }
    }
    UpdateAlbumTask mUpdateAlbumTask;

    private class GetAlbumTask extends AsyncTask<String, Void, String> {
        Page<Album> albumPage;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String page = params[0];
            String pageSize = params[1];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    if (mEventId > 0) {
                        albumPage = webService.listEventAlbums(authToken.getAuthHeader(), page, pageSize, mEventId);
                    } else {
                        albumPage = webService.listUserAlbums(authToken.getAuthHeader(), page, pageSize, mUserId);
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get albums.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get albums.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            if (error == null) {
                if (albumPage.getCount() > 0) {
                    mAlbum = albumPage.getResults().get(0);
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.invalidateOptionsMenu();
                    }
                    mResourceListAdapter.reset(mAlbum.getResources());
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private GetAlbumTask mGetAlbumTask;

    public static AlbumFragment newInstance(Long userId, boolean portraitSelectionMode, int displayType) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        if (userId != null) {
            args.putLong(ARG_USER_ID, userId);
        }
        if (portraitSelectionMode) {
            args.putBoolean(ARG_SELECT_IMAGE, true);
        }
        args.putInt(ARG_DISPLAY_TYPE, displayType);
        fragment.setArguments(args);
        return fragment;
    }

    public static AlbumFragment newInstance(Long eventId, Long eventOwnerId, int displayType) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        if (eventId != null) {
            args.putLong(ARG_EVENT_ID, eventId);
        }
        if (eventOwnerId != null) {
            args.putLong(ARG_EVENT_OWNER_ID, eventOwnerId);
        }
        args.putInt(ARG_DISPLAY_TYPE, displayType);
        fragment.setArguments(args);
        return fragment;
    }

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            // Profile album
            if (bundle.containsKey(ARG_USER_ID)) {
                mUserId = bundle.getLong(ARG_USER_ID);
            }
            if (bundle.containsKey(ARG_SELECT_IMAGE)) {
                mSelectionMode = true;
            }

            // Event album
            if (bundle.containsKey(ARG_EVENT_ID)) {
                mEventId = bundle.getLong(ARG_EVENT_ID);
            }
            if (bundle.containsKey(ARG_EVENT_OWNER_ID)) {
                mEventOwnerId = bundle.getLong(ARG_EVENT_OWNER_ID);
            }

            mDisplayType = bundle.getInt(ARG_DISPLAY_TYPE);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_album, container, false);
        initList();

        return mLayout;
    }

    private void initList() {
        if (mDisplayType == DISPLAY_GRID) {
            mLayout.findViewById(R.id.list).setVisibility(View.GONE);
            mListView = (GridView) mLayout.findViewById(R.id.grid);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mListView = (ListView) mLayout.findViewById(R.id.list);
            mListView.setVisibility(View.VISIBLE);
            mLayout.findViewById(R.id.grid).setVisibility(View.GONE);
        }
        mResourceListAdapter = new ResourceListAdapter(getActivity(), mDisplayType);
        mListView.setAdapter(mResourceListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mSelectionMode) {
                    setProfilePortrait(id);
                } else {
                    // Show selected item in slideshow
                    Intent intent = new Intent(getActivity(), SlideShowActivity.class);
                    intent.putExtra(SlideShowActivity.EXTRA_ALBUM, mAlbum);
                    intent.putExtra(SlideShowActivity.EXTRA_INDEX, position);
                    startActivity(intent);
                }
            }
        });
        if (!mSelectionMode) {
            // Enable contextual options
            mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE_MODAL);
        }
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                if (checked) {
                    mSelectedItemCount = mResourceListAdapter.addSelection(id);
                } else {
                    mSelectedItemCount = mResourceListAdapter.removeSelection(id);
                }


                //mListView.findViewWithTag(id).invalidate();
                mResourceListAdapter.notifyDataSetChanged();

                // Update ContextActionBar
                String str = getResources().getQuantityString(R.plurals.items_selected, mSelectedItemCount, mSelectedItemCount);
                actionMode.setTitle(str);
                mActionMode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.album_context, menu);
                mActionMode = actionMode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                if (mSelectedItemCount == 1) {
                    menu.findItem(R.id.action_delete).setVisible(true);
                    //menu.findItem(R.id.action_set_cover).setVisible(true);
                    if (mUserId > 0) {
                        // This is a profile album
                        menu.findItem(R.id.action_set_portrait).setVisible(true);
                        menu.findItem(R.id.action_set_promo).setVisible(false);
                    } else if (mEventId > 0){
                        // This is an event album
                        menu.findItem(R.id.action_set_portrait).setVisible(false);
                        menu.findItem(R.id.action_set_promo).setVisible(true);
                    }
                } else if (mSelectedItemCount > 1) {
                    menu.findItem(R.id.action_delete).setVisible(true);
                    //menu.findItem(R.id.action_set_cover).setVisible(false);
                    menu.findItem(R.id.action_set_portrait).setVisible(false);
                    menu.findItem(R.id.action_set_promo).setVisible(false);
                } else {
                    menu.findItem(R.id.action_delete).setVisible(false);
                    //menu.findItem(R.id.action_set_cover).setVisible(false);
                    menu.findItem(R.id.action_set_portrait).setVisible(false);
                    menu.findItem(R.id.action_set_promo).setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete: {
                        deleteSelectedItems();
                        mResourceListAdapter.clearSelection();
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    }
                    /*
                    case R.id.action_set_cover: {
                        long resourceId = mResourceListAdapter.getSelectedItemIds().get(0);
                        setCover(resourceId);
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    }
                    */
                    case R.id.action_set_portrait: {
                        long resourceId = mResourceListAdapter.getSelectedItemIds().get(0);
                        setProfilePortrait(resourceId);
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    }
                    case R.id.action_set_promo: {
                        long resourceId = mResourceListAdapter.getSelectedItemIds().get(0);
                        setEventPromo(resourceId);
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    }

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                mResourceListAdapter.clearSelection();
                mActionMode = null;
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                mListView.setItemChecked(position, !mResourceListAdapter.isItemSelected(id));
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        reload();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mUploadBroadcastReceiver = new UploadBroadcastReceiver();
        LocalBroadcastManager.getInstance(activity).registerReceiver(mUploadBroadcastReceiver,
                new IntentFilter(FastFriendsService.RESULT_UPLOAD));
        if (activity instanceof ProfileUpdateListener) {
            mProfileUpdateListener = (ProfileUpdateListener) activity;
        }
        if (activity instanceof EventUpdateListener) {
            mEventUpdateListener = (EventUpdateListener) activity;
        }

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof ListFormatListener) {
            mListFormatListener = (ListFormatListener) parentFragment;
        } else if (activity instanceof ListFormatListener) {
            mListFormatListener = (ListFormatListener) activity;
        }

        activity.invalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        if (mUploadBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUploadBroadcastReceiver);
            mUploadBroadcastReceiver = null;
        }
        mProfileUpdateListener = null;
        mEventUpdateListener = null;
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean showMenu = true;
        Activity activity = getActivity();
        if (activity instanceof NavigationDrawerFragment.NavigationDrawerInterface) {
            NavigationDrawerFragment.NavigationDrawerInterface navigationDrawerInterface = (NavigationDrawerFragment.NavigationDrawerInterface) getActivity();
            showMenu = navigationDrawerInterface.shouldShowMenu();
        }
        if (showMenu && mAlbum != null) {
            // Album is loaded and active
            if (mListFormatListener != null) {
                if (mDisplayType == DISPLAY_LIST) {
                    menu.findItem(R.id.action_view_as_grid).setVisible(true);
                    menu.findItem(R.id.action_view_as_list).setVisible(false);
                } else {
                    menu.findItem(R.id.action_view_as_grid).setVisible(false);
                    menu.findItem(R.id.action_view_as_list).setVisible(true);
                }
            } else {
                menu.findItem(R.id.action_view_as_grid).setVisible(true);
                menu.findItem(R.id.action_view_as_list).setVisible(false);
            }

            if (isEditable()) {
                menu.findItem(R.id.action_add_resource).setVisible(true);
            } else {
                menu.findItem(R.id.action_add_resource).setVisible(false);
            }
        } else {
            menu.findItem(R.id.action_view_as_grid).setVisible(false);
            menu.findItem(R.id.action_add_resource).setVisible(false);
            menu.findItem(R.id.action_view_as_list).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_resource: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Fragment parent = getParentFragment();
                if (parent == null) {
                    startActivityForResult(intent, REQUEST_SELECT_PHOTO);
                }  else {
                    parent.startActivityForResult(intent, REQUEST_SELECT_PHOTO);
                }
                return true;
            }
            case R.id.action_view_as_grid: {
                if (mListFormatListener != null) {
                    mListFormatListener.showGrid();
                    item.setVisible(false);
                }
                return true;
            }
            case R.id.action_view_as_list: {
                if (mListFormatListener != null) {
                    mListFormatListener.showList();
                    item.setVisible(false);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_PHOTO: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle == null) {
                            // Load bitmap from uri
                            Uri uri = data.getData();
                            if (uri != null) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    if (mSelectionMode) {
                                        if (mProfileUpdateListener != null) {
                                            mProfileUpdateListener.showProgress(true, getResources().getString(R.string.progress_please_wait));
                                        } else if (mEventUpdateListener != null) {
                                            mEventUpdateListener.showProgress(true, getResources().getString(R.string.progress_please_wait));
                                        }
                                    }
                                    FastFriendsService.startActionUpload(activity, uri, mAlbum.getId());
                                }
                            }
                        } else {
                            // TODO Image data included in bundle
                        }
                    }
                }
                break;
            }
        }
    }

    private void reload() {
        if (getActivity() != null && !mRefreshing) {
            if (mAlbum == null) {
                showProgress(true, null);
            }
            mRefreshing = true;
            mGetAlbumTask = new GetAlbumTask();
            mGetAlbumTask.execute(String.valueOf(1), String.valueOf(PAGE_SIZE), "true");
        }
    }

    private boolean isEditable() {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }
        if (mEventOwnerId > 0) {
            // Event Album
            return User.isCurrentUser(mEventOwnerId);
        }
        // Profile Album
        return User.isCurrentUser(mUserId);
    }

    private void deleteSelectedItems() {
        mDeleteResourcesTask = new DeleteResourcesTask();
        mDeleteResourcesTask.execute(mResourceListAdapter.getSelectedItemIds());
    }

    private void setCover(long resourceId) {
        Album album = new Album();
        album.setId(mAlbum.getId());
        album.setCover(resourceId);
        mUpdateAlbumTask = new UpdateAlbumTask();
        mUpdateAlbumTask.execute(album);
    }

    private void setProfilePortrait(long resourceId) {
        if (mSelectionMode && mProfileUpdateListener != null) {
            mProfileUpdateListener.showProgress(true, getResources().getString(R.string.progress_please_wait));
        }

        if (mUserId > 0) {
            mSetPortraitTask = new SetPortraitTask();
            mSetPortraitTask.execute(resourceId);
        }
    }

    private void setEventPromo(long resourceId) {
        if (mSelectionMode && mProfileUpdateListener != null) {
            mProfileUpdateListener.showProgress(true, getResources().getString(R.string.progress_please_wait));
        }

        if (mEventId > 0) {
            mSetPromoTask = new SetPromoTask();
            mSetPromoTask.execute(resourceId);
        }
    }

    public void showProgress(boolean show, String message) {
        Activity activity = getActivity();
        if (activity != null) {
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
