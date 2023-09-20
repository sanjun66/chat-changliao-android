package com.legend.main.dialog

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.Fragment
import com.legend.main.R
import com.legend.main.databinding.PopOneTextBinding
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AlphaConfig
import razerdp.util.animation.AnimationHelper

class OneTextPopup: BasePopupWindow {
    private var mDataBinding: PopOneTextBinding? = null
    private var mName: String? = null
    var listener: OnTextClickListener? = null

    constructor(context: Context, name: String) :super(context) {
        setContentView(R.layout.pop_one_text)
        mName = name
        init()
    }

    constructor(fragment: Fragment, name: String): super(fragment) {
        setContentView(R.layout.pop_one_text)
        mName = name
        init()
    }

    private fun init() {
        setAutoMirrorEnable(true)
        showAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.IN).toShow()
        dismissAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.OUT).toDismiss()
        popupGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
    }

    override fun onViewCreated(contentView: View) {
        mDataBinding = PopOneTextBinding.bind(contentView)

        mDataBinding?.apply {
            if (!TextUtils.isEmpty(mName)) tvName.text = mName
            tvName.setOnClickListener {
                listener?.onTextClicked()
                dismiss()
            }
        }
    }

    interface OnTextClickListener {
        fun onTextClicked()
    }
}