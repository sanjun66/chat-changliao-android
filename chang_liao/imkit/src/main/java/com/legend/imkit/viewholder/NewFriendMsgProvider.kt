package com.legend.imkit.viewholder

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.common.TypeConst
import com.legend.common.bean.NewFriendContent
import com.legend.common.bean.NewFriendMsg
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgTipsBinding

class NewFriendMsgProvider: BaseMessageItemProvider<NewFriendContent, ItemMsgTipsBinding>() {
    override fun onBindingContent(data: NewFriendContent?, position: Int) {
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        val message = item.message as NewFriendMsg
        contentBinding?.tvTips?.text = message.message

        rootViewBinding?.llStateLayout?.gravity = Gravity.CENTER_HORIZONTAL
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgTipsBinding {
        return ItemMsgTipsBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_new_friend
}
