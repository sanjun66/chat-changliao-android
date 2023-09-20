package com.legend.imkit.forward.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.bean.FileMsg
import com.legend.common.bean.FileMsgContent
import com.legend.common.bean.UiMessage
import com.legend.imkit.databinding.ItemForwardImgMsgBinding

class ForwardImageMsgProvider: BaseForwardItemProvider<FileMsgContent, ItemForwardImgMsgBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
    }

    override fun convert(helper: BaseViewHolder, item: UiMessage) {
        super.convert(helper, item)
        contentBinding?.apply {
            val imgMsg = item.message as FileMsg
            val localPath = imgMsg.extra.getLocalPath(item.message.session_id, FileUtils.FILE_TYPE_IMAGE)
            if (FileUtils.hasFile(localPath)) {
                ImgLoader.display(context, localPath, imgPic)
            }
//            else if (FileUtils.hasFile(imgMsg.extra.path)) {
//                ImgLoader.display(context, localPath, imgPic)
//            }
            else {
                ImgLoader.display(context, data?.url, imgPic)
            }
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemForwardImgMsgBinding {
        return ItemForwardImgMsgBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_pic
}