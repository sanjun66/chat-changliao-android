package com.legend.common

import android.text.TextUtils
import com.legend.base.utils.MMKVUtils
import com.legend.common.bean.UserBean
import com.legend.commonres.BuildConfig

object KeyConst {
    const val key_url = "key_url"
    const val key_session_uid = "key_session_uid"
    const val key_title = "key_title"
    const val key_talk_single = "key_talk_single"
    const val key_code_result = "key_code_result"
    const val key_is_login = "key_is_login"
    const val key_user_token = "key_user_token"
    const val key_user_id = "key_user_id"
    const val key_user_account = "key_user_account"
    const val key_user_nick_name = "key_user_nick_name"
    const val key_user_avatar = "key_user_avatar"
    const val key_group_id = "key_group_id"
    const val key_group_name = "key_group_name"
    const val key_group_friend = "key_group_friend"
    const val key_select_max_num = "key_select_max_num"
    const val key_is_group_owner = "key_is_group_owner"
    const val key_group_inout_state = "key_group_inout_state"
    const val key_user_info = "key_user_info"
    const val key_file_content = "key_file_content"
    const val key_message_notify_mute = "key_message_notify_mute"
    const val key_money_address = "key_money_address"
    const val key_money_currency = "key_money_currency"

    const val key_qb_user_id = "key_qb_user_id"
    const val key_qb_user_login = "key_qb_user_login"
    const val key_qb_user_pwd = "key_qb_user_pwd"
    const val key_secret_pwd = "key_secret_pwd"
    const val key_id = "key_id"
    const val key_secret_msg_content = "key_secret_msg_content"
    const val key_secret_msg_uuid = "key_secret_msg_uuid"
    const val key_group_list_type = "key_group_list_type"
    const val key_message_ids = "key_message_ids"
    const val key_forward_type = "key_forward_type"
    const val key_pick_result_ids = "key_pick_result_ids"
    const val key_pick_result_name = "key_pick_result_name"
    const val key_page_type = "key_page_type"
    const val key_item_position = "key_item_position"
    const val key_input_content = "key_input_content"
}

object TypeConst {
    const val dev_android = "Android"
    const val type_yes = 1
    const val type_no = 0

    // 登录类型
    const val login_type = "login_type"
    const val type_login_phone = 1
    const val type_login_email = 2
    const val type_login_phone_pwd = 3
    const val type_login_email_pwd = 4

    const val regieter_type = "register_type"
    const val type_register_phone = 1
    const val type_register_email = 2

    const val reset_pwd_type = "reset_pwd_type"
    const val type_reset_pwd_phone = 1
    const val type_reset_pwd_email = 2

    const val type_sex_male = 1
    const val type_sex_female = 2

    const val change_info_type = "change_info_type"
    const val type_modify_user_nick_name = 1
    const val type_modify_group_name = 2
    const val type_modify_note_name = 3

    // 消息相关类型
    const val socket_event_type_system = "event_system"                 // 系统提示消息
    const val socket_event_type_talk = "talk_message"                   // 会话聊天消息
    const val socket_event_talk_revoke = "talk_revoke"                  // 撤回消息
    const val socket_event_type_heart = "heartbeat"                     // 心跳
    const val socket_event_type_pull = "talk_pull"                      // 拉取离线消息
    const val socket_event_type_apply_friend = "friend_apply"           // 好友申请消息
    const val socket_event_type_remote_close = "system_device_close"    // 35s内没有发心跳主动断开前发送过来的消息
    const val socket_event_type_talk_read = "talk_read"                 // 已读消息回执
    const val socket_event_msg_receipt = "msgReceipt"                   // 消息回执
    const val socket_event_online_state_change = "online_state_change"  // 用户在线变更

    const val chat_msg_type_text = 1                // 文本消息
    const val chat_msg_type_file = 2                // 文件消息
    const val chat_msg_type_file_pic = 200
    const val chat_msg_type_file_video = 201
    const val chat_msg_type_file_file = 203
    const val chat_msg_type_file_voice = 204
    const val chat_msg_type_forward = 3             // 转发消息
    const val chat_msg_type_code = 4                // 代码消息
    const val chat_msg_type_vote = 5                // 投票消息
    const val chat_msg_type_group_voice = 6         // 群组公告
    const val chat_msg_type_apply = 7               // 好友申请
    const val chat_msg_type_login  = 8              // 登录通知消息
    const val chat_msg_type_group_op = 9            // 群操作消息
    const val chat_msg_type_audio_call_state = 10   // 语音通话状态消息
    const val chat_msg_type_video_call_state = 11   // 11 视频
    const val chat_msg_type_new_friend = 12         // 添加/拒绝好友

