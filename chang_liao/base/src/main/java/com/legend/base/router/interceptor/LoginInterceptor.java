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
import com.legend.base.router.UrlUtils;
import com.legend.base.router.interfaces.BaseRouteConstants;
import com.legend.base.router.interfaces.RouterListener;

@Interceptor(priority = 1)
public class LoginInterceptor implements IInterceptor {
    public static final String TAG = "LoginInterceptor";

    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        Uri uri = postcard.getUri();

        if (uri != null && "1".equals(uri.getQueryParameter(BaseRouteConstants.PARAM_NEED_LOGIN))) {
            RouterListener routerListener = RouterManager.getInstance().getRouterListener();
            if (routerListener != null) {
                String targetUrl = "";
                if (BaseRoutes.Browser.ROUTE_BROWSER.equals(uri.getPath())) {
                    // 特殊处理 bsdlk 链接转换
                    targetUrl = uri.getQueryParameter(BaseRouteConstants.PARAM_ORIGIN_URL);
                } else {
                    targetUrl = uri.toString();
                }

                if (!routerListener.onLoginIntercept(UrlUtils.removeParam(targetUrl, BaseRouteConstants.PARAM_NEED_LOGIN))) {
                    callback.onContinue(postcard);
                    return;
                }
            }
            callback.onInterrupt(null);
        } else if (postcard.getExtras() != null && TextUtils.equals((CharSequence) postcard.getExtras().get("needLogin"), "1")) {
            RouterListener routerListener = RouterManager.getInstance().getRouterListener();
            if (routerListener != null) {
                String targetUrl = "";
                if (BaseRoutes.Browser.ROUTE_BROWSER.equals(postcard.getPath())) {
                    // 特殊处理 bsdlk 链接转换
                    targetUrl = (String) postcard.getExtras().get(BaseRouteConstants.PARAM_ORIGIN_URL);
                }

                if (!routerListener.onLoginIntercept(UrlUtils.removeParam(targetUrl, BaseRouteConstants.PARAM_NEED_LOGIN))) {
                    callback.onContinue(postcard);
                    return;
                }
            }
            callback.onInterrupt(null);
        } else {
            callback.onContinue(postcard);
        }
    }

    @Override
    public void init(Context context) {

    }
}
