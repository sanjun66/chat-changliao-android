package com.legend.main.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.MMKVUtils
import com.legend.base.utils.NetWorkReceiver
import com.legend.base.utils.NetworkUtils
import com.legend.base.utils.permission.Permission
import com.legend.base.utils.permission.PermissionUtils
import com.legend.base.utils.permission.listener.PermissionListener
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.CommonDialogUtils
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.socket.MSocket
import com.legend.common.socket.SocketStateListener
import com.legend.common.utils.ApkUtil
import com.legend.common.utils.QrIdentifyUtil
import com.legend.common.utils.TimeSecondCount
import com.legend.imkit.bean.ChatBean
import com.legend.imkit.util.EXTRA_LOGIN_ERROR_MESSAGE
import com.legend.imkit.util.EXTRA_LOGIN_RESULT
import com.legend.imkit.util.EXTRA_LOGIN_RESULT_CODE
import com.legend.imkit.util.QbUtil
import com.legend.imkit.videocall.service.LoginService
import com.legend.main.R
import com.legend.main.databinding.ActivityHomeBinding
import com.legend.main.dialog.MorePopWindow
import com.legend.main.dialog.MorePopWindow.OnPopWindowItemClickListener
import com.legend.main.dialog.UpdateDialog
import com.legend.main.home.adapter.HomeVpAdapter
import com.legend.main.home.fragment.ChatListFragment
import com.legend.main.home.fragment.FriendFragment
import com.legend.main.home.fragment.MineFragment
import com.legend.main.network.viewmodel.GroupViewModel
import com.legend.main.network.viewmodel.LoginViewModel
import com.legend.main.util.GroupOpUtil
import com.yxing.ScanCodeConfig

@Route(path = RouterPath.path_home_activity)
class HomeActivity: BaseActivity<ActivityHomeBinding>(), OnPopWindowItemClickListener {
    private val TAB_CHAT = 0
    private val TAB_FRIEND = 1
//    private val TAB_FIND = 2
    private val TAB_MINE = 2

    private var fragments = arrayListOf<Fragment>()
    private var netWorkReceiver: NetWorkReceiver? = null
    private var isForceUpdate = false       // 是否强制更新
    private var isVersionChecked = false    // 是否检查过更新
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }
    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_home

    override fun initView() {
        fragments.add(ChatListFragment())
        fragments.add(FriendFragment())
//        fragments.add(FindFragment())
        fragments.add(MineFragment())

        val adapter = HomeVpAdapter(fragments, this)
        mDataBinding?.apply {
            viewPage.adapter = adapter
            viewPage.isUserInputEnabled = false
            viewPage.offscreenPageLimit = 4
            viewPage.registerOnPageChangeCallback(object:
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomBar.currentItem = position
                }

            })

            bottomBar.setOnItemSelectedListener { bottomBarItem, previousPosition, currentPosition ->
                viewPage.setCurrentItem(currentPosition, false)
                titleChange()
            }

            imgMore.setOnClickListener {
                when(viewPage.currentItem) {
                    TAB_CHAT, TAB_FRIEND/*, TAB_FIND */ -> {
                        val morePopWindow = MorePopWindow(this@HomeActivity, this@HomeActivity)
                        morePopWindow.showPopupWindow(imgMore, 0.8f, -getXOffset(), DisplayUtils.dp2px(mContext, 12f))
                    }
                    TAB_MINE -> {}
                }
            }