    const val oss_state_ucloud = 0
    const val oss_state_local = 1
    const val oss_state_aws = 2

    const val chat_msg_type_file_sub_pic = 1
    const val chat_msg_type_file_sub_video = 2
    const val chat_msg_type_file_sub_file = 3
    const val chat_msg_type_file_sub_audio = 4

    const val talk_type_single_chat = 1             // 单聊
    const val talk_type_group_chat = 2              // 群聊

    const val msg_send_status_sending = 10
    const val msg_send_status_failed = 20
    const val msg_send_status_sent = 30
    const val msg_send_status_received = 40
    const val msg_send_status_read = 50
    const val msg_send_status_destroyed = 60
    const val msg_send_status_canceled = 70
    const val msg_send_status_uploading = 71

    const val msg_rev_status_accept = 0
    const val msg_rev_status_read = 1
    const val msg_rev_status_listened = 2
    const val msg_rev_status_downloading = 3
    const val msg_rev_status_download_fail = 4
    const val msg_rev_status_downloaded = 5
    const val msg_rev_status_retrieved = 8      // 已检索
    const val msg_rev_status_multiplereceive = 16   // 多重接收

    const val voice_type_quality_normal = 0
    const val voice_type_quality_high = 1

    // 会话列表上方状态
    const val state_no_network = 1
    const val state_socket_connecting = 2
    const val state_socket_connect_fail = 3
    const val state_socket_connect_success = 4
    const val state_socket_pulling = 5
    const val state_socket_handle_down = 6
    const val state_login_other_device = 7
    const val state_has_network = 8

    const val state_group_invite = -2       // 邀请入群
    const val state_group_kit_out = -1      // 踢出群聊
    const val state_single_chat = -3        // 单聊
    const val state_invite_group_call = -4  // 邀请群聊
    const val state_group_mention = -5      // @某人
    const val state_group_add_manager = -6  // 群管理员
    const val state_group_remove_manager = -7 // 剔除群管理员
    const val state_pick_one_contact = -8   // 选择单个联系人（转发）

    const val type_call_reject_initiative = "1"
    const val type_call_reject_busy = "2"

    const val type_call_single = "0"          // 单人呼叫
    const val type_call_multitude = "1"       // 多人呼叫

    const val call_state_start = "0"      // 群发起通话
    const val call_state_cancel = "1"     // 发起方取消
    const val call_state_refuse = "2"     // 接收方拒绝
    const val call_state_no_answer = "3"  // 对方无应答
    const val call_state_accept = "4"     // 对方同意
    const val call_state_hang_up = "5"    // 通话正常挂断
    const val call_state_exception = "6"  // 通话异常
    const val call_state_refuse_busy = "7"// 对方正忙未接听

    const val group_member_normal = "0"     // 群普通成员
    const val group_member_master = "1"     // 群主
    const val group_member_manager = "2"    //群管理员

    const val qr_type_single = 1
    const val qr_type_group = 2

    const val group_list_type_show = 1       // 群列表展示
    const val group_list_type_pick = 2       // 群列表选择

    const val forward_type_one_by_one = 1   // 逐条转发
    const val forward_type_merge = 2        // 合并转发

    const val trans_input_page_type_secret = 1
    const val trans_input_page_type_add_friend = 2
    const val trans_input_page_type_refuse =3

    const val add_func_type_album = 1       // 相册
    const val add_func_type_video = 2       // 视频
    const val add_func_type_file = 3        // 文件
    const val add_func_type_call = 4        // 音视频通话
    const val add_func_type_secret = 5      // 密聊模式
    const val add_func_type_red_packet = 6  // 红包
}

object ApplicationConst {
    const val APPLICATION_ID = "com.kaiyiweitong.weilian"
    const val HOST= BuildConfig.SERVER_HOST

    private var USER_TOKEN: String = ""
    fun setUserToken(token: String) {
        USER_TOKEN = token
        MMKVUtils.putString(KeyConst.key_user_token, token)
    }
    fun getUserToken(): String {
        if (!TextUtils.isEmpty(USER_TOKEN)) return USER_TOKEN
        USER_TOKEN = MMKVUtils.getString(KeyConst.key_user_token, "")?:""
        return USER_TOKEN
    }

