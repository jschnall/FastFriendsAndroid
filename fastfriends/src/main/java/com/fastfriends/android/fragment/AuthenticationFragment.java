package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.R;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;

import java.util.List;

public class AuthenticationFragment extends Fragment {
    private final static String LOGTAG = AuthenticationFragment.class.getSimpleName();

    private final static String FACEBOOK_FRAGMENT = "facebook_fragment";
    private final static String GOOGLE_FRAGMENT = "google_fragment";

    public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 1;
    private ImageView mLogoView;
    private Button mCreateAccountButton;
    private TextView mSignInTextView;

    protected PlusClientFragment mGooglePlusClientFragment;
    SignInButton mSignInButton;

    private AuthenticationInteractionListener mListener;
    private PlusClientFragment.OnSignInListener mOnSignInListener;

    private boolean mLogoShown = false;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int id = view.getId();
                switch(id) {
                    case R.id.create_account_button:
                        mListener.showCreateAccount(null);
                        break;
                    case R.id.sign_in:
                        mListener.showSignIn(null);
                        break;
                }
            }
        }
    };

    public static AuthenticationFragment newInstance() {
        AuthenticationFragment fragment = new AuthenticationFragment();
        return fragment;
    }
    public AuthenticationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_authentication, container, false);

        mLogoView = (ImageView) layout.findViewById(R.id.logo);
        if (!mLogoShown) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mLogoView.startAnimation(anim);
            mLogoShown = true;
        }

        mCreateAccountButton = (Button) layout.findViewById(R.id.create_account_button);
        mCreateAccountButton.setOnClickListener(mOnClickListener);

        mSignInTextView = (TextView) layout.findViewById(R.id.sign_in);
        mSignInTextView.setOnClickListener(mOnClickListener);
        mSignInTextView.setText(Html.fromHtml(mSignInTextView.getText().toString()));

        // Google+ sign in
        mGooglePlusClientFragment = PlusClientFragment.getPlusClientFragment(getActivity(), PlusClientFragment.SCOPES,
                PlusClientFragment.VISIBLE_ACTIVITIES);
        mSignInButton = (SignInButton) layout.findViewById(R.id.google_plus_sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGooglePlusClientFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
            }
        });

        // Facebook login
        FragmentActivity activity = getActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.facebook_login_fragment_container, new FacebookLoginFragment(), FACEBOOK_FRAGMENT)
                .commit();

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AuthenticationInteractionListener) activity;
            mOnSignInListener = (PlusClientFragment.OnSignInListener) activity;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

        mGooglePlusClientFragment.handleOnActivityResult(requestCode, resultCode, data);
    }
}