package com.legend.basenet.network.interceptor;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.legend.base.utils.GlobalGsonUtils;
import com.legend.basenet.network.bean.ApiResponse;
import com.legend.basenet.network.util.SoChatEncryptUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class ResponseInterceptor implements Interceptor {
    private static final String TAG = "ResponseInterceptor-网络请求";

    @Override
    public Response intercept(Chain chain) throws IOException {
        //返回request
        Request request = chain.request();
        //返回response
        Response response = chain.proceed(request);
        //isSuccessful () ; 如果代码在[200..300]中，则返回true，这意味着请求已成功接收、理解和接受。
        if (response.isSuccessful()) {
            //返回ResponseBody
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                try {
                    //获取bodyString
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE);
                    Buffer buffer = source.buffer();
                    Charset charset = Charset.forName("UTF-8");
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(charset);
                    }
                    String bodyString = buffer.clone().readString(charset);
                    JSONObject jsonObject = new JSONObject(bodyString);
                    String data = jsonObject.get("data").toString();
                    String decrypt = SoChatEncryptUtil.decrypt(data);
                    jsonObject.remove("data");
                    jsonObject.put("data", decrypt);
                    ApiResponse api = GlobalGsonUtils.fromJson(bodyString, ApiResponse.class);
                    if (decrypt.startsWith("{")) {
                        Object object = GlobalGsonUtils.fromJson(decrypt, Object.class);
                        api.data = object;
                    } else if (decrypt.startsWith("[")) {
                        List list = GlobalGsonUtils.changeGsonToList(decrypt, new TypeToken<List>(){}.getType());
                        api.data = list;

                    } else {
                        api.data = null;
                    }

                    //生成新的ResponseBody
                    ResponseBody newResponseBody = ResponseBody.create(contentType, GlobalGsonUtils.toJson(api));
                    //response
                    response = response.newBuilder().body(newResponseBody).build();

                } catch (IOException e) {
                    //如果发生异常直接返回
                    e.printStackTrace();
                    return response;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.i(TAG, "onHttpResultResponse: 响应体为空");
            }
        }
        return response;
    }

}
