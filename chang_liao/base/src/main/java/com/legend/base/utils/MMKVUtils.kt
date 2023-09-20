package com.legend.base.utils

import android.app.Application
import android.os.Parcelable
import com.tencent.mmkv.MMKV

/**
 * 使用mmkv 来存储配置信息
 */
object MMKVUtils {

    fun initMMKV(application: Application) {
        MMKV.initialize(application)
    }


    fun putInt(key: String, value: Int) {
        MMKV.defaultMMKV().putInt(key, value)
    }
    fun putBoolean(key: String, value: Boolean) {
        MMKV.defaultMMKV().putBoolean(key, value)
    }
    fun putLong(key: String, value: Long) {
        MMKV.defaultMMKV().putLong(key, value)
    }
    fun putFloat(key: String, value: Float) {
        MMKV.defaultMMKV().putFloat(key, value)
    }
    fun putString(key: String, value: String) {
        MMKV.defaultMMKV().putString(key, value)
    }
    fun getBoolean(key: String, default: Boolean): Boolean {
        return MMKV.defaultMMKV().getBoolean(key, default)
    }
    fun getFloat(key: String): Float {
        return MMKV.defaultMMKV().getFloat(key, -1.0f)
    }
    fun getLong(key: String): Long {
        return MMKV.defaultMMKV().getLong(key, -1L)
    }
    fun getString(key: String): String? {
        return MMKV.defaultMMKV().getString(key, "")
    }
    fun getInt(key: String): Int {
        return MMKV.defaultMMKV().getInt(key, -1)
    }
    fun getLong(key: String, defaultVaule: Long = -1L): Long {
        return MMKV.defaultMMKV().getLong(key, defaultVaule)
    }
    fun getString(key: String, defaultVaule: String = ""): String? {
        return MMKV.defaultMMKV().getString(key, defaultVaule)
    }
    fun getInt(key: String,defaultVaule: Int = -1): Int {
        return MMKV.defaultMMKV().getInt(key, defaultVaule)
    }

    fun putDataBean(key: String,T: Parcelable) {
        MMKV.defaultMMKV().encode(key, T)
    }

    fun remove(key: String) {
        MMKV.defaultMMKV().remove(key)
    }

    fun deleteAllData() {
        MMKV.defaultMMKV().clearAll()
    }


}