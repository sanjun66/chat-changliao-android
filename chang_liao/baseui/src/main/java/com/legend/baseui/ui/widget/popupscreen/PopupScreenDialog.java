package com.legend.baseui.ui.widget.popupscreen;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;
import com.legend.baseui.ui.widget.dialog.UIBaseDialog;

public class PopupScreenDialog extends UIBaseDialog implements PopupScreenView.OnImageLoadListener, View.OnClickListener {

    private PopupScreenView popupScreenView;

    private static final float widthRadio = 0.8f;
    private ImageView ivClose;
    private LinearLayout contentView;

    public PopupScreenDialog(Context context) {
        this(context, R.style.UI_Dialog);
    }

    public PopupScreenDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * 初始化插屏弹框
     * @param data 插屏信息
     * @param aspectRadio 宽高比（宽/高）
     */
    public void setData(IPopupScreen data, float aspectRadio) {

        int screenWidth = DensityUtil.getScreenWidth(getContext());

        int width = (int) (screenWidth * widthRadio);
        int height = (int) (width / aspectRadio);

        contentView = new LinearLayout(getContext());
        contentView.setVisibility(View.INVISIBLE);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);
        contentView.setPadding(0, 20, 0, 20);
        ViewGroup.LayoutParams contentLp = new ViewGroup.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        popupScreenView = new PopupScreenView(getContext());
        ViewGroup.LayoutParams popupLp = new ViewGroup.LayoutParams(width, height);
        popupScreenView.init(data, this);
        popupScreenView.setLayoutParams(popupLp);
        contentView.addView(popupScreenView);

        LinearLayout.MarginLayoutParams closeLp = new LinearLayout.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        closeLp.topMargin = DensityUtil.dip2px(getContext(), 25);
        ivClose = new ImageView(getContext());
        ivClose.setImageResource(R.drawable.ui_popup_close);
        ivClose.setLayoutParams(closeLp);
        ivClose.setOnClickListener(this);
        contentView.addView(ivClose);

        addContentView(contentView, contentLp);
    }

    public void setListener(PopupScreenView.OnAreaClickListener listener) {
        if (popupScreenView != null) {
            popupScreenView.setOnAreaClickListener(listener);
        }
    }

    /**
     * 设置是高亮可点击区域 （注意：该方法仅可以在测试时使用！！！）
     *
     * @param show 是否高亮显示
     */
    public void showClickArea(boolean show) {
        if (popupScreenView != null) {
            popupScreenView.showClickArea(show);
        }
    }

    /**
     * 广告图加载成功后回调
     */
    @Override
    public void onLoadSuccess() {

        contentView.setVisibility(View.VISIBLE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(contentView,
                "scaleX", 0.8f, 1.03f, 0.99f, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(contentView,
                "scaleY", 0.8f, 1.03f, 0.99f, 1);

        AnimatorSet endAnimSet = new AnimatorSet();
        endAnimSet.playTogether(scaleX, scaleY);
        endAnimSet.setInterpolator(new LinearInterpolator());
        endAnimSet.setDuration(600);
        endAnimSet.start();
    }

    @Override
    public void onLoadFailed() {
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v == ivClose) {
            dismiss();
        }
    }
}
