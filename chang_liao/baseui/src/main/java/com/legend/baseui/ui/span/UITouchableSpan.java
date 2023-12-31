package com.legend.baseui.ui.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.core.view.ViewCompat;

import com.legend.baseui.ui.link.ITouchableSpan;

/**
 * 可 Touch 的 Span，在 {@link #setPressed(boolean)} 后根据是否 pressed 来触发不同的UI状态
 * <p>
 * 提供设置 span 的文字颜色和背景颜色的功能, 在构造时传入
 * </p>
 */
public abstract class UITouchableSpan extends ClickableSpan implements ITouchableSpan {
    private static final String TAG = "UITouchableSpan";
    private boolean mIsPressed;
    @ColorInt private int mNormalBackgroundColor;
    @ColorInt private int mPressedBackgroundColor;
    @ColorInt private int mNormalTextColor;
    @ColorInt private int mPressedTextColor;

    private int mNormalBgAttr;
    private int mPressedBgAttr;
    private int mNormalTextColorAttr;
    private int mPressedTextColorAttr;

    private boolean mIsNeedUnderline = false;

    public abstract void onSpanClick(View widget);

    @Override
    public final void onClick(View widget) {
        if (ViewCompat.isAttachedToWindow(widget)) {
            onSpanClick(widget);
        }
    }


    public UITouchableSpan(@ColorInt int normalTextColor,
                           @ColorInt int pressedTextColor,
                           @ColorInt int normalBackgroundColor,
                           @ColorInt int pressedBackgroundColor) {
        mNormalTextColor = normalTextColor;
        mPressedTextColor = pressedTextColor;
        mNormalBackgroundColor = normalBackgroundColor;
        mPressedBackgroundColor = pressedBackgroundColor;
    }

    public int getNormalBackgroundColor() {
        return mNormalBackgroundColor;
    }

    public void setNormalTextColor(int normalTextColor) {
        mNormalTextColor = normalTextColor;
    }

    public void setPressedTextColor(int pressedTextColor) {
        mPressedTextColor = pressedTextColor;
    }

    public int getNormalTextColor() {
        return mNormalTextColor;
    }

    public int getPressedBackgroundColor() {
        return mPressedBackgroundColor;
    }

    public int getPressedTextColor() {
        return mPressedTextColor;
    }

    public void setPressed(boolean isSelected) {
        mIsPressed = isSelected;
    }

    public boolean isPressed() {
        return mIsPressed;
    }

    public void setIsNeedUnderline(boolean isNeedUnderline) {
        mIsNeedUnderline = isNeedUnderline;
    }

    public boolean isNeedUnderline() {
        return mIsNeedUnderline;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);
        ds.bgColor = mIsPressed ? mPressedBackgroundColor
                : mNormalBackgroundColor;
        ds.setUnderlineText(mIsNeedUnderline);
    }
}
