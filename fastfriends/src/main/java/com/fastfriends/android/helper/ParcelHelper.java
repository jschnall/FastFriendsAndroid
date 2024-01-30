package com.fastfriends.android.helper;

import android.os.Parcel;

import java.util.Date;

/**
 * Created by jschnall on 3/19/14.
 */
public class ParcelHelper {
    private ParcelHelper() {
    }

    public static boolean readBoolean(Parcel in) {
        return in.readInt() != 0;
    }

    public static void writeBoolean(Parcel out, boolean b) {
        out.writeInt(b?1:0);
    }

    public static Date readDate(Parcel in) {
        int exists = in.readInt();
        if (exists == 0) {
            return null;
        } else {
            return new Date(in.readLong());
        }
    }

    public static void writeDate(Parcel out, Date date) {
        if (date == null) {
            out.writeInt(0);
        } else {
            out.writeInt(1);
            out.writeLong(date.getTime());
        }
    }


}
