package com.fastfriends.android.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.AlbumActivity;
import com.fastfriends.android.activity.ConversationActivity;
import com.fastfriends.android.activity.EditProfileActivity;

import static com.fastfriends.android.fragment.NavigationDrawerFragment.NavigationDrawerInterface;

import com.fastfriends.android.activity.FitActivity;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.activity.SearchActivity;
import com.fastfriends.android.activity.SlideShowActivity;
import com.fastfriends.android.adapter.MutualFriendListAdapter;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.listener.PaginatedHScrollListener;
import com.fastfriends.android.listener.PaginatedScrollListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.EventMember;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;
import com.fastfriends.android.text.style.ClickableMovementMethod;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.fastfriends.android.text.style.TagSpan;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import retrofit.RetrofitError;


public class ProfileDetailsFragment extends Fragment {
    private final static String LOGTAG = ProfileDetailsFragment.class.getSimpleName();

    private static final String ARG_ID = "id";
    private static final String TAG_SEPARATOR = " ";
    public static final int REQUEST_PROFILE_EDIT = 3;
    public static final int REQUEST_SELECT_PHOTO = 4;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressFragment mProgressFragment;

    private Profile mProfile;
    private long mProfileId;
    private boolean mRefreshing;

    private ViewGroup mLayout;

