package com.legend.baseui.ui.util.keyboardvisibilityevent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardVisibilityEvent {

    public static final String TAG = "KeyboardVisibilityEvent";

    public static final int KEYBOARD_VISIBLE_THRESHOLD_DP = 100;

    public static Registry registerEventListener(final Activity activity, final KeyboardVisibilityEventListener listener) {

        if (activity == null) {
            throw new NullPointerException("Parameter:activity must not be null");
        }

        int softInputAdjust = activity.getWindow().getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;

        boolean isNotAdjustNothing = (softInputAdjust & WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;

        if (!isNotAdjustNothing) {
            throw new IllegalArgumentException("Parameter:activity window SoftInputMethod is SOFT_INPUT_ADJUST_NOTHING. In this case window will not be resized");
        }

        if (listener == null) {
            throw new NullPointerException("Parameter:listener must not be null");
        }

        View activityRoot = getActivityRoot(activity);

        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean wasOpened;
            @Override
            public void onGlobalLayout() {
                int heightDiff = getHeightDiff(activity);
                boolean isOpen = isKeyboardVisible(activity, heightDiff);

                if (isOpen == wasOpened) {
                    // keyboard state has not changed
                    return;
                }

                wasOpened = isOpen;
                listener.onVisibilityChanged(isOpen, heightDiff);
            }
        };
        activityRoot.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        return new SimpleRegistry(activity, layoutListener);
    }

    public static boolean isKeyboardVisible(Activity activity) {
        int visibleThreshold = Math.round(dp2px(activity, KEYBOARD_VISIBLE_THRESHOLD_DP));
        
        return getHeightDiff(activity) > visibleThreshold;
    }

    public static boolean isKeyboardVisible(Activity activity, int heightDiff) {
        int visibleThreshold = Math.round(dp2px(activity, KEYBOARD_VISIBLE_THRESHOLD_DP));

        return heightDiff > visibleThreshold;
    }

    /**
     * 获取由于键盘弹起/收起产生的屏幕高度变化量
     * 注意：以下两种情况heightDiff会
     * @param activity activity
     * @return 屏幕高度变化量
     */
    public static int getHeightDiff(Activity activity) {
        Rect r = new Rect();

        View activityRoot = getActivityRoot(activity);

        // activity可视区域
        // 包含SupportActionBar，ActionBar，但不包含Status bar
        getContentRoot(activity).getWindowVisibleDisplayFrame(r);
        // activity rootView
        int screenHeight = activityRoot.getRootView().getHeight();

        // 获取 activity 根视图距离屏幕顶部的距离
        // int[] location = new int[2];
        // activity rootView 内容区域所在屏幕的坐标
        // 注意：有ActionBar时是从ActionBar下面开始计算坐标，
        // 问题：为什么getWindowVisibleDisplayFrame在键盘没有弹起的时候获取的矩阵高度为包含ActionBar?
        //
        // 所以在使用（screenHeight - activity根视图getWindowVisibleDisplayFrame矩阵高度 - activity根视图getLocationOnScreen Y坐标）
        // 在ActionBar显示的情况下计算键盘弹出高度时，会有一个状态栏高度的误差
        // getContentRoot(activity).getLocationOnScreen(location);

        // Log.d(TAG, String.format("content.getChild(0)=%s, r.height=%s, location[1]=%s, r.top=%s, content.height=%s", screenHeight, r.height(), location[1], r.top, getContentRoot(activity).getHeight()));

        return screenHeight - r.height() - r.top;
    }


    /**
     * Show keyboard and focus to given EditText
     *
     * @param editText editText
     * @param delay delay
     */
    public static void showKeyboard(final EditText editText, int delay) {
        if (null == editText)
            return;

        if (!editText.requestFocus()) {
            Log.w(TAG, "showSoftInput() can not get focus");
            return;
        }

        if (delay == 0) {
            delay = 10;
        }

        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) editText.getContext().getApplicationContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, delay);
    }

    /**
     * 隐藏软键盘 可以和{@link #showKeyboard(EditText, int)} 搭配使用，进行键盘的显示隐藏控制。
     *
     * @param view 当前页面上任意一个可用的view
     */
    public static boolean hideKeyboard(final View view) {
        if (null == view)
            return false;

        InputMethodManager im = (InputMethodManager) view.getContext().getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (im == null) {
            return false;
        }

        // 即使当前焦点不在editText，也是可以隐藏的。
        return im.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5);
    }

    public static View getActivityRoot(Activity activity) {
        return getContentRoot(activity).getChildAt(0);
    }

    private static ViewGroup getContentRoot(Activity activity) {
        return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
    }
}
