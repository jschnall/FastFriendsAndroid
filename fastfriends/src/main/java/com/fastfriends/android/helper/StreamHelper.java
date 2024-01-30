package com.fastfriends.android.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by jschnall on 2/12/14.
 */
public class StreamHelper {
    public static StringBuilder streamToString(InputStream is) throws IOException {
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            for (String line; (line = reader.readLine()) != null;) {
                result.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }

        return result;
    }
}
