package com.fastfriends.android.web;

import android.content.Context;
import android.util.Log;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.helper.HashHelper;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Plan;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by jschnall on 2/7/14.
 */
public class WebServiceManager {
    private static final String LOGTAG = WebServiceManager.class.getSimpleName();

    // Schemes
    public static final String SCHEME_HTTPS = "https://";
    //public static final String SCHEME_HTTP = "http://";

    // Staging
    //public static final String HOST = "staging-fast-friends.herokuapp.com";
    //public static final String CLIENT_ID = "aaa";
    //public static final String CLIENT_SECRET = "bbb";
    //public static final String AWS_BUCKET = "staging.fastfriends";

    // Production
    public static final String HOST = "fast-friends.herokuapp.com";
    public static final String CLIENT_ID = "X5qbM8.;HL4seBN3sYF8tAhQkok1PMTgqVHSBh3m";
    public static final String CLIENT_SECRET = "XxuArHw_V.mIjv_9nyrSEQNXtB4-jzjHofM534GZ991Y;d3.3qaIb-_xf??!Oj:I;-Ygll7FO@pCsiDPxaC4G1OWDnoqiDt;3GHoVu=u-X;XVGI.EZ@rMCnVdwRlkkB1";
    public static final String AWS_BUCKET = "fastfriends";

    public static final String MEDIA_ROOT = "media/";

    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    public static final String MIME_TYPE_JSON = "application/json";

    // Used to create passwords for account creation from social logins
    private static final String HASH_SECRET = "ff_social_secret";

    private static FastFriendsWebService mWebService = null;

    public static class JSONError {
        public String mField;
        public String mMessage;

        public JSONError(String field, String message) {
            mField = field;
            mMessage = message;
        }

        public String getField() {
            return mField;
        }

        public void setField(String field) {
            mField = field;
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            mMessage = message;
        }
    };

    private WebServiceManager() {
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new ISO8601DateAdapter())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
    }

    public static FastFriendsWebService getWebService() {
        if (mWebService == null) {
            Gson gson = getGson();

            OkHttpClient okHttpClient = new OkHttpClient();
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setClient(new OkClient(okHttpClient))
                    .setEndpoint(SCHEME_HTTPS + HOST)
                    .setConverter(new GsonConverter(gson))
                    .setLogLevel(RestAdapter.LogLevel.FULL) // DEBUG
                    //.setLogLevel(RestAdapter.LogLevel.HEADERS) // RELEASE
                    .build();

            mWebService = restAdapter.create(FastFriendsWebService.class);
        }

        return mWebService;
    }

    public static String handleRetrofitError(RetrofitError retrofitError) {
        String message = parseNetworkError(FastFriendsApplication.getAppContext(), retrofitError);
        if (message == null) {
            try {
                JsonObject json = (JsonObject) retrofitError.getBodyAs(JsonObject.class);
                message = json.get("status").getAsString();
            } catch (Exception e) {
            }
        }
        if (message == null) {
            try {
                message = retrofitError.getBodyAs(String.class).toString();
            } catch (Exception e) {
            }
        }
        if (message == null) {
            return retrofitError.getResponse().getReason();
        }
        return message;
    }

    public static String parseNetworkError(Context context, RetrofitError retrofitError) {
        if (retrofitError.isNetworkError()) {
            if (retrofitError.getCause() instanceof SocketTimeoutException) {
                return context.getString(R.string.error_connection_timed_out);
            } else {
                return context.getString(R.string.error_no_connection);
            }
        }
        return null;
    }

    public static JSONError parseFirstJSONError(RetrofitError retrofitError) {
        try {
            if (retrofitError.getResponse() != null) {
                JSONObject json = (JSONObject) retrofitError.getBodyAs(JSONObject.class);
                if (json.length() > 0) {
                    String key = (String) json.keys().next();
                    String value = null;
                    Object object = json.get(key);
                    if (object instanceof  JSONArray) {
                        JSONArray jsonArray = (JSONArray) object;
                        if (jsonArray.length() > 0) {
                            value = jsonArray.getString(0);
                        }
                    } else {
                        value = object.toString();
                    }
                    return new JSONError(key, value);
                }
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't parse JSON.", e);
        }
        return null;
    }

    /**
     * Construct a comma separated string of ids
     * @param ids
     * @return
     */
    public static String buildIdListString(List<Long> ids) {
        StringBuilder builder = new StringBuilder();
        Iterator<Long> iter = ids.iterator();
        if (iter.hasNext()) {
            builder.append(String.valueOf(iter.next()));
        }
        while (iter.hasNext()) {
            builder.append(",");
            builder.append(String.valueOf(iter.next()));
        }

        return builder.toString();
    }

    /**
     * Deserialize a user history page.  Results may include both Events and Plans
     * @param json
     * @return Combined page of events and plans
     */
    public static Page<Object> jsonToUserHistoryPage(JsonObject json) {
        Page<Object> page = new Page<Object>();
        List<Object> results = new ArrayList<Object>();

        try {
            JsonArray array = json.getAsJsonArray(Page.RESULTS);
            Gson gson = getGson();
            for (JsonElement item : array) {
                String type = item.getAsJsonObject().get("item_type").getAsString();
                if ("Event".equalsIgnoreCase(type)) {
                    results.add(gson.fromJson(item.toString(), Event.class));
                } else if ("Plan".equalsIgnoreCase(type)) {
                    results.add(gson.fromJson(item.toString(), Plan.class));
                } else {
                    Log.d(LOGTAG, "Unknown item type: " + type);
                }
            }
            page.setCount(json.get(Page.COUNT).getAsInt());
            JsonElement next = json.get(Page.NEXT);
            if (!(next instanceof JsonNull)) {
                page.setNext(next.getAsString());
            }
            JsonElement previous = json.get(Page.PREVIOUS);
            if (!(previous instanceof JsonNull)) {
                page.setPrevious(previous.getAsString());
            }
            page.setResults(results);
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't deserialize user history.", e);
        }

        return page;
    }
}
