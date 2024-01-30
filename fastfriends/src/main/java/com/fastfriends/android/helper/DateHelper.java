package com.fastfriends.android.helper;

import android.content.Context;

import com.fastfriends.android.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    public static final String FACEBOOK_DATE_FORMAT = "MM/dd/yyyy";
    public static final String GOOGLE_PLUS_DATE_FORMAT = "yyyy-MM-dd";

    /** The maximum date possible. */
    public static Date MAX_DATE = new Date(Long.MAX_VALUE);

    private static final long ONE_MINUTE = 1000 * 60;
    private static final long ONE_HOUR = ONE_MINUTE * 60;
    private static final long ONE_DAY = ONE_HOUR * 24;
    private static final long ONE_WEEK = ONE_DAY * 7;
    private static final long THIRTY_DAYS = ONE_DAY * 30;

    private DateHelper() {
    }

    /**
    * <p>Checks if two dates are on the same day ignoring time.</p>
    * @param date1  the first date, not altered, not null
    * @param date2  the second date, not altered, not null
    * @return true if they represent the same day
    * @throws IllegalArgumentException if either date is <code>null</code>
    */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
    * <p>Checks if two calendars represent the same day ignoring time.</p>
    * @param cal1  the first calendar, not altered, not null
    * @param cal2  the second calendar, not altered, not null
    * @return true if they represent the same day
    * @throws IllegalArgumentException if either calendar is <code>null</code>
    */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
       if (cal1 == null || cal2 == null) {
           throw new IllegalArgumentException("The dates must not be null");
       }
       return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
               cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
    * <p>Checks if a date is today.</p>
    * @param date the date, not altered, not null.
    * @return true if the date is today.
    * @throws IllegalArgumentException if the date is <code>null</code>
    */
    public static boolean isToday(Date date) {
       return isSameDay(date, Calendar.getInstance().getTime());
    }

    /**
    * <p>Checks if a calendar date is today.</p>
    * @param cal  the calendar, not altered, not null
    * @return true if cal date is today
    * @throws IllegalArgumentException if the calendar is <code>null</code>
    */
    public static boolean isToday(Calendar cal) {
       return isSameDay(cal, Calendar.getInstance());
    }

    /**
    * <p>Checks if the first date is before the second date ignoring time.</p>
    * @param date1 the first date, not altered, not null
    * @param date2 the second date, not altered, not null
    * @return true if the first date day is before the second date day.
    * @throws IllegalArgumentException if the date is <code>null</code>
    */
    public static boolean isBeforeDay(Date date1, Date date2) {
       if (date1 == null || date2 == null) {
           throw new IllegalArgumentException("The dates must not be null");
       }
       Calendar cal1 = Calendar.getInstance();
       cal1.setTime(date1);
       Calendar cal2 = Calendar.getInstance();
       cal2.setTime(date2);
       return isBeforeDay(cal1, cal2);
    }

    /**
    * <p>Checks if the first calendar date is before the second calendar date ignoring time.</p>
    * @param cal1 the first calendar, not altered, not null.
    * @param cal2 the second calendar, not altered, not null.
    * @return true if cal1 date is before cal2 date ignoring time.
    * @throws IllegalArgumentException if either of the calendars are <code>null</code>
    */
    public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
       if (cal1 == null || cal2 == null) {
           throw new IllegalArgumentException("The dates must not be null");
       }
       if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return true;
       if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return false;
       if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true;
       if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false;
       return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
    * <p>Checks if the first date is after the second date ignoring time.</p>
    * @param date1 the first date, not altered, not null
    * @param date2 the second date, not altered, not null
    * @return true if the first date day is after the second date day.
    * @throws IllegalArgumentException if the date is <code>null</code>
    */
    public static boolean isAfterDay(Date date1, Date date2) {
       if (date1 == null || date2 == null) {
           throw new IllegalArgumentException("The dates must not be null");
       }
       Calendar cal1 = Calendar.getInstance();
       cal1.setTime(date1);
       Calendar cal2 = Calendar.getInstance();
       cal2.setTime(date2);
       return isAfterDay(cal1, cal2);
    }

    /**
    * <p>Checks if the first calendar date is after the second calendar date ignoring time.</p>
    * @param cal1 the first calendar, not altered, not null.
    * @param cal2 the second calendar, not altered, not null.
    * @return true if cal1 date is after cal2 date ignoring time.
    * @throws IllegalArgumentException if either of the calendars are <code>null</code>
    */
    public static boolean isAfterDay(Calendar cal1, Calendar cal2) {
       if (cal1 == null || cal2 == null) {
           throw new IllegalArgumentException("The dates must not be null");
       }
       if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return false;
       if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return true;
       if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return false;
       if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return true;
       return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
    * <p>Checks if a date is after today and within a number of days in the future.</p>
    * @param date the date to check, not altered, not null.
    * @param days the number of days.
    * @return true if the date day is after today and within days in the future .
    * @throws IllegalArgumentException if the date is <code>null</code>
    */
    public static boolean isWithinDaysFuture(Date date, int days) {
       if (date == null) {
           throw new IllegalArgumentException("The date must not be null");
       }
       Calendar cal = Calendar.getInstance();
       cal.setTime(date);
       return isWithinDaysFuture(cal, days);
    }

    /**
    * <p>Checks if a calendar date is after today and within a number of days in the future.</p>
    * @param cal the calendar, not altered, not null
    * @param days the number of days.
    * @return true if the calendar date day is after today and within days in the future .
    * @throws IllegalArgumentException if the calendar is <code>null</code>
    */
    public static boolean isWithinDaysFuture(Calendar cal, int days) {
       if (cal == null) {
           throw new IllegalArgumentException("The date must not be null");
       }
       Calendar today = Calendar.getInstance();
       Calendar future = Calendar.getInstance();
       future.add(Calendar.DAY_OF_YEAR, days);
       return (isAfterDay(cal, today) && ! isAfterDay(cal, future));
    }

    /** Returns the given date with the time set to the start of the day. */
    public static Date getStart(Date date) {
       return clearTime(date);
    }

    /** Returns the given date with the time values cleared. */
    public static Date clearTime(Date date) {
       if (date == null) {
           return null;
       }
       Calendar c = Calendar.getInstance();
       c.setTime(date);
       c.set(Calendar.HOUR_OF_DAY, 0);
       c.set(Calendar.MINUTE, 0);
       c.set(Calendar.SECOND, 0);
       c.set(Calendar.MILLISECOND, 0);
       return c.getTime();
    }

    /** Determines whether or not a date has any time values (hour, minute,
    * seconds or millisecondsReturns the given date with the time values cleared. */

    /**
    * Determines whether or not a date has any time values.
    * @param date The date.
    * @return true iff the date is not null and any of the date's hour, minute,
    * seconds or millisecond values are greater than zero.
    */
    public static boolean hasTime(Date date) {
       if (date == null) {
           return false;
       }
       Calendar c = Calendar.getInstance();
       c.setTime(date);
       if (c.get(Calendar.HOUR_OF_DAY) > 0) {
           return true;
       }
       if (c.get(Calendar.MINUTE) > 0) {
           return true;
       }
       if (c.get(Calendar.SECOND) > 0) {
           return true;
       }
       if (c.get(Calendar.MILLISECOND) > 0) {
           return true;
       }
       return false;
    }

    /** Returns the given date with time set to the end of the day */
    public static Date getEnd(Date date) {
       if (date == null) {
           return null;
       }
       Calendar c = Calendar.getInstance();
       c.setTime(date);
       c.set(Calendar.HOUR_OF_DAY, 23);
       c.set(Calendar.MINUTE, 59);
       c.set(Calendar.SECOND, 59);
       c.set(Calendar.MILLISECOND, 999);
       return c.getTime();
    }

    /**
    * Returns the maximum of two dates. A null date is treated as being less
    * than any non-null date.
    */
    public static Date max(Date d1, Date d2) {
       if (d1 == null && d2 == null) return null;
       if (d1 == null) return d2;
       if (d2 == null) return d1;
       return (d1.after(d2)) ? d1 : d2;
    }

    /**
    * Returns the minimum of two dates. A null date is treated as being greater
    * than any non-null date.
    */
    public static Date min(Date d1, Date d2) {
       if (d1 == null && d2 == null) return null;
       if (d1 == null) return d2;
       if (d2 == null) return d1;
       return (d1.before(d2)) ? d1 : d2;
    }

    public static String buildTimeStamp(Context context, Date date, boolean hideAfterMonth) {
        long now = System.currentTimeMillis();
        long period = now - date.getTime();
        if (period > THIRTY_DAYS) {
            if (hideAfterMonth) {
                return "";
            }

            SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.FACEBOOK_DATE_FORMAT);
            return sdf.format(date);
        }
        if (period > ONE_DAY) {
            return context.getString(R.string.days, (int) (period / ONE_DAY));
        }
        if (period > ONE_HOUR) {
            return context.getString(R.string.hours_ago, (int) (period / ONE_HOUR ));
        }
        if (period > ONE_MINUTE) {
            return context.getString(R.string.minutes_ago, (int) (period / ONE_MINUTE));
        }
        return context.getString(R.string.less_than_1_minute_ago);
    }

    public static String buildShortTimeStamp(Context context, Date date, boolean hideAfterMonth) {
        long now = System.currentTimeMillis();
        long period = now - date.getTime();
        if (period > THIRTY_DAYS) {
            if (hideAfterMonth) {
                return "";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.FACEBOOK_DATE_FORMAT);
            return sdf.format(date);
        }
        if (period > ONE_DAY) {
            return context.getString(R.string.days_short, (int) (period / ONE_DAY));
        }
        if (period > ONE_HOUR) {
            return context.getString(R.string.hours_short, (int) (period / ONE_HOUR ));
        }
        if (period > ONE_MINUTE) {
            return context.getString(R.string.minutes_short, (int) (period / ONE_MINUTE));
        }
        return context.getString(R.string.less_than_1_minute_short);
    }

    // Format time in format "X hrs Y mins", rounding up to nearest minute
    public static String formatFitTime(Context context, long millis) {
        if (millis > ONE_HOUR) {
            int hours = (int) (millis / ONE_HOUR);
            String hourStr = context.getResources().getQuantityString(R.plurals.hours_medium, hours, hours);

            int minutes = (int) ((millis - hours * ONE_HOUR) / ONE_MINUTE);
            String minStr = context.getResources().getQuantityString(R.plurals.minutes_medium, minutes, minutes);

            return hourStr + " " + minStr;
        } else if (millis > ONE_MINUTE) {
            int minutes = (int) (millis / ONE_MINUTE);
            return context.getResources().getQuantityString(R.plurals.minutes_medium, minutes, minutes);
        }
        return context.getString(R.string.less_than_1_minute);
    }
}
