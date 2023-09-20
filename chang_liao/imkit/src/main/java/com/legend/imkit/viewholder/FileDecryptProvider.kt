package com.legend.imkit.viewholder

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.common.TypeConst
import com.legend.common.bean.BaseMsgContent
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgTextBinding

class FileDecryptProvider: BaseMessageItemProvider<BaseMsgContent, ItemMsgTextBinding>() {
    override fun onBindingContent(data: BaseMsgContent?, position: Int) {
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        contentBinding?.tvContent?.text = item.message.message
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgTextBinding {
        return ItemMsgTextBinding.inflate(LayoutInflater.from(context),contentView, true)
    }

    override val itemViewType = TypeConst.chat_msg_type_file

}