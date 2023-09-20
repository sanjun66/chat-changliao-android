package com.legend.baseui.ui.widget.dialog.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.legend.baseui.ui.CommonUIConfig;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;
import com.legend.baseui.ui.util.UiUtils;
import com.bumptech.glide.Glide;

public class CommonDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private TextView tvTitle, tvSubTitle, tvContent, tvTips;
    private ImageView picImg, ivClose;
    private LinearLayout llBtnContainer;
    private Callback iCall;
    private SpecialCodeCallback mSpecialCodeCallback;
    private CommonDialogBean dialogBean;
    private View btnLine, titleContainer, contentContainer;

    public void setSpecialCodeCallback(SpecialCodeCallback specialCodeCallback) {
        mSpecialCodeCallback = specialCodeCallback;
    }

    public CommonDialog(@NonNull Context context, Callback callback, @NonNull CommonDialogBean dialogBean) {
        super(context, R.style.ui_Dialog);
        mContext = context;
        iCall = callback;
        this.dialogBean = dialogBean;

        setCanceledOnTouchOutside(false);

        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ui_common_button_dialog, null);
        setContentView(view);

        titleContainer = view.findViewById(R.id.title_container);
        contentContainer = view.findViewById(R.id.content_container);
        tvTitle = view.findViewById(R.id.title);
        picImg = view.findViewById(R.id.iv_picImg);
        tvContent = view.findViewById(R.id.tv_content);
        tvTips = view.findViewById(R.id.iv_tips);
        llBtnContainer = view.findViewById(R.id.button_container);
        ivClose = view.findViewById(R.id.iv_close);
        tvSubTitle = view.findViewById(R.id.sub_title);
        btnLine = view.findViewById(R.id.btnLine);

        registerEvent();

        initData(view);
    }

    private void registerEvent() {
        ivClose.setOnClickListener(this);
    }

    private void initData(final View rootView) {
        changeButtonData(dialogBean);

        ivClose.setVisibility(dialogBean.showCloseIcon ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(dialogBean.title)) {
            tvTitle.setText(dialogBean.title);
            tvTitle.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(dialogBean.subTitle)) {
            tvSubTitle.setText(dialogBean.subTitle);
            tvSubTitle.setVisibility(View.VISIBLE);
        } else {
            tvSubTitle.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(dialogBean.title) && TextUtils.isEmpty(dialogBean.subTitle)) {
            titleContainer.setVisibility(View.INVISIBLE);
        } else {
            titleContainer.setVisibility(View.VISIBLE);
        }

        double widthRatio;
        if (!TextUtils.isEmpty(dialogBean.picUrl)) {
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    int width = rootView.getWidth();
                    final String thumbnailUrl = dialogBean.picUrl;
                    int[] pixel = UiUtils.parsedPixelFromUrl(thumbnailUrl);
                    int w = (int) (1.0f * (width - DensityUtil.dip2px(mContext, 50)));
                    int mRealHeight = w * pixel[1] / pixel[0];
                    if (mRealHeight <= 0) {
                        mRealHeight = width * 245 / 230;
                    }
                    ViewGroup.LayoutParams layoutParams = picImg.getLayoutParams();
                    layoutParams.height = mRealHeight;
                    picImg.setVisibility(View.VISIBLE);

                    Glide.with(getContext()).load(dialogBean.picUrl).override(w, mRealHeight).into(picImg);
                }
            });
            widthRatio = 0.7866;
        } else {
            picImg.setVisibility(View.GONE);
            widthRatio = 0.7333;
        }

        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = (int) (DensityUtil.getScreenWidth(mContext) * widthRatio);
        window.setAttributes(attributes);

        if (!TextUtils.isEmpty(dialogBean.content)) {
            tvContent.setVisibility(View.VISIBLE);
            tvContent.setText(Html.fromHtml(dialogBean.content));
        } else {
            tvContent.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(dialogBean.tips)) {
            tvTips.setVisibility(View.VISIBLE);
            tvTips.setText(dialogBean.tips);
        } else {
            tvTips.setVisibility(View.GONE);
        }


        TextView btnItemView;
        LinearLayout.LayoutParams params;
        GradientDrawable mDrawable;
        llBtnContainer.removeAllViews();

        params = (LinearLayout.LayoutParams) llBtnContainer.getLayoutParams();
        if (dialogBean.marginTop != 0)
            params.topMargin = dialogBean.marginTop;
        if (dialogBean.marginRight != 0)
            params.rightMargin = dialogBean.marginRight;
        if (dialogBean.marginLeft != 0)
            params.leftMargin = dialogBean.marginLeft;
        if (dialogBean.marginBottom != 0)
            params.bottomMargin = dialogBean.marginBottom;
        llBtnContainer.setLayoutParams(params);

        if (null != dialogBean.buttonList && dialogBean.buttonList.size() > 0) {
            llBtnContainer.setVisibility(View.VISIBLE);
            btnLine.setVisibility(dialogBean.hideBtnLine ? View.GONE : View.VISIBLE);

            int length = dialogBean.buttonList.size();
            int btnHeight = DensityUtil.dip2px(mContext, 44);
            for (int i = 0; i < length; i++) {
                final CommonDialogBean.ButtonBean button = dialogBean.buttonList.get(i);
                if (null == button || TextUtils.isEmpty(button.title))
                    continue;
                mDrawable = null;
                btnItemView = new TextView(mContext);
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
                btnItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (button.actionType == CommonDialogBean.ButtonBean.ACTION_TYPE_CONFIRM || TextUtils.equals(button.action, CommonDialogBean.ButtonBean.ACTION_CONFIRM)) {
                            if (null != iCall)
                                iCall.confirm();
                        } else if (button.actionType == CommonDialogBean.ButtonBean.ACTION_TYPE_GOTO_URL || TextUtils.equals(button.action, CommonDialogBean.ButtonBean.ACTION_LINK)) {
                            if (null != iCall)
                                iCall.link(button.gotoUrl);
                        } else if (button.actionType == CommonDialogBean.ButtonBean.ACTION_TYPE_CANCEL || TextUtils.equals(button.action, CommonDialogBean.ButtonBean.ACTION_CANCEL)) {
                            if (null != iCall)
                                iCall.cancel();
                        } else if (TextUtils.equals(button.action, CommonDialogBean.ButtonBean.ACTION_CODE_1001)) {
                            if (null != mSpecialCodeCallback) {
                                mSpecialCodeCallback.code_1001(dialogBean.extInfo);
                            }
                        } else if (TextUtils.equals(button.action, CommonDialogBean.ButtonBean.ACTION_REFRESH)) {
                            if (null != iCall)
                                iCall.refresh();
                        }
                        dismiss();
                    }
                });
                llBtnContainer.addView(btnItemView);

                if (i + 1 != length) {
                    View view = new View(mContext);
                    view.setBackgroundColor(Color.parseColor("#E8E8E8"));
                    view.setLayoutParams(new LinearLayout.LayoutParams(DensityUtil.dip2px(mContext, 0.5f), DensityUtil.dip2px(getContext(), 44f)));
                    llBtnContainer.addView(view);
                }
            }
        } else {
            llBtnContainer.setVisibility(View.GONE);
            btnLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.iv_close) {
            dismiss();
        }
    }

    /**
     * 按照通用设计修改默认按钮参数
     *
     * @param bean
     */
    private void changeButtonData(CommonDialogBean bean) {
        if (null == bean)
            return;
        int buttonSize = null == bean.buttonList ? 0 : bean.buttonList.size();
        if (!TextUtils.isEmpty(bean.picUrl) && buttonSize == 1) {
            CommonDialogBean.ButtonBean buttonBean = bean.buttonList.get(0);
            if (null != buttonBean) {
                buttonBean.color = "#FFFFFF";
                if (null == buttonBean.background) {
                    buttonBean.background = buttonBean.buildBackground();
                    buttonBean.background.strokeWidth = DensityUtil.dip2px(mContext, 1);
                    buttonBean.background.radius = DensityUtil.dip2px(mContext, 2);
                    buttonBean.background.color = "#FB3B4B";
                }
                int dp25 = DensityUtil.dip2px(mContext, 25);
                bean.marginBottom = dp25;
                bean.marginLeft = dp25;
                bean.marginRight = dp25;
                bean.marginTop = dp25;
                bean.hideBtnLine = true;
                bean.showCloseIcon = true;
            }

            if (!TextUtils.isEmpty(bean.content)) {
                bean.subTitle = bean.content;
                bean.content = null;
            }
        } else if (buttonSize > 0) {
            CommonUIConfig.CommonDialogConfig commonDialogConfig = CommonUIConfig.getInstance().getCommonDialogConfig();
            String cancelButtonColor = null;
            String confirmButtonColor = null;
            String linkButtonColor = null;
            if (null != commonDialogConfig) {
                cancelButtonColor = commonDialogConfig.cancelButtonTextColor;
                confirmButtonColor = commonDialogConfig.confirmButtonTextColor;
                linkButtonColor = commonDialogConfig.linkButtonTextColor;
            }

            for (CommonDialogBean.ButtonBean buttonBean : bean.buttonList) {
                if (null == buttonBean)
                    continue;
                if (TextUtils.isEmpty(buttonBean.color)) {
                    if (buttonBean.actionType == CommonDialogBean.ButtonBean.ACTION_TYPE_CANCEL || TextUtils.equals(buttonBean.action, CommonDialogBean.ButtonBean.ACTION_CANCEL))
                        buttonBean.color = TextUtils.isEmpty(cancelButtonColor) ? "#C9C9C9" : cancelButtonColor;
                    else if (buttonBean.actionType == CommonDialogBean.ButtonBean.ACTION_TYPE_CONFIRM || TextUtils.equals(buttonBean.action, CommonDialogBean.ButtonBean.ACTION_CONFIRM))
                        buttonBean.color = TextUtils.isEmpty(confirmButtonColor) ? "#16D2D7" : confirmButtonColor;
                    else if (buttonBean.actionType == CommonDialogBean.ButtonBean.ACTION_TYPE_GOTO_URL || TextUtils.equals(buttonBean.action, CommonDialogBean.ButtonBean.ACTION_LINK))
                        buttonBean.color = TextUtils.isEmpty(linkButtonColor) ? "#16D2D7" : linkButtonColor;
                    else
                        buttonBean.color = TextUtils.isEmpty(confirmButtonColor) ? "#16D2D7" : confirmButtonColor;
                }
            }
        }
    }

    public interface Callback {
        void confirm();

        void refresh();

        void cancel();

        void link(String url);
    }

    public interface SpecialCodeCallback {
        void code_1001(CommonDialogBean.ExtInfo extInfo);
    }

}
