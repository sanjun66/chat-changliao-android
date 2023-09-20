package com.legend.main.adapter

import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.bean.UserBean
import com.legend.main.databinding.ItemAvatarNameBinding

class FriendBlackAdapter: BaseBindingAdapter<ItemAvatarNameBinding, UserBean.FriendBlack>() {
    override fun convert(holder: VBViewHolder<ItemAvatarNameBinding>, item: UserBean.FriendBlack) {
        holder.vb.apply {
            ImgLoader.display(context, item.avatar, rivAvatar)
            tvName.text = item.remark
        }
    }
}