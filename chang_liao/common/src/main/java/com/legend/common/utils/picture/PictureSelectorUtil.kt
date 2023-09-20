package com.legend.common.utils.picture

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hw.videoprocessor.VideoProcessor
import com.legend.base.utils.FileUtils
import com.legend.base.utils.StringUtils
import com.legend.common.TypeConst
import com.legend.common.network.viewmodel.MainRequest
import com.legend.common.utils.QrIdentifyUtil
import com.legend.common.widget.DialogUitl
import com.legend.commonres.R
import com.luck.lib.camerax.SimpleCameraX
import com.luck.picture.lib.basic.PictureMediaScannerConnection
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.dialog.PictureLoadingDialog
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.*
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.style.TitleBarStyle
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.DownloadFileUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.luck.picture.lib.utils.ToastUtils
import com.yxing.ScanCodeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PictureSelectorUtil {
    private const val TAG = "file file"
    private const val maxSelectNum = 9
    private const val minSelectNum = 1

    fun openGalleryPic(context: Activity, sessionId: String, requestCode: Int) {
        PictureSelector.create(context)
            .openGallery(SelectMimeType.ofImage())  // 全部 .ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .setSelectorUIStyle(getPictureUiStyle(context))    // 设置主题
            .setImageEngine(GlideImageEngine)
            .setMaxSelectNum(maxSelectNum)     // 最大图片选择数量
            .setMinSelectNum(minSelectNum)
//            .setSelectionMode(if (maxSelectNum > minSelectNum) SelectModeConfig.MULTIPLE else SelectModeConfig.SINGLE)  // 多选 or 单选
            .setImageSpanCount(3)   // 每行显示个数
//            .setSelectionMode(SelectModeConfig.SINGLE)  // 单选
//            .isDirectReturnSingle(true)   // 直接返回
            .isPreviewImage(true)   // 是否可预览图片
            .isPreviewAudio(true)   // 是否可预览视频
            .isDisplayCamera(true)  // 是否显示拍照按钮
            .isSelectZoomAnim(true) // 图片列表点击 缩放效果 默认true
            .setVideoThumbnailListener(MeOnVideoThumbnailEventListener(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_VIDEO)))              //视频缩略图
            .setCameraImageFormat(PictureMimeType.PNG)  // 拍照保存图片格式后缀,默认jpeg
            .setOutputCameraImageFileName("/CustomImage")   // 自定义拍照保存路径
            .setOutputCameraVideoFileName("/CustomVideo")
//            .isOriginalSkipCompress()     // 原图是否跳过压缩
            .setCompressEngine(CompressFileEngine { context, source, call ->
//                val picUriList = arrayListOf<Uri>()
//                val videoUriList = arrayListOf<Uri>()
//                for (uri in source) {
//                    val name = DocumentFile.fromSingleUri(context, source[0])?.name
//                    if (PictureMimeType.isUrlHasImage(name)) {
//                        picUriList.add(uri)
//                    } else if (PictureMimeType.isUrlHasVideo(name)) {
//                        videoUriList.add(uri)
//                    }
//                }
//
                Luban.with(context).load(source).ignoreBy(100)
                    .setTargetDir(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_IMAGE))
                    .setCompressListener(object: OnNewCompressListener {
                        override fun onStart() {
                            // 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        override fun onSuccess(source: String?, compressFile: File?) {
                            if (compressFile == null) {
                                call?.onCallback(source, null)
                                return
                            }
                            val targetFile = File(FileUtils.getFilePathExpectName(compressFile.absolutePath) + "/" + FileUtils.getAndroidFileName(FileUtils.getFileSuffix(compressFile.absolutePath)))
                            compressFile.renameTo(targetFile)
                            call?.onCallback(source, targetFile.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }

                    }).launch()
            })
            .forResult(requestCode)
    }

    fun openGalleryPicSingle(context: Activity, sessionId: String, requestCode: Int) {
        PictureSelector.create(context)
            .openGallery(SelectMimeType.ofImage())  // 全部 .ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .setSelectorUIStyle(getPictureUiStyle(context))    // 设置主题
            .setImageEngine(GlideImageEngine)
            .setSelectionMode(SelectModeConfig.SINGLE)  // 多选 or 单选
            .setImageSpanCount(3)   // 每行显示个数
            .isDirectReturnSingle(true)   // 直接返回
            .isPreviewImage(true)   // 是否可预览图片
            .isDisplayCamera(false)  // 是否显示拍照按钮
            .isSelectZoomAnim(true) // 图片列表点击 缩放效果 默认true
            .setCameraImageFormat(PictureMimeType.PNG)  // 拍照保存图片格式后缀,默认jpeg
            .setOutputCameraImageFileName("/CustomImage")   // 自定义拍照保存路径
            .setOutputCameraVideoFileName("/CustomVideo")
//            .isOriginalSkipCompress()     // 原图是否跳过压缩
            .setCompressEngine(CompressFileEngine { context, source, call ->
                Luban.with(context).load(source).ignoreBy(100)
                    .setTargetDir(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_IMAGE))
                    .setCompressListener(object: OnNewCompressListener {
                        override fun onStart() {
                            // 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        override fun onSuccess(source: String?, compressFile: File?) {
                            if (compressFile == null) {
                                call?.onCallback(source, null)
                                return
                            }
                            val targetFile = File(FileUtils.getFilePathExpectName(compressFile.absolutePath) + "/" + FileUtils.getAndroidFileName(FileUtils.getFileSuffix(compressFile.absolutePath)))
                            compressFile.renameTo(targetFile)
                            call?.onCallback(source, targetFile.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }

                    }).launch()
            })
            .forResult(requestCode)
    }

    fun openGalleryWithCrop(context: Activity, requestCode: Int) {
        PictureSelector.create(context)
            .openGallery(SelectMimeType.ofImage())  // 全部 .ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .setSelectorUIStyle(getPictureUiStyle(context))    // 设置主题
            .setImageEngine(GlideImageEngine)
            .setImageSpanCount(3)   // 每行显示个数
            .setSelectionMode(SelectModeConfig.SINGLE)  // 单选
            .isPreviewImage(true)   // 是否可预览图片
            .isDisplayCamera(true)  // 是否显示拍照按钮
            .isSelectZoomAnim(true) // 图片列表点击 缩放效果 默认true
            .setCameraImageFormat(PictureMimeType.PNG)  // 拍照保存图片格式后缀,默认jpeg
            .setOutputCameraImageFileName("/CustomImage")   // 自定义拍照保存路径
            .setCropEngine(ImageFileCropEngine())       // 自定义裁剪
            .setCompressEngine(CompressFileEngine { context, source, call ->
                Luban.with(context).load(source).ignoreBy(100)
                    .setTargetDir(FileUtils.getAppFiles(context, FileUtils.FILE_TYPE_IMAGE))
                    .setCompressListener(object: OnNewCompressListener {
                        override fun onStart() {
                            // 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        override fun onSuccess(source: String?, compressFile: File?) {
                            call?.onCallback(source, compressFile?.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }

                    }).launch()
            })
            .forResult(requestCode)
    }

    fun openGalleryVideo(context: Activity, sessionId: String, requestCode: Int) {
        PictureSelector.create(context)
            .openGallery(SelectMimeType.ofVideo())  // 全部 .ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .setSelectorUIStyle(getPictureUiStyle(context))    // 设置主题
            .setImageEngine(GlideImageEngine)
            .setMaxSelectNum(maxSelectNum)     // 最大图片选择数量
            .setMinSelectNum(minSelectNum)
//            .setSelectionMode(if (maxSelectNum > minSelectNum) SelectModeConfig.MULTIPLE else SelectModeConfig.SINGLE)  // 多选 or 单选
            .setImageSpanCount(3)   // 每行显示个数
//            .setSelectionMode(SelectModeConfig.SINGLE)  // 单选
//            .isDirectReturnSingle(true)   // 直接返回
            .isPreviewImage(true)   // 是否可预览图片
            .isPreviewAudio(true)   // 是否可预览视频
            .isDisplayCamera(true)  // 是否显示拍照按钮
            .isSelectZoomAnim(true) // 图片列表点击 缩放效果 默认true
            .setVideoThumbnailListener(MeOnVideoThumbnailEventListener(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_VIDEO)))              //视频缩略图
            .setCameraImageFormat(PictureMimeType.PNG)  // 拍照保存图片格式后缀,默认jpeg
            .setOutputCameraImageFileName("/CustomImage")   // 自定义拍照保存路径
            .setOutputCameraVideoFileName("/CustomVideo")
//            .isOriginalSkipCompress()     // 原图是否跳过压缩
            .setCompressEngine(CompressFileEngine { context, source, call ->
                Luban.with(context).load(source).ignoreBy(100)
                    .setTargetDir(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_IMAGE))
                    .setCompressListener(object: OnNewCompressListener {
                        override fun onStart() {
                            // 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        override fun onSuccess(source: String?, compressFile: File?) {
                            call?.onCallback(source, compressFile?.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }

                    }).launch()
            })
            .forResult(requestCode)
    }


    fun openCamera(context: Activity, sessionId: String, requestCode: Int) {
        PictureSelector.create(context)
            .openCamera(SelectMimeType.ofVideo())
            .setCameraInterceptListener(MeOnCameraInterceptListener)                   // 自定义camera
            .setVideoThumbnailListener(MeOnVideoThumbnailEventListener(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_VIDEO)))              //视频缩略图
            .setCameraVideoFormat(PictureMimeType.MP4)
            .setCameraImageFormat(PictureMimeType.PNG)      // 拍照保存图片格式后缀,默认jpeg
            .setOutputCameraImageFileName("/CustomImage")   // 自定义拍照保存路径
            .setOutputCameraVideoFileName("/CustomVideo")
            .setCompressEngine(CompressFileEngine { context, source, call ->
                if (source.isNullOrEmpty()) return@CompressFileEngine
                Luban.with(context).load(source).ignoreBy(100)
                    .setTargetDir(FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_IMAGE))
                    .setCompressListener(object: OnNewCompressListener {
                        override fun onStart() {
                            // 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        override fun onSuccess(source: String?, compressFile: File?) {
                            call?.onCallback(source, compressFile?.absolutePath)
                        }

                        override fun onError(source: String?, e: Throwable?) {
                            call?.onCallback(source, null)
                        }

                    }).launch()
            })
            .forResultActivity(requestCode)
    }

    private val MeOnCameraInterceptListener =
        OnCameraInterceptListener { fragment, cameraMode, requestCode ->
            val camera = SimpleCameraX.of();
            camera.isAutoRotation(true)
            camera.setCameraMode(cameraMode)
            camera.setVideoFrameRate(25)
            camera.setVideoBitRate(3 * 1024 * 1024)
            camera.isDisplayRecordChangeTime(true)
            camera.isManualFocusCameraPreview(true)         // 支持手指对焦
            camera.isZoomCameraPreview(true)       // 支持手指缩放相机
//            camera.setOutputPathDir()                    // 自定义相机路径
//            camera.setPermissionDeniedListener(getSimpleXPermissionDeniedListener())  // 添加权限说明
//            camera.setPermissionDescriptionListener(getSimpleXPermissionDescriptionListener())  // // 添加权限说明
            camera.setImageEngine { context, url, imageView ->
                if (context != null && imageView != null) Glide.with(
                    context
                ).load(url).into(imageView)
            }
            camera.start(fragment.requireActivity(), fragment, requestCode)
        }



    private fun getPictureUiStyle(context: Activity): PictureSelectorStyle {
        val selectorStyle = PictureSelectorStyle()
        // 主体风格
        val numberSelectMainStyle = SelectMainStyle()
        numberSelectMainStyle.isSelectNumberStyle = true
        numberSelectMainStyle.isPreviewSelectNumberStyle = false
        numberSelectMainStyle.isPreviewDisplaySelectGallery = true
        numberSelectMainStyle.selectBackground = com.luck.picture.lib.R.drawable.ps_default_num_selector
        numberSelectMainStyle.previewSelectBackground = com.luck.picture.lib.R.drawable.ps_preview_checkbox_selector
        numberSelectMainStyle.selectNormalBackgroundResources = com.luck.picture.lib.R.drawable.ps_select_complete_normal_bg
        numberSelectMainStyle.selectNormalTextColor = ContextCompat.getColor(context, com.luck.picture.lib.R.color.ps_color_53575e)
        numberSelectMainStyle.setSelectNormalText(R.string.chat_send)
        numberSelectMainStyle.adapterPreviewGalleryBackgroundResource = com.luck.picture.lib.R.drawable.ps_preview_gallery_bg
        numberSelectMainStyle.adapterPreviewGalleryItemSize = DensityUtil.dip2px(context, 52f)
        numberSelectMainStyle.setPreviewSelectText(R.string.chat_choose)
        numberSelectMainStyle.previewSelectTextSize = 14
        numberSelectMainStyle.previewSelectTextColor = ContextCompat.getColor(context, com.com.legend.ui.R.color.ui_white)
        numberSelectMainStyle.previewSelectMarginRight = DensityUtil.dip2px(context, 6f)
        numberSelectMainStyle.selectBackgroundResources = com.luck.picture.lib.R.drawable.ps_select_complete_bg
//        numberSelectMainStyle.setSelectText(R.string.ps_send_num)
        numberSelectMainStyle.selectTextColor = ContextCompat.getColor(context, com.com.legend.ui.R.color.white)
        numberSelectMainStyle.mainListBackgroundColor = ContextCompat.getColor(context, com.com.legend.ui.R.color.ui_black)
        numberSelectMainStyle.isCompleteSelectRelativeTop = true
        numberSelectMainStyle.isPreviewSelectRelativeBottom = true
        numberSelectMainStyle.isAdapterItemIncludeEdge = false

        // 头部TitleBar 风格

        // 头部TitleBar 风格
        val numberTitleBarStyle = TitleBarStyle()
        numberTitleBarStyle.isHideCancelButton = true
        numberTitleBarStyle.isAlbumTitleRelativeLeft = true
        numberTitleBarStyle.titleAlbumBackgroundResource = com.luck.picture.lib.R.drawable.ps_album_bg
        numberTitleBarStyle.titleDrawableRightResource = com.luck.picture.lib.R.drawable.ps_ic_grey_arrow
        numberTitleBarStyle.previewTitleLeftBackResource = com.luck.picture.lib.R.drawable.ps_ic_normal_back

        // 底部NavBar 风格

        // 底部NavBar 风格
        val numberBottomNavBarStyle = BottomNavBarStyle()
        numberBottomNavBarStyle.bottomPreviewNarBarBackgroundColor = ContextCompat.getColor(context, com.luck.picture.lib.R.color.ps_color_half_grey)
        numberBottomNavBarStyle.setBottomPreviewNormalText(com.luck.picture.lib.R.string.ps_preview)
        numberBottomNavBarStyle.bottomPreviewNormalTextColor = ContextCompat.getColor(context, com.luck.picture.lib.R.color.ps_color_9b)
        numberBottomNavBarStyle.bottomPreviewNormalTextSize = 16
        numberBottomNavBarStyle.isCompleteCountTips = false
        numberBottomNavBarStyle.setBottomPreviewSelectText(com.luck.picture.lib.R.string.ps_preview_num)
        numberBottomNavBarStyle.bottomPreviewSelectTextColor = ContextCompat.getColor(context, com.com.legend.ui.R.color.ui_white)


        selectorStyle.titleBarStyle = numberTitleBarStyle
        selectorStyle.bottomBarStyle = numberBottomNavBarStyle
        selectorStyle.selectMainStyle = numberSelectMainStyle

        return selectorStyle
    }

    fun picturePreview(activity: Activity, currentPosition: Int, listData: ArrayList<LocalMedia>) {
        PictureSelector.create(activity)
            .openPreview()
            .setImageEngine(GlideImageEngine)
            .setVideoPlayerEngine(null)
            .isVideoPauseResumePlay(true)
            .setExternalPreviewEventListener(object : OnExternalPreviewEventListener {
                override fun onPreviewDelete(position: Int) {
                    // 删除
                }

                override fun onLongPressDownload(context: Context?, media: LocalMedia?): Boolean {
                    // 是否支持长按下载
                    media?.let {
                        var filePath = if (!TextUtils.isEmpty(media.compressPath)) media.compressPath else media.realPath
                        if (!FileUtils.hasFile(filePath) && !TextUtils.isEmpty(media.path)) filePath = FileUtils.getFileAbsolutePath(activity, Uri.parse(media.path))

                        Log.i("byy", "filePath1 === " + filePath + ", url = ${FileUtils.getUriFromPath(context, filePath)} , isExit = ${FileUtils.hasFile(filePath)}")
                        val code = ScanCodeConfig.scanningImage(activity,  FileUtils.getUriFromPath(activity, filePath))
                        Log.i("byy", "图片 -- code = $code, context = $context")
                        if (code == null) {
                            val sparseArray = SparseArray<String>()
                            sparseArray.put(0, StringUtils.getString(R.string.save_to_phone))
                            DialogUitl.showStringArrayDialogBottom(context, sparseArray) { text, tag ->
                                context?.let { saveMedia(media, it) }
                            }
                        } else {
                            val sparseArray = SparseArray<String>()
                            sparseArray.put(0, StringUtils.getString(R.string.save_to_phone))
                            sparseArray.put(1, StringUtils.getString(R.string.identify_qr_code))
                            DialogUitl.showStringArrayDialogBottom(context, sparseArray) { text, tag ->
                                if (tag == 0) {
                                    context?.let { saveMedia(media, it) }
                                } else {
                                    val qrBeanRes = QrIdentifyUtil.qrIdentify(code, false)
                                    qrBeanRes?.let {
                                        when (it.type) {
                                            TypeConst.qr_type_single -> {
                                                if (context != null && context is Activity) {
                                                    context.finish()
                                                }
                                            }
                                            TypeConst.qr_type_group -> {
                                                DialogUitl.showSimpleDialog(context, String.format(StringUtils.getString(com.legend.commonres.R.string.add_group_tips), it.groupName)
                                                ) { dialog, content ->
                                                    MainRequest.scanJoinGroup(it.groupId?:"", it.uid)
                                                    if (context != null && context is Activity) {
                                                        context.finish()
                                                    }
                                                }
                                            }
                                            else -> {
                                                // noting
                                            }
                                        }
                                    }

                                }
                            }


                        }
                    }

                    return true
                 }

            }).startActivityPreview(currentPosition, false, listData)
    }

    private fun saveMedia(media: LocalMedia, context: Context) {
        val path = media.availablePath
        var loading: PictureLoadingDialog? = null
        if (PictureMimeType.isHasHttp(path) && context is Activity && !ActivityCompatHelper.isDestroy(context)) {
            loading = PictureLoadingDialog(context)
            loading.show()
        }
        DownloadFileUtils.saveLocalFile(context, path, media.mimeType
        ) { realPath ->
            if (context is Activity && !ActivityCompatHelper.isDestroy(context)) {
                loading?.dismiss()
            }
            if (TextUtils.isEmpty(realPath)) {
                val errorMsg: String = if (PictureMimeType.isHasAudio(media.mimeType)) {
                    StringUtils.getString(com.luck.picture.lib.R.string.ps_save_audio_error)
                } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                    StringUtils.getString(com.luck.picture.lib.R.string.ps_save_video_error)
                } else {
                    StringUtils.getString(com.luck.picture.lib.R.string.ps_save_image_error)
                }
                ToastUtils.showToast(context, errorMsg)
            } else {
                PictureMediaScannerConnection(context, realPath)
                ToastUtils.showToast(
                    context,
                    StringUtils.getString(com.luck.picture.lib.R.string.ps_save_success) + "\n" + realPath
                )
            }
        }
    }








    suspend fun compressVideo(context: Context, sessionId: String, media: LocalMedia, listener: OnVideoCompressListener?) {
        withContext(Dispatchers.IO) {
            try {
                // 视频
                val srcPath = media.realPath
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(srcPath)
//                val originWith = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
//                val originHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt()
                val videoOutCompressPath = FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_VIDEO) + FileUtils.getAndroidFileName(FileUtils.getFileSuffix(srcPath))
                Log.i("websocket", "video compress srcPath = $srcPath ,outpath = $videoOutCompressPath"
                )
                var isComplete = false
                VideoProcessor.processor(context)
                    .input(srcPath)
                    .bitrate((bitrate?:1400) / 2)
                    .output(videoOutCompressPath)
                    .progressListener { progress ->
                        if (progress >= 1 && !isComplete) {
                            isComplete = true
                            Log.i("websocket", "video compress progress = $progress")
                            media.compressPath = videoOutCompressPath
                            LocalMedia.generateLocalMedia(context, videoOutCompressPath)
                            LocalMedia.generateLocalMedia(context, media.videoThumbnailPath)
                            listener?.compressRes(media)
                        }
                    }
                    .process()
            } catch (e: Exception) {
                Log.i("websocket", "video compress error = " + e.message)
            }
        }
    }

    suspend fun compressVideo(context: Context, sessionId: String, realPath: String, listener: OnVideoCompressListener1?) {
        withContext(Dispatchers.IO) {
            try {
                // 视频
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(realPath)
//                val originWith = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
//                val originHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt()
                val videoOutCompressPath = FileUtils.getAppFiles(context, sessionId, FileUtils.FILE_TYPE_VIDEO) + FileUtils.getAndroidFileName(FileUtils.getFileSuffix(realPath))
                Log.i("websocket", "video compress srcPath = $realPath ,outpath = $videoOutCompressPath")
                var isComplete = false
                VideoProcessor.processor(context)
                    .input(realPath)
                    .bitrate((bitrate?:1400) / 2)
                    .output(videoOutCompressPath)
                    .progressListener { progress ->
                        if (progress >= 1 && !isComplete) {
                            isComplete = true
                            Log.i("websocket", "video compress progress = $progress")
                            listener?.compressRes(videoOutCompressPath)
                        }
                    }
                    .process()
            } catch (e: Exception) {
                Log.i("websocket", "video compress error = " + e.message)
            }
        }
    }

}

class MeOnVideoThumbnailEventListener(private val targetPath: String): OnVideoThumbnailEventListener {

    override fun onVideoThumbnail(
        context: Context?,
        videoPath: String?,
        call: OnKeyValueResultCallbackListener?
    ) {
        Glide.with(context!!).asBitmap().sizeMultiplier(0.6f).load(videoPath)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val stream = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                    var fos: FileOutputStream? = null
                    var result: String? = null
                    try {
                        val targetFile = File(targetPath, "thumbnails_" + System.currentTimeMillis() + ".jpg")
                        fos = FileOutputStream(targetFile)
                        fos.write(stream.toByteArray())
                        fos.flush()
                        result = targetFile.absolutePath
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        PictureFileUtils.close(fos)
                        PictureFileUtils.close(stream)
                    }
                    call?.onCallback(videoPath, result)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    call?.onCallback(videoPath, "")
                }
            })
    }

}

interface OnVideoCompressListener {
    fun compressRes(media: LocalMedia)
}

interface OnVideoCompressListener1 {
    fun compressRes(videoOutCompressPath: String)
}

