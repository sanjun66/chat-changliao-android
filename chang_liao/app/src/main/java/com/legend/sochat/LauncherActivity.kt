package com.legend.sochat

import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.legend.base.utils.MMKVUtils
import com.legend.base.utils.NetworkUtils
import com.legend.basenet.network.HttpHeaderManager
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.common.ApplicationConst
import com.legend.common.KeyConst
import com.legend.common.Router
import com.legend.main.network.viewmodel.LoginViewModel
import com.legend.sochat.databinding.ActivityLauncherBinding

class LauncherActivity : BaseActivity<ActivityLauncherBinding>() {

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }
    
    override fun getLayoutId() = R.layout.activity_launcher

    override fun initView() {
    }

    override fun initData() {
        if (MMKVUtils.getBoolean(KeyConst.key_is_login, false)
            && !TextUtils.isEmpty(MMKVUtils.getString(KeyConst.key_user_token, ""))
            && !TextUtils.isEmpty(MMKVUtils.getString(KeyConst.key_user_id))
        ) {
            if (NetworkUtils.isAvailableByPing()) {
                loginViewModel.refreshToken()
            } else {
                HttpHeaderManager.setBearerToken(ApplicationConst.getUserToken())
                Router.toHomeActivity()
                finish()
            }
        } else {
            Router.toLoginActivity()
            finish()
        }
    
        loginViewModel.refreshTokenRes.observe(this) {
            if (it != null) {
                ApplicationConst.setUserToken(it.token)
                ApplicationConst.setUserId(it.uid)
                HttpHeaderManager.setBearerToken(it.token)
                Router.toHomeActivity()
                finish()
            } else {
                Router.toLoginActivity()
                finish()
            }
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.fakeStatusBar = false
    }
}