package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.Resource;
import com.fastfriends.android.model.User;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;


public class SlideShowItemFragment extends Fragment {
    private final static String LOGTAG = SlideShowItemFragment.class.getSimpleName();

    private static final String ARG_RESOURCE = "resource";
    private static final String ARG_OWNER_ID = "owner_id";
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_OWNER_ID = "event_owner_id";

    private static final String ACTION_SHOW_CAPTION = "show_caption";
    private static final String ACTION_HIDE_CAPTION = "hide_caption";
    private static final String EXTRA_RESOURCE_ID = "resource_id";

    private Resource mResource;
    private long mOwnerId;
    private long mEventId;
    private long mEventOwnerId;
    private boolean mEditMode;

    private ActionBar mActionBar;
    private ProgressBar mProgressBar;
    private View mCaptionLayout;
    private TextView mCaptionView;
    private EditText mEditCaptionView;
    private TextView mCounterView;

    private int mCaptionMaxLength;

    public interface OnEditListener {
        public void onEditStart();
        public void onEditEnd();
    }
    private OnEditListener mOnEditListener = null;

    private class CaptionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                long resourceId = extras.getLong(EXTRA_RESOURCE_ID);
                if (resourceId != mResource.getId()) {
                    // Another SlideShowItemFragment showed/hid the caption
                    String action = intent.getAction();
                    if (ACTION_HIDE_CAPTION.equals(action)) {
                        mCaptionLayout.setVisibility(View.GONE);
                    } else if (ACTION_SHOW_CAPTION.equals(action)) {
                        mCaptionLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
    private CaptionBroadcastReceiver mCaptionBroadcastReceiver;

    private class SetCaptionTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mActionBar.hide();
            mEditCaptionView.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            final String caption = params[0];
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mResource = webService.setResourceCaption(mResource.getId(), authToken.getAuthHeader(), caption);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't set resource caption.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't set resource caption.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mProgressBar.setVisibility(View.GONE);
            mActionBar.show();
            mEditCaptionView.setEnabled(true);
            mEditMode = false;

            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (mOnEditListener != null) {
                        mOnEditListener.onEditEnd();
                    }
                    mEditCaptionView.setVisibility(View.GONE);
                    mCounterView.setVisibility(View.GONE);
                    mCaptionView.setVisibility(View.VISIBLE);

                    // Reset actionBar icons
                    activity.invalidateOptionsMenu();

                    initCaption();
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();

                    mEditMode = true;
                    // Show keyboard
                    mEditCaptionView.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(mEditCaptionView, 0);
                }
            }
        }
    }
    private SetCaptionTask mSetCaptionTask;

    private class SetPortraitTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Profile profile = webService.setProfilePortrait(mOwnerId, authToken.getAuthHeader(), mResource.getId());
                    // Update profile portrait
                    SharedPreferences prefs = Settings.getSharedPreferences();
                    prefs.edit()
                        .putString(Settings.USER_PORTRAIT, profile.getPortrait())
                        .putLong(Settings.USER_PORTRAIT_ID, profile.getPortraitId())
                        .commit();
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't set profile portrait.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't set profile portrait.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mProgressBar.setVisibility(View.GONE);
            mEditMode = false;
            if (mOnEditListener != null) {
                mOnEditListener.onEditEnd();
            }

            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private SetPortraitTask mSetPortraitTask;

    private class SetPromoTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    final Event event = webService.setEventPromo(mEventId, authToken.getAuthHeader(), mResource.getId());
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't set event promo.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't set event promo.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mProgressBar.setVisibility(View.GONE);
            mEditMode = false;
            if (mOnEditListener != null) {
                mOnEditListener.onEditEnd();
            }

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
            mProgressBar.setVisibility(View.VISIBLE);
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
                Log.e(LOGTAG, "Can't delete resource", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't delete resource", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mProgressBar.setVisibility(View.GONE);
            mEditMode = false;
            if (mOnEditListener != null) {
                mOnEditListener.onEditEnd();
            }

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
                    activity.finish();
                }
            }
        }
    }
    DeleteResourcesTask mDeleteResourcesTask;

    public static SlideShowItemFragment newInstance(Resource resource, long ownerId, long eventId, long eventOwnerId) {
        SlideShowItemFragment fragment = new SlideShowItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESOURCE, resource);
        // Profile Album
        args.putLong(ARG_OWNER_ID, ownerId);

        // Event Album
        args.putLong(ARG_EVENT_ID, eventId);
        args.putLong(ARG_EVENT_OWNER_ID, eventOwnerId);

        fragment.setArguments(args);
        return fragment;
    }

    public SlideShowItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(ARG_RESOURCE)) {
                mResource = (Resource) bundle.getParcelable(ARG_RESOURCE);
                mOwnerId = bundle.getLong(ARG_OWNER_ID);
                mEventId = bundle.getLong(ARG_EVENT_ID);
                mEventOwnerId = bundle.getLong(ARG_EVENT_OWNER_ID);
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_slide_show_item, container, false);

        mProgressBar = (ProgressBar) layout.findViewById(R.id.progress);
        final ImageView imageView = (ImageView) layout.findViewById(R.id.portrait);
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mEditMode) {
                    if (mActionBar.isShowing()) {
                        showCaption(false);
                        mActionBar.hide();
                    } else {
                        showCaption(true);
                        mActionBar.show();
                    }
                }
            }
        });

        final String url = mResource.getUrl();
        ImageLoader.getInstance().displayImage(url, imageView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setVisibility(view.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mCaptionLayout = layout.findViewById(R.id.caption_layout);
        mCaptionView = (TextView) layout.findViewById(R.id.caption);
        initCaption();

        mEditCaptionView = (EditText) layout.findViewById(R.id.edit_caption);
        mEditCaptionView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to save the caption.
                    if (!event.isShiftPressed()) {
                        saveCaption();
                        return true;
                    }
                    return false;
                }

                saveCaption();
                return true;
            }
        });
        mEditCaptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int len = editable.length();
                mCounterView.setText(len + " / " + mCaptionMaxLength);
            }
        });

        mCaptionMaxLength = getActivity().getResources().getInteger(R.integer.caption_max_length);
        mCounterView = (TextView) layout.findViewById(R.id.counter);
        mCounterView.setText("0 / " + mCaptionMaxLength);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mActionBar.isShowing()) {
            mCaptionLayout.setVisibility(View.VISIBLE);
        } else {
            mCaptionLayout.setVisibility(View.GONE);
        }

        // Receive hide/show caption requests from other SlideShowItemFragments
        mCaptionBroadcastReceiver = new CaptionBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_HIDE_CAPTION);
        intentFilter.addAction(ACTION_SHOW_CAPTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCaptionBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCaptionBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mCaptionBroadcastReceiver);
            mCaptionBroadcastReceiver = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnEditListener) {
            mOnEditListener = (OnEditListener) activity;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.slide_show_item, menu);

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

        long currentUserId = Settings.getSharedPreferences().getLong(Settings.USER_ID, 0);
        if (shouldShowMenu && (currentUserId == mOwnerId || currentUserId == mEventOwnerId)) {
            if (mEditMode) {
                menu.findItem(R.id.action_edit).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_set_portrait).setVisible(false);
                menu.findItem(R.id.action_set_promo).setVisible(false);
                menu.findItem(R.id.action_cancel).setVisible(true);
                menu.findItem(R.id.action_done).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(true);
                if (mEventId > 0) {
                    menu.findItem(R.id.action_set_portrait).setVisible(false);
                    menu.findItem(R.id.action_set_promo).setVisible(true);
                } else {
                    menu.findItem(R.id.action_set_portrait).setVisible(true);
                    menu.findItem(R.id.action_set_promo).setVisible(false);
                }
                menu.findItem(R.id.action_cancel).setVisible(false);
                menu.findItem(R.id.action_done).setVisible(false);
            }
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_set_portrait).setVisible(false);
            menu.findItem(R.id.action_set_promo).setVisible(false);
            menu.findItem(R.id.action_cancel).setVisible(false);
            menu.findItem(R.id.action_done).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit: {
                editCaption();
                return true;
            }
            case R.id.action_delete: {
                deleteResource();
                return true;
            }
            case R.id.action_set_portrait: {
                setProfilePortrait();
                return true;
            }
            case R.id.action_set_promo: {
                setEventPromo();
                return true;
            }
            case R.id.action_cancel: {
                cancelEdit();
                return true;
            }
            case R.id.action_done: {
                saveCaption();
                return true;
            }
        }
        return false;
    }

    public void showCaption(boolean show) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESOURCE_ID, mResource.getId());

        if (show) {
            intent.setAction(ACTION_SHOW_CAPTION);

            mCaptionLayout.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_in);
            mCaptionLayout.startAnimation(anim);
        } else {
            intent.setAction(ACTION_HIDE_CAPTION);

            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mCaptionLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mCaptionLayout.startAnimation(anim);
        }

        // Broadcast to other SlideShowItemFragments to hide/show their caption
        Activity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }

    }

    private void editCaption() {
        Activity activity = getActivity();
        if (activity != null) {
            mEditMode = true;
            if (mOnEditListener != null) {
                mOnEditListener.onEditStart();
            }
            mCaptionView.setVisibility(View.GONE);
            mCounterView.setVisibility(View.VISIBLE);
            mEditCaptionView.setVisibility(View.VISIBLE);
            mEditCaptionView.setText(mResource.getCaption());
            mEditCaptionView.requestFocus();
            // Show keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(mEditCaptionView, 0);

            activity.invalidateOptionsMenu();
        }
    }

    private void saveCaption() {
        Activity activity = getActivity();
        if (activity != null) {
            // Hide keyboard
            final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        mSetCaptionTask = new SetCaptionTask();
        mSetCaptionTask.execute(mEditCaptionView.getText().toString());
    }

    private void cancelEdit() {
        Activity activity = getActivity();
        if (activity != null) {
            mProgressBar.setVisibility(View.GONE);
            mEditMode = false;
            if (mOnEditListener != null) {
                mOnEditListener.onEditEnd();
            }
            mCounterView.setVisibility(View.GONE);
            mEditCaptionView.setVisibility(View.GONE);
            mEditCaptionView.setEnabled(true);
            mCaptionView.setVisibility(View.VISIBLE);

            // Reset actionBar icons
            activity.invalidateOptionsMenu();

            // Hide keyboard
            final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    public void onBackPressed() {
        if (mEditMode) {
            cancelEdit();
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        }
    }

    private void setProfilePortrait() {
        mEditMode = true;
        mActionBar.hide();
        mCaptionLayout.setVisibility(View.GONE);
        if (mOnEditListener != null) {
            mOnEditListener.onEditStart();
        }

        mSetPortraitTask = new SetPortraitTask();
        mSetPortraitTask.execute();
    }

    private void setEventPromo() {
        mEditMode = true;
        mActionBar.hide();
        mCaptionLayout.setVisibility(View.GONE);
        if (mOnEditListener != null) {
            mOnEditListener.onEditStart();
        }

        mSetPromoTask = new SetPromoTask();
        mSetPromoTask.execute();
    }

    private void deleteResource() {
        mEditMode = true;
        mActionBar.hide();
        mCaptionLayout.setVisibility(View.GONE);
        if (mOnEditListener != null) {
            mOnEditListener.onEditStart();
        }

        List<Long> resourceIds = new ArrayList<Long>();
        resourceIds.add(mResource.getId());

        mDeleteResourcesTask = new DeleteResourcesTask();
        mDeleteResourcesTask.execute(resourceIds);
    }

    private void initCaption() {
        Activity activity = getActivity();
        CharSequence body = TagHelper.getInstance().markup(activity, mResource.getCaption(), mResource.getMentions(), TagHelper.SEARCH_EVENTS);
        mCaptionView.setMovementMethod(new LinkTouchMovementMethod());
        mCaptionView.setText(body);
    }
}
