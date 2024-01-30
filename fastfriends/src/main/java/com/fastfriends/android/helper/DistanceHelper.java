package com.fastfriends.android.helper;

import android.content.Context;

import com.fastfriends.android.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Created by jschnall on 6/22/14.
 */
public class DistanceHelper {
    public static String formatDistance(Context context, double meters, boolean metric) {
        if (metric) {
            // Convert to kilometers
            double value = meters / 1000;

            if (value < 0.1) {
                return "< " + context.getString(R.string.kilometers, 0.1);
            }
            return context.getString(R.string.kilometers, new DecimalFormat("#.#").format(value));
        }
        // Convert to miles
        double value = meters * 0.00062137;

        if (value < 0.1) {
            return "< " + context.getString(R.string.miles, 0.1);
        }
        return context.getString(R.string.miles, new DecimalFormat("#.#").format(value));
    }
}
