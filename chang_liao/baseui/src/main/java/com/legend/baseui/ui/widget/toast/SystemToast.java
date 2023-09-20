package com.legend.baseui.ui.widget.toast;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;

public class SystemToast implements IToast {
    private Toast toast;

    private SystemToast(Context context, @NonNull CharSequence text, int duration) {
        this.toast = Toast.makeText(context, text, duration);
    }

    public static SystemToast makeText(@NonNull Context context, @NonNull CharSequence text, int duration) {
        SystemToast toast = new SystemToast(context, text, duration);
        return toast;
    }

    @Override
    public void show() {
        toast.show();
    }

    @Override
    public void cancel() {
        toast.cancel();
    }

    @Override
    public void setText(CharSequence text) {
    }
}
