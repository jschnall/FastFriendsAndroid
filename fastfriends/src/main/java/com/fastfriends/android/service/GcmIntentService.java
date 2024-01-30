package com.fastfriends.android.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.Settings;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.User;
import com.fastfriends.android.model.UserStatus;
import com.fastfriends.android.receiver.GcmBroadcastReceiver;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import retrofit.RetrofitError;

/**
 * Created by jschnall on 4/30/14.
 */
public class GcmIntentService extends IntentService {
    private final static String LOGTAG = GcmIntentService.class.getSimpleName();

    // Action for incoming GCM
    public static final String ACTION_RECEIVE = "com.google.android.c2dm.intent.RECEIVE";

    // Actions for local broadcasts
    public static final String ACTION_GCM_MESSAGE = "com.fastfriends.action.gcm.message";
    public static final String ACTION_GCM_COMMENT = "com.fastfriends.action.gcm.comment";
    public static final String ACTION_GCM_EVENT_UPDATE = "com.fastfriends.action.gcm.event_update";
    public static final String ACTION_GCM_EVENT_CHECKIN = "com.fastfriends.action.gcm.event_checkin";
    public static final String ACTION_GCM_EVENT_CANCELED = "com.fastfriends.action.gcm.event_canceled";

    public static final String EXTRA_DATA = "data";

    public static final String MESSAGE = "message";
    public static final String COMMENT = "comment";
    public static final String EVENT = "event";
    public static final String TYPE = "type";

    public static final String EVENT_UPDATE = "UPDATE";
    public static final String EVENT_CHECKIN = "CHECKIN";
    public static final String EVENT_CANCEL = "CANCEL";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (action.equals(ACTION_RECEIVE)) {
                handleActionReceive(intent);
            } else if (action.equals(ACTION_GCM_MESSAGE)) {
                handleActionGcmMessage(intent);
            } else if (action.equals(ACTION_GCM_COMMENT)) {
                handleActionGcmComment(intent);
            } else if (action.equals(ACTION_GCM_EVENT_CANCELED)) {
                handleActionGcmEventCanceled(intent);
            } else if (action.equals(ACTION_GCM_EVENT_CHECKIN)) {
                handleActionGcmEventCheckin(intent);
            } else if (action.equals(ACTION_GCM_EVENT_UPDATE)) {
                handleActionGcmEventUpdate(intent);
            }
        }
    }

    private void handleActionReceive(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            String senderId = extras.getString("from");
            if (!senderId.equals(Settings.GCM_SENDER_ID)) {
                Log.i(LOGTAG, "Invalid Sender: " + senderId);
                return;
            }

            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(LOGTAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(LOGTAG, "Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Intent broadcastIntent = new Intent();
                String data;

                if (extras.containsKey(MESSAGE)) {
                    data = extras.getString(MESSAGE);
                    broadcastIntent.setAction(ACTION_GCM_MESSAGE);
                } else if (extras.containsKey(COMMENT)) {
                    data = extras.getString(COMMENT);
                    broadcastIntent.setAction(ACTION_GCM_COMMENT);
                } else if (extras.containsKey(EVENT)) {
                    data = extras.getString(EVENT);
                    String type = extras.getString(TYPE);

                    if (EVENT_CANCEL.equalsIgnoreCase(type)) {
                        broadcastIntent.setAction(ACTION_GCM_EVENT_CANCELED);
                    } else if (EVENT_CHECKIN.equalsIgnoreCase(type)) {
                        broadcastIntent.setAction(ACTION_GCM_EVENT_CHECKIN);
                    } else if (EVENT_UPDATE.equalsIgnoreCase(type)) {
                        broadcastIntent.setAction(ACTION_GCM_EVENT_UPDATE);
                    }
                } else {
                    Log.d(LOGTAG, "Invalid message: " + extras.toString());
                    return;
                }
                broadcastIntent.putExtra(EXTRA_DATA, data);
                sendOrderedBroadcast(broadcastIntent, null);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleActionGcmMessage(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String data = extras.getString(EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Message message = gson.fromJson(data, Message.class);

            if (!User.isCurrentUser(message.getReceiver())) {
                // In case previous user did not log out properly
                Log.d(LOGTAG, "Ignoring message for user " + message.getReceiver());
                return;
            }
            DBManager.save(message);

            // Update UserStatus
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
                if (authToken != null) {
                    Log.d(LOGTAG, "Updating user status.");
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    UserStatus userStatus = webService.getUserStatus(authToken.getAuthHeader());
                    Settings.saveUserStatus(userStatus);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't get user status", e);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't get user status", e);
            }

            NotificationHelper.notifyMessage(this);
            Log.i(LOGTAG, "Received message: " + extras.toString());
        }
    }

    private void handleActionGcmComment(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String data = extras.getString(EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Comment comment = gson.fromJson(data, Comment.class);
            DBManager.save(comment);

            NotificationHelper.notifyComment(this);
            Log.i(LOGTAG, "Received comment: " + extras.toString());
        }
    }

    private void handleActionGcmEventCheckin(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String data = extras.getString(EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Event event = gson.fromJson(data, Event.class);

            NotificationHelper.notifyEventStarting(this, event);
            Log.i(LOGTAG, "Received event starting: " + extras.toString());
        }
    }

    private void handleActionGcmEventCanceled(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String data = extras.getString(EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Event event = gson.fromJson(data, Event.class);

            NotificationHelper.notifyEventCanceled(this, event);
            Log.i(LOGTAG, "Received event canceled: " + extras.toString());
        }
    }

    private void handleActionGcmEventUpdate(Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            String data = extras.getString(EXTRA_DATA);
            Gson gson = WebServiceManager.getGson();
            Event event = gson.fromJson(data, Event.class);
            DBManager.save(event);

            NotificationHelper.notifyEventUpdated(this);
            Log.i(LOGTAG, "Received event updated: " + extras.toString());
        }
    }

}