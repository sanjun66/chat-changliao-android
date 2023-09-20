package com.legend.base.router.ext

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

/**
 * Activity生命周期监听
 */
class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    companion object {

        /**
         * 保存ActivityResultApi的Key
         */
        const val KEY_ACTIVITY_RESULT_API = "wfkj_activityResult"

        /**
         * 保存activity和Fragment的resultLauncher
         */
        val resultLauncherMap: MutableMap<String, XActivityResultContract<Intent, ActivityResult>> =
            mutableMapOf()

    }


    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity is FragmentActivity) {
            //生成一个Key
            val activityKey = activity.javaClass.simpleName + System.currentTimeMillis()
            //添加一个默认ActivityResultLauncher
            val resultLauncher =
                XActivityResultContract(activity, ActivityResultContracts.StartActivityForResult())
            //把生成的Key放到intent中，作为每一个Activity的唯一标识
            activity.intent.putExtra(KEY_ACTIVITY_RESULT_API, activityKey)
            //存放到Map中
            resultLauncherMap[activityKey] = resultLauncher

            Log.d(
                "ActivityLifecycle",
                "activityKey=${activityKey},activity=${activity},resultLauncher=${resultLauncher}"
            )
        }
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is FragmentActivity) {
            val activityKey = activity.intent.getStringExtra(KEY_ACTIVITY_RESULT_API)
            if (!TextUtils.isEmpty(activityKey)) {
                resultLauncherMap[activityKey]?.unregister()
                //移除activity的resultLauncher
                resultLauncherMap.remove(activityKey)
            }
        }
    }
}