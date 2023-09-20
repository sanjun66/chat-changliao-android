package com.legend.common.network.services

import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.UserBean
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface GroupService {
    /**
     * 创建群聊
     */
    @FormUrlEncoded
    @POST("/api/createGroup")
    suspend fun createGroup(@Field("member_id") memberId: String): ApiResponse<Any?>

    /**
     * 解散群聊
     */
    @FormUrlEncoded
    @POST("/api/dismissGroup")
    suspend fun dissolveGroup(@Field("group_id") id: String): ApiResponse<Any?>

    /**
     * 主动退群
     */
    @FormUrlEncoded
    @POST("/api/exitGroup")
    suspend fun exitGroup(@Field("group_id") groupId: String): ApiResponse<Any?>

    /**
     * 修改群资料
     */
    @FormUrlEncoded
    @PUT("/api/groupInfo")
    suspend fun modifyGroupInfo(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    /**
     * 群设置免打扰
     */
    @FormUrlEncoded
    @POST("/api/groupDisturb")
    suspend fun setGroupDisturb(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    /**
     * 邀请加入群聊
     */
    @FormUrlEncoded
    @POST("/api/inviteGroup")
    suspend fun inviteToGroup(@Field("id") groupId: String, @Field("member_id") memberId: String): ApiResponse<Any?>

    /**
     * 踢出群聊
     */
    @FormUrlEncoded
    @POST("/api/kickOutGroup")
    suspend fun kickOutGroup(@Field("group_id") groupId: String, @Field("id") userId: String): ApiResponse<Any?>

    /**
     * 群聊列表
     */
    @POST("/api/groupList")
    suspend fun getGroupList(): ApiResponse<UserBean.GroupList?>

    /**
     * 群聊禁言
     */
    @FormUrlEncoded
    @POST("/api/muteMemberGroup")
    suspend fun groupMemberMute(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    /**
     * 设置群管理员
     */
    @FormUrlEncoded
    @POST("/api/groupManager")
    suspend fun opGroupManager(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>

    @FormUrlEncoded
    @POST("/api/scanGroup")
    suspend fun scanJoinGroup(@FieldMap params: HashMap<String, String>): ApiResponse<Any?>
}