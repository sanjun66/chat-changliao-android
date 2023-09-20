package com.legend.common.utils

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.StringUtils
import com.legend.common.ApplicationConst
import com.legend.common.TypeConst
import com.legend.common.bean.*
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.DBEntity
import com.legend.common.socket.MSocket
import com.legend.commonres.R
import org.json.JSONObject

object ChatDataConvertUtil {
    fun dbDataToUiMessage(src: List<DBEntity.ChatMessageEntity>): MutableList<UiMessage> {
        val uiMsgList = mutableListOf<UiMessage>()
        for (data in src) {
            when(data.message_type) {
                TypeConst.chat_msg_type_text -> {
                    val textMsg = dbMsgConvertTextMsg(data)
                    uiMsgList.add(UiMessage(textMsg))
                }

                TypeConst.chat_msg_type_group_op -> {
                    val joinGroupMsg = dbMsgConvertGroupOpMsg(data)
                    uiMsgList.add(UiMessage(joinGroupMsg))
                }

                TypeConst.chat_msg_type_audio_call_state, TypeConst.chat_msg_type_video_call_state -> {
                    val callStateMsg = dbMsgConvertCallStateMsg(data)
                    uiMsgList.add(UiMessage(callStateMsg))
                }

                TypeConst.chat_msg_type_file -> {
                    val fileMsg = dbMsgConvertFileMsg(data)
                    when(fileMsg.extra.type) {
                        TypeConst.chat_msg_type_file_sub_pic -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_pic
                        TypeConst.chat_msg_type_file_sub_file -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_file
                        TypeConst.chat_msg_type_file_sub_video -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_video
                        TypeConst.chat_msg_type_file_sub_audio -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_voice
                    }
                    uiMsgList.add(UiMessage(fileMsg))
                }

                TypeConst.chat_msg_type_forward -> {
                    val forwardMsg = dbMsgConvertForwardMsg(data)
                    uiMsgList.add(UiMessage(forwardMsg))
                }

                TypeConst.chat_msg_type_new_friend -> {
                    val newFriendMsg = dbMsgConvertNewFriendMsg(data)
                    uiMsgList.add(UiMessage(newFriendMsg))
                }
            }
        }

        return uiMsgList
    }

    fun chatMsgModelAnyForwardToUiMsg(src: List<ChatMessageModel<Any>>, sessionId: String): MutableList<UiMessage> {
        val uiMsgList = mutableListOf<UiMessage>()
        for (data in src) {
            when(data.message_type) {
                TypeConst.chat_msg_type_text -> {
                    val chatTextMsg = chatMsgModelConvertTextMsg(data)
                    val textMsg = chatTextMsgConvertTextMsg(chatTextMsg)
                    if (!TextUtils.isEmpty(sessionId)) textMsg.session_id = sessionId
                    uiMsgList.add(UiMessage(textMsg))
                }

                TypeConst.chat_msg_type_file -> {
                    val fileChatMsg = chatMsgModelConvertFileMsg(data)
                    val fileMsg = chatFileMsgConvertFileMsg(fileChatMsg)
                    if (!TextUtils.isEmpty(sessionId)) fileMsg.session_id = sessionId
                    uiMsgList.add(UiMessage(fileMsg))
                }
            }
        }

        return uiMsgList
    }

