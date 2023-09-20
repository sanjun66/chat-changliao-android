package com.legend.baseui.ui.link;

import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.widget.TextView;

public class UIScrollingMovementMethod extends ScrollingMovementMethod {

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        return sHelper.onTouchEvent(widget, buffer, event)
                || Touch.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new UIScrollingMovementMethod();

        return sInstance;
    }

    private static UIScrollingMovementMethod sInstance;
    private static UILinkTouchDecorHelper sHelper = new UILinkTouchDecorHelper();
}
