package com.legend.baseui.ui.alpha;

import android.content.Context;
import android.util.AttributeSet;

import com.legend.baseui.ui.widget.textview.UISpanTouchFixTextView;

/**
 * 在 pressed 和 disabled 时改变 View 的透明度
 */
public class UIAlphaTextView extends UISpanTouchFixTextView implements UIAlphaViewInf {

    private UIAlphaViewHelper mAlphaViewHelper;

    public UIAlphaTextView(Context context) {
        super(context);
    }

    public UIAlphaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UIAlphaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private UIAlphaViewHelper getAlphaViewHelper() {
        if (mAlphaViewHelper == null) {
            mAlphaViewHelper = new UIAlphaViewHelper(this);
        }
        return mAlphaViewHelper;
    }

    @Override
    protected void onSetPressed(boolean pressed) {
        super.onSetPressed(pressed);
        getAlphaViewHelper().onPressedChanged(this, pressed);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getAlphaViewHelper().onEnabledChanged(this, enabled);
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    @Override
    public void setChangeAlphaWhenPress(boolean changeAlphaWhenPress) {
        getAlphaViewHelper().setChangeAlphaWhenPress(changeAlphaWhenPress);
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    @Override
    public void setChangeAlphaWhenDisable(boolean changeAlphaWhenDisable) {
        getAlphaViewHelper().setChangeAlphaWhenDisable(changeAlphaWhenDisable);
    }
}
