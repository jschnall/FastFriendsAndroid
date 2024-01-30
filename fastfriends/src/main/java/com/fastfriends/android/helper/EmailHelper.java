package com.fastfriends.android.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by jschnall on 2/11/14.
 *
 * This class uses the DBManager to get the primary email address of the
 * current user.
 *
 * Requires "android.permission.GET_ACCOUNTS" in manifest.
 */
public class EmailHelper {
    private EmailHelper() {
    }

    public static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);

        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }
}
