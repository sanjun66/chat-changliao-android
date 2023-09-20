package com.legend.baseui.ui.widget.textview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatTextView;

import com.legend.baseui.ui.layout.IUILayout;
import com.legend.baseui.ui.layout.UILayoutHelper;
import com.legend.baseui.ui.link.UILinkTouchMovementMethod;
import com.legend.baseui.ui.span.UITouchableSpan;

/**
 * <p>
 * 修复了 {@link TextView} 与 {@link android.text.style.ClickableSpan} 一起使用时，
 * 点击 {@link android.text.style.ClickableSpan} 也会触发 {@link TextView} 的事件的问题。
 * </p>
 * <p>
 * 同时通过 {@link #setNeedForceEventToParent(boolean)} 控制该 TextView 的点击事件能否传递给其 Parent，
 * 修复了 {@link TextView} 默认情况下如果添加了 {@link android.text.style.ClickableSpan} 之后就无法把点击事件传递给 {@link TextView} 的 Parent 的问题。
 * </p>
 * <p>
 * 注意: 使用该 {@link TextView} 时, 用 {@link UITouchableSpan} 代替 {@link android.text.style.ClickableSpan},
 * 且同时可以使用 {@link UITouchableSpan} 达到修改 span 的文字颜色和背景色的目的。
 * </p>
 * <p>
 * 注意: 使用该 {@link TextView} 时, 需调用 {@link #setMovementMethodDefault()} 方法设置默认的 {@link UILinkTouchMovementMethod},
 * TextView 会在 {@link #onTouchEvent(MotionEvent)} 时将事件传递给 {@link UILinkTouchMovementMethod},
 * 然后传递给 {@link UITouchableSpan}, 实现点击态的变化和点击事件的响应。
 * </p>
 *
 */
public class UISpanTouchFixTextView extends AppCompatTextView implements ISpanTouchFix, IUILayout {
    /**
     * 记录当前 Touch 事件对应的点是不是点在了 span 上面
     */
    private boolean mTouchSpanHit;

    /**
     * 记录每次真正传入的press，每次更改mTouchSpanHint，需要再调用一次setPressed，确保press状态正确
     */
    private boolean mIsPressedRecord = false;
    /**
     * TextView是否应该消耗事件
     */
    private boolean mNeedForceEventToParent = false;

    private UILayoutHelper mLayoutHelper;

    public UISpanTouchFixTextView(Context context) {
        this(context, null);
    }

