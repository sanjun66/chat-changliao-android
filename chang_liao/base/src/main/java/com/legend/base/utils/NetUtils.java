package com.legend.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author LiuChao
 * @describe 网络相关工具
 * @date 2017/1/19
 * @contact email:450127106@qq.com
 */

public class NetUtils {
    // 手机网络类型
    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    /**
     * 获取当前网络类型
     *
     * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
     */
    public static int getNetworkType(Context context) {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (extraInfo != null && !extraInfo.isEmpty()) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                }
                else {
                    netType = NETTYPE_CMWAP;
                }
            }
        }
        else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }

    /**
     * WIFI是否连接
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        int type = getNetworkType(context);
        if (type == NETTYPE_WIFI)
            return true;
        else
            return false;
    }

    /**
     * 网络是否连接
     * @param context
     * @return
     */
    public static boolean netIsConnected(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //手机网络连接状态
        NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        //WIFI连接状态
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
//            //当前无可用的网络
//            return false;
//        }
        if (mobNetInfo != null && mobNetInfo.isConnected() || (wifiNetInfo != null && wifiNetInfo.isConnected())) return true;
        return false;
    }
}
