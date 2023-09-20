package com.legend.common.utils.picture

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.utils.ActivityCompatHelper

object GlideImageEngine: ImageEngine {
    override fun loadImage(context: Context?, url: String?, imageView: ImageView?) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        if (context == null || imageView == null) return
        Glide.with(context).load(url).into(imageView)
    }

    override fun loadImage(
        context: Context?,
        imageView: ImageView?,
        url: String?,
        maxWidth: Int,
        maxHeight: Int
    ) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        if (context == null || imageView == null) return
        Glide.with(context).load(url).override(maxWidth, maxHeight).into(imageView)
    }

    /**
     * 加载相册目录封面
     */
    override fun loadAlbumCover(context: Context?, url: String?, imageView: ImageView?) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        if (context == null || imageView == null) return
        Glide.with(context).asBitmap().load(url).override(180, 180)
            .sizeMultiplier(0.5f).transform(CenterCrop(), RoundedCorners(8))
            .placeholder(com.luck.picture.lib.R.drawable.ps_image_placeholder).into(imageView)
    }

    /**
     * 加载图片列表图片
     */
    override fun loadGridImage(context: Context?, url: String?, imageView: ImageView?) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        if (context == null || imageView == null) return
        Glide.with(context).load(url)
            .override(200, 200)
            .placeholder(com.luck.picture.lib.R.drawable.ps_image_placeholder)
            .into(imageView)
    }

    override fun pauseRequests(context: Context?) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        context?.let {
            Glide.with(it).pauseRequests()
        }
    }

    override fun resumeRequests(context: Context?) {
        if (!ActivityCompatHelper.assertValidRequest(context)) return
        context?.let {
            Glide.with(context).resumeRequests()
        }
    }
}