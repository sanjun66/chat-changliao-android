package com.legend.imkit.viewholder

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.common.TypeConst
import com.legend.common.bean.BaseMsgContent
import com.legend.common.bean.UiMessage
import com.legend.common.widget.TextViewFixTouchConsume
import com.legend.imkit.R
import com.legend.imkit.databinding.ItemMsgTextBinding
import com.legend.imkit.util.TextViewUtils
import java.lang.String
import kotlin.Int
import kotlin.apply

class TextMsgProvider: BaseMessageItemProvider<BaseMsgContent, ItemMsgTextBinding>() {
    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_text

    override fun onBindingContent(data: BaseMsgContent?, position: Int) {
        contentBinding?.apply {
            if (uiMessage?.contentSpannable == null) {
                val spannable: SpannableStringBuilder = TextViewUtils.getSpannable(
                    data?.sendMsg?:"") {
                    tvContent.post {
                        if (TextUtils.equals(
                                if (tvContent.getTag() == null) "" else tvContent.getTag().toString(),
                                String.valueOf(
                                    uiMessage?.message?.id
                                )
                            )
                        ) tvContent.text = uiMessage?.contentSpannable
                    }
                }
                uiMessage!!.contentSpannable = spannable
            }

            // 设置TextView可点击
            tvContent.movementMethod = TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance()
        }
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        rootViewBinding?.apply {
            val bgResId = if (item.message.isSender) R.drawable.message_text_send else R.drawable.message_text_receive
            flContent.setBackgroundResource(bgResId)
            flContent.setPadding(0,0,0,0)
            val sp = item.contentSpannable

            if (item.message.isSender) {
                contentBinding?.tvContent?.setPadding(DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f))
                contentBinding?.tvContent?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white))
                if (sp != null && sp.isNotEmpty()) {
                    val spans = sp.getSpans(0, sp.length, URLSpan::class.java)
                    for (urlSpan in spans) {
                        sp.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.link_blue)), sp.getSpanStart(urlSpan), sp.getSpanEnd(urlSpan), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            } else {
                contentBinding?.tvContent?.setPadding(DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f))
                contentBinding?.tvContent?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_black))

                if (sp != null && sp.isNotEmpty()) {
                    val spans = sp.getSpans(0, sp.length, URLSpan::class.java)
                    for (urlSpan in spans) {
                        sp.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.link_blue)), sp.getSpanStart(urlSpan), sp.getSpanEnd(urlSpan), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }

        contentBinding?.tvContent?.text = uiMessage!!.contentSpannable
    }
    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgTextBinding {
       return ItemMsgTextBinding.inflate(LayoutInflater.from(context),contentView, true)
    }

}