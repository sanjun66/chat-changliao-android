package com.legend.imkit.chat

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.ClipBoardUtils
import com.legend.base.utils.FileUtils
import com.legend.base.utils.GlobalGsonUtils
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.common.Router
import com.legend.common.TypeConst
import com.legend.common.bean.FileMsg
import com.legend.common.bean.FileMsgContent
import com.legend.common.bean.ForwardMsgContent
import com.legend.common.db.DbManager
import com.legend.common.db.entity.ChatMessageModel
import com.legend.imkit.R
import com.legend.common.bean.UiMessage
import com.legend.common.network.viewmodel.MainRequest
import com.legend.common.network.viewmodel.MsgViewModel
import com.legend.common.socket.MSocket
import com.legend.common.utils.*
import com.legend.imkit.manager.AudioPlayManager
import com.legend.imkit.manager.IAudioPlayListener
import com.legend.common.utils.picture.PictureSelectorUtil
import com.legend.imkit.mention.RongMentionManager
import com.legend.imkit.popwindow.MsgActionPopup
import com.legend.imkit.popwindow.OnPopActionClickListener
import com.legend.imkit.util.*
import com.legend.imkit.videocall.util.getString
import com.luck.picture.lib.entity.LocalMedia
import com.quickblox.users.model.QBUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.io.File

class ChatActivitySub(val context: Chat1Activity) {
    private val handler = Handler(Looper.getMainLooper())
    private var chatAdapter: Chat1Adapter? = null
    private var rvChatList: RecyclerView? = null
    private var msgActionPopup: MsgActionPopup? = null
    private var mGroupId = ""
    private var msgViewMode: MsgViewModel? = null
    var selectedUiMsgList = mutableListOf<UiMessage>()
    private val FORWARD_MAX_NUM = 50
    var isEdit = false

    var qbUser: QBUser? = null
//    lateinit var chatDataList: MutableList<UiMessage>

    fun setConfig(adapter: Chat1Adapter?, msgViewModel: MsgViewModel, rvChat: RecyclerView?) {
        this.chatAdapter = adapter
//        this.chatDataList = chatList
        this.rvChatList = rvChat
        this.msgViewMode = msgViewModel
        init()
    }

    fun setGroupId(groupId: String) {
        mGroupId = groupId
    }

    private fun init() {
        initActionPopup()

        chatAdapter?.addChildClickViewIds(R.id.llt_secret_msg, R.id.fl_content, R.id.img_left_portrait, R.id.img_right_portrait, R.id.rc_v_edit)
        chatAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val uiMessage = adapter.getItem(position) as UiMessage
            when (view.id) {
                R.id.llt_secret_msg -> {
                    if (uiMessage.message.sendStatus != TypeConst.msg_send_status_sent) {
                        context.decryptMsg(uiMessage.message.id, pwd = uiMessage.message.pwd, message = GlobalGsonUtils.toJson(uiMessage.message))
                    } else {
                        context.decryptMsg(uiMessage.message.id)
                    }
                }
                R.id.fl_content -> {
                    when (uiMessage.message.message_type) {
                        TypeConst.chat_msg_type_file -> {
                            when(uiMessage.message.message_local_type) {
                                TypeConst.chat_msg_type_file_pic -> OpenFileUtil.openPicOrVideo(context, uiMessage, chatAdapter?.data)
                                TypeConst.chat_msg_type_file_voice -> onVoiceClicked(uiMessage)
                                TypeConst.chat_msg_type_file_file -> OpenFileUtil.openFile(context, uiMessage)
                                TypeConst.chat_msg_type_file_video -> OpenFileUtil.openPicOrVideo(context, uiMessage, chatAdapter?.data)
                            }
                        }
                        TypeConst.chat_msg_type_audio_call_state -> {
                            context.startCall(false)
                        }
                        TypeConst.chat_msg_type_video_call_state -> {
                            context.startCall(true)
                        }
                        TypeConst.chat_msg_type_forward -> {
                            Router.toForwardMsgActivity()
                            val forwardMsg: ChatMessageModel<ForwardMsgContent> = uiMessage.message as ChatMessageModel<ForwardMsgContent>
                            Log.i("byy", "发送数据 : " + GlobalGsonUtils.toJson(forwardMsg.extra))
                            LiveEventBus.get<UiMessage>(EventKey.key_forward_content_data).post(uiMessage)
                        }
                    }
                }
                R.id.img_left_portrait, R.id.img_right_portrait -> {
                    val uid = uiMessage.userInfo.uid.substring(1)
                    ChatUtil.toUserActivity(uid == ApplicationConst.getUserId(), uid, mGroupId)
                }
                R.id.rc_v_edit -> {
                    if (uiMessage.isSelected) {
                        uiMessage.isSelected = false
                        selectedUiMsgList.remove(uiMessage)
                    } else {
                        if (selectedUiMsgList.size >= FORWARD_MAX_NUM) {
                            ToastUtils.show(String.format(getString(R.string.forward_max_num_tip), FORWARD_MAX_NUM))
                            return@setOnItemChildClickListener
                        }
                        uiMessage.isSelected = true
                        selectedUiMsgList.add(uiMessage)
                    }
                    refreshItemChanged(position)
                }
            }
        }

