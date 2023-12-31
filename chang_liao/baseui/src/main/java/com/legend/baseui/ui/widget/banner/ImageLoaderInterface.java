package com.legend.baseui.ui.widget.banner;

import android.content.Context;
import android.view.View;

import java.io.Serializable;


public interface ImageLoaderInterface<T extends View> extends Serializable {

    void displayImage(Context context, Object path, T imageView);

    T createImageView(Context context);
}
