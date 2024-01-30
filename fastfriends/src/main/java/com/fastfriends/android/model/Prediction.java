package com.fastfriends.android.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by jschnall on 1/20/14.
 */
public class Prediction {
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String REFERENCE = "reference";

    @SerializedName(ID)
    String mId;
    @SerializedName(DESCRIPTION)
    String mDescription;
    @SerializedName(REFERENCE)
    String mReference;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String reference) {
        mReference = reference;
    }
};
