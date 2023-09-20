package com.legend.imkit.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.legend.common.TypeConst
import com.legend.common.db.entity.DBEntity

object ChatBean {
    class SendGetOfflineMessage() {
        private val event_name = TypeConst.socket_event_type_pull
    }

    class SendGetReadMessage() {
        private val event_name = TypeConst.socket_event_type_talk_read
    }

    data class SendAckOfflineMessage(var ids: String) {
        private val event_name = TypeConst.socket_event_type_pull

    }

    data class AddFuncBean(val imgResId: Int, val name: String, val type: Int)

}
