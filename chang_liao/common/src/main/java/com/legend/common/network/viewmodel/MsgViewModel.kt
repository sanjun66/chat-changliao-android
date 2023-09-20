package com.legend.common.network.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.legend.base.Applications
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.StringUtils
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.BaseRes
import com.legend.basenet.network.coroutine.request
import com.legend.basenet.network.util.SoChatEncryptUtil
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.bean.NetChatListBean
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.SimpleMessage
import com.legend.common.network.services.MsgService
import com.legend.commonres.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MsgViewModel: ViewModel() {
    val messageRes = MutableLiveData<NetMsgResBean>()
    val textMsgRes = MutableLiveData<NetMsgResBean>()
    val fileMsgRes = MutableLiveData<NetMsgResBean>()
    val revokeMsgRes = MutableLiveData<SimpleMessage>()
    val checkCanCallRes = MutableLiveData<ChatMessageModel<Any?>>()
    val decryMsgRes = MutableLiveData<String>()
    val netChatListRes = MutableLiveData<List<NetChatListBean>>()
    val msgForwardRes = MutableLiveData<BaseRes>()
    val netChatMsgRes = MutableLiveData<List<ChatMessageModel<Any>>>()

//    companion object {
//        const val fileTypePic = 1
//        const val fileTypeVideo = 2
//        const val fileTypeFile = 3
//        const val fileTypeVoice = 4
//    }

    private val services : MsgService by lazy {
        NetworkManager.getInstance().getService(MsgService::class.java)
    }

    fun uploadFile(toUid: String, quoteId: Int, message: String, warnUsers: String, talkType: Int, duration: Long, weight: Int, height: Int, type: Int, imageFile: File, path: String, uuid: String, isSecret: Boolean, pwd: String) {
        val bodyMap = HashMap<String, RequestBody>()
        bodyMap["message"] = toRequestBody(message)
        bodyMap["warn_users"] = toRequestBody(if (TextUtils.isEmpty(warnUsers)) "0" else warnUsers)
        bodyMap["path"] = toRequestBody(path)
        bodyMap["to_uid"] = toRequestBody(toUid)
        bodyMap["quote_id"] = toRequestBody(quoteId.toString())
        bodyMap["talk_type"] = toRequestBody(talkType.toString())
        bodyMap["duration"] = toRequestBody(duration.toString())
        bodyMap["weight"] = toRequestBody(weight.toString())
        bodyMap["height"] = toRequestBody(height.toString())
        bodyMap["type"] = toRequestBody(type.toString())
        bodyMap["uuid"] = toRequestBody(uuid)
        bodyMap["pwd"] = toRequestBody(pwd)

        val requestFile: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),imageFile)
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        request({ services.uploadFile(isSecret, bodyMap, body) }, {
            fileMsgRes.value = NetMsgResBean(true, uuid, "", it)
        }) {
            fileMsgRes.value = NetMsgResBean(false, uuid, it.message?:"", null)
        }

    }

    fun sendMsg(jsonString: String, uuid: String, msgType: Int) {
        if (Applications.isSecret) {
            val encrypted = SoChatEncryptUtil.encrypt(jsonString)
            request({services.sendMsgEncrypt(encrypted)}, {
                messageRes.value = NetMsgResBean(true, uuid, "", it, msgType)
            }, {
                messageRes.value = NetMsgResBean(false, uuid, it.message?:"", null, msgType)
            })
        } else {
            val body = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
            request({services.sendMsg(body)}, {
                messageRes.value = NetMsgResBean(true, uuid, "", it, msgType)
            }, {
                messageRes.value = NetMsgResBean(false, uuid, it.message?:"", null, msgType)
            })
        }
    }

//    fun sendTextMsg(jsonString: String, uuid: String) {
//        val body = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
//        request({services.sendTextMsg(body)}, {
//            textMsgRes.value = NetMsgResBean(true, uuid, "", it)
//        }, {
//            textMsgRes.value = NetMsgResBean(false, uuid, it.message?:"", null)
//        })
//    }

    fun revokeMsg(msgId: String, uuid: String) {
        val simpleMessage = SimpleMessage()
        simpleMessage.id = msgId
        simpleMessage.uuid = uuid
        if (Applications.isSecret) {
            val encrypt = SoChatEncryptUtil.encrypt(GlobalGsonUtils.toJson(simpleMessage))
            request({services.revokeMsgEncrypt(encrypt)}, {
                revokeMsgRes.value = it
            }, {
                ToastUtils.show(it.message?:"")
            })
        } else {
            val body = GlobalGsonUtils.toJson(simpleMessage).toRequestBody("application/json".toMediaTypeOrNull())
            request({services.revokeMsg(body)}, {
                revokeMsgRes.value = it
            }, {
                ToastUtils.show(it.message?:"")
            })
        }
    }

    fun checkCanCall(talkType: Int, id: String, groupId: String?, isVideoCall: Boolean) {
        val params = HashMap<String, String>()
        params["talk_type"] = talkType.toString()
        params["id"] = id
        groupId?.let {
            params["group_id"] = it
        }
        params["message_type"] = if (isVideoCall) "11" else "10"
        request({services.checkCall(params)}, {
            checkCanCallRes.value = it
        }, {
            ToastUtils.show(it.message?:"")
        })
    }

    fun msgDecrypt(id: String, pwd: String) {
        val params = HashMap<String, String>()
        params["id"] = id;
        params["pwd"] = pwd
        request({services.msgDecrypt(params)}, {
            decryMsgRes.value = GlobalGsonUtils.toJson(it)
        }, {
            ToastUtils.show(it.message?:"")
        })
    }

    fun getNetChatList() {
        request({services.getNetTalkList()}, {
            netChatListRes.value = it
        })
    }

    fun deleteChatList(toUid: String, talkType: Int) {
        val params = HashMap<String, String>()
        params["id"] = toUid
        params["talk_type"] = talkType.toString()
        request({services.deleteChatList(params)}, {

        })
    }

    fun getNetChatMessages(toUid: String, talkType: Int, messageId: String) {
        val params = HashMap<String, String>()
        params["id"] = toUid
        params["talk_type"] = talkType.toString()
        params["messageId"] = messageId     // 本地最后一条消息的messageId，没有为0
        request({services.getNetChatMessage(params)}, {
            netChatMsgRes.value = it
        })
    }

    fun msgForward(ids: String, toUid: String, talkType: Int, forwardType: Int) {
        val params = HashMap<String, String>()
        params["ids"] = ids
        params["to_uid"] = toUid
        params["talk_type"] = talkType.toString()
        params["forward_type"] = forwardType.toString()
        request({services.msgForward(params)}, {
            msgForwardRes.value = BaseRes(true, StringUtils.getString(R.string.forward_success))
        }, {
            msgForwardRes.value = BaseRes(false, it.message)
        })
    }

    private fun toRequestBody(value: String): RequestBody {
        return  RequestBody.create("text/plain".toMediaTypeOrNull(), value)
    }
}

data class NetMsgResBean(val isSuccess: Boolean, val uuid: String, val errorMsg: String, val message: ChatMessageModel<Any>?, val msgType: Int = 0)