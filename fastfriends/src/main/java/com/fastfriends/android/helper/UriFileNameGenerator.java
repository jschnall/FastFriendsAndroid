package com.fastfriends.android.helper;

import android.net.Uri;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;

/**
 * Created by jschnall on 4/9/14.
 */
public class UriFileNameGenerator implements FileNameGenerator {
    @Override
    public String generate(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        String path = uri.getPath();
        return String.valueOf(path.hashCode());
    }
}
