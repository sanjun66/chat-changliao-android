package com.legend.imkit.extrafunc

import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.imkit.bean.ChatBean
import com.legend.imkit.databinding.ItemExtraFuncBinding

class ExtraFuncAdapter: BaseBindingAdapter<ItemExtraFuncBinding, ChatBean.AddFuncBean>() {
    override fun convert(holder: VBViewHolder<ItemExtraFuncBinding>, item: ChatBean.AddFuncBean) {
        holder.vb.apply {
            ImgLoader.display(context, item.imgResId, imgSrc)
            tvName.text = item.name
        }
    }
}