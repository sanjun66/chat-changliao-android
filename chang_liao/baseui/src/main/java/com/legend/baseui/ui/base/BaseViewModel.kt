package com.legend.baseui.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
open class BaseViewModel: ViewModel() {
    companion object {
        /**
         * 首次加载失败
         */
        val FIRST_LOAD_ERROR = 1000

        /**
         * 首次加载成功
         */
        val FIRST_LOAD_SUCCESS = 1001

        /**
         * 首次无数据
         */
        val FIRST_LOAD_EMPTY = 1002

        /**
         * 加载更多无数据
         */
        val LOADMORE_NODATA = 1003

        /**
         * 加载更多接口失败
         */
        val LOADMORE_ERROR = 1008

        /**
         * 加载更多成功
         */
        val LOADMORE_SUCCESS = 1004

        /**
         * 下拉刷新成功
         */
        val REFRESH_SUCCESS = 1005

        /**
         * 下拉刷新失败
         */
        val REFRESH_FAIL = 1006

        /**
         * 下拉刷新无数据
         */
        val REFRESH_EMPTY = 1007
    }


    var isFirstRequest = true
    /**
     * 监听loading状态的数据
     */
    val loadStateLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}