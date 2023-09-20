package com.legend.baseui.ui.util;

import android.app.Activity;
import android.text.TextUtils;

import com.com.legend.ui.R;
import com.legend.base.utils.permission.PermissionUtils;
import com.legend.baseui.ui.widget.dialog.common.CommonDialog;
import com.legend.baseui.ui.widget.dialog.common.CommonDialogBean;
import com.legend.baseui.ui.widget.toast.ToastUtils;

public class CommonDialogUtils {
    public static void showPermissionDeniedTipsDialog(String permissionNames,  Activity activity, CommonDialog.Callback callback) {
        if (null == activity)
            return;
        if (TextUtils.isEmpty(permissionNames))
            return;
        new CommonDialog(activity, callback, CommonDialogBean.buildCommonBean(activity.getString(R.string.ui_tips)
                , String.format(activity.getString(R.string.ui_permission_deined_tip), permissionNames)
                , activity.getString(R.string.ui_cancel), activity.getString(R.string.ui_to_open))).show();
    }

    public static void showPermissionDeniedTipsDialog(String permissionNames, Activity activity) {
        showPermissionDeniedTipsDialog(permissionNames, activity, new CommonDialog.Callback() {
            @Override
            public void confirm() {
                PermissionUtils.gotoAppDetailSetting(activity);
            }

            @Override
            public void refresh() {

            }

            @Override
            public void cancel() {
                ToastUtils.show(activity.getString(R.string.ui_permission_denied));
            }

            @Override
            public void link(String url) {

            }
        });
    }
}
