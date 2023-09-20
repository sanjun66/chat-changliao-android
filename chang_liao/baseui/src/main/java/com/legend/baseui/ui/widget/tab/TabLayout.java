package com.legend.baseui.ui.widget.tab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.legend.baseui.ui.util.DensityUtil;

public class TabLayout extends LinearLayout implements View.OnClickListener {
    private int selectPosition = -1;

    private OnTabClickListener listener;

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {
        setOrientation(HORIZONTAL);
        setPadding(0, DensityUtil.dip2px(getContext(), 5), 0, 0);
    }

    public void addTab(ITab iTab) {
        if (null == iTab || !(iTab instanceof View)) {
            throw new IllegalArgumentException("iTab must extends View and not null");
        }
        View view = (View) iTab;
        LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        view.setLayoutParams(layoutParams);
        iTab.setPosition(getChildCount());
        addView(view);
        view.setOnClickListener(this);
    }

    public void setSelectPosition(int position) {
        if (this.selectPosition == position)
            return;

        View v = getChildAt(position);
        if (v instanceof ITab) {
            ((ITab) v).setState(true);

            v = getChildAt(selectPosition);
            if (v instanceof ITab) {
                ((ITab) v).setState(false);
            }

            if (null != listener) {
                listener.onTabItemClick(position, selectPosition);
            }

            this.selectPosition = position;
        }
    }

    public void setTabClickListener(OnTabClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof ITab) {
            int position = ((ITab) view).getPosition();
            if (null != listener && !listener.onTabItemClick(position, selectPosition)) {
                return;
            }

            View v = getChildAt(selectPosition);
            if (v instanceof ITab) {
                ((ITab) v).setState(false);
            }
            ((ITab) view).setState(true);
            selectPosition = position;
        }
    }

    public interface OnTabClickListener {
        /**
         * tab点击事件回调
         * @param position 点击item的position
         * @param lastPosition 上一次选中item的position
         * @return
         * true 点击生效, 改变item的选中状态
         * false 点击不生效, 不改变item的选中状态
         */
        boolean onTabItemClick(int position, int lastPosition);
    }
}
