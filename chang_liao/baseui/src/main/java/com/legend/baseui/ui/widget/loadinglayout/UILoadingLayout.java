package com.legend.baseui.ui.widget.loadinglayout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import com.airbnb.lottie.LottieAnimationView;
import com.legend.baseui.ui.CommonUIConfig;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.statusbar.StatusBarUtil;

/**
 * todo: 【待完善】全局loadingLayout 待设计定稿 包括LoadingLayout可以向外提供的修改内容的方法等，依据具体需求调整
 */
public class UILoadingLayout extends RelativeLayout {

    private int currentState = PageState.STATE_NONE;

    public CommonUIConfig.LoadingConfig loadingConfig = CommonUIConfig.getInstance().getLoadingConfig();
    public View errorView;
    public View loadingView;
    public View emptyView;
    private LottieAnimationView mLottieAnimationView;
    private View rlLoading;
    public UILoadingLayout(Context context) {
        this(context, null);
    }

    public UILoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UILoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // loadingLayout是覆盖在内容区域上 并非包裹内容区域
        // 设置clickable 为 true 防止点击穿透
        setClickable(true);
        setVisibility(GONE);
        initView();
    }

    private void initView() {
        Activity activityFromCtx = getActivityFromCtx(getContext());
        if (activityFromCtx != null) {
            View decorView = activityFromCtx.getWindow().getDecorView();
            if (decorView instanceof ViewGroup) {
                ViewGroup frameLayout = (ViewGroup) decorView;
                View viewById = frameLayout.findViewById(R.id.page_load_animation_view);
                if (viewById instanceof LottieAnimationView) {
                    mLottieAnimationView = (LottieAnimationView) viewById;
                } else {
                    View view= LayoutInflater.from(getContext()).inflate(R.layout.ui_loading_default, null, false);
                    mLottieAnimationView = view.findViewById(R.id.page_load_animation_view);
                    rlLoading = view.findViewById(R.id.rl_loading);
                    frameLayout.addView(view);
                }
            }
        }
        if (mLottieAnimationView.getParent() != null && mLottieAnimationView.getParent().getParent() != null) {
            ((View)mLottieAnimationView.getParent().getParent()).setVisibility(GONE);
        }
    }


    private void initEmptyView(){
        Integer emptyPageLayoutId;
        if (null != loadingConfig)
            emptyPageLayoutId = loadingConfig.emptyPageLayoutId;
        else
            emptyPageLayoutId = null;
        emptyView = LayoutInflater.from(getContext()).inflate(null != emptyPageLayoutId ? emptyPageLayoutId : R.layout.ui_loading_layout_data_empty, this, false);

    }

    private void initErrorView(){
        Integer errLayoutId;
        if (null != loadingConfig)
            errLayoutId = loadingConfig.errPageLayoutId;
        else
            errLayoutId = null;
        errorView = LayoutInflater.from(getContext()).inflate(null != errLayoutId ? errLayoutId : R.layout.ui_loading_layout_error, this, false);
        errorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRetryListener != null) {
                    onRetryListener.onRetry();
                }
            }
        });

        View back = errorView.findViewById(R.id.back);
        if (null != back) {
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onRetryListener)
                        onRetryListener.errBack();
                }
            });
        }

        View errTitleBar = errorView.findViewById(R.id.err_title_bar);
        if (null != errTitleBar) {
            ViewGroup.LayoutParams layoutParams = errTitleBar.getLayoutParams();
            int barHeight = StatusBarUtil.getStatusBarHeight(getContext());
            if (layoutParams instanceof RelativeLayout.LayoutParams)
                ((LayoutParams) layoutParams).topMargin = barHeight;
            else if (layoutParams instanceof LinearLayout.LayoutParams)
                ((LinearLayout.LayoutParams) layoutParams).topMargin = barHeight;
            else if (layoutParams instanceof FrameLayout.LayoutParams)
                ((FrameLayout.LayoutParams) layoutParams).topMargin = barHeight;
            errTitleBar.setLayoutParams(layoutParams);
        }
    }


    public void show(@PageState int pageState) {
        // 如果之前已经设置是成功状态，并且当前设置仍是成功状态，则直接return;
        if (pageState == PageState.STATE_SUCCESS && currentState == PageState.STATE_SUCCESS) {
            return;
        }
        removeAllViews();
        setVisibility(VISIBLE);
        if (mLottieAnimationView != null && mLottieAnimationView.getParent() != null && mLottieAnimationView.getParent().getParent() != null) {
            ((View)mLottieAnimationView.getParent().getParent()).setVisibility(GONE);
            mLottieAnimationView.setVisibility(GONE);
            mLottieAnimationView.pauseAnimation();
        }

        this.currentState = pageState;
        if (pageState == PageState.STATE_SUCCESS) {
            setVisibility(GONE);
        } else if (pageState == PageState.STATE_DATA_EMPTY) {
            if(emptyView == null){
                initEmptyView();
            }
            addView(emptyView);
        } else if (pageState == PageState.STATE_LOADING) {
            if (mLottieAnimationView != null && mLottieAnimationView.getParent() != null && mLottieAnimationView.getParent().getParent() != null) {
                ((View)mLottieAnimationView.getParent().getParent()).setVisibility(VISIBLE);
                mLottieAnimationView.setVisibility(VISIBLE);
                mLottieAnimationView.playAnimation();
            }
            Log.i("wdd", "show: pageState = "+ pageState);
        } else if (pageState == PageState.STATE_ERROR) {
            if(errorView == null){
                initErrorView();
            }
           addView(errorView);
        }
    }

    @IntDef({
            PageState.STATE_SUCCESS,
            PageState.STATE_DATA_EMPTY,
            PageState.STATE_LOADING,
            PageState.STATE_ERROR,
    })
    public @interface PageState {
        int STATE_NONE = -1;
        int STATE_SUCCESS = 0;
        int STATE_DATA_EMPTY = 1;
        int STATE_LOADING = 2;
        int STATE_ERROR = 3;
    }

    public interface OnRetryListener {
        void onRetry();

        void errBack();
    }

    private OnRetryListener onRetryListener;

    public void setOnRetryListener(OnRetryListener onRetryListener) {
        this.onRetryListener = onRetryListener;
    }

    public void setEmptyText(String tips) {
        if(emptyView == null){
            initEmptyView();
        }
        TextView view = emptyView.findViewById(R.id.tv_tips);
        if (view != null) {
            view.setText(tips);
        }
    }
    public void setErrorText(String tips) {
        if(errorView == null){
            initErrorView();
        }
        TextView view = errorView.findViewById(R.id.tv_tips);
        if (view != null) {
            view.setText(tips);
        }
    }

    public Activity getActivityFromCtx(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setLoadingBackground(@DrawableRes Integer  resId) {
        if (null != rlLoading)
            rlLoading.setBackgroundResource(resId);
    }
}
