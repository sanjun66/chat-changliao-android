package com.legend.common.bean

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.legend.baseui.ui.widget.susindexbar.indexbar.bean.BaseIndexPinyinBean
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack

object UserBean {
    /**
     * 添加好友时搜索结果
     */
    data class SearchFriendRes(
        val friend_list: List<SearchFriend>
    )
    data class SearchFriend(
        val avatar: String,
        val id: String,
        val is_friend: Boolean,
        val keywords: String,
        val nick_name: String,
        val sign: String
    )

    /**
     * 好友申请列表
     * {"apply_list":[{"avatar":"https://june.hk.ufileos.com/1.png","id":13,"nick_name":"巴台甩线目娠犬稳雄潮","friend_id":28,"uid":13,"state":1,"remark":"","created_at":1686706778,"process_message":"","flag":"taker"}]}
     */
    data class ApplyList(
        val apply_list: List<FriendApply>
    )
    data class FriendApply(
        val avatar: String,
        val created_at: Long,
        val flag: String,               // 谁申请的谁是maker 审核的是taker
        val friend_id: String,             // 好友id (socket返回有时候是int 有时候是string)
        val id: Int,                    // 唯一标识
        val nick_name: String,
        val process_message: String,    // 审核信息
        val remark: String? = "",       // 备注信息
        var state: Int,                 // 1 审核 2 同意 3 不同意
        val uid: String                    // 申请人uid id 向 friend_id发起申请  (socket返回有时候是int 有时候是string)
    )

    /**
     * 已读回执消息
     */
    data class MsgReadRes (
        val code: Int,
        val data: MsgReadData,
        val event_name: String,
        val message: String,
        val message_ids: List<String>?,
        val uuid: String
    )
    data class MsgReadData(
        val from_uid: String,
        val id: String,
        val talk_type: Int,
        val to_uid: String
    )

    /**
     * socket online状态
     */
    data class OnLineChangeMsg(val extra: Extra, val from_uid: String)
    data class Extra(val type: Int)

    // 好友审核本地需要的res数据
    data class CheckApplyLocal(val id: Int, val state: Int, val position: Int)
    /**
     * 用户信息
     */
    // {"id":13,"apply_auth":1,"nick_name":"巴台甩线目娠犬稳雄潮","avatar":"https://june.hk.ufileos.com/20230601122643_d78ba4b68d8b582f13720ca9ec92ce88.png","account":"zG73yKDYKAlQYekZ","sex":0,"age":0,"address":"","sign":"","area_code":"","phone":"","email":"123456@qq.com"}
    data class UserInfo(
        val account: String,        // 账号
        val address: String,        // 地址
        val age: Int,               // 年龄
        var apply_auth: Int,        // 添加好友 1需要验证信息，0 不需要验证
        val area_code: String,      // 区号
        val avatar: String,         // 头像
        val email: String?,          // 电子邮箱
        val id: String,
        var nick_name: String,      // 昵称
        val phone: String?,          // 手机号码
        var sex: Int,               // 性别 0未知 1男 2女
        val sign: String,           // 个性签名
        var note_name: String?,     // 备注名
        var is_friend: Int,         // 是否是我的好友 0不是 1是
        var is_black: Int,          // 是否被我已经拉黑 0不是 1是
        var is_disturb: Int,        // 是否免打扰 0否 1是
        var targetId: String?,      // 仅本地使用，用于区分用户头像还是群头像
        var quickblox_id: String?,  // qbId
        var quickblox_login: String
    ) {
        fun getNickName(): String {
            return if (!TextUtils.isEmpty(note_name)) note_name!! else nick_name
        }
    }

    /**
     * 更改头像的信息
     */
    data class AvatarRes(val avatar: String?, val image_name: String?)

    /**
     * 在线状态
     */
    data class OnlineState(val online: Int)

    /**
     * 好友列表
     */
    data class FriendListBean(val friend_list: List<FriendList>)
    data class FriendList(val group_list: List<ConcatFriend>, val group_name: String)
    data class ConcatFriend(val account: String, val avatar: String, val friend_id: String, val group_id: Int,
                     val is_black: Int, val is_disturb: Int, var remark: String, val sign: String, val state: Int, val quickblox_id: String?): BaseIndexPinyinBean() {
        // 发现接口返回有的remark为空字符,方法不能直接写成getRemark
        fun getFinalRemark(): String = if (TextUtils.isEmpty(remark)) "soChatUser" else remark

        override fun getTarget(): String {
            return getFinalRemark()
        }

        var isSelected: Boolean = false

    }

