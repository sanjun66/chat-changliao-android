package com.legend.basenet.network;

import android.app.Application;

import com.legend.basenet.network.factory.RetrofitClientFactory;
import com.legend.basenet.network.factory.RuntimeTypeAdapterFactory;
import com.legend.basenet.network.listener.NetworkListener;
import com.legend.basenet.network.util.Utils;

import retrofit2.Retrofit;

public class NetworkManager {

    private static volatile NetworkManager instance = null;
    private Retrofit globalRetrofit;
//    private Retrofit rxRetrofit;
    private NetworkListener networkListener;
    private Application application;

    // network global settings
    private String baseUrl;
    private boolean debuggable;
    private MockConfig mockConfig;
    private boolean enableSSL;

    public static NetworkManager getInstance() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) {
                    instance = new NetworkManager();
                }
            }
        }
        return instance;
    }

    public void init(Application application) {
        this.application = application;

        Utils.checkNotNull(baseUrl, "baseUrl cannot be null");

        globalRetrofit = RetrofitClientFactory.newBuilder()
                .baseUrl(baseUrl)
                .debuggable(debuggable)
                .build();
//        rxRetrofit = RetrofitClientFactory.newBuilder()
//                .baseUrl(baseUrl)
//                .setCoroutineType(false)
//                .debuggable(debuggable)
//                .build();
    }

    public Application getApplication() {
        return application;
    }

    public NetworkManager setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
        return this;
    }

    public NetworkManager setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public NetworkManager setMockConfig(MockConfig mockConfig) {
        this.mockConfig = mockConfig;
        return this;
    }

    public MockConfig getMockConfig() {
        return mockConfig;
    }

    public NetworkManager setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
        return this;
    }

    public boolean isEnableSSL() {
        return enableSSL;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public NetworkManager registerListener(NetworkListener networkListener) {
        this.networkListener = networkListener;
        return this;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public NetworkListener getNetworkListener() {
        return this.networkListener;
    }

    public <T> T getService(Class<T> service) {
        Utils.checkNotNull(instance, "NetworkManager init first");
        return globalRetrofit.create(service);
    }
//    public <T> T getRxService(Class<T> service) {
//        Utils.checkNotNull(instance, "NetworkManager init first");
//
//        return rxRetrofit.create(service);
//    }

    public <T> T getRunTypeService(Class<T> service, RuntimeTypeAdapterFactory factory1) {
        Utils.checkNotNull(instance, "NetworkManager init first");
        return globalRetrofit.create(service);
    }

}
