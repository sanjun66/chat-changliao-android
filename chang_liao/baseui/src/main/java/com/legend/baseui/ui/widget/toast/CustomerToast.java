package com.legend.baseui.ui.widget.toast;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

class CustomerToast extends Toast implements IToast {
    private TextView mText;
    private Context mContext;

    private CustomerToast(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public static CustomerToast makeText(@NonNull Context context, @NonNull CharSequence text, int duration) {
        CustomerToast toast = new CustomerToast(context);
        toast.setText(text);
        toast.setDuration(duration);
        return toast;
    }

    @Override
    public void setText(@NonNull CharSequence text) {
        if (null == mText)
            return;
        mText.setText(text);
    }

    @Override
    public void setText(@StringRes int resId) {
        setText(mContext.getText(resId));
    }


    private void initView() {
        mText = ToastUtils.makeToastView();
        setView(mText);
        setGravity(Gravity.CENTER, 0, 0);
    }
}
