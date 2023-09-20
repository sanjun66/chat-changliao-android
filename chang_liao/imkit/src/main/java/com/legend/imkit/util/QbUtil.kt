package com.legend.imkit.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.legend.base.utils.MMKVUtils
import com.legend.common.ApplicationConst
import com.legend.common.KeyConst
import com.legend.common.bean.UserBean
import com.legend.imkit.videocall.activity.CallActivity
import com.legend.imkit.videocall.executor.Executor
import com.legend.imkit.videocall.executor.ExecutorTask
import com.legend.imkit.videocall.service.LoginService
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.legend.imkit.videocall.util.sendPushMessage
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCTypes

object QbUtil {

    fun startLoginService(activity: Activity) {
        val qbUser = getCurrentDbUser()
        val tempIntent = Intent(activity, LoginService::class.java)
        val pendingIntent = activity.createPendingResult(EXTRA_LOGIN_RESULT_CODE, tempIntent, 0)
        Log.i("qbVideo", "LoginService")
        LoginService.loginToChatAndInitRTCClient(activity, qbUser, pendingIntent)
    }

    fun loginToRestQb() {
        val qbUser = getCurrentDbUser()
        Log.i("qbVideo", "去认证")
        QBUsers.signIn(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(result: QBUser, params: Bundle) {
                Log.i("qbVideo", "认证成功")
            }

            override fun onError(responseException: QBResponseException) {
                Log.i("qbVideo", "认证失败： "+ responseException.message)
            }
        })
    }

    fun getCurSimpleUserInfo(): UserBean.QbUserInfo {
        val currentUserQb = getCurrentDbUser()
        return UserBean.QbUserInfo(ApplicationConst.getUserId(), ApplicationConst.getUserNickName(), ApplicationConst.getUserAvatar(),currentUserQb.id.toString())
    }

    fun startCall(callId: String, isMultitude: Boolean, isVideoCall: Boolean, context: Activity, opponents: List<UserBean.QbUserInfo>?) {
        if (opponents.isNullOrEmpty()) {
            Log.i("qbVideo", "未获取到对端的user")
            return
        }
        if (!QBChatService.getInstance().isLoggedIn) {
            startLoginService(context)
            Log.i("qbVideo", "没有登录，不能startCall")
            return
        }
        // 对端id
        val opponentIdList = ArrayList<Int>()
        for (opponent in opponents) {
            opponentIdList.add(opponent.qbId.toInt())
        }

        WebRtcSessionManager.callUid = callId
        WebRtcSessionManager.isMultitudeCall = isMultitude
        WebRtcSessionManager.setCaller(getCurrentDbUser().id, ApplicationConst.getUserId())
        WebRtcSessionManager.setOpponentUser(opponents)

        // 设置是视频通话还是音频通话
        val conferenceType = if (isVideoCall) { QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO } else { QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO }
        val rtcClient = QBRTCClient.getInstance(context.applicationContext)
        val session = rtcClient.createNewSessionWithOpponents(opponentIdList, conferenceType)
        WebRtcSessionManager.setCurrentSession(session)

        // make Users FullName Strings and id's list for iOS VOIP push
        val sessionId = session.sessionID
        val opponentIds = ArrayList<String>()
        val opponentNames = ArrayList<String>()
        val usersInCall = ArrayList<UserBean.QbUserInfo>()
        usersInCall.addAll(opponents)
        // the Caller in exactly first position is needed regarding to iOS 13 functionality
        var currentUser = getCurSimpleUserInfo()
        usersInCall.add(0, currentUser)
        for (user in usersInCall) {
            opponentIds.add(user.qbId)
            opponentNames.add(user.name)
        }
        val idsInLine = TextUtils.join(",", opponentIds)
        val namesInLine = TextUtils.join(",", opponentNames)
        opponentIdList.forEach { userId ->
            Executor.addTask(object : ExecutorTask<Boolean> {
                override fun onBackground(): Boolean {
                    val timeout3Seconds = 3000L
                    Log.i("qbVideo", "去ping 用户")
                    return QBChatService.getInstance().pingManager.pingUser(userId, timeout3Seconds)
                }

                override fun onForeground(result: Boolean) {
                    if (result) {
//                        val message = "Participant with id: $userId is online. There is no need to send a VoIP notification."
//                        Log.d("qbVideo", message)
                        Log.d("qbVideo", "ID 为 $userId  的参与者在线。 无需发送 VoIP 通知。")
                    } else {
                        Log.d("qbVideo", "去发送push message")
                        sendPushMessage(userId, currentUser.name, sessionId, idsInLine, namesInLine, isVideoCall)
                    }
                }

                override fun onError(exception: Exception) {
                    Log.i("qbVideo", "startCall exception = " + exception.message)
//                    ToastUtils.show(exception.message.toString())
                }
            })
        }

        CallActivity.start(context, false)
    }

     fun saveCurrentDbUser(user: QBUser) {
        MMKVUtils.putInt(KeyConst.key_qb_user_id, user.id)
        MMKVUtils.putString(KeyConst.key_qb_user_login, user.login)
        MMKVUtils.putString(KeyConst.key_qb_user_pwd, user.password?:"")
    }

    fun getCurrentDbUser(): QBUser {
        val id = MMKVUtils.getInt(KeyConst.key_qb_user_id, 0)
        val login = MMKVUtils.getString(KeyConst.key_qb_user_login)
        val pwd = MMKVUtils.getString(KeyConst.key_qb_user_pwd)

        val user = QBUser(login, pwd)
        user.id = id
        user.login = login
        user.password = pwd
        return user
    }
}