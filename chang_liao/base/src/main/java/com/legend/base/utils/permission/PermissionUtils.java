package com.legend.base.utils.permission;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.com.legend.base.R;
import com.legend.base.Applications;
import com.legend.base.utils.EmptyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PermissionUtils {
    private final static HashMap<String, String> sPermissionName = new HashMap<String, String>() {
        {
            put(Manifest.permission.REQUEST_INSTALL_PACKAGES, "安装应用");
            put(Manifest.permission.SYSTEM_ALERT_WINDOW, "悬浮窗");
            put(Manifest.permission.READ_CALENDAR, "日历");
            put(Manifest.permission.WRITE_CALENDAR, "日历");
            put(Manifest.permission.CAMERA, Applications.getCurrent().getString(R.string.permission_camera));
            put(Manifest.permission.READ_CONTACTS, "读取联系人");
            put(Manifest.permission.WRITE_CONTACTS, "修改联系人");
            put(Manifest.permission.GET_ACCOUNTS, "访问手机账户");
            put(Manifest.permission.ACCESS_FINE_LOCATION, "定位");
            put(Manifest.permission.REQUEST_INSTALL_PACKAGES, "安装应用");
            put(Manifest.permission.ACCESS_COARSE_LOCATION, Applications.getCurrent().getString(R.string.permission_location));
            put(Manifest.permission.RECORD_AUDIO, Applications.getCurrent().getString(R.string.permission_recording));
            put(Manifest.permission.READ_PHONE_STATE, "获取手机信息");
            put(Manifest.permission.CALL_PHONE, "拨打电话");
            put(Manifest.permission.READ_CALL_LOG, "读取通话记录");
            put(Manifest.permission.WRITE_CALL_LOG, "修改通话记录");
            put(Manifest.permission.ADD_VOICEMAIL, "添加语音邮件");
            put(Manifest.permission.USE_SIP, "使用SIP视频");
            put(Manifest.permission.PROCESS_OUTGOING_CALLS, "处理拨出电话");
            put(Manifest.permission.READ_PHONE_NUMBERS, "读取手机号码");
            put(Manifest.permission.BODY_SENSORS, "传感器");
            put(Manifest.permission.SEND_SMS, "发送短信");
            put(Manifest.permission.RECEIVE_SMS, "接收短信");
            put(Manifest.permission.READ_SMS, "读取短信");
            put(Manifest.permission.RECEIVE_WAP_PUSH, "接收WAP PUSH信息");
            put(Manifest.permission.RECEIVE_MMS, "接收彩信");
            put(Manifest.permission.READ_EXTERNAL_STORAGE, Applications.getCurrent().getString(R.string.permission_storage));
            put(Manifest.permission.WRITE_EXTERNAL_STORAGE, Applications.getCurrent().getString(R.string.permission_storage));
        }
    };

    public static final class Group {
        // 日历
        public static final String[] CALENDAR = new String[]{
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR};

        // 联系人
        public static final String[] CONTACTS = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.GET_ACCOUNTS};

        // 位置
        public static final String[] LOCATION = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        // 存储
        public static final String[] STORAGE = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    public static String getPermissionName(String permission) {
        if (TextUtils.isEmpty(permission))
            return "";
        String s = sPermissionName.get(permission);
        return TextUtils.isEmpty(s) ? "Na" : s;
    }

    public static String getPermissionName(String[] permissions) {
        if (EmptyUtils.isEmpty(permissions))
            return "";
        StringBuffer sb = new StringBuffer();
        String s = null;
        Map<String, Boolean> tempMap = new HashMap<>();
        for (String permission : permissions) {
            s = sPermissionName.get(permission);
            if (tempMap.containsKey(s))
                continue;
            tempMap.put(s, true);
            if (TextUtils.isEmpty(s))
                s = "Na";
            sb.append(s).append("、");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }


    /**
     * 跳转当前应用设置页
     *
     * @param activity
     */
    public static void gotoAppDetailSetting(Activity activity) {
        if (null == activity)
            throw new IllegalArgumentException("activity is null");

        try {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
            activity.startActivity(intent);
        } catch (Exception exp) {
        }
    }

    /**
     * 在请求权限之前是否应显示带有基本原理的 UI
     * @param permission
     * @param activity
     * @return
     */
    public static boolean shouldShowRequestPermissionDlg(String permission, Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * 判断是否拥有悬浮窗权限
     */
    public static boolean checkFloatPermission(Context context) {
        boolean result = true;
        boolean booleanValue;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                booleanValue = (Boolean) Settings.class.getDeclaredMethod("canDrawOverlays", Context.class).invoke(null, new Object[] {context});
                return booleanValue;
            } catch (Exception e) {
                return true;
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return true;
        } else if (Build.BRAND.toLowerCase().contains("xiaomi")) {
            Method method;
            Object systemService = context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                method = Class.forName("android.app.AppOpsManager").getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
            } catch (NoSuchMethodException e) {
                method = null;
            } catch (ClassNotFoundException e) {
                method = null;
            }
            if (method != null) {
                try {
                    Integer tmp = (Integer) method.invoke(systemService, new Object[] {24, context.getApplicationInfo().uid, context.getPackageName()});
                    result = tmp != null && tmp == 0;
                } catch (Exception e) {
                }
            }
            return result;
        }
        return true;
    }

    //权限打开
    public static void requestSettingCanDrawOverlays(Activity activity, int requestCode, String packageName) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            activity.startActivityForResult(intent, requestCode);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + packageName));
            activity.startActivityForResult(intent, requestCode);
        } else {//4.4-6.0以下
            //无需处理了
        }
    }

}
