package com.legend.baseui.ui.widget.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.Window;
import androidx.appcompat.app.AppCompatDialog;

public class UIBaseDialog extends AppCompatDialog {
    boolean cancelable = true;
    private boolean canceledOnTouchOutside = true;
    // 标记是否已经设置 canceledOnTouchOutside
    private boolean canceledOnTouchOutsideSet;

    public UIBaseDialog(Context context) {
        this(context, 0);
    }

    public UIBaseDialog(Context context, int theme) {
        super(context, theme);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        if (this.cancelable != cancelable) {
            this.cancelable = cancelable;
            onSetCancelable(cancelable);
        }
    }

    /**
     * 调用 setCancelable() 时回调方法
     *
     * @param cancelable cancelable
     */
    protected void onSetCancelable(boolean cancelable) {

    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
        if (cancel && !cancelable) {
            cancelable = true;
        }
        canceledOnTouchOutside = cancel;
        canceledOnTouchOutsideSet = true;
    }

    /**
     * 返回window是否可以通过触摸外部边缘关闭
     *
     * @return boolean result
     */
    protected boolean shouldWindowCloseOnTouchOutside() {
        if (!canceledOnTouchOutsideSet) {
            TypedArray a =
                    getContext()
                            .obtainStyledAttributes(new int[]{android.R.attr.windowCloseOnTouchOutside});
            canceledOnTouchOutside = a.getBoolean(0, true);
            a.recycle();
            canceledOnTouchOutsideSet = true;
        }
        return canceledOnTouchOutside;
    }
}
