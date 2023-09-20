package com.legend.baseui.ui.util;

import android.graphics.drawable.Drawable;
import android.view.View;

public class UIViewHelper {

    public static void setBackgroundKeepingPadding(View view, Drawable drawable) {
        int[] padding = new int[]{
                view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                view.getPaddingBottom()
        };
        view.setBackground(drawable);
        view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }
}
