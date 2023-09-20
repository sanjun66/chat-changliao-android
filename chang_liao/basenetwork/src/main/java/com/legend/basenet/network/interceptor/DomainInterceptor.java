package com.legend.basenet.network.interceptor;

import android.util.Log;

import com.legend.basenet.network.NetworkManager;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DomainInterceptor implements Interceptor {
    private static final String TAG = "DomainInterceptor";
    public static final String DOMAIN_URL = "Domain-URL";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String domain = obtainDomainNameFromHeaders(request);

        if (domain == null) {
            return chain.proceed(request);
        }

        HttpUrl url = request.url();
        Request.Builder builder = request.newBuilder();
        HttpUrl.Builder urlBuilder = url.newBuilder();

        HttpUrl domainUrl = HttpUrl.parse(domain);

        if (NetworkManager.getInstance().isDebuggable()) {
            Log.d(TAG, "====================");
            Log.d(TAG, "url: " + url);
            Log.d(TAG, "path: " + url.encodedPath());
            Log.d(TAG, "encodedPathSegments: " + url.encodedPathSegments());
        }

        if (domainUrl != null) {
            HttpUrl httpUrl = urlBuilder
                    .scheme(domainUrl.scheme())
                    .host(domainUrl.host())
                    .port(domainUrl.port())
                    .build();

            if (NetworkManager.getInstance().isDebuggable()) {
                Log.d(TAG, "HttpUrl: " + httpUrl);
                Log.d(TAG, "====================");
            }

            builder.url(httpUrl);
        }

        return chain.proceed(builder.build());
    }

    private String obtainDomainNameFromHeaders(Request request) {
        List<String> headers = request.headers(DOMAIN_URL);
        if (headers.size() == 0) {
            return null;
        }
        if (headers.size() > 1) {
            throw new IllegalArgumentException("Only one Domain-URL in the headers!");
        }
        return request.header(DOMAIN_URL);
    }
}
