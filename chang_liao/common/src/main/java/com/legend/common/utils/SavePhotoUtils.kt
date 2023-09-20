package com.legend.common.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider

import com.bumptech.glide.request.target.SimpleTarget

import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.legend.base.Applications
import java.io.*
import java.lang.Exception


/**

 * @description 将图片保存到本地

 */
object SavePhotoUtils {

    fun loadImage(url: String,saveBitmapCallBack: SaveBitmapCallBack? = null,isToAlbum:Boolean = false){
        Glide.with(Applications.getCurrent()).asBitmap().load(url).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val destFile = createSaveFile(
                    Applications.getCurrent(),
                    false,
                    "${System.currentTimeMillis()}.jpg",
                    "sss"
                )
                saveBitmap2SelfDirectory(
                    Applications.getCurrent(),
                    resource,
                    destFile,
                    saveBitmapCallBack,
                    isToAlbum
                )
            }
        })
    }

    /**
     * 创建需要保存的文件
     * @param isUseExternalFilesDir 是否使用getExternalFilesDir,false为保存在sdcard根目录下
     * @param fileName 保存文件名
     * @param folderName 保存在sdcard根目录下的文件夹名（isUseExternalFilesDir=false时需要）
     */
    fun createSaveFile(
        context: Context,
        isUseExternalFilesDir: Boolean,
        fileName: String,
        folderName: String? = "sss"
    ): File {
        val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath!!
        } else {
            if (isUseExternalFilesDir) {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath!!
            } else {
                Environment.getExternalStorageDirectory().absolutePath
            }
        }
        return if (isUseExternalFilesDir) {
            File(filePath, fileName)
        } else {
            val file = File(filePath, folderName!!)
            if (!file.exists()) {
                file.mkdirs()
            }
            File(file, fileName)
        }
    }

    //保存Bitmap至本地
    fun saveBitmap2SelfDirectory(
        context: Context,
        bitmap: Bitmap,
        file: File,
        saveBitmapCallBack: SaveBitmapCallBack?,
        isToAlbum: Boolean
    ) {
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            saveBitmapCallBack?.fail()
            e.printStackTrace()
        } finally {
            saveBitmapCallBack?.saveSuccess(file.absolutePath)
            //通知系统图库更新
            if (isToAlbum){
                refreshSystemPic(context, file)
            }
        }

    }

    /**
     * 通知系统相册更新
     */
    fun refreshSystemPic(context: Context, destFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            insertPicInAndroidQ(context, destFile)
        } else {
            val value = ContentValues()
            value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            value.put(MediaStore.Images.Media.DATA, destFile.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
            val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    context.packageName,
                    destFile
                )
            } else {
                Uri.fromFile(File(destFile.path))
            }
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    contentUri
                )
            )
        }
    }


    /**
     * Android Q以后向系统相册插入图片
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun insertPicInAndroidQ(context: Context, insertFile: File) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DESCRIPTION, insertFile.name)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, insertFile.name)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.TITLE, "Image.jpg")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")

        val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val resolver: ContentResolver = context.contentResolver
        val insertUri = resolver.insert(external, values)
        var inputStream: BufferedInputStream?
        var os: OutputStream? = null
        try {
            inputStream = BufferedInputStream(FileInputStream(insertFile))
            if (insertUri != null) {
                os = resolver.openOutputStream(insertUri)
            }
            if (os != null) {
                val buffer = ByteArray(1024 * 4)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    os.write(buffer, 0, len)
                }
                os.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            os?.close()
        }
    }


    /**
     * 图片保存到本地的回调
     *
     */
    interface SaveBitmapCallBack{
        fun saveSuccess(path: String)
        fun fail()
    }


}
