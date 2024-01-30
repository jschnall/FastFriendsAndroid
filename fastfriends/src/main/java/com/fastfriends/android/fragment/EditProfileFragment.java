package com.fastfriends.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.EditProfileActivity;
import com.fastfriends.android.fragment.dialog.ProgressDialogFragment;
import com.fastfriends.android.fragment.dialog.TagPickerDialogFragment;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;
import com.fastfriends.android.view.FFEditText;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit.RetrofitError;


public class EditProfileFragment extends Fragment {
    private final static String LOGTAG = EditProfileFragment.class.getSimpleName();

    private static final String ARG_PROFILE = "profile";

    private static final String CONFIRM_DISCARD_FRAGMENT = "confirm_discard";
    private static final String PROGRESS_FRAGMENT = "progress";
    private final static String DATE_PICKER_FRAGMENT = "date_picker";

    // TODO
    private User mOldUser;
    private User mNewUser;

    private Profile mOldProfile;
    private Profile mNewProfile;

    private Calendar mBirthdayCalendar;

    // UI
    private FFEditText mDisplayNameView;
    private Button mBirthdayView;
    private RadioGroup mGenderRadioGroup;
    private RadioButton mFemaleGenderRadioButton;
    private RadioButton mMaleGenderRadioButton;
    private FFEditText mFirstNameView;
    private FFEditText mLastNameView;
    private FFEditText mEmailView;
    private Button mInterestsView;
    private FFEditText mAboutView;

    // Validation
    private boolean mSubmitting = false;
    private TextView mViewToFocus;
    private TextView mFocusedView = null;
    private boolean mHasSubmitted = false;

    private OnShowProgressListener mOnShowProgressListener;
    private ProgressDialogFragment mProgressDialogFragment;

    private Boolean mDatesModified = false;
    private Boolean mTagsModified = false;

    private HashSet<String> mTakenEmails;
    Intent mResultIntent;

    public static class ConfirmDiscardDialogFragment extends DialogFragment {
        FragmentActivity fragmentActivity;

        public ConfirmDiscardDialogFragment() {
            // Required empty public constructor
        }

