package com.legend.baseui.ui.widget.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.Nullable;

import com.legend.baseui.ui.widget.base.BaseRecyclerView;

public class NestingHorizontalRecyclerView extends BaseRecyclerView {
    private float mDownX = 0;
    private float mDownY = 0;

    public NestingHorizontalRecyclerView(Context context) {
        super(context);
    }

    public NestingHorizontalRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public NestingHorizontalRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float currentX = ev.getX();
        float currentY = ev.getY();
        float shiftX = Math.abs(currentX - mDownX);
        float shiftY = Math.abs(currentY - mDownY);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = currentY;
                mDownX = currentX;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (shiftX < shiftY)
                    getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
