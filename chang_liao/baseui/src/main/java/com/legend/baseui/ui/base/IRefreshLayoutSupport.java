package com.legend.baseui.ui.base;

import android.graphics.drawable.Drawable;

import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

public interface IRefreshLayoutSupport {
    void onPageRefresh(RefreshLayout refreshLayout);

    void onPageLoadMore(RefreshLayout refreshLayout);

    void autoRefresh();

    void finishRefresh();

    void finishLoadMore();

    void finishLoadMoreWithNoMoreData();

    RefreshFooter getRefreshFooter();

    RefreshHeader getRefreshHeader();

    void setEnableRefresh(boolean enable);

    void setEnableLoadMore(boolean enable);

    boolean isPageRefreshing();

    boolean isPageLoading();

    void setRefreshLayoutBackground(Drawable drawable);
}
