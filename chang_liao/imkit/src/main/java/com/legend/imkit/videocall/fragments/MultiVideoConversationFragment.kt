package com.legend.imkit.videocall.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.legend.base.utils.MMKVUtils
import com.legend.base.utils.permission.PermissionUtils
import com.legend.common.ApplicationConst
import com.legend.common.bean.UserBean
import com.legend.imkit.R
import com.legend.imkit.util.QbUtil
import com.legend.imkit.videocall.activity.CallActivity
import com.legend.imkit.videocall.adapter.MultiCallAdapter
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.legend.imkit.widget.MultiChatLayoutManager
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import java.io.Serializable
import java.util.ArrayList

private const val LOCAL_TRACK_INITIALIZE_DELAY: Long = 800
class MultiVideoConversationFragment(private val isVideo: Boolean): BaseConversationFragment(), Serializable {
    private val TAG1 = MultiVideoConversationFragment::class.java.simpleName
    private val TAG = "qbVideo"

    private lateinit var qbrtcClientSessionCallbacks: QBRTCClientSessionCallbacks
    private lateinit var qbrtcSessionStateCallback: QBRTCSessionStateCallback<QBRTCSession>
    private lateinit var qbrtcClientVideoTrackCallbacks: QBRTCClientVideoTracksCallbacks<QBRTCSession>

    private var parentView: View? = null
    private lateinit var cameraToggle: ToggleButton
    private var recyclerView: RecyclerView? = null
    private var tvTime: TextView? = null
    private var imgSmallWindowSwitch: ImageView? = null

    private var multiCallAdapter: MultiCallAdapter? = null
    private val qbUserInfoXList = mutableListOf<UserBean.QbUserInfoX>()
    private var localVideoTrack: QBRTCVideoTrack? = null
    private var userIdFullScreen: Int = 0
    private var allCallbacksInit: Boolean = false
    private var connectionEstablished: Boolean = false
    private var isCurrentCameraFront: Boolean = true



