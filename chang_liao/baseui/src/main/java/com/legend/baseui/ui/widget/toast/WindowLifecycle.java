package com.legend.baseui.ui.widget.toast;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

class WindowLifecycle implements Application.ActivityLifecycleCallbacks {

    private Activity mActivity;

    private IToast iToast;

    WindowLifecycle(Activity activity) {
        mActivity = activity;
    }

    Activity getActivity() {
        return mActivity;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {}


    @Override
    public void onActivityPaused(Activity activity) {
        if (mActivity != activity) {
            return;
        }

        if (iToast == null) {
            return;
        }

        iToast.cancel();
    }

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (mActivity != activity) {
            return;
        }

        if (iToast != null) {
            iToast.cancel();
        }

        unregister();
        mActivity = null;
    }

    void register(IToast impl) {
        iToast = impl;
        if (mActivity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mActivity.registerActivityLifecycleCallbacks(this);
        } else {
            mActivity.getApplication().registerActivityLifecycleCallbacks(this);
        }
    }

    void unregister() {
        iToast = null;
        if (mActivity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mActivity.unregisterActivityLifecycleCallbacks(this);
        } else {
            mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
    }
}