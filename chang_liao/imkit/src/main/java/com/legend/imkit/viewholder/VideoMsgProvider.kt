package com.legend.imkit.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.bean.FileMsg
import com.legend.common.bean.FileMsgContent
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemMsgVideoBinding
import com.legend.imkit.util.RongDateUtils

class VideoMsgProvider: BaseMessageItemProvider<FileMsgContent, ItemMsgVideoBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
        contentBinding?.apply {
            tvDuration.text = RongDateUtils.getVideoDuration((data?.duration?:0).toInt())
//            ImgLoader.loadChatImage(context, data?.cover, bivPic)

        }
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        contentBinding?.apply {
            val videoMsg = item.message as FileMsg
            val localPath = videoMsg.extra.getLocalPath(item.message.session_id, FileUtils.FILE_TYPE_IMAGE)
            val thumbnailPath = videoMsg.extra.thumbnailPath
            if (FileUtils.hasFile(thumbnailPath)) {
                ImgLoader.loadChatImageFl(context, thumbnailPath, bivPic)
            } else if (FileUtils.hasFile(localPath)) {
                ImgLoader.loadChatImageFl(context, localPath, bivPic)
            } else {
                ImgLoader.loadChatImageFl(context, data?.cover, bivPic)
            }

//            bivPic.setArrowLeftOrRight(item.message.isSender)
            if (item.message.sendStatus == TypeConst.msg_send_status_uploading) {
                compressVideoBar.visibility = View.VISIBLE
                compressVideoBar.progress = item.progress
            } else {
                compressVideoBar.visibility = View.GONE
                compressVideoBar.progress = 0
            }
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgVideoBinding {
        return ItemMsgVideoBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_video
}