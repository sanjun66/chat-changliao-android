package com.legend.main.friends.adapter

import android.text.TextUtils
import android.view.View
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.bean.UserBean
import com.legend.main.R
import com.legend.main.databinding.ItemApplyListBinding

class ApplyListAdapter: BaseBindingAdapter<ItemApplyListBinding, UserBean.FriendApply>() {
    var listener: OnCheckListener? = null
    override fun convert(holder: VBViewHolder<ItemApplyListBinding>, item: UserBean.FriendApply) {
        holder.vb.apply {
            ImgLoader.display(context, item.avatar, rivAvatar)
            tvName.text = item.nick_name
            if (item.flag == "taker") {
                when(item.state) {
                    1 -> {
                        // 审核
                        imgAgree.visibility = View.VISIBLE
                        imgRefuse.visibility = View.VISIBLE
                        tvProcessed.visibility = View.GONE
                        tvTips.visibility = if (TextUtils.isEmpty(item.remark)) View.GONE else View.VISIBLE
                        tvTips.text = item.remark
                    }
                    2 -> {
                        // 同意
                        imgAgree.visibility = View.GONE
                        imgRefuse.visibility = View.INVISIBLE
                        tvProcessed.visibility = View.VISIBLE
                        tvProcessed.text = context.resources.getString(R.string.added)
                        tvTips.visibility = if (TextUtils.isEmpty(item.remark)) View.GONE else View.VISIBLE
                        tvTips.text = item.remark
                    }
                    3 -> {
                        // 拒绝
                        imgAgree.visibility = View.GONE
                        imgRefuse.visibility = View.INVISIBLE
                        tvProcessed.visibility = View.VISIBLE
                        tvProcessed.text = context.resources.getString(R.string.refused)
                        tvTips.visibility = if (TextUtils.isEmpty(item.process_message)) View.GONE else View.VISIBLE
                        tvTips.text = item.process_message
                    }
                }
            } else {
                imgAgree.visibility = View.GONE
                imgRefuse.visibility = View.INVISIBLE
                tvProcessed.visibility = View.VISIBLE
                when(item.state) {
                    1 -> {
                        tvProcessed.text = context.resources.getString(R.string.wait_for_verify)
                        tvTips.visibility = if (TextUtils.isEmpty(item.remark)) View.GONE else View.VISIBLE
                        tvTips.text = item.remark
                    }
                    2 -> {
                        tvProcessed.text = context.resources.getString(R.string.be_added)
                        tvTips.visibility = if (TextUtils.isEmpty(item.remark)) View.GONE else View.VISIBLE
                        tvTips.text = item.remark
                    }
                    3 -> {
                        tvProcessed.text = context.resources.getString(R.string.be_refused)
                        tvTips.visibility = if (TextUtils.isEmpty(item.process_message)) View.GONE else View.VISIBLE
                        tvTips.text = item.process_message
                    }
                }
            }

            imgAgree.setOnClickListener { listener?.onCheckApply(item.id, 2, "", holder.absoluteAdapterPosition) }
            imgRefuse.setOnClickListener { listener?.onCheckApply(item.id, 3, "", holder.absoluteAdapterPosition) }
        }

    }
}

interface OnCheckListener {
    fun onCheckApply(id: Int, state: Int, processMsg: String, position: Int)
}