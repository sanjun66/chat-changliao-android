package com.legend.imkit.videocall.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.MMKVUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.network.viewmodel.MainRequest
import com.legend.imkit.R
import com.legend.imkit.util.EXTRA_IS_INCOMING_CALL
import com.legend.imkit.util.QbUtil
import com.legend.imkit.videocall.activity.CallActivity
import com.legend.imkit.videocall.service.CallService
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.makeramen.roundedimageview.RoundedImageView
import com.quickblox.chat.QBChatService
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCTypes
import java.util.*
import kotlin.collections.HashMap

private val TAG = BaseConversationFragment::class.java.simpleName
const val MIC_ENABLED = "is_microphone_enabled"

abstract class BaseConversationFragment : BaseToolBarFragment() {
    private val TAG = BaseConversationFragment::class.simpleName
    protected var isIncomingCall: Boolean = false       // 来电
//    protected lateinit var timerCallText: TextView
    protected var conversationFragmentCallback: ConversationFragmentCallback? = null
    protected lateinit var currentUser: QBUser
    protected val opponents: ArrayList<UserBean.QbUserInfo> = arrayListOf()
    private var isStarted: Boolean = false

    private lateinit var audioSwitchToggleButton: ToggleButton
    private lateinit var micToggleVideoCall: ToggleButton
    private lateinit var handUpVideoCall: ImageButton
    protected lateinit var outgoingOpponentsRelativeLayout: View
    protected lateinit var allOpponentsTextView: TextView
    protected lateinit var ringingTextView: TextView
    protected lateinit var rivAvatar: RoundedImageView
    private val callStateListener = CallStateListenerImpl(TAG)
    private var audioCheckListener: CompoundButton.OnCheckedChangeListener? = null