    fun chatMsgModelAnyToUiMsg(src: List<ChatMessageModel<Any>>, sessionId: String, sendStatus: Int? = null): MutableList<UiMessage> {
        val uiMsgList = mutableListOf<UiMessage>()
        for (data in src) {
            when(data.message_type) {
                TypeConst.chat_msg_type_text -> {
                    val chatTextMsg = chatMsgModelConvertTextMsg(data)
                    val textMsg = chatTextMsgConvertTextMsg(chatTextMsg)
                    sendStatus?.let { textMsg.sendStatus = sendStatus }
                    if (!TextUtils.isEmpty(sessionId)) textMsg.session_id = sessionId
                    uiMsgList.add(UiMessage(textMsg))
                }

                TypeConst.chat_msg_type_file -> {
                    val fileChatMsg = chatMsgModelConvertFileMsg(data)
                    val fileMsg = chatFileMsgConvertFileMsg(fileChatMsg)
                    sendStatus?.let { fileMsg.sendStatus = sendStatus }
                    if (!TextUtils.isEmpty(sessionId)) fileMsg.session_id = sessionId
                    uiMsgList.add(UiMessage(fileMsg))
                }

                TypeConst.chat_msg_type_forward -> {
                    val forwardChatMsg = chatMsgModelConvertForwardMsg(data)
                    val forwardMsg = chatForwardMsgConvertForwardMsg(forwardChatMsg)
                    sendStatus?.let { forwardChatMsg.sendStatus = sendStatus }
                    if (!TextUtils.isEmpty(sessionId)) forwardMsg.session_id = sessionId
                    uiMsgList.add(UiMessage(forwardMsg))
                }

                TypeConst.chat_msg_type_audio_call_state, TypeConst.chat_msg_type_video_call_state -> {
                    val callStateContent = chatMsgModelConvertCallStateMsg(data)
                    val callStateMsg = chatCallStateMsgConvertCallStateMsg(callStateContent)
                    callStateMsg.extra.sendMsg = ChatUtil.getCallStateMsg(callStateMsg.isSender, callStateMsg.extra.state?:"0", callStateMsg.extra.duration?:0)
                    sendStatus?.let { callStateMsg.sendStatus = sendStatus }
                    if (!TextUtils.isEmpty(sessionId)) callStateMsg.session_id = sessionId
                    uiMsgList.add(UiMessage(callStateMsg))
                }

            }
        }

        return uiMsgList
    }

    /**
     * 兼容之前版本
     */
    private fun getdbSendStatus(sendStatus: Int?): Int {
        if (sendStatus == null || sendStatus == 0) return TypeConst.msg_send_status_sent

        return sendStatus
    }

    fun chatModelToUiMessage(src: List<ChatMessageModel<Any>>): MutableList<UiMessage>{
        val uiMsgList = mutableListOf<UiMessage>()
        for (data in src) {
            when(data.message_type) {
                TypeConst.chat_msg_type_text -> {
                    val textMsg = chatMsgModelConvertTextMsg(data)
                    textMsg.sendStatus = TypeConst.msg_send_status_sent
                    uiMsgList.add(UiMessage(textMsg))
                }

                TypeConst.chat_msg_type_file -> {
                    val fileMsg = chatMsgModelConvertFileMsg(data)
                    fileMsg.sendStatus = TypeConst.msg_send_status_sent
                    when(fileMsg.extra.type) {
                        TypeConst.chat_msg_type_file_sub_pic -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_pic
                        TypeConst.chat_msg_type_file_sub_file -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_file
                        TypeConst.chat_msg_type_file_sub_video -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_video
                        TypeConst.chat_msg_type_file_sub_audio -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_voice
                    }
                    uiMsgList.add(UiMessage(fileMsg))
                }
            }
        }

        return uiMsgList
    }

