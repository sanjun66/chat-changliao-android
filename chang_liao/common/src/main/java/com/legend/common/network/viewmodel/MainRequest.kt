package com.legend.common.network.viewmodel

import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.coroutine.mainRequest
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.network.services.MainRequestService

object MainRequest {
    private val services: MainRequestService by lazy {
        NetworkManager.getInstance().getService(MainRequestService::class.java)
    }

    fun reportCallState(state: String, callId: String) {
        val params = HashMap<String, String>()
        params["state"] = state
        params["id"] = callId
        mainRequest({services.reportTalkState(params)}, {

        })
    }

    fun reportReadMsg(messageId: String, talkType: Int) {
        val params = HashMap<String, String>()
        params["message_id"] = messageId
        params["uid"] = ApplicationConst.getUserId()
        params["talk_type"] = talkType.toString()
        mainRequest({services.reportReadMsg(params)}, {

        })
    }

    fun scanJoinGroup(groupId: String, inviteId: String) {
        val params = HashMap<String, String>()
        params["id"] = groupId
        params["invite_id"] = inviteId
        mainRequest({ services.scanJoinGroup(params)}, {

        }, {
            ToastUtils.show(it.message?:"")
        })
    }

    fun deleteMsg(msgId: String) {
        mainRequest({services.deleteMsgReport(msgId)}, {

        })
    }

}