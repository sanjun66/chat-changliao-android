package com.legend.baseui.ui.util.keyboardvisibilityevent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;

public class SimpleRegistry implements Registry {
    private WeakReference<Activity> activityWeakReference;
    private WeakReference<ViewTreeObserver.OnGlobalLayoutListener> onGlobalLayoutListenerWeakReference;

    public SimpleRegistry(Activity activity,
                          ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.onGlobalLayoutListenerWeakReference = new WeakReference<>(onGlobalLayoutListener);
    }


    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void unRegister() {
        Activity activity = activityWeakReference.get();
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = this.onGlobalLayoutListenerWeakReference.get();

        if (null != activity && null != onGlobalLayoutListenerWeakReference) {
            View activityRoot = KeyboardVisibilityEvent.getActivityRoot(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                activityRoot.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            } else {
                activityRoot.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
            }
        }
    }
}
