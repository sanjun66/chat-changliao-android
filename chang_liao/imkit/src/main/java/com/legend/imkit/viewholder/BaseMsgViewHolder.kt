package com.legend.imkit.viewholder

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.gson.Gson
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.DBEntity
import com.legend.imkit.R
import com.legend.imkit.bean.ParameterizedTypeImpl
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgBaseBinding
import com.legend.imkit.util.RongDateUtils
import java.io.Reader
import java.lang.reflect.Type

abstract class BaseMessageItemProvider<T, VB: ViewDataBinding>() : BaseItemProvider<UiMessage>() {
    protected var mConfig = MessageItemProviderConfig()
    override val layoutId: Int
        get() = R.layout.item_msg_base

    var contentBinding:VB? = null
    var data: T? = null
    var rootViewBinding: ItemMsgBaseBinding? = null
    var uiMessage: UiMessage? = null

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        rootViewBinding = DataBindingUtil.bind(helper.itemView)
        this.uiMessage = item
        rootViewBinding?.apply {
            flContent.removeAllViews()
            contentBinding = onCreateMessageContentViewHolder(flContent, itemViewType)
            if (item.isEdit) {
                if (item.message.message_type == TypeConst.chat_msg_type_group_op
                    || item.message.message_type == TypeConst.chat_msg_type_video_call_state
                    || item.message.message_type == TypeConst.chat_msg_type_audio_call_state
                    || item.message.message_type == TypeConst.chat_msg_type_forward
                    || (item.message.message_type == TypeConst.chat_msg_type_file && item.message.message_local_type == TypeConst.chat_msg_type_file_voice)
                    || item.message.is_secret || item.message.is_revoke == TypeConst.type_yes) {
                    rcSelected.visibility =  View.INVISIBLE
                    rcVEdit.visibility = View.INVISIBLE
                } else {
                    rcSelected.visibility =  View.VISIBLE
                    rcVEdit.visibility = View.VISIBLE
                    rcSelected.isSelected = item.isSelected
                }
            } else {
                rcSelected.visibility =  View.GONE
                rcVEdit.visibility = View.GONE
            }
            val isSender = item.message.isSender
            val position = helper.absoluteAdapterPosition
            val message = item.message as ChatMessageModel<T>
            initTime(message, position)

            initUserInfo(item.userInfo, isSender, message)

            initContent(message, position, isSender)

            initStatus(message, position)
        }
    }

    private fun initUserInfo(userInfo: DBEntity.UserSimpleInfo?, isSender: Boolean, message: ChatMessageModel<T>) {
        rootViewBinding?.apply {
            if (mConfig.showPortrait && message.is_revoke == TypeConst.type_no) {
                if ((message.isGroup && message.message_type == TypeConst.chat_msg_type_group_op)
                    || message.message_type == TypeConst.chat_msg_type_new_friend) {
                    imgLeftPortrait.visibility = View.GONE
                    imgRightPortrait.visibility = View.GONE
                } else {
                    imgLeftPortrait.visibility = if (!isSender) View.VISIBLE else View.INVISIBLE
                    imgRightPortrait.visibility = if (isSender) View.VISIBLE else View.INVISIBLE
                    if (message.isGroup) {
                        tvName.visibility = View.VISIBLE
                        tvName.text = userInfo?.nick_name
                        tvName.gravity = if (isSender) Gravity.END else Gravity.START
                    } else {
                        tvName.visibility = View.GONE
                    }
    
                    val imgView = if (isSender) imgRightPortrait else imgLeftPortrait
                    userInfo?.apply {
                        avatar.let {
                            ImgLoader.display(context, it, imgView)
                        }
                    }?:{
                        ImgLoader.display(context, R.drawable.rc_default_portrait, imgView)
                    }
                }
            } else {
                imgLeftPortrait.visibility = View.GONE
                imgRightPortrait.visibility = View.GONE
            }
        }
    }
    private fun initContent(message: ChatMessageModel<T>, position: Int, isSender: Boolean) {
        rootViewBinding?.apply {
            if (message.is_revoke == TypeConst.type_yes) {
                llContent.visibility = View.GONE
                flContent.visibility = View.VISIBLE
                tvCenterTips.visibility = View.VISIBLE
                tvCenterTips.text = message.message
                lltSecretMsg.visibility = View.GONE
                imgReadReceipt.visibility = View.GONE
            } else if (message.is_secret) {
                llContent.visibility = View.VISIBLE
                flContent.visibility = View.GONE
                tvCenterTips.visibility = View.GONE
                lltSecretMsg.visibility = View.VISIBLE

                llStateLayout.gravity = if (message.isSender) Gravity.END else Gravity.START
                if (message.talk_type == TypeConst.talk_type_single_chat) {
                    tvName.visibility = View.GONE
                } else {
                    tvName.visibility = View.VISIBLE
                    val leftParams = imgLeftPortrait.layoutParams as ConstraintLayout.LayoutParams
                    leftParams.topMargin = DisplayUtils.dp2px(context, 6f)
                    val rightParams = imgRightPortrait.layoutParams as ConstraintLayout.LayoutParams
                    rightParams.topMargin = DisplayUtils.dp2px(context, 6f)
                }
            } else {
                llContent.visibility = View.VISIBLE
                flContent.visibility = View.VISIBLE
                tvCenterTips.visibility = View.GONE
                lltSecretMsg.visibility = View.GONE

                llStateLayout.gravity = if (message.isSender) Gravity.END else Gravity.START
                if (message.talk_type == TypeConst.talk_type_single_chat) {
                    tvName.visibility = View.GONE
                } else {
                    tvName.visibility = View.VISIBLE
                    val leftParams = imgLeftPortrait.layoutParams as ConstraintLayout.LayoutParams
                    leftParams.topMargin = DisplayUtils.dp2px(context, 6f)
                    val rightParams = imgRightPortrait.layoutParams as ConstraintLayout.LayoutParams
                    rightParams.topMargin = DisplayUtils.dp2px(context, 6f)
                }
                data = message.process()
                onBindingContent(data, position)
            }
        }
    }
    open fun <T> fromJsonObject(reader: Reader?, clazz: Class<T>): Result<T>? {
        val type: Type = ParameterizedTypeImpl(Result::class.java, arrayOf<Class<*>>(clazz))
        return Gson().fromJson(reader, type)
    }
    abstract fun onBindingContent(data: T?, position: Int)

    private fun initStatus(message: ChatMessageModel<T>, position: Int) {
        rootViewBinding?.apply {
            if (message.isSender && message.is_revoke == TypeConst.type_no) {
                when (message.sendStatus) {
                    TypeConst.msg_send_status_sending -> {
                        pbProgress.visibility = View.VISIBLE
                        imgWarning.visibility = View.GONE
                        imgReadReceipt.visibility = View.GONE
                    }
                    TypeConst.msg_send_status_failed -> {
                        pbProgress.visibility = View.GONE
                        imgWarning.visibility = View.VISIBLE
                        imgReadReceipt.visibility = View.GONE
                    }
                    TypeConst.msg_send_status_sent -> {
                        pbProgress.visibility = View.GONE
                        imgWarning.visibility = View.GONE
                        imgReadReceipt.visibility = if (message.message_type == TypeConst.chat_msg_type_text || message.message_type == TypeConst.chat_msg_type_file
                            || message.message_type == TypeConst.chat_msg_type_forward) View.VISIBLE else View.GONE
                        if (message.isSender && message.is_read == TypeConst.type_yes) {
                            imgReadReceipt.setImageResource(R.drawable.ic_msg_read_accept)
                        } else {
                            imgReadReceipt.setImageResource(R.drawable.ic_msg_read_unaccept)
                        }
                    }
                    TypeConst.msg_send_status_uploading -> {
                        pbProgress.visibility = if (message.is_secret) View.VISIBLE else View.GONE
                        imgWarning.visibility = View.GONE
                        imgReadReceipt.visibility = View.GONE
                    }
                    else -> {
                        pbProgress.visibility = View.GONE
                        imgWarning.visibility = View.GONE
                        imgReadReceipt.visibility = View.GONE
                    }
                }
            } else {
                pbProgress.visibility = View.GONE
                imgWarning.visibility = View.GONE
                imgReadReceipt.visibility = View.GONE
            }
        }
    }
    private fun initTime(message: ChatMessageModel<T>, position: Int) {
        rootViewBinding?.apply {
            val time = RongDateUtils.getConversationFormatDate(message.timestamp, context)
            itemTvTime.text = time
            message?.let {
                val dataSize = getAdapter()?.data?.size ?: 0
                if (position == dataSize - 1) {
                    itemTvTime.visibility = View.VISIBLE
                } else if (position < dataSize - 1){
                    val pre = getAdapter()?.data?.get(position + 1)

                    if (pre != null && RongDateUtils.isShowChatTime(context, message.timestamp, pre.message.timestamp, 180)) {
                        itemTvTime.visibility = View.VISIBLE
                    } else {
                        itemTvTime.visibility = View.GONE
                    }
                }
            }
        }

    }


    abstract fun onCreateMessageContentViewHolder(contentView: FrameLayout, itemViewType: Int): VB

}