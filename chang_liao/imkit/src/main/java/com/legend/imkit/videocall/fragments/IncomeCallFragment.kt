package com.legend.imkit.videocall.fragments

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.legend.base.utils.permission.PermissionUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.ApplicationConst
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.network.viewmodel.MainRequest
import com.legend.imkit.R
import com.legend.imkit.util.RingtonePlayer
import com.legend.imkit.videocall.activity.CallActivity
import com.legend.imkit.videocall.adapter.OpponentSmallAdapter
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import java.io.Serializable
import java.util.concurrent.TimeUnit

// 打视屏call in页面
class IncomeCallFragment : Fragment(), Serializable, View.OnClickListener {
    private val TAG = IncomeCallFragment::class.java.simpleName
    private val CLICK_DELAY = TimeUnit.SECONDS.toMillis(2)

    //Views
    private var callTypeTextView: TextView? = null
    private lateinit var rejectButton: ImageButton
    private lateinit var takeButton: ImageButton
    private lateinit var alsoOnCallText: TextView
    private lateinit var callerNameTextView: TextView
    private var imgSmallWindowSwitch: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var opponentSmallAdapter: OpponentSmallAdapter? = null
    private var tvTips: TextView? = null
    private var vibrator: Vibrator? = null
    private var conferenceType: QBRTCTypes.QBConferenceType? = null
    private var lastClickTime = 0L
    private lateinit var ringtonePlayer: RingtonePlayer
    private lateinit var incomeCallFragmentCallbackListener: IncomeCallFragmentCallbackListener
    private var caller: UserBean.QbUserInfo? = null

    private var currentSession: QBRTCSession? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            incomeCallFragmentCallbackListener = activity as IncomeCallFragmentCallbackListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement OnCallEventsController")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true

