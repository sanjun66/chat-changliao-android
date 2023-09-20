package com.legend.common.network.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.legend.base.utils.StringUtils
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.BaseRes
import com.legend.basenet.network.coroutine.request
import com.legend.common.bean.UserBean
import com.legend.common.network.services.UserService
import com.legend.commonres.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class UserViewModel: ViewModel() {
    val userInfoRes = MutableLiveData<UserBean.UserInfo>()
    val userInfoRes1 = MutableLiveData<UserInfoResBean>()
    val avatarRes = MutableLiveData<UserBean.AvatarRes>()
    val changeUserRes = MutableLiveData<UserInfoModifyRes>()
    val changeNoteNameRes = MutableLiveData<BaseRes>()
    val addBlackRes = MutableLiveData<BaseRes>()
    val removeBlackRes = MutableLiveData<FriendBlackRes>()
    val blackListRes = MutableLiveData<UserBean.FriendBlackList>()
    val friendDisturbRes = MutableLiveData<FriendDisturbRes>()
    val onlineStateRes = MutableLiveData<UserBean.OnlineState>()

    private val services by lazy {
        NetworkManager.getInstance().getService(UserService::class.java)
    }

    fun getUserInfo(id: String? = null, targetId: String? = "") {
        request({services.getUserInfo(id)}, {
            if (!TextUtils.isEmpty(targetId)) it.targetId = targetId
            userInfoRes.value = it
        })
    }

    fun getUserInfo1() {
        request({services.getUserInfo(null)}, {
            userInfoRes1.value = UserInfoResBean(true, "", it)
        }, {
            userInfoRes1.value = UserInfoResBean(false, it.message?:"", null)
        })
    }

    fun changeUserInfo(nickName: String? = "", sex: String? = "", note_name: String? = "", apply_auth: String? = "", userInfo: UserBean.UserInfo? = null) {

        val params = HashMap<String, String>()
        if (!TextUtils.isEmpty(nickName)) {
            params["nick_name"] = nickName!!
        }
        if (!TextUtils.isEmpty(sex)) {
            params["sex"] = sex!!
        }
        if (!TextUtils.isEmpty(note_name)) {
            params["note_name"] = note_name!!
        }
        if (!TextUtils.isEmpty(apply_auth)) {
            params["apply_auth"] = apply_auth!!
        }

        request({services.changeUserInfo(params)}, {
            if (!TextUtils.isEmpty(nickName)) userInfo?.nick_name = nickName!!
            if (!TextUtils.isEmpty(sex)) userInfo?.sex = sex!!.toInt()
            if (!TextUtils.isEmpty(note_name)) userInfo?.note_name = note_name!!
            if (!TextUtils.isEmpty(apply_auth)) userInfo?.apply_auth = apply_auth!!.toInt()

            changeUserRes.value = UserInfoModifyRes(true, StringUtils.getString(R.string.success), userInfo)
        }, {
            changeUserRes.value = UserInfoModifyRes(false, it.message?:"", userInfo)
        })
    }

    fun changeNoteName(friendId: String, remark: String) {
        val params = HashMap<String, String>()
        params["friend_id"] = friendId
        params["remark"] = remark
        request({services.changNoteName(params)}, {
            changeNoteNameRes.value = BaseRes(true, StringUtils.getString(R.string.success))
        }, {
            changeNoteNameRes.value = BaseRes(false, it.message)
        })
    }

    fun getBlackFriends() {
        request({services.getBlackFriends()}, {
            blackListRes.value = it
        })
    }

    fun addFriendBlack(friendId: String) {
        request({services.addFriendBlack(friendId)}, {
            addBlackRes.value = BaseRes(true, StringUtils.getString(R.string.success))
        }, {
            addBlackRes.value = BaseRes(false, it.message)
        })
    }

    fun removeFriendBlack(friendId: String, friendBlack: UserBean.FriendBlack? = null) {
        request({services.removeFriendBlack(friendId)}, {
            removeBlackRes.value = FriendBlackRes(true, StringUtils.getString(R.string.success), friendBlack)
        }, {
            removeBlackRes.value = FriendBlackRes(false, it.message, friendBlack)
        })
    }

    fun setFriendDisturb(friendId: String, isDisturb: Boolean) {
        val params = HashMap<String, String>()
        params["friend_id"] = friendId
        params["is_disturb"] = if (isDisturb) "1" else "0"
        request({services.setFriendDisturb(params)}, {
            friendDisturbRes.value = FriendDisturbRes(true, StringUtils.getString(R.string.success), !isDisturb)
        }, {
            friendDisturbRes.value = FriendDisturbRes(false, it.message, !isDisturb)
        })
    }

    /**
     * @type 默认为1 修改用户头像 2的话是上传群头像
     */
    fun changeAvatar(type: Int, fileName: String, driver: String) {
        val params = HashMap<String, String>()
        params["image"] = fileName
        params["type"] = type.toString()
        params["driver"] = driver
        request({services.modifyAvatar(params)}, {
            avatarRes.value = it
        })
    }

    fun getUserOnlineState(uid: String) {
        request({services.getUserOnlineState(uid)}, {
            onlineStateRes.value = it
        })
    }


    private fun toRequestBody(value: String): RequestBody {
        return  RequestBody.create("text/plain".toMediaTypeOrNull(), value)
    }

}

data class FriendBlackRes(val isSuccess: Boolean, val message: String?, val blackFriend: UserBean.FriendBlack?)
data class UserInfoModifyRes(val isSuccess: Boolean, val message: String, val userInfo: UserBean.UserInfo?)
data class FriendDisturbRes(val isSuccess: Boolean, val message:String?, val originalDisturb: Boolean)

data class UserInfoResBean(val isSuccess: Boolean, val message: String, val userInfo: UserBean.UserInfo?)