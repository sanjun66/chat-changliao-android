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
import com.legend.imkit.databinding.ItemMsgImageBinding

class ImageMsgProvider: BaseMessageItemProvider<FileMsgContent, ItemMsgImageBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
        contentBinding?.apply {
//            ImgLoader.loadChatImage(context, data?.url, bivPic)
        }
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        contentBinding?.apply {
            val imgMsg = item.message as FileMsg
            val localPath = imgMsg.extra.getLocalPath(item.message.session_id, FileUtils.FILE_TYPE_IMAGE)
            if (FileUtils.hasFile(localPath)) {
                ImgLoader.loadChatImage(context, localPath, bivPic)
            } else if (FileUtils.hasFile(imgMsg.extra.path)) {
                ImgLoader.loadChatImage(context, imgMsg.extra.path, bivPic)
            } else {
                ImgLoader.loadChatImage(context, data?.url, bivPic)
            }
//            bivPic.setArrowLeftOrRight(item.message.isSender)
            if (item.message.sendStatus == TypeConst.msg_send_status_uploading) {
                lltProgress.visibility = View.VISIBLE
//                tvProgress.text = item.progress.toString()+"%"
            } else {
                lltProgress.visibility = View.GONE
//                tvProgress.text = "0%"
            }
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemMsgImageBinding {
        return ItemMsgImageBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_pic
}