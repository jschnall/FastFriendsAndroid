package com.fastfriends.android.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jschnall on 1/31/14.
 */
public class HashHelper {
    private final static String LOGTAG = HashHelper.class.getSimpleName();

    public static String macSha256Hex(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] output = sha256_HMAC.doFinal(data.getBytes());

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < output.length; i++) {
                sb.append(Integer.toString((output[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e){
            Log.e(LOGTAG, "Can't get macSha256Hex: ", e);
        }

        return null;
    }

    public static String sha1Hex(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = inputStream.read(bytes)) > 0) {
                messageDigest.update(bytes, 0, byteCount);
            }
            byte[] output = messageDigest.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < output.length; i++) {
                sb.append(Integer.toString((output[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOGTAG, "Can't get sha1Hex", e);
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "Can't get sha1Hex", e);
        } catch (IOException e) {
            Log.e(LOGTAG, "Can't get sha1Hex", e);
        }

        return null;
    }
}
