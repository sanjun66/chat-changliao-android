package com.legend.main.home.adapter

import android.text.TextUtils
import android.view.View
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.db.entity.DBEntity
import com.legend.imkit.util.RongDateUtils
import com.legend.imkit.videocall.util.getString
import com.legend.main.databinding.ItemChatListBinding

class ChatListAdapter: BaseBindingAdapter<ItemChatListBinding, DBEntity.ChatListEntity>() {
    override fun convert(holder: VBViewHolder<ItemChatListBinding>, item: DBEntity.ChatListEntity) {
        holder.vb.apply {
            if (!TextUtils.isEmpty(item.avatar)) ImgLoader.display(context, item.avatar, rivAvatar)
            tvName.text = item.nick_name
            tvContent.text = if (item.is_secret) getString(com.legend.imkit.R.string.msg_sub_secret_msg) else item.message
            tvTime.text = RongDateUtils.getConversationListFormatDate(item.timestamp, context)
            tvMention.visibility = if (item.show_mention_tip) View.VISIBLE else View.GONE
            imgDisturb.visibility = if (item.is_disturb == TypeConst.type_yes) View.VISIBLE else View.GONE
            if (item.unread_num > 0) {
                if (item.show_unread_num == DBEntity.showUnReadNum) {
//                    tvRedDot.visibility = View.GONE
                    tvUnreadNum.visibility = View.VISIBLE
                    tvUnreadNum.text = if (item.unread_num > 99) "99+" else item.unread_num.toString()
                } else {
//                    tvRedDot.visibility = View.VISIBLE
                    tvUnreadNum.visibility = View.GONE
                }
            } else {
                tvRedDot.visibility = View.GONE
                tvUnreadNum.visibility = View.GONE
            }

        }

    }
}