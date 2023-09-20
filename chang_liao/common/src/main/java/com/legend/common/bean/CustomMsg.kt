package com.legend.common.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.reflect.TypeToken
import com.legend.base.Applications
import com.legend.base.utils.FileUtils
import com.legend.base.utils.GlobalGsonUtils
import com.legend.common.db.entity.ChatMessageModel

/**
 * 群操作消息
 */
class GroupOpMsg: ChatMessageModel<GroupMsgContent> {
    private var joinGroup: GroupMsgContent? = null
    constructor(id: String, sessionId: String, fromUid: String, toUid: String, talkType: Int, isRead: Int, isRevoke: Int, quoteId: Int, messageType: Int, warnUsers: String
                , message: String, createAt: Long, uuid: String?, extra: GroupMsgContent?, sendStatus: Int, revStatus: Int, is_secret: Boolean, pwd: String)
            : super(id, sessionId, fromUid, toUid, talkType, isRead, isRevoke, quoteId, messageType, warnUsers, message, createAt, uuid, extra, sendStatus, revStatus, is_secret, pwd)

    override fun process(): GroupMsgContent {
        joinGroup = GlobalGsonUtils.fromJson(extra, object : TypeToken<GroupMsgContent>(){}.type)

        return joinGroup?: GroupMsgContent()
    }
}

/**
 * 新朋友通知消息
 */
class NewFriendMsg: ChatMessageModel<NewFriendContent> {
    private var newFriendContent: NewFriendContent? = null
    constructor(id: String, sessionId: String, fromUid: String, toUid: String, talkType: Int, isRead: Int, isRevoke: Int, quoteId: Int, messageType: Int, warnUsers: String
                , message: String, createAt: Long, uuid: String?, extra: NewFriendContent?, sendStatus: Int, revStatus: Int, is_secret: Boolean, pwd: String)
            : super(id, sessionId, fromUid, toUid, talkType, isRead, isRevoke, quoteId, messageType, warnUsers, message, createAt, uuid, extra, sendStatus, revStatus, is_secret, pwd)

    override fun process(): NewFriendContent {
        newFriendContent = GlobalGsonUtils.fromJson(extra, object : TypeToken<NewFriendContent>(){}.type)
        return newFriendContent?: NewFriendContent()
    }
}

/**
 * 通话状态消息
 */
class CallStateMsg: ChatMessageModel<CallStateContent> {
    private var callState: CallStateContent? = null
    constructor(id: String, sessionId: String, fromUid: String, toUid: String, talkType: Int, isRead: Int, isRevoke: Int, quoteId: Int, messageType: Int, warnUsers: String
                , message: String, createAt: Long, uuid: String?, extra: CallStateContent?, sendStatus: Int, revStatus: Int, is_secret: Boolean, pwd: String)
            : super(id, sessionId, fromUid, toUid, talkType, isRead, isRevoke, quoteId, messageType, warnUsers, message, createAt, uuid, extra, sendStatus, revStatus, is_secret, pwd)

    override fun process(): CallStateContent {
        callState = GlobalGsonUtils.fromJson(extra, object : TypeToken<CallStateContent>(){}.type)
        return callState?: CallStateContent()
    }
}

/**
 * 文字消息
 */
class TextMsg : ChatMessageModel<BaseMsgContent> {
    var baseMsgContent: BaseMsgContent? = null

    constructor(): super()
    constructor(id: String, sessionId: String, fromUid: String, toUid: String, talkType: Int, isRead: Int, isRevoke: Int, quoteId: Int, messageType: Int, warnUsers: String
                , message: String, createAt: Long, uuid: String?, extra: BaseMsgContent, sendStatus: Int, revStatus: Int, is_secret: Boolean, pwd: String)
            : super(id, sessionId, fromUid, toUid, talkType, isRead, isRevoke, quoteId, messageType, warnUsers, message, createAt, uuid, extra, sendStatus, revStatus, is_secret, pwd)
    override fun process(): BaseMsgContent {
        baseMsgContent =  GlobalGsonUtils.fromJson(
            extra,
            object : TypeToken<BaseMsgContent>() {}.type
        )
        return baseMsgContent?: BaseMsgContent()
    }
}

/**
 * 文件消息
 */
