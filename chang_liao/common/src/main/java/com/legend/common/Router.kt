package com.legend.common

import android.app.Activity
import com.alibaba.android.arouter.launcher.ARouter
import com.legend.common.bean.FileMsgContent
import com.legend.common.bean.UserBean

object Router {
    fun toLoginActivity(): Any? = ARouter.getInstance().build(RouterPath.path_login_activity).navigation()
    fun toLoginCodeActivity(loginType: Int): Any? = ARouter.getInstance().build(RouterPath.path_real_login_activity).withInt(TypeConst.login_type, loginType).navigation()
    fun toResetPwdActivity(resetType: Int): Any? = ARouter.getInstance().build(RouterPath.path_reset_pwd_activity).withInt(TypeConst.reset_pwd_type, resetType).navigation()
    fun toRegisterActivity(registerType: Int): Any? = ARouter.getInstance().build(RouterPath.path_register).withInt(TypeConst.regieter_type, registerType).navigation()
    fun toHomeActivity(): Any? = ARouter.getInstance().build(RouterPath.path_home_activity).navigation()
    fun toChooseCountryCodeActivity(context: Activity, requestCode: Int): Any? = ARouter.getInstance().build(RouterPath.path_choose_country_code_activity).navigation(context, requestCode)
    fun toSearchFriendActivity(): Any? = ARouter.getInstance().build(RouterPath.path_search_friend_activity).navigation()
    fun toApplyListActivity(): Any? = ARouter.getInstance().build(RouterPath.path_apply_list_activity).navigation()
    fun toSelectMemberActivity(context: Activity, requestCode: Int = 0, groupId: String? = null, state: Int, friends: ArrayList<UserBean.GroupMember>? = null, maxSelectNum: Int = 0, isGroupOwner: Boolean = false): Any? = ARouter.getInstance().build(RouterPath.path_select_member_activity)
        .withString(KeyConst.key_group_id, groupId)
        .withInt(KeyConst.key_group_inout_state, state)
        .withParcelableArrayList(KeyConst.key_group_friend, friends)
        .withInt(KeyConst.key_select_max_num, maxSelectNum)
        .withBoolean(KeyConst.key_is_group_owner, isGroupOwner)
        .navigation(context, requestCode)
    fun toUserInfoActivity(): Any? = ARouter.getInstance().build(RouterPath.path_user_info_activity).navigation()
    fun toUserActivity(uid: String, groupId: String): Any? = ARouter.getInstance().build(RouterPath.path_user_activity)
        .withString(KeyConst.key_user_id, uid)
        .withString(KeyConst.key_group_id, groupId)
        .navigation()
    fun toChangeUserInfoActivity(context: Activity, requestCode: Int, type: Int, uid: String): Any? = ARouter.getInstance().build(RouterPath.path_change_user_info_activity)
        .withInt(TypeConst.change_info_type, type).withString(KeyConst.key_user_id, uid).navigation(context, requestCode)
    fun toGroupChatInfoActivity(groupId: String): Any? = ARouter.getInstance().build(RouterPath.path_group_chat_info_activity).withString(KeyConst.key_group_id, groupId).navigation()
    fun toGroupListActivity(context: Activity, type: Int, requestCode: Int): Any? = ARouter.getInstance().build(RouterPath.path_group_list_activity)
        .withInt(KeyConst.key_group_list_type, type)
        .navigation(context, requestCode)
    fun toGroupMemberActivity(groupId: String): Any? = ARouter.getInstance().build(RouterPath.path_group_member_activity).withString(KeyConst.key_group_id, groupId).navigation()
    fun toSettingActivity(): Any? = ARouter.getInstance().build(RouterPath.path_setting_activity).navigation()
    fun toFriendBlackListActivity(): Any? = ARouter.getInstance().build(RouterPath.path_friend_black_list_activity).navigation()
    fun toQrCodeDisplayActivity(uid: String, groupId: String? = null): Any? = ARouter.getInstance().build(RouterPath.path_qrcode_display_activity)
        .withString(KeyConst.key_user_id, uid)
        .withString(KeyConst.key_group_id, groupId)
        .navigation()
//    fun toQrScanActivity(): Any? = ARouter.getInstance().build(RouterPath.path_qrcode_scan_activity).navigation()
    fun toQrGroupIdentifyActivity(uid: String, groupId: String, groupName: String): Any? = ARouter.getInstance().build(RouterPath.path_qrcode_group_identify)
        .withString(KeyConst.key_user_id, uid)
        .withString(KeyConst.key_group_id, groupId)
        .withString(KeyConst.key_group_name, groupName)
        .navigation()
    fun toGroupManagerActivity(groupId: String): Any? = ARouter.getInstance().build(RouterPath.path_group_manager_activity)
        .withString(KeyConst.key_group_id, groupId)
        .navigation()


