//package com.basestonedata.lego.ui.widget.toast;
//
//import android.app.Activity;
//import android.app.Application;
//import android.content.Context;
//import android.graphics.drawable.Drawable;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.view.Gravity;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.StringRes;
//
//import com.basestonedata.lego.ui.Applications;
//import com.basestonedata.lego.ui.R;

//public class LegoToastUtils_backup {
//    private static Context mContext = Applications.getCurrent();
//    private static IToast mToast;
//    private static ActivityStack activityStack;
//    private static boolean sdkCodeGreaterThan29 = true;
//    private static Handler mHandler;
//
//    public static void init(Application application) {
//        sdkCodeGreaterThan29 = Build.VERSION.SDK_INT >= 30;
//        if (sdkCodeGreaterThan29)
//            activityStack = ActivityStack.register(application);
//    }
//
//    public static void show(@StringRes int resId) {
//        if (null == mContext)
//            return;
//        show(mContext.getResources().getText(resId), Toast.LENGTH_SHORT);
//    }
//
//    public static void show(@StringRes int resId, int duration) {
//        if (null == mContext)
//            return;
//        show(mContext.getResources().getText(resId), duration);
//    }
//
//    public static void show(@NonNull CharSequence text) {
//        show(text, Toast.LENGTH_SHORT);
//    }
//
//    public static void show(Context context, @NonNull CharSequence text) {
//        show(text, Toast.LENGTH_SHORT);
//    }
//
//    public static void show(@NonNull final CharSequence text, final int duration) {
//        if (TextUtils.isEmpty(text)) {
//            return;
//        }
//        if (Looper.myLooper() != Looper.getMainLooper()) {
//            synchronized (LegoToastUtils_backup.class) {
//                if (null == mHandler)
//                    mHandler = new Handler(Looper.getMainLooper());
//            }
//            mHandler.post(() -> show(text, duration));
//        } else {
//            if (null != mToast) {
//                mToast.cancel();
//                mToast = null;
//            }
//
//            if (sdkCodeGreaterThan29) {
//                Activity activity = activityStack.getForegroundActivity();
//                if (null == activityStack) {
//                    if (null != mContext)
//                        mToast = SystemToast.makeText(mContext, text, duration);
//                } else {
//                    if (null != activity) {
//                        mToast = Customer2Toast.makeText(activity, text, duration);
//                    } else {
//                        if (null != mContext)
//                            mToast = SystemToast.makeText(mContext, text, duration);
//                    }
//                }
//            } else {
//                if (null != mContext)
//                    mToast = CustomerToast.makeText(mContext, text, duration);
//            }
//
//            if (null != mToast)
//                mToast.show();
//        }
//    }
//
//    public static void show(@StringRes int resId, Object... args) {
//        if (null == mContext)
//            return;
//        show(String.format(mContext.getResources().getString(resId), args),
//                Toast.LENGTH_SHORT);
//    }
//
//    public static void show(@NonNull String format, Object... args) {
//        show(String.format(format, args), Toast.LENGTH_SHORT);
//    }
//
//    public static void show(@StringRes int resId, int duration, Object... args) {
//        if (null == mContext)
//            return;
//        show(String.format(mContext.getResources().getString(resId), args),
//                duration);
//    }
//
//    public static void show(@NonNull String format, int duration, Object... args) {
//        show(String.format(format, args), duration);
//    }
//
//    public static void showApiException(Throwable exp) {
//        String msg = null;
//        if (null != exp)
//            msg = exp.getMessage();
//        if (TextUtils.isEmpty(msg))
//            msg = "服务器开小差了, 请稍后重试";
//        show(msg);
//    }
//
//    public static void cancel() {
//        if (Looper.myLooper() != Looper.getMainLooper()) {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    cancel();
//                }
//            });
//        } else {
//            if (null != mToast)
//                mToast.cancel();
//        }
//    }
//
//    public static void makeText(@NonNull Context context, @NonNull CharSequence text, int duration) {
//        show(text, duration);
//    }
//
//    public static void makeText(@NonNull Context context, @StringRes int textRes, int duration) {
//        show(context.getString(textRes), duration);
//    }
//z
//    static TextView makeToastView() {
//        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
//        int padding = ((int) displayMetrics.density * 10);
//
//        TextView textView = new TextView(mContext);
//        Drawable drawable = mContext.getResources().getDrawable(R.drawable.ui_toast_bg);
//        drawable.setAlpha((int) (255 * 0.7));
//        textView.setBackground(drawable);
//        textView.setMinWidth((int) (displayMetrics.density * 200));
//        textView.setMaxWidth((int) (displayMetrics.widthPixels - displayMetrics.density * 100));
//        textView.setPadding(padding, padding, padding, padding);
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
//        return textView;
//    }
//}
