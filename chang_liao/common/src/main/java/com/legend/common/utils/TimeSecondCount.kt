package com.legend.common.utils

import java.util.*

/**
 * 秒计时器
 */
class TimeSecondCount(val timerChangeListener: OnTimerChangeListener? = null) {
    private var timer: Timer? = null

    var currentSecond: Int = 0

    fun startTimer() {
        if (timer == null) {
            timer = Timer()
        } else {
            timer?.cancel()
        }

        val timeTask = object : TimerTask() {
            override fun run() {
                currentSecond++
                timerChangeListener?.onTimeChanged(currentSecond)
            }
        }

        timer?.schedule(timeTask, 1000, 1000)
    }


    fun closeTimer() {
        timer?.cancel()
        timer = null
        currentSecond = 0
    }

    interface OnTimerChangeListener {
        fun onTimeChanged(second: Int)
    }
}