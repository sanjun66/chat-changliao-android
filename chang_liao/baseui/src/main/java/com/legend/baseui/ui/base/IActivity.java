package com.legend.baseui.ui.base;

public interface IActivity extends IActivitySupport, IAppTitleBarSupport, ILoadingLayoutSupport, IRefreshLayoutSupport, IFakeStatusBarSupport {
    /**
     * 初始化 View layoutId
     */
    int getLayoutId();

    /**
     * 初始化 View
     */
    void initView();

    /**
     * 初始化数据
     */
    void initData();
}
