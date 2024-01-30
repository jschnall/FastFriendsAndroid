package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.CreateAccountFragment;
import com.fastfriends.android.fragment.ForgotPasswordFragment;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import retrofit.RetrofitError;

public class SignInDialogFragment extends DialogFragment {
    private static final String LOGTAG = SignInDialogFragment.class.getSimpleName();

    private final static String FORGOT_PASSWORD_FRAGMENT = "forgot_password_fragment";

    // Fragment launch args
    public static final String ARG_EMAIL = "email";

    // Dialogs
    private static final String DLG_PROGRESS = "progress";
    private static final String DLG_SIGN_IN_FAILED = "sign_in_failed";

    private TextView mEmailView;
    private TextView mPasswordView;
    private TextView mForgotPasswordView;

    // Validated values
    private String mEmail;
    private String mPassword;

    private boolean mSubmitting = false;
    private ProgressDialogFragment mProgressDialogFragment;

    private FragmentActivity mActivity;

    private class SignInTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                FastFriendsWebService webService = WebServiceManager.getWebService();
                AuthToken authToken = webService.signIn(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET,
                        WebServiceManager.GRANT_TYPE_PASSWORD, mEmail, mPassword);
                authToken.save();
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't sign in.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't sign in.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                mSubmitting = false;
                mProgressDialogFragment.dismiss();
            } else {
                showSignInFailedDialogFragment();

                mSubmitting = false;
                mProgressDialogFragment.dismiss();
            }
        }
    }
    private SignInTask mSignInTask;

    public static class SignInFailedDialogFragment extends DialogFragment {
        FragmentActivity fragmentActivity;
        String email;

        public SignInFailedDialogFragment() {
            // Required empty public constructor
        }

        public static SignInFailedDialogFragment newInstance(String email) {
            SignInFailedDialogFragment fragment = new SignInFailedDialogFragment();
            Bundle args = new Bundle();
            args.putString(ARG_EMAIL, email);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null) {
                email = bundle.getString(ARG_EMAIL);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_sign_in_failed_title)
                    .setMessage(R.string.dialog_sign_in_failed_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            fragmentActivity.getSupportFragmentManager().beginTransaction()
                                    .add(SignInDialogFragment.newInstance(email), DLG_SIGN_IN_FAILED)
                                    .commitAllowingStateLoss();

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

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.sign_in:
                    signIn();
                    break;
                case R.id.forgot_password: {
                    forgotPassword();
                    break;
                }
            }
        }
    };

    public static SignInDialogFragment newInstance(String email) {
        SignInDialogFragment fragment = new SignInDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    public SignInDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEmail = bundle.getString(ARG_EMAIL);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_sign_in, null);

        mEmailView = (TextView) layout.findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mPasswordView = (TextView) layout.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.sign_in || id == EditorInfo.IME_NULL) {
                    signIn();
                    return true;
                }
                return false;
            }
        });

        mForgotPasswordView = (TextView) layout.findViewById(R.id.forgot_password);
        mForgotPasswordView.setOnClickListener(mOnClickListener);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setTitle(R.string.dialog_sign_in_title)
                .setPositiveButton(R.string.action_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        signIn();
                    }
                })
                .create();

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void signIn() {
        if (mSubmitting) {
            return;
        }

        if (!validatePassword()) {
            showSignInFailedDialogFragment();
            return;
        }

        showProgressDialogFragment();

        mSignInTask = new SignInTask();
        mSignInTask.execute();
    }

    private boolean validatePassword() {
        mPassword = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(mPassword) || mPassword.length() < CreateAccountFragment.PASSWORD_LEN_MIN) {
            return false;
        }
        return true;
    }

    private void showProgressDialogFragment() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string.progress_signing_in));
        mActivity.getSupportFragmentManager().beginTransaction()
                .add(mProgressDialogFragment, DLG_PROGRESS)
                .commitAllowingStateLoss();
    }

    private void showSignInFailedDialogFragment() {
        mActivity.getSupportFragmentManager().beginTransaction()
                .add(SignInFailedDialogFragment.newInstance(mEmail), DLG_SIGN_IN_FAILED)
                .commitAllowingStateLoss();
    }

    private void forgotPassword() {
        // Launch showPasswordFragment in background and use it to call forgotPassword api
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(ForgotPasswordFragment.newInstance(mEmail), FORGOT_PASSWORD_FRAGMENT)
                .addToBackStack(FORGOT_PASSWORD_FRAGMENT)
                .commit();
        fragmentManager.executePendingTransactions();

        ForgotPasswordFragment forgotPasswordFragment = (ForgotPasswordFragment) fragmentManager.findFragmentByTag(FORGOT_PASSWORD_FRAGMENT);
        forgotPasswordFragment.submit();
    }

}