//            tvNewFriends.setOnClickListener {
//                mDataBinding?.newFriendRedDot?.visibility = View.GONE
//                Router.toApplyListActivity()
//            }

        }
        
        checkNotificationEnable()
    }
    private fun isNotificationEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context.applicationContext).areNotificationsEnabled()
    }
    private fun checkNotificationEnable() {
        if (!isNotificationEnabled(this@HomeActivity)) {
            CommonDialogUtils.showPermissionDeniedTipsDialog("通知", this@HomeActivity)
        }
    }
    
    private fun titleChange() {
        mDataBinding?.apply {
            when(viewPage.currentItem) {
                TAB_CHAT -> {
                    tvTitle.visibility = View.VISIBLE
                    imgMore.visibility = View.VISIBLE
                    tvTitle.text = getString(R.string.tab_chat)
                    showFakeStatusBar()
                }
                TAB_FRIEND -> {
                    tvTitle.visibility = View.VISIBLE
                    imgMore.visibility = View.VISIBLE
                    tvTitle.text = getString(R.string.tab_friends)
                    showFakeStatusBar()
                }
//                TAB_FIND -> {
//                    tvTitle.visibility = View.VISIBLE
//                    imgMore.visibility = View.VISIBLE
//                    tvTitle.text = getString(R.string.tab_find)
//                    showFakeStatusBar()
//                }
                TAB_MINE -> {
                    tvTitle.visibility = View.GONE
                    imgMore.visibility = View.GONE
                    hideFakeStatusBar()
                }
            }
        }
    }

    override fun initData() {
        ApplicationConst.VOICE_ALL_MUTE = MMKVUtils.getBoolean(KeyConst.key_message_notify_mute, false)
        initEvent()
        startSocketTimer()
        if (NetworkUtils.isAvailableByPing()) {
            Log.i("websocket", "opensocket initData")
            openSocket()
        } else {
            LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_no_network)
        }
        initNetChangeReceiver()
        QbUtil.startLoginService(this)

        loginViewModel.appVersionRes.observe(this) {
            if (it.version_code > ApkUtil.getVersionCode(mContext, packageName).toString()) {
                isForceUpdate = it.forced_update == TypeConst.type_yes
                UpdateDialog(mContext, it).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isVersionChecked || isForceUpdate) {
            loginViewModel.getVersionInfo()
            isVersionChecked = true
        }
    }

    private fun initNetChangeReceiver() {
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        netWorkReceiver = NetWorkReceiver { state, lastState ->
            if (lastState == NetWorkReceiver.NETWORK_NONE && state != NetWorkReceiver.NETWORK_NONE) {
                // 有网络了
                if (MSocket.instance.socketClient == null || MSocket.instance.socketClient!!.isClosed || MSocket.instance.socketClient!!.isClosing) {
                    Log.i("websocket", "opensocket 有网络了")
                    LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_connecting)
                    openSocket()
                }
                Log.i("byy", " 有网络了1")
                LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_has_network)
            } else if (lastState != NetWorkReceiver.NETWORK_NONE && state == NetWorkReceiver.NETWORK_NONE) {
                // 无网络了
                if (!NetworkUtils.isConnected()) {
                    Log.i("byy", " 无网络了1")
                    LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_no_network)
                    closeSocket()
                }
            }
        }
        registerReceiver(netWorkReceiver, filter)
    }

    private fun closeSocket() {
        try {
            MSocket.instance.socketClient?.close()
            MSocket.instance.socketClient = null
        } catch (e: java.lang.Exception) {}
    }

    private fun openSocket() {
        Log.i("websocket", "isNetconnect = ${NetworkUtils.isConnected()}, socketIsOpen = ${MSocket.instance.socketClient?.isOpen == true}")
        if (!NetworkUtils.isConnected() || MSocket.instance.socketClient?.isOpen == true) return
        LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_connecting)
        MSocket.instance.setServer(
            "wss://weilian.site/wss?token=" + ApplicationConst.getUserToken(),
            object : SocketStateListener {
                override fun onOpen() {
//                    LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_connect_success)
                    // 第一次去拉取概率性的出现没有socket消息
//                    LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_pulling)
                    pullOfflineMessage()
                    pullTalkReadMessage()
                }

                override fun onError(isNormalClose: Boolean) {
                    LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_socket_connect_fail)
                    if (!isNormalClose) {
                        Log.i("websocket", "opensocket连接失败后去连接")
                        closeSocket()
//                        openSocket()
                    }
                }

            })

        MSocket.instance.connect()
    }

    private fun initEvent() {
        LiveEventBus.get<Boolean>(EventKey.key_is_app_foreground).observe(this) {
            Log.i("websocket", "home 接收到是否在前台 = $it")
            if (it) {
                // 前台
                if (NetworkUtils.isAvailableByPing()) {
                    openSocket()
                } else {
                    LiveEventBus.get<Int>(EventKey.key_network_socket_change).post(TypeConst.state_no_network)
                }
                startSocketTimer()
            } else {
                // 后台
                stopSocketTimer()
                closeSocket()
            }
        }
        // 有好友申请
        LiveEventBus.get<Int>(EventKey.key_friend_show_dot).observe(this) {
            mDataBinding?.tabFriends?.setUnreadNum(it)
        }
        LiveEventBus.get<Boolean>(EventKey.key_to_home_message_page).observe(this) {
            if (mDataBinding?.viewPage?.currentItem != TAB_CHAT) {
                mDataBinding?.viewPage?.setCurrentItem(TAB_CHAT, false)
            }
        }
        LiveEventBus.get<String>(EventKey.key_delete_group).observe(this) {
            GroupOpUtil.deleteGroup(it)
        }
        LiveEventBus.get<Int>(EventKey.key_msg_unread_num).observe(this) {
            mDataBinding?.tabChat?.setUnreadNum(it)
            JPushInterface.setBadgeNumber(this, it)
        }
    }

    private fun pullOfflineMessage() {
        val pullMessage = ChatBean.SendGetOfflineMessage()
        MSocket.instance.sendMessage(GlobalGsonUtils.toJson(pullMessage))
    }

    private fun pullTalkReadMessage() {
        val readMessage = ChatBean.SendGetReadMessage()
        MSocket.instance.sendMessage(GlobalGsonUtils.toJson(readMessage))
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.fakeStatusBar = true
    }

    private fun getXOffset(): Int {
        val popSelfXOffset = resources.getDimension(R.dimen.seal_main_title_popup_width)  - 2.2 * (mDataBinding?.imgMore?.width ?: 0)
        return popSelfXOffset.toInt()
    }

    override fun onStartChartClick() {
        Router.toSelectMemberActivity(this, 0, null, TypeConst.state_single_chat, null)
    }

    override fun onCreateGroupClick() {
        Router.toSelectMemberActivity(this,0, null, 0, null)
    }

    override fun onAddFriendClick() {
        Router.toSearchFriendActivity()
    }

    override fun onScanClick() {
        Permission.request(Manifest.permission.CAMERA, HomeActivity@this, object : PermissionListener {
                override fun preRequest(permissions: Array<String>): Boolean {
                    return false
                }

                override fun granted(permissions: Array<String>) {
                    QrIdentifyUtil.qrScan(this@HomeActivity)
                }

                override fun denied(permissions: Array<String>) {
                    ToastUtils.show(mContext.getString(com.legend.imkit.R.string.permission_denied))
                    CommonDialogUtils.showPermissionDeniedTipsDialog(PermissionUtils.getPermissionName(Manifest.permission.CAMERA), this@HomeActivity)
                }
            })
    }

    override fun onDestroy() {
        LoginService.destroyRTCClient(this)
        stopSocketTimer()
        closeSocket()
        super.onDestroy()
        netWorkReceiver?.let { unregisterReceiver(it) }
        MSocket.instance.socketClient?.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EXTRA_LOGIN_RESULT_CODE) {
            var isLoginQbSuccessInChat = false
            data?.let { isLoginQbSuccessInChat = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false) }
            if (isLoginQbSuccessInChat) {
                QbUtil.loginToRestQb()
            } else {
                var errorMsg = "未知错误"
                data?.let { errorMsg = it.getStringExtra(EXTRA_LOGIN_ERROR_MESSAGE)?:"" }
                Log.e("qbVideo", "errorMsg111 = " + errorMsg)
            }
        }

        if (resultCode == RESULT_OK) {
            when(requestCode) {
                ScanCodeConfig.QUESTCODE -> {
                    // 接收扫码结果
                    if (data == null) return
                    val extras = data.extras ?: return
                    val code = extras.getString(ScanCodeConfig.CODE_KEY)
                    QrIdentifyUtil.qrIdentify(code, true)
                }
            }
        }

    }

    private var socketTimer: TimeSecondCount? = null
    private val CHECK_SOCKET_TIME_COUNT = 5
    private fun startSocketTimer() {
        Log.i("websocket", "startSocketTimer.....")
        stopSocketTimer()
        socketTimer = TimeSecondCount(object : TimeSecondCount.OnTimerChangeListener {
            override fun onTimeChanged(second: Int) {
//                Log.i("websocket", "startSocketTimer..... $second")
                if (second >= CHECK_SOCKET_TIME_COUNT) {
                    socketTimer?.currentSecond = 0
                    if (MSocket.instance.socketClient?.isOpen != true) {
                        Log.i("websocket", "startSocketTimer..... 去连接")
                        openSocket()
                    }
                }
            }

        })
        socketTimer?.startTimer()
    }

    private fun stopSocketTimer() {
        Log.i("websocket", "stopSocketTimer.....")
        socketTimer?.closeTimer()
        socketTimer = null
    }

}