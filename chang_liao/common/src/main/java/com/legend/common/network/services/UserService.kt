package com.legend.common.network.services

import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.UserBean
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserService {

    /**
     * 获取用户信息
     */
    @FormUrlEncoded
    @POST("/api/userInfo")
    suspend fun getUserInfo(@Field("id") id: String?): ApiResponse<UserBean.UserInfo>

    /**
     * 修改用户详情
     */
    @FormUrlEncoded
    @PUT("/api/userInfo")
    suspend fun changeUserInfo(@FieldMap bodyParams: Map<String, String>): ApiResponse<Any?>

    /**
     * 修改好友备注
     */
    @FormUrlEncoded
    @POST("/api/friendNotes")
    suspend fun changNoteName(@FieldMap param: Map<String, String>): ApiResponse<Any?>

    /**
     * 获取好友黑名单
     */
    @POST("/api/friendBlackList")
    suspend fun getBlackFriends(): ApiResponse<UserBean.FriendBlackList?>

    /**
     * 添加好友黑名单
     */
    @FormUrlEncoded
    @POST("/api/friendBlack")
    suspend fun addFriendBlack(@Field("friend_id") friendId: String): ApiResponse<Any?>

    /**
     * 设置消息免打扰
     */
    @FormUrlEncoded
    @POST("/api/friendDisturb")
    suspend fun setFriendDisturb(@FieldMap params: Map<String, String>): ApiResponse<Any?>

    /**
     * 解除好友黑名单
     */
    @FormUrlEncoded
    @PUT("/api/friendBlack")
    suspend fun removeFriendBlack(@Field("friend_id") friendId: String): ApiResponse<Any?>

    /**
     * 修改用户头像
     */
    @FormUrlEncoded
    @POST("/api/modifyAvatarNew")
    suspend fun modifyAvatar(@FieldMap param: Map<String, String>): ApiResponse<UserBean.AvatarRes?>

    /**
     * 获取群聊详情
     */
    @FormUrlEncoded
    @POST("/api/groupInfo")
    suspend fun getGroupInfo(@Field("id") groupId: String): ApiResponse<UserBean.GroupAllInfo?>

    /**
     * 获取用户是否在线接口
     */
    @FormUrlEncoded
    @POST("/api/getUserOnline")
    suspend fun getUserOnlineState(@Field("id") uid: String): ApiResponse<UserBean.OnlineState?>

}