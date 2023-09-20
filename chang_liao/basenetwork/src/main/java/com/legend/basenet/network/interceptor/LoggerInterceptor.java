package com.legend.basenet.network.interceptor;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggerInterceptor implements Interceptor {
    private static final String TAG = "Young";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    String BASE_NETWORK = "net : ";                                    //网络请求时的Tag看日志用
    private int mDelayReuest;
    private boolean showResponse;

    public LoggerInterceptor(boolean showResponse) {
        this(showResponse, 0);
    }

    public LoggerInterceptor(boolean showResponse, int delayRequest) {
        this.showResponse = showResponse;
        this.mDelayReuest = delayRequest;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        StringBuilder builder = new StringBuilder();
        if(showResponse) builder.append("\t\n");
        builder.append(String.format("发送%s请求 %s %s",
                request.method(), request.url(), (request.body()) == null ? "" : Objects.requireNonNull(request.body()).toString()));
        RequestBody requestBody = request.body();
        if(requestBody != null) {
            if (bodyHasUnknownEncoding(request.headers())) {
                if(showResponse) builder.append("\n");
                builder.append( "--> END ")
                        .append(request.method())
                        .append(" (encoded body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if(showResponse) builder.append("\n");
                if (isPlaintext(buffer)) {
                    builder.append("请求消息: ")
                            .append(buffer.readString(charset));
                    builder.append("--> END ")
                            .append(request.method())
                            .append(" (")
                            .append(requestBody.contentLength())
                            .append("-byte body)");
                } else {
                    builder.append("--> END ")
                            .append(request.method())
                            .append(" (binary ")
                            .append(requestBody.contentLength())
                            .append("-byte body omitted)");
                }
            }
        }

        if(showResponse && mDelayReuest != 0) {
            try {
                Thread.sleep(mDelayReuest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Response response = chain.proceed(request);
        if(showResponse && mDelayReuest != 0) {
            try {
                Thread.sleep(mDelayReuest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //nanoTime()的源码注释，这个方法返回的是JVM运行的纳秒数，它只依赖与当前的jvm，并且不会出现同步的情况，所以是准确的
        //currentTimeMillis()这个方法的源码注释,这个方法返回的是当前时间的微秒数。恰恰因为返回的是微秒数，而这个值的颗粒度取决于底层的操作系统，所以就可能会很大。例如，很多操作系统的时间颗粒度是10微妙。而且这个时间又可能受NTP影响而产生微调，从而导致时间很不准确
        //收到响应的时间
        long t2 = System.nanoTime();
        ResponseBody responseBody = response.peekBody(3 * 1024 * 1024);
        if(showResponse) builder.append("\n");
        //1e6d 就是1e6  6d就是6double 就像 6f一样 所以就是 1000000
        builder.append(String.format(Locale.getDefault(), "接收响应-->: 返回json:【%s】 %.1fms",
                unicodeToCn(responseBody.string()),
                ((t2 - t1) / 1e6d)));
        String log = builder.length() > 2998 ? builder.substring(0, 500) + builder.substring(builder.length() - 1000) : builder.toString();
        if(showResponse) {
            log = log + "\t\n  ";
            Log.i(TAG,BASE_NETWORK + log);
        }
        return response;
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }

    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private static String unicodeToCn(String unicode) {
        String[] strs = unicode.split("\\\\u");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            String regex = "^[\\dabcdef]{4}(.|\\n)*";
            if(str.matches(regex)) {
                try {
                    builder.append((char) Integer.valueOf(str.substring(0, 4), 16).intValue());
                } catch (Exception e) {
                    builder.append(str.substring(0, 4));
                }
                if(str.length() > 4) builder.append(str.substring(4));
            } else {
                builder.append(str);
            }
        }
        return builder.toString();
    }

}
