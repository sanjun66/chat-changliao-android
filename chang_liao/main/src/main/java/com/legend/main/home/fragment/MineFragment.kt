package com.legend.main.home.fragment

import android.text.TextUtils
import android.text.util.Linkify
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.ClipBoardUtils
import com.legend.baseui.ui.base.BaseFragment
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.common.Router
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.db.entity.DBEntity
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.common.utils.SaveDataUtils
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.common.widget.DialogUitl
import com.legend.main.R
import com.legend.main.databinding.FragmentMineBinding
import com.legend.main.network.viewmodel.LoginViewModel
import com.legend.main.util.LogoutUtil

class MineFragment: BaseFragment<FragmentMineBinding>() {

    private var userInfo: UserBean.UserInfo? = null
    private var realAccount: String? = null

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_mine

    override fun initView(view: View?) {
        mDataBinding?.apply {
            lltEdtUser.setOnClickListener { Router.toUserInfoActivity() }
            lltPwd.setOnClickListener {
                if (!TextUtils.isEmpty(userInfo?.email)) {
                    Router.toResetPwdActivity(TypeConst.type_reset_pwd_email)
                } else {
                    Router.toResetPwdActivity(TypeConst.type_reset_pwd_phone)
                }
            }
            lltSetting.setOnClickListener{Router.toSettingActivity() }
            tvExit.setOnClickListener {
                DialogUitl.showSimpleDialog(mContext, getString(R.string.logout_tip)
                ) { dialog, content ->
                    showLoadingDialog()
                    loginViewModel.logout()
                }
            }
            tvExit.autoLinkMask = Linkify.ALL

            imgQrCode.setOnClickListener {
                Router.toQrCodeDisplayActivity(ApplicationConst.getUserId())
            }
        }
    }

    override fun initData() {
        userViewModel.getUserInfo()

        userViewModel.userInfoRes.observe(this) {
            userInfo = it
            SaveDataUtils.saveUserInfo(it)
            mDataBinding?.apply {
                ApplicationConst.setUserNickName(it.getNickName())
                ApplicationConst.setUserAvatar(it.avatar)
                UserSimpleDataHelper.saveUserInfo(DBEntity.UserSimpleInfo("s" + it.id, it.avatar, it.getNickName(), it.quickblox_id, it.is_disturb))
                if (!TextUtils.isEmpty(it.avatar)) ImgLoader.display(activity, it.avatar, avatar)
                tvName.text = it.getNickName()
                tvAccount.text = it.account
                realAccount = if (!TextUtils.isEmpty(it.phone)) it.phone else it.email
                tvAccount.text = String.format(getString(R.string.user_account1), realAccount)

                avatar.setOnClickListener { Router.toUserInfoActivity() }
                tvName.setOnClickListener { Router.toUserInfoActivity() }
                tvAccount.setOnClickListener {
                    DialogUitl.showStringArrayDialog(context, arrayOf(R.string.copy_account)) { text, tag ->
                        ClipBoardUtils.clipboardCopyText(context, "message", realAccount?:"") }
                }
            }
//            MMKVUtils.putString(KeyConst.key_user_info, GlobalGsonUtils.toJson(it))
        }

        loginViewModel.logoutRes.observe(this) {
            hideLoadingDialog()
            if (it.isSuccess) {
                LogoutUtil.logout()
            } else {
                ToastUtils.show(it.message)
            }
        }

        LiveEventBus.get<Boolean>(EventKey.key_refresh_user_info).observe(this) {
            if (it) userViewModel.getUserInfo()
        }
    }

}