package com.legend.baseui.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NotchScreenUtil {

    private static final String TAG = "NotchScreenUtil";
    
    /**
     * 华为start
     */
    // 判断是否是华为刘海屏
    public static boolean hasNotchInScreenAtHuawei(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (Boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return ret;
    }

    /**
     * 获取华为刘海的高
     * @param context
     * @return
     */
    public static int getNotchSizeAtHuawei(Context context) {
        int[] ret = new int[] { 0, 0 };
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
            
        } catch (ClassNotFoundException e) {
            return 0;
        } catch (NoSuchMethodException e) {
            return 0;
        } catch (Exception e) {
            return 0;
        }
        return ret[1];
    }

    /**
     * 华为end
     */

    /**
     * Oppo start
     */
    public static boolean hasNotchInScreenAtOppo(Context context) {
        boolean hasNotch = context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        return hasNotch;
    }

    public static int getNotchSizeAtOppo() {
        return 80;
    }

    /**
     * Oppo end
     */

    /**
     * vivo start
     */
    public static final int NOTCH_IN_SCREEN_VOIO = 0x00000020;// 是否有凹槽
    public static final int ROUNDED_IN_SCREEN_VOIO = 0x00000008;// 是否有圆角

    public static boolean hasNotchInScreenAtVivo(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> FtFeature = cl.loadClass("com.util.FtFeature");
            Method get = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (Boolean) get.invoke(FtFeature, NOTCH_IN_SCREEN_VOIO);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return ret;
    }
    
    public static int getNotchSizeAtVivo(Context context){
        return DensityUtil.dip2px(context, 32);
    }

    /**
     * vivo end
     */

    

    
    /** 
     * 获取手机厂商 
     * 
     * @return  手机厂商 
     */  
    
    public final static int DEVICE_BRAND_OPPO = 0x0001;
    public final static int DEVICE_BRAND_HUAWEI = 0x0002;
    public final static int DEVICE_BRAND_VIVO = 0x0003;
    
    
    @SuppressLint("DefaultLocale")
    public static int getDeviceBrand() {
        String brand = Build.BRAND.trim().toUpperCase();
        if (brand.contains("HUAWEI")) {
            return DEVICE_BRAND_HUAWEI;
        }else if (brand.contains("OPPO")) {
            return DEVICE_BRAND_OPPO;
        }else if (brand.contains("VIVO")) {
            return DEVICE_BRAND_VIVO;
        }
        return 0;
    }

    /**
     *  设置应用窗口在华为notch手机使用刘海区的flag值, 该值为华为官方提供, 不要修改
     */
    private static final int FLAG_NOTCH_SUPPORT_HW = 0x00010000;

    /**
     * vivo手机判断是否是notch, vivo官方提供, 不要修改
     */
    private static final int FLAG_NOTCH_SUPPORT_VIVO = 0x00000020;


    public static boolean checkNotchScreen(Context context) {
        if (checkHuaWei(context)) {
            return true;
        } else if (checkVivo(context)) {
            return true;
        } else if (checkMiUI(context)) {
            return true;
        } else if (checkOppo(context)) {
            return true;
        }

        return false;
    }

    /**
     * oppo提供: 刘海屏判断.
     * @return true, 刘海屏; false: 非刘海屏
     */
    private static boolean checkOppo(Context context) {
        try {
            return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        } catch (Exception e) {
            Log.e(TAG, "checkOppo notchScreen exception");
        }
        return false;
    }

    /**
     * 小米提供: 刘海屏判断.
     * @return true, 刘海屏; false: 非刘海屏
     */
    private static boolean checkMiUI(Context context) {

        int result = 0;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressLint("PrivateApi")
            @SuppressWarnings("rawtypes")
            Class systemProperties = classLoader.loadClass("android.os.SystemProperties");
            //参数类型
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = systemProperties.getMethod("getInt", paramTypes);
            //参数
            Object[] params = new Object[2];
            params[0] = "ro.miui.notch";
            params[1] = 0;
            result = (Integer)getInt.invoke(systemProperties, params);
            return result == 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 华为提供: 判断是否是刘海屏
     *
     * @param context Context
     * @return true：刘海屏；false：非刘海屏
     */
    private static boolean checkHuaWei(Context context) {

        boolean ret = false;

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwNotchSizeUtil.getMethod("hasNotchInScreen");

            ret = (boolean)get.invoke(hwNotchSizeUtil);

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            Log.e(TAG, "hasNotchInScreen Exception");

        }
        return ret;
    }

    /**
     * vivo提供: 判断是否是刘海屏
     *
     * @param context Context
     * @return true：是刘海屏；false：非刘海屏
     */
    private static boolean checkVivo(Context context) {

        boolean ret;
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressLint("PrivateApi")
            Class ftFeature = cl.loadClass("android.util.FtFeature");
            Method isFeatureSupport = ftFeature.getMethod("isFeatureSupport");
            ret = (boolean)isFeatureSupport.invoke(ftFeature, FLAG_NOTCH_SUPPORT_VIVO);
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    /**
     * 华为提供: 获取刘海尺寸
     *
     * @param context Context
     * @return int[0]值为刘海宽度 int[1]值为刘海高度。
     */
    public static int[] getNotchSize(Context context) {

        int[] ret = new int[] {0, 0};

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwnotchsizeutil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwnotchsizeutil.getMethod("getNotchSize");

            ret = (int[])get.invoke(hwnotchsizeutil);

        } catch (ClassNotFoundException e) {

            Log.e("test", "getNotchSize ClassNotFoundException");

        } catch (NoSuchMethodException e) {

            Log.e("test", "getNotchSize NoSuchMethodException");

        } catch (Exception e) {

            Log.e("test", "getNotchSize Exception");

        }
        return ret;
    }

    /**
     * 华为提供: 设置应用窗口在华为刘海屏手机使用刘海区
     * @param window 应用页面window对象
     */
    public static void setFullScreenWindowLayoutInDisplayCutout(Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod("addHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT_HW);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (InstantiationException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "hw add notch screen flag api error");
        } catch (Exception e) {
            Log.e(TAG, "other Exception");
        }
    }
    /**
     * 华为提供: 设置应用窗口在华为刘海屏手机不使用刘海区
     *
     * @param window 应用页面window对象
     */
    public static void setNotFullScreenWindowLayoutInDisplayCutout(Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod("clearHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT_HW);
            Log.e(TAG, "............clear");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (InstantiationException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "hw clear notch screen flag api error");
        } catch (Exception e) {
            Log.e(TAG, "other Exception");
        }
    }

    /**
     * 获取传音CF8手机刘海和虚拟按键的总高度
     *
     * @return px
     */
    public static int getTECNOCF8NotchAndNaviHeight() {

        int height = 0;
        if (Build.MODEL.toUpperCase().contains("TECNO CF8")) {
            //传音技术反馈这款手机的刘海是自定义的，不能通过反射得到高度
            //只能通过界面自己计算出来刘海和虚拟底部的总高度
            height = 72;
        }
        return height;
    }
}