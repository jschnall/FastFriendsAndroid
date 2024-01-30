package com.fastfriends.android.helper;

import android.content.Context;

import com.fastfriends.android.R;

import java.text.NumberFormat;
import java.util.Currency;

/**
 * Created by jschnall on 6/22/14.
 */
public class PriceHelper {
    public static  String formatPrice(Context context, String currencyCode, double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(Currency.getInstance(currencyCode));

        if (Math.abs(amount) < .0000000001) {
            return context.getString(R.string.free);
        }
        return format.format(amount);
    }
}
