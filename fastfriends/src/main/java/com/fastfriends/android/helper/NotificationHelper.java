package com.fastfriends.android.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.ConversationActivity;
import com.fastfriends.android.activity.EventActivity;
import com.fastfriends.android.activity.MainActivity;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.adapter.EventListAdapter;
import com.fastfriends.android.fragment.NavigationDrawerFragment;
import com.fastfriends.android.fragment.ProfileFragment;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.service.FastFriendsService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by jschnall on 4/22/14.
 */
public class NotificationHelper {
    private final static String LOGTAG = NotificationHelper.class.getSimpleName();

    public static final int MAX_UPLOAD_NOTIFICATIONS = 16;
    public static final int MESSAGE_NOTIFICATION_ID = 17;
    public static final int COMMENT_NOTIFICATION_ID = 18;
    public static final int EVENT_NOTIFICATION_ID = 19;
    public static final int FRIEND_ADDED_NOTIFICATION_ID = 20;
    public static final int EVENT_CHECKIN_ID = 21;

    public static final int MAX_LINES = 5;

    // Upload state
    public static final int UPLOAD_QUEUED = 0;
    public static final int UPLOAD_STARTED = 1;
    public static final int UPLOAD_COMPLETED = 2;
    public static final int UPLOAD_FAILED = 3;

    private NotificationHelper() {
    }

    public static int nextUploadNotificationId() {
        SharedPreferences prefs = Settings.getSharedPreferences();
        int notificationId = prefs.getInt(Settings.UPLOAD_NOTIFICATION_ID, 0);
        prefs.edit().
                putInt(Settings.UPLOAD_NOTIFICATION_ID, ++notificationId % MAX_UPLOAD_NOTIFICATIONS)
        .commit();

        return notificationId;
    }

