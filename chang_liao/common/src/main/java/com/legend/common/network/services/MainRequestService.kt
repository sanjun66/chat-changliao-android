package com.legend.common.network.services

import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.OssInfo
import com.legend.common.bean.UploadFileBean
import okhttp3.MultipartBody
import retrofit2.http.*

interface MainRequestService {
    /**
     * 上报音视频通话状态
     */
    @FormUrlEncoded
    @POST("/api/talkState")
    suspend fun reportTalkState(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    /**
     * 获取存储配置
     */
    @POST("/api/getOssInfo")
    suspend fun getOssInfo(): ApiResponse<OssInfo?>

    /**
     * 本地上传
     */
    @Multipart
    @POST("/api/localUpload")
    suspend fun localUpload(@Part image: MultipartBody.Part): ApiResponse<UploadFileBean>

    /**
     * 设置消息已读
     */
    @FormUrlEncoded
    @POST("/api/setMessageRead")
    suspend fun reportReadMsg(@FieldMap params: HashMap<String, String>): ApiResponse<Any>

    @FormUrlEncoded
    @POST("/api/scanGroup")
    suspend fun scanJoinGroup(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    /**
     * 删除消息
     */
    @FormUrlEncoded
    @POST("/api/delMsg")
    suspend fun deleteMsgReport(@Field("id") msgId: String): ApiResponse<Any?>

}