package com.legend.baseui.ui.widget.toast;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

/**
 * 适配Android 11
 */
class Customer2Toast implements IToast {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static final int SHORT_DURATION_TIMEOUT = 2000;
    private static final int LONG_DURATION_TIMEOUT = 3500;
    private final String mPackageName;
    private boolean mShow;
    private WindowLifecycle mWindowLifecycle;
    private int duration;
    private TextView toastView;

    private Customer2Toast(Activity activity) {
        mPackageName = activity.getPackageName();
        mWindowLifecycle = new WindowLifecycle(activity);
        toastView = ToastUtils.makeToastView();
    }

    public static Customer2Toast makeText(@NonNull Activity activity, @NonNull CharSequence text, int duration) {
        Customer2Toast toast = new Customer2Toast(activity);
        toast.setText(text);
        toast.setDuration(duration);
        return toast;
    }

    @Override
    public void show() {
        if (isShow())
            return;
        HANDLER.removeCallbacks(mShowRunnable);
        HANDLER.post(mShowRunnable);
    }

    @Override
    public void cancel() {
        if (null == toastView || !isShow())
            return;

        HANDLER.removeCallbacks(mCancelRunnable);
        HANDLER.post(mCancelRunnable);
    }

    @Override
    public void setText(CharSequence text) {
        toastView.setText(text);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    boolean isShow() {
        return mShow;
    }

    void setShow(boolean show) {
        mShow = show;
    }


    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {

            Activity activity = mWindowLifecycle.getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
                return;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = android.R.style.Animation_Toast;
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.packageName = mPackageName;
            params.gravity = Gravity.CENTER;

            WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return;
            }

            try {
                windowManager.addView(toastView, params);
                HANDLER.postDelayed(() -> cancel(), duration == Toast.LENGTH_LONG ?
                        LONG_DURATION_TIMEOUT : SHORT_DURATION_TIMEOUT);
                mWindowLifecycle.register(Customer2Toast.this);
                setShow(true);
            } catch (IllegalStateException | WindowManager.BadTokenException e) {
                e.printStackTrace();
            }
        }
    };

    private final Runnable mCancelRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Activity activity = mWindowLifecycle.getActivity();
                if (activity == null) {
                    return;
                }

                WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager == null) {
                    return;
                }
                windowManager.removeViewImmediate(toastView);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                mWindowLifecycle.unregister();
                setShow(false);
            }
        }
    };
}
