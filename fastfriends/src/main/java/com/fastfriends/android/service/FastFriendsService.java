package com.fastfriends.android.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

/*
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.OpenableColumns;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
*/

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.Settings;
import com.fastfriends.android.helper.HashHelper;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.helper.NotificationHelper;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.DBOpenHelper;
import com.fastfriends.android.model.Resource;
import com.fastfriends.android.model.Tag;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class FastFriendsService extends IntentService {
    private final static String LOGTAG = FastFriendsService.class.getSimpleName();

    // Actions
    public static final String ACTION_SYNC = "com.fastfriends.action.SYNC";
    public static final String RESULT_SYNC = "com.fastfriends.result.SYNC";

    public static final String ACTION_UPLOAD = "com.fastfriends.action.UPLOAD";
    public static final String RESULT_UPLOAD = "com.fastfriends.result.UPLOAD";

    public static final String ACTION_SIGN_OUT = "com.fastfriends.action.SIGN_OUT";
    public static final String RESULT_SIGN_OUT = "com.fastfriends.result.SIGN_OUT";

    public static final String ACTION_CHECK_IN = "com.fastfriends.action.CHECK_IN";
    public static final String RESULT_CHECK_IN = "com.fastfriends.result.CHECK_IN";

    // Action extras
    public static final String EXTRA_ALBUM_ID = "com.fastfriends.extra.ALBUM_ID";
    public static final String EXTRA_NOTIFICATION_ID = "com.fastfriends.extra.NOTIFICATION_ID";

    public static final String EXTRA_EVENT_ID = "com.fastfriends.extra.EVENT_ID";

    // Result extras
    public static final String EXTRA_REQUEST_TIME = "com.fastfriends.extra.REQUEST_TIME";
    public static final String EXTRA_ERROR_MESSAGE = "com.fastfriends.extra.ERROR_MESSAGE";
    public static final String EXTRA_RESOURCE = "com.fastfriends.extra.RESOURCE";


    LocationManager mLocationManager;

    public static void startActionSync(Context context) {
        Intent intent = new Intent(context, FastFriendsService.class);
        intent.setAction(ACTION_SYNC);
        context.startService(intent);
    }

    public static void startActionUpload(Context context, Uri data, long albumId) {
        int notificationId = NotificationHelper.nextUploadNotificationId();
        NotificationHelper.notifyUpload(context, notificationId, NotificationHelper.UPLOAD_QUEUED,
                fileNameForUri(context, data));

        Intent intent = new Intent(context, FastFriendsService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.setData(data);
        intent.putExtra(EXTRA_ALBUM_ID, albumId);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        context.startService(intent);
    }

    public static void startActionSignOut(Context context) {
        Intent intent = new Intent(context, FastFriendsService.class);
        intent.setAction(ACTION_SIGN_OUT);
        context.startService(intent);
    }

    public static void startActionCheckIn(Context context, long eventId, double latitude, double longitude) {
        Intent intent = new Intent(context, FastFriendsService.class);
        intent.setAction(ACTION_CHECK_IN);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        context.startService(intent);
    }

    public FastFriendsService() {
        super("FastFriendsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final Bundle extras = intent.getExtras();
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                handleActionSync();
            } else if (ACTION_UPLOAD.equals(action)) {
                handleActionUpload(intent.getData(), extras.getLong(EXTRA_ALBUM_ID),
                        extras.getInt(EXTRA_NOTIFICATION_ID));
            } else if (ACTION_SIGN_OUT.equals(action)) {
                handleActionSignOut();
            } else if (ACTION_CHECK_IN.equals(action)) {
                // NOTE: This will not work.  service destroyed before location is retrieved
                mLocationManager = LocationHelper.startLocationUpdates(this, new LocationListener() {
                    final static int MAX_UPDATES = 5;
                    float lastAccuracy = -1;
                    int updateCount = 0;
                    @Override
                    public void onLocationChanged(Location location) {
                        float accuracy = location.getAccuracy();
                        if (updateCount >= MAX_UPDATES || (lastAccuracy > 0 && lastAccuracy / accuracy < 10)) {
                            mLocationManager.removeUpdates(this);
                            handleActionCheckIn(extras.getLong(EXTRA_EVENT_ID),
                                    location.getLatitude(), location.getLongitude());

                        } else {
                            lastAccuracy = location.getAccuracy();
                            updateCount++;
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                    }
                });
            }
        }
    }

    private void handleActionCheckIn(long eventId, double latitude, double longitude) {
        String errorMessage = null;
        try {
            AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
            if (authToken != null) {
                Log.d(LOGTAG, "Checking in.");
                FastFriendsWebService webService = WebServiceManager.getWebService();

                JsonObject json = webService.checkInEvent(authToken.getAuthHeader(), eventId, latitude, longitude);
            }
        } catch (RetrofitError e) {
            Log.e(LOGTAG, "Check-in failed.", e);
            errorMessage = e.getBodyAs(JSONObject.class).toString();
        } catch (Exception e) {
            Log.e(LOGTAG, "Check-in failed.", e);
            errorMessage = e.getMessage();
        }

        Intent intent = new Intent();
        intent.setAction(RESULT_CHECK_IN);
        if (errorMessage != null) {
            intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleActionSync() {
        if (!Settings.isSyncNeeded(this)) {
            Log.d(LOGTAG, "Sync is up to date.");
            return;
        }
        String errorMessage = null;

        try {
            AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
            if (authToken != null) {
                Log.d(LOGTAG, "Syncing tags.");
                FastFriendsWebService webService = WebServiceManager.getWebService();

                List<Tag> tags = webService.listTags(authToken.getAuthHeader());
                DBManager.save(tags);

                SharedPreferences prefs = Settings.getSharedPreferences();
                prefs.edit().putLong(Settings.LAST_SYNC, System.currentTimeMillis()).commit();
            }
        } catch (RetrofitError e) {
            Log.e(LOGTAG, "Sync failed.", e);
            errorMessage = WebServiceManager.handleRetrofitError(e);
        } catch (Exception e) {
            Log.e(LOGTAG, "Sync failed.", e);
            errorMessage = e.getMessage();
        }

        Intent intent = new Intent();
        intent.setAction(RESULT_SYNC);
        if (errorMessage != null) {
            intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleActionUpload(Uri data, long albumId, int notificationId) {
        String fileName = fileNameForUri(this, data);
        String hash = HashHelper.sha1Hex(this, data);
        if (hash == null) {
            NotificationHelper.notifyUpload(this, notificationId, NotificationHelper.UPLOAD_FAILED, fileName);
            return;
        }

        ContentResolver contentResolver = getContentResolver();
        String contentType = contentResolver.getType(data);
        if (TextUtils.isEmpty(contentType)) {
            if (!TextUtils.isEmpty(fileName)) {
                String[] segments = fileName.split("\\.");
                if (segments.length > 0) {
                    String extension = segments[segments.length - 1].toLowerCase();
                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                    contentType = mimeTypeMap.getMimeTypeFromExtension(extension);
                }
            }
        }

        NotificationHelper.notifyUpload(this, notificationId, NotificationHelper.UPLOAD_STARTED, fileName);
        String errorMessage = null;
        Resource resource = null;

        if (writeTempFile(contentResolver, data, hash)) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
                if (authToken != null) {
                    Log.d(LOGTAG, "Uploading resource: " + hash);
                    FastFriendsWebService webService = WebServiceManager.getWebService();

                    File file = getFileStreamPath(hash);
                    TypedFile typedFile = new TypedFile("application/octet-stream", file);
                    TypedString album = new TypedString(String.valueOf(albumId));
                    resource = webService.addResource(authToken.getAuthHeader(), typedFile, album);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't upload resource: " + hash, e);
                errorMessage = e.getBodyAs(JSONObject.class).toString();
            } finally {
                deleteFile(hash);
            }
        }

        /*
        // upload data directly to s3
        if (uploadDataToS3(contentResolver, data, hash, contentType)) {
            try {
                // Upload resource to Fast Friends
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
                if (authToken != null) {
                    Log.d(LOGTAG, "Uploading resource: " + hash);
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    Resource resource = new Resource(hash, contentType, albumId);

                    if (contentType.startsWith("image")) {
                        // Get image dimensions
                        InputStream inputStream = null;
                        try {
                            inputStream = contentResolver.openInputStream(data);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(contentResolver.openInputStream(data), null, options);

                            resource.setWidth(options.outWidth);
                            resource.setHeight(options.outHeight);
                        } catch (FileNotFoundException e) {
                            Log.e(LOGTAG, "Can't open image: " + data, e);
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Exception e) {
                                    // Do nothing
                                }
                            }
                        }
                    }
                    resource = webService.addResource(authToken.getAuthHeader(), resource);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't upload resource: " + hash, e);
            }
        }
        */

        Intent intent = new Intent();
        intent.setAction(RESULT_UPLOAD);
        if (resource != null) {
            intent.putExtra(EXTRA_RESOURCE, resource);
        }
        if (errorMessage != null) {
            intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (errorMessage == null) {
            NotificationHelper.notifyUpload(this, notificationId, NotificationHelper.UPLOAD_COMPLETED, fileName);
        } else {
            NotificationHelper.notifyUpload(this, notificationId, NotificationHelper.UPLOAD_FAILED, fileName);
        }
    }

    private boolean writeTempFile(ContentResolver contentResolver, Uri data, String hash) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = contentResolver.openInputStream(data);
            outputStream = openFileOutput(hash, Context.MODE_PRIVATE);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't write temp file: " + hash);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    // Upload directly to Amazon s3
    // Remove keys before launch
    // http://aws.amazon.com/articles/Mobile/4611615499399490
    // http://mobile.awsblog.com/post/Tx31X75XISXHRH8/Managing-Credentials-in-Mobile-Applications
    //private static final String AWS_ACCESS_KEY_ID = "";
    //private static final String AWS_SECRET_ACCESS_KEY = "";
    /*
    private boolean uploadDataToS3(ContentResolver contentResolver, Uri data, String hash, String contentType) {
        String path = WebServiceManager.MEDIA_ROOT + hash;

        String fileSizeColumn[] = {OpenableColumns.SIZE};

        Cursor cursor = contentResolver.query(data,
                fileSizeColumn, null, null, null);

        cursor.moveToFirst();

        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        // If the size is unknown, the value stored is null.  But since an int can't be
        // null in java, the behavior is implementation-specific, which is just a fancy
        // term for "unpredictable".  So as a rule, check if it's null before assigning
        // to an int.  This will happen often:  The storage API allows for remote
        // files, whose size might not be locally known.
        String size = null;
        if (!cursor.isNull(sizeIndex)) {
            // Technically the column stores an int, but cursor.getString will do the
            // conversion automatically.
            size = cursor.getString(sizeIndex);
        }

        cursor.close();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        //if(size != null){
            // This content length is truncated for some reason, and amazon apparently doesn't update metadata info from the actual data
            // This was causing truncated files that Pil could not open, so we could not generate thumbs.
            //metadata.setContentLength(Long.parseLong(size));
        //}

        AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY));
        if (isValidS3File(s3Client, WebServiceManager.AWS_BUCKET, path)) {
            Log.d(LOGTAG, "File already exists with hash: " + hash);
            return true;
        }
        try {
            // Upload the data to S3.
            s3Client.createBucket(WebServiceManager.AWS_BUCKET);

            PutObjectRequest por = new PutObjectRequest(
                    WebServiceManager.AWS_BUCKET, path,
                    contentResolver.openInputStream(data),metadata);
            s3Client.putObject(por);

            return true;
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't upload data from: " + data, e);
        }

        return false;
    }

    public static boolean isValidS3File(AmazonS3 s3,
                                        String bucketName,
                                        String path) throws AmazonClientException, AmazonServiceException {
        boolean isValidFile = true;
        try {
            ObjectMetadata objectMetadata = s3.getObjectMetadata(bucketName, path);
        } catch (AmazonS3Exception s3e) {
            if (s3e.getStatusCode() == 404) {
                // i.e. 404: NoSuchKey - The specified key does not exist
                isValidFile = false;
            }
            else {
                throw s3e;    // rethrow all S3 exceptions other than 404
            }
        }

        return isValidFile;
    }
    */

    private static String fileNameForUri(Context context, Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        else if (scheme.equals("content")) {
            ContentResolver contentResolver = context.getContentResolver();
            String contentType = contentResolver.getType(uri).toLowerCase();

            if (contentType.startsWith("image")) {
                String[] projection = { MediaStore.Images.Media.TITLE };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }
            } else if (contentType.startsWith("video")) {
                String[] projection = { MediaStore.Video.Media.TITLE };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }
            }
        }
        return fileName;
    }

    private void handleActionSignOut() {
        unregisterGCM();

        // Delete database, may fail if it's in use
        DBOpenHelper.clearDatabase();

        // Clear shared preferences
        SharedPreferences prefs = Settings.getSharedPreferences();
        prefs.edit().clear().putBoolean(Settings.LAUNCHED, true).commit();

        FastFriendsApplication app = FastFriendsApplication.getInstance();
        app.clearAuthToken();
        app.clearApplicationData();

        Intent intent = new Intent();
        intent.setAction(RESULT_SIGN_OUT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean unregisterGCM() {
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            gcm.unregister();

            final SharedPreferences prefs = Settings.getSharedPreferences();
            long deviceId = prefs.getLong(Settings.DEVICE_ID, 0);
            prefs.edit()
                    .remove(Settings.GCM_REG_ID)
                    .remove(Settings.APP_VERSION)
                    .remove(Settings.DEVICE_ID)
                    .commit();

            if (deviceId > 0) {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(null);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    JsonObject json = webService.deleteDevice(deviceId, authToken.getAuthHeader());
                }
            }
            return true;
        } catch (RetrofitError e) {
            Log.e(LOGTAG, "Can't unregister GCM", e);
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't unregister GCM", e);
        }
        return false;
    }
}