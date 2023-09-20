package com.legend.baseui.ui.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.statusbar.StatusBarUtil;
import com.legend.baseui.ui.widget.loadding.LoadingDialog;
import com.legend.baseui.ui.widget.loadinglayout.UILoadingLayout;
import com.legend.baseui.ui.widget.titlebar.MTitleBar;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;


/**
 * BaseFragment 支持能力
 * 1. FakeStatusBar支持
 * 2. 全局标题栏支持
 * 3. 页面状态切换支持
 * 4. 页面基础下拉刷新支持
 * 5. loadingDialog支持
 */
public abstract class BaseFragment<DB extends ViewDataBinding> extends Fragment implements IFragment {

    protected final String TAG = getClass().getName();

    private View mRootView;
    public DB mDataBinding;

    private final FragmentPageConfig pageConfig = new FragmentPageConfig();
    private LoadingDialog loadingDialog;
    private MTitleBar titleBar;
    private View fakeStatusBar;
    private SmartRefreshLayout smartRefreshLayout;
    private UILoadingLayout loadingLayout;

    protected Context mContext;
    private boolean showFakeStatusBar;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        // 初始化ARouter
        ARouter.getInstance().inject(this);
        mContext = getContext();

        // 初始化页面信息
        initPageConfig(pageConfig);

        int layoutId = getLayoutId();
        if (layoutId <= 0) {
            throw new IllegalArgumentException("You must set a layout resource id!!!");
        }
        return initContentView(layoutId, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view;
        initView(view);
        initData();
    }

    private View initContentView(int layoutId, ViewGroup container) {
        // contentView
        mDataBinding = DataBindingUtil.inflate(getLayoutInflater(),layoutId, container,false);
        View contentView = mDataBinding.getRoot();
        if (mDataBinding != null) {
            mDataBinding.setLifecycleOwner(this);
        }
        // rootView -> linearLayout { fakeStatusBar, titleLayout ,contentContainerLayout }
        LinearLayout rootView = new LinearLayout(getContext());
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.setOrientation(LinearLayout.VERTICAL);

        FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            int immersionCapability = ((BaseActivity) activity).getImmersionCapability();
            // fakeStatusBar
            if (pageConfig.fakeStatusBar && immersionCapability > 0) {
                fakeStatusBar = new View(getContext());
                if (pageConfig.fakeStatusBarColor != 0)
                    fakeStatusBar.setBackgroundColor(pageConfig.fakeStatusBarColor);
                fakeStatusBar.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                fakeStatusBar.setId(R.id.ui_fake_status_bar_view);
                rootView.addView(fakeStatusBar);
                StatusBarUtil.resetFakeStatusBarView(immersionCapability, fakeStatusBar);
                showFakeStatusBar = true;
            }
        }

        // titleBar
        if (pageConfig.showAppTitleBar) {
            titleBar = new MTitleBar(getContext());
            titleBar.setConfig(pageConfig.titleBarConfig);
            titleBar.setTitle(String.valueOf(getPageTitle()));
            titleBar.setListener(new MTitleBar.OnTitleListenerWrapper() {
                @Override
                public void titleBarLeftClick() {
                    onTitleBarLeftClick();
                }

                @Override
                public void titleBarRightClick() {
                    onTitleBarRightClick();
                }
            });
            rootView.addView(titleBar);
        }

        // contentContainerLayout { contentView, loadingLayout }
        // loadingLayout是覆盖在contentView上，与contentView（或者包裹contentView的SmartRefreshLayout）同级
        FrameLayout contentContainerLayout = new FrameLayout(getContext());
        contentContainerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // check if wrap SmartRefreshLayout
        contentContainerLayout.addView(wrapWithRefreshLayout(contentView));
        // check if add LoadingLayout
        if (pageConfig.enableLoadingLayout) {
            loadingLayout = new UILoadingLayout(getContext());
            loadingLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            loadingLayout.setVisibility(View.GONE);
            loadingLayout.setOnRetryListener(new UILoadingLayout.OnRetryListener() {
                @Override
                public void onRetry() {
                    onLoadingLayoutRetry();
                }

                @Override
                public void errBack() {
                    getActivity().finish();
                }
            });
            contentContainerLayout.addView(loadingLayout);
        }

