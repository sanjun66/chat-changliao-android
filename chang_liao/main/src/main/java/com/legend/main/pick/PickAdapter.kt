package com.legend.main.pick

import android.text.TextUtils
import android.view.View
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.db.entity.DBEntity
import com.legend.main.databinding.ItemConcatPersonBinding

class LatestChatAdapter: BaseBindingAdapter<ItemConcatPersonBinding, DBEntity.ChatListEntity>() {
    override fun convert(
        holder: VBViewHolder<ItemConcatPersonBinding>,
        item: DBEntity.ChatListEntity) {
        holder.vb.apply {
            viewLine.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(item.avatar)) ImgLoader.display(context, item.avatar, rivAvatar)
            tvName.text = item.nick_name
        }
    }

}