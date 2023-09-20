package com.legend.basenet.network.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.com.legend.network.BuildConfig;
import com.legend.basenet.network.util.encode.LSBUtils;
import com.legend.basenet.network.NetworkManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttpClientUtil {

    public static final String TAG = OkHttpClientUtil.class.getSimpleName();

    private OkHttpClientUtil() {
    }

    public static OkHttpClient getSSLClientIgnoreExpire(OkHttpClient client, Context context, String assetsSSLFileName) {

        if (!NetworkManager.getInstance().isEnableSSL()) {
            return client;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(getStream(context, "lego/lego_sec.png"));
        String secKey = FileUtils.getAssetsAsString(context, "lego/lego_sec_key");
        if (bitmap == null || TextUtils.isEmpty(secKey)) {
            throw new IllegalArgumentException("security file not fount!");
        }

        String key = LSBUtils.decode(bitmap, secKey);

        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("security image parse failed!");
        }

        InputStream inputStream = new ByteArrayInputStream(key.getBytes());
        try {
            //Certificate
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            final Certificate certificate = certificateFactory.generateCertificate(inputStream);
            final String pubSub = ((X509Certificate) certificate).getSubjectDN().getName();
            final String pubIssuer = ((X509Certificate) certificate).getIssuerDN().getName();
            final BigInteger serialNumber = ((X509Certificate) certificate).getSerialNumber();

            // Log.e("sssss", "--" + pubSub);
            // Log.e("sssss", "--" + pubIssuer);
            // Log.e("sssss", "--" + serialNumber);

            // Create an SSLContext that uses our TrustManager
            final TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {

                            // 认证私钥签名生成的公钥
                            // try {
                            //     cert.verify(((X509Certificate) ca).getPublicKey());
                            // } catch (Exception e) {
                            //     e.printStackTrace();
                            // }

                            //1、判断证书是否是本地信任列表里颁发的证书
                            try {
                                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                                tmf.init((KeyStore) null);
                                for (TrustManager trustManager : tmf.getTrustManagers()) {
                                    ((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
                                }
                            } catch (Exception e) {
                                Log.e("sssss", "server--" + "验证不通过");

                                throw new CertificateException(e);
                            }
                            // 2、判断服务器证书 发布方的标识名 和 本地证书 发布方的标识名 是否一致
                            // 3、判断服务器证书 主体的标识名 和 本地证书 主体的标识名 是否一致
                            // getIssuerDN() 获取证书的 issuer（发布方的标识名）值。
                            // getSubjectDN() 获取证书的 subject（主体的标识名）值。
                            if (NetworkManager.getInstance().isDebuggable()) {
                                Log.e("sssss", "server--" + chain[0].getSubjectDN().getName());
                                Log.e("sssss", "server--" + chain[0].getIssuerDN().getName());
                            }


                            if (!chain[0].getSubjectDN().getName().equals(pubSub)) {
                                // throw new CertificateException("server's SubjectDN is not equals to client's SubjectDN");
                                throw new CertificateException("SSL verify failed [01]");
                            }
                            if (!chain[0].getIssuerDN().getName().equals(pubIssuer)) {
                                // throw new CertificateException("server's IssuerDN is not equals to client's IssuerDN");
                                throw new CertificateException("SSL verify failed [02]");
                            }

                            if (!chain[0].getSerialNumber().equals(serialNumber)) {
                                throw new CertificateException("SSL verify failed [03]");
                            }

                            try {
                                // 暂时不判断过期时间
                                // 判断证书有效时间
                                // 证书开始时间 1607558400000L Sat Dec 11 07:59:59 GMT+08:00 2021
                                // 证书过期时间 1639180799000L Thu Dec 10 08:00:00 GMT+08:00 2020
                                chain[0].checkValidity();
                            } catch (CertificateExpiredException e) {
                                // throw new CertificateExpiredException("SSL certification expired");
                            } catch (CertificateNotYetValidException e) {
                                // throw new CertificateNotYetValidException("System time is wrong");
                            }
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            //SSLContext  and SSLSocketFactory
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            //okhttpclient
            OkHttpClient.Builder builder = client.newBuilder();
            builder.sslSocketFactory(sslSocketFactory);
            return builder.build();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

    public static OkHttpClient getTrustAllSSLClient(OkHttpClient client) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = client.newBuilder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.e("verify", hostname);
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            return client;
        }
    }

    public static OkHttpClient getDefaultSSLClient(OkHttpClient client) {
        if (BuildConfig.DEBUG || NetworkManager.getInstance().isEnableSSL()) {
            return getTrustAllSSLClient(client);
        }
        return client.newBuilder().proxy(Proxy.NO_PROXY).build();
    }

    private static InputStream getStream(Context context, String assetsFileName) {
        try {
            return context.getAssets().open(assetsFileName);
        } catch (Exception var3) {
            return null;
        }
    }

    private static InputStream getStream(String certificate) {
        try {
            return new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
        } catch (Exception var3) {
            return null;
        }
    }
}