package com.legend.imkit.videocall.fragments

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.legend.base.utils.permission.PermissionUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.ApplicationConst
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.imkit.R
import com.legend.imkit.videocall.activity.CallActivity
import com.legend.imkit.videocall.util.WebRtcSessionManager
import com.makeramen.roundedimageview.RoundedImageView
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.AppRTCAudioManager
import kotlinx.coroutines.launch
import java.util.*

const val SPEAKER_ENABLED = "is_speaker_enabled"

class AudioConversationFragment : BaseConversationFragment(), CallActivity.OnChangeAudioDevice {
    private val TAG = AudioConversationFragment::class.simpleName

//    private lateinit var audioSwitchToggleButton: ToggleButton
//    private lateinit var alsoOnCallText: TextView
    private var callerNameTextView: TextView? = null
//    private lateinit var otherOpponentsTextView: TextView
    private var aboutCallLayout: LinearLayout? = null
    private var callerAvatarImageView: RoundedImageView? = null
    private var timerCallText: TextView? = null
    private var tvTips: TextView? = null
    private var imgSmallWindowSwitch: ImageView? = null

    override fun onStart() {
        super.onStart()
        conversationFragmentCallback?.addOnChangeAudioDeviceListener(this)
    }

    override fun onResume() {
        super.onResume()
        conversationFragmentCallback?.addCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        conversationFragmentCallback?.addCallStatusTipsListener(CallStatusTipsImpl(TAG))
    }

    override fun onPause() {
        super.onPause()
        conversationFragmentCallback?.removeCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
        conversationFragmentCallback?.removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        conversationFragmentCallback?.removeCallStatusTipsListener(CallStatusTipsImpl(TAG))
    }

    override fun configureOutgoingScreen() {
        val context: Context = activity as Context
//        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, com.com.legend.ui.R.color.ui_white))
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), com.legend.commonres.R.color.text_black))
        allOpponentsTextView.setTextColor(ContextCompat.getColor(context, com.com.legend.ui.R.color.white))
        ringingTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_call_type))
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        if (view == null) {
            return
        }
        timerCallText = view.findViewById(R.id.timer_call)
        tvTips = view.findViewById(R.id.tv_tips)
        tvTips?.visibility = if (isIncomingCall) View.GONE else View.VISIBLE
        callerAvatarImageView = view.findViewById(R.id.image_caller_avatar)
        callerNameTextView = view.findViewById(R.id.text_caller_name)
        imgSmallWindowSwitch = view.findViewById(R.id.img_small_window_switch)
        imgSmallWindowSwitch?.setOnClickListener {
            if (PermissionUtils.checkFloatPermission(activity)) {
                (activity as CallActivity).finish()
            } else {
                PermissionUtils.requestSettingCanDrawOverlays(activity, ApplicationConst.REQUEST_CODE_FLOAT_PERMISSION, ApplicationConst.APPLICATION_ID)
            }
        }
//        callerAvatarImageView?.setBackgroundDrawable(getColorCircleDrawable(opponents[0].id))

//        alsoOnCallText = view.findViewById(R.id.text_also_on_call)
//        setVisibilityAlsoOnCallTextView()
    
        
//        otherOpponentsTextView = view.findViewById(R.id.text_other_users)
//        otherOpponentsTextView.text = getOtherOpponentNames()

//        audioSwitchToggleButton = view.findViewById(R.id.toggle_speaker)
//        audioSwitchToggleButton.visibility = View.VISIBLE
//        audioSwitchToggleButton.isChecked = MMKVUtils.getBoolean(SPEAKER_ENABLED, true)
        actionButtonsEnabled(true)
    
        aboutCallLayout = view.findViewById(R.id.layout_info_about_call)
        aboutCallLayout?.visibility = if (isIncomingCall) View.VISIBLE else View.GONE
        if (isIncomingCall) {
            val callUser = WebRtcSessionManager.getCallerUser()
            callUser?.let {
                ImgLoader.display(context, it.avatar, callerAvatarImageView)
                callerNameTextView?.text = it.name
            }
        }

        if (conversationFragmentCallback?.isConnectedCall() == true) {
            startedCall()
        }
    }

//    private fun setVisibilityAlsoOnCallTextView() {
//        if (opponents.size < 2) {
//            alsoOnCallText.visibility = View.INVISIBLE
//        }
//    }
//    private fun getOtherOpponentNames(): String {
//        val otherOpponents = ArrayList<QBUser>()
//        otherOpponents.addAll(opponents)
//        otherOpponents.removeAt(0)
//        return makeStringFromUsersFullNames(otherOpponents)
//    }

    private fun makeStringFromUsersFullNames(allUsers: ArrayList<QBUser>): String {
        val usersNames = StringifyArrayList<String>()
        for (user in allUsers) {
            if (user.fullName != null) {
                usersNames.add(user.fullName)
            } else if (user.id != null) {
                usersNames.add(user.id.toString())
            }
        }
        return usersNames.itemsAsString.replace(",", ", ")
    }

    override fun onStop() {
        super.onStop()
        conversationFragmentCallback?.removeOnChangeAudioDeviceListener(this)
    }

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_audio_conversation
    }

    override fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice) {
//        audioSwitchToggleButton.isChecked = newAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
    }
    
    override fun startedCallToRefreshUi() {
        super.startedCallToRefreshUi()
        aboutCallLayout?.visibility = View.VISIBLE
        tvTips?.visibility = View.GONE

        if (!isIncomingCall) {
            if (WebRtcSessionManager.getOpponentsUser().isNotEmpty()) {
                val curUser = WebRtcSessionManager.getOpponentsUser()[0]
                ImgLoader.display(context, curUser.avatar, callerAvatarImageView)
                callerNameTextView?.text = curUser.name
            }
        }
    }
    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
//            val name = opponents[0].fullName ?: opponents[0].login
//            firstOpponentNameTextView.text = name
//            otherOpponentsTextView.text = getOtherOpponentNames()
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

    private inner class CallTimeUpdateListenerImpl(val tag: String?) : CallActivity.CallTimeUpdateListener {
        override fun updatedCallTime(time: String) {
            timerCallText?.text = time
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallTimeUpdateListenerImpl) {
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