    // Mutual Friends
    private static final int PAGE_SIZE = 20;
    private HListView mMutualFriendsList;
    private MutualFriendListAdapter mListAdapter;
    private PaginatedHScrollListener mPaginatedScrollListener;
    private ViewGroup mLoadingFooter;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            switch(view.getId()) {
                case R.id.action_button: {
                    if (User.isCurrentUser(mProfileId)) {
                        selectPortrait(activity);
                    } else {
                        sendMessage(activity);
                    }
                    break;
                }
                case R.id.action_button_fit: {
                    // Open fitness comparison activity
                    Intent intent = new Intent(getActivity(), FitActivity.class);
                    intent.putExtra(SlideShowActivity.EXTRA_USER_ID, mProfile.getId());
                    startActivity(intent);
                    break;
                }
                case R.id.portrait: {
                    // Open slideshow to portrait
                    Intent intent = new Intent(getActivity(), SlideShowActivity.class);
                    intent.putExtra(SlideShowActivity.EXTRA_USER_ID, mProfile.getId());
                    intent.putExtra(SlideShowActivity.EXTRA_RESOURCE_ID, mProfile.getPortraitId());
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Settings.USER_PORTRAIT.equals(key)) {
                if (mProfile != null) {
                    mProfile.setPortrait(sharedPreferences.getString(key, null));
                    populatePortrait();
                }
            }
        }
    };

    private class GetProfileTask extends AsyncTask<Void, Void, String> {
        Profile profile = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    profile = webService.getProfile(mProfileId, authToken.getAuthHeader());
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get profile details.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get profile details.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);

            if (error == null) {
                if (profile != null) {
                    mProfile = profile;

                    // Update profile portrait
                    SharedPreferences prefs = Settings.getSharedPreferences();
                    prefs.edit().putString(Settings.USER_PORTRAIT, profile.getPortrait()).commit();

                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.invalidateOptionsMenu();
                        activity.setTitle(getString(R.string.title_profile, profile.getDisplayName()));

                    }
                    populateDetails();
                    mMutualFriendsList.setOnScrollListener(mPaginatedScrollListener);
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private GetProfileTask mGetProfileTask;

    private class AddFriendTask extends AsyncTask<Void, Void, String> {
        Friend friend;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            FragmentActivity activity = getActivity();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    long currentUserId = Settings.getSharedPreferences().getLong(Settings.USER_ID, 0);
                    friend = webService.addFriend(authToken.getAuthHeader(), new Friend(currentUserId, mProfileId, false));
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't add friend.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't add friend.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (friend != null) {
                        Toast.makeText(activity, activity.getString(R.string.friend_added), Toast.LENGTH_SHORT).show();
                        reloadProfile();
                    }
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private AddFriendTask mAddFriendTask;

    private class GetMutualFriendsTask extends AsyncTask<String, Void, String> {
        Page<Friend> friendPage = null;
        boolean refresh;

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
                    friendPage = webService.getMutualFriends(mProfileId, authToken.getAuthHeader(), page, pageSize);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get mutual friends.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get mutual friends.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mRefreshing = false;
            showProgress(false, null);
            mMutualFriendsList.removeFooterView(mLoadingFooter);

            if (error == null) {
                if (friendPage != null) {
                    if (refresh) {
                        mListAdapter.reset(friendPage);
                        mPaginatedScrollListener.reset();
                    } else {
                        mListAdapter.addPage(friendPage);
                    }
                    mPaginatedScrollListener.setLoadingComplete(friendPage.getNext() != null);
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private GetMutualFriendsTask mGetMutualFriendsTask;

    public static ProfileDetailsFragment newInstance(long profileId) {
        ProfileDetailsFragment fragment = new ProfileDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, profileId);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mProfileId = bundle.getLong(ARG_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_profile_details, container, false);

        ImageView portrait = (ImageView) mLayout.findViewById(R.id.portrait);
        portrait.setOnClickListener(mOnClickListener);

        ImageButton actionButton = (ImageButton) mLayout.findViewById(R.id.action_button);
        actionButton.setOnClickListener(mOnClickListener);
        if (User.isCurrentUser(mProfileId)) {
            actionButton.setImageResource(R.drawable.ic_action_person);
        } else {
            actionButton.setImageResource(R.drawable.ic_action_chat);
        }

        ImageButton fitButton = (ImageButton) mLayout.findViewById(R.id.action_button_fit);
        fitButton.setOnClickListener(mOnClickListener);

        mMutualFriendsList = (HListView) mLayout.findViewById(R.id.mutual_friends);
        mMutualFriendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Friend friend = (Friend) mListAdapter.getItem(position);
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_USER_ID, friend.getUserId());
                    intent.putExtra(ProfileActivity.EXTRA_TITLE, friend.getUserName());
                    activity.startActivity(intent);
                }
            }
        });
        mLoadingFooter = (ViewGroup) inflater.inflate(R.layout.horizontal_list_footer_loading, mMutualFriendsList, false);
        mMutualFriendsList.addFooterView(mLoadingFooter);

        mListAdapter = new MutualFriendListAdapter(getActivity());
        mMutualFriendsList.setAdapter(mListAdapter);
        mMutualFriendsList.removeFooterView(mLoadingFooter);


        mPaginatedScrollListener = new PaginatedHScrollListener() {
            @Override
            public void loadPage(int page) {
                if (!mRefreshing) {
                    mRefreshing = true;
                    mMutualFriendsList.addFooterView(mLoadingFooter);
                    mGetMutualFriendsTask = new GetMutualFriendsTask();
                    mGetMutualFriendsTask.execute(String.valueOf(page), String.valueOf(PAGE_SIZE), String.valueOf(false));
                }
            }
        };

        return mLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadProfile();
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
        inflater.inflate(R.menu.profile_details, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        boolean shouldShowMenu;
        if (activity instanceof NavigationDrawerInterface) {
            NavigationDrawerInterface navigationDrawerInterface = (NavigationDrawerInterface) getActivity();
            shouldShowMenu = navigationDrawerInterface.shouldShowMenu();
        } else {
            shouldShowMenu = true;
        }
        if (shouldShowMenu && mProfile != null) {
            if (User.isCurrentUser(mProfileId)) {
                menu.findItem(R.id.action_edit).setVisible(true);
                menu.findItem(R.id.action_add_friend).setVisible(false);
            } else {
                menu.findItem(R.id.action_edit).setVisible(false);
                if (mProfile.isFriend()) {
                    menu.findItem(R.id.action_add_friend).setVisible(false);
                } else {
                    menu.findItem(R.id.action_add_friend).setVisible(true);
                }
            }
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_add_friend).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = getActivity();
        if (activity != null) {
            switch (item.getItemId()) {
                case R.id.action_edit: {
                    Intent intent = new Intent(activity, EditProfileActivity.class);
                    intent.putExtra(EditProfileActivity.EXTRA_PROFILE, mProfile);
                    getParentFragment().startActivityForResult(intent, REQUEST_PROFILE_EDIT);
                    return true;
                }
                case R.id.action_add_friend: {
                    mAddFriendTask = new AddFriendTask();
                    mAddFriendTask.execute();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PROFILE_EDIT: {
                break;
            }
            case REQUEST_SELECT_PHOTO: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Profile profile = bundle.getParcelable(AlbumActivity.EXTRA_PROFILE);
                            if (profile != null) {
                                mProfile = profile;

                                String portrait = mProfile.getPortrait();
                                if (portrait != null) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        SharedPreferences prefs = Settings.getSharedPreferences();
                                        prefs.edit().putString(Settings.USER_PORTRAIT, portrait).commit();
                                    }

                                    ImageView imageView = (ImageView) mLayout.findViewById(R.id.portrait);
                                    ImageLoader.getInstance().displayImage(portrait, imageView);
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Settings.getSharedPreferences().registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        Settings.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
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

    private void populateDetails() {
        Activity activity = getActivity();
        if (mProfile == null || activity == null) {
            return;
        }

        populatePortrait();
        TextView displayNameView = (TextView) mLayout.findViewById(R.id.display_name);
        displayNameView.setText(mProfile.getDisplayName());

        TextView ageView = (TextView) mLayout.findViewById(R.id.age);
        ageView.setText(String.valueOf(birthdayToAge(mProfile.getBirthday())));

        TextView genderView = (TextView) mLayout.findViewById(R.id.gender);
        genderView.setText(mProfile.getGender());

        TextView locationView = (TextView) mLayout.findViewById(R.id.location);
        locationView.setText(mProfile.getLocation());

        TextView aboutView = (TextView) mLayout.findViewById(R.id.about);
        String about = mProfile.getAbout();
        if (!TextUtils.isEmpty(about)) {
            CharSequence body = TagHelper.getInstance().markup(activity, about, mProfile.getMentions(), TagHelper.SEARCH_FRIENDS);
            aboutView.setMovementMethod(new LinkTouchMovementMethod());
            aboutView.setText(body);
        }

        TextView friendsTitleView = (TextView) mLayout.findViewById(R.id.mutual_friends_title);
        friendsTitleView.setText(activity.getString(R.string.title_mutual_friends, mProfile.getMutualFriendCount()));

        SeekBar reliabilityView = (SeekBar) mLayout.findViewById(R.id.reliability);
        reliabilityView.setProgress(mProfile.getReliability());
        reliabilityView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        ImageView reliabilityHelpView = (ImageView) mLayout.findViewById(R.id.reliability_help);
        reliabilityHelpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReliabilityHelp(view);
            }
        });
    }

    public void populatePortrait() {
        final ImageView imageView = (ImageView) mLayout.findViewById(R.id.portrait);
        String portrait = mProfile.getPortrait();
        if (portrait == null) {
            imageView.setImageDrawable(null);
        } else {
            ImageLoader.getInstance().displayImage(portrait, imageView);
        }
    }

    public void reloadProfile() {
        if (getActivity() != null && !mRefreshing) {
            mRefreshing = true;
            if (mProfile == null) {
                showProgress(true, null);
            }
            mGetProfileTask = new GetProfileTask();
            mGetProfileTask.execute();
        }
    }

    public int birthdayToAge(Date dateOfBirth) {
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }
        return age;
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

    public void sendMessage(Context context) {
        SharedPreferences prefs = Settings.getSharedPreferences();
        String portrait = prefs.getString(Settings.USER_PORTRAIT, null);

        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_ID, mProfile.getId());
        intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_NAME, mProfile.getDisplayName());
        intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_PORTRAIT, mProfile.getPortrait());
        intent.putExtra(ConversationActivity.EXTRA_CURRENT_USER_PORTRAIT, portrait);
        getParentFragment().startActivityForResult(intent, REQUEST_PROFILE_EDIT);
    }

    public void selectPortrait(Context context) {
        // Open album tab in selection mode
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.putExtra(AlbumActivity.EXTRA_USER_ID, mProfileId);
        intent.putExtra(AlbumActivity.EXTRA_SELECT_PORTRAIT, true);
        getParentFragment().startActivityForResult(intent, REQUEST_SELECT_PHOTO);
    }

    private void showReliabilityHelp(View view) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.reliability_help, null);

        PopupWindow popupWindow = new PopupWindow(layout, 400, 250, true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_bg));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view);
    }
}
