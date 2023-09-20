package com.legend.baseui.ui.widget.dialog.edit.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;
import com.legend.baseui.ui.widget.dialog.bean.DialogButtonBean;
import java.util.List;

public class EditDialogView extends LinearLayout {
    private TextView title, tips;
    private EditText editText;
    private EditCallback iCall;

    public EditDialogView(Context context) {
        super(context);

        initView();
    }

    private void initView() {
        setOrientation(VERTICAL);

        title = new TextView(getContext());
        title.setTextColor(getResources().getColor(R.color.ui_tc_black));
        title.setTextSize(16);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setSingleLine();
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 50));
        title.setLayoutParams(layoutParams);
        addView(title);


        tips = new TextView(getContext());
        tips.setGravity(Gravity.CENTER);
        tips.setTextSize(12);
        tips.setTextColor(getResources().getColor(R.color.ui_tc_grey));
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = DensityUtil.dip2px(getContext(), 10);
        layoutParams.leftMargin = DensityUtil.dip2px(getContext(), 16);
        layoutParams.rightMargin = DensityUtil.dip2px(getContext(), 16);
        tips.setLayoutParams(layoutParams);
        addView(tips);

        editText = new EditText(getContext());
        editText.setBackgroundResource(R.drawable.ui_input_bg);
        editText.setTextSize(14);
        editText.setPadding(DensityUtil.dip2px(getContext(), 5), editText.getPaddingTop(), DensityUtil.dip2px(getContext(), 5), editText.getPaddingBottom());
        editText.setMinWidth(DensityUtil.dip2px(getContext(), 20));
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = DensityUtil.dip2px(getContext(), 16);
        layoutParams.rightMargin = DensityUtil.dip2px(getContext(), 16);
        layoutParams.bottomMargin = DensityUtil.dip2px(getContext(), 10);
        editText.setLayoutParams(layoutParams);
        addView(editText);
    }

    public void setCallback(EditCallback callback) {
        this.iCall = callback;
    }

    public void setButton(List<DialogButtonBean> buttonList) {
        if (null == buttonList || buttonList.size() <= 0)
            return;

        changeButtonData(buttonList);

        View line = new View(getContext());
        line.setBackgroundColor(getResources().getColor(R.color.ui_line_bg));
        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 0.5f)));
        addView(line);

        LinearLayout buttonContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 44));
        buttonContainer.setLayoutParams(params);

        int length = buttonList.size();
        int btnHeight = DensityUtil.dip2px(getContext(), 44);
        GradientDrawable mDrawable;
        TextView btnItemView;
        for (int i = 0; i < length; i++) {
            final DialogButtonBean button = buttonList.get(i);
            if (null == button || TextUtils.isEmpty(button.title))
                continue;
            btnItemView = new TextView(getContext());
            btnItemView.setText(button.title);
            btnItemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            if (null != button.background) {
                mDrawable = new GradientDrawable();
                mDrawable.setShape(GradientDrawable.RECTANGLE);
                if (!TextUtils.isEmpty(button.background.color))
                    mDrawable.setColor(Color.parseColor(button.background.color));
                if (button.background.radius > 0)
                    mDrawable.setCornerRadius(button.background.radius);
                if (button.background.strokeWidth > 0 && !TextUtils.isEmpty(button.background.strokeColor))
                    mDrawable.setStroke(button.background.strokeWidth, Color.parseColor(button.background.strokeColor));
                btnItemView.setBackground(mDrawable);
            }

            if (TextUtils.isEmpty(button.color))
                btnItemView.setTextColor(Color.parseColor("#333333"));
            else
                btnItemView.setTextColor(Color.parseColor(button.color));

            params = new LinearLayout.LayoutParams(0, btnHeight);

            params.weight = 1;
            params.gravity = Gravity.CENTER;
            btnItemView.setGravity(Gravity.CENTER);
            btnItemView.setLayoutParams(params);
            btnItemView.setOnClickListener(v -> {
                if (TextUtils.equals(button.action, DialogButtonBean.ACTION_CONFIRM)) {
                    if (null != iCall)
                        iCall.confirm();
                } else if (TextUtils.equals(button.action, DialogButtonBean.ACTION_LINK)) {
                    if (null != iCall)
                        iCall.link(button.gotoUrl);
                } else if (TextUtils.equals(button.action, DialogButtonBean.ACTION_CANCEL)) {
                    if (null != iCall)
                        iCall.cancel();
                }
            });
            buttonContainer.addView(btnItemView);

            if (i + 1 != length) {
                View view = new View(getContext());
                view.setBackgroundColor(getResources().getColor(R.color.ui_line_bg));
                view.setLayoutParams(new LinearLayout.LayoutParams(DensityUtil.dip2px(getContext(), 0.5f), DensityUtil.dip2px(getContext(), 44)));
                buttonContainer.addView(view);
            }
        }

        addView(buttonContainer);
    }

    public void bindData(String title, String tips, String editHint) {
        if (TextUtils.isEmpty(title)) {
            this.title.setVisibility(GONE);
        } else {
            this.title.setVisibility(VISIBLE);
            this.title.setText(title);
        }


        if (TextUtils.isEmpty(tips)) {
            this.tips.setVisibility(GONE);
        } else {
            this.tips.setVisibility(VISIBLE);
            this.tips.setText(tips);
        }

        if (!TextUtils.isEmpty(editHint))
            editText.setHint(editHint);
    }

    public String getEditText() {
        return null == editText ? "" : editText.getText().toString();
    }


    private void changeButtonData(List<DialogButtonBean> buttonList) {
        for (DialogButtonBean buttonBean : buttonList) {
            if (null == buttonBean)
                continue;
            if (TextUtils.isEmpty(buttonBean.color))
                buttonBean.color = "#0076ff";
        }
    }

    public interface EditCallback {
        void cancel();

        void confirm();

        void link(String link);
    }
}
