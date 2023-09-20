package com.legend.common.utils

import android.text.TextUtils

object TimeUtil {
    fun getCallTimeSecond(callTime: Long?): String {
        if (callTime == null) return ""
        return getCallTime(callTime * 1000)
    }

    fun getCallTime(callTime: Long?): String {
        var time = ""
        callTime?.let {
            val format = String.format("%%0%dd", 2)
            val elapsedTime = it / 1000
            val seconds = String.format(format, elapsedTime % 60)
            val minutes = String.format(format, elapsedTime % 3600 / 60)
            val hours = String.format(format, elapsedTime / 3600)
            time = "$minutes:$seconds"
            if (!TextUtils.isEmpty(hours) && hours != "00") {
                time = "$hours:$minutes:$seconds"
            }
        }
        return time
    }
}