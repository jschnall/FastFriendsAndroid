package com.fastfriends.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.fastfriends.android.Settings;
import com.fastfriends.android.fragment.GooglePlusSignInFragment;
import com.fastfriends.android.fragment.PlusClientFragment;
import com.fastfriends.android.listener.AuthenticationInteractionListener;
import com.fastfriends.android.fragment.AuthenticationFragment;
import com.fastfriends.android.fragment.CreateAccountFragment;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.ForgotPasswordFragment;
import com.fastfriends.android.fragment.SignInFragment;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.User;
import com.fastfriends.android.model.UserStatus;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit.RetrofitError;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class AuthenticationActivity extends FragmentActivity implements
        AuthenticationInteractionListener, PlusClientFragment.OnSignInListener {
    private final static String LOGTAG = AuthenticationActivity.class.getSimpleName();

    // Social service names
    public static final String FACEBOOK = "FACEBOOK";
    public static final String GOOGLE_PLUS = "GOOGLE_PLUS";

    // Fragment tags
    private final static String AUTHENTICATION = "authentication";
    private final static String CREATE_ACCOUNT = "create_account";
    private final static String SIGN_IN = "sign_in";
    private final static String FORGOT_PASSWORD = "forgot_password";

    private View mStatusLayout;
    private TextView mStatusTextView;
    private ViewGroup mFragmentContainer;

    private class SignInWithGooglePlusTask extends AsyncTask<Object, Void, String> {
        String email;
        Person person;
        String accessToken;

        @Override
        protected void onPreExecute() {
            showProgress(true, getString(R.string.progress_please_wait));
        }

        @Override
        protected String doInBackground(Object... params) {
            email = (String) params[0];
            person = (Person) params[1];

            try {
                accessToken = GoogleAuthUtil.getToken(AuthenticationActivity.this, email, "oauth2:" + Scopes.PLUS_LOGIN);
                String social_id = person.getId();
                FastFriendsWebService webService = WebServiceManager.getWebService();
                AuthToken authToken = webService.socialSignIn(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET,
                        WebServiceManager.GRANT_TYPE_PASSWORD, GOOGLE_PLUS, social_id, accessToken);
                authToken.save();

                UserStatus userStatus = webService.getUserStatus(authToken.getAuthHeader());
                Settings.saveUserStatus(userStatus);
            } catch (IOException e) {
                // network or server error, the call is expected to succeed if you try again later.
                // Don't attempt to call again immediately - the request is likely to
                // fail, you'll hit quotas or back-off.
                Log.e(LOGTAG, "Can't get Google+ auth token.", e);
                return e.getMessage();
            } catch (UserRecoverableAuthException e) {
                // Recover
                Log.e(LOGTAG, "Can't get Google+ auth token.", e);
                return e.getMessage();
            } catch (GoogleAuthException e) {
                // Failure. The call is not expected to ever succeed so it should not be
                // retried.
                Log.e(LOGTAG, "Can't get Google+ auth token.", e);
                return e.getMessage();
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't sign in.", e);
                retrofit.client.Response response = e.getResponse();
                if (response == null) {
                    return e.getMessage();
                } else {
                    return response.getReason();
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't sign in.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            showProgress(false, null);

            if (message == null) {
                Settings.getSharedPreferences().edit().putString(Settings.EMAIL, email).commit();

                Intent launchIntent = new Intent(AuthenticationActivity.this, MainActivity.class);
                startActivity(launchIntent);
                finish();
            } else {
                Log.d(LOGTAG, message);

                // No account associated with social service data, prepopulate account creation
                String socialId = person.getId();
                String birthday = person.getBirthday(); // YYYY-MM-DD

                String gender = null;
                switch (person.getGender()) {
                    case Person.Gender.MALE: {
                        gender = Profile.GENDER_MALE;
                        break;
                    }
                    case Person.Gender.FEMALE: {
                        gender = Profile.GENDER_FEMALE;
                        break;
                    }
                    //Person.Gender.OTHER:
                }

                Person.Name name = person.getName();
                String firstName = name.getGivenName();
                String lastName = name.getFamilyName();

                showFragment(CreateAccountFragment.newInstance(email, GOOGLE_PLUS, socialId, accessToken, birthday, gender, firstName, lastName), CREATE_ACCOUNT);
            }

            // Sign user out of social service now that we have their data
            PlusClientFragment plusClientFragment = PlusClientFragment.getPlusClientFragment(AuthenticationActivity.this, PlusClientFragment.SCOPES,
                    PlusClientFragment.VISIBLE_ACTIVITIES);
            plusClientFragment.signOut();
        }
    }
    private SignInWithGooglePlusTask mSignInWithGooglePlusTask;


    private class SignInWithFacebookTask extends AsyncTask<Object, Void, String> {
        String email;
        GraphUser user;
        String accessToken;

        @Override
        protected void onPreExecute() {
            showProgress(true, getString(R.string.progress_please_wait));
        }

        @Override
        protected String doInBackground(Object... params) {
            email = (String) params[0];
            user = (GraphUser) params[1];
            accessToken = (String) params[2];

            try {
                String social_id = String.valueOf(user.getId());
                FastFriendsWebService webService = WebServiceManager.getWebService();
                AuthToken authToken = webService.socialSignIn(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET,
                        WebServiceManager.GRANT_TYPE_PASSWORD, FACEBOOK, social_id, accessToken);
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
        protected void onPostExecute(String message) {
            showProgress(false, null);

            if (message == null) {
                Settings.getSharedPreferences().edit().putString(Settings.EMAIL, email).commit();

                Intent launchIntent = new Intent(AuthenticationActivity.this, MainActivity.class);
                startActivity(launchIntent);
                finish();
            } else {
                Log.d(LOGTAG, message);

                // No account associated with social service data, prepopulate account creation
                String email = (String) user.getProperty("email");
                String socialId = String.valueOf(user.getId());
                String birthday = user.getBirthday(); // MM/DD/YYYY
                String gender = (String) user.getProperty("gender");
                String firstName = user.getFirstName();
                String lastName = user.getLastName();

                showFragment(CreateAccountFragment.newInstance(email, FACEBOOK, socialId, accessToken, birthday, gender, firstName, lastName), CREATE_ACCOUNT);
            }
        }
    }
    private SignInWithFacebookTask mSignInWithFacebookTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_authentication);

        mStatusLayout = findViewById(R.id.status);
        mStatusTextView = (TextView) findViewById(R.id.status_message);
        mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new AuthenticationFragment(), AUTHENTICATION)
                    .commit();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    private void showFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showCreateAccount(String email) {
        showFragment(CreateAccountFragment.newInstance(email), CREATE_ACCOUNT);
    }

    @Override
    public void showSignIn(String email) {
        showFragment(SignInFragment.newInstance(email, null), SIGN_IN);
    }

    @Override
    public void showForgotPassword(String email) {
        showFragment(ForgotPasswordFragment.newInstance(email), FORGOT_PASSWORD);
    }

    @Override
    public void OnLoggedIn(Session session) {
        makeMeRequest(session);
        session.closeAndClearTokenInformation();
    }

    @Override
    public void OnLoggedOut() {
    }

    private void makeMeRequest(final Session session) {
        final String accessToken = session.getAccessToken();

        // Make an API call to get user data and define a
        // new callback to handle the response.
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                String email = (String) user.getProperty("email");
                                mSignInWithFacebookTask = new SignInWithFacebookTask();
                                mSignInWithFacebookTask.execute(email, user, accessToken);
                            }
                        }
                        if (response.getError() != null) {
                            Log.d(LOGTAG, "Can't get Facebook user data: " + response.getError());
                        }
                    }
                });
        request.executeAsync();
    }

    /**
     * Shows the progress UI and hides the fragment.
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, String message) {
        mStatusTextView.setText(message);

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mStatusLayout.setVisibility(View.VISIBLE);
            mStatusLayout.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mFragmentContainer.setVisibility(View.VISIBLE);
            mFragmentContainer.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mFragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBackPressed(){
        if (View.VISIBLE != mStatusLayout.getVisibility()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSignedIn(PlusClient plusClient) {
        if (plusClient.isConnected()) {
            final String email = plusClient.getAccountName();
            Person person = plusClient.getCurrentPerson();

            if (person != null) {
                mSignInWithGooglePlusTask = new SignInWithGooglePlusTask();
                mSignInWithGooglePlusTask.execute(email, person);
            }
        }
    }

    @Override
    public void onSignInFailed() {

    }
}
