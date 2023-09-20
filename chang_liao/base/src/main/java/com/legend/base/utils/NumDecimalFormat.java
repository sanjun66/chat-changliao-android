package com.legend.base.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by lwj on 2019/6/5.
 * Describe：数值整理工具类
 */
public class NumDecimalFormat {
    /*
     * 显示该话题的独立用户（uv）浏览数：
     * 不满万，5000-10000种随机取数显示；
     * 满万，最小以万为单位，向上取整；
     * 满亿，最小以千万为单位，以*.*亿进行显示，向上取整
     * */
    public static String createPageView(int num) {
        if (num >= 100000000) {
            DecimalFormat df = new DecimalFormat("0.0");
            String s = df.format((float) num / 10000000);
            return Float.parseFloat(s) + "亿";
        }
        if (num >= 10000) {
            return (num / 10000) + "万";
        }
        return String.valueOf(num);
    }

    /**
     * 获取点赞数
     */
    public String getStarCount(int count) {
        String s = getCount(count);
        if (TextUtils.equals(s, "0")) {
            return "";
        } else {
            return s;
        }
    }

    /**
     * 获取距离
     */
    public String getDistance(int distance) {
        if (distance >= 1000000) {
            return (distance / 1000) + "km";
        } else if (distance >= 1000) {
            float v = distance * 1.0f / 1000;
            DecimalFormat format = new DecimalFormat("0.0");
            return format.format(v) + "km";
        } else {
            return distance + "米";
        }
    }

    public String getCount(long count) {
        if (count > 10000) {
            long residue = count % 1000;
            if (residue > 500) {
                count = count - residue + 1000;
            }
        }
        String result;
        try {
            if (count >= 100000000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 100000000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "亿";
            } else if (count >= 10000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 10000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "万";
            } else {
                result = String.valueOf(Math.max(0, count));
            }
        } catch (Exception e) {

            if (count >= 100000000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v = Math.round(count * 1.0f / 100000000);
                result = decimalFormat.format(v) + "亿";
            } else if (count >= 10000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v = Math.round(count * 1.0f / 10000);
                result = decimalFormat.format(v) + "万";
            } else {
                result = String.valueOf(Math.max(0, count));
            }

        }
        return result;
    }

    /*
     * 点赞，收藏，评论
     * 最大值99.9万,超过1w四舍五入保留1位，末尾加万
     * */
    public static String numFormat(int count) {
        String result;
        try {
            if (count >= 100000000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 100000000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "亿";
            } else if (count >= 10000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 10000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "万";
            } else {
                result = String.valueOf(Math.max(0, count));
            }
        } catch (Exception e) {

            if (count >= 100000000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v =Math.round( count * 1.0f / 100000000);
                result = decimalFormat.format(v) + "亿";
            } else if (count >= 10000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v = Math.round(count * 1.0f / 10000);

                result = decimalFormat.format(v) + "万";
            } else {
                result = String.valueOf(Math.max(0, count));
            }

        }
        return result;
    }

    /*
     * 点赞，收藏，评论
     * 最大值99.9万,超过1w四舍五入保留1位，末尾加小写w
     * */
    public static String numFormatV2(int count) {
        String result;
        try {
            if (count >= 100000000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 100000000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "e";
            } else if (count >= 10000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 10000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "w";
            } else {
                result = String.valueOf(Math.max(0, count));
            }
        } catch (Exception e) {

            if (count >= 100000000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v =Math.round( count * 1.0f / 100000000);
                result = decimalFormat.format(v) + "e";
            } else if (count >= 10000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v = Math.round(count * 1.0f / 10000);

                result = decimalFormat.format(v) + "w";
            } else {
                result = String.valueOf(Math.max(0, count));
            }

        }
        return result;
    }


    public String getCountV2(long count) {
        if (count > 10000) {
            long residue = count % 1000;
            if (residue > 500) {
                count = count - residue + 1000;
            }
        }
        String result;
        try {
            if (count >= 100000000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 100000000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "e";
            } else if (count >= 10000) {
                BigDecimal bigDecimal = new BigDecimal(count * 1.0f / 10000);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                String f = String.valueOf(bigDecimal);
                String[] split = f.split("\\.");
                result = split[0] + "." + split[1].substring(0, 1) + "w";
            } else {
                result = String.valueOf(Math.max(0, count));
            }
        } catch (Exception e) {

            if (count >= 100000000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v = Math.round(count * 1.0f / 100000000);
                result = decimalFormat.format(v) + "e";
            } else if (count >= 10000) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                float v = Math.round(count * 1.0f / 10000);
                result = decimalFormat.format(v) + "w";
            } else {
                result = String.valueOf(Math.max(0, count));
            }

        }
        return result;
    }

}
