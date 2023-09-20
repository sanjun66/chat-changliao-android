package com.legend.imkit.viewholder

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.common.TypeConst
import com.legend.imkit.R
import com.legend.common.bean.FileMsgContent
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgVoiceBinding
import com.legend.imkit.manager.AudioPlayManager

/**
 *
 *
 * @Author: young
 * @Date: 2023/6/27 23:19
 */
class VoiceMsgProvider : BaseMessageItemProvider<FileMsgContent, ItemMsgVoiceBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
        val minWidth = DisplayUtils.dp2px(context, 40f)
        val maxWith = DisplayUtils.getScreenWidth(context) - DisplayUtils.dp2px(context, 120f)
        val maxDuration = 60
        val duration = data?.duration
        contentBinding?.apply {
            llVoiceContent.layoutParams.width = ((minWidth + (maxWith - minWidth) / maxDuration * (duration?:1)).toInt()) + DisplayUtils.dp2px(context, 16f)
            tvDuration.text = String.format("%s\"", duration)
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun convert(helper: BaseViewHolder, uiMessage: UiMessage) {
        super.convert(helper, uiMessage)
        if (uiMessage.message.is_revoke == TypeConst.type_yes && uiMessage.isPlaying) {
            AudioPlayManager.getInstance().stopPlay()
            return
        }
        contentBinding?.apply {
            val bgResId = if (uiMessage.message.isSender) R.drawable.message_text_send else R.drawable.message_text_receive
            llVoiceContent.setBackgroundResource(bgResId)
            if (uiMessage.message.isSender) {
                llVoiceContent.setPadding(DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f))
                imgVoiceLeft.visibility = View.GONE
                imgVoiceRight.visibility = View.VISIBLE
                tvDuration.gravity = Gravity.START
                tvDuration.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white))
                val params = tvDuration.layoutParams as LinearLayout.LayoutParams
                params.marginEnd = 12
                tvDuration.layoutParams = params
                Log.i("websocket", "voice 播放 发送端：isPlaying = " + uiMessage.isPlaying + " ， " + Thread.currentThread().name)
                if (uiMessage.isPlaying) {
                    val animationDrawable = context.resources.getDrawable(R.drawable.rc_an_voice_send, null) as AnimationDrawable
                    imgVoiceRight.setImageDrawable(animationDrawable)
                    if (!animationDrawable.isRunning) {
                        animationDrawable.start()
                    }
                } else {
                    imgVoiceRight.setImageResource(R.drawable.rc_voice_send_play3)
                }
            } else {
                llVoiceContent.setPadding(DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f))
                imgVoiceLeft.visibility = View.VISIBLE
                imgVoiceRight.visibility = View.GONE
                tvDuration.gravity = Gravity.END
                tvDuration.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_grey))
                val params = tvDuration.layoutParams as LinearLayout.LayoutParams
                params.marginEnd = 12
                tvDuration.layoutParams = params
                Log.i("websocket", "voice 播放 接收端：isPlaying = " + uiMessage.isPlaying + " ， " + Thread.currentThread().name)
                if (uiMessage.isPlaying) {
                    val animationDrawable = context.resources.getDrawable(R.drawable.rc_an_voice_receive, null) as AnimationDrawable
                    imgVoiceLeft.setImageDrawable(animationDrawable)
                    animationDrawable.start()
                } else {
                    imgVoiceLeft.setImageResource(R.drawable.rc_voice_receive_play3)
                }

                if (uiMessage.message.revStatus == TypeConst.msg_rev_status_downloading || uiMessage.message.revStatus == TypeConst.msg_rev_status_download_fail) {
                    pbProgressRight.visibility = View.VISIBLE
                    imgVoiceUnread.visibility = View.GONE
                } else {
                    pbProgressRight.visibility = View.GONE

                    if (uiMessage.message.revStatus != TypeConst.msg_rev_status_listened) {
                        imgVoiceUnread.visibility = View.VISIBLE
                    } else {
                        imgVoiceUnread.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgVoiceBinding {
        return ItemMsgVoiceBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_voice
}