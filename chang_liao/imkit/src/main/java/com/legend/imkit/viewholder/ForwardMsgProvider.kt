package com.legend.imkit.viewholder

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.common.ApplicationConst
import com.legend.common.TypeConst
import com.legend.common.bean.ForwardMsgContent
import com.legend.common.bean.UiMessage
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.imkit.R
import com.legend.imkit.databinding.ItemForwardMsgBinding
import com.legend.imkit.videocall.util.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForwardMsgProvider: BaseMessageItemProvider<ForwardMsgContent, ItemForwardMsgBinding>() {
    @SuppressLint("SetTextI18n")
    override fun onBindingContent(data: ForwardMsgContent?, position: Int) {
        if (data?.talk_list == null || data.talk_list!!.isEmpty()) return
        contentBinding?.apply {
            val itemSize = data.talk_list!!.size
            Log.i("websocket", "转发消息渲染条数：$itemSize")
            val item1 = data.talk_list!![0]
            val talkType = item1.talk_type
            CoroutineScope(Dispatchers.IO).launch {
                if (talkType == TypeConst.talk_type_single_chat) {
                    val toUid = if (item1.from_uid == ApplicationConst.getUserId()) item1.to_uid else item1.from_uid
                    val myName = UserSimpleDataHelper.getUserInfo("s"+ApplicationConst.getUserId())?.nick_name
                    val opponentName = UserSimpleDataHelper.getUserInfo("s$toUid")?.nick_name
                    withContext(Dispatchers.Main) {
                        tvTitle.text = String.format(getString(R.string.forward_chat_of_history), myName, opponentName)
                        val fromName1 = if (item1.from_uid == ApplicationConst.getUserId()) myName else opponentName
                        tvMsg1.text =  "${fromName1}:${item1.message}"
                        if (itemSize >= 2) {
                            val item2 = data.talk_list!![1]
                            val fromName2 = if (item2.from_uid == ApplicationConst.getUserId()) myName else opponentName
                            tvMsg2.text =  "${fromName2}:${item2.message}"
                        }
                        if (itemSize >= 3) {
                            val item3 = data.talk_list!![2]
                            val fromName3 = if (item3.from_uid == ApplicationConst.getUserId()) myName else opponentName
                            tvMsg3.text =  "${fromName3}:${item3.message}"
                        }
                    }
                } else {
                    tvTitle.text = getString(R.string.forward_group_chat)
                    val fromName1 = UserSimpleDataHelper.getUserInfo("s"+ item1.from_uid)?.nick_name
                    var fromName2: String? = null
                    var fromName3: String? = null
                    var message2: String? = null
                    var message3: String? = null
                    if (itemSize >= 2) {
                        val item2 = data.talk_list!![1]
                        fromName2 = UserSimpleDataHelper.getUserInfo("s"+ item2.from_uid)?.nick_name
                        message2 = item2.message
                    }
                    if (itemSize >= 3) {
                        val item3 = data.talk_list!![2]
                        fromName3 = UserSimpleDataHelper.getUserInfo("s"+ item3.from_uid)?.nick_name
                        message3 = item3.message
                    }
                    withContext(Dispatchers.Main) {
                        tvMsg1.text =  "${fromName1}:${item1.message}"
                        if (itemSize >= 2) {
                            tvMsg2.text =  "${fromName2}:${message2}"
                        }
                        if (itemSize >= 3) {
                            tvMsg3.text =  "${fromName3}:${message3}"
                        }
                    }
                }
            }

            tvMsg1.visibility = View.VISIBLE
            tvMsg2.visibility = if (itemSize >= 2) View.VISIBLE else View.GONE
            tvMsg3.visibility = if (itemSize >= 3) View.VISIBLE else View.GONE
        }
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        rootViewBinding?.apply {
            val bgResId = if (item.message.isSender) R.drawable.message_text_send else R.drawable.message_text_receive
            flContent.setBackgroundResource(bgResId)
            flContent.setPadding(0,0,0,0)
            if (item.message.isSender) {
                contentBinding?.lltContent?.setPadding(DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 3f))
                contentBinding?.tvTitle?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white))
                contentBinding?.tvMsg1?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white_trans_80))
                contentBinding?.tvMsg2?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white_trans_80))
                contentBinding?.tvMsg3?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white_trans_80))
                contentBinding?.tvHistoryRecord?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white_trans_80))
            } else {
                contentBinding?.lltContent?.setPadding(DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 3f))
                contentBinding?.tvTitle?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_black))
                contentBinding?.tvMsg1?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_grey))
                contentBinding?.tvMsg2?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_grey))
                contentBinding?.tvMsg3?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_grey))
                contentBinding?.tvHistoryRecord?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_grey))
            }
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemForwardMsgBinding {
        return ItemForwardMsgBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType = TypeConst.chat_msg_type_forward
}