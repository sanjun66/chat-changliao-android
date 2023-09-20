package com.legend.base.router.interceptor;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.legend.base.router.BaseRoutes;
import com.legend.base.router.RouterManager;
import com.legend.base.router.interfaces.BaseRouteConstants;
import com.legend.base.router.interfaces.RouterListener;

/**
 * 跳转下级页面后关闭当前页面及历史堆栈
 */
@Interceptor(priority =2)
public class CloseStackInterceptor implements IInterceptor {
    public static final String TAG = "CloseStackInterceptor";
    public Context context;
    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        try {
            Uri uri = postcard.getUri();
            if (uri != null) {
//                /app/browser
                String path = uri.getPath();
                String value = null;
                if (BaseRoutes.Browser.ROUTE_BROWSER.equals(path)) {
                    String url = uri.getQueryParameter(BaseRoutes.Browser.PARAM_BROWSER_URL);
                    if (!TextUtils.isEmpty(url)) {
                        Uri sUri = Uri.parse(url);
                        if (null != sUri) {
                            value = sUri.getQueryParameter(BaseRouteConstants.PARAM_NEED_STACK);
                        }
                    }
                } else {
                    value = uri.getQueryParameter(BaseRouteConstants.PARAM_NEED_STACK);
                }

                if (!TextUtils.isEmpty(value)) {
                    int count = Integer.parseInt(value);
                    RouterListener routerListener = RouterManager.getInstance().getRouterListener();
                    callback.onContinue(postcard);
                    routerListener.onClosePage(count);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        callback.onContinue(postcard);
    }

    @Override
    public void init(Context context) {

    }
}
