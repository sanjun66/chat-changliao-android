package com.legend.baseui.ui.widget.popupscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 通用插屏控件
 * 功能：
 * 1. 可以设置插屏图片 & 跳转链接
 * 2. 可以设置任意个数任意区域的点击跳转
 */
public class PopupScreenView extends ViewGroup implements View.OnClickListener {

    private final List<IClickArea> clickAreaInfoList = new ArrayList<>();
    /**
     * 用于存储可点击区域列表的View信息
     */
    HashMap<IClickArea, View> clickableMap = new HashMap<>();

    private ImageView imageView;

    private int clickAreaColor = Color.TRANSPARENT;

    private IPopupScreen popupScreenInfo;

    public PopupScreenView(Context context) {
        this(context, null);
    }

    public PopupScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化插屏view
     *
     * @param data 插屏信息
     */
    public void init(IPopupScreen data, OnImageLoadListener onImageLoadListener) {

        this.onImageLoadListener = onImageLoadListener;
        this.popupScreenInfo = data;

        if (data == null || data.getImageUrl() == null) {
            if (onImageLoadListener != null) {
                onImageLoadListener.onLoadFailed();
            }
            return;
        }

        String imageUrl = popupScreenInfo.getImageUrl();
        String gotoUrl = data.getGotoUrl();
        List<? extends IClickArea> list = data.getClickableAreaList();
        if (list != null) {
            this.clickAreaInfoList.addAll(list);
        }

        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(getContext())
                .asBitmap()
                .load(imageUrl)
                .listener(listener)
                .into(imageView);
        imageView.setTag(String.valueOf(gotoUrl));
        imageView.setOnClickListener(this);
        addView(imageView);

        clickableMap.clear();
        for (IClickArea iClickArea : this.clickAreaInfoList) {
            View holderView = new View(getContext());
            clickableMap.put(iClickArea, holderView);
            addView(holderView);
        }
    }

    private final RequestListener<Bitmap> listener = new RequestListener<Bitmap>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                    Target<Bitmap> target, boolean isFirstResource) {
            if (onImageLoadListener != null) {
                onImageLoadListener.onLoadFailed();
            }
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                                       DataSource dataSource, boolean isFirstResource) {
            if (onImageLoadListener != null) {
                onImageLoadListener.onLoadSuccess();
            }
            return false;
        }
    };

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int width = r - l;
        int height = b - t;

        if (imageView != null) {
            imageView.layout(0, 0, width, height);
        }

        for (int i = 0; i < clickAreaInfoList.size(); i++) {
            IClickArea clickAreaInfo = clickAreaInfoList.get(i);
            View view = clickableMap.get(clickAreaInfo);

            if (view != null) {
                view.setBackgroundColor(clickAreaColor);
                view.layout(
                        (int) (width * clickAreaInfo.getStartXRatio()),
                        (int) (height * clickAreaInfo.getStartYRatio()),
                        (int) (width * clickAreaInfo.getEndXRatio()),
                        (int) (height * clickAreaInfo.getEndYRatio())
                );
                view.setTag(clickAreaInfo.getGotoUrl());
                view.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof String && onAreaClickListener != null) {
            onAreaClickListener.onAreaClick(((String) tag), popupScreenInfo);
        }
    }

    public interface OnAreaClickListener {
        void onAreaClick(String gotoUrl, IPopupScreen popupScreenInfo);
    }

    private OnAreaClickListener onAreaClickListener;

    public void setOnAreaClickListener(OnAreaClickListener onAreaClickListener) {
        this.onAreaClickListener = onAreaClickListener;
    }

    public interface OnImageLoadListener {
        void onLoadSuccess();

        void onLoadFailed();
    }

    private OnImageLoadListener onImageLoadListener;

    /**
     * 设置是否显示展示点击区域（仅供测试时使用）
     * @param show 是否显示
     */
    public void showClickArea(boolean show) {
        if (show) {
            clickAreaColor = Color.parseColor("#11000000");
        } else {
            clickAreaColor = Color.TRANSPARENT;
        }
        requestLayout();
    }
}
