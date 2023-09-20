package com.legend.imkit.videocall.util

import android.util.Log
import com.legend.base.Applications
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.imkit.videocall.activity.CallActivity
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 管理通话
object WebRtcSessionManager : QBRTCClientSessionCallbacksImpl() {
//    private val TAG = WebRtcSessionManager::class.java.simpleName
    private val TAG = "qbVideo"

    private var callerUser: UserBean.QbUserInfo? = null
    private val opponentsUser = mutableListOf<UserBean.QbUserInfo>()
    private var currentSession: QBRTCSession? = null
    var isMultitudeCall = false //是否是多人电话
    var callUid: String? = null // 当前通话的id

    fun getCurrentSession(): QBRTCSession? {
//        Log.i(TAG, "WebRtcSessionManager --- getCurrentSession --- session: "  + currentSession?.userInfo.toString())
        return currentSession
    }

    fun setCurrentSession(qbCurrentSession: QBRTCSession?) {
//        Log.i(TAG, "WebRtcSessionManager --- setCurrentSession --- session: "  + qbCurrentSession?.userInfo.toString())
        currentSession = qbCurrentSession
    }

    override fun onReceiveNewSession(session: QBRTCSession) {
        Log.i(TAG, "WebRtcSessionManager --- onReceiveNewSession 有新会话呼入 --- userInfo: "  + session.userInfo.toString())

        if (currentSession == null) {
            CoroutineScope(Dispatchers.IO).launch {
                setCurrentSession(session)
                val isMultitude = session.userInfo["isMultitude"]
                isMultitudeCall = TypeConst.type_call_multitude == isMultitude
                if (isMultitude == TypeConst.type_call_single) {
                    val user = UserSimpleDataHelper.getUserInfo("s" + session.userInfo["uid"])
                    user?.apply {
                        callerUser = UserBean.QbUserInfo(uid, avatar?:"", nick_name?:"", session.callerID.toString())
                    }
                } else {
                    val uId = session.userInfo["uid"]?:"0"
                    val params = session.userInfo["parms"]
                    val tempParams = params?.split(",")
                    tempParams?.let {
                        for (temp in tempParams) {
                            val param = temp.split("&")
                            val user = UserSimpleDataHelper.getUserInfo("s" + param[0])
                            user?.apply {
                                val qbUserInfo = UserBean.QbUserInfo(param[0], avatar?:"", nick_name?:"", param[1])
                                if (param[0] == uId) {
                                    callerUser = qbUserInfo
                                } else {
                                    opponentsUser.add(qbUserInfo)
                                }
                            }
                        }
                    }
                }
                callUid = session.userInfo["callUid"]

                CallActivity.start(Applications.getCurrent(), true)
            }
        }
    }

    override fun onSessionClosed(session: QBRTCSession?) {
        Log.i(TAG, "WebRtcSessionManager --- onSessionClosed 会话被关闭 --- ")

        if (session == getCurrentSession()) {
            callUid = null
            callerUser = null
            opponentsUser.clear()
            setCurrentSession(null)
        }
    }

    fun setCaller(qbId: Int, uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = UserSimpleDataHelper.getUserInfo("s$uid")
            user?.apply {
                callerUser = UserBean.QbUserInfo(uid, avatar?:"", nick_name?:"", qbId.toString())
            }
        }
    }
    fun getCallerUser(): UserBean.QbUserInfo? {
        return callerUser
    }

    fun setOpponentUser(opponents: List<UserBean.QbUserInfo>?) {
        if (opponents == null) {
            this.opponentsUser.clear()
        } else {
            this.opponentsUser.addAll(opponents)
        }
    }
    fun getOpponentsUser(): List<UserBean.QbUserInfo> {
        return opponentsUser
    }

    fun removeMultiCallUser(qbId: Int?) {
        if (qbId == null) return
        if (opponentsUser.isNotEmpty()) {
            for (opponent in opponentsUser) {
                if (opponent.qbId == qbId.toString()) {
                    opponentsUser.remove(opponent)
                    return
                }
            }
        }

        if (callerUser != null && callerUser?.qbId == qbId.toString()) callerUser = null
    }

}