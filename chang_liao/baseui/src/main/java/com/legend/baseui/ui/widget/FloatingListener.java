package com.legend.baseui.ui.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author: bingyan
 * @Date: 2022/12/8 14:52
 */
public class FloatingListener implements View.OnTouchListener {
    //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
    private float mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY, dX, dY;
    //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
    private float mStartX, mStartY, mStopX, mStopY;
    private int mTouchLimit;
    //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
    private boolean isMove;
    private long mTouchStartTime;

    private OnUpdateListener listener;

    public FloatingListener(Context context, OnUpdateListener listener) {
        this.listener = listener;
        mTouchLimit = dp2px(context, 5);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        mStopX = event.getRawX();
        mStopY = event.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                mTouchStartX = event.getRawX();
                mTouchStartY = event.getRawY();
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                dX = mStopX - mStartX;
                dY = mStopY - mStartY;
                mStartX = mStopX;
                mStartY = mStopY;
                if (listener != null) listener.update(dX, dY, false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dX = mStopX - mStartX;
                dY = mStopY - mStartY;
                if (listener != null) listener.update(dX, dY, true);
                if (Math.abs(mStopX - mTouchStartX) >= mTouchLimit || Math.abs(mStartY - mTouchStartY) >= mTouchLimit) {
                    isMove = true;
                }
                break;
            default:
                break;
        }

        //如果是移动事件不触发OnClick事件，防止移动的时候一放手形成点击事件
        return isMove;
    }

    public interface OnUpdateListener {
        void update(float x, float y, boolean toSide);
    }

    public static int dp2px(Context context, float dipValue) {
        float scale = context.getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (dipValue * scale + 0.5f);
    }
}
