package com.legend.baseui.ui.widget.titlebar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.com.legend.ui.R;
import com.legend.baseui.ui.base.TitleBarConfig;
import com.legend.baseui.ui.util.DensityUtil;
import com.bumptech.glide.Glide;

public class MTitleBar extends RelativeLayout implements View.OnClickListener {
    private ImageView leftIcon, rightIcon;
    private TextView title, rightText;

    private final Context mContext;
    private OnTitleListener listener;

    public MTitleBar(Context context) {
        this(context, null);
    }

    public MTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public void setConfig(TitleBarConfig config) {
        if (null == config)
            return;
        ViewGroup.LayoutParams layoutParams = leftIcon.getLayoutParams();
        int iconSize;
        if (null != layoutParams) {
            iconSize = DensityUtil.dip2px(getContext(), config.leftIconSize);
            layoutParams.height = iconSize;
            layoutParams.width = iconSize;
            leftIcon.setLayoutParams(layoutParams);
        }

        layoutParams = rightIcon.getLayoutParams();
        if (null != layoutParams) {
            iconSize = DensityUtil.dip2px(getContext(), config.rightIconSize);
            layoutParams.height = iconSize;
            layoutParams.width = iconSize;
            rightIcon.setLayoutParams(layoutParams);
        }
    }

    public void setListener(OnTitleListener onTitleListener) {
        this.listener = onTitleListener;
    }

    /**
     * 设置左上角icon
     * @param resId drawable res
     */
    public void setLeftIcon(@DrawableRes int resId) {
        leftIcon.setImageResource(resId);
        showLeftIcon(true);
    }

    /**
     * 设置左上角icon
     * @param drawable drawable
     */
    public void setLeftIcon(@NonNull Drawable drawable) {
        leftIcon.setImageDrawable(drawable);
        showLeftIcon(true);
    }