    companion object {
        fun newInstance(baseConversationFragment: BaseConversationFragment, isIncomingCall: Boolean): BaseConversationFragment {
            Log.d(TAG, "isIncomingCall =  $isIncomingCall")
            val args = Bundle()
            args.putBoolean(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            baseConversationFragment.arguments = args
            return baseConversationFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            conversationFragmentCallback = context as ConversationFragmentCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement ConversationFragmentCallback")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conversationFragmentCallback?.addCallStateListener(callStateListener)
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
    }

    private fun initEvent() {
        LiveEventBus.get<Boolean>(EventKey.key_qb_enable_phone_speaker).observe(this) {
            audioSwitchToggleButton.setOnCheckedChangeListener (null)
            audioSwitchToggleButton.isChecked = it
            audioSwitchToggleButton.setOnCheckedChangeListener (audioCheckListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        initFields()
        initViews(view)
        initButtonsListener()
        prepareAndShowOutgoingScreen()
        initEvent()

        return view
    }

    private fun prepareAndShowOutgoingScreen() {
        configureOutgoingScreen()

        if (WebRtcSessionManager.getOpponentsUser().isNotEmpty()) {
            ImgLoader.display(context, WebRtcSessionManager.getOpponentsUser()[0].avatar, rivAvatar)
            allOpponentsTextView.text = WebRtcSessionManager.getOpponentsUser()[0].name
        }

//        allOpponentsTextView.text = getNamesFromOpponents(opponents)
    }

    private fun getNamesFromOpponents(allOpponents: ArrayList<QBUser>): String {
        val opponentNames = StringifyArrayList<String>()
        for (opponent in allOpponents) {
            if (opponent.fullName != null) {
                opponentNames.add(opponent.fullName)
            } else if (opponent.id != null) {
                opponentNames.add(opponent.id.toString())
            }
        }
        return opponentNames.itemsAsString.replace(",", ", ")
    }

    protected abstract fun configureOutgoingScreen()

    protected open fun initFields() {
        if (QBChatService.getInstance().user == null) {
            currentUser = QbUtil.getCurrentDbUser()
        } else {
            currentUser = QBChatService.getInstance().user
        }

        arguments?.let {
            isIncomingCall = it.getBoolean(EXTRA_IS_INCOMING_CALL, false)
        }
        initOpponents()
        Log.d(TAG, "opponents: $opponents")
    }

    override fun onStart() {
        super.onStart()
        if (isIncomingCall) {
            conversationFragmentCallback?.acceptCall(HashMap())
        } else {
            val userInfo = hashMapOf<String, String>()
            if (!WebRtcSessionManager.isMultitudeCall) {
                userInfo["isMultitude"] = TypeConst.type_call_single
                userInfo["uid"] = ApplicationConst.getUserId()
            } else {
                var params = ApplicationConst.getUserId() + "&" + QbUtil.getCurrentDbUser().id.toString()
                for (opponent in ApplicationConst.opponentList) {
                    params += "," + opponent.uid  + "&" + opponent.qbId
                }
                userInfo["isMultitude"] = TypeConst.type_call_multitude
                userInfo["uid"] = ApplicationConst.getUserId()
                userInfo["parms"] = params
            }
            userInfo["callUid"] = WebRtcSessionManager.callUid?:""

            conversationFragmentCallback?.startCall(userInfo)
        }
    }

    override fun onDestroy() {
        conversationFragmentCallback?.removeCallStateListener(callStateListener)
        conversationFragmentCallback?.removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        super.onDestroy()
    }

    protected open fun initViews(view: View?) {
        view?.let {
            micToggleVideoCall = it.findViewById(R.id.toggle_mic)
            micToggleVideoCall.isChecked = MMKVUtils.getBoolean(MIC_ENABLED, true)
            audioSwitchToggleButton = view.findViewById(R.id.toggle_speaker)
            audioSwitchToggleButton.isChecked = MMKVUtils.getBoolean(SPEAKER_ENABLED, false)
//            conversationFragmentCallback?.onSwitchAudio(audioSwitchToggleButton.isChecked)
            handUpVideoCall = it.findViewById(R.id.button_hangup_call)
            outgoingOpponentsRelativeLayout = it.findViewById(R.id.layout_background_outgoing_screen)
            allOpponentsTextView = it.findViewById(R.id.text_outgoing_opponents_names)
            ringingTextView = it.findViewById(R.id.text_ringing)
            rivAvatar = it.findViewById(R.id.riv_avatar)
        }

        if (isIncomingCall) {
            hideOutgoingScreen()
        }
    }

    protected open fun initButtonsListener() {
        micToggleVideoCall.setOnCheckedChangeListener { buttonView, isChecked ->
            MMKVUtils.putBoolean(MIC_ENABLED, isChecked)
            conversationFragmentCallback?.onSetAudioEnabled(isChecked)
        }

        audioCheckListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                MMKVUtils.putBoolean(SPEAKER_ENABLED, isChecked)
                conversationFragmentCallback?.onSwitchAudio(isChecked)
            }
        audioSwitchToggleButton.setOnCheckedChangeListener (audioCheckListener)

        handUpVideoCall.setOnClickListener {
            val hangUpState = if (isStarted) TypeConst.call_state_hang_up else if (isIncomingCall) TypeConst.call_state_refuse else TypeConst.call_state_cancel
            MainRequest.reportCallState(hangUpState, WebRtcSessionManager.callUid?:"")
            actionButtonsEnabled(false)
            handUpVideoCall.isEnabled = false
            handUpVideoCall.isActivated = false
            CallService.stop(activity as Activity)
            conversationFragmentCallback?.onHangUpCurrentSession()
            Log.d(TAG, "Call is stopped")
        }
    }

    private fun clearButtonsState() {
        MMKVUtils.remove(MIC_ENABLED)
        MMKVUtils.remove(SPEAKER_ENABLED)
        MMKVUtils.remove(CAMERA_ENABLED)
    }

    protected open fun actionButtonsEnabled(inability: Boolean) {
        micToggleVideoCall.isEnabled = inability
        micToggleVideoCall.isActivated = inability
        audioSwitchToggleButton.isActivated = inability
    }

    private fun startTimer() {
        if (!isStarted) {
//            timerCallText.visibility = View.VISIBLE
            isStarted = true
        }
    }

    private fun hideOutgoingScreen() {
        outgoingOpponentsRelativeLayout.visibility = View.GONE
    }

    private fun initOpponents() {
        opponents.clear()
        opponents.addAll(WebRtcSessionManager.getOpponentsUser())

        if (!isIncomingCall) {
            val caller = WebRtcSessionManager.getCallerUser()!!
            opponents.add(0, caller)
        }
    }

    fun getConnectionState(userId: Int): QBRTCTypes.QBRTCConnectionState? {
        return conversationFragmentCallback?.getPeerChannel(userId)
    }

    protected fun startedCall() {
        callStateListener.startedCall()
    }

    open fun startedCallToRefreshUi() {
    
    }
    private inner class CallStateListenerImpl(val tag: String?) : CallActivity.CallStateListener {
        override fun startedCall() {
            hideOutgoingScreen()
            startedCallToRefreshUi()
            startTimer()
            actionButtonsEnabled(true)
        }

        override fun stoppedCall() {
            isStarted = false
            clearButtonsState()
            actionButtonsEnabled(false)
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallStateListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
            initOpponents()
//            allOpponentsTextView.text = getNamesFromOpponents(opponents)
        }

        override fun equals(other: Any?): Boolean {
            if (other is UpdateOpponentsListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}