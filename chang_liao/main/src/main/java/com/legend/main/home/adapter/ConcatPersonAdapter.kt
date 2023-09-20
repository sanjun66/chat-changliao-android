package com.legend.main.home.adapter

import android.text.TextUtils
import android.view.View
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.bean.InternationAreaCode
import com.legend.common.bean.UserBean
import com.legend.main.databinding.ItemAreaCodeBinding
import com.legend.main.databinding.ItemConcatPersonBinding

class ConcatPersonAdapter(private val isShowSelect: Boolean = false): BaseBindingAdapter<ItemConcatPersonBinding, UserBean.ConcatFriend>() {
    override fun convert(
        holder: VBViewHolder<ItemConcatPersonBinding>,
        item: UserBean.ConcatFriend
    ) {
        if (isShowSelect) {
            holder.vb.imgSelect.visibility = View.VISIBLE
            holder.vb.imgSelect.isSelected = item.isSelected
        } else {
            holder.vb.imgSelect.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(item.avatar)) ImgLoader.display(context, item.avatar, holder.vb.rivAvatar)
        holder.vb.tvName.text = item.getFinalRemark()
    }
}

class ConcatSimpleAdapter(private val isShowSelect: Boolean = false): BaseBindingAdapter<ItemConcatPersonBinding, UserBean.ConcatSimple>() {
    override fun convert(
        holder: VBViewHolder<ItemConcatPersonBinding>,
        item: UserBean.ConcatSimple
    ) {
        if (isShowSelect) {
            holder.vb.imgSelect.visibility = View.VISIBLE
            holder.vb.imgSelect.isSelected = item.isSelected
        } else {
            holder.vb.imgSelect.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(item.avatar)) ImgLoader.display(context, item.avatar, holder.vb.rivAvatar)
        holder.vb.tvName.text = item.getFinalNickName()
    }

}

class AreaCodeAdapter: BaseBindingAdapter<ItemAreaCodeBinding, InternationAreaCode>() {
    override fun convert(
        holder: VBViewHolder<ItemAreaCodeBinding>,
        item: InternationAreaCode
    ) {
        holder.vb.tvName.text = item.name
    }

}

