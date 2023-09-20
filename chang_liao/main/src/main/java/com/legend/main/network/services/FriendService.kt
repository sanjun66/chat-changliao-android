package com.legend.main.network.services

import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.UserBean
import retrofit2.http.*

interface FriendService {
    /**
     * 查找好友
     */
    @FormUrlEncoded
    @POST("/api/searchFriends")
    suspend fun searchFriends(@FieldMap params: Map<String, String>): ApiResponse<UserBean.SearchFriendRes>

    /**
     *  好友申请
     */
    @FormUrlEncoded
    @POST("/api/friendsApply")
    suspend fun applyFriend(@FieldMap params: Map<String, String>): ApiResponse<Any?>

    /**
     * 好友申请列表
     */
    // Form-encoded method must contain at least one @Field.
//    @FormUrlEncoded
    @POST("/api/applyList")
    suspend fun friendApplyList(): ApiResponse<UserBean.ApplyList>

    /**
     * 好友申请审核
     */
    @FormUrlEncoded
    @POST("/api/checkApply")
    suspend fun checkApply(@FieldMap params: Map<String, String>): ApiResponse<Any?>

    /**
     * 获取好友列表
     */
    @POST("/api/friends")
    suspend fun getFriends(): ApiResponse<UserBean.FriendListBean?>

    /**
     * 删除好友
     */
    @FormUrlEncoded
    @POST("/api/friendsDel")
    suspend fun deleteFriend(@Field("friend_id") friendId: String): ApiResponse<Any?>


}