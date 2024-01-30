package com.fastfriends.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Currency;
import java.util.Locale;

/**
 * Created by jschnall on 2/17/14.
 */
public class Price implements Parcelable {
    public static final int MIN_AMOUNT = 0;
    public static final int MAX_AMOUNT = 1000000000; // 1 billion

    public static final String ID = "id";
    public static final String CURRENCY_CODE = "currency_code";
    public static final String AMOUNT = "amount";
    public static final String CONVERTED_AMOUNT = "converted_amount";

    @SerializedName(ID)
    private long mId;
    @SerializedName(CURRENCY_CODE)
    private String mCurrencyCode;
    @SerializedName(AMOUNT)
    private double mAmount;
    @SerializedName(CONVERTED_AMOUNT)
    private double mConvertedAmount;

    public static final Parcelable.Creator<Price> CREATOR = new Parcelable.Creator<Price>() {
        public Price createFromParcel(Parcel in) {
            return new Price(in);
        }

        public Price[] newArray(int size) {
            return new Price[size];
        }
    };


    public Price() {
        mCurrencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
    }

    public Price(String currencyCode, double amount) {
        mCurrencyCode = currencyCode;
        mAmount = amount;
    }

    public Price(Parcel in) {
        mId = in.readLong();
        mCurrencyCode = in.readString();
        mAmount = in.readDouble();
        mConvertedAmount = in.readDouble();
    }

    public Price(Price price) {
        copy(price);
    }

    public void copy(Price price) {
        mId = price.mId;
        mCurrencyCode = price.mCurrencyCode;
        mAmount = price.mAmount;
        mConvertedAmount = price.mConvertedAmount;

    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mId);
        out.writeString(mCurrencyCode);
        out.writeDouble(mAmount);
        out.writeDouble(mConvertedAmount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }


    public String getCurrencyCode() {
        return mCurrencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        mCurrencyCode = currencyCode;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public double getConvertedAmount() {
        return mConvertedAmount;
    }

    public void setConvertedAmount(double amount) {
        mConvertedAmount = amount;
    }

}
