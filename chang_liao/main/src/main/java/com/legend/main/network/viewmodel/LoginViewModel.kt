package com.legend.main.network.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.legend.base.utils.StringUtils
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.BaseRes
import com.legend.basenet.network.coroutine.request
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.TypeConst
import com.legend.common.bean.AppVersion
import com.legend.common.bean.InternationAreaCode
import com.legend.common.bean.InternationAreaCodesItem
import com.legend.common.bean.LoginRes
import com.legend.main.network.services.LoginService

class LoginViewModel: ViewModel() {
    val internalAllAreaCodes = MutableLiveData<List<InternationAreaCodesItem>>()
    val internalAreaCodes = MutableLiveData<List<InternationAreaCode>>()
    val loginRes = MutableLiveData<LoginRes?>()
    val refreshTokenRes = MutableLiveData<LoginRes?>()
    val resetPwdRes = MutableLiveData<BaseRes>()
    val logoutRes = MutableLiveData<BaseRes>()
    val appVersionRes = MutableLiveData<AppVersion>()

    private val services by lazy {
        NetworkManager.getInstance().getService(LoginService::class.java)
    }

//    fun getAllInternalAreaCodes() {
//        request({services.getInternalAllAreaCodes()}, {
//            val value: List<InternationAreaCodesItem> = it.info
//            for (item in value) {
//                if (item.lists.isEmpty()) continue
//                for (area in item.lists) {
//                    area.title = item.title
//                }
//            }
//            internalAllAreaCodes.value = value
//        }) {
//            ToastUtils.show(it.message ?: it.code.toString())
//        }
//    }

//    fun getInternalAreaCodes(field: String?) {
//        val params = HashMap<String, String?>()
//        params["field"] = field
//        request({services.getInternalAreaCodes(params)}, {
//            internalAreaCodes.value = it.info
//        }) {
//            ToastUtils.show(it.message ?: it.code.toString())
//        }
//    }

    fun register(account: String, pwd: String) {
        val params = HashMap<String, String>()
        params["email"] = account
        params["password"] = pwd
        request({services.register(params)}, {

        }) {
            ToastUtils.show(it.message ?: it.code.toString())
        }
    }

    fun login(account: String, pwdOrVerifyCode: String?, areaCode: String?, type: Int,showToast: Boolean = false, jPushId: String) {
        val params = HashMap<String, String?>()
        if (type == TypeConst.type_login_phone || type == TypeConst.type_login_phone_pwd) {
            params["phone"] = account
            params["login_way"] = "phone"
        } else {
            params["email"] = account
            params["login_way"] = "email"
        }
        if (type == TypeConst.type_login_phone || type == TypeConst.type_login_email) {
            params["code"] = pwdOrVerifyCode
            params["type"] = "2"
        } else {
            params["password"] = pwdOrVerifyCode
            params["type"] = "1"
        }
        params["area_code"] = areaCode?:"86"
        params["platform"] = "android"
        params["registration_id"] = jPushId
    
//        val  v =  SoChatEncryptUtil.encrypt(GlobalGsonUtils.toJson(params))
//        request({services.login(v)}, {
////            loginRes.value = it
//        }) {
////            loginRes.value = null
//            if (showToast) ToastUtils.show(it.message ?: it.code.toString())
//        }
        
        request({services.login(params)}, {
            loginRes.value = it
        }) {
            loginRes.value = null
            if (showToast) ToastUtils.show(it.message ?: it.code.toString())
        }
    }

    fun refreshToken() {
        request({services.refreshToken()}, {
            refreshTokenRes.value = it
        }) {
            refreshTokenRes.value = null
        }
    }

    fun sendVerifyCode(areaCode: String?, account: String, type: String, smsType: String) {
        val params = HashMap<String, String>()
        params["area"] = areaCode?:"86"
        params["account"] = account
        params["type"] = type           // phone 手机，email 邮箱
        params["sms_type"] = smsType    // register 注册，forget 忘记，login 登陆，other 其他
        request({services.sendVerifyCode(params)}, {

        }, {
            if (!TextUtils.isEmpty(it.message)) ToastUtils.show(it.message!!)
        })
    }

    fun forgetPwd(account: String, verifyCode: String, resetPwdType: Int, pwd: String, areaCode: String?) {
        val params = HashMap<String, String>()
        if (resetPwdType == TypeConst.type_reset_pwd_phone) {
            params["phone"] = account
            params["login_way"] = "phone"
        } else {
            params["email"] = account
            params["login_way"] = "email"
        }
        params["password"] = pwd
        params["code"] = verifyCode
        params["area_code"] = areaCode?:"86"
        request({services.forgetPwd(params)}, {
            resetPwdRes.value = BaseRes(true, StringUtils.getString(com.legend.commonres.R.string.success))
        }, {
            resetPwdRes.value = BaseRes(false, it.message)
        })
    }

    fun logout() {
        request({services.logout()}, {
            logoutRes.value = BaseRes(true, "")
        }, {
            logoutRes.value = BaseRes(false, it.message)
        })
    }

    fun getVersionInfo() {
        request({services.getVersionInfo("Android")}, {
            appVersionRes.value = it
        })
    }
}