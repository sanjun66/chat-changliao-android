package com.legend.main.group.adapter

import android.view.View
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.main.databinding.ItemConcatPersonBinding
import com.legend.main.databinding.ItemGroupMemberBinding

class GroupMemberAdapter: BaseBindingAdapter<ItemGroupMemberBinding, UserBean.GroupMember>() {
    override fun convert(holder: VBViewHolder<ItemGroupMemberBinding>, item: UserBean.GroupMember) {
        if (item.uid == TypeConst.state_group_invite || item.uid == TypeConst.state_group_kit_out) {
            ImgLoader.displayNoCache(context, item.avatar!!.toInt(), holder.vb.rivAvatar)
        } else {
            ImgLoader.display(context, item.avatar, holder.vb.rivAvatar)
        }
        holder.vb.tvName.text = item.notes
    }
}

class GroupListAdapter: BaseBindingAdapter<ItemConcatPersonBinding, UserBean.Group>() {
    override fun convert(holder: VBViewHolder<ItemConcatPersonBinding>, item: UserBean.Group) {
        holder.vb.apply {
            viewLine.visibility = View.VISIBLE
            ImgLoader.display(context, item.avatar, rivAvatar)
            tvName.text = item.name
        }

    }

}