    fun toWebViewActivity(url: String?): Any? = ARouter.getInstance().build(RouterPath.path_webview_activity).withString(KeyConst.key_url, url).navigation()


    // imkit
    fun toChatActivity(sessionUid: String, title: String): Any? = ARouter.getInstance().build(RouterPath.path_chat_activity)
        .withString(KeyConst.key_session_uid, sessionUid).withString(KeyConst.key_title, title).navigation()
    fun toPreviewActivity(sessionId: String, fileContent: FileMsgContent): Any? = ARouter.getInstance().build(RouterPath.path_file_preview_activity)
        .withString(KeyConst.key_session_uid, sessionId).withParcelable(KeyConst.key_file_content, fileContent).navigation()
    fun toPickChatActivity(activity: Activity, requestCode: Int, msgIds: String, forwardType: Int): Any? = ARouter.getInstance().build(RouterPath.path_pick_chat_activity)
        .withString(KeyConst.key_message_ids, msgIds)
        .withInt(KeyConst.key_forward_type, forwardType)
        .navigation(activity, requestCode)


    // 发送失败的情况下要传入pwd和msgContent
    fun toInputTransActivity(activity: Activity, requestCode: Int, type: Int, id: String? = "", pwd: String? = "", message: String? = "", itemPosition: Int? = 0): Any? = ARouter.getInstance().build(RouterPath.path_trans_activity)
        .withString(KeyConst.key_id, id)
        .withString(KeyConst.key_secret_pwd, pwd)
        .withString(KeyConst.key_secret_msg_content, message)
        .withInt(KeyConst.key_page_type, type)
        .withInt(KeyConst.key_item_position, itemPosition?:0)
        .navigation(activity, requestCode)

    fun toForwardMsgActivity():Any? = ARouter.getInstance().build(RouterPath.path_forward_msg_activity).navigation()
}

object RouterPath {
    const val path_login_activity = "/main/activity/LoginActivity"
    const val path_real_login_activity = "/main/activity/RealLoginActivity"
    const val path_reset_pwd_activity = "/main/activity/ResetPwdActivity"
    const val path_register = "/main/activity/registerActivity"
    const val path_home_activity = "/main/activity/homeActivity"
    const val path_choose_country_code_activity = "/main/activity/chooseCountryCodeActivity"
    const val path_search_friend_activity = "/main/activity/searchFriendActivity"
    const val path_apply_list_activity = "/main/activity/applyListActivity"
    const val path_select_member_activity = "/main/activity/SelectMemberActivity"
    const val path_user_info_activity = "/main/activity/userInfoActivity"
    const val path_change_user_info_activity = "/main/activity/changeUserInfoActivity"
    const val path_group_chat_info_activity = "/main/activity/groupChatInfoActivity"
    const val path_group_list_activity = "/main/activity/groupListActivity"
    const val path_group_member_activity = "/main/activity/groupMemberActivity"
    const val path_user_activity = "/main/activity/UserActivity"
    const val path_setting_activity = "/main/activity/settingActivity"
    const val path_friend_black_list_activity = "/main/activity/friendBlackListActivity"
    const val path_qrcode_display_activity = "/main/activity/qrCodeDisplayActivity"
//    const val path_qrcode_scan_activity = "/main/activity/qrScanActivity"
    const val path_qrcode_group_identify = "/main/activity/qrGroupIdentify"
    const val path_group_manager_activity = "/main/activity/group/manager"
    const val path_pick_chat_activity = "/main/activity/PickChatActivity"

    const val path_webview_activity = "/common/activity/WebViewActivity"

    const val path_chat_activity = "/im/activity/ChatActivity"
    const val path_file_preview_activity = "/im/activity/FilePreviewActivity"
    const val path_trans_activity = "/im/activity/InputTransActivity"
    const val path_forward_msg_activity = "/im/activity/ForwardMsgActivity"


}