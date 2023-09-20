package com.legend.imkit.widget

import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException

class MultiChatLayoutManager : RecyclerView.LayoutManager() {

    private var leftMargin = 0
    private var rightMargin = 0
    private var mScreenWidth = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler!!)
            return
        }
        if (childCount == 0 && state!!.isPreLayout) {
            return
        }
//        val params = recycler!!.getViewForPosition(0).layoutParams as RecyclerView.LayoutParams
//        leftMargin = params.leftMargin
//        rightMargin = params.rightMargin
        recycler?.let {
            detachAndScrapAttachedViews(it)
            layoutItem(it)
        }
    }

    private fun layoutItem(recycler: RecyclerView.Recycler) {

        if (itemCount > 9) {
            throw IllegalArgumentException("${javaClass.simpleName}最多支持9个item布局， 请检查你的item个数是否正确")
        }

        mScreenWidth = Resources.getSystem().displayMetrics.widthPixels

        val itemSize = if (itemCount > 4) {
            mScreenWidth / 3
        } else {
            mScreenWidth / 2
        }

        if (itemCount <= 4) {
            if (itemCount == 3) {
                for (i in 0 until itemCount) {
                    val view = recycler.getViewForPosition(i)
                    addView(view) // 因为detach过所以重新添加
                    measureChildWithMargins(view, 0, 0)
                    when (i) {
                        0 -> layoutDecoratedWithMargins(view, 0, 0, itemSize, itemSize)
                        1 -> layoutDecoratedWithMargins(view, itemSize, 0, itemSize * 2, itemSize)
                        else -> layoutDecoratedWithMargins(
                            view,
                            itemSize / 2,
                            itemSize,
                            itemSize + itemSize / 2,
                            itemSize * 2
                        )
                    }
                }
            } else {
                for (i in 0 until itemCount) {
                    val view = recycler.getViewForPosition(i)
                    addView(view) // 因为detach过所以重新添加
                    measureChildWithMargins(view, 0, 0)
                    if (i % 2 == 0) {
                        layoutDecoratedWithMargins(view, 0, i / 2 * itemSize, itemSize, i / 2 * itemSize + itemSize)
                    } else {
                        layoutDecoratedWithMargins(
                            view,
                            itemSize,
                            i / 2 * itemSize,
                            2 * itemSize,
                            i / 2 * itemSize + itemSize
                        )
                    }
                }
            }
        } else {
            var currentWidth = 0
            for (i in 0 until itemCount) {
                val view = recycler.getViewForPosition(i)
                addView(view) // 因为detach过所以重新添加
                measureChildWithMargins(view, 0, 0)
                if (i % 3 == 0) {
                    currentWidth = 0
                    layoutDecoratedWithMargins(view, 0, i / 3 * itemSize, itemSize, i / 3 * itemSize + itemSize)
                } else {
                    layoutDecoratedWithMargins(
                        view,
                        currentWidth + itemSize,
                        i / 3 * itemSize,
                        currentWidth + 2 * itemSize,
                        i / 3 * itemSize + itemSize
                    )
                    currentWidth += itemSize
                }
            }
        }
    }

    //因为这个布局不需要有滚动，所以直接将横竖两个方向的滚动全部取消了
    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }
}