        public static ConfirmDiscardDialogFragment newInstance() {
            ConfirmDiscardDialogFragment fragment = new ConfirmDiscardDialogFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_confirm_discard_event_title)
                    .setMessage(R.string.dialog_confirm_discard_event_message)
                    .setNegativeButton(R.string.dialog_confirm_discard_event_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setPositiveButton(R.string.dialog_confirm_discard_event_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            getActivity().finish();
                        }
                    })
                    .create();
            return dialog;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            fragmentActivity = (FragmentActivity) activity;
        }

    };

    private class EditProfileTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            long id = mOldProfile.getId();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mNewProfile = webService.editProfile(id, authToken.getAuthHeader(), mNewProfile);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't edit profile with id " + id, retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't edit profile with id " + id, e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mSubmitting = false;
            if (mOnShowProgressListener == null) {
                mProgressDialogFragment.dismiss();
            } else {
                mOnShowProgressListener.showProgress(false, null);
            }

            if (error == null) {
                Activity activity = getActivity();
                if (activity != null) {
                    SharedPreferences prefs = Settings.getSharedPreferences();
                    prefs.edit().putString(Settings.DISPLAY_NAME, mNewProfile.getDisplayName()).commit();

                    mResultIntent.putExtra(EditProfileActivity.EXTRA_PROFILE, mNewProfile);
                    activity.setResult(Activity.RESULT_OK, mResultIntent);
                    activity.finish();
                }
            } else {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private EditProfileTask mEditProfileTask;

    private class EditUserTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            long id = mOldUser.getId();
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    User user = webService.editUser(id, authToken.getAuthHeader(), mNewUser);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't edit user with id " + id, retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't edit user with id " + id, e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mSubmitting = false;
            if (mOnShowProgressListener == null) {
                mProgressDialogFragment.dismiss();
            } else {
                mOnShowProgressListener.showProgress(false, null);
            }

            if (error == null) {
                mResultIntent.putExtra(EditProfileActivity.EXTRA_USER, mNewUser);

                Activity activity = getActivity();
                if (activity != null) {
                    activity.setResult(Activity.RESULT_OK, mResultIntent);
                    activity.finish();
                }
            } else {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private EditUserTask mEditUserTask;

    private class GetCurrentUserTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mOldUser = webService.getCurrentUser(authToken.getAuthHeader());
                    mNewUser = new User();
                    mNewUser.copy(mOldUser);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get current user", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get current user", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (mOnShowProgressListener == null) {
                mProgressDialogFragment.dismiss();
            } else {
                mOnShowProgressListener.showProgress(false, null);
            }

            if (error == null) {
                populateUser();
            } else {
                getActivity().finish();
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private GetCurrentUserTask mGetCurrentUserTask;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.birthday: {
                    FragmentManager fm = getChildFragmentManager();
                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                            .newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
                                    mDatesModified = true;
                                    mBirthdayView.setText(formatDate(year, month, day));
                                    mBirthdayCalendar.set(year, month, day);
                                }
                            }, mBirthdayCalendar.get(Calendar.YEAR),
                                    mBirthdayCalendar.get(Calendar.MONTH),
                                    mBirthdayCalendar.get(Calendar.DAY_OF_MONTH));
                    calendarDatePickerDialog.show(fm, DATE_PICKER_FRAGMENT);
                    break;
                }
            }
        }
    };

    public static EditProfileFragment newInstance(Profile profile) {
        EditProfileFragment fragment = new EditProfileFragment();
        if (profile != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_PROFILE, profile);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mTakenEmails = new HashSet<String>();
        mResultIntent = new Intent();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOldProfile = bundle.getParcelable(ARG_PROFILE);
        }
        mNewProfile = new Profile();
        mNewProfile.copy(mOldProfile);

        mBirthdayCalendar = Calendar.getInstance();
        mBirthdayCalendar.setTime(mNewProfile.getBirthday());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // Profile fields
        mDisplayNameView = (FFEditText) layout.findViewById(R.id.display_name);

        mBirthdayView = (Button) layout.findViewById(R.id.birthday);
        mBirthdayView.setOnClickListener(mOnClickListener);

        mGenderRadioGroup = (RadioGroup) layout.findViewById(R.id.gender);
        mFemaleGenderRadioButton = (RadioButton) layout.findViewById(R.id.gender_female);
        mMaleGenderRadioButton = (RadioButton) layout.findViewById(R.id.gender_male);

        mAboutView = (FFEditText) layout.findViewById(R.id.about);

        // User fields
        mFirstNameView = (FFEditText) layout.findViewById(R.id.first_name);
        mLastNameView = (FFEditText) layout.findViewById(R.id.last_name);
        mEmailView = (FFEditText) layout.findViewById(R.id.email);

        populateProfile();

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUser();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel: {
                if (isModified()) {
                    showConfirmDiscardDialog();
                } else {
                    getActivity().finish();
                }
                return true;
            }
            case R.id.action_done: {
                submit();
                return true;
            }
        }
        return false;
    }

    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return formatDate(calendar);
    }

    private String formatDate(Calendar calendar) {
        if (DateHelper.isToday(calendar)) {
            return getString(R.string.today);
        }
        if (DateHelper.isWithinDaysFuture(calendar, 1)) {
            return getString(R.string.tomorrow);
        }

        Date date = calendar.getTime();
        return DateFormat.getDateFormat(getActivity()).format(date);
    }

    private String formatTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, hour, minute);

        return formatTime(calendar);
    }

    private String formatTime(Calendar calendar) {
        return DateFormat.getTimeFormat(getActivity()).format(calendar.getTime());
    }

    private String formatTags(List<String> tags) {
        if (tags.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = tags.iterator();
        builder.append(iter.next());
        while(iter.hasNext()) {
            builder.append(", " + iter.next());
        }
        return builder.toString();
    }


    private void submit() {
        if (mSubmitting) {
            return;
        }
        mSubmitting = true;

        if (!validateSubmit()) {
            if (mViewToFocus != null) {
                mViewToFocus.requestFocus();
                Toast.makeText(getActivity(), mViewToFocus.getError(), Toast.LENGTH_SHORT).show();
            }
            mSubmitting = false;
            return;
        }

        // Hide keyboard and show progress
        Activity activity = getActivity();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDisplayNameView.getWindowToken(), 0);
        if (mOnShowProgressListener == null) {
            showProgressDialogFragment();
        } else {
            mOnShowProgressListener.showProgress(true, getString(R.string.progress_saving_profile));
        }

        if (isProfileModified()) {
            mEditProfileTask = new EditProfileTask();
            mEditProfileTask.execute();
        }
        if (isUserModified()) {
            mEditUserTask = new EditUserTask();
            mEditUserTask.execute();
        }
    }

    private boolean validateSubmit() {
        mHasSubmitted = true;
        mViewToFocus = null;

        return validate(mDisplayNameView, false) & validate(mBirthdayView, false) & validate(mMaleGenderRadioButton, false) &
                validate(mFirstNameView, false) & validate(mLastNameView, false) &
                validate(mEmailView, false) & validate(mAboutView, false);
    }

    private boolean validate(TextView view, boolean hideErrorsOnly) {
        if (view == null || !mHasSubmitted) {
            return false;
        }

        switch (view.getId()) {
            case R.id.display_name: {
                String displayName = mDisplayNameView.getText().toString();
                if (TextUtils.isEmpty(displayName)) {
                    if (!hideErrorsOnly) {
                        mDisplayNameView.setError(getString(R.string.error_display_name_required));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                }
                mNewProfile.setDisplayName(displayName);
                break;
            }
            case R.id.birthday: {
                mNewProfile.setBirthday(mBirthdayCalendar.getTime());
                break;
            }
            case R.id.gender_male: {
                switch(mGenderRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.gender_female:
                        mNewProfile.setGender(Profile.GENDER_FEMALE);
                        break;
                    case R.id.gender_male:
                        mNewProfile.setGender(Profile.GENDER_MALE);
                        break;
                }
                if (TextUtils.isEmpty(mNewProfile.getGender())) {
                    if (!hideErrorsOnly) {
                        mMaleGenderRadioButton.setError(getString(R.string.error_gender_required));
                        setViewToFocus(mMaleGenderRadioButton);
                    }
                    return false;
                }
                break;
            }
            case R.id.first_name: {
                String firstName = mFirstNameView.getText().toString();
                if (TextUtils.isEmpty(firstName)) {
                    if (!hideErrorsOnly) {
                        mFirstNameView.setError(getString(R.string.error_first_name_required));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                } else if (!CreateAccountFragment.USER_NAME_PATTERN.matcher(firstName).matches()) {
                    if (!hideErrorsOnly) {
                        mFirstNameView.setError(getString(R.string.error_name_invalid));
                        setViewToFocus(mFirstNameView);
                    }
                    return false;
                }
                mNewUser.setFirstName(firstName);
                break;
            }
            case R.id.last_name: {
                String lastName = mLastNameView.getText().toString();
                if (TextUtils.isEmpty(lastName)) {
                    if (!hideErrorsOnly) {
                        mLastNameView.setError(getString(R.string.error_first_name_required));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                } else if (!CreateAccountFragment.USER_NAME_PATTERN.matcher(lastName).matches()) {
                    if (!hideErrorsOnly) {
                        mLastNameView.setError(getString(R.string.error_name_invalid));
                        setViewToFocus(mFirstNameView);
                    }
                    return false;
                }
                mNewUser.setLastName(lastName);
                break;
            }
            case R.id.email: {
                String email = mEmailView.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_required));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                } else if (email.length() < CreateAccountFragment.EMAIL_LEN_MIN) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_length));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                } else if (!CreateAccountFragment.EMAIL_PATTERN.matcher(email).matches()) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_invalid));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                } else if (mTakenEmails.contains(email)) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_taken));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                }
                mNewUser.setEmail(email);
                break;
            }
            case R.id.about: {
                mNewProfile.setAbout(mAboutView.getText().toString());
                break;
            }
        }

        view.setError(null);
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnShowProgressListener = (OnShowProgressListener) activity;
        } catch (ClassCastException e) {
            Log.d(LOGTAG, "No OnShowProgressListener attached.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnShowProgressListener = null;
    }

    private void showProgressDialogFragment() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string.progress_signing_in));
        getChildFragmentManager().beginTransaction()
                .add(mProgressDialogFragment, PROGRESS_FRAGMENT)
                .commitAllowingStateLoss();
    }

    private void setViewToFocus(TextView viewToFocus) {
        if (mViewToFocus == null) {
            mViewToFocus = viewToFocus;
        }
    }

    private boolean isModified() {
        return isUserModified() || isProfileModified();
    }

    private boolean isUserModified() {
        return !mFirstNameView.getText().toString().equals(mOldUser.getFirstName()) ||
                !mLastNameView.getText().toString().equals(mOldUser.getLastName()) ||
                !mEmailView.getText().toString().equals(mOldUser.getEmail());
    }

    private boolean isProfileModified() {
        return mDatesModified || mTagsModified ||
                !mDisplayNameView.getText().toString().equals(mOldProfile.getDisplayName()) ||
                !mAboutView.getText().toString().equals(mOldProfile.getAbout());
    }

    public boolean onBackPressed() {
        if (mSubmitting) {
            return true;
        }
        if (isModified()) {
            showConfirmDiscardDialog();
            return true;
        }
        return false;
    }

    private void showConfirmDiscardDialog() {
        FragmentManager fm = getChildFragmentManager();
        ConfirmDiscardDialogFragment dialogFragment = ConfirmDiscardDialogFragment.newInstance();
        dialogFragment.show(fm, CONFIRM_DISCARD_FRAGMENT);
    }

    public void loadUser() {
        if (getActivity() != null) {
            if (mOnShowProgressListener == null) {
                showProgressDialogFragment();
            } else {
                mOnShowProgressListener.showProgress(true, getString(R.string.progress_please_wait));
            }

            mGetCurrentUserTask = new GetCurrentUserTask();
            mGetCurrentUserTask.execute();
        }
    }

    private void populateProfile() {
        mDisplayNameView.setText(mNewProfile.getDisplayName());
        mBirthdayView.setText(formatDate(mBirthdayCalendar));
        if (Profile.GENDER_MALE.equals(mNewProfile.getGender())) {
            mMaleGenderRadioButton.setChecked(true);
        } else {
            mFemaleGenderRadioButton.setChecked(true);
        }
        mAboutView.setText(mNewProfile.getAbout());
    }

    private void populateUser() {
        mFirstNameView.setText(mNewUser.getFirstName());
        mLastNameView.setText(mNewUser.getLastName());
        mEmailView.setText(mNewUser.getEmail());
    }
}