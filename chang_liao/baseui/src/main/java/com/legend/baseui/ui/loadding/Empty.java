package com.legend.baseui.ui.loadding;

import android.view.LayoutInflater;
import android.view.View;

import com.com.legend.ui.R;

public class Empty {
    protected View emptyView;

    public Empty() {

    }

    public Empty(LayoutInflater inflater) {
        emptyView = inflater.inflate(R.layout.ui_empty, null);
    }

    void show(LoadingPage loadingPage) {
        if (null == emptyView)
            throw new IllegalArgumentException("emptyView must init");

        loadingPage.removeAllViews();
        loadingPage.addView(emptyView);
    }
}
