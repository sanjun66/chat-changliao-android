package com.legend.baseui.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.com.legend.ui.R;

import java.io.File;
import java.lang.ref.SoftReference;


/**
 * Created by cxf on 2017/8/9.
 */
public class ImgLoader {

    private static final boolean SKIP_MEMORY_CACHE = false;

    public static boolean assertValidRequest(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !isDestroy(activity);
        } else if (context instanceof ContextWrapper){
            ContextWrapper contextWrapper = (ContextWrapper) context;
            if (contextWrapper.getBaseContext() instanceof Activity){
                Activity activity = (Activity) contextWrapper.getBaseContext();
                return !isDestroy(activity);
            }
        }
        return true;
    }

    private static boolean isDestroy(Activity activity) {
        if (activity == null) {
            return true;
        }
        return activity.isFinishing() || activity.isDestroyed();
    }

    public static void display(Context context, String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(url).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void disPlayRoundedCorners(Context context, String url, ImageView imageView, int cornerPx) {
        RequestOptions options = new RequestOptions();
        options.transform(new RoundedCorners(cornerPx));
//                .placeholder(R.mipmap.bg_load_failure);
        Glide.with(context).applyDefaultRequestOptions(options).load(url).into(imageView);
    }

    public static void displayWithError(Context context, String url, ImageView imageView, int errorRes) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(url).skipMemoryCache(SKIP_MEMORY_CACHE).error(errorRes).into(new SoftReference<ImageView>(imageView).get());
    }

//    public static void displayAvatar(Context context, String url, ImageView imageView) {
//        if (context == null) {
//            return;
//        }
//        if (TextUtils.isEmpty(url)) {
//            return;
//        }
//        displayWithError(context, url, imageView, R.mipmap.icon_avatar_placeholder);
//    }

    public static void display(Context context, File file, ImageView imageView) {
        if (context == null || file == null || !file.exists()) {
            return;
        }
        Glide.with(context).asDrawable().load(file).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void display(Context context, int res, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(res).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void displayNoCache(Context context, int res, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(res).skipMemoryCache(true).into(imageView);
    }
    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, String videoPath, ImageView imageView) {
        if (context == null) {
            return;
        }

        Glide.with(context).asDrawable().load(Uri.fromFile(new File(videoPath))).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }


    /**
     * 显示视频封面缩略图
     */
    public static void displayVideoThumb(Context context, File file, ImageView imageView) {
        if (context == null) {
            return;
        }
        Glide.with(context).asDrawable().load(Uri.fromFile(file)).skipMemoryCache(SKIP_MEMORY_CACHE).into(imageView);
    }

    public static void displayDrawable(Context context, String url, final DrawableCallback callback) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(url).skipMemoryCache(SKIP_MEMORY_CACHE).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.callback(resource);
                }
            }
        });
    }


    public static void clear(Context context, ImageView imageView) {
        Glide.with(context).clear(imageView);
    }


    public static void clearMemory(Context context) {
        Glide.get(context).clearMemory();
    }

    public static void displayDrawable(Context context, String url, final DrawableCallback2 callback) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Glide.with(context).asDrawable().load(url).skipMemoryCache(SKIP_MEMORY_CACHE).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (callback != null) {
                    callback.onLoadSuccess(resource);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (callback != null) {
                    callback.onLoadFailed();
                }
            }

        });
    }
    public static void loadChatImage(final Context mContext, String imgUrl,final ImageView imageView) {
        final RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.default_img_failed)// 正在加载中的图片
                .error(R.drawable.default_img_failed); // 加载失败的图片

        Glide.with(mContext)
                .load(imgUrl) // 图片地址
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        ImageSize imageSize = BitmapUtil.getImageSize(((BitmapDrawable)resource).getBitmap() );
                        RelativeLayout.LayoutParams imageLP =(RelativeLayout.LayoutParams )(imageView.getLayoutParams());
                        imageLP.width = imageSize.getWidth();
                        imageLP.height = imageSize.getHeight();
                        imageView.setLayoutParams(imageLP);

                        Glide.with(mContext)
                                .load(resource)
                                .apply(options) // 参数
                                .into(imageView);
                    }
                });
    }

    public static void loadChatImageFl(final Context mContext, String imgUrl,final ImageView imageView) {
        final RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.default_img_failed)// 正在加载中的图片
                .error(R.drawable.default_img_failed); // 加载失败的图片

        Glide.with(mContext)
                .load(imgUrl) // 图片地址
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        ImageSize imageSize = BitmapUtil.getImageSize(((BitmapDrawable)resource).getBitmap() );
                        FrameLayout.LayoutParams imageLP =(FrameLayout.LayoutParams )(imageView.getLayoutParams());
                        int width = imageSize.getWidth();
                        int height = imageSize.getHeight();
                        if (width < height && width < 200) {
                            height = (int) (200 * height * 1.0 / width) ;
                            width = 200;
                        }
                        if (height < width && height < 200) {
                            width = (int) (200 * width * 1.0 / height);
                            height = 200;
                        }
                        imageLP.width = width;
                        imageLP.height = height;
//                        Log.i("websocket", "iamgesize = " + imageSize.getWidth() + " , " + imageSize.getHeight());
//                        Log.i("websocket", "iamgesize1 = " + width + " , " + height);
                        imageView.setLayoutParams(imageLP);

                        Glide.with(mContext)
                                .load(resource)
                                .apply(options) // 参数
                                .into(imageView);
                    }
                });
    }



    public interface DrawableCallback {
        void callback(Drawable drawable);
    }

    public interface DrawableCallback2 {
        void onLoadSuccess(Drawable drawable);

        void onLoadFailed();
    }

}
