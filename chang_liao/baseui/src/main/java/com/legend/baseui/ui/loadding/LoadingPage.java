package com.legend.baseui.ui.loadding;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoadingPage extends FrameLayout {
    private LayoutInflater mInflater;
    private Loading loading;
    private Error error;
    private Empty empty;

    public LoadingPage(@NonNull Context context) {
        this(context, null);
    }

    public LoadingPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 数据加载中显示加载页
     */
    public void showLoading() {
        if (null == loading)
            loading = new Loading(getInflater());
        loading.show(this);
        setVisibility(VISIBLE);
    }

    /**
     * 数据加载失败, 显示错误页
     * @param onRetryListener 重试按钮回调
     */
    public void showError(Error.OnRetryListener onRetryListener) {
        if (null == error)
            error = new Error(getInflater());
        error.show(this);
        error.setRetryClickListener(onRetryListener);
        setVisibility(VISIBLE);
    }

    /**
     * 数据加载成功,但是无数据, 显示空白页
     */
    public void showEmpty() {
        if (null == empty)
            empty = new Empty(getInflater());
        empty.show(this);
        setVisibility(VISIBLE);
    }

    /**
     * 数据加载成功,显示页面内容
     */
    public void showContent() {
        removeAllViews();
        setVisibility(GONE);
    }

    /**
     * 设置自定义加载页. 继承Loading
     * @param loading
     */
    public void setLoading(Loading loading) {
        this.loading = loading;
    }

    /**
     * 设置自定义错误页. 继承Error
     * @param error
     */
    public void setError(Error error) {
        this.error = error;
    }

    /**
     * 设置自定义空白页, 继承Empty
     * @param empty
     */
    public void setEmpty(Empty empty) {
        this.empty = empty;
    }

    private LayoutInflater getInflater() {
        if (null == mInflater)
            mInflater = LayoutInflater.from(getContext());
        return mInflater;
    }
}
