package com.legend.baseui.ui.loadding;

import android.view.LayoutInflater;
import android.view.View;

import com.com.legend.ui.R;

public class Error {
    protected View errView;
    protected View btnRetry;
    public Error() {

    }

    public Error(LayoutInflater inflater) {
        errView = inflater.inflate(R.layout.ui_error, null);
        btnRetry = errView.findViewById(R.id.btn_retry);
    }

    void setRetryClickListener(OnRetryListener onRetryListener) {
        if (null == onRetryListener || null == btnRetry)
            return;

        btnRetry.setOnClickListener(view -> {
            if (null != onRetryListener)
                onRetryListener.retry();
        });
    }

    void show(LoadingPage loadingPage) {
        if (null == errView)
            throw new IllegalArgumentException("emptyView must init");

        loadingPage.removeAllViews();

        loadingPage.addView(errView);
    }

    public interface OnRetryListener {
        void retry();
    }
}
