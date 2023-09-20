package com.legend.baseui.ui.widget.dialog.edit.bean;

import androidx.fragment.app.DialogFragment;

import com.legend.baseui.ui.widget.dialog.bean.DialogButtonBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EditDialogBean implements Serializable {
    public String title;
    public String tips;
    public String hintText;
    public List<DialogButtonBean> buttonBeans;
    public Callback callback;

    public void buildCommonButtons() {
        this.buttonBeans = new ArrayList<DialogButtonBean>() {{
            add(getCancelButton());
            add(getConfirmButton());
        }};
    }

    public DialogButtonBean getConfirmButton() {
        DialogButtonBean buttonBean = new DialogButtonBean();
        buttonBean.action = DialogButtonBean.ACTION_CONFIRM;
        buttonBean.title = "确认";
        return buttonBean;
    }

    public DialogButtonBean getCancelButton() {
        DialogButtonBean buttonBean = new DialogButtonBean();
        buttonBean.action = DialogButtonBean.ACTION_CANCEL;
        buttonBean.title = "取消";
        return buttonBean;
    }

    public interface Callback<T extends DialogFragment> {
        void confirm(T dialog);

        void cancel(T dialog);

        void link(String url);
    }
}
