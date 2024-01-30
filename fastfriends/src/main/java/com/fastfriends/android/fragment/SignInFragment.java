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
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.AuthenticationActivity;
import com.fastfriends.android.activity.MainActivity;
import com.fastfriends.android.helper.EmailHelper;
import com.fastfriends.android.model.AuthError;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.UserStatus;
import com.fastfriends.android.text.style.URLSpanNoUnderline;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;

public class SignInFragment extends Fragment {
    private static final String LOGTAG = SignInFragment.class.getSimpleName();

    // Fragment launch args
    public static final String ARG_EMAIL = "email";
    public static final String ARG_PASSWORD = "password";
    public static final String ARG_SOCIAL_SERVICE = "social_service";
    public static final String ARG_SOCIAL_ID = "social_id";
    public static final String ARG_ACCESS_TOKEN = "access_token";

    // Dialogs
    private static final String DLG_SOCIAL_SIGN_IN = "social_sign_in";
    private static final String DLG_SIGN_IN_FAILED = "sign_in_failed";
    private static final String MESSAGE = "message";
    private static final String SOCIAL_SERVICE = "social_service";

    private TextView mEmailView;
    private TextView mPasswordView;
    private Button mSignInButton;
    private TextView mForgotPasswordView;
    private TextView mConditions;

    // Validated values
    private String mEmail;
    private String mPassword;
    private String mSocialService;
    private String mSocialId;
    private String mAccessToken;

    private boolean mSubmitting = false;
    private TextView mViewToFocus;
    private TextView mFocusedView = null;
    private boolean mHasSubmitted = false;
    boolean mTextChanged = false;
    private boolean mFirstFocus = true;

    private static AuthenticationInteractionListener mListener;

    private class SignInTask extends AsyncTask<Void, Void, String> {
        Activity mActivity;