    fun dbMsgConvertTextMsg(src: DBEntity.ChatMessageEntity): ChatMessageModel<BaseMsgContent> {
        val msg = ChatMessageModel<BaseMsgContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , BaseMsgContent(src.message)
            , getdbSendStatus(src.send_status), src.rev_status?:0, src.is_secret?:false, src.pwd)
        return TextMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users
            , msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    fun dbMsgConvertGroupOpMsg(src: DBEntity.ChatMessageEntity): ChatMessageModel<GroupMsgContent> {
        val groupMsgContent: GroupMsgContent = GlobalGsonUtils.fromJson(src.extra_str, object : TypeToken<GroupMsgContent>(){}.type)
        val msg = ChatMessageModel<GroupMsgContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , groupMsgContent
            , getdbSendStatus(src.send_status), src.rev_status?:0, src.is_secret?:false, src.pwd)
        return  GroupOpMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users
            , msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    fun dbMsgConvertNewFriendMsg(src: DBEntity.ChatMessageEntity): ChatMessageModel<NewFriendContent> {
        val newFriendContent: NewFriendContent = GlobalGsonUtils.fromJson(src.extra_str, object : TypeToken<NewFriendContent>(){}.type)
        val msg = ChatMessageModel<NewFriendContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , newFriendContent
            , getdbSendStatus(src.send_status), src.rev_status?:0, src.is_secret?:false, src.pwd)
        return  NewFriendMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users
            , msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    fun dbMsgConvertCallStateMsg(src: DBEntity.ChatMessageEntity): CallStateMsg {
        val callStateContent: CallStateContent = GlobalGsonUtils.fromJson(src.extra_str, object : TypeToken<CallStateContent>(){}.type)
        val msg = ChatMessageModel<CallStateContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , callStateContent
            , getdbSendStatus(src.send_status), src.rev_status?:0, src.is_secret?:false, src.pwd)

        val isVideo = msg.message_type == TypeConst.chat_msg_type_video_call_state
        msg.session_id = MSocket.instance.getSessionId(msg.from_uid, msg.to_uid, msg.talk_type)
        msg.revStatus = TypeConst.msg_rev_status_accept
        msg.sendStatus = TypeConst.msg_send_status_sent
        msg.message = if (isVideo) StringUtils.getString(R.string.msg_sub_call_video) else StringUtils.getString(R.string.msg_sub_call_audio)
        msg.extra.sendMsg = ChatUtil.getCallStateMsg(msg.isSender, msg.extra.state?:"", msg.extra.duration?:0)

        return CallStateMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users
            , msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    fun textMsgConvertChatModel(src: TextMsg): ChatMessageModel<Any> {
        return ChatMessageModel(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra as Any, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }

    fun fileMsgCovertChatModel(src: FileMsg): ChatMessageModel<Any> {
        return ChatMessageModel(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra as Any, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }

    fun dbMsgConvertFileMsg(src: DBEntity.ChatMessageEntity): ChatMessageModel<FileMsgContent> {
        val fileImageContent: FileMsgContent = GlobalGsonUtils.fromJson(src.extra_str, object : TypeToken<FileMsgContent>(){}.type)
        val msg = ChatMessageModel<FileMsgContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , fileImageContent
            , getdbSendStatus(src.send_status), src.rev_status?:0, src.is_secret?:false, src.pwd)
        return FileMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type
            , msg.warn_users, msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)

    }

