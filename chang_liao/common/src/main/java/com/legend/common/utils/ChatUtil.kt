package com.legend.common.utils

import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.FileUtils
import com.legend.base.utils.StringUtils
import com.legend.common.*
import com.legend.common.db.DbManager
import com.legend.commonres.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object ChatUtil {
    fun deleteFriend(context: Context, friendId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val sessionId = "s$friendId"
            // 删除消息列表
            DbManager.getSoChatDB().msgDao().deleteMsgBySessionId(sessionId)
            // 删除会话列表
            LiveEventBus.get<String>(EventKey.key_delete_session_update).post(sessionId)
            // 删除文件
            val file = FileUtils.getAppRootPath(context, sessionId)
            FileUtils.deleteAllFile(File(file))
        }
    }

    fun toUserActivity(isSelf: Boolean, friendId: String, groupId: String) {
//        if (isSelf) {
//            Router.toUserInfoActivity()
//        } else {
            Router.toUserActivity(friendId, groupId)
//        }
    }

    fun getCallStateMsg(isSender: Boolean, state: String, duration: Long): String {
        return when (state) {
            TypeConst.call_state_cancel -> if (isSender) StringUtils.getString(
                R.string.call_cancel) else StringUtils.getString(R.string.call_opponent_cancel)
            TypeConst.call_state_refuse -> if (isSender) StringUtils.getString(
                R.string.call_opponent_refuse) else StringUtils.getString(R.string.call_refuse)
            TypeConst.call_state_no_answer -> if (isSender) StringUtils.getString(
                R.string.call_opponent_no_answer) else StringUtils.getString(R.string.call_not_accept)
            TypeConst.call_state_hang_up -> String.format(
                StringUtils.getString(
                    R.string.call_continue_time), TimeUtil.getCallTimeSecond(duration))
            TypeConst.call_state_refuse_busy -> if (isSender) StringUtils.getString(
                R.string.call_opponent_busy) else StringUtils.getString(R.string.call_opponent_busy_refuse)
            else ->  StringUtils.getString(R.string.call_exception)
        }
    }

    fun getGroupCallStateMsg(nickName: String, state: String, isVideo: Boolean): String {
        return when(state) {
            TypeConst.call_state_start -> if (isVideo) String.format(StringUtils.getString(R.string.call_start_video_call), nickName) else  String.format(StringUtils.getString(R.string.call_start_audio_call), nickName)
            else -> StringUtils.getString(R.string.call_finished)
        }
    }

    fun getGroupOpMsg(type: Int, isSelf: Boolean, optionedUsers: String, operate_user_name: String): String {
        return when(type) {
            // 1:入群退群通知;2:自动退群;3:管理员踢群;4群解散
            1 -> {
                // 入群退群通知
                if (isSelf) {
                    String.format(StringUtils.getString(R.string.group_invite_msg), StringUtils.getString(R.string.you), optionedUsers)
                } else {
                    String.format(StringUtils.getString(R.string.group_invite_msg), operate_user_name, optionedUsers)
                }
            }
            2 -> {
                // 自动退群
                if (isSelf) {
                    String.format(StringUtils.getString(R.string.group_exit_msg), StringUtils.getString(R.string.you))
                } else {
                    String.format(StringUtils.getString(R.string.group_exit_msg), operate_user_name)
                }
            }
            3 -> {
                // 管理员踢群
                if (isSelf) {
                    String.format(StringUtils.getString(R.string.group_kit_out_msg), optionedUsers, StringUtils.getString(R.string.you))
                } else {
                    String.format(StringUtils.getString(R.string.group_kit_out_msg), optionedUsers, operate_user_name)
                }
            }
            4 -> {
                // 群解散
                if (isSelf) {
                    String.format(StringUtils.getString(R.string.group_dissolve_msg), StringUtils.getString(R.string.you))
                } else {
                    String.format(StringUtils.getString(R.string.group_dissolve_msg), operate_user_name)
                }
            }
            else -> ""
        }
    }

}