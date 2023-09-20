package com.legend.main.home.fragment

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.FileUtils
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.NetworkUtils
import com.legend.baseui.ui.base.BaseFragment
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.common.Router
import com.legend.common.TypeConst
import com.legend.common.bean.*
import com.legend.common.db.DbManager
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.DBEntity
import com.legend.common.db.entity.SimpleMessage
import com.legend.common.network.viewmodel.MsgViewModel
import com.legend.common.socket.MSocket
import com.legend.common.socket.MessageRevListener
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.imkit.bean.ChatBean
import com.legend.imkit.popwindow.ChatListPopup
import com.legend.common.utils.ChatDataConvertUtil
import com.legend.main.R
import com.legend.main.databinding.FragmentChatlIstBinding
import com.legend.main.home.adapter.ChatListAdapter
import kotlinx.coroutines.*
import razerdp.util.animation.AlphaConfig
import razerdp.util.animation.AnimationHelper
import java.io.File
import java.util.*

class ChatListFragment: BaseFragment<FragmentChatlIstBinding>() {
    private var chatListData: MutableList<DBEntity.ChatListEntity> = mutableListOf()
    private val handler = Handler(Looper.getMainLooper())

    private var chatListAdapter: ChatListAdapter? = null
    private var msgRevListener: MessageRevListener? = null

    private var curSessionUid = ""
    private var loadedHistory = false
    private var didOfflineMsg = false
    private var loadOfflineMsgFinish = false
    private var offlineMessageList: MutableList<ChatMessageModel<Any>> = mutableListOf()
    private val offlineChatList = hashMapOf<String, ChatMessageModel<Any>>() // 离线会话列表
    private val offlineChatListSize = hashMapOf<String, Int>()  // 离线会话列表未读消息数
    private var offlineChatShowMention = hashMapOf<String, Boolean>()   // 离线消息会话列表是否显示提示消息
    private val offlineMsgPerPageNum = 100
    private var countDownTimer: CountDownTimer? = null
    
    private var offlineDidCompleteOnce = false
    private var unReadNumTotal = 0
    private var isFirstIn = true

    val msgViewModel: MsgViewModel by lazy {
        ViewModelProvider(this)[MsgViewModel::class.java]
    }
    override fun getLayoutId(): Int = R.layout.fragment_chatl_ist

