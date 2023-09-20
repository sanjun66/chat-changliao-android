package com.legend.base.router;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.android.arouter.utils.MapUtils;
import com.legend.base.router.ext.ActivityResultApi;
import com.legend.base.router.ext.ActivityResultApiExKt;
import com.legend.base.router.interfaces.BaseRouteConstants;
import com.legend.base.router.interfaces.IRouterService;
import com.legend.base.router.interfaces.RouterListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RouterManager {

    private static final String TAG = "RouterManager";

    private static volatile RouterManager instance = null;

    private RouterManager(){

    }

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private static String SCHEME;
    private static String SCHEME_HTTP;
    private static String SCHEME_HTTPS;
    private static String SCHEME_SERVICE;
    private static String HOST;

    private boolean debuggable;
    private boolean ignoreScheme;
    private RouterListener routerListener;

    public RouterManager setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
        return this;
    }

    public RouterManager setIgnoreScheme() {
        if (this.debuggable) {
            ignoreScheme = true;
        }
        return this;
    }

    public RouterManager setSchemeAndHost(String scheme, String host) {
        SCHEME = scheme;
        HOST = host;
        SCHEME_HTTP = scheme + "lk";
        SCHEME_HTTPS = scheme + "lks";
        SCHEME_SERVICE = scheme + "s";
        return this;
    }

    public RouterManager registerRouterListener(RouterListener routerListener) {
        this.routerListener = routerListener;
        return this;
    }

    public RouterListener getRouterListener() {
        return this.routerListener;
    }

    public void init(Application app) {
        if (debuggable) {
            ARouter.openLog(); // 开启日志
            ARouter.openDebug();
        }
        ARouter.init(app);
        ActivityResultApi.INSTANCE.init(app);
    }

    /**
     * 打开通用路由（全局路由总入口，应避免直接使用ARouter直接跳转路由）
     *
     *  App内支持scheme
     *      1. 原生页面 / 服务（工具类 功能同第4点）
     *      2. 原生WebView
     *      3. 其他第三方app scheme
     *      4. 服务（工具类）获取App参数，或执行原生能力等（这种方式iOS目前不支持）
     *
     *
     *  标准URL格式参考：
     *      http://baidu.com/main/index?id=1&page=2#abc
     *      [scheme]://[host][path]?[query]#[fragment]
     *      scheme   = http
     *      host     = baidu.com
     *      path     = /main/index
     *      query    = id=1&page=2
     *      fragment = abc
     *
     *
     *  （注意）约定：
     *      约定1：路由请求参数 query [全部] 用 [字符串] 接收
     *      约定2：RouterManager主要关注路由的分发，关注 scheme 和 host 流向分发，如：判断url跳原生页面还是原生webView等
     *            具体的query，fragment应交由拦截器以及指定页面解析，全局适用的公共参数[必须]交由拦截器处理：
     *            如：全局路由前置登录判断 needLogin=1 当且仅当 needLogin 为 1 时弹出登录页面
     *      约定3：约定scheme或者host为空时，url为不合法
     *      约定4：RouterManager目前支持的格式：
     *              a. scheme -> native page 通过路由跳转原生页面
     *                  eg. bsd://xxyp/user/login
     *              b. [scheme]lk / [scheme]lks -> native webview page 通过路由跳转原生webview
     *                  eg. bsdlk://www.baidu.com?id=111
     *              c. [scheme]s -> function 通过路由调方法
     *                  eg. bsds://xxyp/system/call 调用系统拨号页面
     *              d. 其他App scheme页面 系统/第三方App等
     *
     * 注意！！！
     * 该方法打开[bsd]路由时，既支持Activity，也支持service(IRouteService)
     *
     * @param context context
     * @param url 标准URL协议路由
     */
    public synchronized void open(Context context, String url) {
        open(context, url, -1);
    }

    public synchronized void open(Context context, String url, NavigationCallback callback) {
        open(context, url, -1,callback);
    }

    /**
     * 打开路由
     *
     * 注意！！！
     * 调用这个方法时，requestCode 若不是-1， [bsd]指令默认认为是Activity页面路由，不支持service路由
     *
     * @param context       .
     * @param url           .
     * @param requestCode   for activity result
     */
    public synchronized void open(Context context, String url, int requestCode, NavigationCallback callback) {
        if (context == null || url == null) {
            return;
        }
        try {
            // url parse
//            url = URLDecoder.decode(url, "UTF-8");
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (debuggable)
                Log.d(TAG, "uri -> " + UriUtils.toFormatString(uri));

            // url合法性校验
            if (scheme == null || host == null) {
                return;
            }

            if (scheme.equals(SCHEME)) {
                if (debuggable && !host.equals(HOST)) {
                    Toast.makeText(context, "非法指令提醒: " + url, Toast.LENGTH_LONG).show();
                }
                // navigate to native page
                if (host.equals(HOST) || ignoreScheme) {
                    navigate(context, uri, requestCode, callback);
                }
            } else if (scheme.equals(SCHEME_HTTP)
                    || scheme.equals(SCHEME_HTTPS)
                    || scheme.equals("http")
                    || scheme.equals("https")) {
                // navigate to native browser
                navigateBrowser(context, uri, requestCode);
            } else if (scheme.equals(SCHEME_SERVICE)) {
                // call router service
                navigateService(uri);
            } else {
                // other app scheme
                navigateOtherScheme(context, url);
            }
        } catch (Exception e) {
        }
    }

    public synchronized void open(Context context, String url, int requestCode) {
        open(context, url, requestCode, null);
    }
    public synchronized void open(Context context, String url,  Map<String, String> map) {
        openWithMap(context, url, map, -1, null);
    }
    public synchronized void openWithMap(Context context, String url, Map<String, String> map) {
        openWithMap(context, url, map, -1, null);
    }

    public synchronized void openWithMap(Context context, String url, Map<String, String> map, NavigationCallback callback) {
        openWithMap(context, url, map, -1, callback);
    }
    public synchronized void openWithMap(Context context, String jumpUrl, Map<String, String> map, int requestCode, NavigationCallback callback) {
        try {
            String url = jumpUrl;
            if (url.startsWith("/")) {
                url = SCHEME + "://" + HOST + url;
            }

            Uri uri = Uri.parse(url);
            if (uri != null) {
                String path = uri.getPath();
                String scheme = uri.getScheme();
                boolean isWebView = false;
                if (SCHEME_HTTP.equals(scheme)
                        || SCHEME_HTTPS.equals(scheme)
                        || "http".equals(scheme)
                        || "https".equals(scheme)) {
                    isWebView = true;
                }
                if (isWebView) {
                    path = BaseRoutes.Browser.ROUTE_BROWSER;
                }
                Postcard postcard = ARouter.getInstance().build(path);
                Bundle bundle = buildBundle(uri);
                for(Map.Entry<String, String> entry : map.entrySet()) {
                    bundle.putString(entry.getKey(), entry.getValue());
                }
                if (!bundle.isEmpty()) {
                    postcard.with(bundle);
                }
                if (isWebView) {
                    String browserUrl = url;
                    if (url.startsWith(SCHEME_HTTP)) {
                        browserUrl = url.replaceFirst(SCHEME_HTTP, "http");
                    }
                    postcard.withString(BaseRoutes.Browser.PARAM_BROWSER_URL, browserUrl);
                    // 给登录拦截器提供的参数
                    if ("1".equals(uri.getQueryParameter(BaseRouteConstants.PARAM_NEED_LOGIN))) {
                        postcard.withString(BaseRouteConstants.PARAM_ORIGIN_URL, url);
                        postcard.withString(BaseRouteConstants.PARAM_NEED_LOGIN, "1");
                    }
                }
                if (context instanceof Activity) {
                    if (callback != null) {
                        postcard.navigation((Activity) context, requestCode, callback);
                    } else {
                        postcard.navigation((Activity) context, requestCode);
                    }
                } else {
                    // 非Activity, requestCode 无效
                    if (debuggable)
                        Log.w(TAG, "context is not instance of Activity, open activity without request code");
                    postcard.navigation(context);
                }
            }
        } catch (Exception e) {

        }

    }



    /**
     * 调用路由方法
     * @param uri uri
     */
    private void navigateService(Uri uri) {
        Postcard postcard = ARouter.getInstance().build(uri);
        // parse postcard uri
        Bundle bundle = buildBundle(uri);

        // execute service function
        Object serviceObj = postcard.navigation();
        if (serviceObj instanceof IRouterService) {
            ((IRouterService) serviceObj).execute(bundle);
        }
    }

    private Bundle buildBundle(Uri uri) {
        Map<String, String> queryParameters = UriUtils.splitQueryParameters(uri);
        Bundle bundle = new Bundle();

        if (!MapUtils.isEmpty(queryParameters)) {
            // set key and value to bundle
            for (Map.Entry<String, String> kv : queryParameters.entrySet()) {
                bundle.putString(kv.getKey(), kv.getValue());
            }
        }
        return bundle;
    }

    /**
     * 跳转原生WebView
     * @param context     .
     * @param uri         .
     * @param requestCode .
     */
    private void navigateBrowser(Context context, Uri uri, int requestCode) {
        String url = uri.toString();
        // eg.
        // bsdlk -> http; bsdlks -> https
        String browserUrl = url;
        if (url.startsWith(SCHEME_HTTP)) {
            browserUrl = url.replaceFirst(SCHEME_HTTP, "http");
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(BaseRoutes.Browser.PARAM_BROWSER_URL, browserUrl);
        // 给登录拦截器提供的参数
        if ("1".equals(uri.getQueryParameter(BaseRouteConstants.PARAM_NEED_LOGIN))) {
            params.put(BaseRouteConstants.PARAM_ORIGIN_URL, url);
            params.put(BaseRouteConstants.PARAM_NEED_LOGIN, "1");
        }

        open(context, getCompletePageRoute(BaseRoutes.Browser.ROUTE_BROWSER, params), requestCode);
    }

    /**
     * 跳转第三方APP scheme
     * @param context context
     * @param uriString uriString
     */
    private void navigateOtherScheme(Context context, String uriString) {
        try {
            Intent intent = Intent.parseUri(uriString, Intent.URI_INTENT_SCHEME);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转原生页面 / 服务
     * @param context context
     * @param uri uri
     * @param requestCode for startActivityForResult
     */
    private void navigate(Context context, Uri uri, int requestCode, NavigationCallback callback) {

        // ARouter uri解析拼装参数时 只关注 path 和 query
        // 如 http://baidu.com/index?id=1 和 scheme://host/index?id=1 导航指向的是同一个路由

        Postcard postcard = ARouter.getInstance().build(uri);
        // requestCode > -1 约定认为是开启Activity
        if (requestCode >= 0) {  // Need start for result
            if (context instanceof Activity) {
                if (callback != null) {
                    postcard.navigation((Activity) context, requestCode, callback);
                } else {
                    postcard.navigation((Activity) context, requestCode);
                }
            } else {
                // 非Activity, requestCode 无效
                if (debuggable)
                    Log.w(TAG, "context is not instance of Activity, open activity without request code");
                if (callback != null) {
                    postcard.navigation(context, callback);
                } else {
                    postcard.navigation();
                }
            }
        } else {
            // 没有传入requestCode的方法支持跳转Activity页面或调用服务方法
            // 判断是 Activity 还是 service
            // 如果是 Activity 类型 执行这一行已经正常跳转页面了
            Object navigation;
            if (callback != null) {
                navigation = postcard.navigation(context, callback);
            } else {
                navigation = postcard.navigation();
            }

            // 如果是服务 调用接口方法
            if (navigation instanceof IRouterService) {
                Bundle bundle = buildBundle(uri);
                ((IRouterService) navigation).execute(bundle);
            }
        }
    }

    /**
     * 获取完整page路由（非service）
     * @param routePath ARouter 路由
     * @return base uri + router 完整路由
     */
    public static String getCompletePageRoute(String routePath) {
        return getCompletePageRoute(routePath, null);
    }

    /**
     * 传入路由及参数获取完整页面路由（非service）
     * @param routePath ARouter 路由
     * @param params    query 参数
     * @return base uri + router + query 完整路由
     */
    public static String getCompletePageRoute(String routePath, Map<String, String> params) {
        return getCompleteRoute(SCHEME, routePath, params);
    }

    /**
     * 获取完整service路由
     * @param routePath ARouter 路由
     * @return base uri + router 完整路由
     */
    public static String getCompleteServiceRoute(String routePath) {
        return getCompleteServiceRoute(routePath, null);
    }

    /**
     * 传入路由及参数获取完整service路由
     * @param routePath ARouter 路由
     * @param params    query 参数
     * @return base uri + router + query 完整路由
     */
    public static String getCompleteServiceRoute(String routePath, Map<String, String> params) {
        return getCompleteRoute(SCHEME_SERVICE, routePath, params);
    }

    /**
     * 获取完整路由
     * @param routePath ARouter 路由
     * @param params    query 参数
     * @return base uri + router + query 完整路由
     */
    private static String getCompleteRoute(String scheme, String routePath, Map<String, String> params) {
        // Uri appendPath方法会自动在host和path之间添加 "/"
        // 约定传入的routePath格式为 "/xxx/xxx" （ARouter固定path格式）
        // 这里需要把routePath首位 "/" 删除
        if (routePath != null && routePath.length() > 0 && routePath.startsWith("/")) {
            routePath = routePath.substring(1);
        }

        // baseUri + path组装
        Uri uri = Uri.withAppendedPath(Uri.parse(scheme + "://" + HOST), routePath);

        Uri.Builder builder = uri.buildUpon();

        // 追加 query 参数
        if (params != null && params.size() > 0) {
            Set<Map.Entry<String, String>> set = params.entrySet();
            for (Map.Entry<String, String> e : set) {
                builder.appendQueryParameter(e.getKey(), e.getValue());
            }
        }

        Uri resultUri = builder.build();

        return resultUri.toString();
    }

    /**
     * 获取 ARouter path 对应对象
     * @param path ARouter path
     * @return path对应的对象（activity service等）
     */
    public static Object getNavigation(String path) {
        return ARouter.getInstance().build(path).navigation();
    }




    /***************************新增路由回调************************/
    /**
     * 必须是
     * @param activity
     * @param url
     * @param resultCallback
     */
    public synchronized void open(Activity activity, String url, ActivityResultCallback<ActivityResult> resultCallback) {
        open(activity, url, null, resultCallback);
    }

    public synchronized void open(Fragment fragment, String url, ActivityResultCallback<ActivityResult> resultCallback) {
        open(fragment.requireActivity(), url, null, resultCallback);
    }

    public synchronized void open(Context context, String url, NavigationCallback navigationCallback, ActivityResultCallback<ActivityResult> resultCallback) {
        if (context == null || url == null) {
            return;
        }
        try {
            // url parse
//            url = URLDecoder.decode(url, "UTF-8");
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (debuggable)
                Log.d(TAG, "uri -> " + UriUtils.toFormatString(uri));

            // url合法性校验
            if (scheme == null || host == null) {
                return;
            }

            if (scheme.equals(SCHEME)) {
                if (debuggable && !host.equals(HOST)) {
                    Toast.makeText(context.getApplicationContext(), "非法指令提醒: " + url, Toast.LENGTH_LONG).show();
                }
                // navigate to native page
                if (host.equals(HOST) || ignoreScheme) {
                    navigateCall(context, uri, navigationCallback, resultCallback);
                }
            } else if (scheme.equals(SCHEME_HTTP)
                    || scheme.equals(SCHEME_HTTPS)
                    || scheme.equals("http")
                    || scheme.equals("https")) {
                // navigate to native browser
                navigateBrowserCall(context, uri, resultCallback);
            } else if (scheme.equals(SCHEME_SERVICE)) {
                // call router service
                navigateService(uri);
            } else {
                // other app scheme
                navigateOtherScheme(context, url);
            }
        } catch (Exception e) {
        }
    }


    private void navigateCall(Context context, Uri uri, NavigationCallback navigationCallback, ActivityResultCallback<ActivityResult> resultCallback) {

        // ARouter uri解析拼装参数时 只关注 path 和 query
        // 如 http://baidu.com/index?id=1 和 scheme://host/index?id=1 导航指向的是同一个路由

        Postcard postcard = ARouter.getInstance().build(uri);
        // requestCode > -1 约定认为是开启Activity
        if (resultCallback != null) {  // Need start for result
            if (context instanceof FragmentActivity) {
                if (navigationCallback != null) {
                    ActivityResultApiExKt.navigation(postcard, (FragmentActivity) context, navigationCallback, resultCallback );
                } else {
                    ActivityResultApiExKt.navigation(postcard, (FragmentActivity) context, resultCallback );
                }
            } else {
                // 非Activity, requestCode 无效
                if (debuggable)
                    Log.w(TAG, "context is not instance of Activity, open activity without request code");
                if (navigationCallback != null) {
                    postcard.navigation(context, navigationCallback);
                } else {
                    postcard.navigation();
                }
            }
        } else {
            // 没有传入requestCode的方法支持跳转Activity页面或调用服务方法
            // 判断是 Activity 还是 service
            // 如果是 Activity 类型 执行这一行已经正常跳转页面了
            Object navigation;
            if (navigationCallback != null) {
                navigation = postcard.navigation(context, navigationCallback);
            } else {
                navigation = postcard.navigation();
            }

            // 如果是服务 调用接口方法
            if (navigation instanceof IRouterService) {
                Bundle bundle = buildBundle(uri);
                ((IRouterService) navigation).execute(bundle);
            }
        }
    }


    private void navigateBrowserCall(Context context, Uri uri,  ActivityResultCallback<ActivityResult> resultCallback) {
        String url = uri.toString();
        // eg.
        // bsdlk -> http; bsdlks -> https
        String browserUrl = url;
        if (url.startsWith(SCHEME_HTTP)) {
            browserUrl = url.replaceFirst(SCHEME_HTTP, "http");
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(BaseRoutes.Browser.PARAM_BROWSER_URL, browserUrl);
        // 给登录拦截器提供的参数
        if ("1".equals(uri.getQueryParameter(BaseRouteConstants.PARAM_NEED_LOGIN))) {
            params.put(BaseRouteConstants.PARAM_ORIGIN_URL, url);
            params.put(BaseRouteConstants.PARAM_NEED_LOGIN, "1");
        }

        open(context, getCompletePageRoute(BaseRoutes.Browser.ROUTE_BROWSER, params), null, resultCallback);
    }


    ///////////////带参数的回调路由方法///////////////
    public synchronized void openWithMap(Activity activity, String url,  Map<String, String> map, ActivityResultCallback<ActivityResult> resultCallback) {
        openWithMap(activity, url, map, null, resultCallback);
    }

    public synchronized void openWithMap(Fragment fragment, String url, Map<String, String> map, ActivityResultCallback<ActivityResult> resultCallback) {
        openWithMap(fragment.requireActivity(), url, map, null, resultCallback);
    }


    public synchronized void openWithMap(Context context, String jumpUrl, Map<String, String> map, NavigationCallback navigationCallback, ActivityResultCallback<ActivityResult> resultCallback) {
        try {
            String url = jumpUrl;
            if (url.startsWith("/")) {
                url = SCHEME + "://" + HOST + url;
            }

            Uri uri = Uri.parse(url);
            if (uri != null) {
                String path = uri.getPath();
                String scheme = uri.getScheme();
                boolean isWebView = false;
                if (SCHEME_HTTP.equals(scheme)
                        || SCHEME_HTTPS.equals(scheme)
                        || "http".equals(scheme)
                        || "https".equals(scheme)) {
                    isWebView = true;
                }
                if (isWebView) {
                    path = BaseRoutes.Browser.ROUTE_BROWSER;
                }
                Postcard postcard = ARouter.getInstance().build(path);
                Bundle bundle = buildBundle(uri);
                for(Map.Entry<String, String> entry : map.entrySet()) {
                    bundle.putString(entry.getKey(), entry.getValue());
                }
                if (!bundle.isEmpty()) {
                    postcard.with(bundle);
                }
                if (isWebView) {
                    String browserUrl = url;
                    if (url.startsWith(SCHEME_HTTP)) {
                        browserUrl = url.replaceFirst(SCHEME_HTTP, "http");
                    }
                    postcard.withString(BaseRoutes.Browser.PARAM_BROWSER_URL, browserUrl);
                    // 给登录拦截器提供的参数
                    if ("1".equals(uri.getQueryParameter(BaseRouteConstants.PARAM_NEED_LOGIN))) {
                        postcard.withString(BaseRouteConstants.PARAM_ORIGIN_URL, url);
                        postcard.withString(BaseRouteConstants.PARAM_NEED_LOGIN, "1");
                    }
                }

                if (context instanceof FragmentActivity) {
                    if (navigationCallback != null) {
                        ActivityResultApiExKt.navigation(postcard, (FragmentActivity) context, navigationCallback, resultCallback );
                    } else {
                        ActivityResultApiExKt.navigation(postcard, (FragmentActivity) context, resultCallback );
                    }
                } else {
                    // 非Activity, requestCode 无效
                    if (debuggable)
                        Log.w(TAG, "context is not instance of Activity, open activity without request code");
                    postcard.navigation(context);
                }
            }
        } catch (Exception e) {

        }

    }




}
