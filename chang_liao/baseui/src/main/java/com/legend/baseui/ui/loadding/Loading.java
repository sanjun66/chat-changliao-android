package com.legend.baseui.ui.loadding;


import android.view.LayoutInflater;
import android.view.View;
import com.com.legend.ui.R;

public class Loading {
    protected View loadingView;

    public Loading() {

    }

    public Loading(LayoutInflater inflater) {
        loadingView = inflater.inflate(R.layout.ui_loading_default, null);
    }

    void show(LoadingPage loadingPage) {
        if (null == loadingView)
            throw new IllegalArgumentException("loadingView must init");

        loadingPage.removeAllViews();
        loadingPage.addView(loadingView);
    }
}
