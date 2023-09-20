package com.legend.base.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private final AppManager appManager;
    private int foregroundActivitiesCount = 0;

    public ActivityLifecycle() {
        appManager = AppManager.getInstance();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        appManager.addActivity(activity);

        if (savedInstanceState != null) {
            AppManager.AppListener appListener = AppManager.getInstance().getAppListener();
            if (appListener != null) {
                appListener.onAppRestore();
            }
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        foregroundActivitiesCount++;
        if (foregroundActivitiesCount == 1 && !activity.isChangingConfigurations()) {
            AppManager.AppListener appListener = AppManager.getInstance().getAppListener();
            if (appListener != null) {
                appListener.onBackgroundToForeground();
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        appManager.setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (appManager.getCurrentActivity() == activity) {
            appManager.setCurrentActivity(null);
        }

        foregroundActivitiesCount--;

        AppManager.AppListener appListener = AppManager.getInstance().getAppListener();
        if (appListener != null && foregroundActivitiesCount == 0) {
            appListener.onForegroundToBackground();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        AppManager.getInstance().removeActivity(activity);
    }
}