    public static void notifyUpload(Context context, int id, int uploadState, String fileName) {
        if (id < 0 || id >= MAX_UPLOAD_NOTIFICATIONS) {
            throw new IllegalArgumentException("Invalid upload notificationId.");
        }

        if (fileName == null) {
            fileName = context.getString(R.string.file);
        }
        String title = context.getString(R.string.notification_upload_title, fileName);
        String text = null;
        switch (uploadState) {
            case UPLOAD_QUEUED:
                text = context.getString(R.string.notification_upload_queued_message);
                break;
            case UPLOAD_STARTED:
                text = context.getString(R.string.notification_upload_started_message);
                break;
            case UPLOAD_COMPLETED:
                text = context.getString(R.string.notification_upload_finished_message);
                break;
            case UPLOAD_FAILED:
                text = context.getString(R.string.notification_upload_failed_message);
                break;
            default:
                throw new IllegalArgumentException("Invalid upload state.");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                        //.setLargeIcon(R.drawable.ic_notification_icon_large)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        // TODO pass in owner or event id and open the associated album
        //Intent intent = new Intent(context, AlbumActivity.class);
        //intent.putExtra()
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //builder.setContentIntent(pendingIntent);

        notify(context, id, builder.build());
    }

    public static void notifyFriendAdded(Context context, Profile profile) {
        Resources res = context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                        //.setLargeIcon(R.drawable.ic_notification_icon_large)
                //.setContentTitle(title)
                .setContentText(res.getString(R.string.notification_friend_added_message, profile.getDisplayName()))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_USER_ID, profile.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, EVENT_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        notify(context, FRIEND_ADDED_NOTIFICATION_ID, builder.build());
    }

    public static void notifyEventCanceled(final Context context, Event event) {
        Resources res = context.getResources();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setContentTitle(event.getName());

        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        builder.setContentText(res.getString(R.string.notification_event_canceled_message,
                timeFormat.format(event.getStartDate()), EventListAdapter.formatLocation(event.getLocation())));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra(EventActivity.EXTRA_EVENT_ID, event.getId());
        intent.putExtra(EventActivity.EXTRA_OWNER_ID, event.getOwnerId());
        intent.putExtra(EventActivity.EXTRA_TITLE, event.getName());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, EVENT_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        String imageUrl = event.getImage();
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageLoader.getInstance().loadImage(imageUrl, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    builder.setLargeIcon(loadedImage);
                    NotificationHelper.notify(context, EVENT_NOTIFICATION_ID, builder.build());
                }
            });
        } else {
            notify(context, EVENT_NOTIFICATION_ID, builder.build());
        }
    }

    public static void notifyEventStarting(final Context context, Event event) {
        Resources res = context.getResources();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setContentTitle(event.getName());

        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        builder.setContentText(res.getString(R.string.notification_event_starting_message,
                timeFormat.format(event.getStartDate()), EventListAdapter.formatLocation(event.getLocation())));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra(EventActivity.EXTRA_EVENT_ID, event.getId());
        intent.putExtra(EventActivity.EXTRA_OWNER_ID, event.getOwnerId());
        intent.putExtra(EventActivity.EXTRA_TITLE, event.getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, EVENT_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        // NOTE this will not work with IntentService.  It is destroyed before location retrieved
        //Intent checkInIntent = new Intent(context, FastFriendsService.class);
        //checkInIntent.setAction(FastFriendsService.ACTION_CHECK_IN);
        //checkInIntent.putExtra(FastFriendsService.EXTRA_EVENT_ID, event.getId());
        //PendingIntent piCheckIn = PendingIntent.getService(context, 0, checkInIntent, 0);
        //builder.addAction(R.drawable.ic_action_place, context.getString(R.string.action_check_in), piCheckIn);

        Intent checkInIntent = new Intent(context, EventActivity.class);
        checkInIntent.putExtra(EventActivity.EXTRA_EVENT_ID, event.getId());
        checkInIntent.putExtra(EventActivity.EXTRA_OWNER_ID, event.getOwnerId());
        checkInIntent.putExtra(EventActivity.EXTRA_TITLE, event.getName());
        checkInIntent.putExtra(EventActivity.EXTRA_CHECKIN, true);
        PendingIntent piCheckIn = PendingIntent.getActivity(context, EVENT_CHECKIN_ID, checkInIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_action_place, context.getString(R.string.action_check_in), piCheckIn);


        String imageUrl = event.getImage();
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageLoader.getInstance().loadImage(imageUrl, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    builder.setLargeIcon(loadedImage);
                    NotificationHelper.notify(context, EVENT_NOTIFICATION_ID, builder.build());
                }
            });
        } else {
            notify(context, EVENT_NOTIFICATION_ID, builder.build());
        }
    }

    public static void notifyEventUpdated(Context context) {
        List<Event> events = DBManager.getAll(Event.class);
        if (events == null || events.isEmpty()) {
            Log.d(LOGTAG, "No events to notify user about");
            return;
        }

        Resources res = context.getResources();
        Event newEvent = events.get(0);
        int eventCount = events.size();
        String title = context.getResources().getQuantityString(R.plurals.notification_event_update_title, eventCount, eventCount);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                        //.setLargeIcon(R.drawable.ic_notification_icon_large)
                .setContentTitle(title)
                .setContentText(newEvent.getName())
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        if (eventCount > 1) {
            // Multiple events updated, take user to "My events" section of event list
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
            inBoxStyle.setBigContentTitle(title);
            List<Event> firstEvents = events.subList(0, Math.min(eventCount, MAX_LINES));
            for (Event event : firstEvents) {
                inBoxStyle.addLine(event.getName());
            }
            int moreEventCount = eventCount - MAX_LINES;
            if (moreEventCount > 0) {
                inBoxStyle.setSummaryText(res.getString(R.string.more_messages, moreEventCount));
            }
            builder.setStyle(inBoxStyle);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FRAGMENT, NavigationDrawerFragment.EVENTS);
            intent.putExtra(MainActivity.EXTRA_CATEGORY, Event.CATEGORY_ATTENDING);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, EVENT_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
        } else {
            // single event, take user inside event
            Intent intent = new Intent(context, EventActivity.class);
            intent.putExtra(EventActivity.EXTRA_EVENT_ID, newEvent.getId());
            intent.putExtra(EventActivity.EXTRA_OWNER_ID, newEvent.getOwnerId());
            intent.putExtra(EventActivity.EXTRA_TITLE, newEvent.getName());
            intent.putExtra(EventActivity.EXTRA_SECTION, EventActivity.DETAILS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, EVENT_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        notify(context, EVENT_NOTIFICATION_ID, builder.build());
    }

    public static void notifyComment(Context context) {
        List<Comment> comments = DBManager.getAll(Comment.class);
        if (comments == null || comments.isEmpty()) {
            Log.d(LOGTAG, "No comments to notify user about");
            return;
        }

        Resources res = context.getResources();
        Comment newComment = comments.get(0);
        int commentCount = comments.size();
        String title = context.getResources().getQuantityString(R.plurals.notification_comment_title, commentCount, commentCount);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                //.setLargeIcon(R.drawable.ic_notification_icon_large)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        builder.setContentText(buildCommentLine(res, newComment));
        if (commentCount > 1) {
            // Multiple new messages, take user to conversation list
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
            inBoxStyle.setBigContentTitle(title);
            List<Comment> firstComments = comments.subList(0, Math.min(commentCount, MAX_LINES));
            for (Comment comment : firstComments) {
                String text = buildCommentLine(res, comment);
                inBoxStyle.addLine(Html.fromHtml(text));
            }
            int moreMessageCount = commentCount - MAX_LINES;
            if (moreMessageCount > 0) {
                inBoxStyle.setSummaryText(res.getString(R.string.more_messages, moreMessageCount));
            }
            builder.setStyle(inBoxStyle);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FRAGMENT, NavigationDrawerFragment.MY_PROFILE);
            intent.putExtra(MainActivity.EXTRA_SECTION, ProfileFragment.HISTORY);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, MESSAGE_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
        } else {
            // single comment, take user inside event
            Intent intent = new Intent(context, EventActivity.class);
            intent.putExtra(EventActivity.EXTRA_EVENT_ID, newComment.getEventId());
            intent.putExtra(EventActivity.EXTRA_OWNER_ID, newComment.getOwnerId());
            intent.putExtra(EventActivity.EXTRA_TITLE, newComment.getEventName());
            intent.putExtra(EventActivity.EXTRA_SECTION, EventActivity.COMMENTS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, COMMENT_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        notify(context, COMMENT_NOTIFICATION_ID, builder.build());
    }

    public static void notifyMessage(Context context) {
        List<Message> messages = DBManager.getAll(Message.class);
        if (messages == null || messages.isEmpty()) {
            Log.d(LOGTAG, "No messages to notify user about");
            return;
        }

        Resources res = context.getResources();
        Message newMessage = messages.get(0);
        int messageCount = messages.size();
        String title = context.getResources().getQuantityString(R.plurals.notification_message_title, messageCount, messageCount);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                //.setLargeIcon(R.drawable.ic_notification_icon_large)
                .setContentTitle(title)
                .setContentText(Html.fromHtml(res.getString(R.string.message_line, newMessage.getSenderName(), newMessage.getMessage())))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
        if (messageCount > 1) {
            // Multiple new messages, take user to conversation list
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
            inBoxStyle.setBigContentTitle(title);
            List<Message> firstMessages = messages.subList(0, Math.min(messageCount, MAX_LINES));
            for (Message message : firstMessages) {
                String text = res.getString(R.string.message_line, message.getSenderName(), message.getMessage());
                inBoxStyle.addLine(Html.fromHtml(text));
            }
            int moreMessageCount = messageCount - MAX_LINES;
            if (moreMessageCount > 0) {
                inBoxStyle.setSummaryText(res.getString(R.string.more_messages, moreMessageCount));
            }
            builder.setStyle(inBoxStyle);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FRAGMENT, NavigationDrawerFragment.MESSAGES);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, MESSAGE_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
        } else {
            // single message, take user inside conversation
            String portrait = Settings.getSharedPreferences().getString(Settings.USER_PORTRAIT, null);

            Intent intent = new Intent(context, ConversationActivity.class);
            intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_ID, newMessage.getSender());
            intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_NAME, newMessage.getSenderName());
            intent.putExtra(ConversationActivity.EXTRA_OTHER_USER_PORTRAIT, newMessage.getSenderPortrait());
            intent.putExtra(ConversationActivity.EXTRA_CURRENT_USER_PORTRAIT, portrait);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, MESSAGE_NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        notify(context, MESSAGE_NOTIFICATION_ID, builder.build());
    }

    public static void notify(Context context, int id, Notification notification) {
        SharedPreferences prefs = Settings.getSharedPreferences();
        String ringtone = prefs.getString(Settings.NOTIFICATION_RINGTONE, null);
        boolean vibrate = prefs.getBoolean(Settings.NOTIFICATION_VIBRATE, Settings.NOTIFICATION_VIBRATE_DEFAULT);

        if (vibrate) {
            notification.vibrate = new long[] { 100, 300 };
        } else {
            notification.vibrate = new long[] { 0 };
        }

        if (ringtone == null) {
            notification.sound = null;
        } else {
            notification.sound = Uri.parse(ringtone);
        }

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notification);
    }

    public static void cancel(Context context, int id) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }

    private static String buildCommentLine(Resources res, Comment comment) {
        if (comment.getEventId() > 0) {
            // Event
            return res.getString(R.string.event_comment_line, comment.getOwnerName(), comment.getEventName());
        } else {
            // Plan
            return res.getString(R.string.plan_comment_line, comment.getOwnerName(), comment.getPlanOwnerName());
        }
    }
}
