package com.legend.imkit.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.FileUtils
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.NetworkUtils
import com.legend.base.utils.StringUtils
import com.legend.base.utils.permission.Permission
import com.legend.base.utils.permission.PermissionUtils
import com.legend.base.utils.permission.listener.PermissionListener
import com.legend.baseui.ui.base.TitleBarConfig
import com.legend.baseui.ui.util.ActivityManager
import com.legend.baseui.ui.util.CommonDialogUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.loadding.LoadingDialog
import com.legend.baseui.ui.widget.titlebar.MTitleBar
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.bean.*
import com.legend.common.db.DbManager
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.DBEntity
import com.legend.common.db.entity.SimpleMessage
import com.legend.common.network.viewmodel.MainRequest
import com.legend.common.network.viewmodel.MsgViewModel
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.common.socket.MSocket
import com.legend.common.socket.MessageRevListener
import com.legend.common.upload.UploadCallback
import com.legend.common.upload.UploadStrategy
import com.legend.common.upload.UploadUtil
import com.legend.common.utils.*
import com.legend.common.utils.picture.OnVideoCompressListener1
import com.legend.common.utils.picture.PictureSelectorUtil
import com.legend.common.widget.DialogUitl
import com.legend.common.widget.switchbutton.SwitchButton
import com.legend.imkit.R
import com.legend.imkit.bean.*
import com.legend.imkit.bean.ChatBean.AddFuncBean
import com.legend.imkit.manager.AudioPlayManager
import com.legend.imkit.mention.RongMentionManager
import com.legend.imkit.util.*
import com.legend.imkit.util.ChatUiHelper.OnExtraFuncListener
import com.legend.imkit.videocall.CallFloatBoxView
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.legend.imkit.widget.RecordButton
import com.legend.imkit.widget.StateButton
import com.legend.main.network.viewmodel.GroupViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.ceil

@Route(path = RouterPath.path_chat_activity)
class Chat1Activity: AppCompatActivity() {
    val chatActivitySub: ChatActivitySub = ChatActivitySub(this)
    private var loadingDialog: LoadingDialog? = null
    private var titleBar: MTitleBar? = null
    private var llContent: LinearLayout? = null
    private var rvChatList: RecyclerView? = null
    private var swipeChat: SmartRefreshLayout? = null
    private var btnSend: StateButton? = null
    private var etContent: EditText? = null
    private var bottomLayout: RelativeLayout? = null
    private var rlEmotion: LinearLayout? = null
    private var llAdd: LinearLayout? = null
    private var ivAdd: ImageView? = null
    private var ivEmo: ImageView? = null
    private var btnAudio: RecordButton? = null
    private var imgAudio: ImageView? = null
    private var tvUnReadNum: TextView? = null
    private var bottomSecretLine: View? = null
    private var bottomSecretLayout: LinearLayout? = null
    private var switchSecret: SwitchButton? = null
    var lltBottomInput: LinearLayout? = null
    var tvBottomAction: TextView? = null
    private var onlineStateLayout: View? = null
    private var imgOnlineState: ImageView? = null
    private var tvOnlineState: TextView? = null

    private val REQUEST_CODE_VIDEO = 100
    private val REQUEST_CODE_FILE = 101
    private val REQUEST_CODE_IMAGE  = 103
    private val REQUEST_CODE_IMAG_VIDEO = 104
    private val REQUEST_CODE_CAMERE_VIDEO = 105
    private val REQUEST_CODE_SECRET_MODE = 200
    private val REQUEST_CODE_DECRYPT_SECRET = 201
    private val REQUEST_CODE_DECRYPT_SECRET_ERROR = 202
    val REQUEST_FORWARD_MSG = 300

    private var msgRevListener: MessageRevListener? = null
    private var chatAdapter:  Chat1Adapter? = null
    private val chatDataList: MutableList<UiMessage> = mutableListOf()
    private val sendingFileMsgList = mutableListOf<UiMessage>()

    private var sessionId: String = ""
    private var showTitle: String = ""
    private var toUid = ""
    private var isSingleTalk = true
    private var talkType = TypeConst.talk_type_single_chat
    var isGroupOwner = false
    private val groupMember = arrayListOf<UserBean.GroupMember>()
    private var singleToUser: UserBean.UserInfo? = null
    private var groupResErrorCode: Int = 200

    private var chatUiHelper: ChatUiHelper? = null
    private var isSecretModeOpen = false
    private var secretPwd = ""
    private var switchSecretListener: CompoundButton.OnCheckedChangeListener? = null
    private var dataFromNet = false
    private var lastMsgId: String = "0"  // 从服务器开始拉的消息
    private var canLoadMore = true
    private var curPageIndex = 0
    private val perPageNum = 20
    private var pageOffset = 0
    var uploadTask = HashMap<String, UploadStrategy>()
    private var unReadMsgStartPage = 0  // 未读消息从哪一页开始
    private var unReadMsgSize = 0  // 未读消息数
    private var isUnReadMsgLayoutShow = false
    private var firstVisibleItem = -1

    private val activity = WeakReference<Chat1Activity>(this)
    val msgViewModel: MsgViewModel by lazy {
        ViewModelProvider(this)[MsgViewModel::class.java]
    }
    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    private val mTextWatcher: TextWatcher = object : TextWatcher {
        private var start = 0
        private var count = 0
        private var isProcess = false
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // do nothing
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (isProcess) {
                return
            }
            this.start = start
            this.count = count

            var cursor = 0
            var offSet = 0
            if (count == 0) {
                cursor = start + before
                offSet = -before
            } else {
                cursor = start
                offSet = count
            }
            if (talkType == TypeConst.talk_type_group_chat) RongMentionManager.getInstance().onTextChanged(this@Chat1Activity, cursor, offSet, s.toString(), etContent, toUid, groupMember,isGroupOwner)
        }

