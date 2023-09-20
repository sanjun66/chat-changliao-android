package com.legend.base.router.ext

import android.app.Application

/**
 * 初始化
 */
object ActivityResultApi {

    /**
     * 初始化
     */
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

}