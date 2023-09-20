package com.legend.base.utils.permission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.legend.base.utils.permission.listener.PermissionListener;
import com.legend.base.utils.EmptyUtils;

public class Permission {
    private static final String TAG = Permission.class.getSimpleName();

    /**
     * 权限申请, 如果需要申请权限会回调PermissionListener.preRequest函数, 调用层可做提示或其他处理, 返回值为true时中断权限申请流程, false继续申请
     * 再次申请如不需要preRequest可调用requestNow
     */
    public static void request(String permission, FragmentActivity activity, PermissionListener listener) {
        if (TextUtils.isEmpty(permission)) {
            Log.e(TAG, "permission is empty");
            return;
        }
        request(new String[]{permission}, activity, listener);
    }

    public static void request(String[] permissions, FragmentActivity activity, PermissionListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (null != listener)
                listener.granted(permissions);
            return;
        }
        if (EmptyUtils.isEmpty(permissions)) {
            Log.e(TAG, "permission is empty");
            return;
        }
        if (null == activity) {
            Log.e(TAG, "activity is null");
            return;
        }
        if (checkPermission(permissions, activity)) {
            listener.granted(permissions);
            return;
        }

        if (null != listener && listener.preRequest(permissions)) return;

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(PermissionFragment.TAG);
        if (null == fragment) {
            fragment = PermissionFragment.newInstance(permissions);
            ((PermissionFragment) fragment).setListener(listener);
            fragmentManager.beginTransaction().add(fragment, PermissionFragment.TAG).commitAllowingStateLoss();
        }
    }

    /**
     * 权限申请, 无预申请的场景
     */
    public static void requestNow(String permission, FragmentActivity activity, PermissionListener listener) {
        if (TextUtils.isEmpty(permission)) {
            Log.e(TAG, "permission is empty");
            return;
        }
        requestNow(new String[]{permission}, activity, listener);
    }

    public static void requestNow(String[] permissions, FragmentActivity activity, PermissionListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (null != listener)
                listener.granted(permissions);
            return;
        }

        if (EmptyUtils.isEmpty(permissions)) {
            Log.e(TAG, "permission is empty");
            return;
        }
        if (null == activity) {
            Log.e(TAG, "activity is empty");
            return;
        }
        if (checkPermission(permissions, activity)) {
            listener.granted(permissions);
            return;
        }
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(PermissionFragment.TAG);
        if (null == fragment) {
            fragment = PermissionFragment.newInstance(permissions);
            ((PermissionFragment) fragment).setListener(listener);
            fragmentManager.beginTransaction().add(fragment, PermissionFragment.TAG).commitAllowingStateLoss();
        }

    }

    public static boolean checkPermission(String[] permissions, Context context) {
        for (String permission : permissions) {
            if (!checkPermission(permission, context))
                return false;
        }
        return true;
    }

    public static boolean checkPermission(String permission, Context context) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