        public SignInTask(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                FastFriendsWebService webService = WebServiceManager.getWebService();
                AuthToken authToken;

                if (mSocialService == null) {
                    authToken = webService.signIn(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET,
                            WebServiceManager.GRANT_TYPE_PASSWORD, mEmail, mPassword);
                } else {
                    authToken = webService.socialSignIn(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET,
                            WebServiceManager.GRANT_TYPE_PASSWORD, mSocialService, mSocialId, mAccessToken);
                }
                authToken.save();

                UserStatus userStatus = webService.getUserStatus(authToken.getAuthHeader());
                Settings.saveUserStatus(userStatus);
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't sign in.", e);
                return e.getResponse().getReason();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't sign in.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                Activity activity = getActivity();
                Settings.getSharedPreferences().edit().putString(Settings.EMAIL, mEmail).commit();

                Intent launchIntent = new Intent(activity, MainActivity.class);
                startActivity(launchIntent);
                activity.finish();
            } else {
                mSubmitting = false;
                if (mListener != null) {
                    mListener.showProgress(false, null);
                }

                //TODO show social sign in dialog if UserAttributes has facebook or google+ id

                mSignInFailedDialogFragment = SignInFailedDialogFragment.getInstance(mEmail);
                getChildFragmentManager().beginTransaction()
                        .add(mSignInFailedDialogFragment, DLG_SIGN_IN_FAILED)
                        .commitAllowingStateLoss();
            }
        }
    }
    private SignInTask mSignInTask;

    public static class SocialSignInDialogFragment extends DialogFragment {
        public static SocialSignInDialogFragment getInstance(String message, String socialService) {
            SocialSignInDialogFragment dialogFragment = new SocialSignInDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, message);
            bundle.putString(SOCIAL_SERVICE, socialService);
            dialogFragment.setArguments(bundle);
            return dialogFragment;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            String message = bundle.getString(MESSAGE);
            String socialService = bundle.getString(SOCIAL_SERVICE);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage(message)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });

            if (AuthenticationActivity.FACEBOOK.equals(SOCIAL_SERVICE)) {
                builder.setPositiveButton(R.string.sign_in_with_facebook, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        // TODO sign in with facebook
                        //mFacebookBtn.performClick();
                    }
                });
            } else if (AuthenticationActivity.GOOGLE_PLUS.equals(SOCIAL_SERVICE)) {
                builder.setPositiveButton(R.string.sign_in_with_google_plus, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        // TODO sign in with google+
                    }
                });
            }
            return builder.create();
        }
    };
    DialogFragment mSocialSignInDialogFragment;

    public static class SignInFailedDialogFragment extends DialogFragment {
        public static SignInFailedDialogFragment getInstance(String email) {
            SignInFailedDialogFragment dialogFragment = new SignInFailedDialogFragment();
            Bundle args = new Bundle();
            args.putString(ARG_EMAIL, email);
            dialogFragment.setArguments(args);
            return dialogFragment;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            final String email = bundle.getString(ARG_EMAIL);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_sign_in_failed_title)
                    .setMessage(R.string.dialog_sign_in_failed_message)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setNeutralButton(R.string.forgot_password, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            if (mListener != null) {
                                mListener.showForgotPassword(email);
                            }
                        }
                    })
                    .setPositiveButton(R.string.create_account, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            if (mListener != null) {
                                mListener.showSignIn(email);
                            }
                        }
                    })
                    .create();
            return dialog;
        }
    };
    DialogFragment mSignInFailedDialogFragment;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.sign_in:
                    signIn(true);
                    break;
                case R.id.forgot_password:
                    if (mListener != null) {
                        mListener.showForgotPassword(null);
                    }
            }
        }
    };

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

    public static SignInFragment newInstance(String email, String password) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    public static SignInFragment newInstance(String email, String socialService, String socialId, String accessToken) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_SOCIAL_SERVICE, socialService);
        args.putString(ARG_SOCIAL_ID, socialId);
        args.putString(ARG_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEmail = bundle.getString(ARG_EMAIL);
            mPassword = bundle.getString(ARG_PASSWORD);
            mSocialService = bundle.getString(ARG_SOCIAL_SERVICE);
            mSocialId = bundle.getString(ARG_SOCIAL_ID);
            mAccessToken = bundle.getString(ARG_ACCESS_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.fragment_sign_in, container, false);

        mEmailView = (TextView) layout.findViewById(R.id.email);
        mEmailView.setOnFocusChangeListener(mOnFocusChangeListener);
        mEmailView.addTextChangedListener(mTextWatcher);
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setText(EmailHelper.getEmail(getActivity()));
        } else {
            mEmailView.setText(mEmail);
        }

        mPasswordView = (TextView) layout.findViewById(R.id.password);
        mPasswordView.setOnFocusChangeListener(mOnFocusChangeListener);
        mPasswordView.addTextChangedListener(mTextWatcher);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.sign_in || id == EditorInfo.IME_NULL) {
                    signIn(true);
                    return true;
                }
                return false;
            }
        });
        if (!TextUtils.isEmpty(mPassword)) {
            mPasswordView.setText(mPassword);
        }

        mSignInButton = (Button) layout.findViewById(R.id.sign_in);
        mSignInButton.setOnClickListener(mOnClickListener);

        mForgotPasswordView = (TextView) layout.findViewById(R.id.forgot_password);
        mForgotPasswordView.setOnClickListener(mOnClickListener);

        mConditions = (TextView) layout.findViewById(R.id.conditions);
        mConditions.setMovementMethod(LinkMovementMethod.getInstance());
        mConditions.setText(Html.fromHtml(getString(R.string.conditions_sign_in)));
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

    public void signIn(boolean performLocalValidation) {
        if (mSubmitting) {
            return;
        }

        if (performLocalValidation) {
            if (!validateSubmit()) {
                if (mViewToFocus != null) {
                    mViewToFocus.requestFocus();
                    Toast.makeText(getActivity(), mViewToFocus.getError(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (mListener != null) {
                mListener.showProgress(true, getString(R.string.progress_signing_in));
            }
        }

        mSignInTask = new SignInTask(getActivity());
        mSignInTask.execute();
    }

    private boolean validateSubmit() {
        mHasSubmitted = true;
        mViewToFocus = null;

        return validate(mEmailView, false) & validate(mPasswordView, false);
    }

    private boolean validate(TextView view, boolean hideErrorsOnly) {
        if (view == null || !mHasSubmitted) {
            return false;
        }

        switch (view.getId()) {
            case R.id.email: {
                mEmail = mEmailView.getText().toString();
                if (TextUtils.isEmpty(mEmail)) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.error_email_required));
                        setViewToFocus(mEmailView);
                    }
                    return false;
                }
                break;
            }
            case R.id.password: {
                mPassword = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(mPassword)) {
                    if (!hideErrorsOnly) {
                        mPasswordView.setError(getString(R.string.error_password_required));
                        setViewToFocus(mPasswordView);
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
