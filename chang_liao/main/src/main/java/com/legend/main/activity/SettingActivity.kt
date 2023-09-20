package com.legend.main.activity

import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.base.utils.AppUtils
import com.legend.base.utils.MMKVUtils
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.KeyConst
import com.legend.common.Router
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.common.utils.SaveDataUtils
import com.legend.main.R
import com.legend.main.databinding.ActivitySettingBinding

@Route(path = RouterPath.path_setting_activity)
class SettingActivity: BaseActivity<ActivitySettingBinding>() {
    private var mUserInfo: UserBean.UserInfo? = null
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun getLayoutId(): Int = R.layout.activity_setting

    override fun initView() {
        mDataBinding?.apply {
            sivCurrentVersion.setTips("v" + AppUtils.getVersionName(mContext))
            sivMute.setCheckedWithOutEvent(ApplicationConst.VOICE_ALL_MUTE)
            sivVerify.setSwitchCheckListener { buttonView, isChecked ->
                userViewModel.changeUserInfo(apply_auth = if (isChecked) "1" else "0", userInfo = mUserInfo)
            }
            sivMute.setSwitchCheckListener { buttonView, isChecked ->
                ApplicationConst.VOICE_ALL_MUTE = isChecked
                MMKVUtils.putBoolean(KeyConst.key_message_notify_mute, isChecked)
            }
            sivBlackList.setOnClickListener { Router.toFriendBlackListActivity() }
        }
        setTitleBarTitleText(getString(R.string.setting_setting))
    }

    override fun initData() {
        userViewModel.getUserInfo()

        userViewModel.userInfoRes.observe(this) {
            mUserInfo = it
            SaveDataUtils.saveUserInfo(it)
            mDataBinding?.sivVerify?.setCheckedWithOutEvent(it.apply_auth == TypeConst.type_yes)
        }
        userViewModel.changeUserRes.observe(this) {
            ToastUtils.show(it.message)
            if (it.isSuccess) {
                mDataBinding?.sivVerify?.setCheckedWithOutEvent(it.userInfo?.apply_auth == TypeConst.type_yes)
            } else {
                mDataBinding?.sivVerify?.setCheckedWithOutEvent(it.userInfo?.apply_auth == TypeConst.type_yes)
            }
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}