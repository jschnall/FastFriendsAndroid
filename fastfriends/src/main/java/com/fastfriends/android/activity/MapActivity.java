package com.fastfriends.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.R;
import com.fastfriends.android.adapter.PlacesAutoCompleteAdapter;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Point;
import com.fastfriends.android.model.Prediction;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private final static String LOGTAG = MapActivity.class.getSimpleName();

    private int AUTO_COMPLETE_DELAY = 800; // millisecs

    public static final String EXTRA_EDIT_MODE = "edit_mode";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    // Fragment tags
    private final static String MAP = "map";

    private ViewGroup mStatusLayout;
    private TextView mStatusTextView;
    private ViewGroup mFragmentContainer;
    private GoogleMap mGoogleMap;
    private Button mDoneButton;
    private Button mCancelButton;

    // location selection
    private AutoCompleteTextView mLocationNameView;
    private PlacesAutoCompleteAdapter mAdapter;

    private boolean mLocationEditMode = false;
    private Location mOldLocation;
    private Location mLocation;
    private Marker mMarker;

    private Handler mHandler = new Handler();
    private LocationClient mLocationClient;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.cancel: {
                    finish();
                    break;
                }
                case R.id.done: {
                    if (TextUtils.isEmpty(mLocationNameView.getText()) && mMarker != null) {
                        mGetAddressFromPositionTask = new GetAddressFromPositionTask(MapActivity.this, true);
                        mGetAddressFromPositionTask.execute(mMarker.getPosition());
                    } else {
                        mGetAddressFromNameTask = new GetAddressFromNameTask(MapActivity.this, true);
                        mGetAddressFromNameTask.execute(mLocationNameView.getText().toString());
                    }
                    break;
                }
            }
        }
    };

    private class GetAddressFromPositionTask extends AsyncTask<LatLng, Void, List<Address>> {
        Context mContext;
        LatLng mPosition;
        boolean mFinishActivity;

        public GetAddressFromPositionTask(Context context, boolean finishActivity) {
            super();
            mContext = context;
            mFinishActivity = finishActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mFinishActivity) {
                showProgress(true, getString(R.string.progress_please_wait));
            }
        }

        @Override
        protected List<Address> doInBackground(LatLng... params) {
            mPosition = params[0];
            List<Address> addresses = null;

            try {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                addresses = geocoder.getFromLocation(mPosition.latitude, mPosition.longitude, 1);
            } catch (IOException e) {
                Log.e(LOGTAG, "Can't get address for: " + formatLatLng(mPosition));
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                mLocation = new Location(address);

                updateLocationName(formatAddress(address));
                mMarker.setTitle(mLocation.getStreetAddress());
                mMarker.setSnippet(formatLatLng(address.getLatitude(), address.getLongitude()));
            } else {
                // No known address, show geo coords instead
                String positionStr = formatLatLng(mPosition);
                updateLocationName(positionStr);
                mMarker.setTitle(positionStr);
                mMarker.setSnippet("");
            }
            mMarker.showInfoWindow();

            if (mFinishActivity) {
                finishWithResult();
            }
        }
    }
    private GetAddressFromPositionTask mGetAddressFromPositionTask;

    private class GetAddressFromNameTask extends AsyncTask<String, Void, List<Address>> {
        Context mContext;
        boolean mFinishActivity;

        public GetAddressFromNameTask(Context context, boolean finishActivity) {
            super();
            mContext = context;
            mFinishActivity = finishActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mFinishActivity) {
                showProgress(true, getString(R.string.progress_please_wait));
            }
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            String name = params[0];
            List<Address> addresses = null;

            try {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                addresses = geocoder.getFromLocationName(name, 1);
            } catch (IOException e) {
                Log.e(LOGTAG, "Can't get address for: " + name);
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                mLocation = new Location(address);

                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                showLocation(latLng);
                setMarker(latLng, mLocation.getStreetAddress());
                mMarker.setTitle(mLocation.getStreetAddress());
                mMarker.setSnippet(formatLatLng(address.getLatitude(), address.getLongitude()));

                // Hide keyboard
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mLocationNameView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    Log.e(LOGTAG, "Can't hide keyboard.", e);
                }

                if (mFinishActivity) {
                    finishWithResult();
                }
            }
        }
    }
    private GetAddressFromNameTask mGetAddressFromNameTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initActionBar();

        mStatusLayout = (ViewGroup) findViewById(R.id.status);
        mStatusTextView = (TextView) findViewById(R.id.status_message);
        mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);

        mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        if (mGoogleMap == null) {
            Toast.makeText(this, getString(R.string.maps_unavailable),
                    Toast.LENGTH_LONG).show();
        } else {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    mMarker.hideInfoWindow();
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    // TODO update a textview somewhere showing position of pin
                    //LatLng latLng = marker.getPosition();
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng latLng = marker.getPosition();
                    showLocation(latLng);

                    mGetAddressFromPositionTask = new GetAddressFromPositionTask(MapActivity.this, false);
                    mGetAddressFromPositionTask.execute(latLng);
                }
            });

        }
        mLocationClient = new LocationClient(this, this, this);

        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(mOnClickListener);

        mDoneButton = (Button) findViewById(R.id.done);
        mDoneButton.setOnClickListener(mOnClickListener);
        mDoneButton.setEnabled(false);

        handleIntent();
    }

    public void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        View customView = getLayoutInflater().inflate(R.layout.actionbar_location, null);
        mLocationNameView =  (AutoCompleteTextView) customView.findViewById(R.id.search_box);
        mAdapter = new PlacesAutoCompleteAdapter(this, mGoogleMap);
        // Note: set adapter after setting old location name to prevent extra auto complete call
        if (mOldLocation != null) {
            mLocationNameView.setText(formatAddress(mOldLocation));
        }
        mLocationNameView.setAdapter(mAdapter);
        mLocationNameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mMarker != null) {
                    mMarker.hideInfoWindow();
                }
                Prediction prediction = (Prediction) parent.getItemAtPosition(position);
                showLocation(prediction.getDescription());
            }
        });

        actionBar.setCustomView(customView);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            mLocationEditMode = bundle.getBoolean(EXTRA_EDIT_MODE, false);
            if (mLocationEditMode) {
                if (bundle.containsKey(EXTRA_LOCATION)) {
                    mOldLocation = bundle.getParcelable(EXTRA_LOCATION);
                } else {
                    double latitude = bundle.getDouble(EXTRA_LATITUDE, 0);
                    double longitude = bundle.getDouble(EXTRA_LONGITUDE, 0);
                    if (!LocationHelper.isEmpty(latitude, longitude)) {
                        mOldLocation = new Location();
                        Point point = mOldLocation.getPoint();
                        point.setLatitude(latitude);
                        point.setLongitude(longitude);
                    }
                }

                initActionBar();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            mFragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed(){
        if (View.VISIBLE != mStatusLayout.getVisibility()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mLocation != null) {
            Point point = mLocation.getPoint();
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            showLocation(latLng);
            setMarker(latLng, mLocation.getStreetAddress());
            return;
        }

        if (mOldLocation != null) {
            Point point = mOldLocation.getPoint();
            double latitude = point.getLatitude();
            double longitude = point.getLongitude();
            if (!LocationHelper.isEmpty(latitude, longitude)) {
                LatLng latLng = new LatLng(latitude, longitude);
                showLocation(latLng);
                setMarker(latLng, mOldLocation.getStreetAddress());
                return;
            }
        }
        showMyLocation();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_set_marker:
                LatLng latLng = mGoogleMap.getCameraPosition().target;
                setMarker(latLng, "");
                mMarker.hideInfoWindow();

                mGetAddressFromPositionTask = new GetAddressFromPositionTask(MapActivity.this, false);
                mGetAddressFromPositionTask.execute(latLng);
                return true;
        };
        return super.onOptionsItemSelected(item);
    }

    private void showLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        showLocation(latLng);
    }

    private void showLocation(LatLng latLng) {
        if (mGoogleMap != null) {
            CameraPosition myPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(17).build();
            mGoogleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(myPosition), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            if (mMarker != null) {
                                mMarker.showInfoWindow();
                            }
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
        }
    }

    private void showLocation(String locationName) {
        mGetAddressFromNameTask = new GetAddressFromNameTask(MapActivity.this, false);
        mGetAddressFromNameTask.execute(locationName);
    }

    private void showMyLocation() {
        if (mLocationClient != null) {
            android.location.Location myLocation = mLocationClient.getLastLocation();
            if (myLocation == null) {
                return;
            }
            LatLng myLatLng = new LatLng(myLocation.getLatitude(),
                    myLocation.getLongitude());

            showLocation(myLatLng);
        }
    }

    private void setMarker(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        setMarker(latLng, formatLatLng(latLng));
    }

    private void setMarker(LatLng latLng, String address) {
        if (mGoogleMap == null) {
            return;
        }
        if (mMarker == null) {
            mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .draggable(true)
                    .position(latLng)
                    .title(address)
                    .snippet(formatLatLng(latLng))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        } else {
            mMarker.setPosition(latLng);
        }

        mDoneButton.setEnabled(true);
    }

    private void updateLocationName(String name) {
        // Prevent unnecessary auto complete call
        mLocationNameView.setAdapter((PlacesAutoCompleteAdapter) null);
        mLocationNameView.setText(name);
        mLocationNameView.setAdapter(mAdapter);
    }

    private String formatAddress(Address address) {
        StringBuilder builder = new StringBuilder();

        int maxIndex = address.getMaxAddressLineIndex();
        if (maxIndex < 0) {
            return null;
        }
        builder.append(address.getAddressLine(0));
        for (int i = 1; i <= maxIndex; i++) {
            builder.append(" " + address.getAddressLine(i));
        }
        return builder.toString();
    }


    private String formatAddress(Location location) {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(location.getName())) {
            builder.append(location.getName());
        } else if (!TextUtils.isEmpty(location.getSubThoroughfare())) {
            builder.append(location.getSubThoroughfare());
        }

        if (!TextUtils.isEmpty(location.getThoroughfare())) {
            builder.append(" ");
            builder.append(location.getThoroughfare());
        }
        if (!TextUtils.isEmpty(location.getSubLocality())) {
            builder.append(" ");
            builder.append(location.getSubLocality());
        }
        if (!TextUtils.isEmpty(location.getLocality())) {
            builder.append(" ");
            builder.append(location.getLocality());
        }
        if (!TextUtils.isEmpty(location.getAdminArea())) {
            builder.append(" ");
            builder.append(location.getAdminArea());
        }
        if (!TextUtils.isEmpty(location.getPostalCode())) {
            builder.append(" ");
            builder.append(location.getPostalCode());
        }

        return builder.toString();
    }

    public static String formatLatLng(LatLng latLng) {
       return formatLatLng(latLng.latitude, latLng.longitude);
    }

    public static String formatLatLng(Double latitude, Double longitude) {
        return "(" + latitude +  ", " + longitude +  ")";
    }

    private void finishWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LOCATION, mLocation);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
