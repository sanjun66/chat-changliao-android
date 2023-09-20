package com.legend.main.network.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.BaseRes
import com.legend.basenet.network.coroutine.request
import com.legend.common.bean.UserBean
import com.legend.imkit.videocall.util.getString
import com.legend.main.network.services.WalletService

class WalletViewModel: ViewModel() {
    val walletBalanceRes = MutableLiveData<List<UserBean.WalletBalanceBean>>()
    val uRechargeAddressRes = MutableLiveData<URechargeRes>()
    val coinTypeRes = MutableLiveData<StringArrayRes>()
    val withdrawAddressRes = MutableLiveData<StringArrayRes>()
    val withDrawRes = MutableLiveData<BaseRes>()
    val walletDetailRes = MutableLiveData<List<UserBean.WalletDetailBean>>()

    private val services by lazy {
        NetworkManager.getInstance().getService(WalletService::class.java)
    }

    fun getWalletBalance() {
        request({services.getWalletBalance()}, {
            walletBalanceRes.value = it
        })
    }

    fun getURechargeAddress() {
        request({services.getRechargeAddress()}, {
            uRechargeAddressRes.value = URechargeRes(true, "", it)
        }, {
            uRechargeAddressRes.value = URechargeRes(false, it.message?:"", null)
        })
    }

    fun getWithdrawAddress(showFirst: Boolean) {
        request({services.getWithdrawAddress()}, {
            withdrawAddressRes.value = StringArrayRes(true, "", it, showFirst)
        }, {
            withdrawAddressRes.value = StringArrayRes(false, it.message?:"", null, showFirst)
        })
    }

    fun getCoinType(showFirst: Boolean) {
        request({services.getCoinType()}, {
            coinTypeRes.value = StringArrayRes(true, "", it, showFirst)
        }, {
            coinTypeRes.value = StringArrayRes(false, it.message?:"", null, showFirst)
        })
    }

    fun withDraw(address: String, num: String, currency: String, code: String) {
        val params = HashMap<String, String>()
        params["address"] = address
        params["num"] = num
        params["currency"] = currency
        params["code"] = code
        request({services.withDraw(params)}, {
            withDrawRes.value = BaseRes(true, getString(com.legend.commonres.R.string.success))
        }, {
            withDrawRes.value = BaseRes(false, it.message?:"")
        })
    }

    fun getWalletDetail(page: Int) {
        request({services.getWalletDetail(page)}, {
            walletDetailRes.value = it
        })
    }
}

data class URechargeRes(val isSuccess: Boolean, val message: String, val uRechargeBean: UserBean.URechargeBean?)
data class StringArrayRes(val isSuccess: Boolean, val message: String, val stringArrayRes: List<String>?, val showFirst: Boolean)