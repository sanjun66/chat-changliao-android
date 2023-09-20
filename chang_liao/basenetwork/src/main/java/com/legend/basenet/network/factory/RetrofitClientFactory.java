package com.legend.basenet.network.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.legend.basenet.network.InterceptorManager;
import com.legend.basenet.network.interceptor.LoggerInterceptor;
import com.legend.basenet.network.util.OkHttpClientUtil;
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientFactory {

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = 30_000;
    private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 30_000;
    private static final int DEFAULT_WRITE_TIMEOUT_MILLISECONDS = 30_000;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private final Retrofit.Builder retrofitBuilder;
        private final OkHttpClient.Builder okHttpBuilder;
        /**
         * 支持协程
         */
        private boolean isCoroutine = true;
        public Builder() {
            retrofitBuilder = new Retrofit.Builder();
            okHttpBuilder = InterceptorManager.getInstance().with(new OkHttpClient.Builder());

            // 设置retrofit adapter converter
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create(buildGson()));
            // 设置okHttp默认超时时间
            okHttpBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            okHttpBuilder.readTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            okHttpBuilder.writeTimeout(DEFAULT_WRITE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
        public static Gson buildGson() {
            return new GsonBuilder()
                    .registerTypeAdapter(Integer.class, new IntegerDefaultAdapter())
                    .registerTypeAdapter(int.class, new IntegerDefaultAdapter())
                    .registerTypeAdapter(Double.class, new DoubleDefaultAdapter())
                    .registerTypeAdapter(double.class, new DoubleDefaultAdapter())
                    .registerTypeAdapter(Long.class, new LongDefaultAdapter())
                    .registerTypeAdapter(long.class, new LongDefaultAdapter())
                    .registerTypeAdapter(String.class, new StringNullAdapter())
                    .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
                    .create();
        }
        public Builder connectTimeout(int milliseconds) {
            okHttpBuilder.connectTimeout(milliseconds, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder readTimeout(int milliseconds) {
            okHttpBuilder.readTimeout(milliseconds, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder writeTimeout(int milliseconds) {
            okHttpBuilder.writeTimeout(milliseconds, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder debuggable(boolean debuggable) {
            if (debuggable) {
//                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//                okHttpBuilder.addInterceptor(httpLoggingInterceptor);
                LoggerInterceptor loggerInterceptor = new LoggerInterceptor(true);
                okHttpBuilder.addInterceptor(loggerInterceptor);
                // todo 调试用，给包的时候注释掉
//                okHttpBuilder.addInterceptor(new ChuckInterceptor(BaseApplication.INSTANCE));
            }
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            retrofitBuilder.baseUrl(baseUrl);
            return this;
        }
        public Builder setCoroutineType(boolean isCoroutine) {
            this.isCoroutine = isCoroutine;
            return this;
        }
        public Retrofit build() {
            OkHttpClient okhttpClient;
            //todo wdd后期添加
//            if (NetworkManager.getInstance().isDebuggable()) {
//                okhttpClient = OkHttpClientUtil.getTrustAllSSLClient(okHttpBuilder.build());
//            } else {
//                okhttpClient = OkHttpClientUtil.getSSLClientIgnoreExpire(okHttpBuilder.build(),
//                        NetworkManager.getInstance().getApplication(), "lego/lego_cer.pem");
//            }

            okhttpClient = OkHttpClientUtil.getDefaultSSLClient(okHttpBuilder.build());
            if (isCoroutine) {
                retrofitBuilder.addCallAdapterFactory(CoroutineCallAdapterFactory.create());
            } else {
                retrofitBuilder.addCallAdapterFactory(RxJava3CallAdapterFactory.create());
            }

            retrofitBuilder.client(okhttpClient);
            return retrofitBuilder.build();
        }
    }
}