        rootView.addView(contentContainerLayout);

        rootView.setBackgroundColor(pageConfig.background);

        return rootView;

    }

    private View wrapWithRefreshLayout(View contentView) {
        if (!pageConfig.enableRefresh && !pageConfig.enableLoadMore) {
            return contentView;
        }

        smartRefreshLayout = new SmartRefreshLayout(getContext());
        smartRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        smartRefreshLayout.setRefreshHeader(getRefreshHeader());
        smartRefreshLayout.setEnableRefresh(pageConfig.enableRefresh);
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                onPageRefresh(refreshLayout);
            }
        });
        smartRefreshLayout.setRefreshFooter(getRefreshFooter());
        smartRefreshLayout.setEnableAutoLoadMore(true);
        smartRefreshLayout.setEnableLoadMore(pageConfig.enableLoadMore);
        smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                onPageLoadMore(refreshLayout);
            }
        });
        smartRefreshLayout.setRefreshContent(contentView);
        return smartRefreshLayout;
    }

    // init order custom page config
    public void initPageConfig(FragmentPageConfig pageConfig) {

    }

    /**
     * 初始化页面数据
     */
    @Override
    public void initData() {

    }

    /**
     * activity 标题
     * @return 标题
     */
    public String getPageTitle() {
        return "";
    }

    // ---- refresh layout ----

    /**
     * 如果Activity开启下拉刷新 可实现该方法完成刷新后的操作
     */
    @Override
    public void onPageRefresh(RefreshLayout refreshLayout) {

    }

    /**
     * 如果Activity开启上拉加载更多 可实现该方法完成刷新后的操作
     */
    @Override
    public void onPageLoadMore(RefreshLayout refreshLayout) {

    }

    /**
     * 设置结束下拉刷新
     */
    @Override
    public void finishRefresh() {
        if (smartRefreshLayout != null && smartRefreshLayout.isRefreshing()) {
            smartRefreshLayout.finishRefresh();
        }
    }

    /**
     * 设置结束上拉加载更多
     */
    @Override
    public void finishLoadMore() {
        if (smartRefreshLayout != null && smartRefreshLayout.isLoading()) {
            smartRefreshLayout.finishLoadMore();
        }
    }

    /**
     * 自动刷新
     */
    @Override
    public void autoRefresh() {
        if (smartRefreshLayout != null && !smartRefreshLayout.isRefreshing()) {
            smartRefreshLayout.autoRefresh();
        }
    }

    /**
     * 设置上拉没有更多数据
     */
    @Override
    public void finishLoadMoreWithNoMoreData() {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.finishLoadMoreWithNoMoreData();
        }
    }

    /**
     * 默认使用全局统一的下拉刷新头部，如有页面需要定制可单独重写
     * @return 下拉刷新头部
     */
    @Override
    public RefreshFooter getRefreshFooter() {
        return new ClassicsFooter(getContext());
    }

    /**
     * 默认使用全局统一的下拉刷新底部，如有页面需要定制可单独重写
     * @return 下拉刷新底部
     */
    @Override
    public RefreshHeader getRefreshHeader() {
        return new ClassicsHeader(getContext());
    }

    /**
     * 设置是否可以下拉刷新
     * @param enable enable true or false
     */
    @Override
    public void setEnableRefresh(boolean enable) {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setEnableRefresh(enable);
        }
    }

    /**
     * 设置刷新布局背景
     * @param drawable
     */
    @Override
    public void setRefreshLayoutBackground(Drawable drawable) {
        if (null != smartRefreshLayout ) {
            smartRefreshLayout.setBackground(drawable);
        }
    }

    /**
     * 设置是否可以上拉加载更多
     * @param enable enable true or false
     */
    @Override
    public void setEnableLoadMore(boolean enable) {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setEnableLoadMore(enable);
        }
    }

    /**
     * 是否正在下拉刷新
     * @return boolean
     */
    @Override
    public boolean isPageRefreshing() {
        return smartRefreshLayout != null && smartRefreshLayout.isRefreshing();
    }

    /**
     * 是否正在上拉加载
     */
    @Override
    public boolean isPageLoading() {
        return smartRefreshLayout != null && smartRefreshLayout.isLoading();
    }

    // ---- loading layout ----

    /**
     * 显示 LoadingLayout error 页面
     */
    @Override
    public void showLoadingLayoutError() {
       showLoadingLayoutError(null);
    }

    @Override
    public void showLoadingLayoutError(String text) {
        if (loadingLayout != null) {
            if (null != text)
                loadingLayout.setErrorText(text);

            if (null != fakeStatusBar && fakeStatusBar.getVisibility() == View.VISIBLE)
                fakeStatusBar.setVisibility(View.INVISIBLE);
            loadingLayout.show(UILoadingLayout.PageState.STATE_ERROR);
        }
    }

    protected UILoadingLayout getLoadingLayout() {
        return loadingLayout;
    }

    /**
     * 显示 LoadingLayout success 页面
     */
    @Override
    public void showLoadingLayoutSuccess() {
        if (loadingLayout != null) {
            if (showFakeStatusBar && null != fakeStatusBar)
                fakeStatusBar.setVisibility(View.VISIBLE);
            loadingLayout.show(UILoadingLayout.PageState.STATE_SUCCESS);
        }
    }

    /**
     * 显示 LoadingLayout loading 页面
     */
    @Override
    public void showLoadingLayoutLoading() {
        if (loadingLayout != null) {
            if (null != fakeStatusBar && fakeStatusBar.getVisibility() == View.VISIBLE)
                fakeStatusBar.setVisibility(View.INVISIBLE);
            loadingLayout.show(UILoadingLayout.PageState.STATE_LOADING);
        }
    }

    /**
     * 显示 LoadingLayout loading 页面
     * @param transparentBg 背景是否透明
     */
    public void showLoadingLayoutLoading(boolean transparentBg) {
        if (null != loadingLayout) {
            loadingLayout.setLoadingBackground(transparentBg ? R.color.ui_transparent : R.color.ui_white);
            showLoadingLayoutLoading();
        }
    }

    /**
     * 显示 LoadingLayout data empty 页面
     */
    @Override
    public void showLoadingLayoutDataEmpty() {
        showLoadingLayoutDataEmpty(null);
    }

    @Override
    public void showLoadingLayoutDataEmpty(String text) {
        if (loadingLayout != null) {
            if (null != text)
                loadingLayout.setEmptyText(text);

            if (null != fakeStatusBar && fakeStatusBar.getVisibility() == View.VISIBLE)
                fakeStatusBar.setVisibility(View.INVISIBLE);
            loadingLayout.show(UILoadingLayout.PageState.STATE_DATA_EMPTY);
        }
    }

    /**
     * 监听BaseActivity LoadingLayout中重试回调 重写该方法
     */
    @Override
    public void onLoadingLayoutRetry() {

    }

    // ---- loading dialog ----

    private LoadingDialog getLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        return loadingDialog;
    }

    public void resetLoadMoreState() {
        this.smartRefreshLayout.resetNoMoreData();
    }

    /**
     * 显示loadingDialog
     */
    public void showLoadingDialog() {
        FragmentActivity activity = getActivity();
        if (null == activity || activity.isFinishing() || activity.isDestroyed())
            return;

        LoadingDialog loadingDialog = getLoadingDialog();
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
//            Log.e("loadingDialog show", loadingDialog.hashCode() + "-" + this.getClass().getSimpleName());
        }
    }

    /**
     * 隐藏loadingDialog
     */
    public void hideLoadingDialog() {
        FragmentActivity activity = getActivity();
        if (null == activity || activity.isFinishing() || activity.isDestroyed())
            return;

        LoadingDialog loadingDialog = getLoadingDialog();
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
//        Log.e("loadingDialog hide", loadingDialog.hashCode() + "-" + this.getClass().getSimpleName());
//        loadingDialog = null;
        }
    }

    // ---- title bar ----
    @Override
    public void setTitleBarRightText(String title) {
        if (titleBar != null) {
            titleBar.setRightText(title);
        }
    }

    @Override
    public void setTitleBarTitleText(@StringRes int titleRes) {
        if (titleBar != null) {
            titleBar.setTitle(titleRes);
        }
    }

    @Override
    public void setTitleBarTitleTextSize(float size) {
        if (null != titleBar) {
            titleBar.setTitleSize(size);
        }
    }

    @Override
    public void setTitleBarRightTextSize(float size) {
        if (null != titleBar) {
            titleBar.setRightTitleSize(size);
        }
    }

    @Override
    public void setTitleBarTitleText(String titleText) {
        if (titleBar != null) {
            titleBar.setTitle(titleText);
        }
    }

    @Override
    public void setTitleBarRightText(@StringRes int titleRes) {
        if (titleBar != null) {
            titleBar.setRightText(titleRes);
        }
    }

    @Override
    public void setTitleBarLeftIcon(int iconRes) {
        if (titleBar != null) {
            titleBar.setLeftIcon(iconRes);
        }
    }

    @Override
    public void setTitleBarLeftIcon(Drawable iconDrawable) {
        if (titleBar != null) {
            titleBar.setLeftIcon(iconDrawable);
        }
    }

    @Override
    public void setTitleBarLeftIcon(String iconUrl) {
        if (titleBar != null) {
            titleBar.setLeftIcon(iconUrl);
        }
    }

    @Override
    public void setTitleBarRightIcon(int iconRes) {
        if (titleBar != null) {
            titleBar.setRightIcon(iconRes);
        }
    }

    @Override
    public void setTitleBarRightIcon(Drawable iconDrawable) {
        if (titleBar != null) {
            titleBar.setRightIcon(iconDrawable);
        }
    }

    @Override
    public void setTitleBarRightIcon(String iconUrl) {
        if (titleBar != null) {
            titleBar.setRightIcon(iconUrl);
        }
    }

    @Override
    public void hideTitleBarLeftIcon() {
        if (titleBar != null) {
            titleBar.showLeftIcon(false);
        }
    }

    @Override
    public void showTitleBarLeftIcon() {
        if (null != titleBar) {
            titleBar.showLeftIcon(true);
        }
    }

    @Override
    public void hideTitleBar() {
        if (null != titleBar) {
            titleBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showTitleBar() {
        if (null != titleBar) {
            titleBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setTitleBarRightTextColor(String textColor) {
        if (null != titleBar) {
            titleBar.setRightTitleColor(textColor);
        }
    }

    @Override
    public void hideTitleBarRightTitle() {
        if (titleBar != null) {
            titleBar.showRightIcon(false);
            titleBar.showRightTitle(false);
        }
    }

    @Override
    public MTitleBar getMTitleBar() {
        return titleBar;
    }

    /**
     * 默认直接关闭当前Activity 如有返回键有其他逻辑可重写该方法
     */
    @Override
    public void onTitleBarLeftClick() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    /**
     * 若要监听标题栏右上角点击 可重写该方法
     */
    @Override
    public void onTitleBarRightClick() {

    }

    @Override
    public void showFakeStatusBar() {
        if (null != fakeStatusBar) {
            fakeStatusBar.setVisibility(View.VISIBLE);
        }
        showFakeStatusBar = true;
    }

    @Override
    public void hideFakeStatusBar() {
        if (null != fakeStatusBar) {
            fakeStatusBar.setVisibility(View.GONE);
        }
        showFakeStatusBar = false;
    }

    @Override
    public void setFakeStatusBarColor(@ColorInt int color) {
        if (null != fakeStatusBar) {
            fakeStatusBar.setBackgroundColor(color);
        }
    }

    @Override
    public void setFakeStatusBarAlpha(float alpha) {
        if (null != fakeStatusBar) {
            fakeStatusBar.setAlpha(alpha);
        }
    }

    public void setViewOnClickListener(int viewId, View.OnClickListener listener) {
        if (mRootView == null) {
            return;
        }
        View view = mRootView.findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mContext = null;
        this.loadingDialog = null;
        this.titleBar = null;
        this.smartRefreshLayout = null;
        this.loadingLayout = null;
    }

    public void replaceFragment(int container, Fragment fragment, String tag) {
        getChildFragmentManager().beginTransaction().replace(container, fragment, tag).commitAllowingStateLoss();
    }

    public void replaceFragment(int container, Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(container, fragment, "").commitAllowingStateLoss();
    }
}
