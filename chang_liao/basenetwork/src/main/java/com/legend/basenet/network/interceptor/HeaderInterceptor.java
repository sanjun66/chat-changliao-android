package com.legend.basenet.network.interceptor;

import android.text.TextUtils;
import android.util.Log;

import com.legend.base.Applications;
import com.legend.basenet.network.HttpHeaderManager;
import com.legend.basenet.network.util.NetUtils;
import com.legend.base.utils.GlobalGsonUtils;
import com.legend.base.utils.MMKVUtils;
import com.legend.basenet.network.util.SoChatEncryptUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class HeaderInterceptor implements Interceptor {

    private static final String TAG = "HeaderInterceptor";

    private  ConcurrentHashMap<String, String> headers;

    public static final String HEADER_CACHE = "header_cache";

    public HeaderInterceptor() {
        if (headers == null) {
            headers = new ConcurrentHashMap<>();
        }
        String res = MMKVUtils.INSTANCE.getString(HEADER_CACHE);
        try {
            if (!TextUtils.isEmpty(res)) {
                Map map =GlobalGsonUtils.changeGsonToMaps(res);
                if (!map.isEmpty()) {
                    headers.putAll(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        Request.Builder requestBuilder = request.newBuilder();

        processHeadersInTime();
        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, String.valueOf(headers.get(key)));
        }

        if (Applications.isSecret) {
            try {
//                Log.i("网络请求", "加密请求 request.body() = " + request.body());
                if (request.body() instanceof FormBody) {
                    Map<String, String> originMap = new HashMap<>();
                    if (request.body() != null) {
                        Buffer buffer = new Buffer();
                        request.body().writeTo(buffer);
                        FormBody body1 = (FormBody) request.body();

                        for (int i = 0; i < body1.size(); i ++) {
//                            originMap.put(body1.encodedName(i), body1.encodedValue(i));
                            originMap.put(body1.name(i), body1.value(i));
                        }
                    } else {

                    }

                    if (originMap.size() == 1 && originMap.containsKey("params_body")) {
                    } else {
                        String mapStr = new JSONObject(originMap).toString();
                        String signParams = SoChatEncryptUtil.encrypt(mapStr);
//                    Log.i("网络请求", "加密请求后 参数 = " + signParams);

                        if (signParams != null) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("params_body", signParams);
                            String encryptJson = new JSONObject(map).toString();
                            RequestBody encryptBody = RequestBody.create(
                                    MediaType.parse("application/json; charset=utf-8"),
                                    encryptJson
                            );
                            if (request.method().equals("POST")) {
                                requestBuilder.post(encryptBody);
                            } else if (request.method().equals("PUT")) {
                                requestBuilder.put(encryptBody);
                            } else if (request.method().equals("PATCH")) {
                                requestBuilder.patch(encryptBody);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Request endRequest = requestBuilder.build();
        return chain.proceed(endRequest);
    }

    private void processHeadersInTime() {
        headers.put(HttpHeaderManager.NETWORK, NetUtils.getNetTypeName());
    }

    public void addOrUpdateHeader(String key, String value) {
        headers.put(key, value);
        String update = GlobalGsonUtils.toJson(headers);
        MMKVUtils.INSTANCE.putString(HEADER_CACHE, update);
    }

    public void addOrUpdateHeaders(Map<String, String> map) {
        headers.putAll(map);
        String update = GlobalGsonUtils.toJson(headers);
        MMKVUtils.INSTANCE.putString(HEADER_CACHE, update);
    }

    public void removeHeader(String key) {
        headers.remove(key);
        String update = GlobalGsonUtils.toJson(headers);
        MMKVUtils.INSTANCE.putString(HEADER_CACHE, update);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
