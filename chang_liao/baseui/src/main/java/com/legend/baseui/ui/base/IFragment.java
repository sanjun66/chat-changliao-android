package com.legend.baseui.ui.base;

import android.view.View;

public interface IFragment extends IAppTitleBarSupport, ILoadingLayoutSupport, IRefreshLayoutSupport, IFakeStatusBarSupport {
    /**
     * 初始化 View layoutId
     */
    int getLayoutId();

    /**
     * 初始化 View
     * @param view .
     */
    void initView(View view);

    /**
     * 初始化数据
     */
    void initData();
}
