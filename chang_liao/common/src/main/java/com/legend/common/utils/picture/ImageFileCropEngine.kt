package com.legend.common.utils.picture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.ImgLoader
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.engine.CropFileEngine
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine

/**
 * 自定义裁剪
 */
class ImageFileCropEngine: CropFileEngine {
    override fun onStartCrop(
        fragment: Fragment,
        srcUri: Uri,
        destinationUri: Uri,
        dataSource: java.util.ArrayList<String>?,
        requestCode: Int
    ) {
        val options = buildOptions(fragment)
        val uCrop: UCrop = UCrop.of(srcUri, destinationUri, dataSource)
        uCrop.withOptions(options)
        uCrop.setImageEngine(object : UCropImageEngine {
            override fun loadImage(context: Context, url: String, imageView: ImageView) {
                if (!ImgLoader.assertValidRequest(context)) return
                ImgLoader.display(context, url, imageView)
            }

            override fun loadImage(
                context: Context,
                url: Uri,
                maxWidth: Int,
                maxHeight: Int,
                call: UCropImageEngine.OnCallbackListener<Bitmap>?
            ) {
                Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        call?.let { it.onCall(resource) }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        call?.let { it.onCall(null) }
                    }

                })
            }
        })
        uCrop.start(fragment.requireActivity(), fragment, requestCode)
    }

    /**
     * 配置UCrop，可根据需求自我扩展
     */
    private fun buildOptions(fragment: Fragment): UCrop.Options {
        val options = UCrop.Options()
        options.setHideBottomControls(true)     // 是否隐藏裁剪拦
        options.setFreeStyleCropEnabled(false)  // 裁剪框or图片拖动
        options.setShowCropFrame(true)          // 是否显示裁剪边框
        options.setShowCropGrid(true)           // 是否显示裁剪框网格
        options.withAspectRatio(1f, 1f)
        options.setCropOutputPathDir(FileUtils.getAppRootPath(fragment.context))
        options.isCropDragSmoothToCenter(false)
        options.setSkipCropMimeType(PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()) // 跳过剪裁的类型
//        options.isForbidCropGifWebp(true)       // 禁止裁剪gif与跳过剪裁gif互斥
        options.isForbidSkipMultipleCrop(true)
        options.setMaxScaleMultiplier(100f)

        options.setStatusBarColor(ContextCompat.getColor(fragment.requireContext(), com.luck.picture.lib.R.color.ps_color_grey))
        options.setToolbarColor(ContextCompat.getColor(fragment.requireContext(), com.luck.picture.lib.R.color.ps_color_grey))
        options.setToolbarWidgetColor(ContextCompat.getColor(fragment.requireContext(), com.luck.picture.lib.R.color.ps_color_white))

        return options
    }


}