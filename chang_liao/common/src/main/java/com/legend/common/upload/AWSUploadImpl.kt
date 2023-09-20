package com.legend.common.upload

import android.util.Log
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.legend.base.Applications
import com.legend.base.utils.FileUtils
import org.json.JSONObject
import java.io.File
import java.lang.Exception

class AWSUploadImpl(private val region: String, private val access_key_id: String, private val secret_key: String, private val bucket: String, private val ossState: String): UploadStrategy {
    private val TAG = "AWSUploadImpl"
    private var taskId: Int? = null

    private fun getConfiguration(): AWSConfiguration {
        // 设置region和bucket
        val jsonConfig = JSONObject()
        val defaultObj = JSONObject()
        val s3TransferUtility = JSONObject()
        s3TransferUtility.put("Region", region)
        s3TransferUtility.put("Bucket", bucket)
        defaultObj.putOpt("Default", s3TransferUtility)
        jsonConfig.putOpt("S3TransferUtility", defaultObj)
        return AWSConfiguration(jsonConfig)
    }

    private fun getS3Client(): AmazonS3Client {
        // 设置accessId和secretKey
        val credentials = object : AWSCredentials {
            override fun getAWSAccessKeyId() = access_key_id

            override fun getAWSSecretKey() = secret_key
        }

        return AmazonS3Client(credentials, Region.getRegion(region))
    }

    private fun getTransferUtility(): TransferUtility {
        // 构建TransferUtility
        return TransferUtility.builder()
            .context(Applications.getCurrent().applicationContext)
            .s3Client(getS3Client())
            .awsConfiguration(getConfiguration())
            .build()
    }

    override fun upload(file: File, callback: UploadCallback?) {
        val s3FileName = FileUtils.getOssFileName(file.path)
        val transferObserver = getTransferUtility().upload(s3FileName, file)
        var isComplete = false
        transferObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                taskId = id
                state?.let {
                    when (it) {
                        TransferState.COMPLETED -> {
                            Log.i(TAG, "aws $s3FileName 上传成功")
                            // 获取上传后的临时下载地址
//                            val urlRequest = GeneratePresignedUrlRequest(buckName, s3FileName)
//                            val url = getS3Client().generatePresignedUrl(urlRequest).toString()
//                            val url = getS3Client().getUrl(buckName, s3FileName).toString()
//                            Log.i(TAG, "aws 获取上传后的临时下载地址1: ${url}")
                            if (!isComplete) {
                                isComplete = true
                                taskId = null
                                callback?.onProgress(100, s3FileName, ossState)
                            } else {
                                // nothing
                            }
                        }
                        TransferState.FAILED -> {
                            Log.i(TAG, "aws 上传失败")
                            callback?.onProgress(-1, ossState = ossState)
                        }
                        TransferState.CANCELED -> {
                            Log.i(TAG, "aws 上传取消")
                            callback?.onProgress(-2, ossState = ossState)
                        }
                        else -> {
                            Log.i(TAG, "aws 上传other 状态: $state")
                        }
                    }
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val progress = bytesCurrent * 100 / bytesTotal
                Log.i(TAG, "aws 上传 bytesCurrent = $bytesCurrent, bytesTotal = $bytesTotal, progress = $progress" )
                if (progress < 100) callback?.onProgress(progress.toInt(), ossState = ossState)
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.i(TAG, "aws error ex = ${ex?.message}")
                callback?.onProgress(-1, ossState = ossState)
            }

        })
    }

    override fun cancelUpload() {
        taskId?.let { getTransferUtility().cancel(it) }
    }

}