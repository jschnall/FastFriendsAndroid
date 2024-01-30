package com.fastfriends.android.fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.RetrofitError;

import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;

import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.R;
import com.fastfriends.android.activity.AuthenticationActivity;
import com.fastfriends.android.helper.EmailHelper;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;
import com.fastfriends.android.text.style.URLSpanNoUnderline;
import com.fastfriends.android.view.FFEditText;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import static com.fastfriends.android.web.WebServiceManager.JSONError;

public class CreateAccountFragment extends Fragment {
    private static final String LOGTAG = CreateAccountFragment.class.getSimpleName();

    // Fragment launch args
    public static final String ARG_SOCIAL_SERVICE = "social_service";
    public static final String ARG_SOCIAL_ID = "social_id";
    public static final String ARG_ACCESS_TOKEN = "access_token";
    public static final String ARG_FIRST_NAME = "first_name";
    public static final String ARG_LAST_NAME = "last_name";
    public static final String ARG_EMAIL = "email";
    public static final String ARG_BIRTHDAY = "birthday";
    public static final String ARG_GENDER = "gender";

    // Validation constants
    public static final int EMAIL_LEN_MIN = 6;
    public static final int EMAIL_LEN_MAX = 254;
    public static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;


    public static final int DISPLAY_NAME_MIN = 1;
    public static final int DISPLAY_NAME_MAX = 64;
    public static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]{0,62}[a-z0-9]?$", Pattern.CASE_INSENSITIVE);

    public static final int USER_NAME_LEN_MIN = 1;
    public static final int USER_NAME_LEN_MAX = 128;
    public static final Pattern USER_NAME_PATTERN = Pattern.compile("\\S{1,128}");

    public static final int PASSWORD_LEN_MIN = 6;
    public static final int PASSWORD_LEN_MAX = 255;

    private static final String EMAIL_EXISTS = "User with this Email address already exists.";
    private static final String DISPLAY_NAME_EXISTS = "This display name is already in use.";
    private final static String SIGN_IN_FRAGMENT = "sign_in_fragment";
    private static final String DLG_EMAIL_EXISTS = "email_exists";
    private static final String MESSAGE = "message";
    private static final String SOCIAL_SERVICE = "social_service";
    private static final String EMAIL = "email";

    // UI references.
    private FFEditText mDisplayNameView;
    private FFEditText mFirstNameView;
    private FFEditText mLastNameView;
    private FFEditText mEmailView;
    private FFEditText mPasswordView;
    private Button mBirthdayView;
    private RadioGroup mGenderRadioGroup;
    private RadioButton mMaleGenderRadioButton;
    private RadioButton mFemaleGenderRadioButton;
    private CheckBox mShowPasswordView;
    private Button mCreateAccountButton;
    private TextView mSignInTextView;
    private TextView mConditions;

    // Validated values
    private String mDisplayName;
    private String mEmail;
    private String mPassword;
    private String mFirstName;
    private String mLastName;
    private Date mBirthday;
    private String mGender;
    private String mSocialService;
    private String mSocialId;
    private String mAccessToken;

    private TextView mViewToFocus;
    private TextView mFocusedView = null;
    private boolean mHasSubmitted = false;
    boolean mTextChanged = false;
    private boolean mFirstFocus = true;

    private HashSet<String> mTakenEmails;
    private HashSet<String> mTakenNames;

    public static class EmailExistsDialogFragment extends DialogFragment {
        public static EmailExistsDialogFragment getInstance(String message, String socialService, String email) {
            EmailExistsDialogFragment dialogFragment = new EmailExistsDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, message);
            bundle.putString(SOCIAL_SERVICE, socialService);
            bundle.putString(EMAIL, email);
            dialogFragment.setArguments(bundle);
            return dialogFragment;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            String message = bundle.getString(MESSAGE);
            String socialService = bundle.getString(SOCIAL_SERVICE);
            final String email = bundle.getString(EMAIL);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.dialog_email_exists_title))
                    .setMessage(message);

            if (!TextUtils.isEmpty(socialService)) {
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
            }

            if (AuthenticationActivity.FACEBOOK.equals(socialService)) {
                builder.setPositiveButton(R.string.sign_in_with_facebook, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        // TODO sign in with facebook
                    }
                });
            } else if (AuthenticationActivity.GOOGLE_PLUS.equals(socialService)) {
                builder.setPositiveButton(R.string.sign_in_with_google_plus, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        // TODO sign in with google+
                    }
                });
            } else {
                builder.setNegativeButton(R.string.forgot_password, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mListener != null) {
                            mListener.showForgotPassword(email);
                        }
                    }
                });
                builder.setPositiveButton(R.string.action_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mListener != null) {
                            mListener.showSignIn(email);
                        }
                    }
                });
            }
            return builder.create();
        }
    };
    private DialogFragment mEmailExistsDialogFragment;

    private class CreateAccountTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.GOOGLE_PLUS_DATE_FORMAT);
                String birthdayStr = sdf.format(mBirthday);

                User user = null;
                FastFriendsWebService webService = WebServiceManager.getWebService();
                if (mSocialService != null) {
                    user = webService.createSocialUser(mFirstName, mLastName, mEmail, mSocialService,
                            mSocialId, mAccessToken, birthdayStr, mGender, mDisplayName,
                            WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET);
                } else {
                    user = webService.createUser(mFirstName, mLastName, mEmail, mPassword, birthdayStr, mGender,
                            mDisplayName,
                            WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't create account.", e);
                Activity activity = getActivity();
                if (activity != null) {
                    String networkError = WebServiceManager.parseNetworkError(activity, e);
                    if (networkError != null) {
                        return networkError;
                    }
                    final JSONError jsonError = WebServiceManager.parseFirstJSONError(e);
                    if (jsonError != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String field = jsonError.getField();
                                String message = jsonError.getMessage();
                                if (User.EMAIL.equals(field)) {
                                    if (EMAIL_EXISTS.equalsIgnoreCase(message)) {
                                        mEmailView.setError(getString(R.string.error_email_taken));
                                        mTakenEmails.add(mEmail);

                                        mEmailExistsDialogFragment = EmailExistsDialogFragment.getInstance(message, null, mEmail);
                                        getChildFragmentManager().beginTransaction()
                                                .add(mEmailExistsDialogFragment, DLG_EMAIL_EXISTS)
                                                .commitAllowingStateLoss();
                                    } else {
                                        mEmailView.setError(message);
                                    }
                                } else if (User.PASSWORD.equals(field)) {
                                    mPasswordView.setError(message);
                                } else if (User.FIRST_NAME.equals(field)) {
                                    mFirstNameView.setError(message);
                                } else if (User.LAST_NAME.equals(field)) {
                                    mLastNameView.setError(message);
                                } else if (Profile.BIRTHDAY.equals(field)) {
                                    mBirthdayView.setError(message);
                                } else if (Profile.GENDER.equals(field)) {
                                    mMaleGenderRadioButton.setError(message);
                                } else if (Profile.DISPLAY_NAME.equals(field)) {
                                    if (DISPLAY_NAME_EXISTS.equalsIgnoreCase(message)) {
                                        mDisplayNameView.setError(getString(R.string.error_display_name_taken));
                                        mTakenNames.add(mDisplayName);

                                        mDisplayNameView.setError(message);
                                    } else {
                                        mDisplayNameView.setError(message);
                                    }
                                }
                            }
                        });
                        return jsonError.getMessage();
                    }
                }
                return e.getResponse().getReason();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't create account.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                // TODO if accessToken invalid, drop them back to main AuthenticationFragment
                // Launch SignInFragment in background and use it to sign in
                Fragment fragment;
                if (mSocialService == null) {
                    fragment = SignInFragment.newInstance(mEmail, mPassword);
                } else {
                    fragment = SignInFragment.newInstance(mEmail, mSocialService, mSocialId, mAccessToken);
                }
                getChildFragmentManager().beginTransaction()
                        .add(fragment, SIGN_IN_FRAGMENT)
                        .addToBackStack(SIGN_IN_FRAGMENT)
                        .commit();
                getChildFragmentManager().executePendingTransactions();

                SignInFragment signInFragment = (SignInFragment) getChildFragmentManager().findFragmentByTag(SIGN_IN_FRAGMENT);
                signInFragment.signIn(false);
            } else {
                if (mListener != null) {
                    mListener.showProgress(false, null);
                }

                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private CreateAccountTask mCreateAccountTask;

    private static AuthenticationInteractionListener mListener;

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            TextView textView = (TextView) v;
            if (hasFocus) {
                if (mFirstFocus) {
                    mFirstFocus = false;
                }
                mTextChanged = false;
                mFocusedView = textView;
            } else {
                if (mTextChanged) {
                    validate(textView, false);
                }
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mTextChanged = true;
            validate(mFocusedView, true);
        }

    };

    class DateHandler implements DatePickerDialogFragment.DatePickerDialogHandler {

        @Override
        public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            Date date = calendar.getTime();

            DateFormat dateFormat = DateFormat.getDateInstance();
            String dateStr = dateFormat.format(date);

            mBirthdayView.setText(dateStr);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.create_account:
                    createAccount();
                    break;
                case R.id.sign_in:
                    if (mListener != null) {
                        mListener.showSignIn(null);
                    }
                    break;
                case R.id.birthday:
                    new DatePickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setStyleResId(R.style.BetterPickersDialogFragment)
                            .addDatePickerDialogHandler(new DateHandler())
                            .show();
                    break;
            }
        }
    };


    public static CreateAccountFragment newInstance(String email) {
        CreateAccountFragment fragment = new CreateAccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateAccountFragment newInstance(String email, String socialService, String socialId,
                                                    String accessToken, String birthday, String gender,
                                                    String firstName, String lastName) {
        CreateAccountFragment fragment = new CreateAccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_SOCIAL_SERVICE, socialService);
        args.putString(ARG_SOCIAL_ID, socialId);
        args.putString(ARG_ACCESS_TOKEN, accessToken);
        args.putString(ARG_BIRTHDAY, birthday);
        args.putString(ARG_GENDER, gender);
        args.putString(ARG_FIRST_NAME, firstName);
        args.putString(ARG_LAST_NAME, lastName);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTakenEmails = new HashSet<String>();
        mTakenNames = new HashSet<String>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            mEmail = bundle.getString(ARG_EMAIL);
            mSocialService = bundle.getString(ARG_SOCIAL_SERVICE);
            mSocialId = bundle.getString(ARG_SOCIAL_ID);
            mAccessToken = bundle.getString(ARG_ACCESS_TOKEN);

            String birthdayStr = bundle.getString(ARG_BIRTHDAY);
            Date birthday = null;
            if (birthdayStr != null) {
                SimpleDateFormat sdf = null;
                if (AuthenticationActivity.GOOGLE_PLUS.equals(mSocialService)) {
                    sdf = new SimpleDateFormat(DateHelper.GOOGLE_PLUS_DATE_FORMAT);
                } else if (AuthenticationActivity.FACEBOOK.equals(mSocialService)) {
                    sdf = new SimpleDateFormat(DateHelper.FACEBOOK_DATE_FORMAT);
                }
                if (sdf != null) {
                    try {
                        mBirthday = sdf.parse(birthdayStr);
                    } catch (Exception e) {
                        Log.e(LOGTAG, "Can't parse birthday: " + birthdayStr, e);
                    }
                }
            }

            mGender = bundle.getString(ARG_GENDER);
            mFirstName = bundle.getString(ARG_FIRST_NAME);
            mLastName = bundle.getString(ARG_LAST_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_create_account, container, false);

        mFirstNameView = (FFEditText) layout.findViewById(R.id.first_name);
        mFirstNameView.setOnFocusChangeListener(mOnFocusChangeListener);
        mFirstNameView.addTextChangedListener(mTextWatcher);
        mFirstNameView.setText(mFirstName);

        mLastNameView = (FFEditText) layout.findViewById(R.id.last_name);
        mLastNameView.setOnFocusChangeListener(mOnFocusChangeListener);
        mLastNameView.addTextChangedListener(mTextWatcher);
        mLastNameView.setText(mLastName);

        mDisplayNameView = (FFEditText) layout.findViewById(R.id.display_name);
        mDisplayNameView.setOnFocusChangeListener(mOnFocusChangeListener);
        mDisplayNameView.addTextChangedListener(mTextWatcher);

        mEmailView = (FFEditText) layout.findViewById(R.id.email);
        mEmailView.setOnFocusChangeListener(mOnFocusChangeListener);
        mEmailView.addTextChangedListener(mTextWatcher);
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setText(EmailHelper.getEmail(getActivity()));
        } else {
            mEmailView.setText(mEmail);
            if (mSocialService != null) {
                mEmailView.setEnabled(false);
            }
        }

        mPasswordView = (FFEditText) layout.findViewById(R.id.password);
        mPasswordView.setOnFocusChangeListener(mOnFocusChangeListener);
        mPasswordView.addTextChangedListener(mTextWatcher);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.create_account || id == EditorInfo.IME_NULL) {
                    createAccount();
                    return true;
                }
                return false;
            }
        });

        mShowPasswordView = (CheckBox) layout.findViewById(R.id.show_password);
        mShowPasswordView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    int selectionStart = mPasswordView.getSelectionStart();
                    mPasswordView.setTransformationMethod(null);
                    mPasswordView.setSelection(selectionStart);

                } else {
                    int selectionStart = mPasswordView.getSelectionStart();
                    mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
                    mPasswordView.setSelection(selectionStart);
                }
            }
        });

        if (mSocialService != null) {
            mPasswordView.setVisibility(View.GONE);
            mShowPasswordView.setVisibility(View.GONE);
        }

        mBirthdayView = (Button) layout.findViewById(R.id.birthday);
        mBirthdayView.setOnClickListener(mOnClickListener);
        if (mBirthday != null) {
            DateFormat dateFormat = DateFormat.getDateInstance();
            String dateStr = dateFormat.format(mBirthday);
            mBirthdayView.setText(dateStr);
        }

        mGenderRadioGroup = (RadioGroup) layout.findViewById(R.id.gender);
        mMaleGenderRadioButton = (RadioButton) layout.findViewById(R.id.gender_male);
        mFemaleGenderRadioButton = (RadioButton) layout.findViewById(R.id.gender_female);
        if (mGender != null) {
            if (Profile.GENDER_FEMALE.equals(mGender)) {
                mFemaleGenderRadioButton.setChecked(true);
            } else if (Profile.GENDER_MALE.equals(mGender)) {
                mMaleGenderRadioButton.setChecked(true);
            }
        }

        mCreateAccountButton = (Button) layout.findViewById(R.id.create_account);
        mCreateAccountButton.setOnClickListener(mOnClickListener);

        mSignInTextView = (TextView) layout.findViewById(R.id.sign_in);
        mSignInTextView.setText(Html.fromHtml(getString(R.string.sign_in_text)));
        mSignInTextView.setOnClickListener(mOnClickListener);

        mConditions = (TextView) layout.findViewById(R.id.conditions);
        mConditions.setMovementMethod(LinkMovementMethod.getInstance());
        mConditions.setText(Html.fromHtml(getString(R.string.conditions_create_account)));
        URLSpanNoUnderline.removeUnderlines(getActivity(), mConditions);

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AuthenticationInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void createAccount() {
        if (!validateSubmit()) {
            if (mViewToFocus != null) {
                mViewToFocus.requestFocus();
                Toast.makeText(getActivity(), mViewToFocus.getError(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (mListener != null) {
            mListener.showProgress(true, getString(R.string.progress_creating_account));
        }

        mCreateAccountTask = new CreateAccountTask();
        mCreateAccountTask.execute();
    }

    private boolean validateSubmit() {
        mHasSubmitted = true;
        mViewToFocus = null;

        return validate(mFirstNameView, false) & validate(mLastNameView, false) &
                validate(mEmailView, false) & validate(mPasswordView, false) &
                validate(mBirthdayView, false) & validate(mMaleGenderRadioButton, false) &
                validate(mDisplayNameView, false);
    }

    private boolean validate(TextView view, boolean hideErrorsOnly) {
        if (view == null || !mHasSubmitted) {
            return false;
        }

        switch (view.getId()) {
            case R.id.display_name: {
                mDisplayName = mDisplayNameView.getText().toString();
                if (TextUtils.isEmpty(mDisplayName)) {
                    if (!hideErrorsOnly) {
                        mDisplayNameView.setError(getString(R.string.error_display_name_required));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                } else if (mDisplayName.length() < DISPLAY_NAME_MIN ||
                        mDisplayName.length() > DISPLAY_NAME_MAX) {
                    if (!hideErrorsOnly) {
                        mDisplayNameView.setError(getString(R.string.error_display_name_length));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                } else if (!DISPLAY_NAME_PATTERN.matcher(mDisplayName).matches()) {
                    if (!hideErrorsOnly) {
                        mDisplayNameView.setError(getString(R.string.error_display_name_invalid));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                } else if (mTakenNames.contains(mDisplayName)) {
                    if (!hideErrorsOnly) {
                        mDisplayNameView.setError(getString(R.string.error_display_name_taken));
                        setViewToFocus(mDisplayNameView);
                    }
                    return false;
                }
                break;
            }

            case R.id.email: {
                mEmail = mEmailView.getText().toString();
                if (TextUtils.isEmpty(mEmail)) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_required));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                } else if (mEmail.length() < EMAIL_LEN_MIN) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_length));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                } else if (!EMAIL_PATTERN.matcher(mEmail).matches()) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_invalid));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                } else if (mTakenEmails.contains(mEmail)) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_taken));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                }
                break;
            }

            case R.id.password: {
                if (mSocialService != null) {
                    // Social sign in, no password required
                    break;
                }
                mPassword = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(mPassword)) {
                    if (!hideErrorsOnly) {
                        mPasswordView.setError(getString(R.string.error_password_required));
                        setViewToFocus(mPasswordView);
                    }
                    return false;
                } else if (mPassword.length() < PASSWORD_LEN_MIN) {
                    if (!hideErrorsOnly) {
                        mPasswordView.setError(getString(R.string.error_password_length));
                        setViewToFocus(mPasswordView);
                    }
                    return false;
                }
                break;
            }

            case R.id.first_name: {
                mFirstName = mFirstNameView.getText().toString();
                if (TextUtils.isEmpty(mFirstName)) {
                    if (!hideErrorsOnly) {
                        mFirstNameView.setError(getString(R.string.error_first_name_required));
                        setViewToFocus(mFirstNameView);
                    }
                    return false;
                } else if (!USER_NAME_PATTERN.matcher(mFirstName).matches()) {
                    if (!hideErrorsOnly) {
                        mFirstNameView.setError(getString(R.string.error_name_invalid));
                        setViewToFocus(mFirstNameView);
                    }
                    return false;
                }
                break;
            }

            case R.id.last_name: {
                mLastName = mLastNameView.getText().toString();
                if (TextUtils.isEmpty(mLastName)) {
                    if (!hideErrorsOnly) {
                        mLastNameView.setError(getString(R.string.error_last_name_required));
                        setViewToFocus(mLastNameView);
                    }
                    return false;
                } else if (!USER_NAME_PATTERN.matcher(mLastName).matches()) {
                    if (!hideErrorsOnly) {
                        mLastNameView.setError(getString(R.string.error_name_invalid));
                        setViewToFocus(mLastNameView);
                    }
                    return false;
                }
                break;
            }

            case R.id.birthday: {
                mBirthday = null;
                String birthdayStr = mBirthdayView.getText().toString();

                if (TextUtils.isEmpty(birthdayStr)) {
                    if (!hideErrorsOnly) {
                        mBirthdayView.setError(getString(R.string.error_birthday_required));
                        setViewToFocus(mBirthdayView);
                    }
                    return false;
                }

                DateFormat dateFormat = DateFormat.getDateInstance();
                try {
                    mBirthday = dateFormat.parse(birthdayStr);
                } catch (ParseException e) {
                    Log.e(LOGTAG, "Can't parse birthday.", e);
                }
                break;
            }

            case R.id.gender_male: {
                mGender = null;
                switch(mGenderRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.gender_female:
                        mGender = Profile.GENDER_FEMALE;
                        break;
                    case R.id.gender_male:
                        mGender = Profile.GENDER_MALE;
                        break;
                }
                if (TextUtils.isEmpty(mGender)) {
                    if (!hideErrorsOnly) {
                        mMaleGenderRadioButton.setError(getString(R.string.error_gender_required));
                        setViewToFocus(mMaleGenderRadioButton);
                    }
                    return false;
                }
                break;
            }
        }

        view.setError(null);
        return true;
    }

    private void setViewToFocus(TextView viewToFocus) {
        if (mViewToFocus == null) {
            mViewToFocus = viewToFocus;
        }
    }
}
