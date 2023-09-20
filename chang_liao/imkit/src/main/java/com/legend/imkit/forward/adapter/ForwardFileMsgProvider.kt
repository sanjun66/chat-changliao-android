package com.legend.imkit.forward.adapter

import android.view.LayoutInflater
import android.widget.FrameLayout
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.TypeConst
import com.legend.common.bean.FileMsgContent
import com.legend.imkit.databinding.ItemForwardFileMsgBinding
import com.legend.common.utils.FileTypeUtils

class ForwardFileMsgProvider: BaseForwardItemProvider<FileMsgContent, ItemForwardFileMsgBinding>() {
    override fun onBindingContent(data: FileMsgContent?, position: Int) {
        contentBinding?.apply {
            tvName.setAdaptiveText(data?.original_name)
            tvFileSize.text = FileUtils.convertFileSize(data?.size?:0)
            ImgLoader.display(context, FileTypeUtils.fileTypeImageId(context, data?.original_name), imgPic)
        }
    }

    override fun onCreateMessageContentViewHolder(
        contentView: FrameLayout,
        itemViewType: Int
    ): ItemForwardFileMsgBinding {
        return ItemForwardFileMsgBinding.inflate(LayoutInflater.from(context), contentView, true)
    }

    override val itemViewType: Int
        get() = TypeConst.chat_msg_type_file_file
}