/*
 * Copyright 2018 GcsSloop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 2018-04-13 23:18:56
 *
 * GitHub: https://github.com/GcsSloop
 * WeiBo: http://weibo.com/GcsSloop
 * WebSite: http://www.gcssloop.com
 */

package com.legend.baseui.ui.widget.round;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.legend.baseui.ui.widget.round.helper.RoundAttrs;
import com.legend.baseui.ui.widget.round.helper.RoundHelper;

public class RoundLayout extends RelativeLayout implements Checkable, RoundAttrs {
    RoundHelper helper;

    public RoundLayout(Context context) {
        this(context, null);
    }

    public RoundLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        helper = new RoundHelper();
        helper.initAttrs(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        helper.onSizeChanged(this, w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(helper.mLayer, null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        helper.onClipDraw(canvas);
        canvas.restore();
    }

    @Override
    public void draw(Canvas canvas) {
        if (helper.mClipBackground) {
            canvas.save();
            canvas.clipPath(helper.mClipPath);
            super.draw(canvas);
            canvas.restore();
        } else {
            super.draw(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN && !helper.mAreaRegion.contains((int) ev.getX(), (int) ev.getY())) {
            return false;
        }
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            refreshDrawableState();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            setPressed(false);
            refreshDrawableState();
        }
        return super.dispatchTouchEvent(ev);
    }

    //--- 公开接口 ----------------------------------------------------------------------------------

    public void setClipBackground(boolean clipBackground) {
        helper.mClipBackground = clipBackground;
        invalidate();
    }

    public void setRoundAsCircle(boolean roundAsCircle) {
        helper.mRoundAsCircle = roundAsCircle;
        invalidate();
    }

    public void setRadius(int radius) {
        for (int i = 0; i < helper.radii.length; i++) {
            helper.radii[i] = radius;
        }
        invalidate();
    }

    public void setTopLeftRadius(int topLeftRadius) {
        helper.radii[0] = topLeftRadius;
        helper.radii[1] = topLeftRadius;
        invalidate();
    }

    public void setTopRightRadius(int topRightRadius) {
        helper.radii[2] = topRightRadius;
        helper.radii[3] = topRightRadius;
        invalidate();
    }

    public void setBottomLeftRadius(int bottomLeftRadius) {
        helper.radii[6] = bottomLeftRadius;
        helper.radii[7] = bottomLeftRadius;
        invalidate();
    }

    public void setBottomRightRadius(int bottomRightRadius) {
        helper.radii[4] = bottomRightRadius;
        helper.radii[5] = bottomRightRadius;
        invalidate();
    }

    public void setStrokeWidth(int strokeWidth) {
        helper.mStrokeWidth = strokeWidth;
        invalidate();
    }

    public void setStrokeColor(int strokeColor) {
        helper.mStrokeColor = strokeColor;
        invalidate();
    }

    @Override
    public void invalidate() {
        if (null != helper)
            helper.refreshRegion(this);
        super.invalidate();
    }

    public boolean isClipBackground() {
        return helper.mClipBackground;
    }

    public boolean isRoundAsCircle() {
        return helper.mRoundAsCircle;
    }

    public float getTopLeftRadius() {
        return helper.radii[0];
    }

    public float getTopRightRadius() {
        return helper.radii[2];
    }

    public float getBottomLeftRadius() {
        return helper.radii[4];
    }

    public float getBottomRightRadius() {
        return helper.radii[6];
    }

    public int getStrokeWidth() {
        return helper.mStrokeWidth;
    }

    public int getStrokeColor() {
        return helper.mStrokeColor;
    }


    //--- Selector 支持 ----------------------------------------------------------------------------

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        helper.drawableStateChanged(this);
    }

    @Override
    public void setChecked(boolean checked) {
        if (helper.mChecked != checked) {
            helper.mChecked = checked;
            refreshDrawableState();
            if (helper.mOnCheckedChangeListener != null) {
                helper.mOnCheckedChangeListener.onCheckedChanged(this, helper.mChecked);
            }
        }
    }

    @Override
    public boolean isChecked() {
        return helper.mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!helper.mChecked);
    }

    public void setOnCheckedChangeListener(RoundHelper.OnCheckedChangeListener listener) {
        helper.mOnCheckedChangeListener = listener;
    }
}
