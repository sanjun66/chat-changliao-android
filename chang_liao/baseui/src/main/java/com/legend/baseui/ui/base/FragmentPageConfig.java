package com.legend.baseui.ui.base;

import android.graphics.Color;

import androidx.annotation.ColorInt;

public class FragmentPageConfig {
    /**
     * 是否显示全局自定义APP标题栏
     */
    public boolean showAppTitleBar = false;

    /**
     * 是否使用假控件填充状态栏
     */
    public boolean fakeStatusBar = false;

    /**
     * 是否可以下拉刷新
     */
    public boolean enableRefresh = false;

    /**
     * 是否可以上拉加载更多
     */
    public boolean enableLoadMore = false;

    /**
     * 是否开启LoadingLayout
     */
    public boolean enableLoadingLayout = true;

    /**
     * 伪造的状态栏颜色
     */
    @ColorInt
    public int fakeStatusBarColor = 0;

    /**
     * fragment 背景色
     */
    @ColorInt
    public int background = Color.WHITE;

    /**
     * 顶部标题栏配置
     */
    public TitleBarConfig titleBarConfig;
}