        override fun afterTextChanged(s: Editable) {
            if (isProcess) {
                return
            }
            etContent?.let {
                val selectionStart: Int = it.selectionStart
                if (AndroidEmoji.isEmoji(s.subSequence(start, start + count).toString())) {
                    isProcess = true
                    val resultStr: String = AndroidEmoji.replaceEmojiWithText(s.toString())
                    val spanable = AndroidEmoji.ensure(resultStr)

                    it.setText(
                        spanable, TextView.BufferType.SPANNABLE
                    )
                    it.setSelection(
                        Math.min(
                            it.getText().length, Math.max(0, selectionStart)
                        )
                    )
                    isProcess = false
                }
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityManager.getInstance().addActivity(this)
        hideActionStatusBar()
        setContentView(R.layout.activity_chat1)
        sessionId = intent.getStringExtra(KeyConst.key_session_uid)?:""
        showTitle = intent.getStringExtra(KeyConst.key_title)?:""
        initView()
        initData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        sessionId = intent?.getStringExtra(KeyConst.key_session_uid)?:""
        showTitle = intent?.getStringExtra(KeyConst.key_title)?:""
        initData()
    }

    private fun findView() {
        titleBar = findViewById(R.id.title_bar)
        llContent = findViewById(R.id.ll_content)
        rvChatList = findViewById(R.id.rv_chat_list)
        swipeChat = findViewById(R.id.swipe_chat)
        btnSend = findViewById(R.id.btn_send)
        etContent = findViewById(R.id.et_content)
        bottomLayout = findViewById(R.id.bottom_layout)
        rlEmotion = findViewById(R.id.rlEmotion)
        llAdd = findViewById(R.id.llAdd)
        ivAdd = findViewById(R.id.ivAdd)
        ivEmo = findViewById(R.id.ivEmo)
        btnAudio = findViewById(R.id.btnAudio)
        imgAudio = findViewById(R.id.img_audio)
        tvUnReadNum = findViewById(R.id.unread_message_count)
        bottomSecretLine = findViewById(R.id.secret_line)
        bottomSecretLayout = findViewById(R.id.llt_secret)
        switchSecret = findViewById(R.id.switch_secret)
        lltBottomInput = findViewById(R.id.llt_bottom_input)
        tvBottomAction = findViewById(R.id.tv_bottom_action)
        onlineStateLayout = findViewById(R.id.online_state_layout)
        imgOnlineState = findViewById(R.id.img_online_state)
        tvOnlineState = findViewById(R.id.tv_online_state)
    }
    private fun initView() {
        findView()
        initTitleBar()
        initEvent()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        chatUiHelper = ChatUiHelper.with(this)
        rvChatList?.layoutManager = layoutManager
        chatAdapter = Chat1Adapter()
        rvChatList?.adapter = chatAdapter
        chatAdapter?.data = chatDataList
        rvChatList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!isUnReadMsgLayoutShow) return
                if (layoutManager.findLastCompletelyVisibleItemPosition() >= unReadMsgSize + pageOffset - 1) {
                    tvUnReadNum?.visibility = View.GONE
                    isUnReadMsgLayoutShow = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
//                Log.i("byy", "最后一条可见item的position = $firstVisibleItem")
            }
        })
        etContent?.addTextChangedListener(mTextWatcher)
        etContent?.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                val cursorPos = etContent!!.selectionStart;
                RongMentionManager.getInstance().onDeleteClick(etContent, cursorPos)
            }
            false
        }
        chatActivitySub.setConfig(chatAdapter, msgViewModel, rvChatList)
        swipeChat?.setOnRefreshListener {
            if (canLoadMore) {
                getMsgDataFromDb()
            } else {
                ToastUtils.show(getString(R.string.no_more_data))
                swipeChat?.finishRefresh()
            }

        }
        tvUnReadNum?.setOnClickListener {
            unReadNumClicked()

        }
        btnSend?.setOnClickListener {
            sendTexMessage(etContent?.text?.toString())
            etContent?.setText("")
        }
        switchSecretListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isSecretModeOpen) {
                    isSecretModeOpen = false
                    secretPwd = ""
                } else {
                    setSecretCheckedWithOutEvent(!isChecked)
                    Router.toInputTransActivity(this, REQUEST_CODE_SECRET_MODE, TypeConst.trans_input_page_type_secret)
                }
            }
        switchSecret?.setOnCheckedChangeListener(switchSecretListener)
        tvBottomAction?.setOnClickListener {
            if (chatActivitySub.selectedUiMsgList.isEmpty()) {
                ToastUtils.show(R.string.select_forward_msg)
                return@setOnClickListener
            }
            var ids = ""
            for (msg in chatActivitySub.selectedUiMsgList) {
                ids += msg.message.id + ","
            }
            if (ids.endsWith(",")) ids = ids.substring(0, ids.length - 1)
            DialogUitl.showStringArrayDialog(this, arrayOf(R.string.forward_one_by_one, R.string.forward_merge)) { text, tag ->
                if (text == getString(R.string.forward_one_by_one)) {
                    Router.toPickChatActivity(this, REQUEST_FORWARD_MSG, ids, TypeConst.forward_type_one_by_one)
                } else {
                    Router.toPickChatActivity(this, REQUEST_FORWARD_MSG, ids, TypeConst.forward_type_merge)
                }
            }
        }
        initUiChat()
        initListener()
    }

    private fun initTitleBar() {
        titleBar?.let {
            it.setRightIcon(R.mipmap.title_bar_more1)
            it.setLeftIcon(com.com.legend.ui.R.drawable.ui_double_back)
            val titleBarConfig = TitleBarConfig()
            titleBarConfig.leftIconSize = 38
            titleBarConfig.rightIconSize = 20
            it.setConfig(titleBarConfig)
            it.setListener(object : MTitleBar.OnTitleListener {
                override fun titleBarLeftClick() {
                    finish()
                }

                override fun titleBarRightClick() {
                    if (talkType == TypeConst.talk_type_group_chat) {
                        if (groupResErrorCode == 422) {
                            ToastUtils.show(getString(com.legend.commonres.R.string.not_join_group))
                        } else {
                            Router.toGroupChatInfoActivity(toUid)
                        }
                    } else {
                        Router.toUserActivity(toUid, if (talkType == TypeConst.talk_type_group_chat) toUid else "")
                    }
                }

                override fun titleBarTitleClick() {
                }

            })
        }
    }

    private fun initData() {
        isSecretModeOpen = false
        secretPwd = ""
        canLoadMore = true
        curPageIndex = 0
        pageOffset = 0
        dataFromNet = false
        lastMsgId = "0"
        chatDataList.clear()
        groupMember.clear()
        sendingFileMsgList.clear()
        setSecretCheckedWithOutEvent(false)
        hideBottomLayout()
        RongMentionManager.getInstance().createInstance(etContent)

        btnAudio?.setSessionId(sessionId)
        if (sessionId.startsWith("s") || sessionId.startsWith("g")) {
            toUid = sessionId.substring(1)
            isSingleTalk = sessionId.startsWith("s")
            talkType = if (isSingleTalk) TypeConst.talk_type_single_chat else TypeConst.talk_type_group_chat
        }
        if (talkType == TypeConst.talk_type_group_chat) {
            groupViewModel.getGroupInfo(toUid)
            onlineStateLayout?.visibility = View.GONE
        } else {
            userViewModel.getUserInfo(id = toUid)
            userViewModel.getUserOnlineState(toUid)
            onlineStateLayout?.visibility = View.VISIBLE
        }
        chatActivitySub.setGroupId(if (talkType == TypeConst.talk_type_group_chat) toUid else "")
        LiveEventBus.get<String>(EventKey.key_session_uid).post(sessionId)
        if (!TextUtils.isEmpty(showTitle)) titleBar?.setTitle(showTitle)
        getUnReadDataFromDb()
        getMsgDataFromDb()
    }

    private fun initListener() {
        msgRevListener = object : MessageRevListener {
            override fun onMessageTextRev(message: ChatMessageModel<BaseMsgContent>) {
                if (message.isSender && message.isFromAndroid) return
                if ((message.talk_type == TypeConst.talk_type_single_chat && (toUid == message.from_uid || (!message.isFromAndroid && message.from_uid == ApplicationConst.getUserId() && message.to_uid == toUid)))
                    || (message.talk_type == TypeConst.talk_type_group_chat && toUid == message.to_uid)) {
                    val textMsg = ChatDataConvertUtil.chatTextMsgConvertTextMsg(message)
                    val uiMessage = UiMessage(textMsg)
                    if (chatActivitySub.isEdit) uiMessage.isEdit = true
                    chatActivitySub.refreshAddItem(uiMessage, firstVisibleItem == 0)
                    MainRequest.reportReadMsg(message.id, talkType)
                }

            }

            override fun onMessageFileRev(message: ChatMessageModel<FileMsgContent>) {
                if (message.isSender && message.isFromAndroid) return
                if ((message.talk_type == TypeConst.talk_type_single_chat && (toUid == message.from_uid || (!message.isFromAndroid && message.from_uid == ApplicationConst.getUserId() && message.to_uid == toUid)))
                    || (message.talk_type == TypeConst.talk_type_group_chat && toUid == message.to_uid)) {
                    val fileMsg = ChatDataConvertUtil.chatFileMsgConvertFileMsg(message)
                    fileMsg.message_local_type = message.message_local_type
                    val uiMessage = UiMessage(fileMsg)
                    if (chatActivitySub.isEdit) uiMessage.isEdit = true
                    chatActivitySub.refreshAddItem(uiMessage, firstVisibleItem == 0)
                    MainRequest.reportReadMsg(message.id, talkType)
                }
            }

            override fun onMessageForwardRev(message: ChatMessageModel<ForwardMsgContent>) {
                if (message.isSender && message.isFromAndroid) return
                if ((message.talk_type == TypeConst.talk_type_single_chat && (toUid == message.from_uid || (!message.isFromAndroid && message.from_uid == ApplicationConst.getUserId() && message.to_uid == toUid)))
                    || (message.talk_type == TypeConst.talk_type_group_chat && toUid == message.to_uid)) {
                    val forwardMsg = ChatDataConvertUtil.chatForwardMsgConvertForwardMsg(message)
                    val uiMessage = UiMessage(forwardMsg)
                    if (chatActivitySub.isEdit) uiMessage.isEdit = true
                    chatActivitySub.refreshAddItem(uiMessage, firstVisibleItem == 0)
                    MainRequest.reportReadMsg(message.id, talkType)
                }
            }

            override fun onMessageGroupRev(message: ChatMessageModel<GroupMsgContent>) {
                if (toUid != message.to_uid || message.talk_type != talkType) return
                if (message.extra.type == 1
                    || message.extra.type == 3
                    || (message.extra.type == 2 && message.extra.operate_user_id.toString() != ApplicationConst.getUserId())
                    || (message.extra.type == 4 && message.extra.operate_user_id.toString() != ApplicationConst.getUserId())) {
                    val groupOpMsg = ChatDataConvertUtil.chatGrouOpMsgConvertGroupOpMsg(message)
                    chatActivitySub.refreshAddItem(UiMessage(groupOpMsg), firstVisibleItem == 0)
                }
            }

            override fun onMessageCallStateRev(message: ChatMessageModel<CallStateContent>) {
                if ((message.talk_type == TypeConst.talk_type_single_chat && (toUid == message.from_uid || (!message.isFromAndroid && message.from_uid == ApplicationConst.getUserId() && message.to_uid == toUid)))
                    || (message.talk_type == TypeConst.talk_type_group_chat && toUid == message.to_uid)) {
                    val callStateMsg = ChatDataConvertUtil.chatCallStateMsgConvertCallStateMsg(message)
                    chatActivitySub.refreshAddItem(UiMessage(callStateMsg), firstVisibleItem == 0)
                }
            }

            override fun onMessageNewFriendRev(message: ChatMessageModel<NewFriendContent>) {
                if (MSocket.instance.getSessionId(message as ChatMessageModel<Any>) == sessionId) {
                    val newFriendMsg = ChatDataConvertUtil.chatNewFriendMsgConvertNewFriendMsg(message)
                    chatActivitySub.refreshAddItem(UiMessage(newFriendMsg), firstVisibleItem == 0)
                }
            }

            override fun onRevokeRev(revokeMsg: SimpleMessage) {
                if (revokeMsg.from_uid == ApplicationConst.getUserId() && revokeMsg.uuid.startsWith(TypeConst.dev_android)) return

                doOnRevokeRevMsg(revokeMsg.id)
            }

            override fun onMsgRead(message: UserBean.MsgReadRes) {
                if (message.message_ids.isNullOrEmpty()) return
                if (message.message_ids!!.size == 1) {
                    val position = findUiMsgPositionByMsgId(message.message_ids!![0])
                    position?.let {
                        val uiMsg = chatDataList[it]
                        uiMsg.message.is_read = 1
                        chatActivitySub.refreshItemChanged(position)
                    }
                } else {
                    val startMsgId = message.message_ids!![0]
                    val endMsgId = message.message_ids!![message.message_ids!!.size - 1]
                    var isChanged = false
                    for (uiMsg in chatDataList) {
                        if (uiMsg.message.id >= startMsgId || uiMsg.message.id <= endMsgId) {
                            isChanged = true
                            uiMsg.message.is_read = 1
                        }
                    }
                    if (isChanged) chatActivitySub.refreshMsgList(false)
                }
            }

            override fun onlineStateChangeRev(message: UserBean.OnLineChangeMsg) {
                if (talkType == TypeConst.talk_type_single_chat && message.from_uid == toUid) {
                    setOnlineState(message.extra.type == TypeConst.type_yes)
                }

            }

        }
        MSocket.instance.registerMsgRevListener(msgRevListener!!)
        msgViewModel.messageRes.observe(this) {
            val sendPosition = chatActivitySub.findPositionByUUid(it.uuid)
            Log.i("websocket", "找到相同uuid，position = $sendPosition")

            if (it.isSuccess) {
                // 收到的消息处理
                val sourceModel = it.message!!
                val sessionId = ChatDataConvertUtil.getSessionId(sourceModel.from_uid, sourceModel.to_uid, sourceModel.talk_type)
                sourceModel.session_id = sessionId
                sourceModel.uuid = it.uuid
                sourceModel.sendStatus = TypeConst.msg_send_status_sent
                // 插入数据库
                MSocket.instance.insertRevMsgDb(sourceModel, it.uuid)

                val msgType = sourceModel.message_type
                val model = if (msgType == TypeConst.chat_msg_type_file) ChatDataConvertUtil.chatMsgModelConvertFileMsg(sourceModel) else ChatDataConvertUtil.chatMsgModelConvertTextMsg(sourceModel)
                if (msgType == TypeConst.chat_msg_type_file) {
                    model as ChatMessageModel<FileMsgContent>
                    when(model.extra.type) {
                        TypeConst.chat_msg_type_file_sub_pic -> model.message_local_type = TypeConst.chat_msg_type_file_pic
                        TypeConst.chat_msg_type_file_sub_file -> model.message_local_type = TypeConst.chat_msg_type_file_file
                        TypeConst.chat_msg_type_file_sub_video -> model.message_local_type = TypeConst.chat_msg_type_file_video
                        TypeConst.chat_msg_type_file_sub_audio -> model.message_local_type = TypeConst.chat_msg_type_file_voice
                    }
                    if (sendPosition >= 0) {
                        val foundMessage = chatDataList[sendPosition].message as FileMsg
                        model.extra.thumbnailPath = foundMessage.extra.thumbnailPath
                    }
                }

                if (sendPosition >= 0) {
                    model.uuid = ""
                    chatDataList.removeAt(sendPosition)
                    chatDataList.add(sendPosition, UiMessage(model))
                    chatActivitySub.refreshItemChanged(sendPosition)
                } else {
                    chatActivitySub.refreshAddItem(UiMessage(model), firstVisibleItem == 0)
                }

            } else {
                ToastUtils.show(it.errorMsg)
                Log.i("byy", "sendOrUploadMsgFail1。。")
                sendOrUploadMsgFail(it.msgType, it.uuid)
            }

        }
        groupViewModel.groupInfoRes.observe(this) {
            if (it.isSuccess) {
                val groupInfo = it.groupInfo!!
                if (isSingleTalk) return@observe
                groupMember.clear()
                if (!groupInfo.group_member.isNullOrEmpty()) groupMember.addAll(groupInfo.group_member!!)
                isGroupOwner = groupInfo.group_info?.uid == ApplicationConst.getUserId()
                if (groupInfo.group_info?.audio == 1) {
                    val itemCall = AddFuncBean(R.mipmap.ic_ctype_video_call, StringUtils.getString(R.string.extra_video_call), TypeConst.add_func_type_call)
                    chatUiHelper?.addAddFuncData(itemCall)
                } else {
                    chatUiHelper?.removeAddFuncData(TypeConst.add_func_type_call)
                }
                titleBar?.setTitle(groupInfo.group_info?.name + "(${groupInfo.group_member?.size})")
            } else {
                groupResErrorCode = it.code
                ToastUtils.show(it.errorMsg?:"")
            }

        }
        userViewModel.userInfoRes.observe(this) {
            singleToUser = it
            titleBar?.setTitle(it.getNickName())
        }
        msgViewModel.checkCanCallRes.observe(this) {
            if (isSingleTalk) {
                singleToUser?.let { user->
                    val opponent = UserBean.QbUserInfo(user.id, user.avatar, user.nick_name, user.quickblox_id?:"0")
                    ApplicationConst.opponentList.clear()
                    ApplicationConst.opponentList.add(opponent)
                    QbUtil.startCall(it.id, false, isVideoCall, this@Chat1Activity, ApplicationConst.opponentList)
                }
            } else {
                QbUtil.startCall(it.id,true, isVideoCall, this@Chat1Activity, ApplicationConst.opponentList)
            }
        }
        msgViewModel.netChatMsgRes.observe(this) {
            if (it.isEmpty()) {
                canLoadMore = false
                swipeChat?.finishRefresh()
            } else {
                if (it.size >= perPageNum) {
                    canLoadMore = true
                    swipeChat?.finishRefresh()
                } else {
                    canLoadMore = false
                    swipeChat?.finishRefresh()
                }

                // 存数据库、更新列表、更新lastMsgId
                CoroutineScope(Dispatchers.IO).launch {
                    if (lastMsgId == "0") {
                        val lastItem = it[0]
                        val chatListItem = DbManager.getSoChatDB().chatListDao().getItemBySessionId(sessionId)
                        if (chatListItem == null) {
                            LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_add_session_list_item).post(lastItem)
                        }
                        if (lastItem.is_read == TypeConst.type_no)  MainRequest.reportReadMsg(lastItem.id, lastItem.talk_type)
                    }
                    val chatMessageEntityList = mutableListOf<DBEntity.ChatMessageEntity>()
                    for (item in it) {
                        item.session_id = MSocket.instance.getSessionId(item)
                        item.sendStatus = TypeConst.msg_send_status_sent
                        val chatEntity = ChatDataConvertUtil.chatMsgModelConvertDb(item)
                        chatMessageEntityList.add(chatEntity)
                    }
//                    Log.i("网络请求", "-------网络数据插入数据库 = ${GlobalGsonUtils.toJson(chatMessageEntityList)}")
                    DbManager.getSoChatDB().msgDao().insertAll(chatMessageEntityList)
//                    val dbList = DbManager.getSoChatDB().msgDao().getAllMsgFromSessionId(sessionId)
//                    Log.i("网络请求", "-------网络数据插入数据库11 = ${if (dbList.isNullOrEmpty()) 0 else 1}")

                    val tempUiMsgList = ChatDataConvertUtil.chatMsgModelAnyToUiMsg(it, sessionId, TypeConst.msg_send_status_sent)
                    if (chatActivitySub.isEdit) {
                        for (data in tempUiMsgList) {
                            data.isEdit = true
                        }
                    }

                    chatDataList.addAll(tempUiMsgList)
                    chatActivitySub.refreshMsgList(lastMsgId == "0")
                    lastMsgId = tempUiMsgList[tempUiMsgList.size - 1].message.id
                    Log.i("网络请求", "-------lastMsgId4 = $lastMsgId")
                }

            }
        }
        userViewModel.onlineStateRes.observe(this) {
            setOnlineState(it.online == TypeConst.type_yes)
        }

    }

    private fun sendOrUploadMsgFail(msgType: Int, uuid: String) {
        val sendPosition = findUiMsgPositionByUUid(uuid)
        sendPosition?.let {
            val sendFailMsg = chatAdapter?.getItem(sendPosition)
            if (msgType == TypeConst.chat_msg_type_file) {
                val fileMsg = sendFailMsg?.message as FileMsg
                val sessionId = ChatDataConvertUtil.getSessionId(fileMsg.from_uid, fileMsg.to_uid, fileMsg.talk_type)
                fileMsg.sendStatus = TypeConst.msg_send_status_failed
                fileMsg.uuid = uuid
                fileMsg.session_id = sessionId
                chatActivitySub.refreshItemChanged(sendPosition)
                val chatModel = ChatDataConvertUtil.fileMsgCovertChatModel(fileMsg)
                MSocket.instance.insertRevMsgDb(chatModel)
                LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_send_fail_msg).post(chatModel)
                if (chatDataList.size > sendPosition) sendingFileMsgList.remove(chatDataList[sendPosition])
            } else {
                val textMsg = sendFailMsg?.message as TextMsg
                val sessionId = ChatDataConvertUtil.getSessionId(textMsg.from_uid, textMsg.to_uid, textMsg.talk_type)
                textMsg.sendStatus = TypeConst.msg_send_status_failed
                textMsg.uuid = uuid
                textMsg.session_id = sessionId
                chatActivitySub.refreshItemChanged(sendPosition)
                val chatModel = ChatDataConvertUtil.textMsgConvertChatModel(textMsg)
                MSocket.instance.insertRevMsgDb(chatModel)
                LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_send_fail_msg).post(chatModel)
            }
        }
    }
    private fun isInOpponentUser(uid: String, qbUserInfoList: List<UserBean.QbUserInfo>): Boolean {
        for (opponent in qbUserInfoList) {
            if (opponent.uid == uid) return true
        }

        return false
    }

    fun doOnRevokeRevMsg(id: String) {
        lifecycleScope.launch {
            val position = findUiMsgPositionByMsgId(id)
            val msg = if (position == null) null else chatDataList[position]
            msg?.let {
                it.message.is_revoke = TypeConst.type_yes
                it.message.message = MSocket.instance.getRevokeMsgTip(it.message.talk_type, it.message.from_uid)
                chatAdapter?.notifyItemChanged(position!!)
            }
        }
    }

    private fun getMsgDataFromDb() {
        CoroutineScope(Dispatchers.IO).launch {
            if (dataFromNet) {
                getMsgDataFromNet(lastMsgId)
            } else {
//            Log.i("websocket", "getFromDb : " + System.currentTimeMillis())
                val data: List<DBEntity.ChatMessageEntity>? = DbManager.getSoChatDB().msgDao().getMsgFromSessionId(sessionId ,curPageIndex * perPageNum + pageOffset,  (curPageIndex + 1) * perPageNum + pageOffset)
//            Log.i("websocket", "getFromDb finish : " + System.currentTimeMillis())
                if (data != null && data.isNotEmpty()) {
                    Log.d("网络请求", "----chatActivity from db size = " + data.size)
                    if (data.size >= perPageNum) {
                        canLoadMore = true
                        curPageIndex++
                        swipeChat?.finishRefresh()

                    } else {
//                        canLoadMore = false
                        swipeChat?.finishRefresh()
                        dataFromNet = true
                    }

                    val uiMsgList = ChatDataConvertUtil.dbDataToUiMessage(data)
                    if (chatActivitySub.isEdit) {
                        for (data in uiMsgList) {
                            data.isEdit = true
                        }
                    }
                    Log.i("网络请求", "-------lastMsgId1 = $lastMsgId")
                    chatDataList.addAll(uiMsgList)
                    chatActivitySub.refreshMsgList(lastMsgId == "0")
                    lastMsgId = uiMsgList[uiMsgList.size - 1].message.id
                    findSendingFileAndSend(uiMsgList)
                } else {
//                    canLoadMore = false
//                    swipeChat?.finishRefresh()
                    Log.d("网络请求", "----chatActivity from db size1 = " + 0)
                    lastMsgId = if (chatDataList.isEmpty()) "0" else chatDataList[chatDataList.size - 1].message.id
                    Log.i("网络请求", "-------lastMsgId2 = $lastMsgId")
                    getMsgDataFromNet(lastMsgId)
                }
                Log.d("websocket", "----chatActivity from db = " + data.toString())
                Log.d("网络请求", "----chatActivity from db = " + data.toString())
            }

        }
    }

    private fun getMsgDataFromNet(lastMsgId: String) {
        dataFromNet = true
        msgViewModel.getNetChatMessages(toUid, talkType, lastMsgId)
    }

    private fun getUnReadDataFromDb() {
        if (TextUtils.isEmpty(sessionId)) return
        CoroutineScope(Dispatchers.IO).launch {
            // 获取未读消息
            val unreadMsg = if (talkType == TypeConst.talk_type_single_chat) DbManager.getSoChatDB().msgDao().getReceivedMsgUnread(sessionId, ApplicationConst.getUserId()) else DbManager.getSoChatDB().msgDao().getReceivedGroupMsgUnread(sessionId, ApplicationConst.getUserId())
            if (!unreadMsg.isNullOrEmpty()) {
//                Log.i("byy", "unreadMsg id = " + unreadMsg[0].id + " , " + unreadMsg[1].id)
                MainRequest.reportReadMsg(unreadMsg[0].id, talkType)
                unReadMsgSize = unreadMsg.size
                Log.i("byy", "unReadMsgSize = $unReadMsgSize, page: " + ceil((unReadMsgSize * 1.0) / perPageNum))
                unReadMsgStartPage = ceil((unReadMsgSize * 1.0) / perPageNum).toInt() - 1
//                if (unReadMsgSize > perPageNum) {
//                    tvUnReadNum?.visibility = View.VISIBLE
//                    isUnReadMsgLayoutShow = true
//                    tvUnReadNum?.text = String.format(getString(R.string.new_message_num), unReadMsgSize)
//                } else {
//                    tvUnReadNum?.visibility = View.GONE
//                    isUnReadMsgLayoutShow = false
//                }
            } else {
                Log.i("byy", "unreadMsg is null")
            }

            // 更新消息为已读
            if (talkType == TypeConst.talk_type_single_chat) {
                DbManager.getSoChatDB().msgDao().updateReceivedMsgRead(sessionId, ApplicationConst.getUserId())
            } else {
                DbManager.getSoChatDB().msgDao().updateReceivedGroupMsgRead(sessionId, ApplicationConst.getUserId())
            }
        }
    }

    private fun unReadNumClicked() {
        tvUnReadNum?.visibility = View.GONE
        isUnReadMsgLayoutShow = false
        Log.i("byy", "unReadNumClicked -> curPageIndex = $curPageIndex, unReadMsgStartPage = $unReadMsgStartPage")
        if (curPageIndex > unReadMsgStartPage) return
        CoroutineScope(Dispatchers.IO).launch {
            val unReadDataList = arrayListOf<DBEntity.ChatMessageEntity>()
            while (curPageIndex <= unReadMsgStartPage) {
                val data: List<DBEntity.ChatMessageEntity>? = DbManager.getSoChatDB().msgDao().getMsgFromSessionId(sessionId ,curPageIndex * perPageNum + pageOffset,  (curPageIndex + 1) * perPageNum + pageOffset)
                if (!data.isNullOrEmpty()) {
                    unReadDataList.addAll(data)
                    curPageIndex++
                } else {
                    break
                }
             }
            Log.i("byy", "unReadNumClicked -> unReadDataList.size = ${unReadDataList.size}")
            val uiMsgList = ChatDataConvertUtil.dbDataToUiMessage(unReadDataList)
            chatDataList.addAll(uiMsgList)
            chatActivitySub.refreshMsgList(unReadMsgSize + pageOffset - 1)
        }
    }

    private fun sendTexMessage(message: String?) {
        if (!NetworkUtils.isAvailableByPing()) {
            ToastUtils.show(getString(com.legend.commonres.R.string.rc_notice_network_unavailable))
            return
        }
        if (TextUtils.isEmpty(message)) return
        val messageStr = StringUtils.trimEndNewLine(message)
        CoroutineScope(Dispatchers.IO).launch {
            val uuid = createUUid()
            val warnUsers = RongMentionManager.getInstance().getMentionIds(etContent, true)
            val textMsg = SendMessage(toUid, messageStr, warnUsers, talkType, TypeConst.chat_msg_type_text, uuid, isSecretModeOpen, secretPwd)
            val uiMessage = ChatDataConvertUtil.sendMsgToUiMessage(uuid, System.currentTimeMillis(), textMsg, isSecretModeOpen, secretPwd)
            uiMessage.isChange = false
            chatActivitySub.refreshAddItem(uiMessage, firstVisibleItem == 0)
            msgViewModel.sendMsg(GlobalGsonUtils.toJson(textMsg), uuid, TypeConst.chat_msg_type_text)
        }
    }

    private fun sendFileMessage(media: LocalMedia? = null, file: File? = null, duration: Long? = null, fileSubType: Int) {
        if (media != null) Log.d("websocket","发送之前-- 获取图片路径成功:" + media.path + "\n 压缩path = " + media.compressPath + "\n 原图路径 = " + media.realPath)
        if (file != null) Log.d("websocket","发送之前-- 获取图片路径成功1:" + file.path)

        if (!NetworkUtils.isAvailableByPing()) {
            ToastUtils.show(getString(com.legend.commonres.R.string.rc_notice_network_unavailable))
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val uuid = createUUid()
            var fileMsgContent: FileMsgContent
            val sendMessage: SendMessage
            var sendPath: String
            when(fileSubType) {
                TypeConst.chat_msg_type_file_sub_pic -> {
                    if (media == null) return@launch
                    if (TextUtils.isEmpty(media.compressPath) && TextUtils.isEmpty(media.realPath)) return@launch
                    sendPath = if (!TextUtils.isEmpty(media.compressPath)) media.compressPath else media.realPath
                    // 兼容有uri但file不存在的情况
                    if (!FileUtils.hasFile(sendPath) && !TextUtils.isEmpty(media.path)) sendPath = FileUtils.getFileAbsolutePath(this@Chat1Activity, Uri.parse(media.path))

                    fileMsgContent = FileMsgContent(suffix = FileUtils.getFileSuffix(sendPath), original_name = FileUtils.getFileName(sendPath), type = TypeConst.chat_msg_type_file_sub_pic
                        , size = media.size, path = sendPath, url = "", height = media.height, weight = media.width, duration = media.duration)
                    sendMessage = createSendMsg(uuid, getString(R.string.msg_sub_image), "", fileMsgContent)
                }
                TypeConst.chat_msg_type_file_sub_video -> {
                    if (media == null) return@launch
                    if (TextUtils.isEmpty(media.compressPath) && TextUtils.isEmpty(media.realPath)) return@launch
                    sendPath = if (!TextUtils.isEmpty(media.compressPath)) media.compressPath else media.realPath

                    fileMsgContent = FileMsgContent(suffix = FileUtils.getFileSuffix(sendPath), original_name = FileUtils.getFileName(sendPath), type = TypeConst.chat_msg_type_file_sub_video
                        , path = sendPath, duration = media.duration / 1000, thumbnailPath = media.videoThumbnailPath, weight = media.width, height = media.height)
                    sendMessage = createSendMsg(uuid, getString(R.string.msg_sub_video), "", fileMsgContent)
                }
                TypeConst.chat_msg_type_file_sub_audio -> {
                    if (file == null || duration == null) return@launch
                    sendPath = file.path
                    fileMsgContent = FileMsgContent(suffix = FileUtils.getFileSuffix(sendPath), original_name = FileUtils.getFileName(sendPath), type = TypeConst.chat_msg_type_file_sub_audio
                        , path = sendPath, url = "", duration = duration)
                    sendMessage = createSendMsg(uuid, getString(R.string.msg_sub_audio), "", fileMsgContent)
                }
                else -> {
                    if (file == null) return@launch
                    sendPath = file.path
                    fileMsgContent = FileMsgContent(suffix = FileUtils.getFileSuffix(sendPath), original_name = FileUtils.getFileName(sendPath), type = TypeConst.chat_msg_type_file_sub_file
                        , path = sendPath, size = file.length())
                    sendMessage = createSendMsg(uuid, getString(R.string.msg_sub_file), "", fileMsgContent)
                }
            }

            val uiMessage = ChatDataConvertUtil.sendMsgToUiMessage(uuid, System.currentTimeMillis(), sendMessage, isSecretModeOpen, secretPwd)
            uiMessage.message.sendStatus = if (fileSubType == TypeConst.chat_msg_type_file_sub_audio) TypeConst.msg_send_status_sending else TypeConst.msg_send_status_uploading
            chatActivitySub.refreshAddItem(uiMessage, firstVisibleItem == 0)

            beforeUploadFileMsg(uiMessage, media?.realPath)
        }
    }

    private fun beforeUploadFileMsg(uiMessage: UiMessage, realPath: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            var fileMsgContent = uiMessage.message.extra as FileMsgContent
            val fileSubType = fileMsgContent.type
            val chatModel = ChatDataConvertUtil.fileMsgCovertChatModel(uiMessage.message as FileMsg)
            MSocket.instance.insertRevMsgDb(chatModel)
            LiveEventBus.get<ChatMessageModel<Any>>(EventKey.key_update_chat_list_sending_msg).post(chatModel)

            if (fileSubType == TypeConst.chat_msg_type_file_sub_video && !TextUtils.isEmpty(realPath)) {
                PictureSelectorUtil.compressVideo(this@Chat1Activity, sessionId, realPath!!, object : OnVideoCompressListener1 {
                    override fun compressRes(videoOutCompressPath: String) {
                        if (!TextUtils.isEmpty(videoOutCompressPath)) {
                            fileMsgContent.path = videoOutCompressPath
                            uiMessage.message.extra = fileMsgContent
                            uiMessage.message.message_local_type == TypeConst.chat_msg_type_file_video
                        }
                        uploadAndSendFileMsg(uiMessage)
                    }
                })
            } else {
                uploadAndSendFileMsg(uiMessage)
            }
        }
    }

    fun uploadAndSendFileMsg(uiMessage: UiMessage) {
        val uuid = uiMessage.message.uuid ?:""
        val fileMsgContent = uiMessage.message.extra as FileMsgContent
        val sendMessage = ChatDataConvertUtil.uiMessageToSendMsg(uiMessage)
        UploadUtil.startUpload {
            uploadTask[uuid] = it
            it.upload(File(fileMsgContent.path?:""), object : UploadCallback {
                override fun onProgress(progress: Int, url: String?, ossState: String) {
                    if (progress < 0) {
                        Log.i("byy", "sendOrUploadMsgFail2。。。$progress")
                        sendOrUploadMsgFail(TypeConst.chat_msg_type_file, uuid)
                    } else if (progress >= 100 && !TextUtils.isEmpty(url)) {
                        fileMsgContent.url = url
                        if (fileMsgContent.type == TypeConst.chat_msg_type_file_sub_video && !TextUtils.isEmpty(fileMsgContent.thumbnailPath)) {
                            it.upload(File(fileMsgContent.thumbnailPath!!), object : UploadCallback {
                                override fun onProgress(progress: Int, url: String?, ossState: String) {
                                    if (progress < 0) {
                                        Log.i("byy", "sendOrUploadMsgFail0。。。$progress")
                                        sendOrUploadMsgFail(TypeConst.chat_msg_type_file, uuid)
                                    } else if (progress >= 100 && !TextUtils.isEmpty(url)) {
                                        fileMsgContent.cover = url
                                        fileMsgContent.driver = ossState.toInt()
                                        sendMessage.extra = GlobalGsonUtils.toJson(fileMsgContent)
                                        uiMessage.message.sendStatus = TypeConst.msg_send_status_sending
                                        msgViewModel.sendMsg(GlobalGsonUtils.toJson(sendMessage), uuid, TypeConst.chat_msg_type_file)
                                        uploadTask.remove(uuid)
                                        sendingFileMsgList.remove(uiMessage)
                                        Log.i("byy", "需要重新发送的消息还剩下1： ${sendingFileMsgList.size}")
                                    }
                                    else {
                                        Log.i("websocket", "上传进度2 ： $progress")
//                                        val position = findUiMsgPositionByUUid(uuid)
//                                        position?.let {
//                                            Log.i("websocket", "上传进度3 ： $progress")
//                                            chatDataList[position].progress = progress
//                                            chatActivitySub.refreshItemChanged(position)
//                                        }

                                    }
                                }
                            })

                        } else {
                            fileMsgContent.driver = ossState.toInt()
                            sendMessage.extra = GlobalGsonUtils.toJson(fileMsgContent)
                            uiMessage.message.sendStatus = TypeConst.msg_send_status_sending
                            msgViewModel.sendMsg(GlobalGsonUtils.toJson(sendMessage), uuid, TypeConst.chat_msg_type_file)
                            uploadTask.remove(uuid)
                            sendingFileMsgList.remove(uiMessage)
                            Log.i("byy", "需要重新发送的消息还剩下0： ${sendingFileMsgList.size}")
                        }
                    } else {
                        Log.i("websocket", "上传进度0 ： $progress")
//                        val position = findUiMsgPositionByUUid(uuid)
//                        position?.let {
//                            Log.i("websocket", "上传进度1 ： $progress")
//                            chatDataList[position].progress = progress
//                            chatActivitySub.refreshItemChanged(position)
//                        }
                    }
                }
            })
        }
    }
    private fun createSendMsg(uuid: String, strMsg: String, warnUsers: String, fileMsgContent: FileMsgContent): SendMessage {
        return SendMessage(toUid, strMsg, warnUsers, talkType, TypeConst.chat_msg_type_file, uuid, isSecretModeOpen, secretPwd, GlobalGsonUtils.toJson(fileMsgContent))
    }

    fun createUUid(): String {
        return TypeConst.dev_android + ":" + UniqueUtil.getRandomUUID()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUiChat() {
        chatUiHelper?.bindContentLayout(llContent)
            ?.bindttToSendButton(btnSend)
            ?.bindEditText(etContent as EditText)
            ?.bindBottomLayout(bottomLayout as RelativeLayout)
            ?.bindEmojiLayout(rlEmotion as LinearLayout)
            ?.bindAddLayout(llAdd as LinearLayout)
            ?.bindToAddButton(ivAdd)
            ?.bindToEmojiButton(ivEmo)
            ?.bindAudioBtn(btnAudio)
            ?.bindAudioIv(imgAudio)
            ?.bindEmojiData()
            ?.bindAddFuncData(object: OnExtraFuncListener {
                override fun onExtraFuncClicked(type: Int) {

                    when (type) {
                        TypeConst.add_func_type_album -> { PictureSelectorUtil.openGalleryPic(this@Chat1Activity, sessionId, REQUEST_CODE_IMAG_VIDEO) }
                        TypeConst.add_func_type_video -> { PictureSelectorUtil.openGalleryVideo(this@Chat1Activity, sessionId, REQUEST_CODE_VIDEO) }
                        TypeConst.add_func_type_file -> {
                            // Android11以上设备(无论target多少)不可访问Android/data/...和Android/obb/...目录及其所有子目录,影响不大,忽略.
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            intent.addCategory(Intent.CATEGORY_OPENABLE)
                            intent.type = "*/*"
                            startActivityForResult(intent, REQUEST_CODE_FILE)
                        }
                        TypeConst.add_func_type_call -> {
                            if (WebRtcSessionManager.getCurrentSession() != null) {
                                val callerUser = WebRtcSessionManager.getCallerUser()
                                val opponentUser = WebRtcSessionManager.getOpponentsUser()
                                if (callerUser!= null && callerUser.uid == ApplicationConst.getUserId()) {
                                    CallFloatBoxView.onClickToResume()
                                } else if (opponentUser.isNotEmpty() && isInOpponentUser(ApplicationConst.getUserId(), opponentUser)) {
                                    CallFloatBoxView.onClickToResume()
                                } else {
                                    ToastUtils.show(getString(R.string.calling))
                                }
                                return
                            }

                            if (PermissionUtils.checkFloatPermission(this@Chat1Activity)) {
                                showCallSelect()
                            } else {
                                PermissionUtils.requestSettingCanDrawOverlays(this@Chat1Activity, ApplicationConst.REQUEST_CODE_FLOAT_PERMISSION, ApplicationConst.APPLICATION_ID)
                            }
                        }
                        TypeConst.add_func_type_red_packet -> {}
                    }
                }

                override fun onPageChanged(pageSize: Int) {
                    bottomSecretLine?.visibility = if (pageSize == 0) View.VISIBLE else View.GONE
                    bottomSecretLayout?.visibility = if (pageSize == 0) View.VISIBLE else View.GONE
                }

            })

        // 底部布局弹出，聊天列表上滑
        rvChatList?.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                rvChatList?.post {
                    chatAdapter?.itemCount?.let {
                        if (it > 0) {
                            rvChatList?.smoothScrollToPosition(0)
                        }
                    }
                }
            }
        }

        // 点击空白区域关闭键盘
        rvChatList?.setOnTouchListener { v, event ->
            hideBottomLayout()
            false
        }

        btnAudio?.setOnFinishedRecordListener { audioPath, time ->
            // 录音结束回调
            val file = File(audioPath)
            if (file.exists()) {
                sendFileMessage(file = file, duration = time.toLong(), fileSubType = TypeConst.chat_msg_type_file_sub_audio)
            }
        }
    }

    fun hideBottomLayout() {
        chatUiHelper?.hideBottomLayout(false)
        chatUiHelper?.hideSoftInput()
        etContent?.clearFocus()
        ivEmo?.setImageResource(R.mipmap.ic_emoji)
    }

    private fun setSecretCheckedWithOutEvent(isChecked: Boolean) {
        switchSecret?.setOnCheckedChangeListener(null)
        switchSecret?.isChecked = isChecked
        switchSecret?.setOnCheckedChangeListener(switchSecretListener)
    }

    private fun hideActionStatusBar() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // actionbar隐藏
        supportActionBar?.hide()
        // 添加了之后 edittext弹不上来
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        // 设置状态栏颜色
        window.statusBarColor = resources.getColor(com.legend.commonres.R.color.comm_page_bg)
        // 设置状态栏字体颜色
        val ui = window.decorView.systemUiVisibility
        val ui1 = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.decorView.systemUiVisibility = ui1
