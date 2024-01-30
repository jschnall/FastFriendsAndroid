package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jschnall on 2/13/14.
 */
public class AuthError {
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";

    @SerializedName(ERROR)
    public String mError;
    @SerializedName(ERROR_DESCRIPTION)
    public String mErrorDetails;

    public String getError() {
        return mError;
    }

    public void setError(String error) {
        mError = error;
    }

    public String getErrorDetails() {
        return mErrorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        mErrorDetails = errorDetails;
    }
}
