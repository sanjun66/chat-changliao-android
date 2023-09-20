package com.legend.imkit.popwindow

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.legend.imkit.R
import com.legend.imkit.databinding.PopMsgItemBinding
import razerdp.basepopup.BasePopupWindow

class MsgItemPopup : BasePopupWindow {
    private var mDataBinding: PopMsgItemBinding? = null
    private val dataList = mutableListOf<MsgItemAction>()
    var listener: OnPopItemClickListener? = null

    companion object {
        const val TYPE_DELETE = 1
    }

    constructor(context: Context): super(context) {
        setContentView(R.layout.pop_msg_item)
//        setViewPivotRatio(mDataBinding?.ivArrow, 0.5f, 0.5f)
    }
    constructor(fragment: Fragment): super(fragment)

    override fun onViewCreated(contentView: View) {
        mDataBinding = PopMsgItemBinding.bind(contentView)
        initData()

        mDataBinding?.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = MsgItemAdapter()
            recyclerView.adapter = adapter
            adapter.setList(dataList)

            adapter.setOnItemClickListener { adapter, view, position ->
                listener?.onItemClick((adapter.getItem(position) as MsgItemAction).type)
                dismiss()
            }
        }
    }

    private fun initData() {
        dataList.add(MsgItemAction(TYPE_DELETE, context.getString(R.string.msg_action_delete)))
    }

//    override fun onPopupLayout(popupRect: Rect, anchorRect: Rect) {
//        val gravity = computeGravity(popupRect, anchorRect)
//        var verticalCenter = false
//        when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
//            Gravity.TOP -> {
//                mDataBinding?.ivArrow?.visibility = View.VISIBLE
//                mDataBinding?.ivArrow?.translationX = ((popupRect.width() - (mDataBinding?.ivArrow?.width?:0)) shr 1).toFloat()
//                mDataBinding?.ivArrow?.translationY =
//                    (popupRect.height() - (mDataBinding?.ivArrow?.height?:0)).toFloat()
//                mDataBinding?.ivArrow?.rotation = 0f
//            }
//            Gravity.BOTTOM -> {
//                mDataBinding?.ivArrow?.visibility = View.VISIBLE
//                mDataBinding?.ivArrow?.translationX = (popupRect.width() - (mDataBinding?.ivArrow?.width ?:0) shr 1).toFloat()
//                mDataBinding?.ivArrow?.translationY = 0f
//                mDataBinding?.ivArrow?.rotation = 180f
//            }
//            Gravity.CENTER_VERTICAL -> verticalCenter = true
//        }
//        when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
//            Gravity.LEFT -> {
//                mDataBinding?.ivArrow?.visibility = View.VISIBLE
//                mDataBinding?.ivArrow?.translationX = (popupRect.width() - (mDataBinding?.ivArrow?.width ?:0)).toFloat()
//                mDataBinding?.ivArrow?.translationY = ((popupRect.height() - (mDataBinding?.ivArrow?.height ?:0)shr 1).toFloat())
//                mDataBinding?.ivArrow?.rotation = 270f
//            }
//            Gravity.RIGHT -> {
//                mDataBinding?.ivArrow?.visibility = View.VISIBLE
//                mDataBinding?.ivArrow?.translationX = 0f
//                mDataBinding?.ivArrow?.translationY = (popupRect.height() - (mDataBinding?.ivArrow?.height ?:0) shr 1).toFloat()
//                mDataBinding?.ivArrow?.rotation = 90f
//            }
//            Gravity.CENTER_HORIZONTAL -> mDataBinding?.ivArrow?.visibility = if (verticalCenter) View.INVISIBLE else View.VISIBLE
//        }
//    }

    private fun setViewPivotRatio(v: View?, pvX: Float, pvY: Float) {
        if (v == null) return
        v.post(Runnable {
            v.pivotX = v.width * pvX
            v.pivotY = v.height * pvY
        })
    }

    interface OnPopItemClickListener {
        fun onItemClick(type: Int)
    }
}

class MsgItemAdapter: BaseQuickAdapter<MsgItemAction, BaseViewHolder>(R.layout.item_msg_action) {
    override fun convert(holder: BaseViewHolder, item: MsgItemAction) {
        holder.setText(R.id.tv_name, item.name)
    }

}

data class MsgItemAction(val type: Int, val name: String)