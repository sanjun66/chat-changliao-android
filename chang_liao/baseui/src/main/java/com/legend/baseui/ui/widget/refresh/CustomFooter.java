package com.legend.baseui.ui.widget.refresh;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

public class CustomFooter extends ClassicsFooter {
    public CustomFooter(Context context) {
        super(context);
    }

    public CustomFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int onFinish(@NonNull RefreshLayout layout, boolean success) {
        final View progressView = mProgressView;
        Drawable drawable = mProgressView.getDrawable();
        if (drawable instanceof Animatable) {
            if (((Animatable) drawable).isRunning()) {
                ((Animatable) drawable).stop();
            }
        } else {
            progressView.animate().rotation(0).setDuration(0);
        }
        progressView.setVisibility(GONE);
        return 0;
    }
}
