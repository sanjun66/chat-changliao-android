package com.legend.baseui.ui.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.ActivityManager;
import com.legend.baseui.ui.util.ActivityOFix;
import com.legend.baseui.ui.util.ScreenUtils;
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
 * BaseActivity 支持能力
 * 1. 沉浸式 / 全屏设置
 * 2. 全局标题栏支持
 * 3. 页面状态切换支持
 * 4. 页面基础下拉刷新支持
 * 5. loadingDialog支持
 */
public abstract class BaseActivity<DB extends ViewDataBinding> extends AppCompatActivity implements IActivity {

    protected final String TAG = getClass().getName();

    @Autowired
    protected String from;

    private final ActivityPageConfig pageConfig = new ActivityPageConfig();
    private int immersionCapability = StatusBarUtil.PERFECT_IMMERSION;
    private LoadingDialog loadingDialog;
    private MTitleBar titleBar;
    private View fakeStatusBar;
    private SmartRefreshLayout smartRefreshLayout;
    private UILoadingLayout loadingLayout;
    public DB mDataBinding;
    protected Context mContext;

    private boolean showFakeStatusBar;

    private ViewGroup rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityOFix.INSTANCE.hookOrientation(this);
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);

        if (interceptCreate()) {
            return;
        }

        // 初始化ARouter
        ARouter.getInstance().inject(this);
        mContext = this;

        // 初始化页面信息
        initPageConfig(pageConfig);

        // 设置页面主题样式
        if (pageConfig.themeId > 0) {
            setTheme(pageConfig.themeId);
        }

        if (pageConfig.portrait && Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // 设置竖屏模式
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // 设置全屏
        // 全屏模式/沉浸式模式二选一 不可并存
        if (pageConfig.fullScreen) {
            ScreenUtils.setFullScreen(this, false);
        } else if (pageConfig.immersionStatusBar) {
            immersionCapability = StatusBarUtil.immersion(this);
        }

        // 隐藏系统状态栏
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }

        // init loading dialog
        loadingDialog = new LoadingDialog(this);

        int layoutId = getLayoutId();
        if (layoutId <= 0) {
            throw new IllegalArgumentException("You must set a layout resource id!!!");
        }

        initContentView(layoutId);

        initView();

        initData();

    }

    /**
     * 沉浸式状态栏，darkFont可以设置状态栏字体颜色
     * 在initView中调用
     *
     * @param darkFont true(黑色字体) false(白色字体)
     */
    public void setImmersionStatusBar(boolean darkFont) {
        StatusBarUtil.immersion(this);
        if (fakeStatusBar != null) {
            rootView.removeView(fakeStatusBar);
        }
        setStatusBarFontColor(darkFont);
    }

    /**
     * 设置状态栏的背景色和字体颜色
     * @param bgColor
     * @param darkFont
     */
    public void setNormalStatusBarColor(@ColorInt int bgColor,boolean darkFont){
        setFakeStatusBarColor(bgColor);
        setStatusBarFontColor(darkFont);
    }

    private void setStatusBarFontColor(boolean darkFont){
        if (darkFont) {
            StatusBarUtil.setLightMode(this);
        } else {
            StatusBarUtil.setDarkMode(this);
        }
    }

    /**
     * create 方法拦截
     */
    public boolean interceptCreate() {
        return false;
    }

    private void initContentView(int layoutId) {
        // contentView
        View contentView = LayoutInflater.from(this).inflate(layoutId, null);
        mDataBinding = DataBindingUtil.bind(contentView);
        if (mDataBinding != null) {
            mDataBinding.setLifecycleOwner(this);
        }
        // rootView -> linearLayout { fakeStatusBar, titleLayout ,contentContainerLayout }
        LinearLayout rootView = new LinearLayout(this);
        this.rootView = rootView;
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.setOrientation(LinearLayout.VERTICAL);

        // fakeStatusBar
        if (pageConfig.fakeStatusBar
                && !pageConfig.fullScreen
                && pageConfig.immersionStatusBar
                && immersionCapability > 0) {
            fakeStatusBar = new View(this);
            if (pageConfig.fakeStatusBarColor != 0)
                fakeStatusBar.setBackgroundColor(pageConfig.fakeStatusBarColor);
            fakeStatusBar.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            fakeStatusBar.setId(R.id.ui_fake_status_bar_view);
            rootView.addView(fakeStatusBar);
            StatusBarUtil.resetFakeStatusBarView(immersionCapability, fakeStatusBar);
            showFakeStatusBar = true;
        }

        // titleBar
        if (pageConfig.showAppTitleBar) {
            titleBar = new MTitleBar(this);
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
            titleBar.setLeftIcon(R.drawable.ui_double_back);
            rootView.addView(titleBar);
        }

        // contentContainerLayout { contentView, loadingLayout }
        // loadingLayout是覆盖在contentView上，与contentView（或者包裹contentView的SmartRefreshLayout）同级
        FrameLayout contentContainerLayout = new FrameLayout(this);
        contentContainerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // check if wrap SmartRefreshLayout
        contentContainerLayout.addView(wrapWithRefreshLayout(contentView));

        // check if add LoadingLayout
        if (pageConfig.enableLoadingLayout) {
            loadingLayout = new UILoadingLayout(this);
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
                    finish();
                }
            });
            contentContainerLayout.addView(loadingLayout);
        }

        rootView.addView(contentContainerLayout);

        rootView.setBackgroundColor(pageConfig.backgroundColor);

        setContentView(rootView);

        onViewCreated(rootView);
    }

    private View wrapWithRefreshLayout(View contentView) {
        if (!pageConfig.enableRefresh && !pageConfig.enableLoadMore) {
            return contentView;
        }

        smartRefreshLayout = new SmartRefreshLayout(this);
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
    public void initPageConfig(ActivityPageConfig pageConfig) {

    }

    /**
     * 初始化页面数据
     */
    @Override
    public void initData() {

    }

    /**
     * 布局加载完成
     * @param view
     */
    public void onViewCreated(View view) {

    }

    /**
     * activity 标题
     *
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
     *
     * @return 下拉刷新头部
     */
    @Override
    public RefreshFooter getRefreshFooter() {
        return new ClassicsFooter(this);
    }

    /**
     * 默认使用全局统一的下拉刷新底部，如有页面需要定制可单独重写
     *
     * @return 下拉刷新底部
     */
    @Override
    public RefreshHeader getRefreshHeader() {
        return new ClassicsHeader(this);
    }

    /**
     * 设置是否可以下拉刷新
     *
     * @param enable enable true or false
     */
    @Override
    public void setEnableRefresh(boolean enable) {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setEnableRefresh(enable);
        }
    }

    /**
     * 重置加载更多状态
     */
    public void resetLoadMoreState() {
        this.smartRefreshLayout.resetNoMoreData();
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
     *
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
     *
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
            loadingDialog = new LoadingDialog(this);
        }
        return loadingDialog;
    }

    /**
     * 显示loadingDialog
     */
    public void showLoadingDialog() {
        if (!isActivityActive()) {
            return;
        }

        LoadingDialog loadingDialog = getLoadingDialog();
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * 隐藏loadingDialog
     */
    public void hideLoadingDialog() {
        if (!isActivityActive()) {
            return;
        }

        LoadingDialog loadingDialog = getLoadingDialog();
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
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
        finish();
    }

    /**
     * 若要监听标题栏右上角点击 可重写该方法
     */
    @Override
    public void onTitleBarRightClick() {

    }
    protected UILoadingLayout getLoadingLayout() {
        return loadingLayout;
    }
    /**
     * 获取沉浸式能力状态
     * {@link StatusBarUtil#NOT_SUPPORT_IMMERSION} 不支持沉浸式
     * {@link StatusBarUtil#PERFECT_IMMERSION} 既能沉浸式，也能修改状态栏字体颜色
     * {@link StatusBarUtil#NOT_SUPPORT_IMMERSION} 只能沉浸式，不能修改状态栏字体颜色
     *
     * @return 沉浸式状态
     */
   public int getImmersionCapability() {
        return immersionCapability;
    }

    @Override
    public void replaceFragment(int container, Fragment fragment) {
        if (!isFinishing()) {
            FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
            fm.replace(container, fragment);
            fm.commitAllowingStateLoss();
        }
    }

    @Override
    public void replaceFragment(int container, String fragmentPath) {
        Object navigation = ARouter.getInstance().build(fragmentPath).navigation();
        if (navigation instanceof Fragment) {
            replaceFragment(container, ((Fragment) navigation));
        }
    }

    @Override
    public void replaceFragment(int container, Fragment fragment, int inAnim, int outAnim) {
        if (!isFinishing()) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(inAnim, outAnim)
                    .replace(container, fragment)
                    .commitAllowingStateLoss();
        }
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
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().finishActivity(this);
        this.mContext = null;
        this.loadingDialog = null;
        this.titleBar = null;
        this.smartRefreshLayout = null;
        this.loadingLayout = null;
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration config = resources.getConfiguration();
        if (config != null && config.fontScale != 1) {
            config.fontScale = 1;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        return resources;
    }

    /**
     * 判断activity是否为可用状态
     * @return 可用状态
     */
    private boolean isActivityActive() {
        return !isFinishing() && !isDestroyed();
    }

    public void replaceFragment(int container, Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(container, fragment, tag).commitAllowingStateLoss();
    }

}
