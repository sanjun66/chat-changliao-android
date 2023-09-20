package com.legend.baseui.ui.base;

import android.graphics.drawable.Drawable;

import androidx.annotation.StringRes;

import com.legend.baseui.ui.widget.titlebar.MTitleBar;

public interface IAppTitleBarSupport {

    void setTitleBarTitleText(@StringRes int titleRes);
    void setTitleBarTitleText(String titleText);
    void setTitleBarTitleTextSize(float size);
    void setTitleBarRightTextSize(float size);
    void setTitleBarRightText(@StringRes int titleRes);
    void setTitleBarRightText(String titleText);
    void setTitleBarRightTextColor(String textColor);

    void setTitleBarLeftIcon(int iconRes);
    void setTitleBarLeftIcon(Drawable iconDrawable);
    void setTitleBarLeftIcon(String iconUrl);

    void setTitleBarRightIcon(int iconRes);
    void setTitleBarRightIcon(Drawable iconDrawable);
    void setTitleBarRightIcon(String iconUrl);

    void onTitleBarLeftClick();
    void onTitleBarRightClick();

    void showTitleBar();
    void hideTitleBar();
    void hideTitleBarLeftIcon();
    void showTitleBarLeftIcon();

    /**
     * 隐藏右侧标题（Text & icon）
     */
    void hideTitleBarRightTitle();

    MTitleBar getMTitleBar();
}
