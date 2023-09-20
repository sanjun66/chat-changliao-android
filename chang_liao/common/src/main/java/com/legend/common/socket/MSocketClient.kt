package com.legend.common.socket

import android.text.TextUtils
import android.util.Log
import com.google.gson.reflect.TypeToken
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.MMKVUtils
import com.legend.base.utils.StringUtils
import com.legend.basenet.network.bean.ApiResponse
import com.legend.basenet.network.util.SoChatEncryptUtil
import com.legend.baseui.ui.util.ActivityManager
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.bean.*
import com.legend.common.db.DbManager
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.DBEntity
import com.legend.common.db.entity.SimpleMessage
import com.legend.common.utils.*
import com.legend.commonres.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.enums.ReadyState
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class MSocket private constructor() {
    private val TAG = "websocket"
    var socketClient: MSocketClient? = null
    private val msgRevListeners = mutableListOf<MessageRevListener>()

    private val HEART_TIME_OUT = 30 * 1000;
    private var heartTimer: Timer? = null
    private var timeCount: TimeSecondCount? = null

    private val msgRevListener = object : MsgRevListener {
        override fun onMessageRev(bytes: ByteBuffer?) {
        }

        override fun onMessageRev(message: String?) {
            try {
                message?.let {
                    var jsonObject: JSONObject
                    try {
                        jsonObject = JSONObject(message)
                    } catch (e: Exception) {
                        Log.i("websocket", "onMessageRev message isNotObject = $message")
                        return
                    }

                    val eventName = jsonObject.getString("event_name")
                    val code = jsonObject.optInt("code")
                    val jMsg = jsonObject.optString("message")
                    val jData = jsonObject.get("data")
                    val jUUid = if (jsonObject.has("uuid")) jsonObject.optString("uuid") else ""
                    if (code != 200) {
                        if (code == ApiResponse.CODE_TOKEN_EXPIRED
//                            || code == ApiResponse.CODE_LOGIN_OTHER_DEVICE
                            || code == ApiResponse.CODE_LOGIN_FIRST) {
                            if (!TextUtils.isEmpty(jMsg)) ToastUtils.show(jMsg)
                            MMKVUtils.putBoolean(KeyConst.key_is_login, false)
                            ActivityManager.getInstance().finishAllActivity()
                            Router.toLoginActivity()
                            Log.i("websocket", "socket 返回 error code = $code")
                        } else {
                            if (code != ApiResponse.CODE_LOGIN_OTHER_DEVICE && !TextUtils.isEmpty(jMsg)) ToastUtils.show(jMsg)
                        }
                        return
                    }
                    timeCount?.currentSecond = 0

//                    if (eventName == "heartbeat") return
//                if (socketMsg.event_name != "heartbeat")
//                    Log.i(TAG, "---- onMessageRev0 -- event_name : " + socketMsg.event_name)
                    var jsonMsg: String
                    if (jData is String) {
                        jsonMsg = SoChatEncryptUtil.decrypt(jData)
                        Log.i("websocket", "jsonMsg 解密后 = " + jsonMsg)
                    } else {
                        jsonMsg = jData.toString()
                        Log.i("websocket", "jsonMsg1 = " + jsonMsg)
                    }

                    when(eventName) {
                        TypeConst.socket_event_type_talk -> {
                            val chatMsg = GlobalGsonUtils.fromJson<ChatMessageModel<Any>>(jsonMsg, object : TypeToken<ChatMessageModel<Any>>(){}.type)
                            val talkType = chatMsg.talk_type
                            val messageType = chatMsg.message_type
                            val fromUid = chatMsg.from_uid
                            val toUid = chatMsg.to_uid
                            val isSender = fromUid == ApplicationConst.getUserId()
                            if (!isSender) sendMsgReceipt(chatMsg.id)

//                            CoroutineScope(Dispatchers.IO).launch {
//                                if (!(isSender && jUUid.startsWith(TypeConst.dev_android)) && UserSimpleDataHelper.getUserInfo(if (talkType == TypeConst.talk_type_single_chat) "s$fromUid" else "g$toUid")?.is_disturb == TypeConst.type_no) {
//                                    NotificationManager.playSound()
//                                }
//                            }

                            var needDispatch = true
                            // 类似通知消息需要先写入数据库，只有主动发送的才不用存数据库（在发送时已经插入了数据库）
                            if (messageType == TypeConst.chat_msg_type_group_op) {
                                val groupMsg:ChatMessageModel<GroupMsgContent> = RawMsgConvertUtil.msgToGroupChatModel(jsonMsg, jUUid)

//                                groupMsg.extra?.apply {
//                                    val isSelf = groupMsg.extra.operate_user_id.toString() == ApplicationConst.getUserId()
//                                    when(type) {
//                                        // 1:入群退群通知;2:自动退群;3:管理员踢群;4群解散
//                                        2 -> {
//                                            // 自动退群
//                                            if (isSelf) {
//                                                needDispatch = false
//                                                LiveEventBus.get<String>(EventKey.key_delete_group).post(toUid)
//                                            }
//                                        }
//                                        4 -> {
//                                            // 群解散
//                                            if (isSelf) {
//                                                needDispatch = false
//                                                LiveEventBus.get<String>(EventKey.key_delete_group).post(toUid)
//                                            }
//                                        }
//                                    }
//                                }
                                if (needDispatch) {
                                    insertRevMsgDb(groupMsg as ChatMessageModel<Any>)

                                    for (msgRev in msgRevListeners) {
                                        msgRev.onMessageGroupRev(groupMsg)
                                    }
                                } else {
                                    // empty
                                }
                            } else if (messageType == TypeConst.chat_msg_type_audio_call_state
                                || messageType == TypeConst.chat_msg_type_video_call_state) {
                                if (talkType == TypeConst.talk_type_single_chat) {
                                    val callStateMsg = RawMsgConvertUtil.msgToCallStateChatModel(jsonMsg, jUUid)
                                    insertRevMsgDb(callStateMsg as ChatMessageModel<Any>)

                                    for (msgRev in msgRevListeners) {
                                        msgRev.onMessageCallStateRev(callStateMsg)
                                    }
                                } else {
                                    val groupMsg = RawMsgConvertUtil.callMsgToGroupChatModel(jsonMsg, jUUid)
                                    insertRevMsgDb(groupMsg as ChatMessageModel<Any>)

                                    for (msgRev in msgRevListeners) {
                                        msgRev.onMessageGroupRev(groupMsg)
                                    }
                                }

                            } else if (messageType == TypeConst.chat_msg_type_forward) {
                                val forwardMsg = RawMsgConvertUtil.msgToForwardChatMsgModel(jsonMsg, jUUid)
                                insertRevMsgDb(forwardMsg as ChatMessageModel<Any>)

                                for (msgRev in msgRevListeners) {
                                    msgRev.onMessageForwardRev(forwardMsg)
                                }
                            } else if (messageType == TypeConst.chat_msg_type_text) {
                                val textMsg = RawMsgConvertUtil.msgToTextChatMsgModel(jsonMsg, jUUid)

                                if (!(textMsg.isSender && textMsg.isFromAndroid)) {
                                    textMsg.sendStatus = TypeConst.msg_send_status_sent
                                    insertRevMsgDb(textMsg as ChatMessageModel<Any>)
                                }

                                for (msgRev in msgRevListeners) {
                                    msgRev.onMessageTextRev(textMsg)
                                }
                            } else if (messageType == TypeConst.chat_msg_type_file) {
                                val fileMsg = RawMsgConvertUtil.msgToFileChatModel(jsonMsg, jUUid)

                                if (!(fileMsg.isSender && fileMsg.isFromAndroid)) {
                                    fileMsg.sendStatus = TypeConst.msg_send_status_sent
                                    insertRevMsgDb(fileMsg as ChatMessageModel<Any>)
                                }

                                for (msgRev in msgRevListeners) {
                                    msgRev.onMessageFileRev(fileMsg)
                                }
                            } else if (messageType == TypeConst.chat_msg_type_new_friend) {
                                val newFriendMsg = RawMsgConvertUtil.msgToNewFriendChatModel(jsonMsg, jUUid)
                                newFriendMsg?.let {
                                    insertRevMsgDb(it as ChatMessageModel<Any>)

                                    for (msgRev in msgRevListeners) {
                                        msgRev.onMessageNewFriendRev(it)
                                    }
                                }
                            } else if (messageType == TypeConst.chat_msg_type_apply) {
                                val applyFriendMsg: ChatMessageModel<ApplyFriendContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<ApplyFriendContent>>(){}.type)
                                if (applyFriendMsg.to_uid == ApplicationConst.getUserId()) {
                                    LiveEventBus.get<Boolean>(EventKey.key_have_friend_apply).post(true)
                                } else {
                                    // empty
                                }
                            } else {

                            }

                        }

                        TypeConst.socket_event_talk_revoke -> {
                            val revokeMsg: SimpleMessage = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<SimpleMessage>(){}.type)
                            revokeMsg.uuid = jUUid

                            if (!(revokeMsg.from_uid == ApplicationConst.getUserId() && jUUid.startsWith(TypeConst.dev_android))) {
                                updateRevMsgRevokeDb(revokeMsg.id)
                            }

                            for (msgRev in msgRevListeners) {
                                msgRev.onRevokeRev(revokeMsg)
                            }
                        }

                        TypeConst.socket_event_type_pull -> {
                            val msg: ArrayList<ChatMessageModel<Any>> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ArrayList<ChatMessageModel<Any>>>(){}.type)
                            val needRemoveList = arrayListOf<ChatMessageModel<Any>>()
                            for ((index,item) in msg.withIndex()) {
//                            Log.i("websocket", "获取到离线数据: type = " + item.message_type)
                                if (item.is_revoke == TypeConst.type_yes || item.message_type == TypeConst.chat_msg_type_apply) {
                                    needRemoveList.add(item)
                                    continue
                                } else if (item.message_type == TypeConst.chat_msg_type_new_friend) {
                                    val dataStr = GlobalGsonUtils.toJson(item)
                                    val newFriendMsg: ChatMessageModel<NewFriendContent> = GlobalGsonUtils.fromJson(dataStr, object : TypeToken<ChatMessageModel<NewFriendContent>>(){}.type)
                                    if (newFriendMsg.extra.state == "3") {
                                        needRemoveList.add(item)
                                        continue
                                    }
                                }

                                val sessionId = getSessionId(item)
                                item.session_id = sessionId
                                item.uuid = jUUid
                                item.sendStatus = TypeConst.msg_send_status_sent
                                item.revStatus = TypeConst.msg_rev_status_accept

                                if (item.message_type == TypeConst.chat_msg_type_group_op) {
                                    var dataStr = GlobalGsonUtils.toJson(item)
                                    val groupMsg:ChatMessageModel<GroupMsgContent> = GlobalGsonUtils.fromJson(dataStr, object : TypeToken<ChatMessageModel<GroupMsgContent>>(){}.type)
                                    groupMsg.session_id = sessionId
                                    groupMsg.uuid = jUUid
                                    groupMsg.revStatus = TypeConst.msg_rev_status_accept
                                    groupMsg.sendStatus = TypeConst.msg_send_status_sent

                                    var optionedUsers = ""
                                    groupMsg.extra.users?.let { it1 ->
                                        for ((index, user) in it1.withIndex()) {
                                            optionedUsers += user.nick_name
                                            if (index < it1.size - 1) {
                                                optionedUsers = "$optionedUsers、"
                                            }
                                        }
                                    }
                                    groupMsg.extra?.apply {
                                        val isSelf = groupMsg.extra.operate_user_id.toString() == ApplicationConst.getUserId()
                                        groupMsg.message = ChatUtil.getGroupOpMsg(type?:-1, isSelf, optionedUsers, groupMsg.extra.operate_user_name?:"")
                                    }
                                    msg[index] = groupMsg as ChatMessageModel<Any>

                                } else if (item.message_type == TypeConst.chat_msg_type_audio_call_state || item.message_type == TypeConst.chat_msg_type_video_call_state) {
                                    var dataStr = GlobalGsonUtils.toJson(item)
                                    val callStateMsg: ChatMessageModel<CallStateContent> = GlobalGsonUtils.fromJson(dataStr, object : TypeToken<ChatMessageModel<CallStateContent>>(){}.type)
                                    val isVideo = item.message_type == TypeConst.chat_msg_type_video_call_state
                                    if (item.talk_type == TypeConst.talk_type_single_chat) {
                                        msg[index] = ChatDataConvertUtil.chatMsgModelConvertCallStateMsg(item, jUUid) as ChatMessageModel<Any>
                                    } else {
                                        val groupMsg:ChatMessageModel<GroupMsgContent> = GlobalGsonUtils.fromJson(dataStr, object : TypeToken<ChatMessageModel<GroupMsgContent>>(){}.type)
                                        groupMsg.session_id = sessionId
                                        groupMsg.uuid = jUUid
                                        groupMsg.revStatus = TypeConst.msg_rev_status_accept
                                        groupMsg.sendStatus = TypeConst.msg_send_status_sent
                                        groupMsg.message_type = TypeConst.chat_msg_type_group_op
                                        groupMsg.extra.type = 1 // 保证能够每次正常显示
                                        callStateMsg.message = if (isVideo) StringUtils.getString(R.string.msg_sub_call_video) else StringUtils.getString(R.string.msg_sub_call_audio)
                                        groupMsg.extra.sendMsg = ChatUtil.getGroupCallStateMsg(callStateMsg.extra.nickname?:"", callStateMsg.extra.state?:"0", item.message_type == TypeConst.chat_msg_type_video_call_state)
                                        msg[index] = groupMsg as ChatMessageModel<Any>
                                    }

                                }
                            }

                            if (needRemoveList.isNotEmpty()) {
                                msg.removeAll(needRemoveList.toSet())
                            }

                            for (msgRev in msgRevListeners) {
                                msgRev.onOfflineRev(msg)
                            }
                        }

                        TypeConst.socket_event_type_apply_friend -> {
                            // 只有请求的时候发消息，同意/拒绝的时候没有消息
                            val msg: UserBean.FriendApply = GlobalGsonUtils.fromJson(jsonMsg, object: TypeToken<UserBean.FriendApply>(){}.type)

                            for (msgRev in msgRevListeners) {
                                msgRev.onFriendApply(msg)
                            }
                        }

                        TypeConst.socket_event_type_talk_read -> {
//                            if (jData is String) {
//                                val jsonMsg = SoChatEncryptUtil.decrypt(jData)
//                                val chatMsg = GlobalGsonUtils.fromJson<ChatMessageModel<Any>>(jsonMsg, object : TypeToken<ChatMessageModel<Any>>(){}.type)
//                                jsonObject.put("data",chatMsg)
//                            }
//
//                            val msg: UserBean.MsgReadRes = GlobalGsonUtils.fromJson(GlobalGsonUtils.toJson(jsonObject), object: TypeToken<UserBean.MsgReadRes>(){}.type)
//                            updateReadMsgDb(msg)
//
//                            for (msgRev in msgRevListeners) {
//                                msgRev.onMsgRead(msg)
//                            }
                        }

                        TypeConst.socket_event_online_state_change -> {
                            val msg: UserBean.OnLineChangeMsg = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<UserBean.OnLineChangeMsg>(){}.type)

                            for (msgRev in msgRevListeners) {
                                msgRev.onlineStateChangeRev(msg)
                            }
                        }

                        TypeConst.socket_event_type_remote_close -> {
                            closeHeartCountDown()
                            socketClient?.close()
                            connect()
                        }

                        else -> {}
                    }

                }
            } catch (e: Exception) {
                Log.e("weebsocket", "onMessageRev e = " + e.message)
            }
        }
    }

    private val socketStateListener = object : SocketStateListener {
        override fun onOpen() {
            if (heartTimer == null) {
                heartTimer = Timer()
            } else {
                heartTimer?.cancel()
            }

            heartTimer?.schedule(object : TimerTask() {
                override fun run() {
                    sendMessage(GlobalGsonUtils.toJson(SocketBean.HeartMsg("heart", TypeConst.socket_event_type_heart)))
                }

            }, 0, 5 * 1000)
//            Log.i("websocket", "socket 打开 开始计时")
            startHeartCountDown()
        }

        override fun onError(isNormalClose: Boolean) {
//            Log.i("websocket", "socket 关闭 停止计时")
            closeHeartCountDown()
        }

    }

    companion object {
        val instance: MSocket by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MSocket()
        }
    }

    fun setServer(server: String, socketConnectListener: SocketStateListener? = null): MSocketClient {
        if (socketClient == null) {
            socketClient = MSocketClient(server, msgRevListener, socketStateListener, socketConnectListener)
        }

        return socketClient!!
    }

    fun setServerAlways(server: String, socketConnectListener: SocketStateListener? = null): MSocketClient {
        socketClient?.close()
        socketClient = MSocketClient(server, msgRevListener, socketStateListener, socketConnectListener)

        return socketClient!!
    }

    fun registerMsgRevListener(listener: MessageRevListener) {
        msgRevListeners.add(listener)
    }

    fun unRegisterMsgRevListener(listener: MessageRevListener) {
        msgRevListeners.remove(listener)
    }

    private fun startHeartCountDown() {
        timeCount = TimeSecondCount(object : TimeSecondCount.OnTimerChangeListener {
            override fun onTimeChanged(second: Int) {
//                Log.i("websocket", "开始计时时间 scecond = $second")
                if (second >= HEART_TIME_OUT) {
                    heartTimer?.cancel()
                    heartTimer = null

                    socketClient?.close()
                    connect()
                }
            }
        })

        timeCount?.startTimer()
    }

    private fun closeHeartCountDown() {
        timeCount?.closeTimer()
        heartTimer?.cancel()
        heartTimer = null
    }

    fun getSessionId(msg: ChatMessageModel<Any>): String {
        val id = if (msg.to_uid == ApplicationConst.getUserId()) { msg.from_uid } else { msg.to_uid }
        val sessionId: String = when(msg.talk_type) {
            TypeConst.talk_type_single_chat -> "s$id"
            TypeConst.talk_type_group_chat -> "g$id"
            else -> id.toString()
        }
        return sessionId
    }

    fun getSessionId(fromUid: String, toUid: String, talkType: Int): String {
        val id = if (toUid == ApplicationConst.getUserId()) fromUid else toUid
        val sessionId: String = when(talkType) {
            TypeConst.talk_type_single_chat -> "s$id"
            TypeConst.talk_type_group_chat -> "g$id"
            else -> id.toString()
        }
        return sessionId
    }

    fun insertRevMsgDb(msg: ChatMessageModel<Any>) {
        val extraInfo = if (msg.extra != null) GlobalGsonUtils.toJson(msg.extra) else ""
        val dbMsg = convertMsgToDbBean( msg, extraInfo)
        CoroutineScope(Dispatchers.IO).launch {
            DbManager.getSoChatDB().msgDao().insert(dbMsg)
        }
    }

    fun insertRevMsgDb(msg: ChatMessageModel<Any>, uuid: String?) {
        val extraInfo = if (msg.extra != null) GlobalGsonUtils.toJson(msg.extra) else ""
        val dbMsg = convertMsgToDbBean( msg, extraInfo)
        CoroutineScope(Dispatchers.IO).launch {
            if (!TextUtils.isEmpty(uuid)) {
                val msg = DbManager.getSoChatDB().msgDao().getMsgById(uuid!!)
                if (msg != null) {
                    dbMsg.timestamp = msg.timestamp
                    DbManager.getSoChatDB().msgDao().delete(msg)
                }
            }
            DbManager.getSoChatDB().msgDao().insert(dbMsg)
        }
    }

    fun updateRevMsgRevokeDb(msgId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val msg = DbManager.getSoChatDB().msgDao().getMsgById(msgId)
            if (msg != null) {
                val tips = getRevokeMsgTip(msg.talk_type, msg.from_uid.toString())
                msg.is_revoke = TypeConst.type_yes
                msg.message = tips
                DbManager.getSoChatDB().msgDao().update(msg)

                val chatBean = DbManager.getSoChatDB().chatListDao().getItemByMessageId(msgId)
                if (chatBean != null) {
                    chatBean.message = tips
                    DbManager.getSoChatDB().chatListDao().update(chatBean)
                }
            }
        }
    }

    fun updateReadMsgDb(readMsg: UserBean.MsgReadRes) {
        if (readMsg.message_ids.isNullOrEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            DbManager.getSoChatDB().msgDao().updateSendMsgReadList(readMsg.message_ids)
        }
    }

    suspend fun getRevokeMsgTip(talkType: Int, fromId: String): String {
        val tips = if (fromId == ApplicationConst.getUserId()) {
            StringUtils.getString(R.string.revoke_msg_you)
        } else {
            if (talkType == TypeConst.talk_type_single_chat) {
                StringUtils.getString(R.string.revoke_msg_other)
            } else {
                String.format(StringUtils.getString(R.string.revoke_msg_who), UserSimpleDataHelper.getUserInfo("s$fromId")?.nick_name)
            }
        }
        return tips
    }

    /**
     * BaseMsg转换成DbMsg
     */
    fun convertMsgToDbBean(msg: ChatMessageModel<Any>, extraInfo: String?): DBEntity.ChatMessageEntity{
        return DBEntity.ChatMessageEntity(msg.id, msg.session_id, msg.from_uid.toInt(), msg.to_uid.toInt(), msg.talk_type, msg.is_read
            , msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users, msg.message, msg.timestamp, extraInfo
            , msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    /**
     * 消息回执
     */
    fun sendMsgReceipt(msgId: String) {
        sendMessage(GlobalGsonUtils.toJson(SocketBean.SendMsgReceipt(msgId)))
    }

    /**
     * 发送消息
     */
    fun sendMessage(message: String) {
        if (socketClient == null || socketClient?.isOpen != true) {
//            ToastUtils.show(Applications.getCurrent().getString(R.string.please_check_network))
            return
        }
        try {
            socketClient?.send(message)
        } catch (e: Exception) {
        }
    }

    fun connect() {
        if (instance.socketClient == null) return
        instance.socketClient?.let {
            if (!it.isOpen) {
                if (it.readyState == ReadyState.NOT_YET_CONNECTED) {
                    try {
                        it.connect()
                    } catch (e: java.lang.Exception) {
                    }
                } else if (it.readyState == ReadyState.CLOSING || it.readyState == ReadyState.CLOSED) {
                    try {
                        it.reconnect()
                    } catch (e: java.lang.Exception) {}
                }
            }
        }
    }


}

class MSocketClient(serverUri: String?, val listener: MsgRevListener?, private val socketStateListener: SocketStateListener?
        , private val socketConnectListener: SocketStateListener?) : WebSocketClient(URI(serverUri)) {
    private val TAG = "websocket"

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d(TAG, "-------onOpen-------")
        socketStateListener?.onOpen()
        socketConnectListener?.onOpen()
    }

    override fun onMessage(message: String?) {
        message?.let {
            if (!it.contains("heartbeat"))
                Log.d(TAG, "-------onMessage-------message: $message")
        }
        listener?.onMessageRev(message)
    }

    override fun onMessage(bytes: ByteBuffer?) {
        Log.d(TAG, "-------onMessage-------message byte: $bytes")
        listener?.onMessageRev(bytes)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(TAG, "-------onClose-------code: $code, reson: $reason, remote: $remote")
        socketStateListener?.onError(true)
        socketConnectListener?.onError(true)
    }

    override fun onError(ex: Exception?) {
        Log.d(TAG, "-------onError------- $ex")
        socketStateListener?.onError(false)
        socketConnectListener?.onError(false)
    }

}

