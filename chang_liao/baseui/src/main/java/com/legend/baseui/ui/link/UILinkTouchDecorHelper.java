package com.legend.baseui.ui.link;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.com.legend.ui.BuildConfig;
import com.legend.baseui.ui.widget.textview.ISpanTouchFix;

import java.lang.ref.WeakReference;

public class UILinkTouchDecorHelper {
    private WeakReference<ITouchableSpan> mPressedSpanRf;

    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ITouchableSpan span = getPressedSpan(textView, spannable, event);
            if (span != null) {
                span.setPressed(true);
                Selection.setSelection(spannable, spannable.getSpanStart(span),
                        spannable.getSpanEnd(span));
                mPressedSpanRf = new WeakReference<>(span);
            }
            if (textView instanceof ISpanTouchFix) {
                ISpanTouchFix tv = (ISpanTouchFix) textView;
                tv.setTouchSpanHit(span != null);
            }
            return span != null;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            ITouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
            ITouchableSpan recordSpan = null;
            if (mPressedSpanRf != null){
                recordSpan = mPressedSpanRf.get();
            }

            if(recordSpan != null && recordSpan != touchedSpan){
                recordSpan.setPressed(false);
                mPressedSpanRf = null;
                recordSpan = null;
                Selection.removeSelection(spannable);
            }
            if (textView instanceof ISpanTouchFix) {
                ISpanTouchFix tv = (ISpanTouchFix) textView;
                tv.setTouchSpanHit(recordSpan != null);
            }
            return recordSpan != null;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            boolean touchSpanHint = false;
            ITouchableSpan recordSpan = null;
            if (mPressedSpanRf != null){
                recordSpan = mPressedSpanRf.get();
            }
            if (recordSpan != null) {
                touchSpanHint = true;
                recordSpan.setPressed(false);
                if(event.getAction() == MotionEvent.ACTION_UP){
                    recordSpan.onClick(textView);
                }
            }

            mPressedSpanRf = null;
            Selection.removeSelection(spannable);
            if (textView instanceof ISpanTouchFix) {
                ISpanTouchFix tv = (ISpanTouchFix) textView;
                tv.setTouchSpanHit(touchSpanHint);
            }
            return touchSpanHint;
        } else {
            ITouchableSpan recordSpan = null;
            if (mPressedSpanRf != null){
                recordSpan = mPressedSpanRf.get();
            }
            if (recordSpan != null) {
                recordSpan.setPressed(false);
            }
            if (textView instanceof ISpanTouchFix) {
                ISpanTouchFix tv = (ISpanTouchFix) textView;
                tv.setTouchSpanHit(false);
            }
            mPressedSpanRf = null;
            Selection.removeSelection(spannable);
            return false;
        }

    }

    public ITouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);

        /*
         * BugFix: https://issuetracker.google.com/issues/113348914
         */
        try {
            int off = layout.getOffsetForHorizontal(line, x);
            if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) {
                // 实际上没点到任何内容
                off = -1;
            }
            ITouchableSpan[] link = spannable.getSpans(off, off, ITouchableSpan.class);
            ITouchableSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        } catch (IndexOutOfBoundsException e) {
            if (BuildConfig.DEBUG) {
                Log.d(this.toString(), "getPressedSpan", e);
            }
        }
        return null;
    }
}
