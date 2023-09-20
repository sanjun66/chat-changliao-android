package com.legend.common.bean

import com.legend.common.TypeConst


class SocketBean {
    /**
     * socket message的基本类型
     */
//    data class SocketMsg<T> (val data: T?, val message: String?, val code: Int, val event_name: String, val uuid: String)


    /**
     * 心跳消息
     */
    data class HeartMsg(val message: String?, val event_name: String)

    /**
     * 回执消息
     */
    data class SendMsgReceipt(var msg_id: String) {
        private val event_name = TypeConst.socket_event_msg_receipt
    }



}
