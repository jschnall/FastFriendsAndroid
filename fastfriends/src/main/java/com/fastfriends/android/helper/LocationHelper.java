package com.fastfriends.android.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by jschnall on 7/3/14.
 */
public class LocationHelper {
    private final static String LOGTAG = LocationHelper.class.getSimpleName();

    private final static double TOLERANCE = .000001;

    private LocationHelper() {
    }

    public static LocationManager startLocationUpdates(Context context, LocationListener listener) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return null;
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                        2, listener);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000,
                        2, listener);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't get location updates", e);
        }

        return locationManager;
    }

    public static Location getLastKnownLocation(LocationManager locationManager) {
        Location result = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            result = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            result = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (result == null) {
            result = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (result == null) {
            result = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return result;
    }

    public static boolean isEmpty(double latitude, double longitude) {
        return Math.abs(latitude) < TOLERANCE && Math.abs(longitude) < TOLERANCE;
    }
}
