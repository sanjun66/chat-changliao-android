package com.legend.imkit.viewholder

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.bean.CallStateContent
import com.legend.common.bean.CallStateMsg
import com.legend.common.bean.UiMessage
import com.legend.imkit.R
import com.legend.imkit.databinding.ItemCallStateMsgBinding

class AudioCallStateMsgProvider: BaseMessageItemProvider<CallStateContent, ItemCallStateMsgBinding>() {
    override fun onBindingContent(data: CallStateContent?, position: Int) {
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        rootViewBinding?.apply {
            val bgResId = if (item.message.isSender) R.drawable.message_text_send else R.drawable.message_text_receive
            flContent.setBackgroundResource(bgResId)
//            flContent.setPadding(0,0,0,0)
        }

        contentBinding?.apply {
            val message = item.message as CallStateMsg
            ImgLoader.display(context, R.drawable.ic_msg_calling, imgIcon)
            tvTips.text = if (!TextUtils.isEmpty(message.extra.sendMsg)) message.extra.sendMsg else message.message
            if (item.message.isSender) {
                tvTips.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white))
                ImgLoader.display(context, R.drawable.ic_msg_calling_white, imgIcon)
                rootViewBinding?.flContent?.setPadding(DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f))
            } else {
                tvTips.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_black))
                ImgLoader.display(context, R.drawable.ic_msg_calling, imgIcon)
                rootViewBinding?.flContent?.setPadding(DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f))
            }
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemCallStateMsgBinding {
        return ItemCallStateMsgBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int = TypeConst.chat_msg_type_audio_call_state
}