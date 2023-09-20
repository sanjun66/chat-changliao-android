package com.legend.base.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.legend.base.Applications;

public class ProcessUtils {
    public static String getCurrentProcessName() {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) Applications.getCurrent()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static boolean isMainProcess(String processName) {
        if (TextUtils.isEmpty(processName))
            return false;
        return processName.equals(Applications.getCurrent().getPackageName());
    }
}
