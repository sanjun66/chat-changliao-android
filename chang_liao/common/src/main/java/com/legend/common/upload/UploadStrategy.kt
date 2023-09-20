package com.legend.common.upload

import java.io.File

interface UploadStrategy {
    fun upload(file: File, callback: UploadCallback?)

    fun cancelUpload()
}


interface UploadCallback {
    /**
     * @param progress -1:出错； -2：取消
     */
    fun onProgress(progress: Int, url: String? = "", ossState: String)
}
