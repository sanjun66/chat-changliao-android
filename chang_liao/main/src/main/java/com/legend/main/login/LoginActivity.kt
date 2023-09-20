package com.legend.main.login

import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.common.Router
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.main.R
import com.legend.main.databinding.ActivityLoginBinding

@Route(path = RouterPath.path_login_activity)
class LoginActivity: BaseActivity<ActivityLoginBinding>() {
    override fun getLayoutId() = R.layout.activity_login

    override fun initView() {
        mDataBinding?.slPhone?.setOnClickListener { Router.toLoginCodeActivity(TypeConst.type_login_phone) }
        mDataBinding?.slEmail?.setOnClickListener { Router.toLoginCodeActivity(TypeConst.type_login_email) }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.fakeStatusBar = false
    }
}