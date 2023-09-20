package com.legend.base.router

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.legend.base.utils.AppUtils

/**
 * usage
 */
class SchemeFilterActivity : Activity() {
    companion object {
        const val TAG = "H_RouterUrl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.data == null) {
            finish()
            return
        }
        RouterManager.getInstance().open(this, intent?.data.toString(), object : NavCallback() {
            override fun onArrival(postcard: Postcard?) {
                if (AppUtils.isApkDebuggable(this@SchemeFilterActivity)) {
                    Log.e(TAG, intent.data.toString())
                }
                finish()
            }
        })
    }
}