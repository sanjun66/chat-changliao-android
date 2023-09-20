package com.legend.baseui.ui.base.lazyload

import androidx.databinding.ViewDataBinding
import com.legend.baseui.ui.base.BaseFragment

/**
 * 懒加载fragment
 */
abstract class LazyLoadFragment<DB : ViewDataBinding>: BaseFragment<DB>() {

    /**
     * 默认都是在展示的
     */
    var isCurTab = true

    /**
     * 默认首次展示
     */
    var isFirstLoad = true

    override fun onResume() {
        super.onResume()
        if (isCurTab) {
            if (isFirstLoad) {
                lazyLoad()
                isFirstLoad = false
            } else {
                onFragmentShow()
            }
        }
    }

    /**
     * 非首次的展现
     */
    open fun onFragmentShow() {

    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isCurTab = hidden
    }
    abstract fun lazyLoad()
}