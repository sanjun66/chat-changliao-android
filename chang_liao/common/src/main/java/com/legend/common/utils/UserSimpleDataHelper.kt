package com.legend.common.utils

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import com.google.gson.reflect.TypeToken
import com.legend.base.utils.GlobalGsonUtils
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.coroutine.request
import com.legend.common.db.DbManager
import com.legend.common.db.entity.DBEntity
import com.legend.common.network.services.UserService
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object UserSimpleDataHelper: ViewModel() {
    private const val user_pre = "user_"
    val map = HashMap<String, String>()

    private val services by lazy {
        NetworkManager.getInstance().getService(UserService::class.java)
    }

    suspend fun getUserInfo(uid: String): DBEntity.UserSimpleInfo? {
        return withContext(Dispatchers.IO) {
            var data: DBEntity.UserSimpleInfo? = null
            data = getUserInfoFromMemory(uid)
            if (data == null) {
                data = getDataFromDb(uid)
            }
            if (data == null) {
                data = getUserInfoFromNet(uid)
            }
            data
        }
    }

    fun isGroup(uid: String) = uid.startsWith("g")

    fun getUserInfoForJava(uid: String): DBEntity.UserSimpleInfo? = runBlocking {
        getUserInfo(uid)
    }

    fun saveUserInfo(info: DBEntity.UserSimpleInfo) {
        saveUserInfoToMemory(info)
        saveUserInfoToDb(info)
    }

    private suspend fun getDataFromDb(uid: String): DBEntity.UserSimpleInfo? {
        return withContext(Dispatchers.IO) {
            val info = DbManager.getSoChatDB().userAvatarDao().get(uid)
            if (info != null) saveUserInfoToMemory(info)
            info
        }
    }

    private suspend fun getUserInfoFromMemory(uid: String): DBEntity.UserSimpleInfo? {
        return withContext(Dispatchers.IO) {
            var info: DBEntity.UserSimpleInfo? = null
            val jsonStr = map[user_pre + uid]
            if (jsonStr != null) {
                info = GlobalGsonUtils.fromJson(jsonStr, object : TypeToken<DBEntity.UserSimpleInfo>(){}.type)
            }

            info
        }
    }

    private suspend fun getUserInfoFromNet(uid: String): DBEntity.UserSimpleInfo? {
        return suspendCoroutine {continuation->
            if (isGroup(uid)) {
                request({ services.getGroupInfo(uid.substring(1))}, {
                    val userInfo = DBEntity.UserSimpleInfo(uid, it?.group_info?.avatar, it?.group_info?.name, is_disturb = it?.group_info?.is_disturb?:0)
                    continuation.resume(userInfo)
                    if (!TextUtils.isEmpty(it?.group_info?.avatar)) saveUserInfo(userInfo)
                })
            } else {
                request({ services.getUserInfo(uid.substring(1))}, {
                    val userInfo = DBEntity.UserSimpleInfo(uid, it.avatar, it.getNickName(), it.quickblox_id, it.is_disturb)
                    continuation.resume(userInfo)
                    if (!TextUtils.isEmpty(it.avatar)) saveUserInfo(userInfo)
                })
            }
        }
    }

    private fun saveUserInfoToMemory(info: DBEntity.UserSimpleInfo) {
        map[user_pre + info.uid] = GlobalGsonUtils.toJson(info)
    }

    private fun saveUserInfoToDb(info: DBEntity.UserSimpleInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            DbManager.getSoChatDB().userAvatarDao().insert(info)
        }
    }

    private fun removeUserInfoFromMemory(uid: Int) {
        map.remove(user_pre + uid)
    }

}