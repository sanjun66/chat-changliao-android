package com.legend.baseui.ui.widget.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.com.legend.ui.R
import com.com.legend.ui.databinding.UiDefaultNewDialogLayoutBinding
import com.legend.baseui.ui.util.DensityUtil

/**
 * 通用弹窗
 * desc:普通基础通用弹窗
 * created by cly on 2022/4/2
 */
class DefaultNewDialog(
    context: Context, private val builder: Builder
) : Dialog(context, R.style.ui_DialogNew) {

    companion object {
        private const val WIDTH_RATIO = 0.861f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initAttr()
    }

    private fun initAttr() {
        window?.let {
            val attributes = it.attributes
            attributes.width = (DensityUtil.getScreenWidth(context) * WIDTH_RATIO).toInt()
            it.attributes = attributes
        }
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        val viewBinding = DataBindingUtil.inflate<UiDefaultNewDialogLayoutBinding>(LayoutInflater.from(context), R.layout.ui_default_new_dialog_layout, null, false)
        setContentView(viewBinding.root)
        //title
        viewBinding.tvDialogTitle.visibility = if (builder.title?.isNotEmpty() == true) View.VISIBLE else View.GONE
        viewBinding.tvDialogTitle.text = builder.title
        //content
        viewBinding.tvDialogContent.text = builder.content
        viewBinding.tvDialogContent.movementMethod = ScrollingMovementMethod.getInstance()
        //close
        viewBinding.ivDialogClose.visibility = if (builder.closeable) View.VISIBLE else View.GONE
        viewBinding.ivDialogClose.setOnClickListener { dismiss() }
        //cancelOutside
        setCanceledOnTouchOutside(builder.cancelOnTouchOutside)
        //left
        viewBinding.btnDialogLeft.visibility = if (builder.leftTitle?.isNotEmpty() == true) View.VISIBLE else View.GONE
        viewBinding.btnDialogLeft.text = builder.leftTitle
        viewBinding.btnDialogLeft.setOnClickListener {
            if (builder.onDialogClickListener == null) {
                dismiss()
            } else {
                builder.onDialogClickListener?.clickLeft(this)
            }
        }
        //right
        if (builder.primary) {
            viewBinding.btnDialogRight.backgroundColor = context.resources.getColor(R.color.ui_primary)
            viewBinding.btnDialogRight.setTextColor(context.resources.getColor(R.color.ui_white))
        } else {
            viewBinding.btnDialogRight.backgroundColor = Color.TRANSPARENT
            viewBinding.btnDialogRight.strokeWidth = 1
            viewBinding.btnDialogRight.strokeColor = context.resources.getColor(R.color.ui_gray_26191C1C)
            viewBinding.btnDialogRight.setTextColor(context.resources.getColor(R.color.ui_gray_505457))
        }
        viewBinding.btnDialogRight.text = builder.rightTitle
        viewBinding.btnDialogRight.setOnClickListener {
            if (builder.onDialogClickListener == null) {
                dismiss()
            } else {
                builder.onDialogClickListener?.clickRight(this)
            }
        }
    }

    abstract class OnDialogClickListener {
        open fun clickLeft(dialog: DefaultNewDialog) {
            dialog.dismiss()
        }

        open fun clickRight(dialog: DefaultNewDialog) {
            dialog.dismiss()
        }
    }

    class Builder {

        var title: String? = null
        var content: String? = null
        var leftTitle: String? = null
        var rightTitle: String? = null
        var cancelOnTouchOutside: Boolean = false

        //默认样式蓝色主按钮
        var primary: Boolean = true

        //是否显示关闭按钮
        var closeable: Boolean = false

        var onDialogClickListener: OnDialogClickListener? = null

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
            return this
        }

        fun setLeftTitle(leftTitle: String): Builder {
            this.leftTitle = leftTitle
            return this
        }

        fun setRightTitle(rightTitle: String): Builder {
            this.rightTitle = rightTitle
            return this
        }

        fun setPrimary(primary: Boolean): Builder {
            this.primary = primary
            return this
        }

        fun setCloseable(closeable: Boolean): Builder {
            this.closeable = closeable
            return this
        }

        fun setCancelOnTouchOutside(cancelOnTouchOutside: Boolean): Builder {
            this.cancelOnTouchOutside = cancelOnTouchOutside
            return this
        }

        fun clickListener(clickListener: OnDialogClickListener): Builder {
            this.onDialogClickListener = clickListener
            return this
        }

        fun build(context: Context): DefaultNewDialog {
            return DefaultNewDialog(context, this)
        }
    }
}