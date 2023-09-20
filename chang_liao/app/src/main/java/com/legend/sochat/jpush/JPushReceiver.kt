package com.legend.sochat.jpush

import android.content.Context
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.legend.base.utils.GlobalGsonUtils
import com.legend.common.utils.NotificationManager

class JPushReceiver: JPushMessageReceiver() {
    override fun onMessage(p0: Context?, p1: CustomMessage?) {
        super.onMessage(p0, p1)

    }

    override fun onNotifyMessageArrived(p0: Context?, p1: NotificationMessage?) {
        super.onNotifyMessageArrived(p0, p1)
        Log.i("byy", "JPush --> " + GlobalGsonUtils.toJson(p1))
        NotificationManager.playSound()
    }

    override fun onNotifyMessageOpened(p0: Context?, p1: NotificationMessage?) {
        super.onNotifyMessageOpened(p0, p1)
        // 无通知跳转目标地址有 onNotifyMessageOpened 点击回调，有通知跳转目标地址时没有 onNotifyMessageOpened 点击回调。
    }
}