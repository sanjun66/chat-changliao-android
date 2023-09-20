package com.legend.imkit.videocall.activity

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.legend.base.app.BaseApplication
import com.legend.base.utils.MMKVUtils
import com.legend.base.utils.permission.Permission
import com.legend.base.utils.permission.PermissionUtils
import com.legend.base.utils.permission.listener.PermissionListener
import com.legend.baseui.ui.util.CommonDialogUtils
import com.legend.baseui.ui.util.statusbar.StatusBarUtil
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.TypeConst
import com.legend.imkit.R
import com.legend.imkit.util.*
import com.legend.imkit.videocall.CallFloatBoxView
import com.legend.imkit.videocall.fragments.*
import com.legend.imkit.videocall.service.CallService
import com.legend.imkit.videocall.service.MIN_OPPONENT_SIZE
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.legend.imkit.videocall.util.addFragment
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.jivesoftware.smack.AbstractConnectionListener
import org.jivesoftware.smack.ConnectionListener
import org.webrtc.CameraVideoCapturer

private const val INCOME_CALL_FRAGMENT = "income_call_fragment"
private const val REQUEST_PERMISSION_SETTING = 545

class CallActivity : AppCompatActivity(), IncomeCallFragmentCallbackListener,
//    QBRTCSessionStateCallback<QBRTCSession>,
//    QBRTCClientSessionCallbacks,
      ConversationFragmentCallback
    , ScreenShareFragment.OnSharingEvents
{

    private var TAG = CallActivity::class.java.simpleName
//    private var TAG = "qbVideo"
    private lateinit var qbrtcClientSessionCallbacks: QBRTCClientSessionCallbacks
    private lateinit var qbrtcSessionStateCallback: QBRTCSessionStateCallback<QBRTCSession>

    private val callStateListeners = hashSetOf<CallStateListener>()
    private val updateOpponentsListeners = hashSetOf<UpdateOpponentsListener>()
    private val callTimeUpdateListeners = hashSetOf<CallTimeUpdateListener>()
    private val callStateTipsListeners = hashSetOf<CallStatueTipsListener>()
    private lateinit var showIncomingCallWindowTaskHandler: Handler
    private var connectionListener: ConnectionListenerImpl? = null
    private lateinit var callServiceConnection: ServiceConnection
    private lateinit var showIncomingCallWindowTask: Runnable
//    private var opponentIds: List<Int>? = null
    private lateinit var callService: CallService
    private var connectionView: LinearLayout? = null
    private var isInComingCall: Boolean = false
    private var isVideoCall: Boolean = false
    private val timeCallBack = CallTimerCallback()

    companion object {
        fun start(context: Context, isIncomingCall: Boolean) {
            Log.i("qbVideo", "start CallActivity")
            val intent = Intent(context, CallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            MMKVUtils.putBoolean(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            context.startActivity(intent)
            CallService.start(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        connectionView = View.inflate(this, R.layout.connection_popup, null) as LinearLayout
        hideActionStatusBar()
    }

    private fun hideActionStatusBar() {
        // actionbar隐藏
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        StatusBarUtil.immersion(this)
    }

    private fun initScreen() {
//        Log.i(TAG, "CallActivity -- initScreen")
        callService.setCallTimerCallback(timeCallBack)
        isVideoCall = callService.isVideoCall()

//        opponentIds = callService.getOpponents()

        applyMediaSettings()
        if (!WebRtcSessionManager.isMultitudeCall) {
            QBRTCMediaConfig.setVideoWidth(960)
            QBRTCMediaConfig.setVideoHeight(540)
        }

        addListeners()

        isInComingCall = if (intent != null && intent.extras != null) {
            intent?.extras?.getBoolean(EXTRA_IS_INCOMING_CALL) ?: false
        } else {
            MMKVUtils.getBoolean(EXTRA_IS_INCOMING_CALL, false)
        }

        if (callService.isConnectedCall()) {
            checkPermission()
            if (callService.isSharingScreenState()) {
                startScreenSharing(null)
                return
            }
            addConversationFragment(isInComingCall)
        } else {
            // 如果没人皆通 先播放铃声
            if (!isInComingCall) {
                callService.playRingtone()
            }
            Log.i(TAG, "CallActivity -- isInComingCall = $isInComingCall")
            startSuitableFragment(isInComingCall)
        }
    }

    private fun addListeners() {
        qbrtcClientSessionCallbacks = object: QBRTCClientSessionCallbacks {
            override fun onUserNotAnswer(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "CallActivity --- onUsrNotAnswer 无用户应答 ---")
                if (callService.isCurrentSession(session)) {
                    callService.stopRingtone()
                    notifyCallStatusTips(userId, getString(R.string.call_no_answer), true)
                }
            }

            override fun onCallRejectByUser(session: QBRTCSession?, userId: Int?, userInfo: MutableMap<String, String>?) {
                // empty
                if (callService.isCurrentSession(session)) {
                    val reason = userInfo?.get("reason")
                    if (reason == TypeConst.type_call_reject_initiative) {
                        notifyCallStatusTips(userId, getString(R.string.call_reject_your_invite), true)
                    } else {
                        notifyCallStatusTips(userId, getString(R.string.call_reject_busy), true)
                    }
                }
                Log.i(TAG, "CallActivity --- onCallRejectByUser 被 $userId 拒绝接听 --- empty, userInfo = " + userInfo.toString())
            }

            override fun onCallAcceptByUser(session: QBRTCSession?, userId: Int?, userInfo: MutableMap<String, String>?) {
                Log.i(TAG, "CallActivity --- onCallAcceptByUser 被 $userId 接听 --- userInfo = " + userInfo.toString())
                if (callService.isCurrentSession(session)) {
                    callService.stopRingtone()
                    notifyCallStatusTips(userId, getString(R.string.call_already_accept), false)
                }
            }

            override fun onReceiveHangUpFromUser(session: QBRTCSession?, userId: Int?, userInfo: MutableMap<String, String>?) {
                Log.i(TAG, "CallActivity --- onReceiveHangUpFromUser 被 $userId 挂断 --- userInfo = " + userInfo.toString())
                if (callService.isCurrentSession(session)) {
                    val numberOpponents = session?.opponents?.size
                    if (numberOpponents == MIN_OPPONENT_SIZE) {
                        hangUpCurrentSession()
                    }
                    notifyCallStatusTips(userId, getString(R.string.call_hung_up), true)
//                    val participant = QbUsersDbManager.getUserById(userId)
//                    val participantName = if (participant != null) participant.fullName else userId.toString()
//                    ToastUtils.show("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation")
                }
            }

            override fun onChangeReconnectionState(session: QBRTCSession?, userId: Int?, state: QBRTCTypes.QBRTCReconnectionState?) {
                Log.i(TAG, "CallActivity --- onChangeReconnectionState 重新连接状态 --- state = $state")
            }

            override fun onSessionClosed(session: QBRTCSession?) {
                Log.i(TAG, "CallActivity --- onSessionClosed 会话被关闭 --- ")
                if (callService.isCurrentSession(session)) {
                    callService.stopForeground(true)
                    finish()
                    notifyCallStatusTips(null, getString(R.string.call_closed), false)
                }
            }

            override fun onReceiveNewSession(session: QBRTCSession?) {
                // empty
                Log.i(TAG, "CallActivity --- onReceiveNewSession 有新会话呼入 empty--- session: "  + session?.userInfo.toString())
            }

            override fun onUserNoActions(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "CallActivity --- onUserNoActions 用户超时未响应 --- ")
                startIncomeCallTimer(0)
                notifyCallStatusTips(userId, getString(R.string.call_no_answer), true)
            }

            override fun onSessionStartClose(session: QBRTCSession?) {
                Log.i(TAG, "CallActivity --- onSessionStartClose 通话会话即将结束 --- ")
                if (callService.isCurrentSession(session)) {
                    callService.removeSessionStateListener(qbrtcSessionStateCallback)
                    notifyCallStopped()
                }
            }

        }
        addSessionEventsListener(qbrtcClientSessionCallbacks)

        qbrtcSessionStateCallback = object : QBRTCSessionStateCallback<QBRTCSession> {
            override fun onStateChanged(session: QBRTCSession?, state: BaseSession.QBRTCSessionState?) {
                // empty
                Log.i(TAG, "CallActivity --- onStateChanged empty --- state = $state")
            }

            override fun onConnectedToUser(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "CallActivity --- onConnectedToUser --- ")
                notifyCallStarted()
                if (isInComingCall) {
                    stopIncomeCallTimer()
                }
            }

            override fun onDisconnectedFromUser(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "CallActivity --- onDisconnectedFromUser 断开连接 userId = $userId --- empty")
            }

            override fun onConnectionClosedForUser(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "CallActivity --- onConnectionClosedForUser 被 $userId 关闭连接--- empty")
            }

        }
        addSessionStateListener(qbrtcSessionStateCallback)

        connectionListener = ConnectionListenerImpl()
        addConnectionListener(connectionListener)
    }

    private fun removeListeners() {
        removeSessionEventsListener(qbrtcClientSessionCallbacks)
        removeSessionStateListener(qbrtcSessionStateCallback)
        removeConnectionListener(connectionListener)

        callService.removeCallTimerCallback(timeCallBack)
    }

    private fun bindCallService() {
        callServiceConnection = CallServiceConnection()
        Intent(this, CallService::class.java).also { intent ->
            bindService(intent, callServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult requestCode=$requestCode, resultCode= $resultCode")
        if (resultCode == EXTRA_LOGIN_RESULT_CODE) {
            data?.let {
                val isLoginSuccess = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false)
                Log.i(TAG, "onActivityResult isLoginSuccess = $isLoginSuccess")
                if (isLoginSuccess) {
                    initScreen()
                } else {
                    finish()
                }
            }
        }
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK) {
            data?.let {
//                startScreenSharing(it)
                Log.i(TAG, "Starting screen capture")
            }
        }
    }

    private fun startScreenSharing(data: Intent?) {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(ScreenShareFragment::class.simpleName)
        if (fragmentByTag !is ScreenShareFragment) {
            addFragment(
                supportFragmentManager, R.id.fragment_container,
                ScreenShareFragment.newInstance(), ScreenShareFragment::class.java.simpleName
            )
            data?.let {
                callService.startScreenSharing(it)
            }
        }
    }

    private fun startSuitableFragment(isInComingCall: Boolean) {
        val session = WebRtcSessionManager.getCurrentSession()
        if (session != null) {
            loadAbsentUsers()
            if (isInComingCall) {
                initIncomingCallTask()
                addIncomeCallFragment()
                checkPermission()
            } else {
                addConversationFragment(isInComingCall)
                intent.removeExtra(EXTRA_IS_INCOMING_CALL)
                MMKVUtils.putBoolean(EXTRA_IS_INCOMING_CALL, false)
            }
        } else {
            finish()
        }
    }

    private fun checkPermission() {
        if (isVideoCall) {
            Permission.request(CALL_PERMISSIONS, this, object : PermissionListener {
                override fun preRequest(permissions: Array<out String>?): Boolean {
                    return false
                }

                override fun granted(permissions: Array<out String>?) {
                }

                override fun denied(permissions: Array<out String>?) {
                    ToastUtils.show(getString(R.string.permission_denied))
                    CommonDialogUtils.showPermissionDeniedTipsDialog(PermissionUtils.getPermissionName(
                        CALL_PERMISSIONS), this@CallActivity)
                }

            })
        } else {
            Permission.request(CALL_PERMISSIONS[1], this, object : PermissionListener {
                override fun preRequest(permissions: Array<out String>?): Boolean {
                    return false
                }

                override fun granted(permissions: Array<out String>?) {
                }

                override fun denied(permissions: Array<out String>?) {
                    ToastUtils.show(getString(R.string.permission_denied))
                    CommonDialogUtils.showPermissionDeniedTipsDialog(PermissionUtils.getPermissionName(
                        CALL_PERMISSIONS[1]), this@CallActivity)
                }

            })
        }
    }

    private fun loadAbsentUsers() {
//        val usersFromDb = QbUsersDbManager.allUsers
//        val allParticipantsOfCall = ArrayList<Int>()

//        opponentIds?.let {
//            allParticipantsOfCall.addAll(it)
//        }

//        if (isInComingCall) {
//            val callerId = callService.getCallerId()
//            callerId?.let {
//                allParticipantsOfCall.add(it)
//            }
//        }

//        val idsNotLoadedUsers = ArrayList<Int>()
//
//        for (userId in allParticipantsOfCall) {
//            val user = QBUser(userId)
//            user.fullName = userId.toString()
//            if (!usersFromDb.contains(user)) {
//                idsNotLoadedUsers.add(userId)
//            }
//        }
//        if (idsNotLoadedUsers.isNotEmpty()) {
//            // 获取user列表
//            QBUsers.getUsersByIDs(idsNotLoadedUsers, null)
//                .performAsync(object : QBEntityCallbackImpl<ArrayList<QBUser>>() {
//                    override fun onSuccess(users: ArrayList<QBUser>, params: Bundle) {
//                        QbUsersDbManager.saveAllUsers(users, false)
//                        notifyOpponentsUpdated(users)
//                    }
//                })
//        }
    }

    private fun initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = Handler(Looper.getMainLooper())
        showIncomingCallWindowTask = Runnable {
            if (callService.currentSessionExist()) {
//                ToastUtils.show("Call was stopped by UserNoActions timer")
                callService.clearCallState()
                callService.clearButtonsState()
                WebRtcSessionManager.setCurrentSession(null)
                CallService.stop(this@CallActivity)
                finish()
            }
        }
    }

    private fun hangUpCurrentSession() {
        callService.stopRingtone()
        if (!callService.hangUpCurrentSession(HashMap())) {
            finish()
        }
    }

    private fun startIncomeCallTimer(time: Long) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time)
    }

    private fun stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer")
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "CallActivity -- onResume bindCallService")
        bindCallService()
        CallFloatBoxView.hideFloatBox()
    }

    override fun onPause() {
        super.onPause()
        if (PermissionUtils.checkFloatPermission(this)) {
            if (WebRtcSessionManager.getCurrentSession() != null) {
                val connected = WebRtcSessionManager.getCurrentSession()?.state == BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED
                println("wdd---> $TAG onPause  state = ${WebRtcSessionManager.getCurrentSession()} connected = $connected")
                callService.setCallTimerCallback(CallFloatBoxView.getTimeCallBack())
                CallFloatBoxView.showFB(BaseApplication.INSTANCE, null, isVideoCall, callService.getVideoTrackMap(), connected, isInComingCall)
            }
        } else {
            ToastUtils.show(getString(R.string.float_window_not_allowed))
        }
    }

    override fun finish() {
        // fix bug when user returns to call from service and the backstack doesn't have any screens
//        OpponentsActivity.start(this)
//        CallService.stop(this)

        super.finish()

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(callServiceConnection)
        if (::callService.isInitialized) {
            removeListeners()
        }
    }

    override fun onBackPressed() {
        // to prevent returning from Call Fragment
    }

    private fun addIncomeCallFragment() {
        if (callService.currentSessionExist()) {
            val fragment = IncomeCallFragment()
            if (supportFragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT) == null) {
                addFragment(supportFragmentManager, R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT)
            }
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method")
        }
    }

    private fun addConversationFragment(isIncomingCall: Boolean) {
        val baseConversationFragment: BaseConversationFragment = if (WebRtcSessionManager.isMultitudeCall) {
            MultiVideoConversationFragment(isVideoCall)
        } else if (isVideoCall) {
            // 视频通话
            VideoConversationFragment()
        } else {
            // 音频通话
            AudioConversationFragment()
        }
        val conversationFragment = BaseConversationFragment.newInstance(baseConversationFragment, isIncomingCall)
        addFragment(
            supportFragmentManager,
            R.id.fragment_container,
            conversationFragment,
            conversationFragment.javaClass.simpleName
        )
    }

    private fun showConnectionPopUp() {
        runOnUiThread {
            val fragmentContainer: FrameLayout = findViewById(R.id.fragment_container)
            val connectionNotificationView: TextView? = connectionView?.findViewById(R.id.notification)
            connectionNotificationView?.setText(R.string.connection_was_lost)
            if (connectionView?.parent == null) {
                fragmentContainer.addView(connectionView)
            }
        }
    }

    private fun hideConnectionPopUp() {
        runOnUiThread {
            val fragmentContainer: FrameLayout = findViewById(R.id.fragment_container)
            fragmentContainer.removeView(connectionView)
        }
    }

    private inner class ConnectionListenerImpl : AbstractConnectionListener() {
        override fun connectionClosedOnError(e: Exception?) {
            showConnectionPopUp()
        }

        override fun reconnectionSuccessful() {
            hideConnectionPopUp()
        }
    }

    override fun addConnectionListener(connectionCallback: ConnectionListener?) {
        callService.addConnectionListener(connectionCallback)
    }

    override fun removeConnectionListener(connectionCallback: ConnectionListener?) {
        callService.removeConnectionListener(connectionCallback)
    }

    override fun addSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?) {
        callService.addSessionStateListener(clientConnectionCallbacks)
    }

    override fun addSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?) {
        callService.addSessionEventsListener(eventsCallback)
    }

    override fun onSetAudioEnabled(isAudioEnabled: Boolean) {
        callService.setAudioEnabled(isAudioEnabled)
    }

    override fun onHangUpCurrentSession() {
        hangUpCurrentSession()
    }

    @TargetApi(21)
    override fun onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        QBRTCScreenCapturer.requestPermissions(this)
    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        callService.switchCamera(cameraSwitchHandler)
    }

    override fun onSetVideoEnabled(isNeedEnableCam: Boolean) {
        callService.setVideoEnabled(isNeedEnableCam)
    }

    override fun onSwitchAudio(enablSpeaker: Boolean) {
        callService.switchAudio(enablSpeaker)
    }

    override fun removeSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?) {
        callService.removeSessionStateListener(clientConnectionCallbacks)
    }

    override fun removeSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?) {
        callService.removeSessionEventsListener(eventsCallback)
    }

    override fun addCallStateListener(callStateListener: CallStateListener) {
        callStateListeners.add(callStateListener)
    }

    override fun removeCallStateListener(callStateListener: CallStateListener) {
        callStateListeners.remove(callStateListener)
    }

    override fun addUpdateOpponentsListener(updateOpponentsListener: UpdateOpponentsListener) {
        updateOpponentsListeners.add(updateOpponentsListener)
    }

    override fun removeUpdateOpponentsListener(updateOpponentsListener: UpdateOpponentsListener) {
        updateOpponentsListeners.remove(updateOpponentsListener)
    }

    override fun addCallTimeUpdateListener(callTimeUpdateListener: CallTimeUpdateListener) {
        callTimeUpdateListeners.add(callTimeUpdateListener)
    }

    override fun removeCallTimeUpdateListener(callTimeUpdateListener: CallTimeUpdateListener) {
        callTimeUpdateListeners.remove(callTimeUpdateListener)
    }

    override fun addCallStatusTipsListener(callStatusTipListener: CallStatueTipsListener) {
        callStateTipsListeners.add(callStatusTipListener)
    }

    override fun removeCallStatusTipsListener(callStatueTipsListener: CallStatueTipsListener) {
        callStateTipsListeners.remove(callStatueTipsListener)
    }

    override fun addOnChangeAudioDeviceListener(onChangeDynamicCallback: OnChangeAudioDevice?) {
        // empty
    }

    override fun removeOnChangeAudioDeviceListener(onChangeDynamicCallback: OnChangeAudioDevice?) {
        // empty
    }

    override fun acceptCall(userInfo: Map<String, String>) {
        callService.acceptCall(userInfo)
    }

    override fun startCall(userInfo: Map<String, String>) {
        callService.startCall(userInfo)
    }

    override fun currentSessionExist(): Boolean {
        return callService.currentSessionExist()
    }

    override fun getOpponents(): List<Int>? {
        return callService.getOpponents()
    }

    override fun getCallerId(): Int? {
        return callService.getCallerId()
    }

    override fun addVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        callService.addVideoTrackListener(callback)
    }

    override fun removeVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        callService.removeVideoTrackListener(callback)
    }

    override fun getCurrentSessionState(): BaseSession.QBRTCSessionState? {
        return callService.getCurrentSessionState()
    }

    override fun getPeerChannel(userId: Int): QBRTCTypes.QBRTCConnectionState? {
        return callService.getPeerChannel(userId)
    }

    override fun isMediaStreamManagerExist(): Boolean {
        return callService.isMediaStreamManagerExist()
    }

    override fun isConnectedCall(): Boolean {
        return callService.isConnectedCall()
    }

    override fun getVideoTrackMap(): MutableMap<Int, QBRTCVideoTrack> {
        return callService.getVideoTrackMap()
    }

    override fun getVideoTrack(userId: Int): QBRTCVideoTrack? {
        return callService.getVideoTrack(userId)
    }

    // IncomeCallFragmentCallbackListener
    override fun onAcceptCurrentSession() {
        if (callService.currentSessionExist()) {
            addConversationFragment(true)
        } else {
            Log.d(TAG, "SKIP addConversationFragment method")
        }
    }

    // IncomeCallFragmentCallbackListener
    override fun onRejectCurrentSession() {
        val userInfo = HashMap<String, String>()
        userInfo["reason"] = TypeConst.type_call_reject_initiative
        callService.rejectCurrentSession(userInfo)
    }

    // OnSharingEvents
    override fun onStopPreview() {
        callService.stopScreenSharing()
        addConversationFragment(isInComingCall)
    }

    private fun notifyCallStarted() {
        for (listener in callStateListeners) {
            listener.startedCall()
        }
    }

    private fun notifyCallStopped() {
        for (listener in callStateListeners) {
            listener.stoppedCall()
        }
    }

    private fun notifyOpponentsUpdated(opponents: ArrayList<QBUser>) {
        for (listener in updateOpponentsListeners) {
            listener.updatedOpponents(opponents)
        }
    }

    private fun notifyCallTimeUpdated(callTime: String) {
        for (listener in callTimeUpdateListeners) {
            listener.updatedCallTime(callTime)
        }
    }

    private fun notifyCallStatusTips(qbId: Int?, tips: String, isShow: Boolean) {
        for (listener in callStateTipsListeners) {
            listener.onTips(qbId, tips, isShow)
        }
    }

    private inner class CallServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            // empty
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CallService.CallServiceBinder
            callService = binder.getService()
            Log.i(TAG, "CallActivity -- onResume onServiceConnected currentSessionExist = " + callService.currentSessionExist())
            if (callService.currentSessionExist()) {
                initScreen();
            } else {
                finish()
            }
        }
    }

    private inner class CallTimerCallback : CallService.CallTimerListener {
        override fun onCallTimeUpdate(time: String) {
            runOnUiThread {
                notifyCallTimeUpdated(time)
            }
        }
    }

    interface OnChangeAudioDevice {
        fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice)
    }

    interface CallStateListener {
        fun startedCall()

        fun stoppedCall()
    }

    interface UpdateOpponentsListener {
        fun updatedOpponents(updatedOpponents: ArrayList<QBUser>)
    }

    interface CallTimeUpdateListener {
        fun updatedCallTime(time: String)
    }

    interface CallStatueTipsListener {
        fun onTips(qbId: Int?, tips: String, isShow: Boolean)
    }
}