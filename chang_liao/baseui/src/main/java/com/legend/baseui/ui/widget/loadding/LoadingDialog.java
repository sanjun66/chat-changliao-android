package com.legend.baseui.ui.widget.loadding;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import com.com.legend.ui.R;

public class LoadingDialog extends AlertDialog {

    private boolean canBackPressed = true;

    public LoadingDialog(Context context) {
        super(context, R.style.ui_LoadDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.ui_loading_default);
        setContentView(R.layout.dialog_loading);

        setCanceledOnTouchOutside(false);
    }

    public void canBackPressed(boolean canBackPressed) {
        this.canBackPressed = canBackPressed;
    }

    @Override
    public void onBackPressed() {
        if (canBackPressed)
            super.onBackPressed();
    }
}
