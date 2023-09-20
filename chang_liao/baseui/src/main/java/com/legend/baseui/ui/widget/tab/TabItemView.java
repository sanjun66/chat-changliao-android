package com.legend.baseui.ui.widget.tab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;
import com.bumptech.glide.Glide;

public class TabItemView extends LinearLayout implements ITab {
    private ImageView ivUnCheckIcon;
    private ImageView ivCheckIcon;
    private LottieAnimationView lottieView;
    private TextView tvTitle;
    private RelativeLayout iconContainer;

    private int defaultIconSize;
    private int bigIconSize;
    private int tabFlag = 0;
    private int INIT = 2;
    private int CHECK_ICON_SHOW = 4;
    private int UNCHECK_ICON_SHOW = 8;
    private int LOTTIE_SHOW = 16;

    private int position;


    private static final String sSelectedColor = "#16D2D7";
    private static final String sNormalColor = "#999999";

    private int iSelectedColor;
    private int iNormalColor;

    private ImageView chips;
    private BadgeView badgeView;

    public TabItemView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        defaultIconSize = DensityUtil.dip2px(getContext(), 22);
        bigIconSize = DensityUtil.dip2px(getContext(), 60);

        setOrientation(VERTICAL);

        iconContainer = new RelativeLayout(getContext());
        LayoutParams params = new LayoutParams(defaultIconSize + DensityUtil.dip2px(getContext(), 8), defaultIconSize);
        params.topMargin = DensityUtil.dip2px(getContext(), 7);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        iconContainer.setLayoutParams(params);

        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(defaultIconSize, defaultIconSize);
        rp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ivUnCheckIcon = new ImageView(getContext());
        ivUnCheckIcon.setLayoutParams(rp);
        ivUnCheckIcon.setVisibility(GONE);
        iconContainer.addView(ivUnCheckIcon);


        ivCheckIcon = new ImageView(getContext());
        ivCheckIcon.setLayoutParams(rp);
        ivCheckIcon.setVisibility(GONE);
        iconContainer.addView(ivCheckIcon);

        lottieView = new LottieAnimationView(getContext());
        lottieView.setLayoutParams(rp);
        lottieView.setVisibility(GONE);
        iconContainer.addView(lottieView);


        chips = new ImageView(getContext());
        rp = new RelativeLayout.LayoutParams(DensityUtil.dip2px(getContext(), 10), DensityUtil.dip2px(getContext(), 10));
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        chips.setLayoutParams(rp);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius((float) DensityUtil.dip2px(this.getContext(), 32.0F));
        drawable.setColor(Color.parseColor("#FB3B4B"));
        chips.setImageDrawable(drawable);
        chips.setVisibility(GONE);
        iconContainer.addView(chips);


        rp = new RelativeLayout.LayoutParams(DensityUtil.dip2px(getContext(), 14), DensityUtil.dip2px(getContext(), 14));
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        badgeView = new BadgeView(getContext());
        badgeView.setId(com.com.legend.ui.R.id.ui_badge_view);
        badgeView.setLayoutParams(rp);
        badgeView.setVisibility(GONE);
        iconContainer.addView(badgeView);

        addView(iconContainer);


        tvTitle = new TextView(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTitle.setLayoutParams(layoutParams);
        tvTitle.setTextSize(10);
        tvTitle.setGravity(Gravity.CENTER);
        addView(tvTitle);
    }

    public void init(ITabInfo info) {
        if (null == info)
            throw new IllegalArgumentException("ITabInfo is null");

        tabFlag = tabFlag | INIT;

        String title = info.getText();
        String checkIconUrl = info.getCheckIconUrl();
        String uncheckIconUrl = info.getUncheckIconUrl();
        String animationUrl = info.getAnimationUrl();

        int iconSize;
        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(GONE);
            iconSize = bigIconSize;
        } else {
            tvTitle.setVisibility(VISIBLE);
            iconSize = defaultIconSize;
            tvTitle.setText(title);
        }

        ViewGroup.LayoutParams layoutParams = iconContainer.getLayoutParams();
        layoutParams.height = iconSize;
        layoutParams.width = iconSize + DensityUtil.dip2px(getContext(), 8);
        iconContainer.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams params;

