package com.example.midtermnotepad;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间戳的两种读法：列表里用「相对时间」（刚刚 / 3 分钟前 / 昨天 21:40），
 * 编辑器里用「绝对时间」（2026-11-19 21:40）。
 */
public final class TimeUtil {

    private static final SimpleDateFormat TIME_HM =
            new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final SimpleDateFormat MONTH_DAY =
            new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat FULL =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    private TimeUtil() {
    }

    /** 列表用的相对时间，同一天显示「今天/昨天 + 时刻」，更早显示月日。 */
    public static String relative(long millis) {
        long now = System.currentTimeMillis();
        long diff = now - millis;
        if (diff < 0) {
            return FULL.format(new Date(millis));
        }
        if (diff < 60_000L) {
            return "刚刚";
        }
        if (diff < 3_600_000L) {
            return (diff / 60_000L) + " 分钟前";
        }

        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(millis);
        boolean sameDay = today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);

        if (sameDay) {
            if (diff < 6 * 3_600_000L) {
                return (diff / 3_600_000L) + " 小时前 · " + TIME_HM.format(new Date(millis));
            }
            return "今天 " + TIME_HM.format(new Date(millis));
        }

        today.add(Calendar.DAY_OF_YEAR, -1);
        boolean yesterday = today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
        if (yesterday) {
            return "昨天 " + TIME_HM.format(new Date(millis));
        }

        Calendar weekBefore = Calendar.getInstance();
        weekBefore.add(Calendar.DAY_OF_YEAR, -7);
        if (millis > weekBefore.getTimeInMillis()) {
            return weekName(target.get(Calendar.DAY_OF_WEEK)) + " " + TIME_HM.format(new Date(millis));
        }
        return MONTH_DAY.format(new Date(millis));
    }

    /** 编辑器用的绝对时间。 */
    public static String absolute(long millis) {
        return FULL.format(new Date(millis));
    }

    private static String weekName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "周一";
            case Calendar.TUESDAY:
                return "周二";
            case Calendar.WEDNESDAY:
                return "周三";
            case Calendar.THURSDAY:
                return "周四";
            case Calendar.FRIDAY:
                return "周五";
            case Calendar.SATURDAY:
                return "周六";
            default:
                return "周日";
        }
    }
}
