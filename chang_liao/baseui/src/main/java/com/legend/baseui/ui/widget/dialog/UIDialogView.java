package com.legend.baseui.ui.widget.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.legend.baseui.ui.layout.UILinearLayout;

/**
 *  Dialog 容器
 */
public class UIDialogView extends UILinearLayout {


    private OnDecorationListener mOnDecorationListener;

    public UIDialogView(Context context) {
        this(context, null);
    }

    public UIDialogView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UIDialogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnDecorationListener(OnDecorationListener onDecorationListener) {
        mOnDecorationListener = onDecorationListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOnDecorationListener != null) {
            mOnDecorationListener.onDraw(canvas, this);
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mOnDecorationListener != null) {
            mOnDecorationListener.onDrawOver(canvas, this);
        }
    }

    public interface OnDecorationListener {
        void onDraw(Canvas canvas, UIDialogView view);

        void onDrawOver(Canvas canvas, UIDialogView view);
    }
}
