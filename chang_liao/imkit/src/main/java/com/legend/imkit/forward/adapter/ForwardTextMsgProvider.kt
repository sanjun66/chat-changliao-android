package com.legend.imkit.forward.adapter

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.legend.common.TypeConst
import com.legend.common.bean.BaseMsgContent
import com.legend.imkit.databinding.ItemForwardTextMsgBinding

class ForwardTextMsgProvider: BaseForwardItemProvider<BaseMsgContent, ItemForwardTextMsgBinding>() {
    override fun onBindingContent(data: BaseMsgContent?, position: Int) {
        contentBinding?.tvContent?.text = uiMessage?.message?.message
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemForwardTextMsgBinding {
        return ItemForwardTextMsgBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_text
}