    public UISpanTouchFixTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UISpanTouchFixTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setHighlightColor(Color.TRANSPARENT);
        mLayoutHelper = new UILayoutHelper(context, attrs, defStyleAttr, this);
    }

    public void setNeedForceEventToParent(boolean needForceEventToParent) {
        mNeedForceEventToParent = needForceEventToParent;
        setFocusable(!needForceEventToParent);
        setClickable(!needForceEventToParent);
        setLongClickable(!needForceEventToParent);
    }

    /**
     * 使用者主动调用
     */
    public void setMovementMethodDefault(){
        setMovementMethodCompat(UILinkTouchMovementMethod.getInstance());
    }

    public void setMovementMethodCompat(MovementMethod movement){
        setMovementMethod(movement);
        if(mNeedForceEventToParent){
            setNeedForceEventToParent(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!(getText() instanceof Spannable) || !(getMovementMethod() instanceof UILinkTouchMovementMethod)) {
            mTouchSpanHit = false;
            return super.onTouchEvent(event);
        }
        mTouchSpanHit = true;
        // 调用super.onTouchEvent,会走到UILinkTouchMovementMethod
        // 会走到UILinkTouchMovementMethod#onTouchEvent会修改mTouchSpanHint
        boolean ret = super.onTouchEvent(event);
        if(mNeedForceEventToParent){
            return mTouchSpanHit;
        }
        return ret;
    }

    @Override
    public void setTouchSpanHit(boolean hit) {
        if (mTouchSpanHit != hit) {
            mTouchSpanHit = hit;
            setPressed(mIsPressedRecord);
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean performClick() {
        if (!mTouchSpanHit && !mNeedForceEventToParent) {
            return super.performClick();
        }
        return false;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean performLongClick() {
        if (!mTouchSpanHit && !mNeedForceEventToParent) {
            return super.performLongClick();
        }
        return false;
    }

    @Override
    public final void setPressed(boolean pressed) {
        mIsPressedRecord = pressed;
        if (!mTouchSpanHit) {
            onSetPressed(pressed);
        }
    }

    protected void onSetPressed(boolean pressed) {
        super.setPressed(pressed);
    }

    @Override
    public void updateTopDivider(int topInsetLeft, int topInsetRight, int topDividerHeight, int topDividerColor) {
        mLayoutHelper.updateTopDivider(topInsetLeft, topInsetRight, topDividerHeight, topDividerColor);
        invalidate();
    }

    @Override
    public void updateBottomDivider(int bottomInsetLeft, int bottomInsetRight, int bottomDividerHeight, int bottomDividerColor) {
        mLayoutHelper.updateBottomDivider(bottomInsetLeft, bottomInsetRight, bottomDividerHeight, bottomDividerColor);
        invalidate();
    }

    @Override
    public void updateLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor) {
        mLayoutHelper.updateLeftDivider(leftInsetTop, leftInsetBottom, leftDividerWidth, leftDividerColor);
        invalidate();
    }

    public void updateRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor) {
        mLayoutHelper.updateRightDivider(rightInsetTop, rightInsetBottom, rightDividerWidth, rightDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowTopDivider(int topInsetLeft, int topInsetRight,
                                   int topDividerHeight, int topDividerColor) {
        mLayoutHelper.onlyShowTopDivider(topInsetLeft, topInsetRight, topDividerHeight, topDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowBottomDivider(int bottomInsetLeft, int bottomInsetRight,
                                      int bottomDividerHeight, int bottomDividerColor) {
        mLayoutHelper.onlyShowBottomDivider(bottomInsetLeft, bottomInsetRight, bottomDividerHeight, bottomDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor) {
        mLayoutHelper.onlyShowLeftDivider(leftInsetTop, leftInsetBottom, leftDividerWidth, leftDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor) {
        mLayoutHelper.onlyShowRightDivider(rightInsetTop, rightInsetBottom, rightDividerWidth, rightDividerColor);
        invalidate();
    }

    @Override
    public void setTopDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setTopDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setBottomDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setBottomDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setLeftDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setLeftDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setRightDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setRightDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setHideRadiusSide(int hideRadiusSide) {
        mLayoutHelper.setHideRadiusSide(hideRadiusSide);
        invalidate();
    }

    @Override
    public int getHideRadiusSide() {
        return mLayoutHelper.getHideRadiusSide();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = mLayoutHelper.getMeasuredWidthSpec(widthMeasureSpec);
        heightMeasureSpec = mLayoutHelper.getMeasuredHeightSpec(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int minW = mLayoutHelper.handleMiniWidth(widthMeasureSpec, getMeasuredWidth());
        int minH = mLayoutHelper.handleMiniHeight(heightMeasureSpec, getMeasuredHeight());
        if (widthMeasureSpec != minW || heightMeasureSpec != minH) {
            super.onMeasure(minW, minH);
        }
    }

    @Override
    public void setRadiusAndShadow(int radius, int shadowElevation, final float shadowAlpha) {
        mLayoutHelper.setRadiusAndShadow(radius, shadowElevation, shadowAlpha);
    }

    @Override
    public void setRadiusAndShadow(int radius, @UILayoutHelper.HideRadiusSide int hideRadiusSide, int shadowElevation, final float shadowAlpha) {
        mLayoutHelper.setRadiusAndShadow(radius, hideRadiusSide, shadowElevation, shadowAlpha);
    }

    @Override
    public void setRadiusAndShadow(int radius, int hideRadiusSide, int shadowElevation, int shadowColor, float shadowAlpha) {
        mLayoutHelper.setRadiusAndShadow(radius, hideRadiusSide, shadowElevation,  shadowColor, shadowAlpha);
    }

    @Override
    public void setRadius(int radius) {
        mLayoutHelper.setRadius(radius);
    }

    @Override
    public void setRadius(int radius, @UILayoutHelper.HideRadiusSide int hideRadiusSide) {
        mLayoutHelper.setRadius(radius, hideRadiusSide);
    }

    @Override
    public int getRadius() {
        return mLayoutHelper.getRadius();
    }

    @Override
    public void setOutlineInset(int left, int top, int right, int bottom) {
        mLayoutHelper.setOutlineInset(left, top, right, bottom);
    }

    @Override
    public void setBorderColor(@ColorInt int borderColor) {
        mLayoutHelper.setBorderColor(borderColor);
        invalidate();
    }

    @Override
    public void setBorderWidth(int borderWidth) {
        mLayoutHelper.setBorderWidth(borderWidth);
        invalidate();
    }

    @Override
    public void setShowBorderOnlyBeforeL(boolean showBorderOnlyBeforeL) {
        mLayoutHelper.setShowBorderOnlyBeforeL(showBorderOnlyBeforeL);
        invalidate();
    }

    @Override
    public boolean setWidthLimit(int widthLimit) {
        if (mLayoutHelper.setWidthLimit(widthLimit)) {
            requestLayout();
            invalidate();
        }
        return true;
    }

    @Override
    public boolean setHeightLimit(int heightLimit) {
        if (mLayoutHelper.setHeightLimit(heightLimit)) {
            requestLayout();
            invalidate();
        }
        return true;
    }

    @Override
    public void setUseThemeGeneralShadowElevation() {
        mLayoutHelper.setUseThemeGeneralShadowElevation();
    }

    @Override
    public void setOutlineExcludePadding(boolean outlineExcludePadding) {
        mLayoutHelper.setOutlineExcludePadding(outlineExcludePadding);
    }

    @Override
    public void setShadowElevation(int elevation) {
        mLayoutHelper.setShadowElevation(elevation);
    }

    @Override
    public int getShadowElevation() {
        return mLayoutHelper.getShadowElevation();
    }

    @Override
    public void setShadowAlpha(float shadowAlpha) {
        mLayoutHelper.setShadowAlpha(shadowAlpha);
    }

    @Override
    public float getShadowAlpha() {
        return mLayoutHelper.getShadowAlpha();
    }

    @Override
    public void setShadowColor(int shadowColor) {
        mLayoutHelper.setShadowColor(shadowColor);
    }

    @Override
    public int getShadowColor() {
        return mLayoutHelper.getShadowColor();
    }

    @Override
    public void setOuterNormalColor(int color) {
        mLayoutHelper.setOuterNormalColor(color);
    }

    @Override
    public void updateBottomSeparatorColor(int color) {
        mLayoutHelper.updateBottomSeparatorColor(color);
    }

    @Override
    public void updateLeftSeparatorColor(int color) {
        mLayoutHelper.updateLeftSeparatorColor(color);
    }

    @Override
    public void updateRightSeparatorColor(int color) {
        mLayoutHelper.updateRightSeparatorColor(color);
    }

    @Override
    public void updateTopSeparatorColor(int color) {
        mLayoutHelper.updateTopSeparatorColor(color);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mLayoutHelper.drawDividers(canvas, getWidth(), getHeight());
        mLayoutHelper.dispatchRoundBorderDraw(canvas);
    }

    @Override
    public boolean hasBorder() {
        return mLayoutHelper.hasBorder();
    }

    @Override
    public boolean hasLeftSeparator() {
        return mLayoutHelper.hasLeftSeparator();
    }

    @Override
    public boolean hasTopSeparator() {
        return mLayoutHelper.hasTopSeparator();
    }

    @Override
    public boolean hasRightSeparator() {
        return mLayoutHelper.hasRightSeparator();
    }

    @Override
    public boolean hasBottomSeparator() {
        return mLayoutHelper.hasBottomSeparator();
    }
}
