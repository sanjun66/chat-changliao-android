package com.com.commonui.flow

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.com.legend.ui.R
import com.com.legend.ui.databinding.UiLayoutFlowMoreBinding


/**
 * @author: 付浩
 * @date: 2021/6/16
 * description: 流式布局默认展示更多view
 */
class DefaultMoreView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val binding: UiLayoutFlowMoreBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ui_layout_flow_more, this, true)

    init {

    }
}