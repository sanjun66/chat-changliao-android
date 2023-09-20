package com.legend.main.network.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.BaseRes
import com.legend.basenet.network.coroutine.request
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.bean.UserBean
import com.legend.main.network.services.FriendService

class FriendViewModel : ViewModel() {
    val searchFriendRes = MutableLiveData<UserBean.SearchFriendRes>()
    val applyFriendSuccess = MutableLiveData<BaseRes>()
    val applyList = MutableLiveData<UserBean.ApplyList>()
    val checkApplyRes = MutableLiveData<UserBean.CheckApplyLocal>()
    val friendListRes = MutableLiveData<UserBean.FriendListBean>()
    val friendDeleteRes = MutableLiveData<Int>()

    private val services by lazy {
        NetworkManager.getInstance().getService(FriendService::class.java)
    }

    fun searchFriend(account: String) {
        val params = HashMap<String, String>()
        params["keywords"] = account
        request({services.searchFriends(params)}, {
            searchFriendRes.value = it
        }) {
            ToastUtils.show(it.message!!)
        }

    }

    fun applyFriend(id: String, remark: String) {
        val params = HashMap<String, String>()
        params["id"] = id
        params["remark"] = remark
        params["notes"] = ""
        request({services.applyFriend(params)}, {
            // 接口返回 {"data":null,"code":200,"message":"","s":"0.113"}
            applyFriendSuccess.value = BaseRes(true, "")
        }) {
            applyFriendSuccess.value = BaseRes(false, it.message)
        }
    }

    fun getFriendApplyList() {
        request({services.friendApplyList()}, {
            applyList.value = it
        })
    }

    fun checkApply(id: Int, state: Int, processMsg: String, itemPosition: Int) {
        val params = HashMap<String, String>()
        params["id"] = id.toString()
        params["state"] = state.toString()
        params["process_message"] = processMsg
        request({services.checkApply(params)}, {
            checkApplyRes.value = UserBean.CheckApplyLocal(id, state, itemPosition)
        })
    }
    fun getFriends() {
        request({services.getFriends()}, {
            friendListRes.value = it
        })
    }

    fun deleteFriend(friendId: String, position: Int) {
        request({services.deleteFriend(friendId)}, {
            friendDeleteRes.value = position
        }, {
            friendDeleteRes.value = -1
        })
    }

}