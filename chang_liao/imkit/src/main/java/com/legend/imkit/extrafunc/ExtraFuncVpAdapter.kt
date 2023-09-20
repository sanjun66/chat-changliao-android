package com.legend.imkit.extrafunc

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class ExtraFuncVpAdapter(private val viewList: List<View>): PagerAdapter() {

    override fun getCount() = viewList.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewList[position])
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])
        return viewList[position]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}