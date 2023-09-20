package com.legend.baseui.ui.widget.dialog.permission;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;

public class PerRequestPermissionItemView extends RelativeLayout {

    private ImageView ivIcon;
    private TextView tvPermissionName;
    private TextView tvDescription;

    public PerRequestPermissionItemView(Context context) {
        super(context);

        initView();
    }

    private void initView() {
        int dp16 = DensityUtil.dip2px(getContext(), 16);
        LayoutParams layoutParams = new RelativeLayout.LayoutParams(dp16, dp16);
        layoutParams.rightMargin = DensityUtil.dip2px(getContext(), 5);
        layoutParams.topMargin = DensityUtil.dip2px(getContext(), 2);
        ivIcon = new ImageView(getContext());
        ivIcon.setId(R.id.ui_permission_icon);
        ivIcon.setLayoutParams(layoutParams);


        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(getContext(), 16));
        layoutParams.addRule(RelativeLayout.END_OF, R.id.ui_permission_icon);
        tvPermissionName = new TextView(getContext());
        tvPermissionName.setId(R.id.ui_permission_name);
        tvPermissionName.setSingleLine();
        tvPermissionName.setTextSize(14);
        tvPermissionName.setGravity(Gravity.CENTER_VERTICAL);
        tvPermissionName.setTextColor(getResources().getColor(R.color.ui_black_2b2e34));
        tvPermissionName.setLayoutParams(layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.END_OF, R.id.ui_permission_icon);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.ui_permission_name);
        layoutParams.topMargin = DensityUtil.dip2px(getContext(), 10);
        tvDescription = new TextView(getContext());
        tvDescription.setId(R.id.ui_permission_description);
        tvDescription.setTextSize(11);
        tvDescription.setTextColor(Color.parseColor("#AAAABB"));
        tvDescription.setLayoutParams(layoutParams);

        addView(ivIcon);
        addView(tvPermissionName);
        addView(tvDescription);
    }

    public void bindData(PerRequestPermissionConstant.PerRequestItem item) {
        if (null == item) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        ivIcon.setBackgroundResource(item.resIcon);
        tvPermissionName.setText(item.permissionName);
        tvDescription.setText(item.description);
    }
}
