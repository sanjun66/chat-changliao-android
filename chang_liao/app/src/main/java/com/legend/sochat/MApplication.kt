package com.legend.sochat

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.launcher.ARouter
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.Applications
import com.legend.base.app.BaseApplication
import com.legend.base.utils.MMKVUtils
import com.legend.basenet.network.HttpHeaderManager
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.ApiResponse
import com.legend.basenet.network.listener.NetworkListener
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.imkit.util.AndroidEmoji
import com.legend.main.util.LogoutUtil
import com.legend.sochat.exception.UncaughtExceptionHandlerImpl
import com.liulishuo.filedownloader.FileDownloader
import com.quickblox.auth.session.QBSettings
import com.tencent.bugly.crashreport.CrashReport

//private const val APPLICATION_ID = "101365"
//private const val AUTH_KEY = "MKA5COLWfHLD2cZ"
//private const val AUTH_SECRET = "hsBRgEyFtU4pDcU"
//private const val ACCOUNT_KEY = "BYqyYrJ5yGsPExDWDzhj"

private const val APPLICATION_ID = "101658"
private const val AUTH_KEY = "khLRzzpkhvQukWD"
private const val AUTH_SECRET = "ugMT5RxV74Ub2P-"
private const val ACCOUNT_KEY = "XgZWsvgxmpBwQmZAwSvr"

private const val BUGLY_APP_ID = "53bfbb1b89"
class MApplication: BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AutoForegroundObserver())
        initARouter()
        MMKVUtils.initMMKV(this)
        initNet()
        FileDownloader.setup(this)
        AndroidEmoji.init(this)
        initQB()

        initJPush()
//        RongPushClientHelper.initRongPush(this)

        initUncaughtException()
    }

    private fun initJPush() {
        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(this)
        JPushInterface.setChannel(this, "JPush100")
        initChannel()
    }

    private fun initChannel() {
        val myChannelId = "JPush100"
        val myGroupId = "JPush101"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannelGroup = NotificationChannelGroup(myGroupId, "自定义通知组")
            nm.createNotificationChannelGroup(notificationChannelGroup)

            val notificationChannel = NotificationChannel(myChannelId, "自定义通知", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.group = myGroupId
            notificationChannel.enableLights(true)
            notificationChannel.enableLights(true)
            notificationChannel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/doda"), null)   // 设置自定义铃声
            nm.createNotificationChannel(notificationChannel)
        }
    }

    private fun initUncaughtException() {
        CrashReport.initCrashReport(this, BUGLY_APP_ID, BuildConfig.DEBUG)

        UncaughtExceptionHandlerImpl().init()
    }

    private fun initQB() {
        QBSettings.getInstance().init(applicationContext, APPLICATION_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().accountKey = ACCOUNT_KEY
    }

    private fun initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }

    private fun initNet() {
        NetworkManager.getInstance()
            .setBaseUrl(ApplicationConst.HOST)
            .setDebuggable(BuildConfig.DEBUG)
            .setEnableSSL(true) // debug 模式下不校验证书
            .registerListener(object : NetworkListener{
                override fun onTokenExpired(code: Int) {
                    if (code == ApiResponse.CODE_TOKEN_EXPIRED) {
                        ToastUtils.show(getString(R.string.login_token_expired))
                        LogoutUtil.logout()
                    } else if (code == ApiResponse.CODE_LOGIN_OTHER_DEVICE ||  code == ApiResponse.CODE_LOGIN_FIRST) {
                        ToastUtils.show(getString(R.string.login_other_devices))
                        LogoutUtil.logout()
                    }
                }

                override fun onReceivedCmd(cmd: String?) {
                }

            })
            .init(this)
        HttpHeaderManager.setHeader("Content-Type", "applicaion/json")
        if (Applications.isSecret) HttpHeaderManager.setHeader("app-encrypt", "0")
//        if (!TextUtils.isEmpty(MMKVUtils.getString(KeyConst.key_user_token))) {
//            HttpHeaderManager.setBearerToken(MMKVUtils.getString(KeyConst.key_user_token))
//        }
    }

    inner class AutoForegroundObserver: DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            LiveEventBus.get<Boolean>(EventKey.key_is_app_foreground).post(true)
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            LiveEventBus.get<Boolean>(EventKey.key_is_app_foreground).post(false)
        }
    }
}