package com.legend.common.bean

import com.legend.baseui.ui.widget.susindexbar.indexbar.bean.BaseIndexPinyinBean
import com.legend.common.TypeConst

/**
 * 国际电话区号
 */
data class InternationAreaCodesItem(val lists: List<InternationAreaCode>, val title: String)

/**
 * App版本信息
 */
data class AppVersion(val desc: String, val forced_update: Int, val platform: String, val release_date: String, val update_url: String, val version_code: String, val version_name: String)

data class InternationAreaCode(val name: String?, val name_en: String?, val tel: String?, var title: String?): BaseIndexPinyinBean() {
    override fun getTarget(): String {
        return title ?: ""
    }
}

/**
 * 登录成功返回结果
 */
data class LoginRes(
    val expire_seconds: Long,
    val platform: String,
    val token: String,
    val uid: String,
    val quickblox_login: String,
    val rong_yun_token: String,
    val quickblox_id: String,
    val quickblox_pwd: String
)

data class OssInfo(
    val aws: Aws,
    val oss_status: String
)

data class Aws(
    val aws_access_key_id: String,
    val aws_bucket: String,
    val aws_default_region: String,
    val aws_secret_access_key: String,
    val aws_url: String
)

/**
 * 上传文件
 */
data class UploadFileBean(
    val file_name: String,
    val url: String
)

data class SendMessage(val to_uid: String, val message: String, val warn_users: String, val talk_type: Int, val  message_type: Int, val uuid: String?, var is_secret: Boolean? = false, var pwd: String? = "", var extra: String? = "") {
    private val event_name = TypeConst.socket_event_type_talk
}

/**
 * 历史会话列表
 */
data class NetChatListBean(val avatar: String, val id: Int, val message: String, val name: String, val talk_type: Int, val timestamp: Long
                           , val msg_id: String, val is_pwd: Int, val is_disturb: Int, val message_type: Int)