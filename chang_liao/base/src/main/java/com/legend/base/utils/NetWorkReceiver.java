package com.legend.base.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


import java.text.SimpleDateFormat;

/**
 * @author MrLiu
 * @date 2020/5/15
 * desc 广播接收者
 */
public class NetWorkReceiver extends BroadcastReceiver {
    private static long WIFI_TIME = 0;
    private static long ETHERNET_TIME = 0;
    private static long NONE_TIME = 0;
    private static int LAST_TYPE = -3;
    private static String TAG = "TAG";

    private OnNetChangeListener mListener;

    public NetWorkReceiver(OnNetChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        // 特殊注意：如果if条件生效，那么证明当前是有连接wifi或移动网络的，如果有业务逻辑最好把esle场景酌情考虑进去！
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            long time = getTime();
            if (time != WIFI_TIME && time != ETHERNET_TIME && time != NONE_TIME) {
                final int netWorkState = getNetWorkState(context);
                if (netWorkState == NETWORK_WIFI && LAST_TYPE != NETWORK_WIFI) {
                    if (mListener != null) mListener.onNetChanged(netWorkState, LAST_TYPE);
                    WIFI_TIME = time;
                    LAST_TYPE = netWorkState;
                    Log.e(TAG, "wifi：" + time);
                } else if (netWorkState == NETWORK_MOBILE && LAST_TYPE != NETWORK_MOBILE) {
                    if (mListener != null) mListener.onNetChanged(netWorkState, LAST_TYPE);
                    ETHERNET_TIME = time;
                    LAST_TYPE = netWorkState;
                    Log.e(TAG, "数据网络：" + time);
                } else if (netWorkState == NETWORK_NONE && LAST_TYPE != NETWORK_NONE) {
                    if (mListener != null) mListener.onNetChanged(netWorkState, LAST_TYPE);
                    NONE_TIME = time;
                    LAST_TYPE = netWorkState;
                    Log.e(TAG, "无网络：" + time);
                }
            } else {
                if (mListener != null) mListener.onNetChanged(NETWORK_NONE, LAST_TYPE);
                NONE_TIME = time;
                LAST_TYPE = NETWORK_NONE;
                Log.e(TAG, "无网络：" + time);
            }
        }
    }

    public long getTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String date = sDateFormat.format(new java.util.Date());
        return Long.valueOf(date);
    }

    public static final int NETWORK_NONE = -1; //无网络连接
    public static final int NETWORK_WIFI = 0; //wifi
    public static final int NETWORK_MOBILE = 1; //数据网络
    //判断网络状态与类型
    public static int getNetWorkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    public static interface OnNetChangeListener {
        void onNetChanged(int state, int lastState);
    }
}
