package com.legend.base.router.ext

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract


class XActivityResultContract<I, O>(
    activityResultCaller: ActivityResultCaller,
    activityResultContract: ActivityResultContract<I, O>
) {

    private var activityResultCallback: ActivityResultCallback<O>? = null


    //registerForActivityResult只能在onCreate中注册。onStart之后就不能注册了,这里是在onCreate中注册
    private val launcher: ActivityResultLauncher<I>? =
        activityResultCaller.registerForActivityResult(activityResultContract) {
            activityResultCallback?.onActivityResult(it)
        }


    /**
     * 启动
     */
    @JvmOverloads
    fun launch(input: I, activityResultCallback: ActivityResultCallback<O>?) {
        this.activityResultCallback = activityResultCallback
        launcher?.launch(input)
    }

    /**
     * 注销
     */
    fun unregister() {
        launcher?.unregister()
    }

}