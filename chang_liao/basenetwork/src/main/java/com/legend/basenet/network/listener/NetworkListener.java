package com.legend.basenet.network.listener;

public interface NetworkListener {

    /**
     * token过期回调
     */
    void onTokenExpired(int code);

    /**
     * response header cmd
     */
    void onReceivedCmd(String cmd);
}
