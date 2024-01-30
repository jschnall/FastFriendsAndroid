package com.fastfriends.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.fastfriends.android.model.UserStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by jschnall on 1/19/14.
 */
public class Settings {
    private final static String LOGTAG = Settings.class.getSimpleName();

    private static final String FACEBOOK_SECRET = "325166d8f861ec56fd0659d8e751c636";

    public static final int SYNC_INTERVAL = 1000 * 60 * 60 * 24; // 1 Day(s)
    public static final String PREFS_NAME = "settings";
    public static final String GCM_SENDER_ID = "724418083805";  // Project Number in Google Developers Console

    public static final int FIT_SYNC_INTERVAL = 1000 * 60 * 60; // 1 Hr

    // SharedPref keys
    public static final String LAUNCHED = "launched"; // has app been launched before
    public static final String LAST_SYNC = "last_sync";
    public static final String LAST_FIT_SYNC = "last_sync";
    public static final String EMAIL = "email";
    public static final String METRIC = "metric"; // Whether to display units in metric or US customary

    // User Status
    public static final String USER_ID = "user_id";
    public static final String USER_PORTRAIT = "user_portrait";
    public static final String USER_PORTRAIT_ID = "user_portrait_id";
    public static final String DISPLAY_NAME = "display_name";
    public static final String BIRTHDAY = "birthday";
    public static final String GENDER = "gender";
    public static final String INTERESTS = "interests";
    public static final String MESSAGE_COUNT = "message_count";
    public static final String MAX_MESSAGES = "max_messages";
    public static final String UNREAD_MESSAGE_COUNT = "unread_message_count";
    public static final String DRAFT_MESSAGE_COUNT = "draft_message_count";

    // Google Cloud Messaging
    public static final String APP_VERSION = "app_version";
    public static final String GCM_REG_ID = "gcm_reg_id";
    public static final String DEVICE_ID = "device_id";

    // TODO cleanup upload notifications to stack in a single notification
    public static final String UPLOAD_NOTIFICATION_ID = "upload_notification_id";

    public static final String NOTIFICATION_RINGTONE = "notification_ringtone";
    public static final String NOTIFICATION_VIBRATE = "notification_vibrate";
    public static final String NOTIFICATIONS = "notifications";
    public static final String MESSAGE_NOTIFICATIONS = "message_notifications";
    public static final String COMMENT_NOTIFICATIONS = "comment_notifications";
    public static final String EVENT_NOTIFICATIONS = "event_notifications";

    // Default SharedPref values
    public static final boolean NOTIFICATION_VIBRATE_DEFAULT = false;

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String USER_LEARNED_DRAWER = "navigation_drawer_learned";

    public static SharedPreferences getSharedPreferences() {
        return FastFriendsApplication.getAppContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences prefs = Settings.getSharedPreferences();
        String email = prefs.getString(Settings.EMAIL, null);

        return !TextUtils.isEmpty(email);
    }

    public static boolean isSyncNeeded(Context context) {
        SharedPreferences prefs = Settings.getSharedPreferences();
        long lastSync = prefs.getLong(Settings.LAST_SYNC, 0);
        long now = System.currentTimeMillis();

        return now - lastSync > SYNC_INTERVAL;
    }

    public static boolean isFitSyncNeeded(Context context) {
        SharedPreferences prefs = Settings.getSharedPreferences();
        long lastSync = prefs.getLong(Settings.LAST_FIT_SYNC, 0);
        long now = System.currentTimeMillis();

        return now - lastSync > FIT_SYNC_INTERVAL;
    }

    public static boolean isMetric() {
        Locale locale = Locale.getDefault();
        SharedPreferences prefs = Settings.getSharedPreferences();
        return prefs.getBoolean(Settings.METRIC, Locale.US.getCountry().equals(locale.getCountry()) ? false : true);
    }

    public static void saveUserStatus(UserStatus userStatus) {
        Set<String> interestSet = null;
        List<String> interests = userStatus.getInterests();
        if (interests != null) {
            interestSet = new HashSet<String>(userStatus.getInterests());
        }

        SharedPreferences prefs = Settings.getSharedPreferences();
        prefs.edit()
                .putLong(Settings.USER_ID, userStatus.getUserId())
                .putString(Settings.USER_PORTRAIT, userStatus.getPortrait())
                .putLong(Settings.USER_PORTRAIT_ID, userStatus.getPortraitId())
                .putString(Settings.DISPLAY_NAME, userStatus.getDisplayName())
                .putString(Settings.BIRTHDAY, userStatus.getBirthday())
                .putString(Settings.GENDER, userStatus.getGender())
                .putStringSet(Settings.INTERESTS, interestSet)
                .putInt(Settings.MESSAGE_COUNT, userStatus.getMessageCount())
                .putInt(Settings.MAX_MESSAGES, userStatus.getMaxMessages())
                .putInt(Settings.UNREAD_MESSAGE_COUNT, userStatus.getUnreadMessageCount())
                .putInt(Settings.DRAFT_MESSAGE_COUNT, userStatus.getDraftMessageCount())
                .commit();
    }
}