//        actionBar?.hide()
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("qbVideo", "onActiviytResult -- resultCode : $resultCode , requestCode: $requestCode")
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_FILE -> {
                    data?.let {
                        val uri: Uri = it.data!!
                        val takeFlags: Int = (it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                        ExecutorHelper.getInstance().diskIO().execute(Runnable {
                            val destPath = FileUtils.getAppFiles(activity.get(), sessionId, FileUtils.FILE_TYPE_FILE) + DocumentFile.fromSingleUri(activity.get()!!, uri)?.name
                            val destFile = File(destPath)
                            FileUtils.copyFile(activity.get(), uri, destFile)

                            sendFileMessage(file = destFile, fileSubType = TypeConst.chat_msg_type_file_sub_file)
                        })
                    }
                }
                REQUEST_CODE_VIDEO -> {
                    val videoSelector = PictureSelector.obtainSelectorList(data)
                    for (media in videoSelector) {
                        sendFileMessage(media = media, fileSubType = TypeConst.chat_msg_type_file_sub_video)
//                        doCompressVideo(media)
                    }
                }
                REQUEST_CODE_IMAGE -> {
                    val selectPics = PictureSelector.obtainSelectorList(data)
                    for (media in selectPics) {
                        sendFileMessage(media = media, fileSubType = TypeConst.chat_msg_type_file_sub_pic)
                    }
                }
                REQUEST_CODE_IMAG_VIDEO -> {
                    val selectAll = PictureSelector.obtainSelectorList(data)
                    for (media in selectAll) {
                        if (PictureMimeType.isHasImage(media.mimeType)) {
                            sendFileMessage(media = media, fileSubType = TypeConst.chat_msg_type_file_sub_pic)
                        } else if (PictureMimeType.isHasVideo(media.mimeType)) {
//                            doCompressVideo(media)
                            sendFileMessage(media = media, fileSubType = TypeConst.chat_msg_type_file_sub_video)
                        }
                    }
                }
                REQUEST_CODE_CAMERE_VIDEO -> {
                    val videoSelector = PictureSelector.obtainSelectorList(data)
                    for (media in videoSelector) {
//                        doCompressVideo(media)
                        sendFileMessage(media = media, fileSubType = TypeConst.chat_msg_type_file_sub_video)
                    }
                }
                REQUEST_CODE_SECRET_MODE -> {
                    isSecretModeOpen = true
                    secretPwd = data?.getStringExtra(KeyConst.key_secret_pwd)?:""
                    setSecretCheckedWithOutEvent(true)
                }
                REQUEST_CODE_DECRYPT_SECRET, REQUEST_CODE_DECRYPT_SECRET_ERROR -> {
                    val msgId = data?.getStringExtra(KeyConst.key_id)?:""
                    val msgContent = data?.getStringExtra(KeyConst.key_secret_msg_content)?:""
                    val msgUUid = data?.getStringExtra(KeyConst.key_secret_msg_uuid)?:""
                    if (!TextUtils.isEmpty(msgId)) {
                        changeCurMsg(msgId, msgContent, msgUUid)
                    }
                }
                REQUEST_FORWARD_MSG -> {
                    chatActivitySub.clearEdit()
                }

            }
        } else if (requestCode == EXTRA_LOGIN_RESULT_CODE) {
            var isLoginQbSuccessInChat = false
            data?.let { isLoginQbSuccessInChat = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false) }
            if (isLoginQbSuccessInChat) {
                QbUtil.loginToRestQb()
            } else {
                var errorMsg = "Unknow error"
                data?.let { errorMsg = it.getStringExtra(EXTRA_LOGIN_ERROR_MESSAGE)?:"" }
                ToastUtils.show(errorMsg)
            }
        }
    }

    private fun changeCurMsg(msgId: String, message: String, uuid: String) {
        var position = findUiMsgPositionByMsgId(msgId)
        position?.let {
            var uiMsg = chatDataList[position]
            RawMsgConvertUtil.replaceDecryptedUiMsg(uiMsg, message, uuid)
            chatAdapter?.notifyItemChanged(position)
        }
    }

    private fun initEvent() {
        LiveEventBus.get<MutableList<ChatMessageModel<Any>>?>(EventKey.key_offline_msg_finished).observe(this) {
            if (it.isNotEmpty()) {
                // 过滤出当前会话的消息，计算偏移量，偏移量时累计的。更新当前会话 - 加入后排序
                val tempList = mutableListOf<ChatMessageModel<Any>>()
                for (item in it) {
                    if (item.session_id.equals(sessionId)) {
                        tempList.add(item)
                    }
                }
                if (tempList.isNotEmpty()) {
                    val tempUiMsgList = ChatDataConvertUtil.chatModelToUiMessage(tempList)
                    if (chatActivitySub.isEdit) {
                        for (data in tempUiMsgList) {
                            data.isEdit = true
                        }
                    }
                    pageOffset+=tempList.size
                    // 离线消息时从小到大给的
                    chatDataList.addAll(tempUiMsgList)
                    chatDataList.sort()
                    chatActivitySub.refreshMsgList(false)
                    MainRequest.reportReadMsg(tempUiMsgList[tempUiMsgList.size -1].message.id, talkType)
                }
            }
        }

        LiveEventBus.get<UserBean.GroupInfo>(EventKey.key_modify_group_info).observe(this) {
            titleBar?.setTitle(it.name)
        }

        LiveEventBus.get<Boolean>(EventKey.key_in_out_group).observe(this) {
            groupViewModel.getGroupInfo(toUid)
        }

        LiveEventBus.get<UserBean.UserInfo>(EventKey.key_modify_other_user_info).observe(this) {
            titleBar?.setTitle(it.getNickName())
        }
    
        LiveEventBus.get<List<UserBean.ConcatSimple>>(EventKey.key_group_call_selected).observe(this) {
            ApplicationConst.opponentList.clear()
            var opponentId = ""
            for (member in it) {
                opponentId += member.id + ", "
                val opponent = UserBean.QbUserInfo(member.id, member.avatar, member.nick_name, member.quickblox_id?:"0")
                ApplicationConst.opponentList.add(opponent)
            }
            opponentId.substring(0, opponentId.length - 1)

            msgViewModel.checkCanCall(talkType, opponentId, toUid, isVideoCall)
        }

        LiveEventBus.get<List<UserBean.ConcatSimple>>(EventKey.key_mention_selected).observe(this) {
            RongMentionManager.getInstance().mentionMembers(it)
        }

        LiveEventBus.get<Int>(EventKey.key_network_socket_change).observe(this) {
            sendingFileMsgList.clear()
            if (it == TypeConst.state_has_network) {
                findSendingFileAndSend(chatDataList)
            }
        }
    }

    private fun findSendingFileAndSend(uiMsgList: MutableList<UiMessage>) {
        if (uiMsgList.isEmpty()) return

        for (uiMsg in uiMsgList) {
            if (uiMsg.message.message_type == TypeConst.chat_msg_type_file
                && uiMsg.message.isSender
                && (uiMsg.message.sendStatus == TypeConst.msg_send_status_uploading || uiMsg.message.sendStatus == TypeConst.msg_send_status_uploading)) {
                uiMsg.message.uuid = uiMsg.message.id
                sendingFileMsgList.add(uiMsg)
            }
        }
        Log.i("byy", "需要重新发送的消息数: ${sendingFileMsgList.size}")
        for (resendMsg in sendingFileMsgList) {
            val fileMsgContent = resendMsg.message.extra as FileMsgContent
            // 如果没封面就默认为视频没上传压缩成功
            beforeUploadFileMsg(resendMsg, if (TextUtils.isEmpty(fileMsgContent.thumbnailPath)) fileMsgContent.path else null)
        }
    }

    fun findUiMsgPositionByMsgId(msgId: String): Int? {
        for ((index, msg) in chatDataList.withIndex()) {
            if (msg.message.id == msgId) {
                return index
            }
        }
        return null
    }

    private fun findUiMsgPositionByUUid(uuid: String): Int? {
        for ((index, msg) in chatDataList.withIndex()) {
            if (msg.message.uuid == uuid) return index
        }
        return null
    }

    private var isVideoCall: Boolean = false
    private fun showCallSelect() {
        DialogUitl.showStringArrayDialog(this, arrayOf(
            R.string.video_call, R.string.audio_call)) { text, tag ->
            startCall(text == getString(R.string.video_call))
        }
    }

    fun startCall(isVideoCall: Boolean) {
        this.isVideoCall =isVideoCall
        if (isVideoCall) {
            Permission.request(CALL_PERMISSIONS, this, object : PermissionListener {
                override fun preRequest(permissions: Array<out String>?): Boolean {
                    return false
                }

                override fun granted(permissions: Array<out String>?) {
                    if (isSingleTalk) {
                        msgViewModel.checkCanCall(talkType, toUid, null, isVideoCall)
                    } else {
                        selectGroupCallMembers()
                    }
                }

                override fun denied(permissions: Array<out String>?) {
                    ToastUtils.show(getString(R.string.permission_denied))
                    CommonDialogUtils.showPermissionDeniedTipsDialog(PermissionUtils.getPermissionName(CALL_PERMISSIONS), this@Chat1Activity)
                }

            })
        } else {
            Permission.request(CALL_PERMISSIONS[1], this, object : PermissionListener {
                override fun preRequest(permissions: Array<out String>?): Boolean {
                    return false
                }

                override fun granted(permissions: Array<out String>?) {
                    if (isSingleTalk) {
                        msgViewModel.checkCanCall(talkType, toUid, null, isVideoCall)
                    } else {
                        selectGroupCallMembers()
                    }
                }

                override fun denied(permissions: Array<out String>?) {
                    ToastUtils.show(getString(R.string.permission_denied))
                    CommonDialogUtils.showPermissionDeniedTipsDialog(PermissionUtils.getPermissionName(CALL_PERMISSIONS[1]), this@Chat1Activity)
                }

            })
        }
    }

    private fun setOnlineState(isOnline: Boolean) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                imgOnlineState?.let {
                    ImgLoader.display(this@Chat1Activity, if (isOnline) R.mipmap.ic_online_dot else R.mipmap.ic_offline_dot, it)
                }
                tvOnlineState?.let {
                    it.text = if (isOnline) getString(R.string.online) else getString(R.string.offline)
                }
            }
        }

    }

    private fun selectGroupCallMembers() {
        val members = arrayListOf<UserBean.GroupMember>()
        for (member in groupMember) {
            if (member.uid.toString() != ApplicationConst.getUserId()) {
                members.add(member)
            }
        }
    
        Router.toSelectMemberActivity(this, 0, null, TypeConst.state_invite_group_call, members, 3)
    }

    fun decryptMsg(id:String) {
        Router.toInputTransActivity(this, REQUEST_CODE_DECRYPT_SECRET, TypeConst.trans_input_page_type_secret, id = id)
    }

    fun decryptMsg(id: String, pwd: String, message: String) {
        Router.toInputTransActivity(this, REQUEST_CODE_DECRYPT_SECRET_ERROR, TypeConst.trans_input_page_type_secret, id = id, pwd = pwd, message = message)
    }

    override fun onPause() {
        super.onPause()
        if (AudioPlayManager.getInstance().isPlaying) {
            AudioPlayManager.getInstance().stopPlay()
        }
    }

    override fun onDestroy() {
        LiveEventBus.get<String>(EventKey.key_session_uid).post("")
        chatActivitySub.onDestroy()
        RongMentionManager.getInstance().destroyInstance(etContent)
        super.onDestroy()
        ActivityManager.getInstance().finishActivity(this)
        msgRevListener?.let {
            MSocket.instance.unRegisterMsgRevListener(it)
        }
    }

    private fun showLoadingDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (loadingDialog == null) loadingDialog = LoadingDialog(this)
        if (!loadingDialog!!.isShowing) {
            loadingDialog!!.show()
        }
    }

    private fun hideLoadingDialog() {
        if (isFinishing || isDestroyed) return
        loadingDialog?.let { it.dismiss() }
    }

    override fun onBackPressed() {
        if (chatActivitySub.isEdit) {
            chatActivitySub.clearEdit()
            return
        }
        super.onBackPressed()
    }

}