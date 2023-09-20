package com.legend.baseui.ui.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class DensityUtil {
    private static WindowManager mWindowManager;
    private static int screenWidth;
    private static int screenHeight;

    public static int dip2px(Context context, float dpValue) {
        if (context == null) {
            return 0;
        } else {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int)(dpValue * scale + 0.5F);
        }
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5F);
    }

    public static int sp2px(Context context, float spValue) {
        return (int)(TypedValue.applyDimension(2, spValue, context.getResources().getDisplayMetrics()) + 0.5F);
    }

    public static int getScreenWidth(Context context) {
        if(context == null) return 0;
        if (screenWidth == 0) {
            if (mWindowManager == null) {
                mWindowManager = (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE); ;
            }
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }

        return screenWidth;
    }

    public static int getScreenHeight(Context context) {
        if(context == null) return 0;
        if (screenHeight == 0) {
            if (mWindowManager == null) {
                mWindowManager = (WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE); ;
            }
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }

        return screenHeight;
    }

    public static int[] getRealW_H(Context context, int width, int height) {
        int deviceWidth = getScreenWidth(context);
        int realWidth = width * deviceWidth / 720;
        int realHeight = height * realWidth / width;

        return new int[]{realWidth, realHeight};
    }

    public static int[] getRealW_H2(Context context, int width, int height) {
        int deviceWidth = getScreenWidth(context);
        int realWidth = width * deviceWidth / 750;
        int realHeight = height * realWidth / width;

        return new int[]{realWidth, realHeight};
    }

    public static int[] getRealW_H1(Context context, int width, int height, int padding) {
        int deviceWidth = getScreenWidth(context) - padding;
        int realWidth = width * deviceWidth / 750;
        int realHeight = height * realWidth / width;

        return new int[]{realWidth, realHeight};
    }
}
