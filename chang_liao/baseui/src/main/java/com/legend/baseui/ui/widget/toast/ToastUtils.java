package com.legend.baseui.ui.widget.toast;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.legend.baseui.ui.Applications;
import com.com.legend.ui.R;

public class ToastUtils {
    private static boolean hasInit = false;
    public static void init(Application application) {
        initToastConfig();
    }

    private static void initToastConfig() {
        if (!hasInit) {
            com.blankj.utilcode.util.ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER,0,0)
                    .setBgResource(R.drawable.ui_toast_b2111111_bg)
                    .setTextColor(Color.parseColor("#FFFFFF"));
            hasInit = true;
        }
    }

    public static void show(@StringRes int resId) {
        if (resId == 0)
            return;

        initToastConfig();
        com.blankj.utilcode.util.ToastUtils.showShort(resId);
    }

    public static void show(@StringRes int resId, int duration) {
        if (resId == 0)
            return;

        initToastConfig();
        if (duration == Toast.LENGTH_LONG) {
            com.blankj.utilcode.util.ToastUtils.showLong(resId);
        } else {
            com.blankj.utilcode.util.ToastUtils.showShort(resId);
        }
    }

    public static void show(@NonNull CharSequence text) {
        if (TextUtils.isEmpty(text))
            return;

        initToastConfig();
        com.blankj.utilcode.util.ToastUtils.showShort(text);
    }

    public static void show(Context context, @NonNull CharSequence text) {
        if (TextUtils.isEmpty(text))
            return;

        initToastConfig();
        com.blankj.utilcode.util.ToastUtils.showShort(text);
    }

    public static void show(@NonNull final CharSequence text, final int duration) {
        if (TextUtils.isEmpty(text))
            return;

        initToastConfig();
        if (duration == Toast.LENGTH_LONG) {
            com.blankj.utilcode.util.ToastUtils.showLong(text);
        } else {
            com.blankj.utilcode.util.ToastUtils.showShort(text);
        }
    }

    public static void show(@StringRes int resId, Object... args) {
        Application current = Applications.getCurrent();
        if (null == current)
            return;
        show(String.format(current.getResources().getString(resId), args),
                Toast.LENGTH_SHORT);
    }

    public static void show(@NonNull String format, Object... args) {
        show(String.format(format, args), Toast.LENGTH_SHORT);
    }

    public static void show(@StringRes int resId, int duration, Object... args) {
        Application current = Applications.getCurrent();
        if (null == current)
            return;
        show(String.format(current.getResources().getString(resId), args),
                duration);
    }

    public static void show(@NonNull String format, int duration, Object... args) {
        show(String.format(format, args), duration);
    }

    public static void showApiException(Throwable exp) {
        String msg = null;
        if (null != exp)
            msg = exp.getMessage();
        if (TextUtils.isEmpty(msg))
            msg = "服务器开小差了, 请稍后重试";
        show(msg);
    }

    public static void cancel() {
        com.blankj.utilcode.util.ToastUtils.cancel();
    }

    public static void makeText(@NonNull Context context, @NonNull CharSequence text, int duration) {
        show(text, duration);
    }

    public static void makeText(@NonNull Context context, @StringRes int textRes, int duration) {
        show(context.getString(textRes), duration);
    }

    @Deprecated()
    static TextView makeToastView() {
        Application current = Applications.getCurrent();
        DisplayMetrics displayMetrics = current.getResources().getDisplayMetrics();
        int padding = ((int) displayMetrics.density * 10);
        TextView textView = new TextView(current);
        Drawable drawable = current.getResources().getDrawable(R.drawable.ui_toast_bg);
        drawable.setAlpha((int) (255 * 0.7));
        textView.setBackground(drawable);
        textView.setMinWidth((int) (displayMetrics.density * 200));
        textView.setMaxWidth((int) (displayMetrics.widthPixels - displayMetrics.density * 100));
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(current.getResources().getColor(android.R.color.white));
        return textView;
    }
}
