package com.legend.main.network

import com.legend.base.utils.GlobalGsonUtils
import java.lang.reflect.Type

/**
 * 业务内容数据
 */
data class BaseData<T>(val info: T, val msg: String, val code: String)

/**
 * 数据模型不确定的list
 */
data class BaseListData<T>(val info: List<T>, val msg: String, val code: String) {
    fun<E> getDatabyPosition(index: Int, type: Type): E {
        if (index >= info.size) {
            throw IllegalStateException("index out of list size")
        }
        val str = info[index].toString()
        return GlobalGsonUtils.fromJson(str, type)
    }
}