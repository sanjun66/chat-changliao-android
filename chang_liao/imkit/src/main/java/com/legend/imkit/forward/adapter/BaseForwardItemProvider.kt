package com.legend.imkit.forward.adapter

import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.bean.UiMessage
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.DBEntity
import com.legend.imkit.R
import com.legend.imkit.databinding.ItemForwardMsgBaseBinding
import com.legend.imkit.util.RongDateUtils

abstract class BaseForwardItemProvider<T, VB: ViewDataBinding>(): BaseItemProvider<UiMessage>() {
    override val layoutId = R.layout.item_forward_msg_base

    var contentBinding: VB? = null
    var data: T? = null
    var rootViewBinding: ItemForwardMsgBaseBinding? = null
    var uiMessage: UiMessage? = null

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        rootViewBinding = DataBindingUtil.bind(helper.itemView)
        this.uiMessage = item
        rootViewBinding?.apply {
            fltContent.removeAllViews()
            contentBinding = onCreateMessageContentViewHolder(fltContent, itemViewType)
        }

        val position = helper.absoluteAdapterPosition
        val message = item.message as ChatMessageModel<T>
        initTime(message, position)
        initUserInfo(item.userInfo, message)
        initContent(message, position)
    }

    private fun initTime(message: ChatMessageModel<T>, position: Int) {
        rootViewBinding?.apply {
            val time = RongDateUtils.getConversationFormatDate(message.timestamp, context)
            tvTime.text = time
        }
    }

    private fun initUserInfo(userInfo: DBEntity.UserSimpleInfo?, message: ChatMessageModel<T>) {
        rootViewBinding?.apply {
            userInfo?.apply {
                ImgLoader.display(context, avatar, imgAvatar)
                tvName.text = nick_name
            } ?: {
                ImgLoader.display(context, R.drawable.rc_default_portrait, imgAvatar)
            }

//            val dataSize = getAdapter()?.data?.size?:0
            // todo 这里头像合并
        }
    }

    private fun initContent(message: ChatMessageModel<T>, position: Int) {
        data = message.process()
        onBindingContent(data, position)
    }



    abstract fun onBindingContent(data: T?, position: Int)

    abstract fun onCreateMessageContentViewHolder(contentView: FrameLayout, itemViewType: Int): VB
}