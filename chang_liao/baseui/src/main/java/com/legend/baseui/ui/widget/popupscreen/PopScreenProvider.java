package com.legend.baseui.ui.widget.popupscreen;

import android.app.Activity;
import android.content.DialogInterface;
import com.legend.baseui.ui.util.UiUtils;
import java.util.ArrayList;
import java.util.List;

public class PopScreenProvider {

    private final List<IPopupScreen> popupScreenInfoList = new ArrayList<>();
    private boolean showClickArea;
    private boolean canceledOnTouchOutside;
    private boolean cancelable;

    public void setData(List<? extends IPopupScreen> list) {
        popupScreenInfoList.clear();
        popupScreenInfoList.addAll(list);
    }

    public void setShowClickArea(boolean showClickArea) {
        this.showClickArea = showClickArea;
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public void show(Activity activity) {
        if (popupScreenInfoList.size() > 0) {
            IPopupScreen info = popupScreenInfoList.remove(0);
            float aspect;
            String imageUrl = info.getImageUrl();
            int[] arr = UiUtils.parsedPixelFromUrl(imageUrl);
            int width = arr[0];
            int height = arr[1];
            if (height == 0) {
                aspect = 1;
            } else {
                aspect = 1.0f * width / height;
            }
            PopupScreenDialog popupScreenDialog = new PopupScreenDialog(activity);
            popupScreenDialog.setData(info, aspect);
            popupScreenDialog.showClickArea(showClickArea);
            popupScreenDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            popupScreenDialog.setCancelable(cancelable);
            popupScreenDialog.setListener(new PopupScreenView.OnAreaClickListener() {
                @Override
                public void onAreaClick(String gotoUrl, IPopupScreen popupScreenInfo) {
                    popupScreenDialog.dismiss();
                    if (onPopScreenListener != null) {
                        onPopScreenListener.onAreaClick(gotoUrl, popupScreenInfo);
                    }
                }
            });
            popupScreenDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    show(activity);
                }
            });
            popupScreenDialog.show();
            if (onPopScreenListener != null) {
                onPopScreenListener.onPopupShow(info);
            }
        } else {
            if (onPopScreenListener != null) {
                onPopScreenListener.onPopupScreenComplete();
            }
        }
    }

    public interface OnPopScreenListener {

        void onPopupShow(IPopupScreen info);

        /**
         * 点击区域点击回调
         * @param gotoUrl 跳转链接
         * @param popupScreenInfo 插屏弹窗信息
         */
        void onAreaClick(String gotoUrl, IPopupScreen popupScreenInfo);

        /**
         * 所有弹窗全部弹出完毕之后的回调
         */
        void onPopupScreenComplete();
    }

    private OnPopScreenListener onPopScreenListener;

    public void setOnPopScreenListener(OnPopScreenListener onPopScreenListener) {
        this.onPopScreenListener = onPopScreenListener;
    }
}
