package com.legend.baseui.ui.base;

import androidx.annotation.ColorInt;

public interface IFakeStatusBarSupport {
    void hideFakeStatusBar();

    void showFakeStatusBar();

    void setFakeStatusBarColor(@ColorInt int color);

    void setFakeStatusBarAlpha(float alpha);
}
