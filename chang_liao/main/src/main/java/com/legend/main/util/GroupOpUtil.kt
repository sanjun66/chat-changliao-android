package com.legend.main.util

import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.Applications
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.ActivityManager
import com.legend.common.EventKey
import com.legend.common.db.DbManager
import com.legend.imkit.chat.Chat1Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object GroupOpUtil {
    /**
     * 删除群组
     */
    fun deleteGroup(groupId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            DbManager.getSoChatDB().msgDao().deleteMsgBySessionId("g$groupId")
            FileUtils.deleteAllFile(File(FileUtils.getAppRootPath(Applications.getCurrent(), "g$groupId")))
            DbManager.getSoChatDB().userAvatarDao().deleteItemById("g$groupId")
            LiveEventBus.get<String>(EventKey.key_delete_session_update).post("g$groupId")
            ActivityManager.getInstance().getActivity(Chat1Activity::class.java)?.finish()
        }
    }
}