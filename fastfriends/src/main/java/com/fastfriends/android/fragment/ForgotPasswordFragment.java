package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.Settings;
import com.fastfriends.android.fragment.dialog.ProgressDialogFragment;
import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.R;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit.RetrofitError;

public class ForgotPasswordFragment extends Fragment {
    private static final String LOGTAG = ForgotPasswordFragment.class.getSimpleName();

    private static final String DLG_PROGRESS = "progress";

    // Fragment launch args
    public static final String ARG_EMAIL = "email";

    private AuthenticationInteractionListener mListener;

    private String mEmail;

    private TextView mEmailView;
    private Button mForgotPasswordButton;

    private boolean mSubmitting = false;
    private TextView mViewToFocus;
    private TextView mFocusedView = null;
    private boolean mHasSubmitted = false;
    boolean mTextChanged = false;
    private boolean mFirstFocus = true;

    private ProgressDialogFragment mProgressDialogFragment;

    private class ResetPasswordTask extends AsyncTask<String, Void, String> {
        JsonObject json = null;

        @Override
        protected void onPreExecute() {
            // Close keyboard so toast with response is more visible
            Activity activity = getActivity();
            if (activity != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            FragmentActivity activity = getActivity();
            try {
                FastFriendsWebService webService = WebServiceManager.getWebService();
                json = webService.forgotPassword(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET, email);
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't reset password.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't reset password.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (mListener != null) {
                mListener.showProgress(false, null);
            } else {
                mProgressDialogFragment.dismiss();
            }

            Activity activity = getActivity();
            if (activity != null) {
                if (error == null) {
                    if (json != null) {
                        String status = json.get("status").getAsString();
                        Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();

                        if (mListener != null) {
                            mListener.showSignIn(mEmail);
                        }
                    }
                } else {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private ResetPasswordTask mResetPasswordTask;

    public static ForgotPasswordFragment newInstance(String email) {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    public ForgotPasswordFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_forgot_password, container, false);

        mEmailView = (TextView) layout.findViewById(R.id.email);
        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.reset_password || id == EditorInfo.IME_NULL) {
                    submit();
                    return true;
                }
                return false;
            }
        });
        if (!TextUtils.isEmpty(mEmail)) {
            mEmailView.setText(mEmail);
        }

        mForgotPasswordButton = (Button) layout.findViewById(R.id.reset_password);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AuthenticationInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.e(LOGTAG, "ForgotPasswordFragment: Listener not set.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void submit() {
        if (mSubmitting) {
            return;
        }

        if (!validateSubmit()) {
            if (mViewToFocus != null) {
                mViewToFocus.requestFocus();
                Toast.makeText(getActivity(), mViewToFocus.getError(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (mListener != null) {
            mListener.showProgress(true, getString(R.string.progress_please_wait));
        } else {
            showProgressDialogFragment();
        }

        mResetPasswordTask = new ResetPasswordTask();
        mResetPasswordTask.execute(mEmail);
    }

    private boolean validateSubmit() {
        mHasSubmitted = true;
        mViewToFocus = null;

        return validate(mEmailView, false);
    }

    private boolean validate(TextView view, boolean hideErrorsOnly) {
        if (view == null) {
            return false;
        }

        switch (view.getId()) {
            case R.id.email: {
                mEmail = mEmailView.getText().toString();
                if (mHasSubmitted && TextUtils.isEmpty(mEmail)) {
                    if (!hideErrorsOnly) {
                        mEmailView.setError(getString(R.string.progress_please_wait));
                        setViewToFocus(mEmailView);
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

    private void showProgressDialogFragment() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string.progress_signing_in));
        getChildFragmentManager().beginTransaction()
                .add(mProgressDialogFragment, DLG_PROGRESS)
                .commitAllowingStateLoss();
    }

}
