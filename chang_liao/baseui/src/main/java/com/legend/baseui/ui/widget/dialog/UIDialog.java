package com.legend.baseui.ui.widget.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.UIResHelper;

public class UIDialog extends UIBaseDialog {

    public UIDialog(Context context) {
        this(context, R.style.UI_Dialog);
    }

    public UIDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * 消息类型的对话框 Builder。通过它可以生成一个带标题、文本消息、按钮的对话框。
     */
    public static class MessageDialogBuilder extends UIDialogBuilder<MessageDialogBuilder> {
        protected CharSequence mMessage;

        public MessageDialogBuilder(Context context) {
            super(context);
        }

        @Override
        protected View onCreateContentLayout(UIDialog mDialog) {
            if (!TextUtils.isEmpty(mMessage)) {
                TextView textView = new TextView(mDialog.getContext());
                textView.setId(R.id.ui_dialog_operator_layout_id);
                textView.setText(mMessage);
                UIResHelper.assignTextViewWithAttr(textView, R.attr.ui_dialog_message_content_style);
                return textView;
            }

            return new View(getBaseContext());
        }

        /**
         * 设置对话框的消息文本
         */
        public MessageDialogBuilder setMessage(CharSequence message) {
            this.mMessage = message;
            return this;
        }

        /**
         * 设置对话框的消息文本
         */
        public MessageDialogBuilder setMessage(int resId) {
            return setMessage(getBaseContext().getResources().getString(resId));
        }

    }
}
