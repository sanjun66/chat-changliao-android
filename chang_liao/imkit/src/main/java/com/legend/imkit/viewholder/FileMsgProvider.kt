package com.legend.imkit.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.imkit.R
import com.legend.common.bean.FileMsgContent
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgFileBinding
import com.legend.common.utils.FileTypeUtils

class FileMsgProvider: BaseMessageItemProvider<FileMsgContent, ItemMsgFileBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
        contentBinding?.apply {
            rcMsgTvFileName.setAdaptiveText(data?.original_name)
            rcMsgTvFileSize.text = FileUtils.convertFileSize(data?.size?:0)
            ImgLoader.display(context, FileTypeUtils.fileTypeImageId(context, data?.original_name), rcMsgIvFileTypeImage)
        }
    }


    override fun convert(helper: BaseViewHolder, uiMessage: UiMessage) {
        super.convert(helper, uiMessage)
        contentBinding?.apply {
            if (uiMessage.message.isSender) {
                rcMessage.setBackgroundResource(R.drawable.message_text_send)
                contentBinding?.rcMsgTvFileName?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white))
                contentBinding?.rcMsgTvFileSize?.setTextColor(context.resources.getColor(com.com.legend.ui.R.color.white))
                contentBinding?.rcMessage?.setPadding(DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f))
            } else {
                rcMessage.setBackgroundResource(R.drawable.message_text_receive)
                contentBinding?.rcMsgTvFileName?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_black))
                contentBinding?.rcMsgTvFileSize?.setTextColor(context.resources.getColor(com.legend.commonres.R.color.text_black))
                contentBinding?.rcMessage?.setPadding(DisplayUtils.dp2px(context, 16f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f), DisplayUtils.dp2px(context, 10f))
            }

            if (uiMessage.message.sendStatus == TypeConst.msg_send_status_uploading) {
                rcMsgPbFileUploadProgress.visibility = View.VISIBLE
                rcMsgPbFileUploadProgress.setProgress(uiMessage.progress)
            } else {
                rcMsgPbFileUploadProgress.visibility = View.GONE
                rcMsgPbFileUploadProgress.setProgress(0)
            }

        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgFileBinding {
        return ItemMsgFileBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_file
}