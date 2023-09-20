package com.legend.main.login

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.text.InputType
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.base.utils.KeyboardUtils
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ActivityManager
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.Router
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.socket.MSocket
import com.legend.main.R
import com.legend.main.databinding.ActivityForgetPwdBinding
import com.legend.main.network.viewmodel.LoginViewModel
import com.legend.main.util.LogoutUtil

@Route(path = RouterPath.path_reset_pwd_activity)
class ResetPwdActivity: BaseActivity<ActivityForgetPwdBinding>() {
    private var type: Int = TypeConst.type_reset_pwd_phone
    private var countDownTimer: CountDownTimer? = null
    private var isNewPwdEyeOpen = false
    private var isConfirmPwdEyeOpen = false

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun getLayoutId(): Int = R.layout.activity_forget_pwd

    override fun initView() {
        type = intent.getIntExtra(TypeConst.reset_pwd_type, TypeConst.type_reset_pwd_phone)

        mDataBinding?.apply {
            ImgLoader.display(mContext, if (isNewPwdEyeOpen) R.mipmap.eye_open else R.mipmap.eye_close, imgEyeNew)
            ImgLoader.display(mContext, if (isConfirmPwdEyeOpen) R.mipmap.eye_open else R.mipmap.eye_close, imgEyeConfirm)
            when(type) {
                TypeConst.type_reset_pwd_phone -> {
                    imgAccount.setImageResource(R.mipmap.login_real_phone)
                    edtAccount.inputType = InputType.TYPE_CLASS_PHONE
                    edtAccount.setHint(R.string.input_phone)
                }

                TypeConst.type_reset_pwd_email -> {
                    imgAccount.setImageResource(R.mipmap.login_real_email)
                    edtAccount.inputType = InputType.TYPE_CLASS_TEXT
                    edtAccount.setHint(R.string.input_email)
                }
            }

            tvGetCode.setOnClickListener { getVerifyCode() }
            tvConfirm.setOnClickListener { doConfirm() }
            rootView.setOnClickListener { KeyboardUtils.hideSoftInput(this@ResetPwdActivity) }
            imgEyeNew.setOnClickListener {
                if (isNewPwdEyeOpen) {
                    isNewPwdEyeOpen = false
                    edtNewPwd.transformationMethod = PasswordTransformationMethod.getInstance()
                    edtNewPwd.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    isNewPwdEyeOpen = true
                    edtNewPwd.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    edtNewPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                ImgLoader.display(mContext, if (isNewPwdEyeOpen) R.mipmap.eye_open else R.mipmap.eye_close, imgEyeNew)
            }
            imgEyeConfirm.setOnClickListener {
                if (isConfirmPwdEyeOpen) {
                    isConfirmPwdEyeOpen = false
                    edtConfirmPwd.transformationMethod = PasswordTransformationMethod.getInstance()
                    edtConfirmPwd.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    isConfirmPwdEyeOpen = true
                    edtConfirmPwd.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    edtConfirmPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                ImgLoader.display(mContext, if (isConfirmPwdEyeOpen) R.mipmap.eye_open else R.mipmap.eye_close, imgEyeConfirm)
            }
        }
    }

    override fun initData() {
        viewModel.resetPwdRes.observe(this) {
            ToastUtils.show(it.message)
            if (it.isSuccess) {
                if (!TextUtils.isEmpty(ApplicationConst.getUserId())) {
                    ToastUtils.show(it.message)
                    LogoutUtil.logout()
                } else {
                    finish()
                }

            }
        }
    }

    private fun getVerifyCode() {
        if (TextUtils.isEmpty(mDataBinding?.edtAccount?.text)) {
            ToastUtils.show(if (type == TypeConst.type_reset_pwd_phone) getString(R.string.input_phone) else getString(R.string.input_email))
            return
        }
        mDataBinding?.tvGetCode?.isEnabled = false
        startCountDown()
//        var countryCode = mDataBinding?.tvCountryCode?.text?.toString()
//        if (countryCode != null && countryCode.startsWith("+")) {
//            countryCode = countryCode.substring(1)
//        }
        viewModel.sendVerifyCode(null, mDataBinding?.edtAccount?.text.toString(), if (type == TypeConst.type_reset_pwd_phone) "phone" else "email", "forget")
    }

    private fun startCountDown() {
        if (countDownTimer == null) countDownTimer = object : CountDownTimer(60 * 1000L, 1 * 1000L) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mDataBinding?.tvGetCode?.text = String.format(getString(R.string.second_resend, (millisUntilFinished / 1000).toString()))
            }

            override fun onFinish() {
                mDataBinding?.tvGetCode?.text = getString(R.string.send_verify_code)
                mDataBinding?.tvGetCode?.isEnabled = true
            }
        }

        countDownTimer?.start()
    }

    private fun doConfirm() {
        mDataBinding?.apply {
            if (TextUtils.isEmpty(edtAccount.text)) {
                ToastUtils.show(
                    when(type) {
                        TypeConst.type_reset_pwd_phone -> getString(R.string.input_phone)
                        TypeConst.type_reset_pwd_email -> getString(R.string.input_email)
                        else -> getString(R.string.input_account)
                    }
                )
                return
            }
            if (TextUtils.isEmpty(edtCode.text)) {
                ToastUtils.show(getString(R.string.input_verification))
                return
            }
            if (TextUtils.isEmpty(edtNewPwd.text)) {
                ToastUtils.show(getString(R.string.input_new_pwd))
                return
            }
            if (TextUtils.isEmpty(edtConfirmPwd.text)) {
                ToastUtils.show(getString(R.string.input_confirm_pwd))
                return
            }
            if (edtNewPwd.text.toString() != edtConfirmPwd.text.toString()) {
                ToastUtils.show(getString(R.string.pwd_not_equal))
                return
            }
            viewModel.forgetPwd(edtAccount.text.toString().trim(), edtCode.text.toString(), type, edtNewPwd.text.toString(), null)
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.fakeStatusBar = true
        pageConfig?.showAppTitleBar = true
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}