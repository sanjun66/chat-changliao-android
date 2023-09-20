package com.legend.baseui.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class UITextView extends AppCompatTextView {
    public UITextView(Context context) {
        this(context, null);
    }

    public UITextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UITextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
