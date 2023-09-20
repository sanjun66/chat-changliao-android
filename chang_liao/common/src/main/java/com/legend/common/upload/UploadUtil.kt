package com.legend.common.upload

import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.coroutine.mainRequest
import com.legend.common.TypeConst
import com.legend.common.network.services.MainRequestService
import kotlinx.coroutines.Job

object UploadUtil {
    private val services: MainRequestService by lazy {
        NetworkManager.getInstance().getService(MainRequestService::class.java)
    }

    private var sStrategy: UploadStrategy? = null
    private var task: Job? = null

    fun startUpload(uploadStrategyCallback: UploadStrategyCallback?) {
        task = mainRequest({ services.getOssInfo()}, {
            task = null
            it?.let { it1 ->
                when(it1.oss_status.toInt()) {
                    TypeConst.oss_state_local -> {
                        sStrategy = LocalUploadImpl(it1.oss_status)
                        uploadStrategyCallback?.callback(sStrategy)
                    }
                    TypeConst.oss_state_aws -> {
                        val awsInfo = it1.aws
                        sStrategy = AWSUploadImpl(awsInfo.aws_default_region, awsInfo.aws_access_key_id, awsInfo.aws_secret_access_key, awsInfo.aws_bucket, it1.oss_status)
                        uploadStrategyCallback?.callback(sStrategy)
                    }
                }
            }
        }, {
            task = null
        })
    }

    fun cancelUpload() {
        task?.cancel(null)
        sStrategy?.cancelUpload()
    }
}
