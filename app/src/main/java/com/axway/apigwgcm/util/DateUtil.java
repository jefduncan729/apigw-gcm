package com.axway.apigwgcm.util;

import android.text.format.DateUtils;

import com.axway.apigwgcm.BaseApp;
import com.axway.apigwgcm.db.DbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by su on 12/5/2014.
 */
public class DateUtil {
    public static DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
    public static DateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    public static DateFormat TIME_ONLY_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);
    public static DateFormat TIME_SHORT_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    public static DateFormat FILENAME_FORMAT = new SimpleDateFormat("MMddyyyy_HHmmss_S", Locale.US);

    public static int MILLIS_PER_SECOND = 1000;
    public static int SECONDS_PER_MINUTE = 60;
    public static int MINUTES_PER_HOUR = 60;
    public static int HOURS_PER_DAY = 24;
    public static int DAYS_PER_WEEK = 7;

    public static long ONE_SECOND_MS = MILLIS_PER_SECOND;
    public static long ONE_MINUTE_MS = ONE_SECOND_MS * SECONDS_PER_MINUTE;
    public static long ONE_HOUR_MS = ONE_MINUTE_MS * MINUTES_PER_HOUR;
    public static long ONE_DAY_MS = ONE_HOUR_MS * HOURS_PER_DAY;
    public static long ONE_WEEK_MS = ONE_DAY_MS * DAYS_PER_WEEK;

    public static String SECOND = "second";
    public static String MINUTE = "minute";
    public static String HOUR = "hour";
    public static String DAY = "day";
    public static String WEEK = "week";
    public static String MONTH = "month";
    public static String YEAR = "year";

    public static String formatDatetime(long time) {
        Date d = new Date(time);
        String rv = DATE_TIME_FORMAT.format(d);
        return rv;
    }

    public static String filenameFormat(long time) {
        Date d = new Date(time);
        String rv = FILENAME_FORMAT.format(d);
        return rv;
    }

    public static String formatDate(long time) {
        Date d = new Date(time);
        String rv = DATE_ONLY_FORMAT.format(d);
        return rv;
    }

    public static String formatTime(long time) {
        Date d = new Date(time);
        String rv = TIME_ONLY_FORMAT.format(d);
        return rv;
    }

    private static String ago(long val, String unit) {
        return ago(val, unit, true);
    }

    private static String ago(long val, String unit, boolean finish) {
        StringBuilder sb = new StringBuilder();
        sb.append(val).append(" ").append(unit);
        if (val != 1)
            sb.append("s");
        if (finish)
            sb.append(" ago");
        return sb.toString();
    }

    private static String fromNow(long val, String unit) {
        return ago(val, unit, true);
    }

    private static String fromNow(long val, String unit, boolean finish) {
        StringBuilder sb = new StringBuilder();
        sb.append(val).append(" ").append(unit);
        if (val != 1)
            sb.append("s");
        if (finish)
            sb.append(" from now");
        return sb.toString();
    }

    public static long secondsFromNow(final long time) {
        return (millisFromNow(time) / ONE_SECOND_MS);
    }

    public static long millisFromNow(final long time) {
        if (time == 0)
            return 0;
        return (System.currentTimeMillis() - time);
    }

    public static String relativeTime(final long time, final int flags) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, flags).toString();
/*
                BaseApp.getInstance().getBaseContext(),
                time,
                DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, flags).toString();
*/
    }
    public static String relativeTime(final long time) {
        return relativeTime(time, 0);
//        long now = System.currentTimeMillis();
//        if (now == time)
//            return "Just now";
//        String s = null;
//        if (now > time)
//            s = asPastTime(now - time);
//        else
//            s = asFutureTime(time - now);
//        if (s == null)
//            s = formatDatetime(time);
//        return s;
    }

    public static String asFutureTime(final long delta) {
//        String unit ;
        long n = delta/ ONE_SECOND_MS;
        if (n < SECONDS_PER_MINUTE) {
            if (n == 0)
                return "Right now";
            return fromNow(n , SECOND);
        }
        n = delta / ONE_MINUTE_MS;
        if (n < MINUTES_PER_HOUR)
            return fromNow(n , MINUTE);
        n = delta / ONE_HOUR_MS;
        if (n < HOURS_PER_DAY)
            return fromNow(n, HOUR);
        n = delta / ONE_DAY_MS;
        if (n < DAYS_PER_WEEK)
            return fromNow(n, DAY);
        n = delta / ONE_WEEK_MS;
        if (n < 52)
            return fromNow(n, WEEK);
        n = (delta / (ONE_WEEK_MS*52));
        return fromNow(n, YEAR);
    }

    public static String asPastTime(final long delta) {
//        long delta = millisFromNow(time);
        long secs = delta / ONE_SECOND_MS;
        if (secs < SECONDS_PER_MINUTE) {
            if (secs == 0)
                return "Just now";
            return ago(secs, SECOND);
        }
        long mins = delta / ONE_MINUTE_MS;
        if (mins < MINUTES_PER_HOUR)
            return ago(mins, MINUTE);
        long hrs = (mins / MINUTES_PER_HOUR);
        if (hrs == 0)
            hrs = 1;
        mins = (mins % MINUTES_PER_HOUR);
        if (hrs < HOURS_PER_DAY) {
            if (mins > 0)
                return ago(hrs, HOUR, false) + " " + ago(mins, MINUTE);
            return ago(hrs, HOUR);
        }
        if (delta < ONE_DAY_MS*2)
            return "yesterday";
        if (delta < ONE_WEEK_MS)
            return ago((delta/ONE_DAY_MS), DAY);
        if (delta < (ONE_DAY_MS*12))
            return "last week";
        if (delta < (ONE_WEEK_MS*4))
            return "a few weeks ago";
        if (delta < (ONE_WEEK_MS*52))
            return ago((delta/ONE_WEEK_MS), WEEK);
        return null;
    }
}
