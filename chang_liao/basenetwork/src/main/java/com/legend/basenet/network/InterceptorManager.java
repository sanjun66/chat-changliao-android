package com.legend.basenet.network;

import com.legend.base.Applications;
import com.legend.basenet.network.interceptor.DomainInterceptor;
import com.legend.basenet.network.interceptor.HeaderInterceptor;
import com.legend.basenet.network.interceptor.MockInterceptor;
import com.legend.basenet.network.interceptor.ResponseInterceptor;

import java.util.Map;

import okhttp3.OkHttpClient;

public class InterceptorManager {

    public HeaderInterceptor headerInterceptor;

    private static volatile InterceptorManager instance = null;

    public InterceptorManager() {
        headerInterceptor = new HeaderInterceptor();
    }

    public static InterceptorManager getInstance() {
        if (instance == null) {
            synchronized (InterceptorManager.class) {
                if (instance == null) {
                    instance = new InterceptorManager();
                }
            }
        }
        return instance;
    }

    public OkHttpClient.Builder with(OkHttpClient.Builder builder) {
        builder.addInterceptor(headerInterceptor);
        if (Applications.isSecret) builder.addInterceptor(new ResponseInterceptor());
        // builder.addInterceptor(new SignInterceptor());
        builder.addInterceptor(new DomainInterceptor());
        if (NetworkManager.getInstance().isDebuggable()
                && NetworkManager.getInstance().getMockConfig() != null) {
            builder.addInterceptor(new MockInterceptor(NetworkManager.getInstance().getMockConfig()));
        }
//        builder.addInterceptor(new ResponseHeaderInterceptor());
        return builder;
    }

    protected void addOrUpdateHeader(String key, String value) {
        if (headerInterceptor != null) {
            headerInterceptor.addOrUpdateHeader(key, value);
        }
    }

    protected void addOrUpdateHeaders(Map<String, String> map) {
        if (headerInterceptor != null) {
            headerInterceptor.addOrUpdateHeaders(map);
        }
    }

    protected void removeHeader(String key) {
        if (null != headerInterceptor) {
            headerInterceptor.removeHeader(key);
        }
    }
}
