package com.legend.basenet.network.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.legend.basenet.network.NetworkManager;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetUtils {
    public static final String NET_TYPE_2G = "2G";//网络2G
    public static final String NET_TYPE_3G = "3G";//网络3G
    public static final String NET_TYPE_4G = "4G";//网络4G
    public static final String NET_TYPE_WIFI = "wifi";//网络wifi
    public static final String NET_TYPE_NONE = "none";//没有类型
    public static final String NET_TYPE_UNKNOWN = "unknown";//未知类型

    /**
     * 获取网络连接方式名称
     */
    public static String getNetTypeName() {
        Context context = NetworkManager.getInstance().getApplication();

        if (context == null) {
            return NET_TYPE_NONE;
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return NET_TYPE_NONE;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            int type = networkInfo.getType();
            int subType = networkInfo.getSubtype();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return NET_TYPE_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NET_TYPE_2G;

                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NET_TYPE_3G;

                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NET_TYPE_4G;

                    default:
                        return NET_TYPE_UNKNOWN;
                }
            } else {
                return NET_TYPE_NONE;
            }
        }
        return NET_TYPE_NONE;
    }


    /**
     * ip地址
     */
    public static String getIp() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;
    }
}
