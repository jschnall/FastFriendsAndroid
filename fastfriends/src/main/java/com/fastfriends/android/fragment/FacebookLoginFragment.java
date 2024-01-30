package com.fastfriends.android.fragment;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.R;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class FacebookLoginFragment extends Fragment {
	private static final String LOGTAG = FacebookLoginFragment.class.getSimpleName();

    private boolean mLoggedIn = false;
    private UiLifecycleHelper uiHelper;
    
    private AuthenticationInteractionListener mListener = null;
    
    private Session.StatusCallback mFacebookLoginCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened() && !mLoggedIn) {
	        Log.i(LOGTAG, "Logged in to Facebook");
	    	mLoggedIn = true;
	    	
	    	if (mListener != null) {
                mListener.OnLoggedIn(session);
	    	}
	    } else if (state.isClosed()) {
	        Log.i(LOGTAG, "Logged out of Facebook");
	    	mLoggedIn = false;
	    	
	    	if (mListener != null) {
                mListener.OnLoggedOut();
	    	}
	    }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), mFacebookLoginCallback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_facebook_login, container, false);
	    
	    LoginButton authButton = (LoginButton) view.findViewById(R.id.facebook_login_button);
	    authButton.setFragment(this);
	    authButton.setReadPermissions(Arrays.asList("email"));

	    return view;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
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

}
