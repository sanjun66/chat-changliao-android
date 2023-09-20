package com.legend.imkit.popwindow

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.legend.common.widget.DialogUitl
import com.legend.imkit.R
import com.legend.imkit.databinding.PopFriendListBinding
import com.legend.imkit.videocall.util.getString
import razerdp.basepopup.BasePopupWindow

class FriendListPopup: BasePopupWindow {
    private var mDataBinding: PopFriendListBinding? = null
    var listener: OnPopItemClickListener? = null

    companion object {
        const val TYPE_DELETE_FRIEND = 1
    }

    constructor(context: Context) :super(context) {
        setContentView(R.layout.pop_friend_list)
    }

    constructor(fragment: Fragment): super(fragment) {
        setContentView(R.layout.pop_friend_list)
    }

    override fun onViewCreated(contentView: View) {
        mDataBinding = PopFriendListBinding.bind(contentView)
        mDataBinding?.apply {
            tvDeleteFriend.setOnClickListener {
                DialogUitl.showSimpleDialog(context, getString(R.string.delete_friend_tips)
                ) { dialog, content ->
                    listener?.onItemClicked(TYPE_DELETE_FRIEND)
                }
                dismiss()
            }
        }
    }

    interface OnPopItemClickListener {
        fun onItemClicked(type: Int)
    }
}