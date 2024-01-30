package com.fastfriends.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.fragment.FriendListFragment;
import com.fastfriends.android.fragment.PlanListFragment;
import com.fastfriends.android.fragment.dialog.ProgressDialogFragment;
import com.fastfriends.android.helper.DeviceHelper;
import com.fastfriends.android.helper.FitHistoryHelper;
import com.fastfriends.android.model.Device;
import com.fastfriends.android.model.FitHistory;
import com.fastfriends.android.service.FastFriendsService;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.fragment.EventListFragment;
import com.fastfriends.android.fragment.MessageCategoriesFragment;
import com.fastfriends.android.fragment.NavigationDrawerFragment;
import com.fastfriends.android.fragment.ProfileFragment;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.UserStatus;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerInterface {
    private final static String LOGTAG = MainActivity.class.getSimpleName();

    private final static String NAVIGATION_DRAWER = "navigation_drawer";

    public final static String EXTRA_FRAGMENT = "fragment";
    public final static String EXTRA_SECTION = "section";
    public final static String EXTRA_CATEGORY = "category";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging mGoogleCloudMessaging;

    // Google fit
    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;

    private int mLaunchFragment = NavigationDrawerFragment.EVENTS;
    private int mLaunchSection;
    private int mCurrentFragment = -1;

    private static final String PROGRESS_FRAGMENT = "progress";
    private ProgressDialogFragment mProgressDialogFragment;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static class SyncBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO
        }
    }
    SyncBroadcastReceiver mSyncBroadcastReceiver;

    private class SignOutBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent launchIntent = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(launchIntent);
            finish();
            mProgressDialogFragment.dismiss();
        }
    }
    private SignOutBroadcastReceiver mSignOutBroadcastReceiver;

    private class RegisterGCMTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mGoogleCloudMessaging == null) {
                    mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(MainActivity.this);
                }
                String regId = mGoogleCloudMessaging.register(Settings.GCM_SENDER_ID);
                Log.d(LOGTAG, "Device registered with GCM, registration ID=" + regId);

                long currentUserId = Settings.getSharedPreferences().getLong(Settings.USER_ID, 0);

                Device device = new Device();
                device.setOwnerId(currentUserId);
                device.setName(DeviceHelper.getDeviceName());
                device.setDeviceId(DeviceHelper.getDeviceId(MainActivity.this));
                device.setTelephonyId(DeviceHelper.getTelephonyId(MainActivity.this));
                device.setGcmRegId(regId);

                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(MainActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    device = webService.addDevice(authToken.getAuthHeader(), device);
                }
                // Persist the regID so no need to register again.
                storeRegistrationId(MainActivity.this, regId, device.getId());
            } catch (IOException ex) {
                return "Error :" + ex.getMessage();
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't add device.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't add device.", e);
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private RegisterGCMTask mRegisterGCMTask;

    private class GetUserStatusTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(MainActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    UserStatus userStatus = webService.getUserStatus(authToken.getAuthHeader());
                    if (userStatus.isActive()) {
                        Settings.saveUserStatus(userStatus);
                    } else {
                        // TODO sign out
                    }
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't get user status", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get user status", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private GetUserStatusTask mGetUserStatusTask;

    private class AddHistoryTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            DataReadRequest readRequest = FitHistoryHelper.readFitnessHistory(FitHistory.WEEK);
            DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest)
                    .await(1, TimeUnit.MINUTES);
            List<FitHistory> historyList = FitHistoryHelper.buildHistoryList(dataReadResult, FitHistory.WEEK);

            readRequest = FitHistoryHelper.readFitnessHistory(FitHistory.MONTH);
            dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest)
                    .await(1, TimeUnit.MINUTES);
            historyList.addAll(FitHistoryHelper.buildHistoryList(dataReadResult, FitHistory.MONTH));

            readRequest = FitHistoryHelper.readFitnessHistory(FitHistory.YEAR);
            dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest)
                    .await(1, TimeUnit.MINUTES);
            historyList.addAll(FitHistoryHelper.buildHistoryList(dataReadResult, FitHistory.YEAR));

            //FitHistoryHelper.printData(dataReadResult);

            // Upload fit history
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(MainActivity.this);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    webService.addFitHistory(authToken.getAuthHeader(), historyList);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't add fitHistory", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't add fitHistory", e);
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                SharedPreferences prefs = Settings.getSharedPreferences();
                prefs.edit().putLong(Settings.LAST_FIT_SYNC, System.currentTimeMillis()).commit();
            } else {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT);
            }
        }
    }
    private AddHistoryTask mAddFitHistoryTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Print key hash
        /*
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.fastfriends.android",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOGTAG, "Can't get KeyHash", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOGTAG, "Can't get KeyHash", e);
        }
        */

        SharedPreferences prefs = Settings.getSharedPreferences();
        boolean launched = prefs.getBoolean(Settings.LAUNCHED, false);
        if (!launched) {
            // First launch, create launcher shortcut
            createShortcut();
            prefs.edit().putBoolean(Settings.LAUNCHED, true).commit();
        }

        if (!Settings.isSignedIn(this)) {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        handleIntent();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check device for Google Play Services APK.
        if (checkPlayServices()) {
            mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(this);
            String regId = getRegistrationId(this);

            if (regId.isEmpty()) {
                mRegisterGCMTask = new RegisterGCMTask();
                mRegisterGCMTask.execute();
            }
        } else {
            Log.i(LOGTAG, "No valid Google Play Services APK found.");
        }

        Fragment fragment = NavigationDrawerFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.navigation_container, fragment, NAVIGATION_DRAWER)
                .commit();
        fragmentManager.executePendingTransactions();

        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                fragmentManager.findFragmentByTag(NAVIGATION_DRAWER);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_container,
                drawerLayout);

        mNavigationDrawerFragment.selectItem(mLaunchFragment);

        buildFitnessClient();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mLaunchFragment = bundle.getInt(EXTRA_FRAGMENT, NavigationDrawerFragment.EVENTS);
            mLaunchSection = bundle.getInt(EXTRA_SECTION, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Settings.isSignedIn(this)) {
            checkPlayServices();
            refreshStatus();
            if (Settings.isSyncNeeded(this)) {
                mSyncBroadcastReceiver = new SyncBroadcastReceiver();
                LocalBroadcastManager.getInstance(this).registerReceiver(mSyncBroadcastReceiver,
                        new IntentFilter(FastFriendsService.RESULT_SYNC));
                FastFriendsService.startActionSync(this);
            } else {
                Log.d(LOGTAG, "Sync is up to date.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSyncBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mSyncBroadcastReceiver);
            mSyncBroadcastReceiver = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOGTAG, "Connecting...");
        // Connect to the Fitness API
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSignOutBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mSignOutBroadcastReceiver);
            mSignOutBroadcastReceiver = null;
        }

        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case NavigationDrawerFragment.MY_PROFILE: {
                long currentUserId = Settings.getSharedPreferences().getLong(Settings.USER_ID, 0);
                fragment = ProfileFragment.newInstance(currentUserId, mLaunchSection);
                SharedPreferences prefs = Settings.getSharedPreferences();
                String userName = prefs.getString(Settings.DISPLAY_NAME, null);
                setTitle(getString(R.string.title_profile, userName));
                mCurrentFragment = position;
                break;
            }
            case NavigationDrawerFragment.MESSAGES: {
                fragment = MessageCategoriesFragment.newInstance();
                setTitle(R.string.title_messages);
                mCurrentFragment = position;
                break;
            }
            case NavigationDrawerFragment.EVENTS: {
                fragment = EventListFragment.newInstance(null);
                this.setTitle(R.string.title_event_list);
                mCurrentFragment = position;
                break;
            }
            case NavigationDrawerFragment.CONTACTS: {
                fragment = FriendListFragment.newInstance();
                setTitle(R.string.title_friend_list);
                mCurrentFragment = position;
                break;
            }
            case NavigationDrawerFragment.PLANS: {
                fragment = PlanListFragment.newInstance(null);
                setTitle(R.string.title_plan_list);
                mCurrentFragment = position;
                break;
            }
            case NavigationDrawerFragment.SETTINGS: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case NavigationDrawerFragment.SIGN_OUT: {
                signOut();
                break;
            }

        }

        if (fragment != null) {
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    public void onSectionAttached(String title) {
        mTitle = title;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        spinner.setVisibility(View.GONE);

        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean shouldShowMenu() {
        return mNavigationDrawerFragment.shouldShowMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshStatus() {
        mGetUserStatusTask = new GetUserStatusTask();
        mGetUserStatusTask.execute();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
                Toast.makeText(this, "This device does not support google play services.", Toast.LENGTH_SHORT).show();
                Log.i(LOGTAG, "This device does not support google play services.");
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @param context application's context.
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = Settings.getSharedPreferences();
        String registrationId = prefs.getString(Settings.GCM_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(LOGTAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Settings.APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOGTAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId, long deviceId) {
        final SharedPreferences prefs = Settings.getSharedPreferences();
        int appVersion = getAppVersion(context);
        Log.i(LOGTAG, "Saving regId on app version " + appVersion);
        prefs.edit()
            .putString(Settings.GCM_REG_ID, regId)
            .putInt(Settings.APP_VERSION, appVersion)
            .putLong(Settings.DEVICE_ID, deviceId)
        .commit();
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override()
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void createShortcut() {
        Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent removeIntent = new Intent();
        removeIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        removeIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, this.getResources().getString(R.string.app_name));
        removeIntent.putExtra("duplicate", false);
        removeIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        sendBroadcast(removeIntent);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, this.getResources().getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                R.drawable.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(addIntent);
    }

    private void signOut() {
        showProgressDialogFragment();
        mSignOutBroadcastReceiver = new SignOutBroadcastReceiver();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mSignOutBroadcastReceiver,
                new IntentFilter(FastFriendsService.RESULT_SIGN_OUT));
        FastFriendsService.startActionSignOut(MainActivity.this);
    }

    private void showProgressDialogFragment() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string.progress_signing_out));
        getSupportFragmentManager().beginTransaction()
                .add(mProgressDialogFragment, PROGRESS_FRAGMENT)
                .commitAllowingStateLoss();
    }

    /**
     *  Build a {@link com.google.android.gms.common.api.GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(LOGTAG, "Google API client Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Play with some sessions!!
                                if (Settings.isFitSyncNeeded(MainActivity.this)) {
                                    Log.d(LOGTAG, "Updating fit history...");
                                    mAddFitHistoryTask = new AddHistoryTask();
                                    mAddFitHistoryTask.execute();
                                } else {
                                    Log.d(LOGTAG, "Fit History up to date.");
                                }
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.d(LOGTAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.d(LOGTAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.d(LOGTAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.d(LOGTAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(LOGTAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }
}
