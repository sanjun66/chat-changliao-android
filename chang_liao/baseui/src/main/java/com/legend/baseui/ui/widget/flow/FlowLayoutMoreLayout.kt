package com.com.commonui.flow

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

/**
 * @author: fuhao
 * @date: 2021/6/15
 * description:流式布局支持展开操作，不支持收缩功能
 */
class FlowLayoutMoreLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var lineSpacing = 0   //the spacing between lines in flowlayout
    private var defaultMaxLines = 2 //默认流式布局显示的最大行数
    private var isShowAll = false  //是否展示所有的行数，默认展示约定的defaultMaxLines行数
    private var isAddMoreView = false //是否添加展示更多view
    var lineNumList = mutableListOf<Int>()
    private var viewMoreWidth = 0   //更多view的宽度
    private val moreView: View by lazy {
        DefaultMoreView(context)
    } // 更多view

    init {

    }

    fun setDefaultShowAll(isShowAll: Boolean) {
        this.isShowAll = isShowAll
    }

    fun addViewList(viewList: List<View>?) {
        if (viewList != null && viewList.isNotEmpty()) {
            for (view in viewList) {
                addView(view)
            }
            isAddMoreView = if (!isShowAll) {
                addView(moreView)
                true
            } else {
                false
            }
        }
        moreView.setOnClickListener {
            moreListener.invoke()
            isShowAll = !isShowAll
            requestLayout()
        }
    }

    private var moreListener: () -> Unit = {}  //更多的点击
    fun setOnMoreListener(moreListener: () -> Unit) {
        this.moreListener = moreListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mPaddingLeft = paddingLeft
        val mPaddingRight = paddingRight
        val mPaddingTop = paddingTop
        val mPaddingBottom = paddingBottom
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var lineUsed = mPaddingLeft + mPaddingRight
        var lineY = mPaddingTop
        var lineHeight = 0
        var allLines = 1 //总行数
        for (i in 0 until this.childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            if (isAddMoreView && i == childCount - 1) {
                continue
            }
            var spaceWidth = 0
            var spaceHeight = 0
            val childLp = child.layoutParams
            if (childLp is MarginLayoutParams) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, lineY)
                val mlp = childLp
                spaceWidth = mlp.leftMargin + mlp.rightMargin
                spaceHeight = mlp.topMargin + mlp.bottomMargin
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            spaceWidth += childWidth
            spaceHeight += childHeight
            if (lineUsed + spaceWidth > widthSize) {
                allLines += 1
                //approach the limit of width and move to next line
                if (allLines > defaultMaxLines && !isShowAll && defaultMaxLines > 0) {
                    val child = getChildAt(childCount - 1)
                    measureChild(child, widthMeasureSpec, heightMeasureSpec)
                    if (viewMoreWidth <= 0) {
                        viewMoreWidth = child.measuredWidth
                    }
                    break
                } else {
                    lineY += lineHeight + lineSpacing
                    lineUsed = mPaddingLeft + mPaddingRight
                    lineHeight = 0
                }
            }
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight
            }
            lineUsed += spaceWidth
        }
        setMeasuredDimension(
                widthSize,
                if (heightMode == MeasureSpec.EXACTLY) heightSize else lineY + lineHeight + mPaddingBottom
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val mPaddingLeft = paddingLeft
        val mPaddingRight = paddingRight
        val mPaddingTop = paddingTop

        var lineX = mPaddingLeft
        var lineY = mPaddingTop
        val lineWidth = r - l
        var lineUsed = mPaddingLeft + mPaddingRight
        var lineHeight = 0
        var lineNum = 0
        var countLines = 1  //在位置参数的总行数变化
        lineNumList.clear()
        for (i in 0 until this.childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            if (isAddMoreView && i == childCount - 1) {
                child.visibility = GONE
                continue
            }
            var spaceWidth = 0
            var spaceHeight = 0
            var left = 0
            var top = 0
            var right = 0
            var bottom = 0
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val childLp = child.layoutParams
            if (childLp is MarginLayoutParams) {
                val mlp = childLp
                spaceWidth = mlp.leftMargin + mlp.rightMargin
                spaceHeight = mlp.topMargin + mlp.bottomMargin
                left = lineX + mlp.leftMargin
                top = lineY + mlp.topMargin
                right = lineX + mlp.leftMargin + childWidth
                bottom = lineY + mlp.topMargin + childHeight
            } else {
                left = lineX
                top = lineY
                right = lineX + childWidth
                bottom = lineY + childHeight
            }
            spaceWidth += childWidth
            spaceHeight += childHeight
            if ((countLines == defaultMaxLines) && !isShowAll && (lineUsed + spaceWidth + viewMoreWidth > lineWidth) && i < childCount - 1) {
                val child1 = getChildAt(childCount - 1)
                child1.visibility = View.VISIBLE
                var left = 0
                var top = 0
                var right = 0
                var bottom = 0
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val childLp = child.layoutParams
                if (childLp is MarginLayoutParams) {
                    val mlp = childLp
                    left = lineX + mlp.leftMargin
                    top = lineY + mlp.topMargin
                    right = lineX + mlp.leftMargin + childWidth
                    bottom = lineY + mlp.topMargin + childHeight
                } else {
                    left = lineX
                    top = lineY
                    right = lineX + childWidth
                    bottom = lineY + childHeight
                }
                child1.layout(left, top, right, bottom)
                break
            }
            if (lineUsed + spaceWidth > lineWidth) {
                countLines += 1
                //approach the limit of width and move to next line
                lineNumList.add(lineNum)
                lineY += lineHeight + lineSpacing
                lineUsed = mPaddingLeft + mPaddingRight
                lineX = mPaddingLeft
                lineHeight = 0
                lineNum = 0
                if (childLp is MarginLayoutParams) {
                    val mlp = childLp
                    left = lineX + mlp.leftMargin
                    top = lineY + mlp.topMargin
                    right = lineX + mlp.leftMargin + childWidth
                    bottom = lineY + mlp.topMargin + childHeight
                } else {
                    left = lineX
                    top = lineY
                    right = lineX + childWidth
                    bottom = lineY + childHeight
                }
            }
            child.layout(left, top, right, bottom)
            lineNum++
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight
            }
            lineUsed += spaceWidth
            lineX += spaceWidth
//            if (i == childCount - 1) {
//                child.visibility = View.GONE
//            }
        }
        // add the num of last line
        // add the num of last line
        lineNumList.add(lineNum)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams? {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams? {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams? {
        return MarginLayoutParams(super.generateDefaultLayoutParams())
    }

    fun View.isSafeToRequestDirectly(): Boolean {
        return if (isInLayout) {
            // when isInLayout == true and isLayoutRequested == true,
            // means that this layout pass will layout current view which will
            // make currentView.isLayoutRequested == false, and this will let currentView
            // ignored in process handling requests called during last layout pass.
            isLayoutRequested.not()
        } else {
            var ancestorLayoutRequested = false
            var p: ViewParent? = parent
            while (p != null) {
                if (p.isLayoutRequested) {
                    ancestorLayoutRequested = true
                    break
                }
                p = p.parent
            }
            ancestorLayoutRequested.not()
        }
    }

    fun View.safeRequestLayout() {
        if (isSafeToRequestDirectly()) {
            requestLayout()
        } else {
            post { requestLayout() }
        }
    }

}