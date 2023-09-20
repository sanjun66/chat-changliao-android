package com.legend.base.app;

import android.app.Application;
import android.content.Context;


/**
 * App delegate 管理APP全局管理器
 */
public class AppDelegate {


    /**
     *  全局Activity声明周期管理
     */
    private ActivityLifecycle mActivityLifecycle;

    protected void attachBaseContext(Context base) {

    }

    public void onCreate(Application application) {

        if (mActivityLifecycle == null) {
            mActivityLifecycle = new ActivityLifecycle();
        }
        application.registerActivityLifecycleCallbacks(mActivityLifecycle);
    }

    public void onTerminate() {
        mActivityLifecycle = null;
    }
}