    override fun configureOutgoingScreen() { }
    override fun getFragmentLayout() = if (isVideo) R.layout.fragment_multi_video_conversation else R.layout.fragment_multi_audio_conversation

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
        if (isVideo) {
            fillVideoTrack()
            toggleCamera(cameraToggle.isChecked)
        }
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG1))
        conversationFragmentCallback?.addCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG1))
        conversationFragmentCallback?.addCallStatusTipsListener(CallStatusTipsImpl(TAG1))
    }

    override fun onPause() {
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
        if (view == null) return
        tvTime = view.findViewById(R.id.tv_time)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView?.layoutManager = MultiChatLayoutManager()
//        recyclerView?.layoutManager = GridLayoutManager(context, 2)
        recyclerView?.itemAnimator = null
        multiCallAdapter = MultiCallAdapter(isVideo)
        recyclerView?.adapter = multiCallAdapter
        multiCallAdapter?.data = qbUserInfoXList
        imgSmallWindowSwitch = view.findViewById(R.id.img_small_window_switch)
        imgSmallWindowSwitch?.setOnClickListener {
            if (PermissionUtils.checkFloatPermission(activity)) {
                (activity as CallActivity).finish()
            } else {
                PermissionUtils.requestSettingCanDrawOverlays(activity, ApplicationConst.REQUEST_CODE_FLOAT_PERMISSION, ApplicationConst.APPLICATION_ID)
            }
        }
        if (isVideo) {
            cameraToggle = view.findViewById(R.id.toggle_camera)
            cameraToggle.isChecked = MMKVUtils.getBoolean(CAMERA_ENABLED, true)
//        toggleCamera(cameraToggle.isChecked)
            cameraToggle.visibility = View.VISIBLE
        }

        initListData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initListData() {
        qbUserInfoXList.clear()
        val caller = WebRtcSessionManager.getCallerUser()
        val users = WebRtcSessionManager.getOpponentsUser()
        if (caller != null) {
            val qbUserX = UserBean.QbUserInfoX(caller.uid, caller.avatar, caller.name, caller.qbId, if (caller.uid == ApplicationConst.getUserId()) localVideoTrack else null, true)
            Log.i(TAG, "当前user 状态0 = " + qbUserX.uid + " - " + qbUserX.qbId + " - " + getConnectionState(qbUserX.qbId.toInt()))
            qbUserInfoXList.add(qbUserX)
        }
        for (user in users) {
            if (user.uid == ApplicationConst.getUserId()) {
                val qbUserX = UserBean.QbUserInfoX(user.uid, user.avatar, user.name, user.qbId, localVideoTrack, true)
                Log.i(TAG, "当前user 状态1 = " + qbUserX.uid + " - " + qbUserX.qbId + " - " + getConnectionState(qbUserX.qbId.toInt()))
                qbUserInfoXList.add(qbUserX)
            } else {
                val qbUserX = UserBean.QbUserInfoX(user.uid, user.avatar, user.name, user.qbId)
                Log.i(TAG, "当前user 状态2 = " + qbUserX.uid + " - " + qbUserX.qbId + " - "+ getConnectionState(qbUserX.qbId.toInt()))
                qbUserInfoXList.add(qbUserX)
            }
        }
        Log.i("MultiVideo position", "first notify - qbUserXsize = " + qbUserInfoXList.size)
        multiCallAdapter?.notifyDataSetChanged()
    }

    private fun getUserXById(qbId: Int): Int? {
        for ((index, qbUserX) in qbUserInfoXList.withIndex()) {
            if (qbUserX.qbId == qbId.toString()) {
                return index
            }
        }

        return null
    }

    private fun fillVideoTrack() {
        if (localVideoTrack == null) {
            val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap()
            if (!videoTrackMap.isNullOrEmpty()) {
                val entryIterator = videoTrackMap.entries.iterator()
                while (entryIterator.hasNext()) {
                    val entry = entryIterator.next()
                    val userId = entry.key

                    if (userId == currentUser.id) {
                        localVideoTrack =  entry.value
                        refreshVideoTrack(QbUtil.getCurrentDbUser().id, localVideoTrack)
                    } else {
                        val position = getUserXById(userId)
                        position?.let {
                            refreshVideoTrack(userId, entry.value)
                        }
                    }
                }
            }
        }
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        if (isVideo) {
            cameraToggle.isEnabled = inability
            cameraToggle.isActivated = inability
        }
    }

    override fun initButtonsListener() {
        super.initButtonsListener()
        if (isVideo) {
            cameraToggle.setOnCheckedChangeListener { buttonView, isChecked ->
                MMKVUtils.putBoolean(CAMERA_ENABLED, isChecked)
                toggleCamera(isChecked)
            }
        }
    }

    private fun toggleCamera(isNeedEnableCam: Boolean) {
        if (!isVideo) return
        if (conversationFragmentCallback?.isMediaStreamManagerExist() == true) {
            conversationFragmentCallback?.onSetVideoEnabled(isNeedEnableCam)
        }
        if (connectionEstablished && !cameraToggle.isEnabled) {
            cameraToggle.isEnabled = true
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

    @SuppressLint("NotifyDataSetChanged")
    private fun removeUser(qbId: Int?) {
        if (qbId == null) return
        if (qbUserInfoXList.isEmpty()) return
        for ((index, qbUser) in qbUserInfoXList.withIndex()) {
            if (qbUser.qbId == qbId.toString()) {
                qbUserInfoXList.remove(qbUser)
                multiCallAdapter?.notifyDataSetChanged()
//                multiCallAdapter?.notifyItemChanged(index)
                return
            }
        }

    }
    private fun initListener() {
        qbrtcClientSessionCallbacks = object : QBRTCClientSessionCallbacks {
            override fun onUserNotAnswer(session: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onUsrNotAnswer 无用户应答 ---")
                removeUser(userId)
            }

            override fun onCallRejectByUser(p0: QBRTCSession?, userId: Int?, userInfo: MutableMap<String, String>?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onCallRejectByUser 被 $userId 拒绝接听 ---")
                removeUser(userId)
            }

            override fun onCallAcceptByUser(p0: QBRTCSession?, userId: Int?, p2: MutableMap<String, String>?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onCallAcceptByUser 被 $userId 接听 --- ")
//                refreshData(userId, TypeConst.type_multi_call_accept)
            }

            override fun onReceiveHangUpFromUser(p0: QBRTCSession?, userId: Int?, p2: MutableMap<String, String>?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onReceiveHangUpFromUser 被 $userId 挂断 --- ")
                removeUser(userId)
                Log.d(TAG, "onReceiveHangUpFromUser userId= $userId")
                if (userId == userIdFullScreen) {
                    Log.d(TAG, "setAnotherUserToFullScreen call userId= $userId")
                    setAnotherUserToFullScreen()
                }
            }

            override fun onChangeReconnectionState(p0: QBRTCSession?, p1: Int?, state: QBRTCTypes.QBRTCReconnectionState?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onChangeReconnectionState 重新连接状态 --- state = $state empty")
            }

            override fun onSessionClosed(p0: QBRTCSession?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onSessionClosed 会话被关闭 --- empty")
            }

            override fun onReceiveNewSession(p0: QBRTCSession?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onReceiveNewSession 有新会话呼入 empty--- ")
            }

            override fun onUserNoActions(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onUserNoActions 用户超时未响应 --- empty")
                removeUser(userId)
            }

            override fun onSessionStartClose(p0: QBRTCSession?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onSessionStartClose 通话会话即将结束 ---empty")
            }

        }

        qbrtcSessionStateCallback = object : QBRTCSessionStateCallback<QBRTCSession> {
            override fun onStateChanged(session: QBRTCSession?, state: BaseSession.QBRTCSessionState?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onStateChanged empty --- state = $state")
            }

            override fun onConnectedToUser(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onConnectedToUser --- ")
                connectionEstablished = true
                refreshUserConnectedState(userId)
            }

            override fun onDisconnectedFromUser(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onDisconnectedFromUser 断开连接 userId = $userId --- ")
                removeUser(userId)
            }

            override fun onConnectionClosedForUser(p0: QBRTCSession?, userId: Int?) {
                Log.i(TAG, "MultiVideoConversationFragment --- onConnectionClosedForUser 被 $userId 关闭连接--- ")
                removeUser(userId)
            }

        }

        qbrtcClientVideoTrackCallbacks = object : QBRTCClientVideoTracksCallbacks<QBRTCSession> {
            override fun onRemoteVideoTrackReceive(p0: QBRTCSession?, videoTrack: QBRTCVideoTrack?, userID: Int?) {
                Log.d("MultiVideo position", "MultiVideoConversationFragment --- onRemoteVideoTrackReceive for opponent= $userID ---")
//                remoteVideoTrack = videoTrack
//                remoteUserId = userID
                if (userID != null) {
                    Log.i("MultiVideo position", "first notify22 ------ $userID")
                    refreshVideoTrack(userID, videoTrack)
                }

            }

            override fun onLocalVideoTrackReceive(p0: QBRTCSession?, videoTrack: QBRTCVideoTrack?) {
                Log.d(TAG, "MultiVideoConversationFragment --- onLocalVideoTrackReceive ---")
                localVideoTrack = videoTrack
//                isLocalVideoFullScreen = true
//                isLocalVideoFullScreen = false
                mainHandler?.postDelayed({
                    Log.i("MultiVideo position", "first notify23 ------ ")
                    refreshVideoTrack(QbUtil.getCurrentDbUser().id, videoTrack) }, LOCAL_TRACK_INITIALIZE_DELAY)
            }
        }

    }

    private fun refreshUserConnectedState(userId: Int?) {
        if (userId == null) return
        val position = getUserXById(userId)
        position?.let {
            val userX = qbUserInfoXList[position]
            userX.isConnected = true
            multiCallAdapter?.notifyItemChanged(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshVideoTrack(qbId: Int, videoTrack: QBRTCVideoTrack?) {
        if (videoTrack == null) return
        val position = getUserXById(qbId)
        if (position != null){
            val userX = qbUserInfoXList[position]
//            Log.d(TAG, "MultiVideoConversationFragment --- refreshVideoTrack for opponent=  ---" + GlobalGsonUtils.toJson(userX))
            userX.videoTrack = videoTrack
            Log.i("MultiVideo position", "first notify2 ------ ")
            multiCallAdapter?.notifyItemChanged(position)
        }
    }



    private fun setAnotherUserToFullScreen() {
        if (qbUserInfoXList.isEmpty()) {
            return
        }
    // todo 处理大视频被挂断大情况

//        for (user in opponents) {
//            val videoTrack = conversationFragmentCallback?.getVideoTrack(user.qbId.toInt())
//            userIdFullScreen = qbUserInfoXList[0].qbId.toInt()
//            videoTrack?.let { track ->
//                val userFullScreen = getUserById(userIdFullScreen)
//
//                val itemHolder = findHolder(user.qbId.toInt())
//
//                itemHolder?.setUserId(userIdFullScreen)
//                itemHolder?.setUserName(userFullScreen?.name?:"")
//                itemHolder?.setStatus(getString(R.string.call_closed))
//                itemHolder?.getOpponentView()?.release()
//                itemHolder?.adapterPosition?.let { position ->
//                    replaceUsersInAdapter(position)
//                }
//                bigVideoView?.let {
//                    fillVideoView(user.qbId.toInt(), it, track)
//                    Log.d(TAG, "fullscreen enabled")
//                }
//                return
//            }
//        }
    }


    private fun releaseViewHolders() {

    }

    private fun releaseViews() {
        // todo
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
//                tvTips?.text = tips
//                tvTips?.visibility = if (isShow) View.VISIBLE else View.GONE
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