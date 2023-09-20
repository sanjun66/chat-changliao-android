package com.legend.imkit.videocall.adapter

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.legend.baseui.ui.adapter.recyclerview.BaseBindingAdapter
import com.legend.baseui.ui.adapter.recyclerview.VBViewHolder
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.ApplicationConst
import com.legend.common.bean.UserBean
import com.legend.imkit.databinding.ItemMultiOpponentBinding
import com.legend.imkit.databinding.ItemOpponentSmallBinding
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

class OpponentSmallAdapter: BaseBindingAdapter<ItemOpponentSmallBinding, UserBean.QbUserInfo>() {
    override fun convert(
        holder: VBViewHolder<ItemOpponentSmallBinding>,
        item: UserBean.QbUserInfo
    ) {
        ImgLoader.display(context, item.avatar, holder.vb.bivUser)
    }
}

class MultiCallAdapter(private val isVideo: Boolean): BaseBindingAdapter<ItemMultiOpponentBinding, UserBean.QbUserInfoX>() {
    override fun convert(
        holder: VBViewHolder<ItemMultiOpponentBinding>,
        item: UserBean.QbUserInfoX
    ) {
        val screenWith = DisplayUtils.getScreenWidth(context)
        holder.vb.apply {
            println("MultiVideo position = ${getItemPosition(item)} videoTrack = ${item.videoTrack} , uid = ${item.uid}, qbId = ${item.qbId}")
            if (isVideo) {
                if (item.videoTrack != null) {
                    val param = videoView.layoutParams as FrameLayout.LayoutParams
                    param.width = screenWith / 2
                    param.height = screenWith / 2
                    videoView.layoutParams = param
                    videoView.setZOrderMediaOverlay(true)
                    fillVideoView(videoView, item.videoTrack, item.uid != ApplicationConst.getUserId())

                    imgAvatar.visibility = View.GONE
                    viewProgress.visibility = View.GONE
                } else {
                    viewProgress.visibility = View.VISIBLE
                    imgAvatar.visibility = View.VISIBLE
                }
            } else {
                viewProgress.visibility = if (item.isConnected) View.GONE else View.VISIBLE
            }

            ImgLoader.display(context, item.avatar, imgAvatar)
        }
    }

    private fun fillVideoView(videoView: QBRTCSurfaceView?, videoTrack: QBRTCVideoTrack?, remoteRenderer: Boolean) {
        if (videoTrack == null) return
        videoTrack.removeRenderer(videoTrack.renderer)
        videoTrack.addRenderer(videoView)
        if (!remoteRenderer) {
            updateVideoView(videoView, true)
        } else {
            updateVideoView(videoView, false)
        }
        Log.d("MultiVideo", "MultiCallAdapter-fillVideoView " + (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun updateVideoView(videoView: SurfaceViewRenderer?, mirror: Boolean) {
        val scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL
//        Log.i("MultiVideo", "MultiCallAdapter-updateVideoView mirror:$mirror, scalingType = $scalingType")
        videoView?.setScalingType(scalingType)
        videoView?.setMirror(mirror)
        videoView?.requestLayout()
    }

}