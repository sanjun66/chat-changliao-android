package com.legend.common.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.TypeConst
import com.legend.common.bean.FileMsg
import com.legend.common.bean.UiMessage
import com.legend.common.utils.picture.PictureSelectorUtil
import com.legend.commonres.R
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia

object OpenFileUtil {
    fun openFile(context: Activity, uiMessage: UiMessage) {
        val fileMsg = uiMessage.message as FileMsg
        val localPath = fileMsg.extra.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_FILE)
        if (PictureMimeType.isUrlHasImage(localPath) || PictureMimeType.isUrlHasVideo(localPath)) {
            val mediaList = arrayListOf<LocalMedia>()
            if (FileUtils.hasFile(localPath)) {
                val media: LocalMedia = LocalMedia.generateLocalMedia(context, localPath)
                mediaList.add(media)
            } else {
                ImFileDownloadUtil.downloadWithNoProgress(fileMsg.extra.url, localPath, null)
                val media: LocalMedia = LocalMedia.generateHttpAsLocalMedia(fileMsg.extra.url)
                mediaList.add(media)
            }
            PictureSelectorUtil.picturePreview(context, 0, mediaList)
            return
        }
        if (FileUtils.hasFile(localPath)) {
            openFile(context, localPath)
        } else {
            ImFileDownloadUtil.downloadWithProgress(fileMsg.extra.url, localPath, object : ImFileDownloadUtil.DownloadListener {
                override fun onStart() {
                }

                override fun onProgress(progress: Int) {

                }

                override fun onCompleted() {
                    openFile(context, fileMsg.extra.getLocalPath(uiMessage.message.session_id, FileUtils.FILE_TYPE_FILE))
                }

                override fun onFail() {
                    ToastUtils.show(context.getString(R.string.rc_ac_file_preview_download_error))
                }

            })
        }
    }

    private fun openFile(context: Context, filePath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val type = FileTypeUtils.getIntentType(context, intent, FileUtils.getFileName(filePath))
        if (TextUtils.isEmpty(type)) {
            ToastUtils.show(context.getString(R.string.rc_ac_file_preview_can_not_open_file))
            return
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val path = FileUtils.getUriFromPath(context, filePath)
        intent.setDataAndType(path, type)
//        context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_style)))
        context.startActivity(intent)
    }


    fun openPicOrVideo(context: Activity, uiMessage: UiMessage, dataList: List<UiMessage>?) {
        if (dataList.isNullOrEmpty()) return
        val tempData = mutableListOf<UiMessage>()
        for (data in dataList) {
            if (data.message.message_type == TypeConst.chat_msg_type_file && data.message.is_revoke == TypeConst.type_no
                && (data.message.message_local_type == TypeConst.chat_msg_type_file_pic || data.message.message_local_type == TypeConst.chat_msg_type_file_video)) {
                tempData.add(data)
            }
        }
        if (tempData.isEmpty()) return
        val mediaList = arrayListOf<LocalMedia>()
        var position = 0
        for ((index, data) in tempData.withIndex()) {
            if (data.message.id == uiMessage.message.id) {
                position = index
            }

            val fileMsg: FileMsg = data.message as FileMsg
            if (data.message.message_local_type == TypeConst.chat_msg_type_file_pic) {
                val localPath = fileMsg.extra.getLocalPath(data.message.session_id, FileUtils.FILE_TYPE_IMAGE)
                if (FileUtils.hasFile(localPath)) {
                    val media: LocalMedia = LocalMedia.generateLocalMedia(context, localPath)
                    mediaList.add(media)
                } else {
                    ImFileDownloadUtil.downloadWithNoProgress(fileMsg.extra.url, localPath, null)
                    val media: LocalMedia = LocalMedia.generateHttpAsLocalMedia(fileMsg.extra.url)
                    mediaList.add(media)
                }
            } else {
                val videoLocalPath = fileMsg.extra.getLocalPath(data.message.session_id, FileUtils.FILE_TYPE_VIDEO)
                val imgLocalPath = fileMsg.extra.getLocalCover(data.message.session_id)
                if (FileUtils.hasFile(videoLocalPath)) {
                    val media: LocalMedia = LocalMedia.generateLocalMedia(context, videoLocalPath)
                    mediaList.add(media)
                } else {
                    if (TextUtils.isEmpty(fileMsg.extra.url)) {
                        // 本地
                        val videoOriginalPath = fileMsg.extra.path
                        if (FileUtils.hasFile(videoOriginalPath)) {
                            val media: LocalMedia = LocalMedia.generateLocalMedia(context, videoOriginalPath)
                            mediaList.add(media)
                        }
                    } else {
                        ImFileDownloadUtil.downloadWithNoProgress(fileMsg.extra.url, videoLocalPath, null)
                        val media: LocalMedia = LocalMedia.generateHttpAsLocalMedia(fileMsg.extra.url)
                        mediaList.add(media)
                    }
                }
                if (!FileUtils.hasFile(fileMsg.extra.thumbnailPath) && !FileUtils.hasFile(imgLocalPath)) {
                    ImFileDownloadUtil.downloadWithNoProgress(fileMsg.extra.cover, imgLocalPath, null)
                }
            }
        }

        PictureSelectorUtil.picturePreview(context, position, mediaList)
    }
}