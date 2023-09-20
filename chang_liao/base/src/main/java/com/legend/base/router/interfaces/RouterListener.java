package com.legend.base.router.interfaces;

public interface RouterListener {
    boolean onLoginIntercept(String targetUrl);

    void onClosePage(int count);
}