    override fun initView(view: View?) {
        mDataBinding?.recyclerView?.let {
            it.layoutManager = LinearLayoutManager(mContext)
            chatListAdapter = ChatListAdapter()
            it.adapter = chatListAdapter
            chatListAdapter?.setOnItemClickListener { adapter, view, position ->
                val item: DBEntity.ChatListEntity = adapter.data[position] as DBEntity.ChatListEntity
                Router.toChatActivity(item.session_id, item.nick_name)
                clearUnReadNum(item.session_id)
            }
            chatListAdapter?.setOnItemLongClickListener { adapter, view, position ->
                val chatListPop = ChatListPopup(requireContext())
                chatListPop.setAutoMirrorEnable(true)
                chatListPop.showAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.IN).toShow()
                chatListPop.dismissAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.OUT).toDismiss()
                chatListPop.popupGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                chatListPop.onItemClickListener = object : ChatListPopup.OnPopItemClickListener {
                    override fun onItemClicked(type: Int) {
                        if (type == chatListPop.typeDelete) {
                            doDeleteChat(position, adapter.getItem(position) as DBEntity.ChatListEntity)
                        }
                    }

                }
                chatListPop.showPopupWindow(view)
                true
            }
            chatListAdapter?.data= chatListData
        }
    }

    // 如果limit为3，每次viewCreated销毁都会走initData
    override fun initData() {
//        Log.i(TAG, "ChatListFragment initData")
        initEvent()
        if (!loadedHistory) doHistoryMessage()

        msgRevListener = object : MessageRevListener {
            override fun onMessageTextRev(message: ChatMessageModel<BaseMsgContent>) {
                doUpdateChatListSingle(message as ChatMessageModel<Any>)
            }
    
            override fun onMessageFileRev(message: ChatMessageModel<FileMsgContent>) {
                doUpdateChatListSingle(message as ChatMessageModel<Any>)
            }

            override fun onMessageForwardRev(message: ChatMessageModel<ForwardMsgContent>) {
                doUpdateChatListSingle(message as ChatMessageModel<Any>)
            }
    
            override fun onMessageGroupRev(message: ChatMessageModel<GroupMsgContent>) {
                doUpdateChatListSingle(message as ChatMessageModel<Any>)
            }

            override fun onMessageNewFriendRev(message: ChatMessageModel<NewFriendContent>) {
                doUpdateChatListSingle(message as ChatMessageModel<Any>)
            }

            override fun onMessageCallStateRev(message: ChatMessageModel<CallStateContent>) {
                doUpdateChatListSingle(message as ChatMessageModel<Any>)
            }

            override fun onOfflineRev(messageList: List<ChatMessageModel<Any>>) {
                Log.d("websocket", "chatList onOfflineRev --- size: " + messageList.size)
                if (messageList.isEmpty()) {
                    doOfflineMessage(false)
                    return
                }
                
                // 离线消息状态复位
                if (offlineDidCompleteOnce) {
                    offlineMessageList.clear()
                    offlineChatList.clear()
                    offlineChatListSize.clear()
                    offlineChatShowMention.clear()
                    offlineDidCompleteOnce = false
                    loadOfflineMsgFinish = false
                    didOfflineMsg = false
                }

                cancelOfflineMsgTimer()
                if (messageList.size == offlineMsgPerPageNum) {
                    startOfflineMsgTimer()
                }

                offlineMessageList.addAll(messageList)
                if (messageList.size < offlineMsgPerPageNum && loadedHistory) {
                    loadOfflineMsgFinish = true
                    doOfflineMessage()
                }
            }

            override fun onFriendApply(message: UserBean.FriendApply) {
                if (message.uid == ApplicationConst.getUserId() && message.flag == "taker") {
                    LiveEventBus.get<Boolean>(EventKey.key_have_friend_apply).post(true)
                }
            }

            override fun onRevokeRev(revokeMsg: SimpleMessage) {
                // 去找到对话 如果有 就去获取更新
                lifecycleScope.launch {
                    val position = findChatBeanByMsgId(revokeMsg.id)
                    if (position >= 0) {
                        val chatBean = chatListData[position]
                        val isGroup = chatBean.session_id.startsWith("g")
                        chatBean.message = MSocket.instance.getRevokeMsgTip(if (isGroup) TypeConst.talk_type_group_chat else TypeConst.talk_type_single_chat, chatBean.from_uid.toString())
                        chatListAdapter?.notifyItemChanged(position)
                    }
                }
            }
        }
        MSocket.instance.registerMsgRevListener(msgRevListener!!)

        msgViewModel.netChatListRes.observe(this) {
            if (it.isNullOrEmpty()) return@observe
            CoroutineScope(Dispatchers.IO).launch {
                val chatList= arrayListOf<DBEntity.ChatListEntity>()
                for (item in it) {
                    val chatListItem = DBEntity.ChatListEntity(if (item.talk_type == TypeConst.talk_type_single_chat) "s${item.id}" else "g${item.id}"
                        , ApplicationConst.getUserId().toInt(), item.id, item.name, item.avatar, 0, 0, 0
                        , ChatDataConvertUtil.getChatListShowMsg(item.message, item.message_type, item.is_pwd == TypeConst.type_yes), item.msg_id, item.timestamp, ""
                        , item.is_pwd == TypeConst.type_yes, false, item.is_disturb, item.message_type)
                    chatList.add(chatListItem)
                }
                if (chatList.isNotEmpty()) {
                    DbManager.getSoChatDB().chatListDao().insertAll(chatList)
                    chatListData.addAll(chatList)
                    dataNotify()
                }
            }
        }
    }

    private fun startOfflineMsgTimer() {
        handler.post {
            if (countDownTimer == null) {
                countDownTimer = object : CountDownTimer(1 * 1000L, 2 * 500L) {
                    override fun onTick(millisUntilFinished: Long) {
                        // do nothing
                    }

                    override fun onFinish() {
                        if (loadedHistory && !didOfflineMsg) {
                            loadOfflineMsgFinish = true
                            doOfflineMessage()
                        }
                    }
                }
            }

            countDownTimer?.start()
        }
    }

    private fun cancelOfflineMsgTimer() {
        handler.post {
            countDownTimer?.cancel()
            countDownTimer = null
        }
    }


    // websocket接收到到
    private fun doUpdateChatListSingle(message: ChatMessageModel<Any>) {
        lifecycleScope.launch {
//            Log.i("websocket", "会话列表页面 msgId = " + message.id)
//            if (!TextUtils.isEmpty(message.uuid)) {
//                val msg = DbManager.getSoChatDB().msgDao().getMsgById(message.uuid)
//                if (msg != null) return@launch
//            }

            val strUid = if (message.talk_type == TypeConst.talk_type_group_chat) {
                "g" + message.to_uid
            } else {
                if (message.to_uid == ApplicationConst.getUserId()) { "s" + message.from_uid } else { "s" + message.to_uid }
            }
            val avatarInfo = UserSimpleDataHelper.getUserInfo(strUid)
            val chatListEntity = DBEntity.ChatListEntity(message.session_id, message.from_uid.toInt(), message.to_uid.toInt(), avatarInfo?.nick_name?: "" , avatarInfo?.avatar ?: ""
                , message.is_read, DBEntity.showUnReadNum, 0, ChatDataConvertUtil.getChatListShowMsg(message.message, message.message_type, message.is_secret), message.id, message.timestamp, "", message.is_secret
                , isWarnMe(message.warn_users),avatarInfo?.is_disturb?:0, message.message_type)
            updateChatBean(chatListEntity)
        }
    }

    private fun isWarnMe(warnUsers: String): Boolean {
        if (!TextUtils.isEmpty(warnUsers)) {
            if (warnUsers.contains(",")) {
                val ids = warnUsers.split(",")
                for (id in ids) {
                    if (id == "0" || id == ApplicationConst.getUserId()) return true
                }
            } else {
                if (warnUsers == "0" || warnUsers == ApplicationConst.getUserId()) return true
            }
        }

        return false
    }

    private fun doDeleteChat(position: Int, entity: DBEntity.ChatListEntity) {
        lifecycleScope.launch {
            val talkType = if (entity.session_id.startsWith("s")) TypeConst.talk_type_single_chat else TypeConst.talk_type_group_chat
            msgViewModel.deleteChatList(entity.to_uid.toString(), talkType)
            // 删除会话item
            chatListData.removeAt(position)
            refreshRemoveItem(position)
            // 删除会话数据库
            DbManager.getSoChatDB().chatListDao().delete(entity)
            // 删除会话下的消息
            DbManager.getSoChatDB().msgDao().deleteMsgBySessionId(entity.session_id)
            // 删除会话下的文件
            val file = FileUtils.getAppRootPath(context, entity.session_id)
            FileUtils.deleteAllFile(File(file))
        }
    }


    private fun doHistoryMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            val data: List<DBEntity.ChatListEntity>? = DbManager.getSoChatDB().chatListDao().getAllChatList()
            if (data.isNullOrEmpty()) {
                doHistoryMsgFinished()
            } else {
                val chatListDataFromDb: MutableList<DBEntity.ChatListEntity> = mutableListOf()
                chatListDataFromDb.addAll(data)
                updateHistoryChatListData(chatListDataFromDb)
                doHistoryMsgFinished()
            }
        }
    }

    private fun doHistoryMsgFinished() {
        Log.i("websocket", "doHistoryMsg finished")
        loadedHistory = true
        if (loadOfflineMsgFinish && !didOfflineMsg) {
            doOfflineMessage()
        }
    }

    private fun doOfflineMessage() {
        doOfflineMessage(true)
    }
    private fun doOfflineMessage(needContinueHandle: Boolean) {
        didOfflineMsg = true
        if (!needContinueHandle) {
            getNetHistoryChatList()
            LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_handle_down)
            return
        }
        cancelOfflineMsgTimer()
        val offlineMsgDb = mutableListOf<DBEntity.ChatMessageEntity>()
        val ids = StringBuilder()
        for ((index, offlineMsg) in offlineMessageList.withIndex()) {
            offlineMsgDb.add(ChatDataConvertUtil.chatMsgModelConvertDb(offlineMsg))
            if (index == 0) {
                ids.append(offlineMsg.id)
            } else {
                ids.append(",").append(offlineMsg.id)
            }
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                DbManager.getSoChatDB().msgDao().insertAll(offlineMsgDb)
                MSocket.instance.sendMessage(GlobalGsonUtils.toJson(ChatBean.SendAckOfflineMessage(ids.toString())))
                for (offlineItem in offlineMessageList) {
                    // 离线消息从小到大排序
                    offlineChatList[offlineItem.session_id] = offlineItem
                    offlineChatListSize[offlineItem.session_id] = (offlineChatListSize[offlineItem.session_id]?:0) + 1
                    if (isWarnMe(offlineItem.warn_users)) offlineChatShowMention[offlineItem.session_id] = true
                }
                if (offlineChatList.size > 0) {
                    val temp = arrayListOf<ChatMessageModel<Any>>()
                    temp.addAll(offlineChatList.values)

                    val chatListEntities = mutableListOf<DBEntity.ChatListEntity>()
                    for (message in temp) {
                        val targetId = if (message.talk_type == TypeConst.talk_type_group_chat) {
                            "g" + message.to_uid
                        } else {
                            if (message.to_uid == ApplicationConst.getUserId()) { "s" + message.from_uid } else { "s" + message.to_uid }
                        }
                        val userSimpleInfo = UserSimpleDataHelper.getUserInfo(targetId)
                        chatListEntities.add(DBEntity.ChatListEntity(message.session_id, message.from_uid.toInt(), message.to_uid.toInt(),userSimpleInfo?.nick_name?: "" , userSimpleInfo?.avatar ?: ""
                            , message.is_read, DBEntity.showUnReadNum, offlineChatListSize[message.session_id]?:0, ChatDataConvertUtil.getChatListShowMsg(message.message, message.message_type, message.is_secret), message.id, message.timestamp, ""
                            , message.is_secret, offlineChatShowMention[message.session_id]?:false, userSimpleInfo?.is_disturb?:0, message.message_type))
                    }
                    DbManager.getSoChatDB().chatListDao().insertAll(chatListEntities)
                    updateOfflineChatList(chatListEntities)
                }

                sendDoOfflineFinishEvent()
                Log.i("websocket", "doOffline finished ---")
            }
        }
    }

    private fun getNetHistoryChatList() {
        if (ApplicationConst.IS_SUPPORT_HISTORY == TypeConst.type_yes && isFirstIn) {
            isFirstIn = false
            CoroutineScope(Dispatchers.IO).launch {
                val list = DbManager.getSoChatDB().chatListDao().getAllChatList()
                if (list.isNullOrEmpty()) msgViewModel.getNetChatList()
            }
        }
    }

    // 历史消息
    private fun updateHistoryChatListData(chatList: List<DBEntity.ChatListEntity>?) {
        if (chatList.isNullOrEmpty()) return
        var unReadNum = 0
        for (history in chatList) {
            unReadNum += history.unread_num
        }
        if (unReadNum > 0) refreshBarUnRead(true, unReadNum)

        synchronized(chatListData) {
            if (chatListData.isEmpty()) {
                chatListData.addAll(chatList)
            } else {
                val firstTime = chatList[0].timestamp
                for ((index, data) in chatListData.withIndex()) {
                    if (data.timestamp < firstTime) {
                        chatListData.addAll(index, chatList)
                        dataNotify()
                        return
                    }
                }
                chatListData.addAll(0, chatList)
            }
            dataNotify()
        }
    }

    // 离线消息
    private fun updateOfflineChatList(chatEntityList: List<DBEntity.ChatListEntity>) {
        synchronized(chatListData) {
            for (chatEntity in chatEntityList) {
                var isContained = false
                for ((index, data) in chatListData.withIndex()) {
                    if (data.session_id == chatEntity.session_id) {
                        if (chatListData[index].session_id != curSessionUid) {
                            refreshBarUnRead(true, offlineChatListSize[data.session_id]?:1)
                            chatListData[index].unread_num += offlineChatListSize[data.session_id]?:1
                            if (!chatListData[index].show_mention_tip && (offlineChatShowMention[data.session_id] == true)) {
                                chatListData[index].show_mention_tip = true
                            }
                        } else {
                            chatListData[index].show_mention_tip = false
                        }
                        chatListData[index].message = chatEntity.message
                        if (index != 0) Collections.swap(chatListData, index, 0)
                        isContained = true
                        break
                    }
                }
                if (!isContained) {
                    if (chatEntity.from_uid.toString() != ApplicationConst.getUserId()) {
                        refreshBarUnRead(true, offlineChatListSize[chatEntity.session_id]?:1)
                        if (chatEntity.from_uid.toString() != ApplicationConst.getUserId()) chatEntity.unread_num = offlineChatListSize[chatEntity.session_id]?:1
                        chatEntity.show_mention_tip = offlineChatShowMention[chatEntity.session_id]?:false
                    } else {
                        chatEntity.show_mention_tip = false
                    }
                    chatListData.add(0, chatEntity)
                }
            }

            dataNotify()
        }
    }

    // socket接收的消息
    private fun updateChatBean(chatEntity: DBEntity.ChatListEntity) {
        synchronized(chatListData) {
            // 当前会话列表里有
            for ((index, data) in chatListData.withIndex()) {
                if (data.session_id == chatEntity.session_id) {
                    if (chatEntity.session_id != curSessionUid) {
                        if (chatEntity.from_uid.toString() != ApplicationConst.getUserId()) {
                            refreshBarUnRead(true, 1)
                            chatEntity.unread_num = data.unread_num + 1
                        }
                        if (!chatEntity.show_mention_tip && data.show_mention_tip) {
                            chatEntity.show_mention_tip = true
                        }
                    } else {
                        chatEntity.show_mention_tip = false
                    }
                    chatListData.remove(data)
                    refreshRemoveItem(index)
                    chatListData.add(0, chatEntity)
                    refreshInsertItem(0)

                    lifecycleScope.launch {
                        DbManager.getSoChatDB().chatListDao().insert(chatEntity)
                    }
                    return
                }
            }

            if (chatEntity.from_uid.toString() != ApplicationConst.getUserId()) {
                refreshBarUnRead(true,  1)
                chatEntity.unread_num = 1
            } else {
                chatEntity.show_mention_tip = false
            }
            lifecycleScope.launch {
                DbManager.getSoChatDB().chatListDao().insert(chatEntity)
            }
            chatListData.add(0, chatEntity)
            refreshInsertItem(0)
        }
    }

    // 清除数据
    private fun clearUnReadNum(sessionUid: String) {
        if (chatListData.isEmpty()) return
        for ((index, data) in chatListData.withIndex()) {
            if (data.session_id == sessionUid) {
                refreshBarUnRead(false, data.unread_num)
                data.unread_num = 0
                data.show_mention_tip = false
                updateSingleDb(data)
                refreshChangedItem(index)
                return
            }
        }
    }

    private fun updateSingleDb(item: DBEntity.ChatListEntity) {
        lifecycleScope.launch {
            DbManager.getSoChatDB().chatListDao().insert(item)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun dataNotify() {
        handler.post {
            chatListAdapter?.notifyDataSetChanged()
        }
    }

    private fun refreshChangedItem(position: Int) {
        handler.post {
            chatListAdapter?.notifyItemChanged(position)
        }
    }

    private fun refreshRemoveItem(position: Int) {
        handler.post {
            chatListAdapter?.notifyItemRemoved(position)
        }
    }

    private fun refreshRemoveItem(item: DBEntity.ChatListEntity) {
        handler.post {
            chatListAdapter?.remove(item)
        }
    }

    private fun refreshInsertItem(position: Int) {
        handler.post {
            chatListAdapter?.notifyItemInserted(position)
        }
    }

    private fun sendDoOfflineFinishEvent() {
        getNetHistoryChatList()
        LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_handle_down)

        offlineDidCompleteOnce = true
        LiveEventBus.get<MutableList<ChatMessageModel<Any>>>(EventKey.key_offline_msg_finished).post(offlineMessageList)
    }

    private fun initEvent() {
        LiveEventBus.get<String>(EventKey.key_session_uid).observe(this) {
            curSessionUid = it
            clearUnReadNum(it)
        }

        if (!NetworkUtils.isAvailableByPing()) {
            showNoNet()
        }
        LiveEventBus.get<Int>(EventKey.key_network_socket_change).observe(this) {
            when(it) {
                TypeConst.state_no_network -> {
                    showNoNet()
                }
                TypeConst.state_socket_connecting -> {
                    mDataBinding?.llState?.visibility = View.VISIBLE
                    mDataBinding?.imgNotice?.visibility = View.GONE
                    mDataBinding?.pbProgress?.visibility = View.VISIBLE
                    mDataBinding?.llState?.setBackgroundColor(resources.getColor(com.com.legend.ui.R.color.ui_line_bg))
                    mDataBinding?.tvTips?.text = getString(com.legend.imkit.R.string.rc_conversation_list_notice_connecting)
                }
                TypeConst.state_socket_connect_fail -> {
                    mDataBinding?.llState?.visibility = View.VISIBLE
                    mDataBinding?.imgNotice?.visibility = View.VISIBLE
                    mDataBinding?.pbProgress?.visibility = View.GONE
                    mDataBinding?.llState?.setBackgroundColor(resources.getColor(com.legend.imkit.R.color.rc_notice_error_bg))
                    ImgLoader.display(context, com.legend.commonres.R.drawable.rc_ic_error_notice, mDataBinding?.imgNotice)
                    mDataBinding?.tvTips?.text = getString(com.legend.imkit.R.string.rc_conversation_list_notice_disconnect)
                }
                TypeConst.state_socket_pulling -> {
                    mDataBinding?.llState?.visibility = View.VISIBLE
                    mDataBinding?.imgNotice?.visibility = View.GONE
                    mDataBinding?.pbProgress?.visibility = View.VISIBLE
                    mDataBinding?.llState?.setBackgroundColor(resources.getColor(com.com.legend.ui.R.color.ui_line_bg))
                    mDataBinding?.tvTips?.text = getString(com.legend.imkit.R.string.rc_conversation_list_notice_pulling)
                }
                else -> {
                    mDataBinding?.llState?.visibility = View.GONE
                }
            }
        }

        LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_from_chat).observe(this) {
            // 找到当前会话，更新时间和messge
            for ((index, item) in chatListData.withIndex()) {
                if (item.session_id == it.session_id) {
                    item.message = it.message
                    item.timestamp = it.timestamp
                    item.from_uid = it.from_uid.toInt()
                    refreshChangedItem(index)
                    updateSingleDb(item)
                    break
                }
            }
        }

        LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_send_fail_msg).observe(this) {
            doUpdateChatListSingle(it)
        }

        LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_sending_msg).observe(this) {
            doUpdateChatListSingle(it)
        }

        LiveEventBus.get<String>(EventKey.key_delete_session_update).observe(this) {
            // 找到position， 去删除
            val item = findChatListBeanBySessionId(it)
            if (item != null) {
                refreshRemoveItem(item)
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        DbManager.getSoChatDB().chatListDao().delete(item)
                    }
                }
            }
        }

        LiveEventBus.get<UserBean.GroupInfo>(EventKey.key_modify_group_info).observe(this) {
            val position = findChatBeanBySessionId("g" + it.id)
            if (position >= 0) {
                val item = chatListData[position]
                item.nick_name = it.name!!
                item.avatar = it.avatar!!
                refreshChangedItem(position)
                updateChatListItemDb("g" + it.id, it.name!!, it.avatar!!)
            }
        }

        LiveEventBus.get<UserBean.UserInfo>(EventKey.key_modify_other_user_info).observe(this) {
            val position = findChatBeanBySessionId("s" + it.id)
            if (position >= 0) {
                val item = chatListData[position]
                item.nick_name = it.getNickName()
                refreshChangedItem(position)
                updateChatListItemDb("s"+it.id, it.getNickName())
            }
        }

        LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_add_session_list_item).observe(this) {
            CoroutineScope(Dispatchers.IO).launch {
                val strUid = if (it.talk_type == TypeConst.talk_type_group_chat) {
                    "g" + it.to_uid
                } else {
                    if (it.to_uid == ApplicationConst.getUserId()) { "s" + it.from_uid } else { "s" + it.to_uid }
                }
                val avatarInfo = UserSimpleDataHelper.getUserInfo(strUid)
                val chatListEntity = DBEntity.ChatListEntity(strUid, it.from_uid.toInt(), it.to_uid.toInt(), avatarInfo?.nick_name?: "" , avatarInfo?.avatar ?: ""
                    , it.is_read, 0, 0, ChatDataConvertUtil.getChatListShowMsg(it.message, it.message_type, it.is_secret), it.id, it.timestamp, "", it.is_secret
                    , isWarnMe(it.warn_users),avatarInfo?.is_disturb?:0, it.message_type)

                DbManager.getSoChatDB().chatListDao().insert(chatListEntity)
                chatListData.add(0, chatListEntity)
                refreshInsertItem(0)
            }
        }
    }

    private fun updateChatListItemDb(sessionId: String, nickName: String, avatar: String? = "") {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dbItem = DbManager.getSoChatDB().chatListDao().getItemBySessionId(sessionId)
                if (dbItem != null) {
                    dbItem.nick_name = nickName
                    if (!TextUtils.isEmpty(avatar)) dbItem.avatar = avatar!!
                    DbManager.getSoChatDB().chatListDao().update(dbItem)
                }
            }
        }
    }

    private fun showNoNet() {
        mDataBinding?.llState?.visibility = View.VISIBLE
        mDataBinding?.imgNotice?.visibility = View.VISIBLE
        mDataBinding?.pbProgress?.visibility = View.GONE
        mDataBinding?.llState?.setBackgroundColor(resources.getColor(com.legend.imkit.R.color.rc_notice_error_bg))
        ImgLoader.display(context, com.legend.commonres.R.drawable.rc_ic_error_notice, mDataBinding?.imgNotice)
        mDataBinding?.tvTips?.text = getString(com.legend.imkit.R.string.rc_conversation_list_notice_network_unavailable)
    }

    private fun findChatListBeanBySessionId(sessionId: String): DBEntity.ChatListEntity? {
        synchronized(chatListData) {
            for (item in chatListData) {
                if (item.session_id == sessionId) {
                    return item
                }
            }

            return null
        }
    }

    private fun findChatBeanBySessionId(sessionId: String): Int {
        synchronized(chatListData) {
            for ((index, item) in chatListData.withIndex()) {
                if (item.session_id == sessionId) {
                    return index
                }
            }

            return -1
        }
    }

    private fun findChatBeanByMsgId(msgId: String): Int {
        synchronized(chatListData) {
            for ((index, item) in chatListData.withIndex()) {
                if (item.message_id == msgId) {
                    return index
                }
            }

            return -1
        }
    }

    private fun refreshBarUnRead(isAdd: Boolean, num: Int) {
        if (isAdd) {
            unReadNumTotal += num
        } else {
            unReadNumTotal -= num
        }
        if (unReadNumTotal < 0) unReadNumTotal = 0

        LiveEventBus.get<Int>(EventKey.key_msg_unread_num).post(unReadNumTotal)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        msgRevListener?.let {
            MSocket.instance.unRegisterMsgRevListener(it)
        }
    }
}