        Log.d(TAG, "onCreate() from IncomeCallFragment")
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as CallActivity).addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        (requireActivity() as CallActivity).addCallStatusTipsListener(CallStatusTipsImpl(TAG))
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as CallActivity).removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        (requireActivity() as CallActivity).removeCallStatusTipsListener(CallStatusTipsImpl(TAG))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_income_call, container, false)

        initFields()
        conferenceType?.let {
            initUI(view)
            val isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
            tvTips?.text = if (isVideoCall) getString(R.string.call_invite_you_video) else getString(R.string.call_invite_you_audio)
//            setDisplayedTypeCall(it)
            initButtonsListener()
        }

        val context = activity as Context
        ringtonePlayer = RingtonePlayer(context)
        return view
    }

    private fun initFields() {
        currentSession = WebRtcSessionManager.getCurrentSession()

        currentSession?.let {
            conferenceType = it.conferenceType
            Log.d(TAG, conferenceType.toString() + "From onCreateView()")
        }
    }

    override fun onStart() {
        super.onStart()
        startCallNotification()
    }

    private fun initButtonsListener() {
        rejectButton.setOnClickListener(this)
        takeButton.setOnClickListener(this)
    }

    private fun initUI(view: View) {
        callTypeTextView = view.findViewById(R.id.call_type)
        val callerAvatarImageView: ImageView = view.findViewById(R.id.image_caller_avatar)
        callerNameTextView = view.findViewById(R.id.text_caller_name)
        alsoOnCallText = view.findViewById(R.id.text_also_on_call)
        recyclerView = view.findViewById(R.id.recycler_view)
        rejectButton = view.findViewById(R.id.image_button_reject_call)
        takeButton = view.findViewById(R.id.image_button_accept_call)
        tvTips = view.findViewById(R.id.tv_tips)
        imgSmallWindowSwitch = view.findViewById(R.id.img_small_window_switch)
        imgSmallWindowSwitch?.setOnClickListener {
            if (PermissionUtils.checkFloatPermission(activity)) {
                (activity as CallActivity).finish()
            } else {
                PermissionUtils.requestSettingCanDrawOverlays(activity, ApplicationConst.REQUEST_CODE_FLOAT_PERMISSION, ApplicationConst.APPLICATION_ID)
            }
        }

        caller = WebRtcSessionManager.getCallerUser()
        caller?.let {
            ImgLoader.display(context, it.avatar, callerAvatarImageView)
            callerNameTextView.text = it.name
        }

        if (WebRtcSessionManager.isMultitudeCall) {
            recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            opponentSmallAdapter = OpponentSmallAdapter()
            recyclerView?.adapter = opponentSmallAdapter
            opponentSmallAdapter?.setList(WebRtcSessionManager.getOpponentsUser())

            callTypeTextView?.visibility = View.VISIBLE
            if (WebRtcSessionManager.getOpponentsUser().size < 2) {
                recyclerView?.visibility = View.GONE
                alsoOnCallText.visibility = View.GONE
            } else {
                recyclerView?.visibility = View.VISIBLE
                alsoOnCallText.visibility = View.VISIBLE
                tvTips?.visibility = View.GONE
            }
        } else {
            callTypeTextView?.visibility = View.GONE
            recyclerView?.visibility = View.GONE
            alsoOnCallText.visibility = View.GONE
            tvTips?.visibility = View.VISIBLE
        }


    }

    private fun startCallNotification() {
        Log.d(TAG, "startCallNotification()")

        ringtonePlayer.play(false)

        vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        val vibrationCycle = longArrayOf(0, 1000, 1000)
        vibrator?.hasVibrator()?.let {
            vibrator?.vibrate(vibrationCycle, 1)
        }
    }

    private fun stopCallNotification() {
        Log.d(TAG, "stopCallNotification()")

        ringtonePlayer.stop()
        vibrator?.cancel()
    }

    private fun setDisplayedTypeCall(conferenceType: QBRTCTypes.QBConferenceType) {
        val isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO

        val imageResource = if (isVideoCall) {
            R.drawable.ic_video_white
        } else {
            R.drawable.ic_call
        }
        takeButton.setImageResource(imageResource)
    }

    override fun onStop() {
        stopCallNotification()
        super.onStop()
        Log.d(TAG, "onStop() from IncomeCallFragment")
    }

    override fun onClick(v: View) {
        if (SystemClock.uptimeMillis() - lastClickTime < CLICK_DELAY) {
            return
        }
        lastClickTime = SystemClock.uptimeMillis()

        when (v.id) {
            R.id.image_button_reject_call -> reject()
            R.id.image_button_accept_call -> accept()
            else -> {
            }
        }
    }

    // 接听
    private fun accept() {
        // 按钮设置不可点击，停止铃声播放，接听回调
        enableButtons(false)
        stopCallNotification()
        MainRequest.reportCallState(TypeConst.call_state_accept, WebRtcSessionManager.callUid?:"")

        incomeCallFragmentCallbackListener.onAcceptCurrentSession()
        Log.d(TAG, "Call is started")
    }

    // 拒绝
    private fun reject() {
        enableButtons(false)
        stopCallNotification()

        MainRequest.reportCallState(TypeConst.call_state_refuse, WebRtcSessionManager.callUid?:"")
        incomeCallFragmentCallbackListener.onRejectCurrentSession()
        Log.d(TAG, "Call is rejected")
    }

    private fun enableButtons(enable: Boolean) {
        takeButton.isEnabled = enable
        rejectButton.isEnabled = enable
    }

    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
//            otherUsersTextView?.text = getOtherIncUsersNames()
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

    private inner class CallStatusTipsImpl(val tag: String?): CallActivity.CallStatueTipsListener {
        override fun onTips(qbId: Int?, tips: String, isShow: Boolean) {
            if (WebRtcSessionManager.isMultitudeCall) {
                // todo
            } else {
                tvTips?.text = tips
                tvTips?.visibility = if (isShow) View.VISIBLE else View.GONE
            }
        }
        override fun equals(other: Any?): Boolean {
            if (other is CallStatusTipsImpl) {
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