    data class ConcatSimple(val avatar: String, val id: String, val nick_name: String, val quickblox_id: String?): BaseIndexPinyinBean() {

        fun getFinalNickName(): String = if (TextUtils.isEmpty(nick_name)) "null" else nick_name
        override fun getTarget(): String {
            return getFinalNickName()
        }

        var isSelected: Boolean = false
    }

    /**
     * 黑名单列表
     */
    data class FriendBlackList(val friend_black: List<FriendBlack>)
    data class FriendBlack(val avatar: String, val friend_id: String, val remark: String)

    /**
     * 群详情
     */
    data class GroupAllInfo(val group_info: GroupInfo?, val group_member: List<GroupMember>?, val group_notice: List<String>?): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(GroupInfo.javaClass.classLoader),
            parcel.createTypedArrayList(GroupMember.CREATOR),
            parcel.createStringArrayList()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(group_info, 0)
            parcel.writeTypedList(group_member)
            parcel.writeStringList(group_notice)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<GroupAllInfo> {
            override fun createFromParcel(parcel: Parcel): GroupAllInfo {
                return GroupAllInfo(parcel)
            }

            override fun newArray(size: Int): Array<GroupAllInfo?> {
                return arrayOfNulls(size)
            }
        }

    }

    // audio 总控制， is_audio 手动控制
    data class GroupInfo(val id: Int, var avatar: String?, val describe: String?, val is_dismiss: String?, var audio: Int, var is_audio: Int, var is_mute: Int
        , var is_disturb: Int, val max_num: Int, val max_manager: Int, var name: String?, val uid: String?, val manager_explain: String?): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(avatar)
            parcel.writeString(describe)
            parcel.writeString(is_dismiss)
            parcel.writeInt(audio)
            parcel.writeInt(is_audio)
            parcel.writeInt(is_mute)
            parcel.writeInt(is_disturb)
            parcel.writeInt(max_num)
            parcel.writeInt(max_manager)
            parcel.writeString(name)
            parcel.writeString(uid)
            parcel.writeString(manager_explain)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<GroupInfo> {
            override fun createFromParcel(parcel: Parcel): GroupInfo {
                return GroupInfo(parcel)
            }

            override fun newArray(size: Int): Array<GroupInfo?> {
                return arrayOfNulls(size)
            }
        }

    }

    data class GroupMember(val avatar: String?, val notes: String?, val uid: Int, var is_mute: Int, var quickblox_id: String?, var role: String?): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(avatar)
            parcel.writeString(notes)
            parcel.writeInt(uid)
            parcel.writeInt(is_mute)
            parcel.writeString(quickblox_id)
            parcel.writeString(role)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<GroupMember> {
            override fun createFromParcel(parcel: Parcel): GroupMember {
                return GroupMember(parcel)
            }

            override fun newArray(size: Int): Array<GroupMember?> {
                return arrayOfNulls(size)
            }
        }

    }

    data class GroupList(val group_list: List<Group>)
    data class Group(var avatar: String, val id: String, var name: String, var is_disturb: Int)

    data class QbUserInfo(var uid: String, var avatar: String, var name: String, var qbId: String)

    data class QbUserInfoX(var uid: String, var avatar: String, var name: String, var qbId: String, var videoTrack: QBRTCVideoTrack? = null, var isConnected: Boolean = false)

    data class QRBean(val appName: String, val groupId: String, val groupName: String, val uid: String, val type: Int)

    data class QRBeanRes(val type: Int, val uid: String, val groupId: String?, val groupName: String?)


    /**
     * 钱包余额
     */
    data class WalletBalanceBean(val balance: String, val coin: String, val recharge_channel: Int, val withdraw_channel: Int)
    data class URechargeBean(val address: String, val currency: String)
    data class WalletDetailBean(val amount: String, val coin: String, val created_at: String, val from: String, val reason: String, val state: Int, val to: String, val type: Int, val uid: Int)

}
