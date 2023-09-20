package com.legend.main.friends

import android.view.View
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.bean.UserBean
import com.legend.main.databinding.ItemSearchFriendBinding

class FriendAdapter {

}

class SearchFriendAdapter: BaseBindingAdapter<ItemSearchFriendBinding, UserBean.SearchFriend>() {
    var addFriendListner: AddFriendListener? = null
    override fun convert(
        holder: VBViewHolder<ItemSearchFriendBinding>,
        item: UserBean.SearchFriend
    ) {
        ImgLoader.display(context, item.avatar, holder.vb.rivAvatar)
        holder.vb.tvName.text = item.nick_name
        holder.vb.tvAddFriend.visibility = if (item.is_friend) View.GONE else View.VISIBLE
        holder.vb.tvAddFriend.setOnClickListener { addFriendListner?.onAddFriend(item.id) }
    }

}

interface AddFriendListener{
    fun onAddFriend(id: String)
}