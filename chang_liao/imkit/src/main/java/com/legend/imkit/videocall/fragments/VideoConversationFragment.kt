package com.legend.imkit.videocall.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.legend.base.utils.MMKVUtils
import com.legend.base.utils.permission.PermissionUtils
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.widget.FloatingListener
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.network.viewmodel.MainRequest
import com.legend.imkit.R
import com.legend.imkit.videocall.activity.CallActivity
import com.legend.imkit.videocall.adapter.OpponentsFromCallAdapter
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.io.Serializable
import java.util.*

const val CAMERA_ENABLED = "is_camera_enabled"
const val IS_CURRENT_CAMERA_FRONT = "is_camera_front"
private const val LOCAL_TRACK_INITIALIZE_DELAY: Long = 800
private const val RECYCLE_VIEW_PADDING = 2
private const val UPDATING_USERS_DELAY: Long = 2000
private const val FULL_SCREEN_CLICK_DELAY: Long = 1000

class VideoConversationFragment : BaseConversationFragment(), Serializable,
    OpponentsFromCallAdapter.OnAdapterEventListener {
    private val TAG1 = VideoConversationFragment::class.java.simpleName
    private val TAG = "qbVideo"

    private lateinit var qbrtcClientSessionCallbacks: QBRTCClientSessionCallbacks
    private lateinit var qbrtcSessionStateCallback: QBRTCSessionStateCallback<QBRTCSession>
    private lateinit var qbrtcClientVideoTrackCallbacks: QBRTCClientVideoTracksCallbacks<QBRTCSession>

    private var rootView: ViewGroup? = null
    private lateinit var cameraToggle: ToggleButton
    private var parentView: View? = null
    private lateinit var actionVideoButtonsLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private var tvTime: TextView? = null
    private var tvTips: TextView? = null
    private var smallVideoView: QBRTCSurfaceView? = null
    private var bigVideoView: QBRTCSurfaceView? = null
    private var imgSmallWindowSwitch: ImageView? = null
    private var imgCameraReverse: ImageView? = null
//    private var imgCameraState: ImageView? = null
    private lateinit var opponentViewHolders: SparseArray<OpponentsFromCallAdapter.ViewHolder>
    private lateinit var opponentsAdapter: OpponentsFromCallAdapter
    private lateinit var allOpponents: MutableList<UserBean.QbUserInfo>
    private lateinit var bigVideoViewOnClickListener: BigVideoViewOnClickListener
    private var isPeerToPeerCall: Boolean = false
    private var localVideoTrack: QBRTCVideoTrack? = null
    private var remoteVideoTrack: QBRTCVideoTrack? = null
    private var remoteUserId: Int? = null
    private var isRemoteShown: Boolean = false
    private var userIdFullScreen: Int = 0
    private var connectionEstablished: Boolean = false
    private var allCallbacksInit: Boolean = false
    private var isCurrentCameraFront: Boolean = false
    private var isLocalVideoFullScreen: Boolean = false
    private var isLocalVideoBig = true

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_video_conversation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListener()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parentView = super.onCreateView(inflater, container, savedInstanceState)
        return parentView
    }

    override fun onStart() {
        super.onStart()
        if (!allCallbacksInit) {
            addListeners()
            allCallbacksInit = true
        }
    }

    override fun onResume() {
        super.onResume()
        fillLocalVideoTrack()
        toggleCamera(cameraToggle.isChecked)
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG1))
        conversationFragmentCallback?.addCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG1))
        conversationFragmentCallback?.addCallStatusTipsListener(CallStatusTipsImpl(TAG1))
    }

    override fun onPause() {
        // if camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
//        toggleCamera(false)

        if (connectionEstablished) {
            allCallbacksInit = false
        } else {
            Log.d(TAG, "We are in dialing process yet!")
        }


        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        releaseViewHolders()
        removeListeners()
        releaseViews()
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        Log.i(TAG, "initViews")
        if (view == null) {
            return
        }
        opponentViewHolders = SparseArray(opponents.size)
        isRemoteShown = false
        isCurrentCameraFront = true
        tvTime = view.findViewById(R.id.tv_time)
        tvTips = view.findViewById(R.id.tv_tips)
        tvTips?.visibility = if (isIncomingCall) View.GONE else View.VISIBLE
        imgCameraReverse = view.findViewById(R.id.img_camera_reverse)
        imgCameraReverse?.setOnClickListener {
            switchCamera()
        }
        isCurrentCameraFront = MMKVUtils.getBoolean(IS_CURRENT_CAMERA_FRONT, true)
//        if (!isCurrentCameraFront) { switchCamera()}
        imgSmallWindowSwitch = view.findViewById(R.id.img_small_window_switch)
        imgSmallWindowSwitch?.setOnClickListener {
            if (PermissionUtils.checkFloatPermission(activity)) {
                (activity as CallActivity).finish()
            } else {
                PermissionUtils.requestSettingCanDrawOverlays(activity, ApplicationConst.REQUEST_CODE_FLOAT_PERMISSION, ApplicationConst.APPLICATION_ID)
            }
        }
        initSmallVideoView(view)

        // 远程视频渲染view
        bigVideoView = view.findViewById(R.id.big_video_view)
        bigVideoView?.setOnClickListener(bigVideoViewOnClickListener)


        if (!isPeerToPeerCall) {
            recyclerView = view.findViewById(R.id.grid_opponents)

            recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), R.dimen.grid_item_divider))
            recyclerView.setHasFixedSize(true)
            val columnsCount = defineColumnsCount()
            val layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager

            // for correct removing item in adapter
            recyclerView.itemAnimator = null
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setGrid(columnsCount)
                    recyclerView.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                }
            })
        }

        cameraToggle = view.findViewById(R.id.toggle_camera)
        cameraToggle.isChecked = MMKVUtils.getBoolean(CAMERA_ENABLED, true)
        toggleCamera(cameraToggle.isChecked)

        actionVideoButtonsLayout = view.findViewById(R.id.element_set_video_buttons)

        actionButtonsEnabled(true)
        restoreSession()
    }
    
    private fun initSmallVideoView(view: View) {
        rootView = view.findViewById(R.id.fragmentOpponents)
        // 本地视频渲染view
        smallVideoView = view.findViewById(R.id.small_video_view)
        smallVideoView?.setOnClickListener{ v ->
            isLocalVideoBig = !isLocalVideoBig
            fillLocalVideoTrack()
            fillRemoteVideoTrack()
        }
        initCorrectSizeForLocalView()
        smallVideoView?.setZOrderMediaOverlay(true)
        val screenWith = DisplayUtils.getScreenWidth(context)
        val screenHeight = DisplayUtils.getScreenHeight(context)
        val params = smallVideoView?.layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = screenWith - params.width - DisplayUtils.dp2px(context, 8f)
        smallVideoView?.layoutParams = params
        val mediumScreen = (screenWith / 2f - params.width / 2f)
        smallVideoView?.let {
            
            it.setOnTouchListener(FloatingListener(context) { x, y, toSide ->
               
                params.leftMargin += x.toInt()
                params.topMargin += y.toInt()
                if (toSide) {
                    if (params.leftMargin <  DisplayUtils.dp2px(context, 8f)) {
                        params.leftMargin = DisplayUtils.dp2px(context, 8f)
                    }else if (params.leftMargin <= mediumScreen) {
                        params.leftMargin = DisplayUtils.dp2px(context, 8f)
                    } else {
                        params.leftMargin = screenWith - params.width - DisplayUtils.dp2px(context, 8f)
                    }
            
                    if (params.topMargin < DisplayUtils.dp2px(context, 80f)) {
                        params.topMargin = DisplayUtils.dp2px(context, 80f)
                    } else if (params.topMargin + params.height >screenHeight) {
                        params.topMargin = screenHeight - params.height
                    }
                }
                try {
                    rootView!!.updateViewLayout(smallVideoView, params)
                } catch (e: Exception) {
                }
            })
        }
        
    }

    override fun startedCallToRefreshUi() {
        super.startedCallToRefreshUi()
        tvTips?.visibility = View.GONE
    }
    override fun initFields() {
        super.initFields()
        bigVideoViewOnClickListener = BigVideoViewOnClickListener()
        allOpponents = Collections.synchronizedList(ArrayList(opponents.size))
        allOpponents.addAll(opponents)

        isPeerToPeerCall = !WebRtcSessionManager.isMultitudeCall
    }

    override fun configureOutgoingScreen() {
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_transparent_50))
        allOpponentsTextView.setTextColor(ContextCompat.getColor(requireContext(), com.com.legend.ui.R.color.ui_white))
        ringingTextView.setTextColor(ContextCompat.getColor(requireContext(), com.com.legend.ui.R.color.ui_white))
    }

    private fun setDuringCallActionBar() {
//        actionBar.setDisplayShowTitleEnabled(false)
        val user: UserBean.QbUserInfo? = if (isPeerToPeerCall) {
            if (isIncomingCall) WebRtcSessionManager.getCallerUser() else WebRtcSessionManager.getOpponentsUser()[0]
        } else {
            getUserById(userIdFullScreen)
        }

        user?.let {
            actionButtonsEnabled(true)
        }
    }

    private fun getUserById(qbId: Int): UserBean.QbUserInfo? {
        for (opponent in opponents) {
            if (opponent.qbId == qbId.toString()) return opponent
        }
        return null
    }

    private fun initListener() {
        qbrtcClientSessionCallbacks = object : QBRTCClientSessionCallbacks {
            override fun onUserNotAnswer(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "VideoConversationFragment --- onUsrNotAnswer 无用户应答 ---")
                hideProgressForOpponent(userId!!)
                setStatusForOpponent(userId, getString(R.string.call_no_answer))
            }

            override fun onCallRejectByUser(p0: QBRTCSession?, userId: Int?, userInfo: MutableMap<String, String>?) {
                Log.i(TAG, "VideoConversationFragment --- onCallRejectByUser 被 $userId 拒绝接听 ---")
                val reason = userInfo?.get("reason")?:""
                val tips = if (reason == TypeConst.type_call_reject_initiative) getString(R.string.call_reject_your_invite) else getString(R.string.call_reject_busy)
                setStatusForOpponent(userId, tips)
            }

            override fun onCallAcceptByUser(p0: QBRTCSession?, userId: Int?, p2: MutableMap<String, String>?) {
                Log.i(TAG, "VideoConversationFragment --- onCallAcceptByUser 被 $userId 接听 --- ")
                setStatusForOpponent(userId, getString(R.string.call_already_accept))
            }

            override fun onReceiveHangUpFromUser(p0: QBRTCSession?, userId: Int?, p2: MutableMap<String, String>?) {
                Log.i(TAG, "VideoConversationFragment --- onReceiveHangUpFromUser 被 $userId 挂断 --- ")
                setStatusForOpponent(userId, getString(R.string.call_hung_up))
                Log.d(TAG, "onReceiveHangUpFromUser userId= $userId")
                if (!isPeerToPeerCall) {
                    if (userId == userIdFullScreen) {
                        Log.d(TAG, "setAnotherUserToFullScreen call userId= $userId")
                        setAnotherUserToFullScreen()
                    }
                }
            }

            override fun onChangeReconnectionState(p0: QBRTCSession?, p1: Int?, state: QBRTCTypes.QBRTCReconnectionState?) {
                Log.i(TAG, "VideoConversationFragment --- onChangeReconnectionState 重新连接状态 --- state = $state empty")
            }

            override fun onSessionClosed(p0: QBRTCSession?) {
                Log.i(TAG, "VideoConversationFragment --- onSessionClosed 会话被关闭 --- empty")
            }

            override fun onReceiveNewSession(p0: QBRTCSession?) {
                Log.i(TAG, "VideoConversationFragment --- onReceiveNewSession 有新会话呼入 empty--- ")
            }

            override fun onUserNoActions(p0: QBRTCSession?, p1: Int?) {
                Log.i(TAG, "VideoConversationFragment --- onUserNoActions 用户超时未响应 --- empty")
            }

            override fun onSessionStartClose(p0: QBRTCSession?) {
                Log.i(TAG, "VideoConversationFragment --- onSessionStartClose 通话会话即将结束 ---empty")
            }

        }

        qbrtcSessionStateCallback = object : QBRTCSessionStateCallback<QBRTCSession> {
            override fun onStateChanged(p0: QBRTCSession?, state: BaseSession.QBRTCSessionState?) {
                Log.i(TAG, "VideoConversationFragment --- onStateChanged empty --- state = $state")
            }

            override fun onConnectedToUser(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "VideoConversationFragment --- onConnectedToUser --- ")
                connectionEstablished = true
                setStatusForOpponent(userId, getString(R.string.call_already_accept))
                hideProgressForOpponent(userId!!)
            }

            override fun onDisconnectedFromUser(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "VideoConversationFragment --- onDisconnectedFromUser 断开连接 userId = $userId --- ")
                setStatusForOpponent(userId, getString(R.string.call_interrupt))
            }

            override fun onConnectionClosedForUser(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "VideoConversationFragment --- onConnectionClosedForUser 被 $userId 关闭连接--- ")
                userId?.let {
                    setStatusForOpponent(it, getString(R.string.call_closed))
                    if (!isPeerToPeerCall) {
                        Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= $userId")
                        setBackgroundOpponentView(it)
                        hideProgressForOpponent(userId);
                    }
                }
            }

        }

        qbrtcClientVideoTrackCallbacks = object : QBRTCClientVideoTracksCallbacks<QBRTCSession> {
            override fun onLocalVideoTrackReceive(p0: QBRTCSession?, videoTrack: QBRTCVideoTrack?) {
                Log.d(TAG, "VideoConversationFragment --- onLocalVideoTrackReceive ---")
                localVideoTrack = videoTrack
                isLocalVideoFullScreen = true
                fillLocalVideoTrack()
                isLocalVideoFullScreen = false
            }

            override fun onRemoteVideoTrackReceive(p0: QBRTCSession?, videoTrack: QBRTCVideoTrack?, userID: Int?) {
                Log.d(TAG, "VideoConversationFragment --- onRemoteVideoTrackReceive for opponent= $userID ---")
                remoteVideoTrack = videoTrack
                remoteUserId = userID
                fillRemoteVideoTrack()
            }
        }

    }

    private fun fillLocalVideoTrack() {
        if (localVideoTrack == null) {
            val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap()
            if (!videoTrackMap.isNullOrEmpty()) {
                val entryIterator = videoTrackMap.entries.iterator()
                while (entryIterator.hasNext()) {
                    val entry = entryIterator.next()
                    val userId = entry.key

                    if (userId == currentUser.id) {
                        localVideoTrack =  entry.value
                        break
                    }
                }
            }
        }
        localVideoTrack?.let {
            fillVideoView(if (isLocalVideoBig) bigVideoView else smallVideoView, it, false)
        }
    }

    private fun fillRemoteVideoTrack() {
        remoteUserId?.let {
            if (isPeerToPeerCall) {
                setDuringCallActionBar()
                bigVideoView?.let {
                    fillVideoView(if (isLocalVideoBig) smallVideoView else bigVideoView, remoteVideoTrack, true)
                    updateVideoView(if (isLocalVideoBig) smallVideoView else bigVideoView, true)
                }
            } else {
                mainHandler?.postDelayed({ setRemoteViewMultiCall(it, remoteVideoTrack!!) }, LOCAL_TRACK_INITIALIZE_DELAY)
            }
        }
    }

    private fun addListeners() {
        conversationFragmentCallback?.addSessionEventsListener(qbrtcClientSessionCallbacks)
        conversationFragmentCallback?.addSessionStateListener(qbrtcSessionStateCallback)
        conversationFragmentCallback?.addVideoTrackListener(qbrtcClientVideoTrackCallbacks)
    }

    private fun removeListeners() {
        conversationFragmentCallback?.removeSessionStateListener(qbrtcSessionStateCallback)
        conversationFragmentCallback?.removeSessionEventsListener(qbrtcClientSessionCallbacks)
        conversationFragmentCallback?.removeVideoTrackListener(qbrtcClientVideoTrackCallbacks)
        conversationFragmentCallback?.removeCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG1))
        conversationFragmentCallback?.removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG1))
        conversationFragmentCallback?.removeCallStatusTipsListener(CallStatusTipsImpl((TAG1)))
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        cameraToggle.isEnabled = inability
        // inactivate toggle buttons
        cameraToggle.isActivated = inability
    }

    private fun restoreSession() {
        Log.d(TAG, "restoreSession ")
        if (conversationFragmentCallback?.isConnectedCall() == false) {
            return
        }
        startedCall()
        val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap() ?: return
        if (videoTrackMap.isNotEmpty()) {
            val entryIterator = videoTrackMap.entries.iterator()
            while (entryIterator.hasNext()) {
                val entry = entryIterator.next()
                Log.d(TAG, "check ability to restoreSession for user:" + entry.key)
                val userId = entry.key
                val videoTrack = entry.value

                if (userId == currentUser.id) {
                    Log.d(TAG, "execute restoreSession for user:$userId")
                    mainHandler?.postDelayed({
                        qbrtcClientVideoTrackCallbacks.onLocalVideoTrackReceive(null, videoTrack)
                    }, LOCAL_TRACK_INITIALIZE_DELAY)
                } else if (conversationFragmentCallback?.getPeerChannel(userId) != QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
                    Log.d(TAG, "execute restoreSession for user:$userId")
                    mainHandler?.postDelayed({
                        qbrtcSessionStateCallback.onConnectedToUser(null, userId)
                        qbrtcClientVideoTrackCallbacks.onRemoteVideoTrackReceive(null, videoTrack, userId)
                    }, LOCAL_TRACK_INITIALIZE_DELAY)
                } else {
                    entryIterator.remove()
                }
            }
        }
    }

    private fun initCorrectSizeForLocalView() {
        val params = smallVideoView?.layoutParams
        val displaymetrics = resources.displayMetrics

        val screenWidthPx = displaymetrics.widthPixels
//        Log.d(TAG, "screenWidthPx $screenWidthPx")

        val width = (screenWidthPx * 0.3).toInt()
        val height = width / 2 * 3
        params?.width = width
        params?.height = height
        smallVideoView?.layoutParams = params
    }

    private fun setGrid(columnsCount: Int) {
        val gridWidth = parentView?.measuredWidth
        Log.i(TAG, "onGlobalLayout : gridWidth= $gridWidth; columnsCount= $columnsCount")
        val itemMargin = resources.getDimension(R.dimen.grid_item_divider)

        gridWidth?.let {
            val cellSizeWidth = defineSize(it, columnsCount, itemMargin)
            Log.i(TAG, "onGlobalLayout : cellSize=$cellSizeWidth")
            opponentsAdapter = OpponentsFromCallAdapter(requireContext(), this, opponents, cellSizeWidth, resources.getDimension(R.dimen.item_height).toInt())
            opponentsAdapter.setAdapterListener(this)
            recyclerView.adapter = opponentsAdapter
        }
    }

    private fun defineSize(measuredWidth: Int, columnsCount: Int, padding: Float): Int {
        return measuredWidth / columnsCount - (padding * 2).toInt() - RECYCLE_VIEW_PADDING
    }

    private fun defineColumnsCount(): Int {
        return opponents.size - 1
    }

    private fun releaseViewHolders() {
        opponentViewHolders.clear()
    }

    private fun releaseViews() {
        if (conversationFragmentCallback?.getCurrentSessionState() != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CLOSED) {
            for (item in (activity as CallActivity).getVideoTrackMap()) {
                val renderer = item.value.renderer
//                item.value.removeRenderer(renderer)
            }
        }

        smallVideoView?.release()
        bigVideoView?.release()

        bigVideoView = null
        if (!isPeerToPeerCall) {
            releaseOpponentsViews()
        }
    }

    override fun initButtonsListener() {
        super.initButtonsListener()

        cameraToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            MMKVUtils.putBoolean(CAMERA_ENABLED, isChecked)
            toggleCamera(isChecked)
        }
    }

    private fun switchCamera() {
        cameraToggle.isEnabled = false
        conversationFragmentCallback?.onSwitchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(b: Boolean) {
                Log.d(TAG, "camera switched, bool = $b")
                isCurrentCameraFront = b
                MMKVUtils.putBoolean(IS_CURRENT_CAMERA_FRONT, b)
                toggleCameraInternal()
                cameraToggle.isEnabled = true
            }

            override fun onCameraSwitchError(s: String) {
                Log.d(TAG, "camera switch error $s")
                ToastUtils.show(getString(R.string.camera_swicth_failed) + s)
                cameraToggle.isEnabled = true
            }
        })
    }


    private fun toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!")
        if (bigVideoView == null) {
            return
        }
        val surfaceViewRenderer = if (isLocalVideoFullScreen) {
            bigVideoView
        } else {
            smallVideoView
        }
        updateVideoView(surfaceViewRenderer, isCurrentCameraFront)
        toggleCamera(cameraToggle.isChecked)
    }

    private fun toggleCamera(isNeedEnableCam: Boolean) {
        if (conversationFragmentCallback?.isMediaStreamManagerExist() == true) {
            conversationFragmentCallback?.onSetVideoEnabled(isNeedEnableCam)
        }
        if (connectionEstablished && !cameraToggle.isEnabled) {
            cameraToggle.isEnabled = true
        }
    }

    override fun onBindLastViewHolder(holder: OpponentsFromCallAdapter.ViewHolder, position: Int) {
        Log.i(TAG, "onBindLastViewHolder position=$position")
    }

    override fun onItemClick(position: Int) {
        val userId = opponentsAdapter.getItem(position)
        Log.d(TAG, "USer onItemClick= $userId")

        val connectionState = conversationFragmentCallback?.getPeerChannel(userId)
        val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap()
        val isNotExistVideoTrack = videoTrackMap != null && !videoTrackMap.containsKey(userId)
        val isConnectionStateClosed =
            connectionState?.ordinal == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal
        val holder = findHolder(userId)
        if (isNotExistVideoTrack || isConnectionStateClosed || holder == null) {
            return
        }

        replaceUsersInAdapter(position)
        updateViewHolders(position)
        swapUsersFullscreenToPreview(holder, userId)
    }

    private fun replaceUsersInAdapter(position: Int) {
        val opponents = allOpponents
        for (qbUser in opponents) {
            if (qbUser.qbId == userIdFullScreen.toString()) {
                opponentsAdapter.replaceUsers(position, qbUser)
                break
            }
        }
    }

    private fun updateViewHolders(position: Int) {
        val childView = recyclerView.getChildAt(position)
        val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
        opponentViewHolders.put(position, childViewHolder)
    }

    private fun swapUsersFullscreenToPreview(holder: OpponentsFromCallAdapter.ViewHolder, userId: Int) {
        val videoTrack = conversationFragmentCallback?.getVideoTrackMap()?.get(userId)
        val videoTrackFullScreen = conversationFragmentCallback?.getVideoTrackMap()?.get(userIdFullScreen)

        val videoView = holder.getOpponentView()

        videoTrack?.let {
            fillVideoView(userId, bigVideoView, videoTrack);
        }

        if (videoTrackFullScreen != null) {
            fillVideoView(0, videoView, videoTrackFullScreen)
        } else {
            holder.getOpponentView().setBackgroundColor(Color.BLACK)
            bigVideoView?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setRemoteViewMultiCall(userID: Int, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "setRemoteViewMultiCall fillVideoView")
        val itemHolder = getViewHolderForOpponent(userID)
        if (itemHolder == null) {
            Log.d(TAG, "itemHolder == null - true")
            return
        }
        val remoteVideoView = itemHolder.getOpponentView()
        remoteVideoView.setZOrderMediaOverlay(true)
        updateVideoView(remoteVideoView, false)

        Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView")
        if (isRemoteShown) {
            Log.d(TAG, "onRemoteVideoTrackReceive User = $userID")
            fillVideoView(remoteVideoView, videoTrack, true)
            showRecyclerView();
        } else {
            isRemoteShown = true
            itemHolder.getOpponentView().release()
            opponentsAdapter.removeItem(itemHolder.adapterPosition)
            this.bigVideoView?.let {
                fillVideoView(userID, it, videoTrack)
                updateVideoView(this.bigVideoView, false)
            }
            setDuringCallActionBar()
        }
    }

    private fun showRecyclerView() {
        val params = recyclerView.layoutParams
        params.height = resources.getDimension(R.dimen.item_height).toInt()
        recyclerView.layoutParams = params
        recyclerView.visibility = View.VISIBLE
    }

    private fun getViewHolderForOpponent(userID: Int): OpponentsFromCallAdapter.ViewHolder? {
        var holder: OpponentsFromCallAdapter.ViewHolder? = opponentViewHolders.get(userID)
        if (holder == null) {
            Log.d(TAG, "holder not found in cache")
            holder = findHolder(userID)
            if (holder != null) {
                opponentViewHolders.append(userID, holder)
            }
        }
        return holder
    }

    private fun findHolder(userID: Int?): OpponentsFromCallAdapter.ViewHolder? {
        Log.d(TAG, "findHolder for $userID")
        val childCount = recyclerView.childCount
        for (index in 0 until childCount) {
            val childView = recyclerView.getChildAt(index)
            val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
            if (userID == childViewHolder.getUserId()) {
                return childViewHolder
            }
        }
        return null
    }

    private fun releaseOpponentsViews() {
        val layoutManager = recyclerView.layoutManager
        val childCount = layoutManager?.childCount!!
        Log.d(TAG, " releaseOpponentsViews for  $childCount views")
        for (index in 0 until childCount) {
            val childView = layoutManager.getChildAt(index)
            childView?.let {
                Log.d(TAG, " relese View for  $index, $childView")
                val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
                childViewHolder.getOpponentView().release()
            }
        }
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private fun fillVideoView(videoView: QBRTCSurfaceView?, videoTrack: QBRTCVideoTrack?, remoteRenderer: Boolean) {
        videoTrack?.removeRenderer(videoTrack.renderer)
        videoTrack?.addRenderer(videoView)
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront)
        }
        Log.d(TAG, (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun updateVideoView(videoView: SurfaceViewRenderer?, mirror: Boolean) {
        val scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL
        Log.i(TAG, "updateVideoView mirror:$mirror, scalingType = $scalingType")
        videoView?.setScalingType(scalingType)
        videoView?.setMirror(mirror)
        videoView?.requestLayout()
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private fun fillVideoView(userId: Int, videoView: QBRTCSurfaceView?, videoTrack: QBRTCVideoTrack) {
        if (userId != 0) {
            userIdFullScreen = userId
        }
        fillVideoView(videoView, videoTrack, true)
    }

    private fun setStatusForOpponent(userId: Int?, status: String) {
        if (isPeerToPeerCall) {
            return
        }

        val holder = findHolder(userId) ?: return

        holder.setStatus(status)
    }

    private fun updateNameForOpponent(userId: Int, newUserName: String) {
        if (isPeerToPeerCall) {
//            actionBar.subtitle = getString(R.string.opponent, newUserName)
        } else {
            val holder = findHolder(userId)
            if (holder == null) {
                Log.d("UPDATE_USERS", "holder == null")
                return
            }

            Log.d("UPDATE_USERS", "holder != null")
//            holder.setUserName(newUserName)
        }
    }

    private fun hideProgressForOpponent(userId: Int) {
        if (isPeerToPeerCall) {
            return
        }
        val holder = getViewHolderForOpponent(userId) ?: return

        holder.getProgressBar().visibility = View.GONE
    }

    private fun setBackgroundOpponentView(userId: Int?) {
        if (userId != userIdFullScreen) {
            val holder = findHolder(userId) ?: return
            holder.getOpponentView().setBackgroundColor(Color.BLACK)
        } else {
            bigVideoView?.setBackgroundColor(Color.BLACK)
        }
    }

    private fun setAnotherUserToFullScreen() {
        if (opponentsAdapter.opponents.isEmpty()) {
            return
        }
        for (user in opponents) {
            val videoTrack = conversationFragmentCallback?.getVideoTrack(user.qbId.toInt())
            videoTrack?.let { track ->
                val userFullScreen = getUserById(userIdFullScreen)

                val itemHolder = findHolder(user.qbId.toInt())

                itemHolder?.setUserId(userIdFullScreen)
                itemHolder?.setUserName(userFullScreen?.name?:"")
                itemHolder?.setStatus(getString(R.string.call_closed))
                itemHolder?.getOpponentView()?.release()
                itemHolder?.adapterPosition?.let { position ->
                    replaceUsersInAdapter(position)
                }
                bigVideoView?.let {
                    fillVideoView(user.qbId.toInt(), it, track)
                    Log.d(TAG, "fullscreen enabled")
                }
                return
            }
        }
    }

//    private fun updateAllOpponentsList(newUsers: ArrayList<UserBean.QbUserInfo>) {
//        val indexList = allOpponents.indices
//        for (index in indexList) {
//            for (updatedUser in newUsers) {
//                if (updatedUser == allOpponents[index]) {
//                    allOpponents[index] = updatedUser
//                }
//            }
//        }
//    }

    private fun runUpdateUsersNames(newUsers: ArrayList<QBUser>) {
        // need delayed for synchronization with recycler parentView initialization
//        mainHandler?.postDelayed({
//            for (user in newUsers) {
//                Log.d(TAG, "runUpdateUsersNames. foreach, user = " + user.fullName)
//                updateNameForOpponent(user.id, user.fullName)
//            }
//        }, UPDATING_USERS_DELAY)
    }

    internal inner class DividerItemDecoration(context: Context, @DimenRes dimensionDivider: Int) :
        RecyclerView.ItemDecoration() {
        private val space: Int = context.resources.getDimensionPixelSize(dimensionDivider)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(space, space, space, space)
        }
    }

    internal inner class BigVideoViewOnClickListener : View.OnClickListener {
        private var lastFullScreenClickTime = 0L
        private var isActionButtonVisible = true

        override fun onClick(v: View) {
            if (SystemClock.uptimeMillis() - lastFullScreenClickTime < FULL_SCREEN_CLICK_DELAY) {
                return
            }
            lastFullScreenClickTime = SystemClock.uptimeMillis()

            if (connectionEstablished) {
                setFullScreenOnOff()
            }
        }

        private fun setFullScreenOnOff() {
            if (isActionButtonVisible) {
                hideToolBarAndButtons()
            } else {
                showToolBarAndButtons()
            }
        }

        private fun hideToolBarAndButtons() {
            isActionButtonVisible = false
            actionVideoButtonsLayout.visibility = View.GONE
            tvTime?.visibility = View.GONE
            imgCameraReverse?.visibility = View.GONE
            imgSmallWindowSwitch?.visibility = View.GONE
            if (!isPeerToPeerCall) {
                shiftBottomListOpponents()
            }
        }

        private fun showToolBarAndButtons() {
            isActionButtonVisible = true
            actionVideoButtonsLayout.visibility = View.VISIBLE
            tvTime?.visibility = View.VISIBLE
            imgCameraReverse?.visibility = View.VISIBLE
            imgSmallWindowSwitch?.visibility = View.VISIBLE
            if (!isPeerToPeerCall) {
                shiftMarginListOpponents()
            }
        }

        private fun shiftBottomListOpponents() {
            val params = recyclerView.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.setMargins(0, 0, 0, 0)

            recyclerView.layoutParams = params
        }

        private fun shiftMarginListOpponents() {
            val params = recyclerView.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            params.setMargins(0, 0, 0, resources.getDimension(R.dimen.margin_common).toInt())

            recyclerView.layoutParams = params
        }
    }

    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
//            updateAllOpponentsList(updatedOpponents)
            Log.d(TAG, "updateOpponentsList(), opponents = $updatedOpponents")
//            runUpdateUsersNames(updatedOpponents)
        }

        override fun equals(other: Any?): Boolean {
            if (other is UpdateOpponentsListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class CallTimeUpdateListenerImpl(val tag: String?) : CallActivity.CallTimeUpdateListener {
        override fun updatedCallTime(time: String) {
            tvTime?.text = time
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallTimeUpdateListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class CallStatusTipsImpl(val tag: String?): CallActivity.CallStatueTipsListener {
        override fun onTips(qbId: Int?, tips: String, isShow: Boolean) {
            if (WebRtcSessionManager.isMultitudeCall) {

            } else {
                tvTips?.text = tips
                tvTips?.visibility = if (isShow) View.VISIBLE else View.GONE
            }
        }
        override fun equals(other: Any?): Boolean {
            if (other is CallStatusTipsImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}