package com.fastfriends.android.web;

import android.net.Uri;
import android.util.Log;

import com.fastfriends.android.model.AuthToken;

import java.net.URI;
import java.util.Date;


/**
 * Created by jschnall on 1/19/14.
 */
public class AccountWebServiceManager {
/*
    private static final String LOGTAG = AccountWebServiceManager.class.getSimpleName();

    // Used to create passwords for account creation from social logins
    private static final String HASH_SECRET = "social_secret";

    // Schemes
    public static final String HTTPS = "https://";
    public static final String HTTP = "http://";


    private static class CreateAccountMessage {
        String mFirstName;
        String mLastName;
        String mEmail;
        String mPassword;
        Date mBirthday;
        String mGender;

        CreateAccountMessage() {
        }

        CreateAccountMessage(String firstName, String lastName, String email, String password, Date birthday, String gender) {
            mFirstName = firstName;
            mLastName = lastName;
            mEmail = email;
            mPassword = password;
            mBirthday = birthday;
            mGender = gender;
        }

        public String getFirstName() {
            return mFirstName;
        }

        public void setFirstName(String firstName) {
            mFirstName = firstName;
        }

        public String getLastName() {
            return mLastName;
        }

        public void setLastName(String lastName) {
            mLastName = lastName;
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String email) {
            mEmail = email;
        }

        public String getPassword() {
            return mPassword;
        }

        public void setPassword(String password) {
            mPassword = password;
        }

        public Date getBirthday() {
            return mBirthday;
        }

        public void setBirthday(Date birthday) {
            mBirthday = birthday;
        }

        public String getGender() {
            return mGender;
        }

        public void setGender(String gender) {
            mGender = gender;
        }
    }

    public static String createUser(String firstName, String lastName, String email, String password, Date birthday, String gender) {
        try {
            CreateAccountMessage message = new CreateAccountMessage(firstName, lastName, email, password, birthday, gender);
            String url = urlForCreateAccount();
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            String response = restTemplate.postForObject(url, message, String.class);

            return response;
        } catch (Exception e) {
            Log.e(LOGTAG, "CreateAccount request failed.", e);
        }

        return null;
    }

    public static String resetPassword(String email) {
        try {
            URI url = URI.create(urlForForgotPassword(email));
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);

            return entity.getStatusCode().getReasonPhrase();
        } catch (Exception e) {
            Log.e(LOGTAG, "ForgotPassword request failed.", e);
        }

        return null;
    }

    public static AuthToken refreshAuthToken(AuthToken authToken) {
        URI url = URI.create(urlForRefreshAuthToken(authToken));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        AuthToken newAuthToken = restTemplate.postForObject(url, null, AuthToken.class);

        return newAuthToken;
    }

    public static AuthToken signIn(String email, String password) throws Exception {
        URI url = URI.create(urlForSignIn(email, password));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        AuthToken authToken = restTemplate.postForObject(url, null, AuthToken.class);

        return authToken;
    }

    public static String signOut() {
        // TODO delete tokens for user?
        return null;
    }

    private static String urlForCreateAccount() {
        Uri url = Uri.parse(HTTPS + HOST + "/create_account/");

        Log.d(LOGTAG, "urlForCreateAccount: " + url);
        return url.toString();
    }

    private static String urlForForgotPassword(String email) {
        StringBuilder url = new StringBuilder(HTTPS + HOST +  "/reset_password/");

        url.append("?email=" + email);

        Log.d(LOGTAG, "urlForForgotPassword: " + url);
        return url.toString();
    }

    private static String urlForRefreshAuthToken(AuthToken authToken) {
        Uri.Builder builder = Uri.parse(HTTP + HOST + "/o/token/").buildUpon();

        builder.appendQueryParameter("client_id", CLIENT_ID);
        builder.appendQueryParameter("client_secret", CLIENT_SECRET);
        builder.appendQueryParameter("grant_type", "refresh_token");
        builder.appendQueryParameter("refresh_token", authToken.getRefreshToken());
        builder.appendQueryParameter("scope", authToken.getScope());

        String url = builder.build().toString();
        Log.d(LOGTAG, "urlForRefreshAuthToken: " + url);

        return url;
    }

    private static String urlForSignIn(String email, String password) {
        Uri.Builder builder = Uri.parse(HTTP + HOST + "/o/token/").buildUpon();

        builder.appendQueryParameter("client_id", CLIENT_ID);
        builder.appendQueryParameter("client_secret", CLIENT_SECRET);
        builder.appendQueryParameter("grant_type", "password");
        builder.appendQueryParameter("username", email);
        builder.appendQueryParameter("password", password);

        String url = builder.build().toString();
        Log.d(LOGTAG, "urlForSignIn: " + url);

        return url;
    }

    private static String urlForSignOut() {
        // TODO
        return null;
    }
*/
}
