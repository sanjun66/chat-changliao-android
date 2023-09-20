package com.legend.base.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * 创建：WithWings 时间 2019/5/21
 * Email:wangtong1175@sina.com
 */
public class DateUtils {

    @SuppressLint("SimpleDateFormat")
    public static boolean isSameDay(Date date, Date sameDate) {
        if (null == date || null == sameDate) {
            return false;
        }
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(sameDate);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        if (nowCalendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) && nowCalendar.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH) && nowCalendar.get(Calendar.DATE) == dateCalendar.get(Calendar.DATE)) {
            return true;
        }
        return false;
    }

    public static boolean isSameDay(long currentTimeMillis, long sameTimeMillis) {
        return isSameDay(new Date(currentTimeMillis), new Date(sameTimeMillis));
    }

    /**
     * 13位时间戳 转为 月-日
     * @param msTime timestamp
     */
    public static String getCommentDate(long msTime) {
        long curTime = System.currentTimeMillis() / (long) 1000;
        long timeStamp = msTime / 1000;
        long dTime = curTime - timeStamp;

        // 当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msTime);
        Date time = calendar.getTime();

        int year = calendar.get(Calendar.YEAR);

        Calendar current = Calendar.getInstance();
        current.setTime(new Date());
        int currentYear = current.get(Calendar.YEAR);

        // 60;
        int DIFF_TIME_60S = 60;
        // 3600 * 24;
        int DIFF_TIME_1DAY = 86400;
        // 3600 * 24 * 2;
        int DIFF_TIME_2DAY = 172800;
        // 3600 * 24 * 30 * 12;
        int DIFF_TIME_1YEAR = 31104000;

        if (dTime < DIFF_TIME_60S && dTime >= 0) {
            // <=60s
            return "刚刚";
        } else {
            // 当天的23:59:59
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            long today24 = c.getTimeInMillis() / 1000;
            long dayDiffTime = today24 - timeStamp;
            if (dayDiffTime < DIFF_TIME_1DAY) {
                // 今天
                SimpleDateFormat hmFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return "今天 " + hmFormat.format(time);
            } else if (dayDiffTime < DIFF_TIME_2DAY) {
                // 昨天
                SimpleDateFormat hmFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return "昨天 " + hmFormat.format(time);
            } else if (dayDiffTime < DIFF_TIME_1YEAR) {
                // 月日
                if(currentYear == year) {
                    SimpleDateFormat mdFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
                    return mdFormat.format(time);
                }else{
                    // 年月日
                    SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    return ymdFormat.format(time);
                }
            } else if (dTime >= DIFF_TIME_1YEAR) {
                // 年月日
                SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return ymdFormat.format(time);
            } else {
                return "刚刚";
            }
        }
    }

    /**
     * 13位时间戳 转为 月-日
     * @param msTime timestamp
     */
    public static String getCommuityDate(long msTime) {
        long curTime = System.currentTimeMillis() / (long) 1000;
        long timeStamp = msTime / 1000;
        long dTime = curTime - timeStamp;

        // 当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msTime);
        Date time = calendar.getTime();

        int year = calendar.get(Calendar.YEAR);

        Calendar current = Calendar.getInstance();
        current.setTime(new Date());
        int currentYear = current.get(Calendar.YEAR);

        // 60;
        int DIFF_TIME_60S = 60;
        int DIFF_TIME_1HOUR=  DIFF_TIME_60S * 60;
        // 3600 * 24;
        int DIFF_TIME_1DAY = 86400;
        // 3600 * 24 * 2;
        int DIFF_TIME_2DAY = 172800;
        // 3600 * 24 * 30 * 12;
        int DIFF_TIME_1YEAR = 31104000;

        if (dTime < DIFF_TIME_60S && dTime >= 0) {
            // <=60s
            return "刚刚";
        } else {
            // 当天的23:59:59
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            long today24 = c.getTimeInMillis() / 1000;
            long dayDiffTime = today24 - timeStamp;
            if (dTime < DIFF_TIME_1HOUR) {
                //一小时内
                int selectMin = (int) Math.ceil(dTime / DIFF_TIME_60S);
                return  selectMin + "分钟前";
            } else if (dayDiffTime < DIFF_TIME_1DAY) {
                // 今天
                int selectHour = (int) Math.ceil(dTime / DIFF_TIME_1HOUR);
                return selectHour+ "小时前";
            } else if (dayDiffTime < DIFF_TIME_1YEAR) {
                // 月日
                if(currentYear == year) {
                    SimpleDateFormat mdFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                    return mdFormat.format(time);
                }else{
                    // 年月日
                    SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    return ymdFormat.format(time);
                }
            } else if (dTime >= DIFF_TIME_1YEAR) {
                // 年月日
                SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return ymdFormat.format(time);
            } else {
                return "刚刚";
            }
        }
    }
    /**
     * 返回00:天00时00分00秒
     * @param selectTime
     * @return
     */
    public static String getSeckillDate(long selectTime) {
        String res;
        long sec = 1000;
        long min = 60 * sec;
        long hh = 60 * min;
        long dd = hh * 24;
        int selectDays = (int) Math.ceil(selectTime / dd);
        int selectHour = (int) Math.ceil((selectTime - selectDays * dd) / hh);
        int selectMinutes = (int) Math.ceil((selectTime - selectDays * dd - selectHour * hh) / min);
        int selectSec = (int) Math.ceil((selectTime - selectDays * dd - selectHour * hh - selectMinutes * min) / sec);
        if (selectDays <= 9) {
            res = "0" + selectDays + "天";
        } else {
            res = selectDays + "天";
        }
        if (selectHour <= 9) {
            res += "0" + selectHour + "时";
        } else {
            res += selectHour + "时";
        }
        if (selectMinutes <= 9) {
            res += "0" + selectMinutes + "分";
        } else {
            res += selectMinutes + "分";
        }
        if (selectSec <= 9) {
            res += "0" + selectSec + "秒";
        } else {
            res += selectSec + "秒";
        }
        return res;
    }

    /**
     * 返回00-    00:00:00:00:
     * @param selectTime
     * @return
     */
    public static String getBiddingDate(long selectTime) {
        String res;
        long sec = 1000;
        long min = 60 * sec;
        long hh = 60 * min;
        long dd = hh * 24;
        int selectDays = (int) Math.ceil(selectTime / dd);
        int selectHour = (int) Math.ceil((selectTime - selectDays * dd) / hh);
        int selectMinutes = (int) Math.ceil((selectTime - selectDays * dd - selectHour * hh) / min);
        int selectSec = (int) Math.ceil((selectTime - selectDays * dd - selectHour * hh - selectMinutes * min) / sec);
        if (selectDays <= 9) {
            res = "0" + selectDays + ":";
        } else {
            res = selectDays + ":";
        }
        if (selectHour <= 9) {
            res += "0" + selectHour + ":";
        } else {
            res += selectHour + ":";
        }
        if (selectMinutes <= 9) {
            res += "0" + selectMinutes + ":";
        } else {
            res += selectMinutes + ":";
        }
        if (selectSec <= 9) {
            res += "0" + selectSec;
        } else {
            res += selectSec;
        }
        return res;
    }
    /**
     * 返回00-  yyyy-MM-dd HH:mm:ss
     * @param msTime
     * @return
     */
    public static String getFullData(long msTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msTime);
        Date time = calendar.getTime();
        SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return ymdFormat.format(time);
    }


    public static String getMonthDay(long msTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msTime);
        Date time = calendar.getTime();
        SimpleDateFormat mdFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        return mdFormat.format(time);
    }

    /**
     * 13位时间戳 转为 月-日
     * 1分钟内=刚刚，1-59分钟=xx分钟前，
     * 1小时-24小时=xx小时前，
     * 1天-7天=xx天前，
     * 7天以上=显示日期：08-09，
     * 一年以上：21-08-09
     * @param msTime timestamp
     */
    public static String getHbCommentDate(long msTime) {
        long curTime = System.currentTimeMillis() / (long) 1000;
        long timeStamp = msTime / 1000;
        long dTime = curTime - timeStamp;

        // 当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msTime);
        Date time = calendar.getTime();

        int year = calendar.get(Calendar.YEAR);

        Calendar current = Calendar.getInstance();
        current.setTime(new Date());
        int currentYear = current.get(Calendar.YEAR);

        // 60;
        int DIFF_TIME_60S = 60;
        int DIFF_TIME_1HOUR=  DIFF_TIME_60S * 60;
        // 3600 * 24;
        int DIFF_TIME_1DAY = 86400;
        // 3600 * 24 * 7;
        int DIFF_TIME_7DAY = 604800;
        // 3600 * 24 * 30 * 12;
        int DIFF_TIME_1YEAR = 31104000;

        if (dTime < DIFF_TIME_60S) {
            // <=60s
            return "刚刚";
        } else {
            // 当天的23:59:59
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            long today24 = c.getTimeInMillis() / 1000;
            long dayDiffTime = curTime - timeStamp;
            if (dTime < DIFF_TIME_1HOUR) {
                //一小时内
                int selectMin = (int) Math.ceil(dTime / DIFF_TIME_60S);
                return  selectMin + "分钟前";
            } else if (dayDiffTime < DIFF_TIME_1DAY) {
                // 今天
                int selectHour = (int) Math.ceil(dTime / DIFF_TIME_1HOUR);
                return selectHour+ "小时前";
            } else if (dayDiffTime < DIFF_TIME_7DAY) {
                // 今天
                int selectHour = (int) Math.ceil(dTime / DIFF_TIME_1DAY);
                return selectHour+ "天前";
            } else if (currentYear != year) {
                // 不是同一年的，显示年月日
                SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return ymdFormat.format(time);
            } else if (dTime >= DIFF_TIME_1YEAR) {
                // 年月日
                SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return ymdFormat.format(time);
            } else if (dayDiffTime < DIFF_TIME_1YEAR) {
                // 月日
                SimpleDateFormat mdFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
                return mdFormat.format(time);
            }  else {
                return "刚刚";
            }
        }
    }

    /**
     * 13位时间戳 转为 月-日
     * 1分钟内=刚刚，1-59分钟=xx分钟前，
     * 1小时-24小时=xx小时前，
     * 7天以上=显示日期：08-09，
     * 一年以上：21-08-09
     * @param msTime timestamp
     */
    public static String getHbCommentDateV2(long msTime) {
        long curTime = System.currentTimeMillis() / (long) 1000;
        long timeStamp = msTime / 1000;
        long dTime = curTime - timeStamp;

        // 当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(msTime);
        Date time = calendar.getTime();

        int year = calendar.get(Calendar.YEAR);

        Calendar current = Calendar.getInstance();
        current.setTime(new Date());
        int currentYear = current.get(Calendar.YEAR);

        // 60;
        int DIFF_TIME_60S = 60;
        int DIFF_TIME_1HOUR=  DIFF_TIME_60S * 60;
        // 3600 * 24;
        int DIFF_TIME_1DAY = 86400;
        // 3600 * 24 * 7;
        int DIFF_TIME_7DAY = 604800;
        // 3600 * 24 * 30 * 12;
        int DIFF_TIME_1YEAR = 31104000;

        if (dTime < DIFF_TIME_60S) {
            // <=60s
            return "刚刚";
        } else {
            // 当天的23:59:59
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            long today24 = c.getTimeInMillis() / 1000;
            long dayDiffTime = curTime - timeStamp;
            if (dTime < DIFF_TIME_1HOUR) {
                //一小时内
                int selectMin = (int) Math.ceil(dTime / DIFF_TIME_60S);
                return  selectMin + "分钟前";
            } else if (dayDiffTime < DIFF_TIME_1DAY) {
                // 今天
                int selectHour = (int) Math.ceil(dTime / DIFF_TIME_1HOUR);
                return selectHour + "小时前";
            } else if (dTime >= DIFF_TIME_1YEAR) {
                // 年月日
                SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return ymdFormat.format(time);
            } else if (dayDiffTime < DIFF_TIME_1YEAR) {
                // 月日
                SimpleDateFormat mdFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
                return mdFormat.format(time);
            } else {
                return "刚刚";
            }
        }
    }
}