    fun dbMsgConvertForwardMsg(src: DBEntity.ChatMessageEntity): ChatMessageModel<ForwardMsgContent> {
        val forwardMsgContent: ForwardMsgContent = GlobalGsonUtils.fromJson(src.extra_str, object : TypeToken<ForwardMsgContent>(){}.type)
        val msg = ChatMessageModel<ForwardMsgContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , forwardMsgContent
            , getdbSendStatus(src.send_status), src.rev_status?:0, src.is_secret?:false, src.pwd)
        return ForwardMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type
            , msg.warn_users, msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)

    }

    /**
     * 接收到的消息转成db消息
     */
    fun chatMsgModelConvertDb(src: ChatMessageModel<Any>): DBEntity.ChatMessageEntity {
        return DBEntity.ChatMessageEntity(src.id, src.session_id, src.from_uid.toInt(), src.to_uid.toInt(), src.talk_type, src.is_read, src.is_revoke, src.quote_id
            , src.message_type, src.warn_users, src.message, src.timestamp, if (src.extra == null) "" else GlobalGsonUtils.toJson(src.extra)
            , src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }


    /**
     * 转换成对应messageContent
     */
    fun chatMsgModelConvertBaseMsgContent(src: ChatMessageModel<Any>): ChatMessageModel<BaseMsgContent> {
        return ChatMessageModel<BaseMsgContent>(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id
            , src.message_type, src.warn_users, src.message, src.timestamp, src.uuid, BaseMsgContent(src.message)
            , src.sendStatus?:0, src.revStatus?:0, src.is_secret, src.pwd)
    }

    /**
     * 转换成文字消息
     */
    fun chatMsgModelConvertTextMsg(src: ChatMessageModel<Any>): ChatMessageModel<BaseMsgContent> {
        val msg = ChatMessageModel<BaseMsgContent>(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id
            , src.message_type, src.warn_users, src.message, src.timestamp, src.uuid, BaseMsgContent(src.message), src.sendStatus, src.revStatus, src.is_secret, src.pwd)
        return TextMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users
            , msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    fun chatMsgModelConvertForwardMsg(src: ChatMessageModel<Any>): ChatMessageModel<ForwardMsgContent> {
        val extraJson = GlobalGsonUtils.toJson(src.extra)
        val forwardMsgContent: ForwardMsgContent = GlobalGsonUtils.fromJson(extraJson, object : TypeToken<ForwardMsgContent>(){}.type)
        val msg = ChatMessageModel<ForwardMsgContent>(src.id, getSessionId(src.from_uid.toString(), src.to_uid.toString(), src.talk_type), src.from_uid.toString(), src.to_uid.toString(), src.talk_type
            , src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users, src.message, src.timestamp,"" , forwardMsgContent
            , src.sendStatus, src.revStatus, src.is_secret, src.pwd)
        return ForwardMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type
            , msg.warn_users, msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
    }

    fun chatMsgModelConvertCallStateMsg(src: ChatMessageModel<Any>, uuid: String? = null): ChatMessageModel<CallStateContent> {
        var extraJson = GlobalGsonUtils.toJson(src)
        val callStateMsg: ChatMessageModel<CallStateContent> = GlobalGsonUtils.fromJson(extraJson, object : TypeToken<ChatMessageModel<CallStateContent>>(){}.type)
        val isVideo = src.message_type == TypeConst.chat_msg_type_video_call_state
        callStateMsg.session_id = MSocket.instance.getSessionId(src)
        uuid?.let { callStateMsg.uuid = uuid }
        callStateMsg.revStatus = TypeConst.msg_rev_status_accept
        callStateMsg.sendStatus = TypeConst.msg_send_status_sent
        callStateMsg.message = if (isVideo) StringUtils.getString(R.string.msg_sub_call_video) else StringUtils.getString(R.string.msg_sub_call_audio)
        callStateMsg.extra.sendMsg = ChatUtil.getCallStateMsg(callStateMsg.isSender, callStateMsg.extra.state?:"0", callStateMsg.extra.duration?:0)
        return callStateMsg
    }

    fun chatMsgModelConvertFileMsg(src: ChatMessageModel<Any>): ChatMessageModel<FileMsgContent> {
        val extraJson = GlobalGsonUtils.toJson(src.extra)
        val fileImageContent: FileMsgContent = GlobalGsonUtils.fromJson(extraJson, object : TypeToken<FileMsgContent>(){}.type)
        val msg = ChatMessageModel<FileMsgContent>(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id
            , src.message_type, src.warn_users, src.message, src.timestamp, src.uuid, fileImageContent, src.sendStatus, src.revStatus, src.is_secret, src.pwd)

        val fileMsg = FileMsg(msg.id, msg.session_id, msg.from_uid, msg.to_uid, msg.talk_type, msg.is_read, msg.is_revoke, msg.quote_id, msg.message_type, msg.warn_users
            , msg.message, msg.timestamp, msg.uuid, msg.extra, msg.sendStatus, msg.revStatus, msg.is_secret, msg.pwd)
        when(fileMsg.extra.type) {
            TypeConst.chat_msg_type_file_sub_pic -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_pic
            TypeConst.chat_msg_type_file_sub_file -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_file
            TypeConst.chat_msg_type_file_sub_video -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_video
            TypeConst.chat_msg_type_file_sub_audio -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_voice
        }
        return fileMsg
    }

    fun chatTextMsgConvertTextMsg(src: ChatMessageModel<BaseMsgContent>): ChatMessageModel<BaseMsgContent> {
        return TextMsg(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }

    fun chatFileMsgConvertFileMsg(src: ChatMessageModel<FileMsgContent>): ChatMessageModel<FileMsgContent> {
        val fileMsg =  FileMsg(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
        fileMsg.message_local_type = src.message_local_type
        return fileMsg
    }

    fun chatForwardMsgConvertForwardMsg(src: ChatMessageModel<ForwardMsgContent>): ChatMessageModel<ForwardMsgContent> {
        val forwardMsg = ForwardMsg(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
        forwardMsg.message = StringUtils.getString(R.string.msg_sub_history)
        return forwardMsg
    }

    fun chatGrouOpMsgConvertGroupOpMsg(src: ChatMessageModel<GroupMsgContent>): ChatMessageModel<GroupMsgContent> {
        return GroupOpMsg(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }

    fun chatCallStateMsgConvertCallStateMsg(src: ChatMessageModel<CallStateContent>): CallStateMsg {
        return CallStateMsg(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }

    fun chatNewFriendMsgConvertNewFriendMsg(src: ChatMessageModel<NewFriendContent>): NewFriendMsg {
        return NewFriendMsg(src.id, src.session_id, src.from_uid, src.to_uid, src.talk_type, src.is_read, src.is_revoke, src.quote_id, src.message_type, src.warn_users
            , src.message, src.timestamp, src.uuid, src.extra, src.sendStatus, src.revStatus, src.is_secret, src.pwd)
    }

    fun sendMsgToUiMessage(msgId: String, createAt: Long, src: SendMessage, isSecret: Boolean, pwd: String): UiMessage {
        if (src.message_type == TypeConst.chat_msg_type_text) {
            val textMsg = TextMsg(msgId, getSessionId(ApplicationConst.getUserId(), src.to_uid, src.talk_type), ApplicationConst.getUserId(), src.to_uid, src.talk_type
                , 0, 0, 0, src.message_type, src.warn_users, src.message, createAt
                , src.uuid, BaseMsgContent(src.message), TypeConst.msg_send_status_sending, TypeConst.msg_rev_status_accept, isSecret, pwd
            )

            return UiMessage(textMsg)
        } else {
            val fileMsgContent: FileMsgContent = GlobalGsonUtils.fromJson(src.extra, object : TypeToken<FileMsgContent>(){}.type)
            val fileMsg = FileMsg(msgId, getSessionId(ApplicationConst.getUserId(), src.to_uid, src.talk_type), ApplicationConst.getUserId(), src.to_uid, src.talk_type, 0, 0, 0
                , TypeConst.chat_msg_type_file, src.warn_users, src.message, System.currentTimeMillis(), src.uuid, fileMsgContent, TypeConst.msg_send_status_sending, TypeConst.msg_rev_status_accept
                , isSecret, pwd)
            when(fileMsgContent.type) {
                TypeConst.chat_msg_type_file_sub_pic -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_pic
                TypeConst.chat_msg_type_file_sub_file -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_file
                TypeConst.chat_msg_type_file_sub_video -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_video
                TypeConst.chat_msg_type_file_sub_audio -> fileMsg.message_local_type = TypeConst.chat_msg_type_file_voice
            }

            return UiMessage(fileMsg)
        }

    }

    fun uiMessageToSendMsg(src: UiMessage): SendMessage {
        return SendMessage(src.message.to_uid, src.message.message, src.message.warn_users, src.message.talk_type, src.message.message_type, src.message.uuid, src.message.is_secret, src.message.pwd, if (src.message.message_type == TypeConst.chat_msg_type_text) "" else GlobalGsonUtils.toJson(src.message.extra))
    }

    fun getSessionId(fromUid: String, toUid: String, talkType: Int): String {
        val otherId = if (fromUid == ApplicationConst.getUserId()) toUid else fromUid
         return if (talkType == TypeConst.talk_type_group_chat) {
            return "g$otherId"
        } else {
            return "s$otherId"
        }
    }

    fun getChatListShowMsg(message: String, msgType: Int, isSecret: Boolean): String {
        if (isSecret) return StringUtils.getString(R.string.msg_sub_secret_msg)
        if (!TextUtils.isEmpty(message)) return message
        return getSubMessageByMsgType(msgType)
    }
    fun getSubMessageByMsgType(msgType: Int) : String {
        var subMsg = ""
        when(msgType) {
            TypeConst.chat_msg_type_forward -> subMsg = StringUtils.getString(R.string.msg_sub_history)
            TypeConst.chat_msg_type_audio_call_state -> subMsg = StringUtils.getString(R.string.msg_sub_call_audio)
            TypeConst.chat_msg_type_video_call_state -> subMsg = StringUtils.getString(R.string.msg_sub_call_video)
        }
        return subMsg
    }

}

object RawMsgConvertUtil {
    fun msgToTextChatMsgModel(jsonMsg: String, uuid: String): ChatMessageModel<BaseMsgContent> {
        val destMsg: ChatMessageModel<BaseMsgContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<BaseMsgContent>>(){}.type)
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept
        destMsg.extra.sendMsg = destMsg.message

        return destMsg
    }

    fun msgToForwardChatMsgModel(jsonMsg: String, uuid: String): ChatMessageModel<ForwardMsgContent> {
        val destMsg: ChatMessageModel<ForwardMsgContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<ForwardMsgContent>>(){}.type)
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept
        destMsg.sendStatus = TypeConst.msg_send_status_sent
        destMsg.message = StringUtils.getString(R.string.msg_sub_history)

        return destMsg
    }

    fun msgToNewFriendChatModel(jsonMsg: String, uuid: String): ChatMessageModel<NewFriendContent>? {
        val destMsg: ChatMessageModel<NewFriendContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<NewFriendContent>>(){}.type)
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept
        destMsg.sendStatus = TypeConst.msg_send_status_sent

        if (destMsg.extra.state == "2") {
            destMsg.message = StringUtils.getString(R.string.new_friend_msg_tip)
            return destMsg
        }

        return null
    }

    fun msgToFileChatModel(jsonMsg: String, uuid: String): ChatMessageModel<FileMsgContent> {
        val destMsg: ChatMessageModel<FileMsgContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<FileMsgContent>>(){}.type)
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept

        when(destMsg.extra.type) {
            TypeConst.chat_msg_type_file_sub_pic -> destMsg.message_local_type = TypeConst.chat_msg_type_file_pic
            TypeConst.chat_msg_type_file_sub_file -> destMsg.message_local_type = TypeConst.chat_msg_type_file_file
            TypeConst.chat_msg_type_file_sub_video -> destMsg.message_local_type = TypeConst.chat_msg_type_file_video
            TypeConst.chat_msg_type_file_sub_audio -> destMsg.message_local_type = TypeConst.chat_msg_type_file_voice
        }

        return destMsg
    }

    fun msgToGroupChatModel(jsonMsg: String, uuid: String): ChatMessageModel<GroupMsgContent> {
        val destMsg:ChatMessageModel<GroupMsgContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<GroupMsgContent>>(){}.type)
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept
        destMsg.sendStatus = TypeConst.msg_send_status_sent
        var optionedUsers = ""
        destMsg.extra.users?.let { it1 ->
            for ((index, user) in it1.withIndex()) {
                optionedUsers += user.nick_name
                if (index < it1.size - 1) {
                    optionedUsers = "$optionedUsers、"
                }
            }
        }
        destMsg.message = ChatUtil.getGroupOpMsg(destMsg.extra?.type?:0, destMsg.extra.operate_user_id.toString() == ApplicationConst.getUserId(), optionedUsers, destMsg.extra.operate_user_name?:"")

        return destMsg
    }

    fun msgToCallStateChatModel(jsonMsg: String, uuid: String): ChatMessageModel<CallStateContent> {
        val destMsg: ChatMessageModel<CallStateContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<CallStateContent>>(){}.type)
        val isVideo = destMsg.message_type == TypeConst.chat_msg_type_video_call_state
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept
        destMsg.sendStatus = TypeConst.msg_send_status_sent
        destMsg.message = if (isVideo) StringUtils.getString(R.string.msg_sub_call_video) else StringUtils.getString(R.string.msg_sub_call_audio)
        destMsg.extra.sendMsg = ChatUtil.getCallStateMsg(destMsg.isSender, destMsg.extra.state?:"", destMsg.extra.duration?:0)

        return destMsg
    }

    fun callMsgToGroupChatModel(jsonMsg: String, uuid: String): ChatMessageModel<GroupMsgContent> {
        val callStateMsg: ChatMessageModel<CallStateContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<CallStateContent>>(){}.type)
        val isVideo = callStateMsg.message_type == TypeConst.chat_msg_type_video_call_state
        val destMsg:ChatMessageModel<GroupMsgContent> = GlobalGsonUtils.fromJson(jsonMsg, object : TypeToken<ChatMessageModel<GroupMsgContent>>(){}.type)
        destMsg.session_id = MSocket.instance.getSessionId(destMsg.from_uid, destMsg.to_uid, destMsg.talk_type)
        destMsg.uuid = uuid
        destMsg.revStatus = TypeConst.msg_rev_status_accept
        destMsg.sendStatus = TypeConst.msg_send_status_sent
        destMsg.message_type = TypeConst.chat_msg_type_group_op
        destMsg.extra.type = 1 // 保证能够每次正常显示
        destMsg.message = if (isVideo) StringUtils.getString(R.string.msg_sub_call_video) else StringUtils.getString(R.string.msg_sub_call_audio)
        destMsg.extra.sendMsg = ChatUtil.getGroupCallStateMsg(callStateMsg.extra.nickname?:"", callStateMsg.extra.state?:"0", isVideo)

        return destMsg
    }





    fun replaceDecryptedUiMsg(uiMsg: UiMessage, jsonMsg: String, uuid: String): UiMessage {
        val jData = JSONObject(jsonMsg)
        if (!jsonMsg.contains("message_type")) return uiMsg
        when (jData.get("message_type")) {
            TypeConst.chat_msg_type_text -> {
                val textMsg = msgToTextChatMsgModel(jsonMsg, uuid)
                if (textMsg.sendStatus != TypeConst.msg_send_status_failed) textMsg.sendStatus = TypeConst.msg_send_status_sent
                MSocket.instance.insertRevMsgDb(textMsg as ChatMessageModel<Any>)
                textMsg.is_secret = false
                uiMsg.message = ChatDataConvertUtil.chatTextMsgConvertTextMsg(textMsg)
                return uiMsg
            }
            TypeConst.chat_msg_type_file -> {
                val fileMsg = msgToFileChatModel(jsonMsg, uuid)
                if (fileMsg.sendStatus != TypeConst.msg_send_status_failed && fileMsg.sendStatus != TypeConst.msg_send_status_uploading) fileMsg.sendStatus = TypeConst.msg_send_status_sent
                MSocket.instance.insertRevMsgDb(fileMsg as ChatMessageModel<Any>)
                fileMsg.is_secret = false
                uiMsg.message = ChatDataConvertUtil.chatFileMsgConvertFileMsg(fileMsg)
                return uiMsg
            }
        }

        return uiMsg
    }
}