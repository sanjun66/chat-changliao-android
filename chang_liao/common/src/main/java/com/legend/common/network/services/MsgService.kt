package com.legend.common.network.services

import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.NetChatListBean
import com.legend.common.db.entity.ChatMessageModel
import com.legend.common.db.entity.SimpleMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface MsgService {
    /**
     * 发送消息
     */
    @POST("/api/msgSend")
    suspend fun sendMsg(@Body info:RequestBody): ApiResponse<ChatMessageModel<Any>?>

    @FormUrlEncoded
    @POST("/api/msgSend")
    suspend fun sendMsgEncrypt(@Field("params_body") encrypted: String): ApiResponse<ChatMessageModel<Any>?>

    /**
     * 上传文件
     *
     * @JvmSuppressWildcards 解决以下的错误
     * Parameter type must not include a type variable or wildcard: java.util.Map<java.lang.String, ? extends okhttp3.RequestBody> (parameter #1)
     */
    @Multipart
    @POST("/api/uploadFile")
    @JvmSuppressWildcards
    suspend fun uploadFile(@Part("is_secret") isSecret:Boolean,  @PartMap strParams: Map<String,  RequestBody>, @Part image: MultipartBody.Part): ApiResponse<ChatMessageModel<Any>?>


    // json实现 不用@FormUrlEncoded
//    @FormUrlEncoded
//    @POST("/api/msgSend")
//    suspend fun sendTextMsg(@Body info:RequestBody): ApiResponse<ChatMessageModel<Any>?>

    /**
     * 消息撤回
     */
//    @FormUrlEncoded
    @POST("/api/msgRevoke")
    suspend fun revokeMsg(@Body info: RequestBody): ApiResponse<SimpleMessage?>

    @FormUrlEncoded
    @POST("/api/msgRevoke")
    suspend fun revokeMsgEncrypt(@Field("params_body") encrypted: String): ApiResponse<SimpleMessage?>

    /**
     * 检查是否能视频聊天
     */
    @FormUrlEncoded
    @POST("/api/checkParams")
    suspend fun checkCall(@FieldMap params: HashMap<String, String>): ApiResponse<ChatMessageModel<Any?>>

    /**
     * 消息解密
     */
    @FormUrlEncoded
    @POST("/api/msgDecrypt")
    suspend fun msgDecrypt(@FieldMap params: HashMap<String, String>): ApiResponse<Any>

    /**
     * 获取历史会话列表
     */
    @POST("/api/getTalkList")
    suspend fun getNetTalkList(): ApiResponse<List<NetChatListBean>?>

    /**
     * 删除对话框
     */
    @FormUrlEncoded
    @POST("/api/delMeeting")
    suspend fun deleteChatList(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    /**
     * 获取历史聊天记录
     */
    @FormUrlEncoded
    @POST("/api/getChatMessage")
    suspend fun getNetChatMessage(@FieldMap params: HashMap<String, String>): ApiResponse<List<ChatMessageModel<Any>>>

    /**
     * 转发消息
     */
    @FormUrlEncoded
    @POST("/api/msgForward")
    suspend fun msgForward(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>
}