        chatAdapter?.addChildLongClickViewIds(R.id.llt_secret_msg, R.id.fl_content, R.id.img_left_portrait)
        chatAdapter?.setOnItemChildLongClickListener { adapter, view, position ->
            val msg = adapter.getItem(position) as UiMessage
            when (view.id) {
                R.id.llt_secret_msg -> {
                    val contentView = view.findViewById<LinearLayout>(R.id.llt_secret_msg)
                    msgActionPopup?.showItemPopMenu(position, msg, contentView)
                    true
                }
                R.id.fl_content -> {
                    val contentView = view.findViewById<FrameLayout>(R.id.fl_content)
                    msgActionPopup?.showItemPopMenu(position, msg, contentView)
                    true
                }
                R.id.img_left_portrait -> {
                    RongMentionManager.getInstance().mentionMember(msg.userInfo)
                    true
                }
                else -> {
                    false
                }
            }
        }

        msgViewMode?.revokeMsgRes?.observe(context) {
            MSocket.instance.updateRevMsgRevokeDb(it.id)
            context.doOnRevokeRevMsg(it.id)
        }
    }

    private fun initActionPopup() {
        msgActionPopup = MsgActionPopup(context)
        msgActionPopup?.mOnPopActionClickListener = object : OnPopActionClickListener {
            override fun onCopyClicked(position: Int, msg: UiMessage) {
                ClipBoardUtils.clipboardCopyText(context, "message", msg.message.message)
            }

            override fun onForwardClicked(position: Int, msg: UiMessage) {
                Router.toPickChatActivity(context, context.REQUEST_FORWARD_MSG, msg.message.id, TypeConst.forward_type_one_by_one)
            }

            override fun onMultiSelectClick(position: Int, msg: UiMessage) {
                isEdit = true
                selectedUiMsgList.clear()
                val dataList = chatAdapter?.data
                for (data in dataList!!) {
                    data.isEdit = true
                    if (data.message.id == msg.message.id) {
                        data.isSelected = true
                        selectedUiMsgList.add(data)
                    }
                }
                refreshMsgList(false)

                context.tvBottomAction?.visibility = View.VISIBLE
                context.lltBottomInput?.visibility = View.GONE
                handler.postDelayed(Runnable { context.hideBottomLayout() }, 500)
            }

            override fun onSendMessageClicked(msg: UiMessage, retry: Boolean) {
                if (retry) {
                    when(msg.message.message_type) {
                        TypeConst.chat_msg_type_text -> {
                            if (TextUtils.isEmpty(msg.message.uuid)) msg.message.uuid = msg.message.id
                            val sendMessage = ChatDataConvertUtil.uiMessageToSendMsg(msg)
                            context.msgViewModel.sendMsg(GlobalGsonUtils.toJson(sendMessage), msg.message.id, TypeConst.chat_msg_type_text)
                        }
                        TypeConst.chat_msg_type_file -> {
                            if (TextUtils.isEmpty(msg.message.uuid)) msg.message.uuid = msg.message.id
                            val fileMsg = msg.message as FileMsg
                            if (fileMsg.extra.type != TypeConst.chat_msg_type_file_sub_video && !TextUtils.isEmpty(fileMsg.extra.url)) {
                                val sendMessage = ChatDataConvertUtil.uiMessageToSendMsg(msg)
                                context.msgViewModel.sendMsg(GlobalGsonUtils.toJson(sendMessage), msg.message.id, TypeConst.chat_msg_type_file)
                            } else if (fileMsg.extra.type == TypeConst.chat_msg_type_file_sub_video && !TextUtils.isEmpty(fileMsg.extra.url) && !TextUtils.isEmpty(fileMsg.extra.cover)) {
                                val sendMessage = ChatDataConvertUtil.uiMessageToSendMsg(msg)
                                context.msgViewModel.sendMsg(GlobalGsonUtils.toJson(sendMessage), msg.message.id, TypeConst.chat_msg_type_file)
                            } else {
                                context.uploadAndSendFileMsg(msg)
                            }
                        }
                    }

                }
            }

            override fun onDeleteMessageClick(position: Int, msg: UiMessage) {
                doDeleteAction(position, msg)
            }

            override fun onRevokeMessageClick(position: Int, msg: UiMessage) {
                msgViewMode?.revokeMsg(msg.message.id, context.createUUid())
            }

        }
    }

    private fun doDeleteAction(position: Int, uiMessage: UiMessage) {
        MainRequest.deleteMsg(uiMessage.message.id)
        if(uiMessage.message.message_type == TypeConst.chat_msg_type_file) {
            if (uiMessage.message.sendStatus != TypeConst.msg_send_status_sent) {
                if (!TextUtils.isEmpty(uiMessage.message.uuid)) context.uploadTask.remove(uiMessage.message.uuid)
            }
            when(uiMessage.message.message_local_type) {
                TypeConst.chat_msg_type_file_voice -> {
                    val playingUri = AudioPlayManager.getInstance().playingUri
                    if (playingUri != null) {
                        val fileContent = uiMessage.message.extra as FileMsgContent
                        val uri = Uri.parse(fileContent.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_VOICE))
                        if (uri != null && playingUri == uri) {
                            AudioPlayManager.getInstance().stopPlay()
                        }
                    }
                }
            }
        }
        if (position == 0) {
            // 去更新会话列表
            var temp: ChatMessageModel<Any>
            if (chatAdapter?.data?.size!! == 1) {
                temp = uiMessage.message
                temp.message = ""
            } else {
                temp = (chatAdapter?.getItem(1) as UiMessage).message
            }
            LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_from_chat).post(temp)
        }

        // 删除消息
        refreshDeleteItem(position)
        // 删除数据库
        deleteMsgFromDbAndFile(uiMessage)
    }

    fun clearEdit() {
        if (!isEdit) return
        isEdit = false
        selectedUiMsgList.clear()
        val dataList = chatAdapter?.data
        for (data in dataList!!) {
            data.isEdit = false
            data.isSelected = false
        }
        refreshMsgList(false)

        context.tvBottomAction?.visibility = View.GONE
        context.lltBottomInput?.visibility = View.VISIBLE
    }
    
    private fun onVideoClicked(uiMessage: UiMessage) {
        val videoMsg: FileMsg = uiMessage.message as FileMsg
        val videoLocalPath = videoMsg.extra.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_VIDEO)
        val imgLocalPath = videoMsg.extra.getLocalCover(uiMessage.message.session_id)
        val mediaList = arrayListOf<LocalMedia>()
        if (FileUtils.hasFile(videoLocalPath)) {
            val media: LocalMedia = LocalMedia.generateLocalMedia(context, videoLocalPath)
            mediaList.add(media)
        } else {
            ImFileDownloadUtil.downloadWithNoProgress(videoMsg.extra.url, videoLocalPath, null)
            val media: LocalMedia = LocalMedia.generateHttpAsLocalMedia(videoMsg.extra.url)
            mediaList.add(media)
        }
        if (!FileUtils.hasFile(videoMsg.extra.thumbnailPath) && !FileUtils.hasFile(imgLocalPath)) {
            ImFileDownloadUtil.downloadWithNoProgress(videoMsg.extra.cover, imgLocalPath, null)
        }

        PictureSelectorUtil.picturePreview(context, 0, mediaList)
    }



    private fun onPicClicked(uiMessage: UiMessage) {
        val imgMsg: FileMsg = uiMessage.message as FileMsg
        val localPath = imgMsg.extra.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_IMAGE)
        val mediaList = arrayListOf<LocalMedia>()
        if (FileUtils.hasFile(localPath)) {
            val media: LocalMedia = LocalMedia.generateLocalMedia(context, localPath)
            mediaList.add(media)
        } else {
            ImFileDownloadUtil.downloadWithNoProgress(imgMsg.extra.url, localPath, null)
            val media: LocalMedia = LocalMedia.generateHttpAsLocalMedia(imgMsg.extra.url)
            mediaList.add(media)
        }

        PictureSelectorUtil.picturePreview(context, 0, mediaList)
    }

    // 音频
    private fun onVoiceClicked(uiMessage: UiMessage) {
        val fileContent = uiMessage.message.extra as FileMsgContent
        if (AudioPlayManager.getInstance().isPlaying) {
            val playingUri = AudioPlayManager.getInstance().playingUri
            AudioPlayManager.getInstance().stopPlay()
            // 暂停的是当前播放的 Uri
            if (playingUri.equals(Uri.parse(fileContent.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_VOICE))) || playingUri.equals(Uri.parse(fileContent.url))) return
        }
        // 如果被 voip 占用通道，则不播放，弹提示框
        if (AudioPlayManager.getInstance().isInVOIPMode(context.applicationContext)) {
            ToastUtils.show(context.getString(R.string.rc_voip_occupying))
            return
        }
        playOrDownloadVoiceMsg(uiMessage)
    }

    private fun playOrDownloadVoiceMsg(uiMessage: UiMessage) {
        val fileContent = uiMessage.message.extra as FileMsgContent
        val localPath = fileContent.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_VOICE)
        if (FileUtils.hasFile(localPath)) {
            playVoiceMessage(uiMessage)
        } else {
            downloadVoiceMsg(uiMessage)
        }
    }

    private fun downloadVoiceMsg(uiMessage: UiMessage) {
        val fileContent = uiMessage.message.extra as FileMsgContent
        ImFileDownloadUtil.downloadWithProgress(fileContent.url!!, fileContent.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_VOICE), object : ImFileDownloadUtil.DownloadListener {
            override fun onStart() {
                // 正常情况下只有接收方才需要下载
                uiMessage.message.revStatus = TypeConst.msg_rev_status_downloading
                refreshSingleMessage(uiMessage)
            }

            override fun onProgress(progress: Int) {
            }

            override fun onCompleted() {
                uiMessage.message.revStatus = TypeConst.msg_rev_status_downloaded
                refreshSingleMessage(uiMessage)
                playVoiceMessage(uiMessage)
            }

            override fun onFail() {
                uiMessage.message.revStatus = TypeConst.msg_rev_status_download_fail
                refreshSingleMessage(uiMessage)
            }

        })
    }

    private fun playVoiceMessage(uiMessage: UiMessage) {
        val fileContent = uiMessage.message.extra as FileMsgContent
        val voicePath = fileContent.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_VOICE)

        AudioPlayManager.getInstance().startPlay(context.applicationContext, Uri.parse(voicePath), object: IAudioPlayListener {
            override fun onStart(uri: Uri?) {
                Log.i("websocket", "voice 播放 onStart ")
                uiMessage.isPlaying = true
                // to do 回复已听消息，如果是接收方且消息破损 readTime设置未0，上报服务器等操作
                uiMessage.message.revStatus = TypeConst.msg_rev_status_listened

                if (!(uiMessage.message.isSender && uiMessage.message.isFromAndroid)) {
                    uiMessage.message.revStatus = TypeConst.msg_rev_status_listened
                    refreshItemChanged(findPositionByMessageId(uiMessage.message.id))
                    updateDbMsgStatus(uiMessage.message.id, null, TypeConst.msg_rev_status_listened)
                }
                refreshSingleMessage(uiMessage)
            }

            override fun onStop(uri: Uri?) {
                Log.i("websocket", "voice 播放 onStop ")
                uiMessage.isPlaying = false
                // to do 如果是接收方且消息破损 readTime设置未0，上报服务器操作

                refreshSingleMessage(uiMessage)
            }

            override fun onComplete(uri: Uri?) {
                Log.i("websocket", "voice 播放 onComplete ")
                uiMessage.isPlaying = false
                // to do 如果是接收方且消息破损 readTime设置未0，上报服务器操作

                refreshSingleMessage(uiMessage)
                // 找到下个播放消息继续播放
                // 不切换线程会造成，ui 一直显示播放的 bug
//                ExecutorHelper.getInstance().mainThread().execute(Runnable {
//                    findNextHQVoice(uiMessage)
//                })
            }

        })
    }

    private fun findNextHQVoice(uiMessage: UiMessage) {
        val position = findPositionByMessageId(uiMessage.message.id)
        if (position == -1 || position + 1 >= chatAdapter?.data!!.size) return
        for (i in position + 1  until chatAdapter?.data!!.size) {
            val item = chatAdapter?.data!![i]
            if (item.message.extra is FileMsgContent) {
                if (item.message.revStatus != TypeConst.msg_rev_status_listened) {
                    onVoiceClicked(item)
                    break
                }
            }
        }

    }

    fun refreshSingleMessage(uiMessage: UiMessage) {
        uiMessage.change()
        val position = findPositionByMessageId(uiMessage.message.id)
        refreshItemChanged(position)
//        if (position >= 0) {
//            uiMessage.isChange = true
//            chatAdapter?.notifyItemChanged(position)
//        }
    }

    private fun findPositionByMessageId(msgId: String) : Int {
        for ((index, msg) in chatAdapter?.data!!.withIndex()) {
            if (msg.message.id == msgId) return index
        }

        return -1
    }

    fun findPositionByUUid(uuid: String?): Int {
//        Log.i("websocket", "去找uid = $uuid")
        if (TextUtils.isEmpty(uuid) && chatAdapter?.data?.isEmpty()!!) return -1
        for ((index, msg) in chatAdapter?.data!!.withIndex()) {
//            if (index == 0)Log.i("websocket", "第0个数据第uuid = " + msg.message.uuid)
            if (!TextUtils.isEmpty(msg.message.uuid) && uuid == msg.message.uuid) return index
        }

        return -1
    }

    fun deleteMsgFromDbAndFile(uiMessage: UiMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            val msg = ChatDataConvertUtil.chatMsgModelConvertDb(uiMessage.message)
            DbManager.getSoChatDB().msgDao().delete(msg)

            if (uiMessage.message.message_type == TypeConst.chat_msg_type_file) {
                val fileMsg = uiMessage.message as FileMsg
                when(uiMessage.message.message_local_type) {
                    TypeConst.chat_msg_type_file_file -> FileUtils.deleteAllFile(File(fileMsg.extra.getLocalPath(fileMsg.session_id, FileUtils.FILE_TYPE_FILE)))
                    TypeConst.chat_msg_type_file_voice -> FileUtils.deleteAllFile(File(fileMsg.extra.getLocalPath(fileMsg.session_id, FileUtils.FILE_TYPE_VOICE)))
                    TypeConst.chat_msg_type_file_pic -> {
                        FileUtils.deleteAllFile(File(fileMsg.extra.getLocalPath(fileMsg.session_id, FileUtils.FILE_TYPE_IMAGE)))
                        // to do 从LocalMedia里找到并删除（预览的时候去加入LocalMedial了）
                    }
                    TypeConst.chat_msg_type_file_video -> {
                        val videoPath = fileMsg.extra.getLocalPath(fileMsg.session_id, FileUtils.FILE_TYPE_VIDEO)
                        FileUtils.deleteAllFile(File(videoPath))
                        val coverPath = fileMsg.extra.getLocalCover(fileMsg.session_id)
                        FileUtils.deleteAllFile(File(coverPath))
                        val thumbnailPath = fileMsg.extra.thumbnailPath
                        FileUtils.deleteAllFile(File(thumbnailPath))
                        // to do 从LocalMedia里找到并删除（预览的时候去加入LocalMedial了）
                    }
                }
            }
        }
    }

    fun refreshItemChanged(position: Int) {
        if (position < 0) return
        handler.post {
            chatAdapter?.notifyItemChanged(position)
        }
    }

    fun refreshAddItem(data: UiMessage, scrollerToBottom: Boolean) {
        handler.post {
//            Log.i("websocket", "添加到0 刷新 = " + data.message.uuid)
            chatAdapter?.addData(0, data)
            if (scrollerToBottom && chatAdapter?.data!!.size > 0) rvChatList?.smoothScrollToPosition(0)
        }
    }

    fun refreshDeleteItem(position: Int) {
        handler.post {
            chatAdapter?.removeAt(position)
//            if (chatAdapter?.data!!.size > 0) rvChatList?.smoothScrollToPosition(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshMsgList(scrollerToBottom: Boolean) {
        handler.post() {
            chatAdapter?.notifyDataSetChanged()
            if (scrollerToBottom && chatAdapter?.data!!.size > 0) rvChatList?.scrollToPosition(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshMsgList(toPosition: Int) {
        handler.post {
            chatAdapter?.notifyDataSetChanged()
            val size = chatAdapter?.data!!.size
            Log.i("byy", "refreshMsgList -> toPosition = $toPosition, size = $size")
            if (size > toPosition) rvChatList?.scrollToPosition(toPosition) else rvChatList?.scrollToPosition(size - 1)
        }
    }


    fun updateDbMsgStatus(msgId: String, sendStatus: Int?, revStatus: Int?) {
        if (sendStatus == null && revStatus == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val msgItem = DbManager.getSoChatDB().msgDao().getMsgById(msgId)
            msgItem?.let {
                if (sendStatus != null) msgItem.send_status = sendStatus
                if (revStatus != null) msgItem.rev_status = revStatus
                DbManager.getSoChatDB().msgDao().update(msgItem)
            }
        }
    }



    fun onDestroy() {

    }



}