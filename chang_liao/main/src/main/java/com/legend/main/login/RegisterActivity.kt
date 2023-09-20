package com.legend.main.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
import android.text.InputType
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.Router
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.main.R
import com.legend.main.databinding.ActivityRegisterBinding
import com.legend.main.network.viewmodel.LoginViewModel

@Route(path = RouterPath.path_register)
class RegisterActivity: BaseActivity<ActivityRegisterBinding>() {
    private var registerType = TypeConst.type_register_phone
    private var countDownTimer: CountDownTimer? = null

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_register

    override fun initView() {
        registerType = intent.extras?.getInt(TypeConst.regieter_type) ?: TypeConst.type_register_phone

        mDataBinding?.apply {
            when(registerType) {
                TypeConst.type_register_phone -> {
                    edtAccount.hint = getString(R.string.input_phone)
                    edtAccount.inputType = InputType.TYPE_CLASS_PHONE
                }

                TypeConst.type_register_email -> {
                    edtAccount.hint = getString(R.string.input_email)
                    edtAccount.inputType = InputType.TYPE_CLASS_TEXT
                    tvCountryCode.visibility = View.GONE
                    imgDown.visibility = View.GONE
                }
            }

            tvGetCode.setOnClickListener { getVerifyCode() }
            tvConfirm.setOnClickListener { register() }
            tvCountryCode.setOnClickListener { Router.toChooseCountryCodeActivity(mContext as Activity, 101) }
        }

    }

    private fun register() {
        if (TextUtils.isEmpty(mDataBinding?.edtAccount?.text)) {
            ToastUtils.show(if (registerType == TypeConst.type_login_phone) getString(R.string.input_phone) else getString(R.string.input_email))
            return
        }
        // todo
//        if (TextUtils.isEmpty(mDataBinding?.edtCode?.text)) {
//            ToastUtils.show(getString(R.string.input_verification))
//            return
//        }
        if (TextUtils.isEmpty(mDataBinding?.edtPwd?.text)) {
            ToastUtils.show(getString(R.string.input_pwd))
            return
        }
        viewModel.register(mDataBinding?.edtAccount?.text.toString(), mDataBinding?.edtPwd?.text.toString())
    }

    private fun getVerifyCode() {
        if (TextUtils.isEmpty(mDataBinding?.edtAccount?.text)) {
            ToastUtils.show(if (registerType == TypeConst.type_login_phone) getString(R.string.input_phone) else getString(R.string.input_email))
            return
        }
        mDataBinding?.tvGetCode?.isClickable = false
        startCountDown()
        var countryCode = mDataBinding?.tvCountryCode?.text?.toString()
        if (countryCode != null && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1)
        }
        // todo 获取验证码

    }

    private fun startCountDown() {
        if (countDownTimer == null) countDownTimer = object : CountDownTimer(60 * 1000L, 1 * 1000L) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mDataBinding?.tvGetCode?.text = (millisUntilFinished / 1000).toString() + " s"
            }

            override fun onFinish() {
                mDataBinding?.tvGetCode?.text = getString(R.string.get_verification_code)
                mDataBinding?.tvGetCode?.isClickable = true
            }
        }

        countDownTimer?.start()
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
        pageConfig?.fakeStatusBar = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val countryCode = data?.getStringExtra(KeyConst.key_code_result)
            countryCode?.let {
                mDataBinding?.tvCountryCode?.text = "+$it"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}