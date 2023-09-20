package com.legend.imkit.videocall.util

import com.legend.imkit.util.QbUtil
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack

object FLoatViewUtils {
    @JvmStatic
    fun getUserVideos(videoTrackMap: MutableMap<Int, QBRTCVideoTrack>): QBRTCVideoTrack? {
       val item =  videoTrackMap.entries.find {
            val userId = it.key
            val videoTrack = it.value
            QbUtil.getCurrentDbUser().id == userId
        }
        return item?.value
    }

}