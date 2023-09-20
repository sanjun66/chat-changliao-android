package com.legend.common.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.legend.base.utils.StringUtils;
import com.legend.baseui.ui.util.DisplayUtils;
import com.legend.baseui.ui.widget.toast.ToastUtils;
import com.legend.commonres.R;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by cxf on 2017/8/8.
 */

public class DialogUitl {
    public static final int INPUT_TYPE_TEXT = 0;
    public static final int INPUT_TYPE_NUMBER = 1;
    public static final int INPUT_TYPE_NUMBER_PASSWORD = 2;
    public static final int INPUT_TYPE_TEXT_PASSWORD = 3;
    public static final int INPUT_TYPE_VARIATION_PASSWORD = 4;

    //第三方登录的时候用显示的dialog
    public static Dialog loginAuthDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_login_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * 用于网络请求等耗时操作的LoadingDialog
     */
    public static Dialog loadingDialog(Context context, String text) {
        Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(com.com.legend.ui.R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(text)) {
            TextView titleView = (TextView) dialog.findViewById(com.com.legend.ui.R.id.text);
            if (titleView != null) {
                titleView.setText(text);
            }
        }
        return dialog;
    }

    /**
     * 用于网络请求等耗时操作的ProgressDialog
     */
    public static ProgressDialog progressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_loading_2);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Dialog loadingDialog(Context context) {
        return loadingDialog(context, "");
    }

    public static Dialog showOneBtnDlg(Context context, String content, String confirm, SimpleCallback simpleCallback) {
        return showSimpleNoTitDialog(context, content, confirm, "", false, simpleCallback);
    }

    public static Dialog showSimpleNoTitDialog(Context context, String content, String confirm, String cancel, final SimpleCallback simpleCallback) {
        return showSimpleNoTitDialog(context, content, confirm, cancel, true, simpleCallback);
    }

    public static Dialog showSimpleNoTitDialog(Context context, String content, String confirm, final SimpleCallback simpleCallback) {
        return showSimpleNoTitDialog(context, content, confirm, "",true, simpleCallback);
    }

    public static Dialog showSimpleNoTitDialog(Context context, String content, String confirm, String cancel, boolean showCancel, final SimpleCallback simpleCallback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_simple_notit);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(content)) {
            TextView contentTextView = (TextView) dialog.findViewById(R.id.content);
            contentTextView.setText(content);
        }
        TextView tvConfirm = dialog.findViewById(R.id.btn_confirm);
        if (!TextUtils.isEmpty(confirm)) {
            tvConfirm.setText(confirm);
        }
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleCallback != null) {
                    simpleCallback.onConfirmClick(dialog, "");
                }
                dialog.dismiss();
            }
        });
        TextView tvCancel = dialog.findViewById(R.id.btn_cancel);
        if (!TextUtils.isEmpty(cancel)) tvCancel.setText(cancel);
        tvCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (simpleCallback != null && simpleCallback instanceof SimpleCallback2) {
                    ((SimpleCallback2) simpleCallback).onCancelClick();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
        return dialog;
    }

    public static void showSimpleNoTitDialog(Context context, SpannableStringBuilder content, String confirm, final SimpleCallback simpleCallback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_simple_notit);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (!TextUtils.isEmpty(content)) {
            TextView contentTextView = (TextView) dialog.findViewById(R.id.content);
            contentTextView.setText(content);
        }
        TextView tvConfirm = dialog.findViewById(R.id.btn_confirm);
        if (!TextUtils.isEmpty(confirm)) {
            tvConfirm.setText(confirm);
        }
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleCallback != null) {
                    simpleCallback.onConfirmClick(dialog, "");
                }
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public static void showSimpleTipDialog(Context context, String content) {
        showSimpleTipDialog(context, null, content);
    }

    public static void showSimpleTipDialog(Context context, String title, String content) {
        showSimpleTipDialog(context, title, false, content, false);
    }

    public static void showSimpleTipDialog(Context context, String title, boolean hideTitle, String content, boolean dark) {
        final Dialog dialog = new Dialog(context, dark ? R.style.dialog : R.style.dialog2);
        dialog.setContentView(R.layout.dialog_simple_tip);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        TextView titleView = (TextView) dialog.findViewById(R.id.title);
        if(hideTitle){
            titleView.setVisibility(View.GONE);
        }else{
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
            }
        }
        if (!TextUtils.isEmpty(content)) {
            TextView contentTextView = (TextView) dialog.findViewById(R.id.content);
            contentTextView.setText(content);
        }
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public static Dialog showSimpleTipDialogRe(Context context, String content) {
        final Dialog dialog = new Dialog(context, R.style.dialog2);
        dialog.setContentView(R.layout.dialog_simple_tip);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        if (!TextUtils.isEmpty(content)) {
            TextView contentTextView = (TextView) dialog.findViewById(R.id.content);
            contentTextView.setText(content);
        }
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        return dialog;
    }

    public static void showSimpleDialog(Context context, String content, SimpleCallback callback) {
        showSimpleDialog(context, content, true, callback);
    }

    public static void showSimpleDialog(Context context, String content, boolean cancelable, SimpleCallback callback) {
        showSimpleDialog(context, null, content, cancelable, callback);
    }

    public static void showSimpleDialog(Context context, String title, String content, boolean cancelable, SimpleCallback callback) {
        new Builder(context)
                .setTitle(title)
                .setContent(content)
                .setCancelable(cancelable)
                .setClickCallback(callback)
                .build()
                .show();
    }


    public static void showSimpleInputDialog(Context context, String title, String hint, int inputType, int length, SimpleCallback callback) {
        showSimpleInputDialog(context, title, hint, inputType, length, true, callback);
    }
    public static void showSimpleInputDialog(Context context, String title, String hint, int inputType, int length, boolean isCancelAble, SimpleCallback callback) {
        new Builder(context).setTitle(title)
                .setCancelable(isCancelAble)
                .setInput(true)
                .setHint(hint)
                .setInputType(inputType)
                .setLength(length)
                .setClickCallback(callback)
                .build()
                .show();
    }


    public static void showSimpleInputDialog(Context context, String title, int inputType, int length, SimpleCallback callback) {
        showSimpleInputDialog(context, title, null, inputType, length, callback);
    }

    public static void showSimpleInputDialog(Context context, String title, int inputType, SimpleCallback callback) {
        showSimpleInputDialog(context, title, inputType, 0, callback);
    }

    public static void showSimpleInputDialog(Context context, String title, SimpleCallback callback) {
        showSimpleInputDialog(context, title, INPUT_TYPE_TEXT, callback);
    }


    public static void showStringArrayDialog(Context context, Integer[] array, final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, 0xff323232, callback);
    }


    public static void showStringArrayDialog(Context context, Integer[] array, int textColor, final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, textColor, textColor, callback);
    }
    public static void showStringArrayDialog(Context context, Integer[] array, int textColor, int lastTextColor, final StringArrayDialogCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_string_array);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.container);
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if (callback != null) {
                    callback.onItemClick(textView.getText().toString(), (int) v.getTag());
                }
                dialog.dismiss();
            }
        };
        for (int i = 0, length = array.length; i < length; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dp2px(context, 50)));
            textView.setTextColor(i == length - 1 ? lastTextColor : textColor);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setGravity(Gravity.CENTER);
            textView.setText(array[i]);
            textView.setTag(array[i]);
            textView.setOnClickListener(itemListener);
            container.addView(textView);
            if (i != length - 1) {
                View v = new View(context);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dp2px(context, 1)));
                v.setBackgroundColor(0xffe5e5e5);
                container.addView(v);
            }
        }
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void showStringArrayDialog(Context context, SparseArray<String> array, final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, true, callback);
    }

    public static void showStringArrayDialogBottom(Context context, SparseArray<String> array, final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, true, true, callback);
    }

    public static void showStringArrayDialog(Context context, SparseArray<String> array, boolean showCancel, final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, showCancel, false, callback);
    }

    public static void showStringArrayDialog(Context context, SparseArray<String> array, boolean showCancel, boolean isBottom, final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, showCancel,isBottom,  -1, callback);
    }

    public static void showStringArrayDialog(Context context, SparseArray<String> array, boolean showCancel, int maxHeightNum,  final StringArrayDialogCallback callback) {
        showStringArrayDialog(context, array, showCancel, false, maxHeightNum, callback);
    }

    public static void showStringArrayDialog(Context context, SparseArray<String> array, boolean showCancel, boolean isBottom, int maxHeightNum,  final StringArrayDialogCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_string_array);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (isBottom) params.gravity = Gravity.BOTTOM;
        else params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.container);
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if (callback != null) {
                    callback.onItemClick(textView.getText().toString(), (int) v.getTag());
                }
                dialog.dismiss();
            }
        };
        for (int i = 0, length = array.size(); i < length; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dp2px(context, 50)));
            textView.setTextColor(0xff323232);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setGravity(Gravity.CENTER);
            textView.setText(array.valueAt(i));
            textView.setTag(array.keyAt(i));
            textView.setOnClickListener(itemListener);
            container.addView(textView);
            if (i != length - 1) {
                View v = new View(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dp2px(context, 1));
                lp.leftMargin = DisplayUtils.dp2px(context, 10);
                lp.rightMargin = DisplayUtils.dp2px(context, 10);
                v.setLayoutParams(lp);
                v.setBackgroundColor(0xfff5f5f5);
                container.addView(v);
            }
        }
        if (array.size() > maxHeightNum) {
            ScrollView scrollView = dialog.findViewById(R.id.scroll_view);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) scrollView.getLayoutParams();
            layoutParams.height = DisplayUtils.dp2px(context, 53) * maxHeightNum;  // 每行多2个像素
            scrollView.setLayoutParams(layoutParams);
        }
        if (!showCancel) dialog.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static Dialog getStringArrayDialog(Context context, Integer[] array, boolean dark, final StringArrayDialogCallback callback) {
        final Dialog dialog = new Dialog(context, dark ? R.style.dialog : R.style.dialog2);
        dialog.setContentView(R.layout.dialog_string_array);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        LinearLayout container = (LinearLayout) dialog.findViewById(R.id.container);
        View.OnClickListener itemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if (callback != null) {
                    callback.onItemClick(textView.getText().toString(), (int) v.getTag());
                }
                dialog.dismiss();
            }
        };
        Drawable bg = ContextCompat.getDrawable(context, R.drawable.bg_dialog_2);
        for (int i = 0, length = array.length; i < length; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dp2px(context, 50)));
            textView.setTextColor(0xff323232);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setGravity(Gravity.CENTER);
            textView.setBackground(bg);
            textView.setText(array[i]);
            textView.setTag(array[i]);
            textView.setOnClickListener(itemListener);
            container.addView(textView);
            if (i != length - 1) {
                View v = new View(context);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dp2px(context, 1)));
                v.setBackgroundColor(0xffe5e5e5);
                container.addView(v);
            }
        }
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static void showDatePickerDialog(Context context, final DataPickerCallback callback) {
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_date_picker);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
        final Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
                c.set(year, month, dayOfMonth);
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_confirm) {
                    if (callback != null) {
                        if (c.getTime().getTime() > new Date().getTime()) {
                            ToastUtils.show(StringUtils.getString(R.string.edit_profile_right_date));
                        } else {
                            String result = DateFormat.format("yyyy-MM-dd", c).toString();
                            callback.onConfirmClick(result);
                            dialog.dismiss();
                        }
                    }
                } else {
                    dialog.dismiss();
                }
            }
        };
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(listener);
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(listener);
        dialog.show();
    }



    public static class Builder {

        private Context mContext;
        private String mTitle;
        private String mContent;
        private String mConfirmString;
        private String mCancelString;
        private boolean mIsHideTitle;
        private int mConfirmColor;
        private int mCancelColor;
        private boolean mCancelable;
        private boolean mBackgroundDimEnabled = true;//显示区域以外是否使用黑色半透明背景
        private boolean mInput;//是否是输入框的
        private boolean mGravityCenter = true;
        private String mHint;
        private int mInputType;
        private int mLength;
        private SimpleCallback mClickCallback;
        private int mDialogLayout;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setDialogLayout(int layoutId) {
            mDialogLayout = layoutId;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setContent(String content) {
            mContent = content;
            return this;
        }

        public Builder setConfrimString(String confirmString) {
            mConfirmString = confirmString;
            return this;
        }

        public Builder setCancelString(String cancelString) {
            mCancelString = cancelString;
            return this;
        }

        public Builder setIsHideTitle(boolean isHideTitle) {
            mIsHideTitle = isHideTitle;
            return this;
        }

        public Builder setConfirmColor(int confirmColor) {
            mConfirmColor = confirmColor;
            return this;
        }

        public Builder setCancelColor(int cancelColor) {
            mCancelColor = cancelColor;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder setBackgroundDimEnabled(boolean backgroundDimEnabled) {
            mBackgroundDimEnabled = backgroundDimEnabled;
            return this;
        }

        public Builder setInput(boolean input) {
            mInput = input;
            return this;
        }

        public Builder setGravityCenter(boolean gravityCenter) {
            mGravityCenter = gravityCenter;
            return this;
        }

        public Builder setHint(String hint) {
            mHint = hint;
            return this;
        }

        public Builder setInputType(int inputType) {
            mInputType = inputType;
            return this;
        }

        public Builder setLength(int length) {
            mLength = length;
            return this;
        }

        public Builder setClickCallback(SimpleCallback clickCallback) {
            mClickCallback = clickCallback;
            return this;
        }

        public Dialog build() {
            final Dialog dialog = new Dialog(mContext, mBackgroundDimEnabled ? R.style.dialog : R.style.dialog2);
            if (mDialogLayout != 0) {
                dialog.setContentView(mDialogLayout);
            } else {
                dialog.setContentView(mInput ? R.layout.dialog_input : R.layout.dialog_simple);
            }
            dialog.setCancelable(mCancelable);
            dialog.setCanceledOnTouchOutside(mCancelable);
            TextView titleView = (TextView) dialog.findViewById(R.id.title);
            if (titleView != null) {
                if (mIsHideTitle) {
                    titleView.setVisibility(View.GONE);
                } else {
                    if (!TextUtils.isEmpty(mTitle)) {
                        titleView.setText(mTitle);
                    }
                }
            }
            final TextView content = (TextView) dialog.findViewById(R.id.content);
            if (!TextUtils.isEmpty(mHint)) {
                content.setHint(mHint);
            }
            if (!TextUtils.isEmpty(mContent)) {
                content.setText(mContent);
                if (!mGravityCenter) {
                    content.setGravity(Gravity.LEFT);
                }
            }
            if (mInputType == INPUT_TYPE_NUMBER) {
                content.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if (mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                content.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            } else if (mInputType == INPUT_TYPE_TEXT_PASSWORD) {
                content.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else if (mInputType == INPUT_TYPE_VARIATION_PASSWORD) {
                content.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }
            if (mInput && mLength > 0 && content instanceof EditText) {
                content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mLength)});
            }
            TextView btnConfirm = (TextView) dialog.findViewById(R.id.btn_confirm);
            if (!TextUtils.isEmpty(mConfirmString)) {
                btnConfirm.setText(mConfirmString);
            }
            if (mConfirmColor != 0) {
                btnConfirm.setTextColor(mConfirmColor);
            }
            TextView btnCancel = (TextView) dialog.findViewById(R.id.btn_cancel);
            if (!TextUtils.isEmpty(mCancelString)) {
                btnCancel.setText(mCancelString);
            }
            if (mCancelColor != 0) {
                btnCancel.setTextColor(mCancelColor);
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.btn_confirm) {
                        if (mClickCallback != null) {
                            if (mInput) {
                                mClickCallback.onConfirmClick(dialog, content.getText().toString());
                            } else {
                                dialog.dismiss();
                                mClickCallback.onConfirmClick(dialog, "");
                            }
                        } else {
                            dialog.dismiss();
                        }
                    } else {
                        dialog.dismiss();
                        if (mClickCallback instanceof SimpleCallback2) {
                            ((SimpleCallback2) mClickCallback).onCancelClick();
                        }
                    }
                }
            };
            btnConfirm.setOnClickListener(listener);
            btnCancel.setOnClickListener(listener);
            return dialog;
        }

    }

    public interface DataPickerCallback {
        void onConfirmClick(String date);
    }

    public interface StringArrayDialogCallback {
        void onItemClick(String text, int tag);
    }

    public interface SimpleCallback {
        void onConfirmClick(Dialog dialog, String content);
    }

    public interface SimpleCallback2 extends SimpleCallback {
        void onCancelClick();
    }

}
