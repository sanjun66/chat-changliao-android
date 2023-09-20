package com.legend.imkit.chat

import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.legend.common.TypeConst
import com.legend.common.bean.UiMessage
import com.legend.imkit.viewholder.*

class Chat1Adapter(): BaseProviderMultiAdapter<UiMessage>() {
    override fun getItemType(data: List<UiMessage>, position: Int): Int {
        val message = data[position].message

        if (message.message_type == TypeConst.chat_msg_type_file) {
            return if (message.message_local_type > 0) message.message_local_type else message.message_type
        }

        return message.message_type
    }

    init {
        addItemProvider(TextMsgProvider())
        addItemProvider(FileDecryptProvider())
        addItemProvider(ImageMsgProvider())
        addItemProvider(VoiceMsgProvider())
        addItemProvider(VideoMsgProvider())
        addItemProvider(FileMsgProvider())
        addItemProvider(JoinGroupMsgProvide())
        addItemProvider(VideoCallStateMsgProvide())
        addItemProvider(AudioCallStateMsgProvider())
        addItemProvider(NewFriendMsgProvider())
        addItemProvider(ForwardMsgProvider())
    }

}