    /**
     * 设置左上角icon
     * @param imageUrl 图片链接
     */
    public void setLeftIcon(String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(mContext).load(imageUrl).into(leftIcon);
            showLeftIcon(true);
        }
    }

    /**
     * 设置主标题
     * @param resId string res
     */
    public void setTitle(@StringRes int resId) {
        setTitle(mContext.getResources().getString(resId));
        showTitle(true);
    }

    /**
     * 设置主标题
     * @param titleText title text
     */
    public void setTitle(String titleText) {
        title.setText(titleText == null ? "" : titleText);
        showTitle(true);
    }

    /**
     * 设置右上角icon
     * @param resId drawable res
     */
    public void setRightIcon(@DrawableRes int resId) {
        rightIcon.setImageResource(resId);
        showRightIcon(true);
    }

    /**
     * 设置右上角icon
     * @param drawable drawable
     */
    public void setRightIcon(@NonNull Drawable drawable) {
        rightIcon.setImageDrawable(drawable);
        showRightIcon(true);
    }

    /**
     * 设置右上角icon
     * @param imgUrl 图片链接
     */
    public void setRightIcon(String imgUrl) {
        if (!TextUtils.isEmpty(imgUrl)) {
            Glide.with(mContext).load(imgUrl).into(rightIcon);
        }
        showRightIcon(true);
    }

    /**
     * 设置右上角副标题
     * @param resId string res
     */
    public void setRightText(@StringRes int resId) {
        rightText.setText(resId);
        showRightTitle(true);
    }

    /**
     * 设置右上角副标题
     * @param titleText title text
     */
    public void setRightText(String titleText) {
        rightText.setText(titleText == null ? "" : titleText);
        showRightTitle(true);
    }

    // set title color

    /**
     * 设置标题颜色
     * @param color color字符串
     */
    public void setTitleColor(String color) {
        int c = Color.BLACK;
        try {
            c = Color.parseColor(color);
        } catch (Exception exp) {
            // no-op
        }
        title.setTextColor(c);
    }

    /**
     * 设置标题颜色
     * @param color color res
     */
    public void setTitleColor(@ColorInt int color) {
        title.setTextColor(color);
    }

    /**
     * 设置标题大小
     */
    public void setTitleSize(float size) {
        title.setTextSize(size);
    }

    /**
     * 设置右侧标题大小
     */
    public void setRightTitleSize(float size) {
        rightText.setTextSize(size);
    }

    /**
     * 设置右侧标题颜色
     * @param color color string
     */
    public void setRightTitleColor(String color) {
        int c = Color.BLACK;
        try {
            c = Color.parseColor(color);
        } catch (Exception exp) {
            // no-op
        }
        rightText.setTextColor(c);
    }

    /**
     * 设置标题颜色
     * @param color color res
     */
    public void setRightTitleColor(@ColorInt int color) {
        rightText.setTextColor(color);
    }

    /**
     * 设置是否显示左侧icon
     * @param show true 显示 false 隐藏
     */
    public void showLeftIcon(boolean show) {
        leftIcon.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 设置是否显示右侧icon
     * @param show true 显示 false 隐藏
     */
    public void showRightIcon(boolean show) {
        rightIcon.setVisibility(show ? VISIBLE : GONE);
        if (show) {
            rightText.setVisibility(GONE);
        }

    }

    /**
     * 设置是否显示标题
     * @param show true 显示 / false 隐藏
     */
    public void showTitle(boolean show) {
        title.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 设置是否显示右侧标题
     * @param show true 显示 / false 隐藏
     */
    public void showRightTitle(boolean show) {
        rightText.setVisibility(show ? VISIBLE : GONE);
        if (show) {
            rightIcon.setVisibility(GONE);
        }
    }

    private void init() {
        int dp48 = DensityUtil.dip2px(mContext, 48);
        int dp38 = DensityUtil.dip2px(mContext, 38);
        int dp29 = DensityUtil.dip2px(mContext, 29);
        int dp16 = DensityUtil.dip2px(mContext, 16);
        int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
        int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 标题 宽撑满 高48dp
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(MATCH_PARENT, dp48);
        setLayoutParams(params);

        // 左侧icon
        // 居左16sp 垂直居中 图片 29dp
        leftIcon = new ImageView(mContext);
        LayoutParams layoutParams = new LayoutParams(dp38, dp38);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.leftMargin = dp16;
        leftIcon.setLayoutParams(layoutParams);
        leftIcon.setOnClickListener(this);
        addView(leftIcon);

        // 中间标题
        // 居中 文字 黑色 18sp
        title = new TextView(mContext);
        layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.leftMargin = DensityUtil.dip2px(mContext, 60);
        layoutParams.rightMargin = DensityUtil.dip2px(mContext, 60);
        title.setMaxLines(1);
        title.setSingleLine();
        title.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        title.setLayoutParams(layoutParams);
        title.setTextColor(Color.parseColor("#000000"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setOnClickListener(this);
        title.setVisibility(GONE);
        addView(title);

        // 右侧文字
        // 居右16sp 垂直居中 文字 黑色 16sp
        rightText = new TextView(mContext);
        layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.rightMargin = dp16;
        rightText.setLayoutParams(layoutParams);
        rightText.setTextColor(Color.parseColor("#999999"));
        rightText.setTextSize(15);
        rightText.setOnClickListener(this);
        rightText.setVisibility(GONE);
        addView(rightText);

        // 右侧图标
        // 居右16sp 居中显示 图片29dp
        rightIcon = new ImageView(mContext);
        layoutParams = new LayoutParams(dp16, dp16);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.rightMargin = dp16;
        rightIcon.setLayoutParams(layoutParams);
        rightIcon.setOnClickListener(this);
        rightIcon.setVisibility(GONE);
        addView(rightIcon);

        // 默认展示返回按钮
        setLeftIcon(R.drawable.ui_back_nor);
    }

    @Override
    public void onClick(View view) {
        if (view == leftIcon) {
            if (null != listener) {
                listener.titleBarLeftClick();
            }
        } else if (view == rightIcon || view == rightText) {
            if (null != listener) {
                listener.titleBarRightClick();
            }
        } else if (view == title) {
            if (null != listener) {
                listener.titleBarTitleClick();
            }
        }
    }

    public interface OnTitleListener {
        void titleBarLeftClick();

        void titleBarRightClick();

        void titleBarTitleClick();
    }

    public static class OnTitleListenerWrapper implements OnTitleListener {

        @Override
        public void titleBarLeftClick() {

        }

        @Override
        public void titleBarRightClick() {

        }

        @Override
        public void titleBarTitleClick() {

        }
    }
}
