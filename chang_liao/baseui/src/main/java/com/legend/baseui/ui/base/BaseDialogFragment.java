package com.legend.baseui.ui.base;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;

import com.legend.baseui.ui.util.DensityUtil;

public abstract class BaseDialogFragment<DB extends ViewDataBinding> extends DialogFragment {

    private BaseDialogFragmentConfig config = new BaseDialogFragmentConfig();
    public DB mDataBinding;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initPageConfig(config);
        return super.onCreateDialog(savedInstanceState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (null != dialog)
            initDialog(dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = getView(inflater);
        if (null == view)
            throw new IllegalArgumentException("view is null");
        mDataBinding = DataBindingUtil.bind(view);
        if (mDataBinding != null) {
            mDataBinding.setLifecycleOwner(this);
        }

        initView(view);
        return view;
    }

    protected abstract View getView(LayoutInflater inflater);

    protected abstract void initView(View view);

    protected void initDialog(@Nullable Dialog dialog) {
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = (int) (DensityUtil.getScreenWidth(dialog.getContext()) * config.widthRadio);
        attributes.gravity = config.gravity;
        if (config.height > 0)
            attributes.height = config.height;
    }

    protected void initPageConfig(@Nullable BaseDialogFragmentConfig config) {

    }
}
