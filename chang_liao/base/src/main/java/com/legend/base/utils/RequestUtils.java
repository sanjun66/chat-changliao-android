package com.legend.base.utils;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RequestUtils {
    public static final String CHARSET_UTF_8 = "utf-8";
    public static final String CHARSET_GBK = "GBK";
    public static final String CHARSET_GB2312 = "GB2312";

    public static final String DEFAULT_CHARSET = CHARSET_UTF_8;
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";

    public static <T> T post(String url, Map<String, String> params, String charset,
                             int connectTimeout, int readTimeout, Class<T> classOfT) throws Exception {
        String result = post(url, params, charset, connectTimeout, readTimeout);
        if (TextUtils.isEmpty(result))
            return null;
        return new Gson().fromJson(result, classOfT);
    }

    /**
     * 执行HTTP POST请求。
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param charset 字符集，如UTF-8, GBK, GB2312
     * @return 响应字符串
     * @throws java.io.IOException
     */
    public static String post(String url, Map<String, String> params, String charset,
                              int connectTimeout, int readTimeout) throws Exception {
        String contentType = "application/x-www-form-urlencoded;charset=" + charset;
        String query = buildQuery(params, charset, true);
        byte[] content = {};
        if (query != null)
            content = query.getBytes(charset);
        return post(url, contentType, content, connectTimeout, readTimeout);
    }

    private static String post(String url, String contentType, byte[] content, int connectTimeout,
                               int readTimeout) throws Exception {

        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url is empty");

        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp;
        try {
            try {
                conn = getConnection(new URL(url), METHOD_POST, contentType);
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
            } catch (IOException e) {
                throw e;
            }
            try {
                out = conn.getOutputStream();
                out.write(content);
                rsp = getResponseAsString(conn);
            } catch (IOException e) {
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rsp;
    }

    public static <T> T get(String url, String contentType, int connectTimeout,
                            int readTimeout, Class<T> classOfT) throws Exception {
        String result = get(url, contentType, connectTimeout, readTimeout);
        if (TextUtils.isEmpty(result))
            return null;
        return new Gson().fromJson(result, classOfT);
    }


    /**
     * 执行HTTP GET请求。
     *
     * @param url         请求地址
     * @param contentType 请求类型
     * @return 响应字符串
     * @throws java.io.IOException
     */
    public static String get(String url, String contentType, int connectTimeout,
                             int readTimeout) throws Exception {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url is empty");

        HttpURLConnection conn;
        String rsp;
        try {
            conn = getConnection(new URL(url), METHOD_GET, contentType);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
        } catch (IOException e) {
            throw e;
        }
        try {
            rsp = getResponseAsString(conn);
        } catch (IOException e) {
            throw e;
        }
        if (conn != null) {
            conn.disconnect();
        }
        return rsp;
    }


    public static String getResponseAsString(HttpURLConnection conn) throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        InputStream es = conn.getErrorStream();
        if (es == null) {
            return getStreamAsString(conn.getInputStream(), charset);
        } else {
            String msg = getStreamAsString(es, charset);
            if (StringUtils.isStrEmpty(msg)) {
                throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
            } else {
                throw new IOException(msg);
            }
        }
    }

    private static String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringWriter writer = new StringWriter();

            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            return writer.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static String getResponseCharset(String contentType) {
        String charset = DEFAULT_CHARSET;

        if (!StringUtils.isStrEmpty(contentType)) {
            String[] params = contentType.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (!StringUtils.isStrEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    public static HttpURLConnection getConnection(URL url, String method, String contentType)
            throws IOException {
        HttpURLConnection conn = null;
        if ("https".equals(url.getProtocol())) {
            SSLContext ctx;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()},
                        new SecureRandom());
            } catch (Exception e) {
                throw new IOException(e);
            }
            HttpsURLConnection connHttps = (HttpsURLConnection) url.openConnection();
            connHttps.setSSLSocketFactory(ctx.getSocketFactory());
            connHttps.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
            conn = connHttps;
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        conn.setDoInput(true);
        if ("POST".equals(method)) {
            conn.setDoOutput(true);
        }
        conn.setRequestMethod(method);

//        conn.setRequestProperty("User-Agent", AppUtils.getAppendCustomUserAgent(Applications.getCurrent()));
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Accept-Charset", "utf-8");
        return conn;
    }

    public static String buildQuery(Map<String, String> params, String charset, boolean encode) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        List<Map.Entry<String, String>> entries = new ArrayList<>(params.entrySet());

        // 对HashMap中的key 进行排序
        Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        boolean hasParam = false;

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (StringUtils.areNotEmpty(name, value)) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }
                String valueE;
                if (encode) {
                    valueE = URLEncoder.encode(value, charset);
                } else {
                    valueE = value;
                }

                query.append(name).append("=").append(valueE);
            }
        }

        return query.toString();
    }

    private static class DefaultTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }
}
