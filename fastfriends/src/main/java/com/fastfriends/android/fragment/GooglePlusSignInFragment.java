package com.fastfriends.android.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import static android.content.IntentSender.SendIntentException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.R;
import com.google.android.gms.common.ConnectionResult;
import static com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import static com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;

/**
 * Created by jschnall on 1/25/14.
 */
public class GooglePlusSignInFragment extends Fragment implements ConnectionCallbacks,
        OnConnectionFailedListener {
    private final static String LOGTAG = GooglePlusSignInFragment.class.getSimpleName();


    private AuthenticationInteractionListener mListener = null;

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlusClient = new PlusClient.Builder(getActivity(), this, this)
                //.setActions("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_google_plus_sign_in, container, false);

        mConnectionProgressDialog = new ProgressDialog(getActivity());
        mConnectionProgressDialog.setMessage(getString(R.string.progress_signing_in));
        mConnectionProgressDialog.setCancelable(true);
        mConnectionProgressDialog.setIndeterminate(true);

        SignInButton signInButton = (SignInButton) view.findViewById(R.id.google_plus_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.google_plus_sign_in_button && !mPlusClient.isConnected()) {
                    if (mConnectionResult == null) {
                        mConnectionProgressDialog.show();
                        mPlusClient.connect();
                    } else {
                        try {
                            mConnectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_ERR);
                        } catch (SendIntentException e) {
                            // Try connecting again.
                            mConnectionResult = null;
                            mPlusClient.connect();
                        }
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors.
        mConnectionProgressDialog.dismiss();

        //if (mListener != null) {
        //    mListener.OnSignIn(mPlusClient);
       //}
    }

    @Override
    public void onDisconnected() {
        Log.d(LOGTAG, "disconnected");

        //if (mListener != null) {
        //    mListener.OnSignOut(mPlusClient);
        //}
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing()) {
            // The user clicked the sign-in button already. Start to resolve
            // connection errors. Wait until onConnected() to dismiss the
            // connection dialog.
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_ERR);
                } catch (SendIntentException e) {
                    mPlusClient.connect();
                }
            }
        }

        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = connectionResult;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AuthenticationInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == Activity.RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        }
    }
}