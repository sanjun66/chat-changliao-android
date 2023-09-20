package com.legend.sochat.exception

import com.legend.baseui.ui.util.ActivityManager
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

class UncaughtExceptionHandlerImpl: UncaughtExceptionHandler {
    private lateinit var defaultExceptionHandler: UncaughtExceptionHandler
    fun init() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler() as UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
//        val appContext = Applications.getCurrent().applicationContext
//        val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
//        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        appContext.startActivity(intent)

        ActivityManager.getInstance().finishAllActivity()
        defaultExceptionHandler.uncaughtException(thread, ex)
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(1)

    }

}