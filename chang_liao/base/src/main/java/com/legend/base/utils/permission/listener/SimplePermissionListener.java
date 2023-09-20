package com.legend.base.utils.permission.listener;

public abstract class SimplePermissionListener implements PermissionListener {
    @Override
    public boolean preRequest(String[] permissions) {
        return false;
    }
}