class FileMsg: ChatMessageModel<FileMsgContent> {
    var fileMsgContent: FileMsgContent? = null
    constructor(): super()
    constructor(id: String, sessionId: String, fromUid: String, toUid: String, talkType: Int, isRead: Int, isRevoke: Int, quoteId: Int, messageType: Int
                , warnUsers: String, message: String, createAt: Long, uuid: String?, extra: FileMsgContent, sendStatus: Int, revStatus: Int, is_secret: Boolean, pwd: String)
            : super(id, sessionId, fromUid, toUid, talkType, isRead, isRevoke, quoteId, messageType, warnUsers, message, createAt, uuid, extra, sendStatus, revStatus, is_secret, pwd)

    override fun process(): FileMsgContent {
        fileMsgContent = GlobalGsonUtils.fromJson(extra, object: TypeToken<FileMsgContent>(){}.type)
        return fileMsgContent ?: FileMsgContent()
    }
}

/**
 * 合并转发消息
 */
class ForwardMsg: ChatMessageModel<ForwardMsgContent> {
    private var forwardMsgContent: ForwardMsgContent? = null

    constructor(id: String, sessionId: String, fromUid: String, toUid: String, talkType: Int, isRead: Int, isRevoke: Int, quoteId: Int, messageType: Int
                , warnUsers: String, message: String, createAt: Long, uuid: String?, extra: ForwardMsgContent, sendStatus: Int, revStatus: Int, is_secret: Boolean, pwd: String)
            : super(id, sessionId, fromUid, toUid, talkType, isRead, isRevoke, quoteId, messageType, warnUsers, message, createAt, uuid, extra, sendStatus, revStatus, is_secret, pwd)
    override fun process(): ForwardMsgContent {
        forwardMsgContent = GlobalGsonUtils.fromJson(extra, object: TypeToken<ForwardMsgContent>(){}.type)
        return forwardMsgContent ?: ForwardMsgContent()
    }
}

class FileMsgContent(var driver: Int = 0, var suffix: String? = "", var original_name: String? = "", var type: Int? = 0, var size: Long? = 0, var path: String? = "", var url: String? = ""
                     , var record_id: Int? = 0, val weight: Int? = 0, val height: Int? = 0, var duration: Long? = 0, var cover: String? = "", var thumbnailPath: String? = ""): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString()
    ) {
    }
    
    fun getLocalPath(sessionId: String, fileType: Int): String {
        var fileName = FileUtils.getFileName(path)
        if (fileName.contains("iOS", true)) {
            fileName = FileUtils.getFileName(url)
        }
        return FileUtils.getAppFiles(Applications.getCurrent(), sessionId, fileType) + fileName
    }

    fun getLocalCover(sessionId: String): String {
        val fileName = FileUtils.getFileName(cover)
        return FileUtils.getAppFiles(Applications.getCurrent(), sessionId, FileUtils.FILE_TYPE_IMAGE) + fileName
    }
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(driver)
        parcel.writeString(suffix)
        parcel.writeString(original_name)
        parcel.writeValue(type)
        parcel.writeValue(size)
        parcel.writeString(path)
        parcel.writeString(url)
        parcel.writeValue(record_id)
        parcel.writeValue(weight)
        parcel.writeValue(height)
        parcel.writeValue(duration)
        parcel.writeString(cover)
        parcel.writeString(thumbnailPath)
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object CREATOR : Parcelable.Creator<FileMsgContent> {
        override fun createFromParcel(parcel: Parcel): FileMsgContent {
            return FileMsgContent(parcel)
        }
        
        override fun newArray(size: Int): Array<FileMsgContent?> {
            return arrayOfNulls(size)
        }
    }
}

open class BaseMsgContent {
    var sendMsg: String? = null
    constructor()
    constructor(sendMsg: String?){
        this.sendMsg = sendMsg
    }

}

/**
 * type: 1:入群通知;2:自动退群;3:管理员踢群;4群解散
 * operate_user_id: 谁操作
 * users: 被操作人信息
 */
data class GroupMsgContent(var sendMsg: String? = "", var type: Int? = -1, val operate_user_id: Int? = -1, val operate_user_name: String? = "", val users: List<OptionedUser>? = null)

data class CallStateContent(var sendMsg: String? = "", val state: String? = "0", val duration: Long? = 0, val user_ids: String? ="", val nickname: String? = "")

data class OptionedUser(val id: String, val nick_name: String)

data class NewFriendContent(val state: String? = "", val process_message: String? = "")

/**
 * 好友申请
 */
data class ApplyFriendContent(val from_avatar: String?, val from_nick_name: String?, val process_message: String?, val record_id: Int, val remark: String?, val state: String?, val to_avatar: String?, val to_nick_name: String?)

data class ForwardMsgContent(val talk_list: List<ChatMessageModel<Any>>?=null)