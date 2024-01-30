package com.fastfriends.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.fragment.SettingsFragment;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Device;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import org.json.JSONObject;

import java.util.List;

import retrofit.RetrofitError;


public class SettingsActivity extends ActionBarActivity {
    private static final String LOGTAG = SettingsActivity.class.getSimpleName();

    private ProgressFragment mProgressFragment;
    private Device mDevice;

    private View mStatusLayout;
    private TextView mStatusTextView;
    private ViewGroup mFragmentContainer;

    private class GetSettingsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            showProgress(true, null);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
                if (authToken != null) {
                    SharedPreferences prefs = Settings.getSharedPreferences();
                    long deviceId = prefs.getLong(Settings.DEVICE_ID, 0);
                    if (deviceId > 0) {
                        FastFriendsWebService webService = WebServiceManager.getWebService();
                        mDevice = webService.getDevice(deviceId, authToken.getAuthHeader());
                        mDevice.saveSettings();
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get device.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get device.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, SettingsFragment.newInstance(mDevice)).commit();
            } else {
                Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
            showProgress(false, null);
        }
    }
    GetSettingsTask mGetSettingsTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initActionBar();

        mStatusLayout = findViewById(R.id.status);
        mStatusTextView = (TextView) findViewById(R.id.status_message);
        mFragmentContainer = (ViewGroup) findViewById(R.id.container);

        GetSettingsTask getSettingsTask = new GetSettingsTask();
        getSettingsTask.execute();
    }

    public void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Shows the progress UI and hides the fragment.
     */
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
}
