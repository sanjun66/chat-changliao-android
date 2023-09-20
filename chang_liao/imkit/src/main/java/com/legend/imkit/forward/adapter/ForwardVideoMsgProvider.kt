package com.legend.imkit.forward.adapter

import android.text.TextUtils
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
import com.legend.imkit.databinding.ItemForwardFileMsgBinding
import com.legend.imkit.util.RongDateUtils

class ForwardVideoMsgProvider:
    BaseForwardItemProvider<FileMsgContent, ItemForwardFileMsgBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        contentBinding?.apply {
            val videoMsg = item.message as FileMsg
            val localPath = videoMsg.extra.getLocalPath(item.message.session_id, FileUtils.FILE_TYPE_IMAGE)
            val thumbnailPath = videoMsg.extra.thumbnailPath
            if (FileUtils.hasFile(thumbnailPath)) {
                ImgLoader.display(context, thumbnailPath, imgPic)
            } else if (FileUtils.hasFile(localPath)) {
                ImgLoader.display(context, localPath, imgPic)
            } else {
                ImgLoader.display(context, data?.cover, imgPic)
            }
            imgVideoSign.visibility = View.VISIBLE
            tvName.text = videoMsg.extra.original_name
            tvFileSize.text = RongDateUtils.getVideoDuration((data?.duration?:0).toInt())
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemForwardFileMsgBinding {
        return ItemForwardFileMsgBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_video
}