        if (!TextUtils.isEmpty(checkIconUrl)) {

            tabFlag = tabFlag | CHECK_ICON_SHOW;

            params = ivCheckIcon.getLayoutParams();
            params.height = iconSize;
            params.width = iconSize;
            ivCheckIcon.setLayoutParams(params);

            Glide.with(getContext()).load(checkIconUrl).fitCenter().override(iconSize, iconSize).into(ivCheckIcon);
        }

        if (!TextUtils.isEmpty(uncheckIconUrl)) {

            tabFlag = tabFlag | UNCHECK_ICON_SHOW;

            params = ivUnCheckIcon.getLayoutParams();
            params.height = iconSize;
            params.width = iconSize;
            ivUnCheckIcon.setLayoutParams(params);

            ivUnCheckIcon.setVisibility(VISIBLE);

            Glide.with(getContext()).load(uncheckIconUrl).fitCenter().override(iconSize, iconSize).into(ivUnCheckIcon);
        }

        if (!TextUtils.isEmpty(animationUrl)) {

            tabFlag = tabFlag | LOTTIE_SHOW;

            params = lottieView.getLayoutParams();
            params.height = iconSize;
            params.width = iconSize;
            lottieView.setLayoutParams(params);

            lottieView.addLottieOnCompositionLoadedListener(composition -> lottieView.pauseAnimation());
            lottieView.setAnimationFromUrl(animationUrl);
        }

        iSelectedColor = parseColor(info.getSelectTitleColor(), sSelectedColor);
        iNormalColor = parseColor(info.getNormalTitleColor(), sNormalColor);
    }

    public void setChipsVisibility(boolean visibility) {
        chips.setVisibility(visibility ? VISIBLE : GONE);
    }

    public void setBadge(int num) {
        if (num <= 0) {
            badgeView.setVisibility(GONE);
        } else {
            badgeView.setVisibility(VISIBLE);
            String numText;
            if (num > 99) {
                numText = "99";
                badgeView.setTextSize(6);
            } else {
                if (num >= 10)
                    badgeView.setTextSize(8);
                else
                    badgeView.setTextSize(10);

                numText = String.valueOf(num);
            }
            badgeView.setText(numText);
        }
    }

    private int parseColor(String textColor, String defColor) {
        if (!TextUtils.isEmpty(textColor)) {
            try {
                return Color.parseColor(textColor);
            } catch (Exception exp) {
                return Color.parseColor(defColor);
            }
        } else {
            return Color.parseColor(defColor);
        }
    }

    @Override
    public void setState(boolean check) {
        if ((tabFlag & INIT) != INIT)
            throw new IllegalArgumentException("not init");

        LottieComposition composition = lottieView.getComposition();
        if (check) {
            tvTitle.setTextColor(iSelectedColor);
            if (null != composition) {
                ivCheckIcon.setVisibility(GONE);
                ivUnCheckIcon.setVisibility(GONE);
                lottieView.setVisibility(VISIBLE);
                lottieView.playAnimation();
            } else {
                ivCheckIcon.setVisibility(VISIBLE);
                ivUnCheckIcon.setVisibility(GONE);
                lottieView.setVisibility(GONE);
            }
        } else {
            tvTitle.setTextColor(iNormalColor);
            ivCheckIcon.setVisibility(GONE);
            if (null != composition) {
                ivUnCheckIcon.setVisibility(GONE);
                lottieView.setVisibility(VISIBLE);
                lottieView.pauseAnimation();
            } else {
                ivUnCheckIcon.setVisibility(VISIBLE);
                lottieView.setVisibility(GONE);
            }
        }
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int getPosition() {
        return position;
    }

    private class BadgeView extends AppCompatTextView {
        public BadgeView(Context context) {
            super(context);
            initView();
        }

        private void initView() {
            setTextColor(getResources().getColor(R.color.ui_tc_white));
            setBackgroundResource(R.color.ui_fff28187);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(DensityUtil.dip2px(getContext(), 32));
            drawable.setColor(Color.parseColor("#FB3B4B"));
            setBackground(drawable);

            setTextSize(10);
            setGravity(Gravity.CENTER);
            setLines(1);
            setEllipsize(TextUtils.TruncateAt.END);
        }
    }
}
