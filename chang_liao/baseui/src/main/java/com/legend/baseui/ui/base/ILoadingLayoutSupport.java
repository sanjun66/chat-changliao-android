package com.legend.baseui.ui.base;

import com.com.legend.ui.R;

public interface ILoadingLayoutSupport {
    void showLoadingLayoutError();

    void showLoadingLayoutError(String text);

    void showLoadingLayoutSuccess();

    void showLoadingLayoutLoading();

    void showLoadingLayoutLoading(boolean transparentBg);

    void showLoadingLayoutDataEmpty();

    void showLoadingLayoutDataEmpty(String text);

    void onLoadingLayoutRetry();
}
