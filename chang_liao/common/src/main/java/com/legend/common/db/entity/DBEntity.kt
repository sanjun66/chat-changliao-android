package com.legend.common.db.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.legend.common.TypeConst

object DBEntity {
    val notShowUnReadNum = 0
    val showUnReadNum = 1

    /**
     * 聊天消息实体
     * 与ChatMessageModel<T>辉映
     */
    @Entity(tableName = "chat_message")
    data class ChatMessageEntity(
        @PrimaryKey var id: String,
        var session_id: String,
        var from_uid: Int,
        var to_uid: Int,
        var talk_type: Int,
        var is_read:Int,
        var is_revoke: Int,
        var quote_id: Int,
        var message_type: Int,
        var warn_users: String,
        var message: String,
//        var created_at: Long,
        var timestamp: Long,
        var extra_str: String?,     // 必须指定类型
        var send_status: Int? = TypeConst.msg_send_status_sent,
        var rev_status: Int? = TypeConst.msg_rev_status_accept,
        var is_secret: Boolean? = false,
        var pwd: String? = ""
        )

    /**
     * 会话列表消息实体
     */
    @Entity(tableName = "chat_list")
    class ChatListEntity(
        @PrimaryKey var session_id: String,    // 对方s+uid 或者是 g+groupId
        var from_uid: Int,
        var to_uid: Int,                      // 对方id 可能是单聊是对方uid，群聊是groupId
        var nick_name: String,
        var avatar: String,
        var is_read: Int,
        var show_unread_num: Int = 0,       // 0 不显示， 1 显示
        var unread_num: Int = 0,            // 未读消息数
        var message: String,                // 最后一条消息展示
        var message_id: String,
//        var created_at: Long,
        var timestamp: Long,
        var extra_info: String? = "",
        var is_secret: Boolean = false,
        var show_mention_tip: Boolean = false,   // 是否显示有人@我
        var is_disturb: Int = 0,          // 是否免到扰
        var message_type: Int = 0
    )

    /**
     * 用户信息
     * UserAvatarEntity
     */
    @Entity(tableName = "user_simple_info")
    data class UserSimpleInfo(
        @PrimaryKey var uid: String,      // 个人用s+id， 群组 g+id
        var avatar: String? = "",
        var nick_name: String? = "",
        var quickblox_id: String? = "",
        var is_disturb: Int = 0,    // 是否免打扰
        var name: String? = "",      // 预留字段
        var extra: String? = ""      // 预留字段
        ): Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(uid)
            parcel.writeString(avatar)
            parcel.writeString(nick_name)
            parcel.writeString(quickblox_id)
            parcel.writeInt(is_disturb)
            parcel.writeString(name)
            parcel.writeString(extra)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<UserSimpleInfo> {
            override fun createFromParcel(parcel: Parcel): UserSimpleInfo {
                return UserSimpleInfo(parcel)
            }

            override fun newArray(size: Int): Array<UserSimpleInfo?> {
                return arrayOfNulls(size)
            }
        }

    }


}