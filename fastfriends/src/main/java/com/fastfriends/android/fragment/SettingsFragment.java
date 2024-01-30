package com.fastfriends.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.listener.ConfirmListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Device;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.github.machinarius.preferencefragment.PreferenceFragment;

import retrofit.RetrofitError;


public class SettingsFragment extends PreferenceFragment
{
    private static final String LOGTAG = SettingsFragment.class.getSimpleName();

    private static final String ARG_DEVICE = "device";

    private boolean mModified = false;
    private Device mDevice;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (Settings.NOTIFICATIONS.equals(key) ||
                            Settings.MESSAGE_NOTIFICATIONS.equals(key) ||
                            Settings.COMMENT_NOTIFICATIONS.equals(key) ||
                            Settings.EVENT_NOTIFICATIONS.equals(key)) {
                        mModified = true;
                    }
                }
            };

    private class SaveSettingsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
                if (authToken != null) {
                    mDevice.loadSettings();
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    mDevice = webService.editDevice(mDevice.getId(), authToken.getAuthHeader(), mDevice);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't edit device.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't edit device.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private SaveSettingsTask mSaveSettingsTask;

    public static SettingsFragment newInstance(Device device) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        if (device != null) {
            args.putParcelable(ARG_DEVICE, device);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mDevice = bundle.getParcelable(ARG_DEVICE);
        }

        getPreferenceManager().setSharedPreferencesName(Settings.PREFS_NAME);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onPause() {
        super.onPause();

        Settings.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mListener);
        if (mModified) {
            mSaveSettingsTask = new SaveSettingsTask();
            mSaveSettingsTask.execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Settings.getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);
    }

}