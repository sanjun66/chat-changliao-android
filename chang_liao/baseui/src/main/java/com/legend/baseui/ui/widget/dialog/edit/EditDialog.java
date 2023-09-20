package com.legend.baseui.ui.widget.dialog.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.legend.baseui.ui.widget.dialog.edit.view.EditDialogView;
import com.legend.baseui.ui.widget.dialog.edit.bean.EditDialogBean;

public class EditDialog extends DialogFragment implements EditDialogView.EditCallback {

    private EditDialogBean editDialogBean;
    private EditDialogView view;

    public EditDialog(@NonNull EditDialogBean dialogBean) {
        this.editDialogBean = dialogBean;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = new EditDialogView(getContext());
        view.setCallback(this);
        view.bindData(editDialogBean.title, editDialogBean.tips, editDialogBean.hintText);
        view.setButton(editDialogBean.buttonBeans);
        return view;
    }

    public String getEditText() {
        return null == view ? "" : view.getEditText();
    }

    @Override
    public void cancel() {
        if (null != editDialogBean.callback)
            editDialogBean.callback.cancel(this);
        else
            dismiss();
    }

    @Override
    public void confirm() {
        if (null != editDialogBean.callback)
            editDialogBean.callback.confirm(this);
        else
            dismiss();
    }

    @Override
    public void link(String link) {
        if (null != editDialogBean.callback)
            editDialogBean.callback.link(link);
        dismiss();
    }
}
