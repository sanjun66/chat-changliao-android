package com.legend.base.utils;

import android.content.Context;

class DensityUtil {
    static int dip2px(Context context, float dpValue) {
        if (context == null) {
            return 0;
        } else {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int)(dpValue * scale + 0.5F);
        }
    }
}
