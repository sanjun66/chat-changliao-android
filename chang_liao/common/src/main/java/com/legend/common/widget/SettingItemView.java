package com.legend.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.legend.baseui.ui.util.DisplayUtils;
import com.legend.common.widget.switchbutton.SwitchButton;
import com.legend.commonres.R;

/**
 * @Description:
 * @Author: bingyan
 * @CreateDate: 2022/4/28 18:08
 */
public class SettingItemView extends LinearLayout {
    private Context mContext;
    private LinearLayout lltItem;
    private ImageView imgIcon;
    private TextView tvContent;
    private LinearLayout lltTips;
    private TextView tvTips;
    private ImageView imgArrow;

    private SwitchButton sbSwitch;
    private ImageView imgRedDot;
    private View viewLine;

    private CompoundButton.OnCheckedChangeListener checkedListener;

    public SettingItemView(Context context) {
        this(context, null);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray mTypedArray = mContext.obtainStyledAttributes(attrs, R.styleable.setting_item_view);
        if (mTypedArray == null) return;

        int itemHeight = mTypedArray.getDimensionPixelOffset(R.styleable.setting_item_view_item_height, 0);
        if (itemHeight != 0) {
            LayoutParams itemLp = (LayoutParams) lltItem.getLayoutParams();
            itemLp.height = itemHeight;
            lltItem.setLayoutParams(itemLp);
        }

        int leftIconWidth = mTypedArray.getDimensionPixelOffset(R.styleable.setting_item_view_left_icon_width, 0);
        int leftIconHeight = mTypedArray.getDimensionPixelOffset(R.styleable.setting_item_view_left_icon_height, 0);
        if (leftIconWidth > 0 || leftIconHeight > 0) {
            LayoutParams iconLp = (LayoutParams) imgIcon.getLayoutParams();
            if (leftIconWidth > 0) iconLp.width = leftIconWidth;
            if (leftIconHeight > 0) iconLp.height = leftIconHeight;
            imgIcon.setLayoutParams(iconLp);
        }

        LayoutParams contentLp = (LayoutParams) tvContent.getLayoutParams();
        int iconRes = mTypedArray.getResourceId(R.styleable.setting_item_view_icon_res, 0);
        if (iconRes != 0) {
            imgIcon.setVisibility(VISIBLE);
            imgIcon.setImageResource(iconRes);
            contentLp.setMargins(DisplayUtils.dp2px(mContext, 15), 0, 0, 0);
        } else {
            imgIcon.setVisibility(GONE);
            contentLp.setMargins(DisplayUtils.dp2px(mContext, 20), 0, 0, 0);
        }

        String content = mTypedArray.getString(R.styleable.setting_item_view_content_text);
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        }
        tvContent.setLayoutParams(contentLp);

        float contetnTextSize = mTypedArray.getDimension(R.styleable.setting_item_view_content_text_size, 0f);
        if (contetnTextSize != 0) {
            tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, contetnTextSize);
        }

        if(!mTypedArray.getBoolean(R.styleable.setting_item_view_content_text_bold, false)) {
            tvContent.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        } else {
            tvContent.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        int contentTextColor = mTypedArray.getColor(R.styleable.setting_item_view_content_text_color, 0);
        if (contentTextColor > 0) {
            tvContent.setTextColor(contentTextColor);
        }

        String tips = mTypedArray.getString(R.styleable.setting_item_view_tips_text);
        if (TextUtils.isEmpty(tips)) {
            tvTips.setVisibility(GONE);
        } else {
            tvTips.setVisibility(VISIBLE);
            tvTips.setText(tips);
        }

        int arrowVisibility = mTypedArray.getInt(R.styleable.setting_item_view_arrow_visibility, View.VISIBLE);
        imgArrow.setVisibility(arrowVisibility);

        int arrowRes = mTypedArray.getResourceId(R.styleable.setting_item_view_arrow_res, 0);
        if (arrowRes != 0) {
            imgArrow.setImageResource(arrowRes);
        }
        int switchBtnVisibility = mTypedArray.getInt(R.styleable.setting_item_view_switch_btn_visibility, View.GONE);
        sbSwitch.setVisibility(switchBtnVisibility);

        int leftIconVisibility = mTypedArray.getInt(R.styleable.setting_item_view_left_icon_visibility, View.VISIBLE);
        imgIcon.setVisibility(leftIconVisibility);

        LayoutParams tipsLp = (LayoutParams) lltTips.getLayoutParams();
        if(arrowVisibility == GONE) {
            tipsLp.setMargins(0, 0, DisplayUtils.dp2px(mContext, 20), 0);
        } else {
            tipsLp.setMargins(0, 0, DisplayUtils.dp2px(mContext, 6), 0);
        }
        lltTips.setLayoutParams(tipsLp);

        int lineVisibility = mTypedArray.getInt(R.styleable.setting_item_view_line_visibility, View.GONE);
        viewLine.setVisibility(lineVisibility);

        mTypedArray.recycle();

    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_setting_item, this, true);
        lltItem = findViewById(R.id.llt_item);
        imgIcon = findViewById(R.id.img_icon);
        tvContent = findViewById(R.id.tv_content);
        lltTips = findViewById(R.id.llt_tips);
        tvTips = findViewById(R.id.tv_tips);
        imgArrow = findViewById(R.id.img_arrow);
        sbSwitch = findViewById(R.id.sb_switch);
        imgRedDot = findViewById(R.id.img_red_dot);
        viewLine = findViewById(R.id.view_line);
    }


    public void setTips(String msg) {
        if (tvTips != null) {
            if (!TextUtils.isEmpty(msg)) tvTips.setVisibility(VISIBLE);
            tvTips.setText(msg);
        }
    }

    public void setContent(String content) {
        if (tvContent == null) return;
        tvContent.setText(content);
    }

    public void setRedDotVisible(boolean visible) {
        if (imgRedDot != null) imgRedDot.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置 switch 按钮选择状态
     *
     * @param isChecked
     */
    public void setChecked(boolean isChecked) {
        sbSwitch.setChecked(isChecked);
    }

    /**
     * 设置 switch 按钮选择状态,不触发选中事件
     *
     * @param isChecked
     */
    public void setCheckedWithOutEvent(boolean isChecked) {
        sbSwitch.setOnCheckedChangeListener(null);
        sbSwitch.setChecked(isChecked);
        sbSwitch.setOnCheckedChangeListener(checkedListener);
    }

    /**
     * 立即设置 switch 按钮选择状态，没有动画
     *
     * @param isChecked
     */
    public void setCheckedImmediately(boolean isChecked) {
        sbSwitch.setCheckedImmediately(isChecked);
    }

    /**
     * 立即设置 switch 按钮选择状态，没有动画,不触发选中事件
     *
     * @param isChecked
     */
    public void setCheckedImmediatelyWithOutEvent(boolean isChecked) {
        sbSwitch.setOnCheckedChangeListener(null);
        sbSwitch.setCheckedImmediately(isChecked);
        sbSwitch.setOnCheckedChangeListener(checkedListener);
    }

    /** 获取当前 switch 状态 */
    public boolean isChecked() {
        return sbSwitch.isChecked();
    }

    /**
     * 设置 switch 按钮选择监听
     *
     * @param listener
     */
    public void setSwitchCheckListener(CompoundButton.OnCheckedChangeListener listener) {
        checkedListener = listener;
        sbSwitch.setOnCheckedChangeListener(checkedListener);
    }
}