    private var USER_ID: String = "0"
    fun setUserId(userId: String) {
        USER_ID = userId
        MMKVUtils.putString(KeyConst.key_user_id, userId)
    }
    fun getUserId(): String {
        if (!TextUtils.isEmpty(USER_ID) && USER_ID != "0") return USER_ID
        USER_ID = MMKVUtils.getString(KeyConst.key_user_id, "0")?:"0"
        return USER_ID
    }

    private var USER_ACCOUNT = ""
    fun setUserAccount(userAccount: String) {
        USER_ACCOUNT = userAccount
        MMKVUtils.putString(KeyConst.key_user_account, userAccount)
    }
    fun getUserAccount(): String {
        if (!TextUtils.isEmpty(USER_ACCOUNT)) return USER_ACCOUNT
        USER_ACCOUNT = MMKVUtils.getString(KeyConst.key_user_account, "")?:""
        return USER_ACCOUNT
    }

    var USER_NICK_NAME = ""
    fun setUserNickName(nickName: String) {
        USER_NICK_NAME = nickName
        MMKVUtils.putString(KeyConst.key_user_nick_name, nickName)
    }
    fun getUserNickName(): String {
        if (!TextUtils.isEmpty(USER_NICK_NAME)) return USER_NICK_NAME
        USER_NICK_NAME = MMKVUtils.getString(KeyConst.key_user_nick_name, "")?:""
        return USER_NICK_NAME
    }

    private var USER_AVATAR = ""
    fun setUserAvatar(avatar: String) {
        USER_AVATAR = avatar
        MMKVUtils.putString(KeyConst.key_user_avatar, avatar)
    }
    fun getUserAvatar(): String {
        if (!TextUtils.isEmpty(USER_AVATAR)) return USER_AVATAR
        USER_AVATAR = MMKVUtils.getString(KeyConst.key_user_avatar, "")?:""
        return USER_AVATAR
    }

    var VOICE_QUALITY: Int = TypeConst.voice_type_quality_high
    var VOICE_ALL_MUTE: Boolean = false
    val opponentList = mutableListOf<UserBean.QbUserInfo>()     // 对端

    val REQUEST_CODE_FLOAT_PERMISSION = 106

    val IS_SUPPORT_HISTORY = TypeConst.type_yes
}

object EventKey {
    const val key_add_friend = "key_add_friend"
    const val key_have_friend_apply = "key_have_friend_apply"
    const val key_friend_show_dot = "key_friend_show_dot"
    const val key_msg_unread_num = "key_msg_unread_num"
    const val key_apply_friend_activity_destroy = "key_apply_friend_activity_destroy"
    const val key_session_uid = "key_session_uid"
    const val key_offline_msg_finished = "key_offline_msg_finished"
    const val key_is_app_foreground = "key_is_app_foreground"
    const val key_network_socket_change = "key_network_socket_change"
    const val key_update_chat_list_from_chat = "key_update_chat_list_from_chat"
    const val key_update_chat_list_send_fail_msg = "key_update_chat_list_send_fail_msg"
    const val key_update_chat_list_sending_msg = "key_update_chat_list_sending_msg"
    const val key_delete_session_update = "key_delete_session_update"                    // 删除会话列表
    const val key_add_session_list_item = "key_add_session_list_item"                    // 添加对话框
    const val key_refresh_user_info = "key_refresh_user_info"
    const val key_to_home_message_page = "key_to_home_message_page"
    const val key_modify_other_user_info = "key_modify_other_user_info"                 // 修改其它用户info
    const val key_modify_group_info = "key_modify_group_info"                           // 群组Info修改
    const val key_delete_group = "key_delete_group"                                     // 删除群组
    const val key_in_out_group = "key_in_out_group"                                     // 进出群
    const val key_group_manager_change = "key_group_manager_change"                     // 群管理员变更
    const val key_group_call_selected = "key_group_call_selected"                       // 群电话选择成员
    const val key_qb_enable_phone_speaker = "key_qb_enable_phone_speaker"
    const val key_mention_selected = "key_mention_selected"                             // 选中的提醒的人
    const val key_forward_content_data = "key_forward_content_data"                     // 转发消息数据
}