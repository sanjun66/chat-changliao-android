package com.legend.common.upload

import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.coroutine.mainRequest
import com.legend.common.network.services.MainRequestService
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class LocalUploadImpl(private val ossState: String): UploadStrategy {
    private var task: Job? = null
    private val services: MainRequestService by lazy {
        NetworkManager.getInstance().getService(MainRequestService::class.java)
    }

    override fun upload(file: File, callback: UploadCallback?) {
        val requestFile: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        task = mainRequest({services.localUpload(body)}, {
            callback?.onProgress(100, it.file_name, ossState)
            task = null
        }, {
            callback?.onProgress(-1, ossState = ossState)
            task = null
        })

    }

    override fun cancelUpload() {
        task?.cancel(null)
    }
}