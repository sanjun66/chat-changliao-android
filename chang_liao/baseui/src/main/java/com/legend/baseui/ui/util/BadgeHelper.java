package com.legend.baseui.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import com.com.legend.ui.R;

public class BadgeHelper {
    private BadgeView badgeView;

    private int[] badgeTextSize = new int[]{13, 11, 9};
    private int width;
    private int height;
    private boolean isAlignParentRight = false;
    private int rightMargin;
    private View originView;

    public BadgeHelper(@NonNull Context context) {
        width = height = DensityUtil.dip2px(context, 20);
    }

    public void bind(View view) {
        bind(view, -1);
    }

    public void bind(View view, @ColorInt int color) {
        if (null == view)
            return;
        ViewParent parent = view.getParent();
        RelativeLayout parentView;
        if (parent instanceof RelativeLayout) {
            parentView = (RelativeLayout) parent;
        } else {
            throw new IllegalArgumentException("view parent must is RelativeLayout");
        }

        int id = view.getId();
        if (id <= 0) {
            throw new IllegalArgumentException("view must set id");
        }

        originView = view;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        int[] rules = layoutParams.getRules();

        if (null != rules && rules.length > RelativeLayout.ALIGN_PARENT_RIGHT) {
            if (rules[RelativeLayout.ALIGN_PARENT_RIGHT] == -1) {
                isAlignParentRight = true;
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.ui_badge_view);
            }
        }
        rightMargin = layoutParams.rightMargin;
        int topMargin = layoutParams.topMargin;
        layoutParams.rightMargin = 0;
        view.setLayoutParams(layoutParams);


        layoutParams = new RelativeLayout.LayoutParams(width, height);
        if (isAlignParentRight) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            layoutParams.addRule(RelativeLayout.RIGHT_OF, id);
        }
        badgeView = new BadgeView(view.getContext(), color);
        badgeView.setId(R.id.ui_badge_view);
        badgeView.setLayoutParams(layoutParams);
        layoutParams.topMargin = topMargin;
        layoutParams.rightMargin = rightMargin;
        parentView.addView(badgeView);

        setBadge(0);
    }

    public void setBadgeMarginRight(int margin) {
        if (null == badgeView)
            return;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) badgeView.getLayoutParams();
        layoutParams.rightMargin = margin;
        badgeView.setLayoutParams(layoutParams);
    }

    public void setBadgeMarginTop(int margin) {
        if (null == badgeView)
            return;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) badgeView.getLayoutParams();
        layoutParams.topMargin = margin;
        badgeView.setLayoutParams(layoutParams);
    }

    public void setBadgeMarginLeft(int margin) {
        if (null == badgeView)
            return;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) badgeView.getLayoutParams();
        layoutParams.leftMargin = margin;
        badgeView.setLayoutParams(layoutParams);
    }

    public void setBadgeMarginBottom(int margin) {
        if (null == badgeView)
            return;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) badgeView.getLayoutParams();
        layoutParams.bottomMargin = margin;
        badgeView.setLayoutParams(layoutParams);
    }


    public void modifyBadgeSize(int width, int height) {
        modifyBadgeSize(width, height, badgeTextSize);
    }


    public void showMediumBadge(Context context) {
        int dp15 = DensityUtil.dip2px(context, 15);
        modifyBadgeSize(dp15, dp15, new int[]{11, 9, 7});
    }

    public void modifyBadgeSize(int width, int height, int[] badgeTextSize) {
        if (null == badgeView)
            return;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) badgeView.getLayoutParams();
        layoutParams.width = this.width = width;
        layoutParams.height = this.height = height;

        if (null == badgeTextSize || badgeTextSize.length != 3) {
            return;
        }

        CharSequence text = badgeView.getText();
        if (!TextUtils.isEmpty(text)) {
            int l = text.length();
            if (l > 2)
                badgeView.setTextSize(badgeTextSize[2]);
            else if (l > 1)
                badgeView.setTextSize(badgeTextSize[1]);
            else
                badgeView.setTextSize(badgeTextSize[0]);
        }
        this.badgeTextSize = badgeTextSize;
    }


    public void setBadge(String text) {
        if (null == badgeView)
            return;

        if (TextUtils.isEmpty(text)) {
            badgeView.setVisibility(View.GONE);
            if (isAlignParentRight) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) originView.getLayoutParams();
                layoutParams.rightMargin = rightMargin;
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                originView.setLayoutParams(layoutParams);
            }
        } else {
            badgeView.setVisibility(View.VISIBLE);
            if (isAlignParentRight) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) originView.getLayoutParams();
                layoutParams.rightMargin = 0;
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                originView.setLayoutParams(layoutParams);
            }

            int l = text.length();
            if (l > 2)
                badgeView.setTextSize(badgeTextSize[2]);
            else if (l > 1)
                badgeView.setTextSize(badgeTextSize[1]);
            else
                badgeView.setTextSize(badgeTextSize[0]);

            badgeView.setText(text);
        }
    }

    public void setBadge(int number) {
        if (number <= 0) {
            setBadge(null);
        } else {
            String sNumber;
            if (number > 99)
                sNumber = "99+";
            else
                sNumber = String.valueOf(number);
            setBadge(sNumber);
        }
    }

    private class BadgeView extends AppCompatTextView {

        public BadgeView(Context context) {
            this(context, -1);
        }

        public BadgeView(Context context, @ColorInt int color) {
            super(context);
            initView(color);
        }

        private void initView(@ColorInt int color) {
            setTextColor(getResources().getColor(R.color.ui_tc_white));
            setBackgroundResource(R.color.ui_tc_light_red);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(DensityUtil.dip2px(getContext(), 32));
            if (color > 0)
                drawable.setColor(color);
            else
                drawable.setColor(Color.parseColor("#FB3B4B"));
            setBackground(drawable);

            setGravity(Gravity.CENTER);
            setLines(1);
            setEllipsize(TextUtils.TruncateAt.END);
        }
    }
}
