package com.legend.imkit.popwindow

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.legend.imkit.R
import com.legend.imkit.databinding.PopChatListBinding
import razerdp.basepopup.BasePopupWindow


class ChatListPopup: BasePopupWindow {
    var typeDelete = 1

    var mDataBinding: PopChatListBinding? = null
    var onItemClickListener: OnPopItemClickListener? = null

    constructor(context: Context) :super(context) {
        setContentView(R.layout.pop_chat_list)
    }

    constructor(fragment: Fragment): super(fragment) {
        setContentView(R.layout.pop_chat_list)
    }

    override fun onViewCreated(contentView: View) {
        mDataBinding = PopChatListBinding.bind(contentView)

        mDataBinding?.apply {
            tvDeleteConvert.setOnClickListener {
                onItemClickListener?.onItemClicked(typeDelete)
                dismiss()
            }
        }
    }

    interface OnPopItemClickListener {
        fun onItemClicked(type: Int)
    }
}