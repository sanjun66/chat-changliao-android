package com.legend.baseui.ui.base.tab;

import android.app.Activity;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.com.legend.ui.R;
import com.legend.baseui.ui.base.BaseFragment;
import com.legend.baseui.ui.util.StatusBarStyleUtil;

public abstract class TabFragment<DB extends ViewDataBinding> extends BaseFragment<DB> {

    private boolean isCurrentTab = true;
    private boolean isTab = false;
    private int statusBarStyle = 2;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        changeStatusBaStyle();
    }

    @Override
    public final void onStart() {
        super.onStart();
        if (isCurrentTab)
            onTabStart();
    }

    @Override
    public final void onPause() {
        super.onPause();
        if (isCurrentTab)
            onTabPause();
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (isCurrentTab)
            onTabResume();
        else
            onHomeResume();
    }

    @Override
    public final void onStop() {
        super.onStop();
        if (isCurrentTab)
            onTabStop();
    }

    @Override
    public final void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (isTab)
            isCurrentTab = !hidden;
        onTabHiddenChanged(hidden);
    }

    protected void setStatusBarStyle(int statusBarStyle) {
        this.statusBarStyle = statusBarStyle;
    }

    public void changeStatusBaStyle() {
        StatusBarStyleUtil.changeStyle(getActivity(), statusBarStyle);
    }

    public void setTab(boolean tab) {
        this.isTab = tab;
    }

    public void onTabStart() {
    }

    public void onTabPause() {
    }

    public void onTabResume() {
    }

    public void onTabStop() {
    }

    public void onTabHiddenChanged(boolean hidden) {

    }

    /**
     * HomeActivity 执行onResume方法时所有TabFragment都会回调onResume方法.
     * TabFragment 界面刷新的回调 实现onTabResume和onTabHiddenChanged
     * 这个回调主要是一些后台数据更新使用. 谨慎实现
     */
    public void onHomeResume() {

    }

    /**
     * 回到顶部或者执行下拉刷新逻辑
     */
    public void onTabRefresh() {

    }

    /**
     * 执行二级操作行为
     * @param innerParams json string 类型的数据， 需具体的二级页面具体手动解析具体的行为
     * "{"selector":"xxxxxx", "params":"{"key":"value"}", "bridgeName":"xxxxx"}"
     */
    public void onExecInnerAction(String innerParams) {

    }

    @Override
    public void showLoadingLayoutError() {
        super.showLoadingLayoutError();

        if (!isTab)
            return;

        View view = getView();
        if (null != view) {
            view = view.findViewById(R.id.back);
            if (null != view)
                view.setVisibility(View.GONE);
        }
    }
}
