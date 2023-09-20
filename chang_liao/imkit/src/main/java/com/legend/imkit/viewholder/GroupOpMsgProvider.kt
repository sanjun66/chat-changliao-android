package com.legend.imkit.viewholder

import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.common.TypeConst
import com.legend.common.bean.GroupMsgContent
import com.legend.common.bean.GroupOpMsg
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgTipsBinding

class JoinGroupMsgProvide: BaseMessageItemProvider<GroupMsgContent, ItemMsgTipsBinding>() {
    override fun onBindingContent(data: GroupMsgContent?, position: Int) {
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        val message = item.message as GroupOpMsg
        contentBinding?.tvTips?.text = if (!TextUtils.isEmpty(message.extra.sendMsg)) message.extra.sendMsg else message.message
        rootViewBinding?.apply {
            llStateLayout.gravity = Gravity.CENTER_HORIZONTAL
            tvName.visibility = View.GONE
        }

    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgTipsBinding {
        return ItemMsgTipsBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_group_op
}