package com.legend.baseui.ui.base;

import android.graphics.Color;

import androidx.annotation.ColorInt;

public class ActivityPageConfig {
    /**
     * 是否显示全局自定义APP标题栏
     */
    public boolean showAppTitleBar = false;

    /**
     * 是否设置为全屏模式
     */
    public boolean fullScreen = false;

    /**
     * 是否设置沉浸式状态栏
     */
    public boolean immersionStatusBar = true;

    /**
     * 是否使用假控件填充状态栏
     */
    public boolean fakeStatusBar = true;

    /**
     * 是否设置为竖屏模式
     */
    public boolean portrait = true;

    /**
     * theme id
     */
    public int themeId = 0;

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
     * activity 背景色
     */
    @ColorInt
//    public int backgroundColor = Color.parseColor("#F9F9FF");
    public int backgroundColor = Color.parseColor("#F4FBFE");

    /**
     * 顶部标题栏配置
     */
    public TitleBarConfig titleBarConfig;
}
