package com.legend.main.util

import com.legend.base.utils.MMKVUtils
import com.legend.baseui.ui.util.ActivityManager
import com.legend.common.ApplicationConst
import com.legend.common.KeyConst
import com.legend.common.Router
import com.legend.common.db.DbManager
import com.legend.common.socket.MSocket

object LogoutUtil {
    fun logout() {
        MMKVUtils.putBoolean(KeyConst.key_is_login, false)
        Router.toLoginActivity()
        ActivityManager.getInstance().finishAllActivity()
        MSocket.instance.socketClient?.close()
        MSocket.instance.socketClient = null
        ApplicationConst.setUserId("0")
        DbManager.closeSoChatDB()
    }
}