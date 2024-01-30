package com.fastfriends.android.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

import java.util.Date;
import java.util.List;

/**
 * Created by jschnall on 1/20/14.
 */
@DatabaseTable(tableName = "auth_token")
public class AuthToken {
    private static final String LOGTAG = AuthToken.class.getSimpleName();

    public static final String ID = "id";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES = "expires";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String SCOPE = "scope";
    public static final String EXPIRES_IN = "expires_in";

    @SerializedName(ID)
    @DatabaseField(id = true, columnName = ID)
    private long mId = 1;

    @SerializedName(ACCESS_TOKEN)
    @DatabaseField(columnName = ACCESS_TOKEN)
    private String mAccessToken = null;

    @SerializedName(TOKEN_TYPE)
    @DatabaseField(columnName = TOKEN_TYPE)
    private String mTokenType = null;

    @DatabaseField(columnName = EXPIRES)
    private Date mExpires = null;

    @SerializedName(REFRESH_TOKEN)
    @DatabaseField(columnName = REFRESH_TOKEN)
    private String mRefreshToken = null;

    @SerializedName(SCOPE)
    @DatabaseField(columnName = SCOPE)
    private String mScope = null;

    @SerializedName(EXPIRES_IN)
    private long mExpiresIn = 0;


    public AuthToken() {
        // Used by ORMLite
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    public String getTokenType() {
        return mTokenType;
    }

    public void setTokenType(String tokenType) {
        mTokenType = tokenType;
    }

    public Date getExpires() {
        return mExpires;
    }

    public void setExpires(Date expires) { mExpires = expires; }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        mRefreshToken = refreshToken;
    }

    public String getScope() {
        return mScope;
    }

    public void setScope(String scope) {
        mScope = scope;
    }

    public void setExpiresInSecs(long expiresIn) {
        mExpires = new Date(System.currentTimeMillis() + expiresIn * 1000);
    }

    public boolean isExpired() {
        Date now = new Date(System.currentTimeMillis());
        return now.after(mExpires);
    }

    public String getAuthHeader() {
        return mTokenType + " " + mAccessToken;
    }

    /**
     * Delete authToken with id, or all authTokens if no id is provided
     * @param id id of authToken to delete
     * @return number of authTokens deleted
     */
    public static int delete(Long id) {
        return DBManager.delete(AuthToken.class, id);
    }

    /**
     * Delete this authToken
     * @return number of authTokens deleted
     */

    public int delete() {
        Log.d(LOGTAG, "Deleting authToken.");
        return delete(mId);
    }

    public static List<AuthToken> getAll() {
        return DBManager.getAll(AuthToken.class);
    }

    public static AuthToken get() {
        return DBManager.get(AuthToken.class, 1);
    }

    /**
     * Initialize "expires" field and save an authToken to the database
     * @return number of authTokens saved
     */
    public static int save(AuthToken authToken) {
        authToken.setId(1);
        authToken.setExpiresInSecs(authToken.mExpiresIn);

        return DBManager.save(authToken);
    }

    /**
     * Initialize "expires" field and save this authToken to the database
     * @return number of authTokens saved
     */
    public int save() {
        return save(this);
    }


}
