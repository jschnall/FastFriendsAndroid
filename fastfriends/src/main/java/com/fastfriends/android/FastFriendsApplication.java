package com.fastfriends.android;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.fastfriends.android.fragment.dialog.NoConnectionDialogFragment;
import com.fastfriends.android.fragment.dialog.SignInDialogFragment;
import com.fastfriends.android.web.AuthImageDownloader;
import com.fastfriends.android.helper.UriFileNameGenerator;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.annotations.SerializedName;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.HashMap;

import retrofit.RetrofitError;

/**
 * Created by jschnall on 2/13/14.
 */
public class FastFriendsApplication extends Application implements NoConnectionDialogFragment.OnCloseListener {
    public static final String LOGTAG = FastFriendsApplication.class.getSimpleName();

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    private static final int THIRTY_DAYS = 60 * 60 * 24 * 30; // 30 days in seconds
    private static final String DLG_SIGN_IN = "sign_in";
    private static final String DLG_NO_CONNECTION = "no_connection";

    private static FastFriendsApplication mInstance = null;
    private AuthToken mAuthToken;
    private SignInDialogFragment mSignInDialog;
    private NoConnectionDialogFragment mNoConnectionDialog;

    static class AuthError {
        static final String INVALID_GRANT = "invalid_grant";

        @SerializedName("error")
        public String error;
    }

    public static FastFriendsApplication getInstance() {
        return mInstance;
    }

    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initImageLoader(getApplicationContext());
    }

    public static Context getAppContext() {
        return getInstance().getApplicationContext();
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new AuthImageDownloader(getAppContext(), 1000, 1000))
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCache(new LimitedAgeDiscCache(context.getCacheDir(), new UriFileNameGenerator(), THIRTY_DAYS))
                .build();
        ImageLoader.getInstance().init(config);
    }

    public AuthToken getAuthToken(FragmentActivity activity) {
        if (!isNetworkAvailable()) {
            //showNoConnectionDialog(activity);
            return null;
        }

        if (mAuthToken == null) {
            // Retrieve authToken from DB
            mAuthToken = AuthToken.get();
        }

        if (mAuthToken == null) {
            // TODO social sign in: reauth or just sign them out?
            showSignInDialog(activity);
            return null;
        }
        if (mAuthToken.isExpired()) {
            if (!refreshAuthToken()) {
                showSignInDialog(activity);
                return null;
            }
        }
        return mAuthToken;
    }

    private boolean refreshAuthToken() {
        String username = Settings.getSharedPreferences().getString(Settings.EMAIL, null);

        try {
            FastFriendsWebService webService = WebServiceManager.getWebService();
            mAuthToken = webService.refreshAuthToken(WebServiceManager.CLIENT_ID, WebServiceManager.CLIENT_SECRET,
                    WebServiceManager.GRANT_TYPE_REFRESH_TOKEN, mAuthToken.getRefreshToken(),
                    mAuthToken.getScope());

            mAuthToken.save();
            return true;
        } catch (RetrofitError e) {
            Log.e(LOGTAG, "Can't refresh authToken.", e);
            AuthError authError = (AuthError) e.getBodyAs(AuthError.class);
            if (AuthError.INVALID_GRANT.equals(authError.error)) {
                // Auth token is no longer valid, require user to sign in
                // or auth with their social service again
                //mAuthToken.delete();
                //mAuthToken = null;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't refresh authToken.", e);
        }

        return false;
    }

    private void showNoConnectionDialog(FragmentActivity activity) {
        if (activity == null) {
            Log.d(LOGTAG, "Can't show NoConnectionDialog, activity is null.");
            return;
        }
        if (mNoConnectionDialog == null) {
            mNoConnectionDialog = NoConnectionDialogFragment.newInstance();
            activity.getSupportFragmentManager().beginTransaction()
                    .add(mNoConnectionDialog, DLG_NO_CONNECTION)
                    .commit();
        } else {
            Log.d(LOGTAG, "Already showing NoConnectionDialog.");
        }
    }

    public void onClose() {
        mNoConnectionDialog = null;
    }

    private void showSignInDialog(FragmentActivity activity) {
        if (activity == null) {
            Log.d(LOGTAG, "Can't show SignInDialog, activity is null.");
            return;
        }
        if (mSignInDialog != null && mSignInDialog.isAdded()) {
            Log.d(LOGTAG, "Already showing SignInDialog.");
            return;
        }

        String email = Settings.getSharedPreferences().getString(Settings.EMAIL, null);

        mSignInDialog = SignInDialogFragment.newInstance(email);
        activity.getSupportFragmentManager().beginTransaction()
                .add(mSignInDialog, DLG_SIGN_IN)
                .commit();
    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "Deleted /data/data/APP_PACKAGE/" + s);
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public void clearAuthToken() {
        mAuthToken = null;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
