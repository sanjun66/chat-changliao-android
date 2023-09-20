package com.legend.baseui.ui.widget.round;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.com.legend.ui.R;

/**
 * 圆角布局
 * (注:在用该布局包裹ImageView时会出现锯齿问题，因为使用了clipPath来做裁剪)
 */
public class RoundRelativeLayout extends RelativeLayout {
    private float roundLayoutRadius = 50f;
    private float mTopRadius = 0f;
    private float mBottomRadius = 0f;
    private Path roundPath;
    private RectF rectF;

    public RoundRelativeLayout(Context context) {
        this(context, null);
    }

    public RoundRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public RoundRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.ui_RoundRelativeLayout);
            roundLayoutRadius = typedArray.getDimensionPixelSize(R.styleable.ui_RoundRelativeLayout_ui_rl_r_radius, 50);
            mTopRadius = typedArray.getDimensionPixelSize(R.styleable.ui_RoundRelativeLayout_ui_rl_top_r_radius, 0);
            mBottomRadius = typedArray.getDimensionPixelSize(R.styleable.ui_RoundRelativeLayout_ui_rl_bottom_r_radius, 0);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }

        init();

    }

    private void init() {
        setWillNotDraw(false);//如果你继承的是ViewGroup,注意此行,否则draw方法是不会回调的;
        roundPath = new Path();
        rectF = new RectF();
    }

    private void setRoundPath() {
        //添加一个圆角矩形到path中, 如果要实现任意形状的View, 只需要手动添加path就行
        roundPath.reset();
        if (mTopRadius > 0 || mBottomRadius > 0) {
            roundPath.addRoundRect(rectF, new float[]{mTopRadius, mTopRadius, mTopRadius, mTopRadius, mBottomRadius, mBottomRadius, mBottomRadius, mBottomRadius}, Path.Direction.CW);
        } else {
            roundPath.addRoundRect(rectF, roundLayoutRadius, roundLayoutRadius, Path.Direction.CW);
        }
    }


    public void setRoundLayoutRadius(float topRadius, float bottomRadius) {
        mTopRadius = topRadius;
        mBottomRadius = bottomRadius;
        roundLayoutRadius = 0;
        setRoundPath();
        postInvalidate();
    }

    public void setRoundLayoutRadius(float roundLayoutRadius) {
        mTopRadius = 0;
        mBottomRadius = 0;
        this.roundLayoutRadius = roundLayoutRadius;
        setRoundPath();
        postInvalidate();
    }

    public float getRoundLayoutRadius() {
        return roundLayoutRadius;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        rectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
        setRoundPath();
    }

    @Override
    public void draw(Canvas canvas) {
        if (roundLayoutRadius > 0f || mTopRadius > 0 || mBottomRadius > 0) {
            canvas.clipPath(roundPath);
        }
        super.draw(canvas);
    }
    //    protected void onDraw(Canvas canvas) {
    //        if (mRectF == null) {
    //            mRectF = new RectF(0，0，getWidth(), getHeight());
    //        }
    //        // 开始画后面的背景
    //        mPaint.setColor(mPaintColor);
    //        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
    //        // 这里是画文字的部分，我们不管
    //        super.onDraw(canvas);
    //    }
}