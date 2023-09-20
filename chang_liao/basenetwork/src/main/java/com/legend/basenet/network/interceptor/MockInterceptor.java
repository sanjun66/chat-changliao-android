package com.legend.basenet.network.interceptor;

import android.util.Log;

import com.legend.basenet.network.MockConfig;
import com.legend.basenet.network.NetworkManager;

import java.io.IOException;
import java.util.Random;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    private static final String TAG = MockInterceptor.class.getSimpleName();

    private final int failurePercentage;
    private final int minDelayMilliseconds;
    private final int maxDelayMilliseconds;

    public MockInterceptor(MockConfig config) {
        this.failurePercentage = config.getFailurePercentage();
        this.minDelayMilliseconds = config.getMinDelayMilliseconds();
        this.maxDelayMilliseconds = config.getMaxDelayMilliseconds();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();

        String path = url.encodedPath();

        MockConfig mockConfig = NetworkManager.getInstance().getMockConfig();

        if (mockConfig == null) {
            return chain.proceed(request);
        }

        String responseString = mockConfig.getMockString(path);

        // 判断 assets 目录下是否包含mock文件夹，如果不包含直接走网络
        if (responseString == null) {
            return chain.proceed(request);
        }

        // 在最大最小延时间随机一个网络请求时间
        if (minDelayMilliseconds != 0 && maxDelayMilliseconds != 0
                && maxDelayMilliseconds > minDelayMilliseconds) {

            try {
                Thread.sleep(Math.abs(new Random().nextInt()
                        % (maxDelayMilliseconds - minDelayMilliseconds))
                        + minDelayMilliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 随机失败百分比，如果低于预设值，则认为请求失败
        // 如果不需要失败页 可将 failurePercentage 置为 -1
        boolean failure = Math.abs(new Random().nextInt() % 100) < failurePercentage;
        int statusCode = failure ? 504 : 200;

        Log.e(TAG, "MOCK: Returning result from " + path + "\t\tStatusCode : " + statusCode);

        if (failure) {
            responseString = "";
        }

        return new Response.Builder()
                .code(statusCode)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), responseString))
                .addHeader("content-type", "application/json")
                .build();
    }
}
