package com.legend.main.network.services

import com.legend.common.bean.InternationAreaCodesItem
import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.AppVersion
import com.legend.common.bean.InternationAreaCode
import com.legend.common.bean.LoginRes
import com.legend.main.network.BaseData
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface LoginService {
    /**
     * 国际电话区号
     */
//    @GET("/appapi/public/index.php?service=Login.getCountrys")
//    suspend fun getInternalAreaCodes(@QueryMap map: Map<String, String?>?): ApiResponse<BaseData<List<InternationAreaCode>>>

    /**
     * 搜索 - 国际电话区号
     */
//    @GET("/appapi/public/index.php?service=Login.getCountrys")
//    suspend fun getInternalAllAreaCodes(): ApiResponse<BaseData<List<InternationAreaCodesItem>>>

    /**
     * 注册
     */
    @FormUrlEncoded
    @POST("/api/register")
    suspend fun register(@FieldMap map: Map<String, String>): ApiResponse<Any>

    /**
     * 登录
     */
    @FormUrlEncoded
    @POST("/api/login")
    suspend fun login(@FieldMap map: Map<String, String?>): ApiResponse<LoginRes?>
//    suspend fun login(@Field("params_body") param: String): ApiResponse<Any?>

    /**
     * 刷新token
     * 没有请求参数时不需要用 @FormUrlEncoded
     */
//    @FormUrlEncoded
    @POST("/api/refreshToken")
    suspend fun refreshToken(): ApiResponse<LoginRes?>

    /**
     * 发送验证码
     */
    @FormUrlEncoded
    @POST("/api/sms")
    suspend fun sendVerifyCode(@FieldMap map: Map<String, String>): ApiResponse<Any?>

    /**
     * 忘记密码
     */
    @FormUrlEncoded
    @POST("/api/forgetPassword")
    suspend fun forgetPwd(@FieldMap map: Map<String, String>): ApiResponse<Any?>

    @POST("/api/logout")
    suspend fun logout(): ApiResponse<Any?>

    /**
     * 版本信息
     */
    @FormUrlEncoded
    @POST("/api/getVersionInfo")
    suspend fun getVersionInfo(@Field("platform") param: String): ApiResponse<AppVersion?>
}