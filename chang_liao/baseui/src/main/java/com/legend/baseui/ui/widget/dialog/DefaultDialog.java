package com.legend.baseui.ui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;


/**
 * Created by niuzj on 2018/11/27
 * description:
 */
public class DefaultDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    //文案
    private String title;
    private String content;
    private String leftTitle;
    private String rightTitle;
    //颜色
    private String titleColor;
    private String contentColor;
    private String leftColor;
    private String rightColor;
    //文字大小
    private int titleSize;
    private int contentSize;
    private int leftSize;
    private int rightSize;
    //内容行间距 倍数
    private float contentLineSpace;
    //content距左右margin  dp
    private int contentHoriMargin;
    //content距上边margin  dp
    private int contentVertiMarginTop;
    //点击事件
    private OnDialogClickListener mOnDialogClickListener;
    //弹窗占屏幕宽度的比例
    private float widthRatio;
    //背景
    private int resId = -1;
    //内容位置
    private int contentGravity;
    //点击外部是否可取消
    private boolean mCancelOnTouchOutside;

    //标题加粗
    private boolean titleBold;


    public DefaultDialog(@NonNull Context context, Builder builder) {
        super(context, R.style.ui_Dialog);
        mContext = context;
        title = builder.title;
        content = builder.content;
        leftTitle = builder.leftTitle;
        rightTitle = builder.rightTitle;
        titleColor = builder.titleColor;
        contentColor = builder.contentColor;
        leftColor = builder.leftColor;
        rightColor = builder.rightColor;
        titleSize = builder.titleSize;
        contentSize = builder.contentSize;
        leftSize = builder.leftSize;
        rightSize = builder.rightSize;
        mOnDialogClickListener = builder.mOnDialogClickListener;
        widthRatio = builder.widthRatio;
        contentGravity = builder.contentGravity;
        resId = builder.resId;
        mCancelOnTouchOutside = builder.mCancelOnTouchOutside;
        contentLineSpace = builder.contentLineSpace;
        contentHoriMargin = builder.contentHoriMargin;
        contentVertiMarginTop = builder.contentVertiMarginTop;
        titleBold = builder.titleBold;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();

        initAttr();

    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ui_default_dialog_layout, null);
        setContentView(view);

        if (resId > 0) {
            view.setBackgroundResource(resId);
        }

        //标题
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
            setTextColor(titleColor, tvTitle);
            setTextSize(titleSize, tvTitle);
            if (titleBold)
                tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        }
        //内容
        final TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
        if (TextUtils.isEmpty(title)) {
            LinearLayout.LayoutParams tvContentLayoutParams = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
            tvContentLayoutParams.topMargin = 0;
            tvContent.setLayoutParams(tvContentLayoutParams);
        }
        if (TextUtils.isEmpty(content)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setGravity(contentGravity);
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(Html.fromHtml(content));
            setTextColor(contentColor, tvContent);
            if (contentSize <= 0) {
                if (TextUtils.isEmpty(title)) {
                    contentSize = 16;
                } else {
                    contentSize = 14;
                }
            }
            setTextSize(contentSize, tvContent);
            if (contentLineSpace != 0)
                tvContent.setLineSpacing(0, contentLineSpace);
            if (contentHoriMargin > 0) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
                layoutParams.leftMargin = DensityUtil.dip2px(mContext, contentHoriMargin);
                layoutParams.rightMargin = DensityUtil.dip2px(mContext, contentHoriMargin);
                tvContent.setLayoutParams(layoutParams);
            }

            if (contentVertiMarginTop > 0) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
                layoutParams.topMargin = DensityUtil.dip2px(mContext, contentVertiMarginTop);
                tvContent.setLayoutParams(layoutParams);
            }

        }
        //底部按钮
        TextView tvLeft = (TextView) view.findViewById(R.id.tvLeft);
        View line = view.findViewById(R.id.line);
        TextView tvRight = (TextView) view.findViewById(R.id.tvRight);
        if (!TextUtils.isEmpty(leftTitle) && !TextUtils.isEmpty(rightTitle)) {
            tvLeft.setText(leftTitle);
            setTextColor(leftColor, tvLeft);
            setTextSize(leftSize, tvLeft);
            tvLeft.setOnClickListener(this);

            tvRight.setText(rightTitle);
            setTextColor(rightColor, tvRight);
            setTextSize(rightSize, tvRight);
            tvRight.setOnClickListener(this);
        } else {
            line.setVisibility(View.GONE);
            if (TextUtils.isEmpty(leftTitle)) {
                tvLeft.setVisibility(View.GONE);
            } else {
                tvLeft.setText(leftTitle);
                setTextColor(leftColor, tvLeft);
                setTextSize(leftSize, tvLeft);
                tvLeft.setOnClickListener(this);
            }
            if (TextUtils.isEmpty(rightTitle)) {
                tvRight.setVisibility(View.GONE);
            } else {
                tvRight.setText(rightTitle);
                setTextColor(rightColor, tvRight);
                setTextSize(rightSize, tvRight);
                tvRight.setOnClickListener(this);
            }
        }
        //点击外部是否可取消
        setCanceledOnTouchOutside(mCancelOnTouchOutside);
        final LinearLayout ll_content = view.findViewById(R.id.ll_content);
        tvContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tvContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int lineCount = tvContent.getLineCount();
                if (lineCount <= 1) {
                    tvContent.setGravity(Gravity.CENTER);
                } else {
                    tvContent.setGravity(Gravity.LEFT);
                }

                int height = ll_content.getHeight();
                int dip2px_360dp = DensityUtil.dip2px(mContext, 360);
                if (height > dip2px_360dp) {
                    ViewGroup.LayoutParams layoutParams = ll_content.getLayoutParams();
                    layoutParams.height = dip2px_360dp;
                    ll_content.setLayoutParams(layoutParams);
                    tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
                }

            }
        });


    }

    private void initAttr() {
        if (widthRatio != 0.0f) {
            Window window = getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = (int) (DensityUtil.getScreenWidth(mContext) * widthRatio);
            window.setAttributes(attributes);
        }
    }

    private void setTextColor(String textColor, TextView textView) {
        if (!TextUtils.isEmpty(textColor)) {
            try {
                int color = Color.parseColor(textColor);
                textView.setTextColor(color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setTextSize(int textSize, TextView textView) {
        if (textSize > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tvLeft) {
            if (mOnDialogClickListener != null) {
                mOnDialogClickListener.clickLeft();
            }

        } else if (i == R.id.tvRight) {
            if (mOnDialogClickListener != null) {
                mOnDialogClickListener.clickRight();
            }

        }
        dismiss();
    }

    public interface OnDialogClickListener {
        void clickLeft();

        void clickRight();
    }

    public static class Builder {
        private String title;
        private String content;
        private String leftTitle;
        private String rightTitle;

        private String titleColor;
        private String contentColor;
        private String leftColor;
        private String rightColor;

        private int titleSize;
        private int contentSize;
        private int leftSize;
        private int rightSize;
        private float contentLineSpace = 1.0f;

        private int contentHoriMargin;

        private int contentVertiMarginTop;

        private int contentGravity = Gravity.LEFT;

        private OnDialogClickListener mOnDialogClickListener;

        private float widthRatio;

        private int resId;

        private boolean mCancelOnTouchOutside;

        private boolean titleBold;

        public Builder() {
            this.widthRatio = 0.861f;
        }

        public Builder contentHoriMargin(int contentHoriMargin) {
            this.contentHoriMargin = contentHoriMargin;
            return this;
        }

        public Builder contentVertiMarginTop(int contentVertiMarginTop) {
            this.contentVertiMarginTop = contentVertiMarginTop;
            return this;
        }

        public Builder contentLineSpace(float mult) {
            this.contentLineSpace = mult;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder contentGravity(int gravity) {
            this.contentGravity = gravity;
            return this;
        }

        public Builder leftTitle(String leftTitle) {
            this.leftTitle = leftTitle;
            return this;
        }

        public Builder rightTitle(String rightTitle) {
            this.rightTitle = rightTitle;
            return this;
        }

        public Builder titleColor(String titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder contentColor(String contentColor) {
            this.contentColor = contentColor;
            return this;
        }

        public Builder leftColor(String leftColor) {
            this.leftColor = leftColor;
            return this;
        }

        public Builder rightColor(String rightColor) {
            this.rightColor = rightColor;
            return this;
        }

        public Builder titleSize(int titleSize) {
            this.titleSize = titleSize;
            return this;
        }

        public Builder contentSize(int contentSize) {
            this.contentSize = contentSize;
            return this;
        }

        public Builder leftSize(int leftSize) {
            this.leftSize = leftSize;
            return this;
        }

        public Builder rightSize(int rightSize) {
            this.rightSize = rightSize;
            return this;
        }

        public Builder clickListener(OnDialogClickListener onDialogClickListener) {
            this.mOnDialogClickListener = onDialogClickListener;
            return this;
        }

        public Builder widthRatio(float widthRatio) {
            this.widthRatio = widthRatio;
            return this;
        }

        public Builder bgResId(int resId) {
            this.resId = resId;
            return this;
        }

        public Builder setCancelOnTouchOutside(boolean cancelOnTouchOutside) {
            mCancelOnTouchOutside = cancelOnTouchOutside;
            return this;
        }

        public Builder titleBold(boolean bold) {
            this.titleBold = bold;
            return this;
        }

        public DefaultDialog build(Context context) {
            return new DefaultDialog(context, this);
        }


    }

}
