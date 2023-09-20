package com.legend.main.network.services

import com.legend.basenet.network.bean.ApiResponse
import com.legend.common.bean.UserBean
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface WalletService {
    /**
     * 钱包余额
     */
    @POST("/api/walletBalance")
    suspend fun getWalletBalance(): ApiResponse<List<UserBean.WalletBalanceBean>>

    /**
     * USDT 充值接口
     */
    @POST("/api/userAddress")
    suspend fun getRechargeAddress(): ApiResponse<UserBean.URechargeBean>

    /**
     * 用户历史提现地址
     */
    @POST("/api/withdrawAddress")
    suspend fun getWithdrawAddress(): ApiResponse<List<String>>

    /**
     * 币种分类
     */
    @POST("/api/getCoinType")
    suspend fun getCoinType(): ApiResponse<List<String>>

    /**
     * 提现
     */
    @FormUrlEncoded
    @POST("/api/withdraw")
    suspend fun withDraw(@FieldMap params: Map<String, String>):ApiResponse<Any?>

    /**
     * 钱包明细
     */
    @FormUrlEncoded
    @POST("/api/walletDetail")
    suspend fun getWalletDetail(@Field("page") page: Int): ApiResponse<List<UserBean.WalletDetailBean>>
}
