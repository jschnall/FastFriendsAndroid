package com.fastfriends.android.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.fastfriends.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by jschnall on 8/18/14.
 */
public class SharingHelper {
    public static String LOGTAG = SharingHelper.class.getSimpleName();

    Context mContext;
    String mTitle;
    String mSubject;
    String mText;

    public SharingHelper(Context context, String title, String subject, String text) {
        mContext = context;
        mTitle = title;
        mText = text;
        mSubject = subject;
    }

    public void createChooser() {
        Intent intent = createShareIntent();
        mContext.startActivity(Intent.createChooser(intent, mTitle));
    }

    public Intent createShareIntent() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_SUBJECT, mSubject);
        intent.putExtra(Intent.EXTRA_TEXT, mText);

        return intent;
    }

    private boolean providerIsFacebook(String packageName) {
        return packageName.equalsIgnoreCase("com.facebook.katana") || packageName.equalsIgnoreCase("com.amazon.kindle.facebook");
    }

    private boolean providerIsTwitter(String packageName) {
        return packageName.equalsIgnoreCase("com.twitter.android");
    }

    private boolean providerIsTextEmail(String packageName) {
        return packageName.equalsIgnoreCase("com.android.email");
    }

    private boolean providerIsHtmlEmail(String packageName) {
        return packageName.toLowerCase().startsWith("com.google.android.gm") || packageName.equals("com.amazon.email");
    }

    private boolean providerIsSMS(String packageName) {
        return packageName.equalsIgnoreCase("com.android.mms");
    }
}
