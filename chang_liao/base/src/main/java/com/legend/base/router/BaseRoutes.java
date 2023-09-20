package com.legend.base.router;

/**
 * App Base Route
 * 每一个APP都应该有的公共路由
 */
public class BaseRoutes {

    /**
     * 约定：base WebView route，APP内部WebView【必须使用】下面两个路由及参数，才能使用RouterManager lk lks协议
     */
    public static class Browser {
        public static final String ROUTE_BROWSER = "/app/browser";
        public static final String PARAM_BROWSER_URL = "url";
        public static final String ROUTE_BROWSER_FRAGMENT = "/app/browser/fragment";
    }

}
