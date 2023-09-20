package com.legend.common.utils

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.MMKVUtils
import com.legend.common.KeyConst
import com.legend.common.bean.UserBean

object SaveDataUtils {
    fun saveUserInfo(userInfo: UserBean.UserInfo) {
        MMKVUtils.putString(KeyConst.key_user_info, GlobalGsonUtils.toJson(userInfo))
    }

    fun getUserInfo(): UserBean.UserInfo? {
        val user = MMKVUtils.getString(KeyConst.key_user_info)
        if (TextUtils.isEmpty(user)) return null
        return GlobalGsonUtils.fromJson(user, object: TypeToken<UserBean.UserInfo>(){}.type)
    }
}