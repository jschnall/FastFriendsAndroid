package com.fastfriends.android.listener;

import com.facebook.Session;
import com.google.android.gms.plus.PlusClient;

/**
 * Created by jschnall on 2/13/14.
 */
public interface AuthenticationInteractionListener {
    public void showCreateAccount(String email);
    public void showSignIn(String email);
    public void showProgress(boolean show, String message);
    public void showForgotPassword(String email);

    public void OnLoggedIn(Session session);
    public void OnLoggedOut();
}
