package com.legend.imkit.forward

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.bean.ForwardMsgContent
import com.legend.common.bean.UiMessage
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.utils.ChatDataConvertUtil
import com.legend.common.utils.OpenFileUtil
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.imkit.R
import com.legend.imkit.databinding.ActivityForwardMsgBinding
import com.legend.imkit.forward.adapter.ForwardAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = RouterPath.path_forward_msg_activity)
class ForwardMsgActivity : BaseActivity<ActivityForwardMsgBinding>() {
    private var forwardAdapter: ForwardAdapter? = null
    private val forwardDataList: MutableList<UiMessage> = arrayListOf()
    private var sessionId: String = ""

    override fun getLayoutId() = R.layout.activity_forward_msg

    override fun initView() {
        mDataBinding?.apply {
            forwardAdapter = ForwardAdapter()
            recyclerView.layoutManager = LinearLayoutManager(mContext)
            recyclerView.adapter = forwardAdapter
            forwardAdapter?.data = forwardDataList
            forwardAdapter?.addChildClickViewIds(R.id.flt_content)
            forwardAdapter?.setOnItemChildClickListener { adapter, view, position ->
                val uiMessage = adapter.getItem(position) as UiMessage
                if (TextUtils.isEmpty(uiMessage.message.session_id)) uiMessage.message.session_id = sessionId
                when(view.id) {
                    R.id.flt_content -> {
                        when(uiMessage.message.message_local_type) {
                            TypeConst.chat_msg_type_file_pic, TypeConst.chat_msg_type_file_video -> OpenFileUtil.openPicOrVideo(this@ForwardMsgActivity, uiMessage, forwardDataList)
                            TypeConst.chat_msg_type_file_file -> OpenFileUtil.openFile(this@ForwardMsgActivity, uiMessage)
                        }
                    }
                }
            }
        }

        LiveEventBus.get<UiMessage>(EventKey.key_forward_content_data).observeSticky(this) {
            val forwardMsg: ChatMessageModel<ForwardMsgContent> = it.message as ChatMessageModel<ForwardMsgContent>
            sessionId = getSessionId(forwardMsg)
            forwardMsg.extra.talk_list?.let {talkList->
                updateData(ChatDataConvertUtil.chatMsgModelAnyForwardToUiMsg(talkList, sessionId))
            }
        }
    }

    private fun getSessionId(msg: ChatMessageModel<ForwardMsgContent>): String {
        val id = if (msg.to_uid == ApplicationConst.getUserId()) { msg.from_uid } else { msg.to_uid }
        val sessionId: String = when(msg.talk_type) {
            TypeConst.talk_type_single_chat -> "s$id"
            TypeConst.talk_type_group_chat -> "g$id"
            else -> id.toString()
        }
        return sessionId
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateData(uiMsgList: MutableList<UiMessage>) {
        forwardDataList.addAll(uiMsgList)
        forwardAdapter?.notifyDataSetChanged()

        if (uiMsgList.isNotEmpty()) {
            val item1 = uiMsgList[0]
            val talkType = item1.message.talk_type
            if (talkType == TypeConst.talk_type_single_chat) {
                val toUid = if (item1.message.from_uid == ApplicationConst.getUserId()) item1.message.to_uid else item1.message.from_uid
                lifecycleScope.launch {
                    val myName = UserSimpleDataHelper.getUserInfo("s"+ ApplicationConst.getUserId())?.nick_name
                    val opponentName = UserSimpleDataHelper.getUserInfo("s$toUid")?.nick_name
                    withContext(Dispatchers.Main) {
                        setTitleBarTitleText(String.format(getString(R.string.forward_chat_of_history), myName, opponentName))
                    }
                }
            } else {
                setTitleBarTitleText(getString(R.string.forward_group_chat))
            }
        }

    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}