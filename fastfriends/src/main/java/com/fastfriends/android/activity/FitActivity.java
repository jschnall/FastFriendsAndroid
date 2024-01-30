package com.fastfriends.android.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.fragment.FitHistoryFragment;
import com.fastfriends.android.fragment.ProgressFragment;
import com.fastfriends.android.helper.FitHistoryHelper;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.FitHistory;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.fitness.result.SessionStopResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;

/**
 * Created by jschnall on 12/19/14.
 */
public class FitActivity extends ActionBarActivity {
    public static final String LOGTAG = FitActivity.class.getSimpleName();

    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    public static final String EXTRA_USER_ID = "user_id";
    private static final String FIT_FRAGMENT = "fit";
    private static final int REQUEST_OAUTH = 1;
    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;
    private Session mSession;
    private String mActivityType;
    private OnDataPointListener mListener;

    private long mUserId;

    private FitHistoryFragment mFitFragment;
    private ProgressFragment mProgressFragment;


    private class AddHistoryTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }


        /*
        @Override
        protected String doInBackground(Void... params) {
            SessionReadRequest readRequest = readFitnessSession();
            SessionReadResult sessionReadResult =
                    Fitness.SessionsApi.readSession(mClient, readRequest)
                            .await(1, TimeUnit.MINUTES);

            // Get a list of the sessions that match the criteria to check the result.
            Log.i(LOGTAG, "Session read was successful. Number of returned sessions is: "
                    + sessionReadResult.getSessions().size());
            for (Session session : sessionReadResult.getSessions()) {
                // Process the session
                dumpSession(session);

                // Process the data sets for this session
                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }

            return null;
        }
        */

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

            //printData(dataReadResult);

            // TODO do this before adding the fragment, or refresh after and only update once a day in a bg service
            // Upload fit history
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(FitActivity.this);
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
        }
    }
    private AddHistoryTask mAddHistoryTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit);
        initActionBar();

        handleIntent();

        if (savedInstanceState == null) {
            mFitFragment = FitHistoryFragment.newInstance(mUserId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFitFragment, FIT_FRAGMENT)
                    .commit();
        } else {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
            mFitFragment = (FitHistoryFragment) getSupportFragmentManager().findFragmentByTag(FIT_FRAGMENT);
        }

        //buildFitnessClient();
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mUserId = bundle.getLong(EXTRA_USER_ID);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
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
                                Log.i(LOGTAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Play with some sessions!!
                                //mAddHistoryTask = new AddHistoryTask();
                                //mAddHistoryTask.execute();

                                //findDataSources();
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
                                            FitActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.d(LOGTAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(FitActivity.this,
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    private SessionReadRequest readFitnessSession() {
        Log.i(LOGTAG, "Reading session");
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -4);
        long startTime = cal.getTimeInMillis();

        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                //.read(DataType.TYPE_SPEED)
                //.read(DataType.TYPE_DISTANCE_DELTA)
                //.read(DataType.TYPE_HEART_RATE_BPM)
                //.read(DataType.TYPE_ACTIVITY_SAMPLE)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .readSessionsFromAllApps()
                //.setSessionName(SAMPLE_SESSION_NAME)
                .build();

        return readRequest;
    }

    private void dumpSession(Session session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Log.i(LOGTAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }

    private void startRecording() {
        subscribe(DataType.TYPE_ACTIVITY_SAMPLE);
        //subscribe(DataType.TYPE_DISTANCE_DELTA);
        subscribe(DataType.TYPE_STEP_COUNT_DELTA);
        subscribe(DataType.TYPE_SPEED);

        mSession = startSession(mActivityType);
    }

    private void stopRecording() {
        stopSession(mSession);
        mSession = null;

        unSubscribe(DataType.TYPE_ACTIVITY_SAMPLE);
        //unSubscribe(DataType.TYPE_DISTANCE_DELTA);
        subscribe(DataType.TYPE_STEP_COUNT_DELTA);
        unSubscribe(DataType.TYPE_SPEED);
    }

    private void subscribe(final DataType dataType) {
        Fitness.RecordingApi.subscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(LOGTAG, "Already subscribed for data type: " + dataType.getName());
                            } else {
                                Log.i(LOGTAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(LOGTAG, "There was a problem subscribing.");
                        }
                    }
                });
    }

    private void unSubscribe(final DataType dataType) {
        //Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLE)
        Fitness.RecordingApi.unsubscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(LOGTAG, "Successfully unsubscribed for data type: " + dataType.getName());
                        } else {
                            // Subscription not removed
                            Log.i(LOGTAG, "Failed to unsubscribe for data type: " + dataType.getName());
                        }
                    }
                });
    }

    private void listSubscriptions(final DataType dataType) {
        Fitness.RecordingApi.listSubscriptions(mClient, dataType)
                // Create the callback to retrieve the list of subscriptions asynchronously.
                .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            DataType dt = sc.getDataType();
                            Log.i(LOGTAG, "Active subscription for data type: " + dt.getName());
                        }
                    }
                });
    }

    private Session startSession(String fitnessActivity) {
        // Example: FitnessActivities.RUNNING

        // (provide a name, identifier, description and start time)
        Session session = new Session.Builder()
                //.setName(sessionName)
                //.setIdentifier(identifier)
                //.setDescription(description)
                //.setStartTime(startTime.getMillis(), TimeUnit.MILLISECONDS)
                        // optional - if your app knows what activity:
                .setActivity(fitnessActivity)
                .build();

        Fitness.SessionsApi.startSession(mClient, session).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.i(LOGTAG, "Successfully started session.");
                } else {
                    // Subscription not removed
                    Log.i(LOGTAG, "Failed to start session.");
                }
            }
        });

        return session;
    }

    private void stopSession(Session session) {
        Fitness.SessionsApi.stopSession(mClient, session.getIdentifier()).setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(SessionStopResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.i(LOGTAG, "Successfully stopped session.");
                } else {
                    // Subscription not removed
                    Log.i(LOGTAG, "Failed to stop session.");
                }

            }
        });
    }

    private void findDataSources() {
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(
                        //DataType.TYPE_LOCATION_SAMPLE,
                        //DataType.TYPE_ACTIVITY_SAMPLE,
                        // Distance and speed don't seem to work, because not raw data???
                        //DataType.TYPE_DISTANCE_DELTA,
                        //DataType.TYPE_SPEED,
                        //DataType.TYPE_HEART_RATE_BPM,
                        DataType.TYPE_STEP_COUNT_DELTA
                )
                        // Can specify whether data type is raw or derived.
                //.setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        Log.i(LOGTAG, "Result: " + dataSourcesResult.getStatus().toString());
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Device device = dataSource.getDevice();
                            DataType dataType = dataSource.getDataType();
                            Log.i(LOGTAG, "Data source found: " + dataSource.toString());
                            Log.i(LOGTAG, "Data Source type: " + dataType.getName());
                            Log.i(LOGTAG, "Device: " + device.getManufacturer() + " " + device.getModel());

                            registerFitnessDataListener(dataSource, dataType);
                        }
                    }
                });
    }

    // Register for real time fitness data
    public void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(LOGTAG, "Detected DataPoint field: " + field.getName());
                    Log.i(LOGTAG, "Detected DataPoint value: " + val);
                }
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(10, TimeUnit.SECONDS)
                        .build(),
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(LOGTAG, "Listener registered!");
                        } else {
                            Log.i(LOGTAG, "Listener not registered.");
                        }
                    }
                });
    }

    public void unregisterFitnessDataListener() {
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.remove(
                mClient,
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(LOGTAG, "Listener was removed!");
                        } else {
                            Log.i(LOGTAG, "Listener was not removed.");
                        }
                    }
                });
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
