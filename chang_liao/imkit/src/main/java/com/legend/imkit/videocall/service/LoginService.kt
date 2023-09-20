package com.legend.imkit.videocall.service

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.legend.imkit.util.*
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.quickblox.chat.QBChatService
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val EXTRA_COMMAND_TO_SERVICE = "command_for_service"
private const val EXTRA_QB_USER = "qb_user"

private const val COMMAND_NOT_FOUND = 0
private const val COMMAND_LOGIN = 1
private const val COMMAND_LOGOUT = 2
private const val COMMAND_DESTROY_RTC_CLIENT = 3

private const val EXTRA_PENDING_INTENT = "pending_Intent"

class LoginService : Service() {
//    private val TAG = LoginService::class.java.simpleName
    private val TAG = "qbVideo"
    private lateinit var chatService: QBChatService
    private lateinit var rtcClient: QBRTCClient
    private var pendingIntent: PendingIntent? = null
    private var currentCommand: Int = 0
    private var currentUser: QBUser? = null

    companion object {
        fun loginToChatAndInitRTCClient(context: Context, qbUser: QBUser, pendingIntent: PendingIntent? = null) {
            val intent = Intent(context, LoginService::class.java)
            intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGIN)
            intent.putExtra(EXTRA_QB_USER, qbUser)
            intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent)

            context.startService(intent)
        }

        fun logoutFromChat(context: Context) {
            val intent = Intent(context, LoginService::class.java)
            intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
            context.startService(intent)
        }

        fun destroyRTCClient(context: Context) {
            try {
                val intent = Intent(context, LoginService::class.java)
                intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
                context.startService(intent)
            } catch (e: Exception) {

            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        createChatService()
        Log.d(TAG, "Service onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        parseIntentExtras(intent)
        startSuitableActions()

        return START_REDELIVER_INTENT
    }

    private fun parseIntentExtras(intent: Intent?) {
        intent?.extras?.let {
            currentCommand = intent.getIntExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_NOT_FOUND)
            pendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT)
            val currentUser: QBUser? = intent.getSerializableExtra(EXTRA_QB_USER) as QBUser?
            currentUser?.let {
                this.currentUser = currentUser
            }
        }
    }

    private fun startSuitableActions() {
        Log.i(TAG, "LoginService - startSuitableActions - currentCommand: $currentCommand")
        when (currentCommand) {
            COMMAND_LOGIN -> loginToChatAndInitRTCClient()
            COMMAND_LOGOUT -> logoutFomChat()
            COMMAND_DESTROY_RTC_CLIENT -> destroyRtcClient()
        }
    }

    private fun createChatService() {
        val configurationBuilder = QBTcpConfigurationBuilder()
        configurationBuilder.socketTimeout = 300
        QBChatService.setConnectionFabric(QBTcpChatConnectionFabric(configurationBuilder))
        QBChatService.setDebugEnabled(true)
        QBChatService.setDefaultPacketReplyTimeout(10000)
        chatService = QBChatService.getInstance()
    }

    private fun loginToChatAndInitRTCClient() {
        Log.i(TAG, "service -- 去连接chat ： " + chatService.isLoggedIn)
        if (chatService.isLoggedIn) {
            sendResultToActivity(true, null)
        } else {
            currentUser?.let {
                loginToChatAndInitRTCClient(it)
            }
        }
    }

    private fun loginToChatAndInitRTCClient(user: QBUser) {
        // 登录
        Log.i(TAG, "service -- 连接chat接口 ： ")
        chatService.login(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser?, bundle: Bundle) {
                Log.i(TAG, "service -- 连接chat返回成功 并初始化qbrtcClient ")
                GlobalScope.launch {
                    initQBRTCClient()
                }
                sendResultToActivity(true, null)
            }

            override fun onError(e: QBResponseException) {
                Log.i(TAG, "service -- 连接chat返回失败 ； " +  e.message)
                var errorMessage: String? = e.message
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = "Login error"
                }
                sendResultToActivity(false, errorMessage)
            }
        })
    }

    private fun initQBRTCClient() {
        // 初始化rtc client
        rtcClient = QBRTCClient.getInstance(applicationContext)
        // 为了能够接收传入的视频聊天呼叫，您应该初始化并向其添加 WebRTC 信号； 如果您忘记设置信令管理器，您将无法处理呼叫。
        chatService.videoChatWebRTCSignalingManager?.addSignalingManagerListener { signaling, createdLocally ->
            val needAddSignaling = !createdLocally
            if (needAddSignaling) {
                rtcClient.addSignaling(signaling)
            }
        }

        applyRTCSettings()

        rtcClient.addSessionCallbacksListener(WebRtcSessionManager)
        Log.i(TAG, "LoginService -- pepareToProcessCalls() ---")
        rtcClient.prepareToProcessCalls()
    }

    private fun sendResultToActivity(isSuccess: Boolean, errorMessage: String?) {
        Log.d(TAG, "sendResultToActivity()")
        try {
            val intent = Intent()
            intent.putExtra(EXTRA_LOGIN_RESULT, isSuccess)
            intent.putExtra(EXTRA_LOGIN_ERROR_MESSAGE, errorMessage)

            pendingIntent?.send(this, EXTRA_LOGIN_RESULT_CODE, intent)
            stopForeground(true)
        } catch (e: PendingIntent.CanceledException) {
            val errorMessageSendingResult = e.message
            Log.d(TAG, errorMessageSendingResult ?: "Error sending result to activity")
        }
    }

    private fun destroyRtcClient() {
        if (::rtcClient.isInitialized) {
            rtcClient.destroy()
        }
        stopSelf()
    }

    private fun logoutFomChat() {
        chatService.logout(object : QBEntityCallback<Void?> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle) {
                chatService.destroy()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "logout onError " + e.message)
                chatService.destroy()
            }
        })
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Service onBind)")
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.d(TAG, "Service onTaskRemoved()")
        super.onTaskRemoved(rootIntent)
        if (isCallServiceNotRunning()) {
            logoutFomChat()
            destroyRtcClient()
        }
    }

    private fun isCallServiceNotRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var notRunning = true
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CallService::class.java.name == service.service.className) {
                notRunning = false
            }
        }
        return notRunning
    }
}