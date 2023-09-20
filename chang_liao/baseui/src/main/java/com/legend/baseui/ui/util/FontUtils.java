package com.legend.baseui.ui.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import com.com.legend.ui.R;

public class FontUtils {
    public static final String SUPPORT_CHAR = "₀₁₂₃₄₅₆₇₈₉⁰¹²³⁴⁵⁶⁷⁸⁹½↉⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅐⅛⅜⅝⅞⅑⅒0123456789.,$¥:;…!/\\_⁄+−%‰|＄￥- ";

    /**
     * 设置Light字体, 不做参数校验
     * @param textView
     * @param context
     */
    public static void setTypefaceLight(TextView textView, Context context) {
        if (null == textView || null == context)
            return;
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.ui_xlfq_light));
    }

    /**
     * 设置Medium字体, 不做参数校验
     * @param textView
     * @param context
     */
    public static void setTypefaceMedium(TextView textView, Context context) {
        if (null == textView || null == context)
            return;
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.ui_xlfq_medium));
    }


    /**
     * 设置Regular字体, 不做参数校验
     * @param textView
     * @param context
     */
    public static void setTypefaceRegular(TextView textView, Context context) {
        if (null == textView || null == context)
            return;
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.ui_xlfq_regular));
    }


    /**
     * 设置Light字体, 如果字符中包含不支持的字符则恢复系统默认字体
     * @param textView
     * @param context
     */
    public static void checkSupportLightSetTypeface(TextView textView, Context context) {
        if (null == textView || null == context)
            return;
        if (supportTypefaceLight(textView.getText().toString()))
            textView.setTypeface(ResourcesCompat.getFont(context, R.font.ui_xlfq_light));
        else
            resetTypeface(textView);
    }

    /**
     * 设置Medium字体, 如果字符中包含不支持的字符则恢复系统默认字体
     * @param textView
     * @param context
     */
    public static void checkSupportMediumSetTypeface(TextView textView, Context context) {
        if (null == textView || null == context)
            return;
        if (supportTypefaceMedium(textView.getText().toString()))
            textView.setTypeface(ResourcesCompat.getFont(context, R.font.ui_xlfq_medium));
        else
            resetTypeface(textView);
    }

    /**
     * 设置Regular字体, 如果字符中包含不支持的字符则恢复系统默认字体
     * @param textView
     * @param context
     */
    public static void checkSupportRegularSetTypeface(TextView textView, Context context) {
        if (null == textView || null == context)
            return;
        if (supportTypefaceRegular(textView.getText().toString()))
            textView.setTypeface(ResourcesCompat.getFont(context, R.font.ui_xlfq_regular));
        else
            resetTypeface(textView);
    }


    /**
     * 恢复系统默认字体
     * @param textView
     */
    public static void resetTypeface(TextView textView) {
        if (null == textView)
            return;
        textView.setTypeface(null);
    }

    /**
     * 检查Light字体是否支持该字符
     * @param text
     * @return
     */
    public static boolean supportTypefaceLight(String text) {
        return containsOnly(text, SUPPORT_CHAR);
    }

    /**
     * 检查Medium字体是否支持该字符
     * @param text
     * @return
     */
    public static boolean supportTypefaceMedium(String text) {
        return containsOnly(text, SUPPORT_CHAR);
    }

    /**
     * 检查Regular字体是否支持该字符
     * @param text
     * @return
     */
    public static boolean supportTypefaceRegular(String text) {
        return containsOnly(text, SUPPORT_CHAR);
    }


    /**
     * 字符串A是否只包含字符串B
     * @param str
     * @param validChars
     * @return
     */
    private static boolean containsOnly(String str, String validChars) {
        if (str == null || validChars == null) {
            return false;
        }
        return containsOnly(str, validChars.toCharArray());
    }


    private static boolean containsOnly(String str, char[] valid) {
        if ((valid == null) || (str == null)) {
            return false;
        }
        if (str.length() == 0) {
            return true;
        }
        if (valid.length == 0) {
            return false;
        }
        return indexOfAnyBut(str, valid) == -1;
    }


    private static int indexOfAnyBut(String str, char[] searchChars) {
        if (TextUtils.isEmpty(str) || null == searchChars || searchChars.length <= 0) {
            return -1;
        }
        int csLen = str.length();
        int csLast = csLen - 1;
        int searchLen = searchChars.length;
        int searchLast = searchLen - 1;
        outer:
        for (int i = 0; i < csLen; i++) {
            char ch = str.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (i < csLast && j < searchLast && Character.isHighSurrogate(ch)) {
                        if (searchChars[j + 1] == str.charAt(i + 1)) {
                            continue outer;
                        }
                    } else {
                        continue outer;
                    }
                }
            }
            return i;
        }
        return -1;
    }
}
