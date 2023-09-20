package com.legend.main.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.CountDownTimer
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.base.utils.KeyboardUtils
import com.legend.base.utils.MMKVUtils
import com.legend.basenet.network.HttpHeaderManager
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ActivityManager
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.imkit.util.QbUtil
import com.legend.main.R
import com.legend.main.databinding.ActivityRealLoginBinding
import com.legend.main.network.viewmodel.LoginViewModel
import com.quickblox.users.model.QBUser
import io.rong.imlib.IRongCoreCallback
import io.rong.imlib.IRongCoreEnum
import io.rong.imlib.RongCoreClient
import io.rong.imlib.RongIMClient
import org.json.JSONArray
import org.json.JSONObject

@Route(path = RouterPath.path_real_login_activity)
class LoginRealActivity: BaseActivity<ActivityRealLoginBinding>() {
    private var loginType: Int? = TypeConst.type_login_phone
    private var countDownTimer: CountDownTimer? = null
    private var isPwdEyeOpen = false

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_real_login

    @SuppressLint("SetTextI18n")
    override fun initView() {
        loginType = intent.extras?.getInt(TypeConst.login_type)
        spanPrivacy()
        mDataBinding?.apply {
            ImgLoader.display(mContext, if (isPwdEyeOpen) R.mipmap.eye_open else R.mipmap.eye_close, imgEye)
            initShowView()
            val spannableString = SpannableString(getString(R.string.to_register))
            val foregroundColorSpan = ForegroundColorSpan(resources.getColor(com.legend.commonres.R.color.primary_color))
            spannableString.setSpan(foregroundColorSpan, 8, 10, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tvRegister.text = spannableString

            tvGetCode.setOnClickListener { getVerifyCode() }
            imgEye.setOnClickListener {
                if (isPwdEyeOpen) {
                    isPwdEyeOpen = false
                    edtCode.transformationMethod = PasswordTransformationMethod.getInstance()
                    edtCode.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    isPwdEyeOpen = true
                    edtCode.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    edtCode.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
                ImgLoader.display(mContext, if (isPwdEyeOpen) R.mipmap.eye_open else R.mipmap.eye_close, imgEye)
            }
            tvSwitchLogin.setOnClickListener { switchLogin() }
            tvLogin.setOnClickListener { login() }
            tvRegister.setOnClickListener {
                when(loginType) {
                    TypeConst.type_login_phone, TypeConst.type_login_phone_pwd -> Router.toRegisterActivity(TypeConst.type_register_phone)
                    TypeConst.type_login_email, TypeConst.type_login_email_pwd -> Router.toRegisterActivity(TypeConst.type_register_email)
                }
            }
            tvForwardPwd.setOnClickListener {
                when(loginType) {
                    TypeConst.type_login_phone, TypeConst.type_login_phone_pwd -> Router.toResetPwdActivity(TypeConst.type_reset_pwd_phone)
                    TypeConst.type_login_email, TypeConst.type_login_email_pwd -> Router.toResetPwdActivity(TypeConst.type_reset_pwd_email)

                }
            }
            tvCountryCode.setOnClickListener {
                // todo 暂时不给点击
//                Router.toChooseCountryCodeActivity(mContext as Activity, 101)
            }
            rootView.setOnClickListener { KeyboardUtils.hideSoftInput(this@LoginRealActivity) }
        }

    }

    override fun initData() {
        viewModel.loginRes.observe(this) {
            hideLoadingDialog()
            if (TextUtils.isEmpty(it?.token)) return@observe

            val account = mDataBinding?.edtAccount?.text.toString()
            ApplicationConst.setUserToken(it?.token?:"")
            ApplicationConst.setUserId(it?.uid?:"")
            ApplicationConst.setUserAccount(account)
            MMKVUtils.putBoolean(KeyConst.key_is_login, true)
            val qbUser = QBUser()
            println("wdd---> quickblox_id = ${it?.quickblox_id}")
            qbUser.id = it?.quickblox_id?.toInt()?:0
            qbUser.login = it?.quickblox_login?:""
            qbUser.password = it?.quickblox_pwd?:""
            QbUtil.saveCurrentDbUser(qbUser)
            HttpHeaderManager.setBearerToken(it?.token?:"")
            Router.toHomeActivity()
            ActivityManager.getInstance().finishActivity(LoginActivity::class.java)
            if (it?.rong_yun_token?.isNotEmpty() == true) {
                RongCoreClient.connect(it?.rong_yun_token,object : IRongCoreCallback.ConnectCallback() {
                    override fun onSuccess(t: String?) {
                        RongIMClient.getInstance().disconnect()
                    }
        
                    override fun onError(e: IRongCoreEnum.ConnectionErrorCode?) {
            
                    }
        
                    override fun onDatabaseOpened(code: IRongCoreEnum.DatabaseOpenStatus?) {
            
                    }
        
                })
            }
            finish()
        }
    }

    private fun initShowView() {
        mDataBinding?.apply {
            when(loginType) {
                TypeConst.type_login_phone -> {
                    tvCountryCode.visibility = View.GONE
                    imgDown.visibility = View.GONE
                    imgAccount.visibility = View.VISIBLE
                    tvTip.text = getString(R.string.login_use_phone)
                    edtAccount.inputType = InputType.TYPE_CLASS_PHONE
                    edtAccount.setHint(R.string.input_phone)
                    tvGetCode.visibility = View.VISIBLE
                    edtCode.inputType = InputType.TYPE_CLASS_TEXT
                    edtCode.hint = getString(R.string.input_verification)
                    imgEye.visibility = View.GONE
                    imgAccount.setImageResource(R.mipmap.login_real_phone)
                    imgVerify.setImageResource(R.mipmap.login_verify)
                    mDataBinding?.tvSwitchLogin?.text = getString(R.string.login_pwd)
                }
                TypeConst.type_login_email -> {
                    tvCountryCode.visibility = View.GONE
                    imgDown.visibility = View.GONE
                    imgAccount.visibility = View.VISIBLE
                    tvTip.text = getString(R.string.login_use_email)
                    edtAccount.inputType = InputType.TYPE_CLASS_TEXT
                    edtAccount.setHint(R.string.input_email)
                    tvGetCode.visibility = View.VISIBLE
                    edtCode.inputType = InputType.TYPE_CLASS_TEXT
                    edtCode.hint = getString(R.string.input_verification)
                    imgEye.visibility = View.GONE
                    imgAccount.setImageResource(R.mipmap.login_real_email)
                    imgVerify.setImageResource(R.mipmap.login_verify)
                    mDataBinding?.tvSwitchLogin?.text = getString(R.string.login_pwd)
                }
                TypeConst.type_login_phone_pwd -> {
                    tvCountryCode.visibility = View.GONE
                    imgDown.visibility = View.GONE
                    imgAccount.visibility = View.VISIBLE
                    tvTip.text = getString(R.string.login_use_pwd)
                    edtAccount.inputType = InputType.TYPE_CLASS_PHONE
                    edtAccount.setHint(R.string.input_phone)
                    tvGetCode.visibility = View.GONE
                    edtCode.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    edtCode.transformationMethod = PasswordTransformationMethod.getInstance() // 解决不生效问题
                    edtCode.hint = getString(R.string.input_pwd)
                    imgEye.visibility = View.VISIBLE
                    imgAccount.setImageResource(R.mipmap.login_real_phone)
                    imgVerify.setImageResource(R.mipmap.login_real_pwd)
                    mDataBinding?.tvSwitchLogin?.text = getString(R.string.login_code)
                }
                TypeConst.type_login_email_pwd -> {
                    tvCountryCode.visibility = View.GONE
                    imgDown.visibility = View.GONE
                    imgAccount.visibility = View.VISIBLE
                    tvTip.text = getString(R.string.login_use_pwd)
                    edtAccount.inputType = InputType.TYPE_CLASS_TEXT
                    edtAccount.setHint(R.string.input_email)
                    tvGetCode.visibility = View.GONE
                    edtCode.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    edtCode.transformationMethod = PasswordTransformationMethod.getInstance() // 解决不生效问题
                    edtCode.hint = getString(R.string.input_pwd)
                    imgEye.visibility = View.VISIBLE
                    imgAccount.setImageResource(R.mipmap.login_real_email)
                    imgVerify.setImageResource(R.mipmap.login_real_pwd)
                    mDataBinding?.tvSwitchLogin?.text = getString(R.string.login_code)
                }
            }
        }
    }

    private fun switchLogin() {
        when(loginType) {
            TypeConst.type_login_phone -> loginType = TypeConst.type_login_phone_pwd
            TypeConst.type_login_phone_pwd -> loginType = TypeConst.type_login_phone
            TypeConst.type_login_email -> loginType = TypeConst.type_login_email_pwd
            TypeConst.type_login_email_pwd -> loginType = TypeConst.type_login_email
        }
        mDataBinding?.edtCode?.setText("")
        initShowView()
    }

    private fun spanPrivacy() {
        val privacy = getString(R.string.login_privacy)
        val spannablePrivacy = SpannableString(privacy)
        val msgArray = JSONArray()
        val obj0 = JSONObject()
        obj0.put("title", getString(R.string.privacy_policy))
        obj0.put("url", "file:///android_asset/yszc.html")
        val obj1 = JSONObject()
        obj1.put("title", getString(R.string.service_agreement))
        obj1.put("url", "file:///android_asset/fwxy.html")
        msgArray.put(obj0)
        msgArray.put(obj1)

        val size = msgArray.length()
        for (index in 0 until size) {
            val msgItem: JSONObject = msgArray.get(index) as JSONObject
            val title: String = msgItem.get("title") as String
            val clickableSpan = object : ClickableSpan() {
                @SuppressLint("ResourceAsColor")
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(com.legend.commonres.R.color.primary_color)
                    ds.isUnderlineText = false
                }

                override fun onClick(widget: View) {
                    Router.toWebViewActivity(msgItem.get("url") as String)
                }
            }

            val startIndex = privacy.indexOf(title)
            if (startIndex >= 0) {
                val endIndex = startIndex + title.length
                spannablePrivacy.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }

        mDataBinding?.tvPrivacy?.apply {
            movementMethod = LinkMovementMethod.getInstance()   // 不设置会点击失效
//            setHintTextColor(Color.TRANSPARENT) // 不设置会有背景色（）
            text = spannablePrivacy
        }
    }

    private fun getVerifyCode() {
        if (TextUtils.isEmpty(mDataBinding?.edtAccount?.text)) {
            ToastUtils.show(if (loginType == TypeConst.type_login_phone) getString(R.string.input_phone) else getString(R.string.input_email))
            return
        }
        mDataBinding?.tvGetCode?.isEnabled = false
        startCountDown()
        var countryCode = mDataBinding?.tvCountryCode?.text?.toString()
        if (countryCode != null && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1)
        }
        // todo 获取验证码 接口还没写完
        viewModel.sendVerifyCode(countryCode, mDataBinding?.edtAccount?.text.toString(), if (loginType == TypeConst.type_login_phone) "phone" else "email", "login")
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

    private fun login() {
        if (TextUtils.isEmpty(mDataBinding?.edtAccount?.text)) {
            ToastUtils.show(
                when (loginType) {
                    TypeConst.type_login_phone, TypeConst.type_login_phone_pwd -> getString(R.string.input_phone)
                    TypeConst.type_login_email, TypeConst.type_login_email_pwd -> getString(R.string.input_email)
                    else -> getString(R.string.input_account)
                }
            )
            return
        }
        if (TextUtils.isEmpty(mDataBinding?.edtCode?.text)) {
            ToastUtils.show(if (loginType == TypeConst.type_login_phone_pwd || loginType == TypeConst.type_login_email_pwd) getString(R.string.input_pwd) else getString(R.string.input_verification))
            return
        }
        if (mDataBinding?.checkBox?.isChecked == false) {
            ToastUtils.show(getString(R.string.read_login_privacy))
            return
        }

        var countryCode = mDataBinding?.tvCountryCode?.text?.toString()
        if (countryCode != null && countryCode.startsWith("+")) {
            countryCode = countryCode.substring(1)
        }
        showLoadingDialog()
        val jPushId = JPushInterface.getRegistrationID(mContext)
        viewModel.login(mDataBinding?.edtAccount?.text.toString(), mDataBinding?.edtCode?.text.toString()
            , countryCode, loginType!!,true, jPushId)
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.fakeStatusBar = true
        pageConfig?.showAppTitleBar = true
    }

    // ARouter 启动 registerForActivityResult 收不到
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