interface MsgRevListener {
    fun onMessageRev(bytes: ByteBuffer?)

    fun onMessageRev(message: String?)
}
interface MessageRevListener {
    /**
     * 聊天消息
     */
    fun onMessageRev(message: ChatMessageModel<Any>, jsonMsg: String) {}

    /**
     * 文字信息
     */
    fun onMessageTextRev(message: ChatMessageModel<BaseMsgContent>) {}

    /**
     * 文件信息
     */
    fun onMessageFileRev(message: ChatMessageModel<FileMsgContent>) {}

    /**
     * 转发消息
     */
    fun onMessageForwardRev(message: ChatMessageModel<ForwardMsgContent>){}

    /**
     * 群操作信息
     */
    fun onMessageGroupRev(message: ChatMessageModel<GroupMsgContent>) {}

    /**
     * 新朋友消息
     */
    fun onMessageNewFriendRev(message: ChatMessageModel<NewFriendContent>) {}

    /**
     * 通话状态消息
     */
    fun onMessageCallStateRev(callStateMsg: ChatMessageModel<CallStateContent>) {}

    /**
     * 离线消息
     */
    fun onOfflineRev(messageList: List<ChatMessageModel<Any>>) {}

    /**
     * 撤回消息
     */
    fun onRevokeRev(revokeMsg: SimpleMessage){}

    /**
     * 好友申请消息
     */
    fun onFriendApply(message: UserBean.FriendApply) {}

    /**
     * 消息已读回执
     */
    fun onMsgRead(message: UserBean.MsgReadRes){}

    /**
     * 在线状态变更
     */
    fun onlineStateChangeRev(message: UserBean.OnLineChangeMsg) {}

}
interface SocketStateListener {
    fun onOpen()

    fun onError(isNormalClose: Boolean)
}